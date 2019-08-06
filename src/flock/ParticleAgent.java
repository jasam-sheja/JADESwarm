package flock;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ParticleAgent extends Agent {

	private static final long serialVersionUID = -1719985819865167435L;

	protected IParticle particle;
	protected String conversationId; // ID of the agent and its conversation

	@Override
	protected void setup() {
		super.setup();

		conversationId = ("" + System.identityHashCode(this)) + System.currentTimeMillis();

		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.setContent("join");
		msg.addReceiver(new AID("ENV", AID.ISLOCALNAME));
		msg.setConversationId(conversationId);
		send(msg);

		// join environment
		addBehaviour(new OneShotBehaviour() {

			private static final long serialVersionUID = 1L;

			@Override
			public void action() {
				MessageTemplate template = MessageTemplate.MatchConversationId(conversationId);
				ACLMessage env_reply = blockingReceive(template);
				try {
					particle = (IParticle) (env_reply.getContentObject());
					System.out.println(String.format("recieve particle: %s", particle.toString()));
				} catch (UnreadableException e) {
					e.printStackTrace();
				}
			}
		});
	}

}
