/**
 * 
 */
package org.carnegiesciencecenter.buhl;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * @author Anh Le
 *
 */
public class TimelineWindow extends JFrame {
	JPanel pane;
	TimelineWindow() {
		super("Timeline");
		setBounds(100,100,800,400);
	    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		pane = new JPanel() {
			public void paintComponent( Graphics g ) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D)g;

            Line2D line = new Line2D.Double(10, 10, 40, 40);
            g2.setColor(Color.blue);
            g2.setStroke(new BasicStroke(10));
            g2.draw(line);
			}
		};
		this.getContentPane().add(pane);
		setVisible(true);
	}
}
