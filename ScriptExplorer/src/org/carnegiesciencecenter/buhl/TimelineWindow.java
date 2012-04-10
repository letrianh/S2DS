/**
 * 
 */
package org.carnegiesciencecenter.buhl;

import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

/**
 * @author Anh Le
 *
 */

public class TimelineWindow extends JFrame {
	JPanel pane;
	TimelineWindow(ScriptExplorer se) {
		super("Timeline");
		setBounds(100,100,800,400);
	    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	    
		pane = new Diagram(se);
		pane.setMinimumSize(new Dimension(1000, 10000));
        pane.setPreferredSize(new Dimension(1000, 10000));
		JScrollPane spane = new JScrollPane (pane);
		
		this.getContentPane().add(spane);
		setVisible(true);
	}
}
