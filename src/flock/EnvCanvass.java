package flock;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Map.Entry;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

public class EnvCanvass extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Environment env;
	private Canvess canvess;

	public EnvCanvass(Environment env) {
		this.env = env;
		initComponents();
	}

	private void initComponents() {
		canvess = new Canvess();
		// add the component to the frame to see it!
		this.setContentPane(canvess);
		// be nice to testers..
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
	}

	class Canvess extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private int center = (int) Environment.mapsize;

		Canvess() {
			// set a preferred size for the custom panel to be the size of the environment.
			setPreferredSize(new Dimension(2 * center, 2 * center));
			setBackground(new java.awt.Color(255, 255, 255));
			setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);

			// draw coordinates
			g.drawLine(center, 0, center, 2 * center);
			g.drawLine(0, center, 2 * center, center);

			Random r = new Random();
			for (Entry<String, IParticle> entery : env.particles.entrySet()) {
				IParticle p = entery.getValue();
				r.setSeed(entery.getKey().hashCode()); // same colors for each agent
				int source_x, source_y; // agent location on canvass
				source_x = center + Math.round(p.getPosX());
				source_y = center + Math.round(p.getPosY());
				int tip_x, tip_y; // speed vector representation on canvass
				tip_x = center + Math.round(p.getPosX() + 10 * p.getVelX());
				tip_y = center + Math.round(p.getPosY() + 10 * p.getVelY());
				g.setColor(Color.black);
				g.drawLine(source_x, source_y, tip_x, tip_y); // speed vector

				g.setColor(new Color(r.nextInt(256), r.nextInt(256), r.nextInt(256)));
				g.drawOval(source_x - 1, source_y - 1, 3, 3);// agent location

				int radius = (int) Environment.maxlookdistance;// view radius
				g.drawOval(source_x - radius, source_y - radius, 2 * radius, 2 * radius);
				// daunts space
				g.drawOval(2 * center + source_x - radius, 2 * center + source_y - radius, 2 * radius, 2 * radius);
				g.drawOval(2 * center + source_x - radius, source_y - radius, 2 * radius, 2 * radius);
				g.drawOval(source_x - radius, 2 * center + source_y - radius, 2 * radius, 2 * radius);
				g.drawOval(-2 * center + source_x - radius, -2 * center + source_y - radius, 2 * radius, 2 * radius);
				g.drawOval(-2 * center + source_x - radius, source_y - radius, 2 * radius, 2 * radius);
				g.drawOval(source_x - radius, -2 * center + source_y - radius, 2 * radius, 2 * radius);
				g.drawOval(2 * center + source_x - radius, -2 * center + source_y - radius, 2 * radius, 2 * radius);
				g.drawOval(-2 * center + source_x - radius, 2 * center + source_y - radius, 2 * radius, 2 * radius);
			}
		}
	}

}
