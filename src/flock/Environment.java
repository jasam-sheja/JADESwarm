package flock;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class Environment extends Agent {

	private static final long serialVersionUID = 7752306237656421927L;
	public static final float mapsize = 500;
	public static final float maxspeed = 1;
	public static final float dt = 0.001f;
	public static final float maxlookdistance = mapsize / 3;

	/*
	 * keep the magnitude of speed vector under `maxspeed`
	 */
	public static final void BoundSpeed(IParticle particle) {
		float speed = (float) Math.sqrt(Math.pow(particle.getVelX(), 2) + Math.pow(particle.getVelY(), 2));
		if (speed > maxspeed) {
			float factor = maxspeed / speed;
			particle.setVelX(factor * particle.getVelX());
			particle.setVelY(factor * particle.getVelY());
		}
	}

	/*
	 * keep the magnitude of speed change vector under some limit
	 */
	public static final void BoundDeltaSpeed(IParticle dspeedvector, float maxdeltaspeed) {
		float dspeed = (float) Math.sqrt(Math.pow(dspeedvector.getVelX(), 2) + Math.pow(dspeedvector.getVelY(), 2));
		if (dspeed > maxdeltaspeed) {
			float factor = maxdeltaspeed / dspeed;
			dspeedvector.setVelX(factor * dspeedvector.getVelX());
			dspeedvector.setVelY(factor * dspeedvector.getVelY());
		}
	}

	protected HashMap<String, IParticle> particles = new HashMap<String, IParticle>();
	protected HashMap<String, IParticle> particlesRelPos = new HashMap<String, IParticle>();

	protected EnvCanvass frame;

	@Override
	protected void setup() {
		// visualization
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});

		addBehaviour(new JoinBehaviour());
		addBehaviour(new LookAroundBehaviour());
		addBehaviour(new ChangeCourseBehaviour());
		addBehaviour(new TimePassBehaviour());
		super.setup();
	}

	private void createAndShowGUI() {
		// Create and set up the window.
		frame = new EnvCanvass(this);

		// Display the window.
		frame.setVisible(true);
	}

	/*
	 * this should check that the particles don't leave the environment
	 */
	protected void BoundaryConstrains() {
		for (IParticle p : particles.values()) {
			if (Math.abs(p.getPosX()) > mapsize)
				p.setPosX(-p.getPosX());
			if (Math.abs(p.getPosY()) > mapsize)
				p.setPosY(-p.getPosY());
		}
	}

	/*
	 * returns p1-p2 under torus coordinates
	 */
	private IParticle getRelPos(IParticle p1, IParticle p2) {
		float size = mapsize * 2;
		float rel_x = (p1.getPosX() + size) % size - (p2.getPosX() + size) % size;
		float rel_y = (p1.getPosY() + size) % size - (p2.getPosY() + size) % size;
		return new Particle(rel_x, rel_y, 0, 0);
	}

	/*
	 * update system after dt of time has passed
	 */
	public class TimePassBehaviour extends CyclicBehaviour {

		private static final long serialVersionUID = 1L;

		@Override
		public void action() {
			// update position
			for (IParticle p : particles.values()) {
				p.setPosX(p.getPosX() + p.getVelX() * dt);
				p.setPosY(p.getPosY() + p.getVelY() * dt);
			}
			BoundaryConstrains();

			// update visualization
			if (frame == null) {
//				System.err.println("frame is null");
			} else {
				frame.repaint();
			}

			// update relative positions
			for (Entry<String, IParticle> entry1 : particles.entrySet()) {
				for (Entry<String, IParticle> entry2 : particles.entrySet()) {
					String pairkey = entry1.getKey() + entry2.getKey();
					particlesRelPos.put(pairkey, getRelPos(entry1.getValue(), entry2.getValue()));
				}
			}
		}
	}

	/*
	 * add new agent and give them a random position and speed
	 */
	public class JoinBehaviour extends CyclicBehaviour {

		private static final long serialVersionUID = 1L;
		MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
				MessageTemplate.MatchContent("join"));

		@Override
		public void action() {
			ACLMessage msg = receive(template);
			if (msg != null) {
				System.out.println(String.format("join flock %s", msg.getConversationId()));
				Random r = new Random();
				Particle particle = new Particle(mapsize * r.nextFloat(), mapsize * r.nextFloat(),
						maxspeed * r.nextFloat(), maxspeed * r.nextFloat());
				BoundSpeed(particle);

				particles.put(msg.getConversationId(), particle);
				// update relative position
				for (Entry<String, IParticle> entry : particles.entrySet()) {
					String pairkey;
					pairkey = entry.getKey() + msg.getConversationId();
					particlesRelPos.put(pairkey, getRelPos(entry.getValue(), particle));

					pairkey = msg.getConversationId() + entry.getKey();
					particlesRelPos.put(pairkey, getRelPos(particle, entry.getValue()));
				}

				ACLMessage reply = msg.createReply();
				reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
				reply.setContent("particle");
				try {
					reply.setContentObject(particle);
				} catch (IOException e) {
					e.printStackTrace();
				}
				send(reply);
			}
		}

	}

	/*
	 * simulate an agent looking in the environment the agent will request to know
	 * relative position of other agents close by
	 */
	public class LookAroundBehaviour extends CyclicBehaviour {

		private static final long serialVersionUID = 1L;

		MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.QUERY_REF),
				MessageTemplate.MatchContent("where are my neighbors?"));

		@Override
		public void action() {
			ACLMessage msg = receive(template);
			if (msg != null) {
				List<IParticle> reply_content = new LinkedList<IParticle>();
				for (String key : particles.keySet()) {
					if (key.equals(msg.getConversationId()))
						continue;
					String pairkey = key + msg.getConversationId();
					IParticle relpos = particlesRelPos.get(pairkey);
					float distance = (float) Math.pow(relpos.getPosX(), 2) + (float) Math.pow(relpos.getPosY(), 2);
					if (distance < Math.pow(maxlookdistance, 2)) {
						reply_content.add(relpos);
					}
				}

				ACLMessage reply = msg.createReply();
				reply.setPerformative(ACLMessage.INFORM);
				reply.setLanguage("LinkedList<IParticle>");
				try {
					reply.setContentObject((Serializable) reply_content);
				} catch (IOException e) {
					e.printStackTrace();
				}
				send(reply);
			}
		}

	}

	/*
	 * an agent wants to change its direction
	 */
	public class ChangeCourseBehaviour extends CyclicBehaviour {

		public static final float maxdeltaspeed = 1f * dt;

		private static final long serialVersionUID = 1L;

		MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
				MessageTemplate.MatchLanguage("IParticle"));

		@Override
		public void action() {
			ACLMessage msg = receive(template);
			if (msg != null) {
				try {
					IParticle dspeed = (IParticle) msg.getContentObject();
					Environment.BoundDeltaSpeed(dspeed, maxdeltaspeed); // angular resistance
					IParticle agent = particles.get(msg.getConversationId());
					agent.setVelX(agent.getVelX() + dspeed.getVelX());
					agent.setVelY(agent.getVelY() + dspeed.getVelY());
					BoundSpeed(agent);
				} catch (UnreadableException e) {
					e.printStackTrace();
				}
			}

		}

	}
}
