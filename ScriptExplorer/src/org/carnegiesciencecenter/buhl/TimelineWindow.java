/**
 * 
 */
package org.carnegiesciencecenter.buhl;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * @author Anh Le
 *
 */

public class TimelineWindow extends JFrame {
	JPanel pane;
	TimelineWindow(ScriptExplorer se) {
		super("Timeline");
		setBounds(100,100,800,400);
	    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	    
		pane = new Diagram(se);
		pane.setMinimumSize(new Dimension(1000, 10000));
        pane.setPreferredSize(new Dimension(1000, 10000));
		JScrollPane spane = new JScrollPane (pane);
		
		this.getContentPane().add(spane);
		setVisible(true);
	}
}
