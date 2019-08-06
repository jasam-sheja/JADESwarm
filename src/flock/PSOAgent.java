package flock;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class PSOAgent extends ParticleAgent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1394404321145361579L;
	Random random;

	@Override
	protected void setup() {
		super.setup();
		addBehaviour(new LookAround());
		random = new Random(conversationId.hashCode() + System.currentTimeMillis());
	}

	class LookAround extends CyclicBehaviour {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		// holds the relative distance from this agent
		private List<IParticle> neighbors = new LinkedList<IParticle>();

		/**
		 * update location of neighboring agents
		 */
		private void lookAround() {
			ACLMessage query = new ACLMessage(ACLMessage.QUERY_REF);
			query.setContent("where are my neighbors?");
			query.addReceiver(new AID("ENV", AID.ISLOCALNAME));
			query.setConversationId(conversationId);
			send(query);

			MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchConversationId(conversationId),
					MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
							MessageTemplate.MatchLanguage("LinkedList<IParticle>")));
			ACLMessage env_reply = blockingReceive(template, 1000);
			try {
				neighbors = (List<IParticle>) env_reply.getContentObject();
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
		}

		private float Rayleigh(float sigma) {
			// https://en.wikipedia.org/wiki/Rayleigh_distribution
			float x = 10 * random.nextFloat();
			return (float) (x / Math.pow(sigma, 2) * Math.exp(-Math.pow(x, 2) / 2 / Math.pow(sigma, 2)));
		}

		private void changeCourse() {
			Particle deltaspeed = new Particle(0, 0, 0, 0);

			// consider to go towards far agents
			float mean_x = 0, mean_y = 0;
			for (IParticle p : neighbors) {
				float distance = (float) (Math.pow(p.getPosX(), 2) + Math.pow(p.getPosY(), 2));
				if (distance > Math.pow(Environment.maxlookdistance / 2, 2)) {
					mean_x += p.getPosX();
					mean_y += p.getPosY();
				}
			}

			if (neighbors.isEmpty() || (mean_x == 0 && mean_y == 0)) {
				// random walk
				deltaspeed = new Particle(0, 0, (Rayleigh(0.5f) * (float) Math.pow(-1, random.nextInt(2))),
						(Rayleigh(0.5f) * (float) Math.pow(-1, random.nextInt(2))));
			} else {
				mean_x /= neighbors.size();
				mean_y /= neighbors.size();

				deltaspeed = new Particle(0, 0, mean_x, mean_y);
			}

			if (deltaspeed.getVelX() == 0 && deltaspeed.getVelY() == 0)
				return; // nothing to update !!

			Environment.BoundDeltaSpeed(deltaspeed, 0.1f);
			deltaspeed.setVelX(deltaspeed.getVelX() * random.nextFloat());
			deltaspeed.setVelY(deltaspeed.getVelY() * random.nextFloat());

			// send decision
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.setLanguage("IParticle");
			msg.addReceiver(new AID("ENV", AID.ISLOCALNAME));
			msg.setConversationId(conversationId);
			try {
				msg.setContentObject(deltaspeed);
			} catch (IOException e) {
				e.printStackTrace();
			}
			send(msg);
		}

		@Override
		public void action() {
			lookAround();
			changeCourse();
		}

	}

}
