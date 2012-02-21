/**
 * 
 */
package org.carnegiesciencecenter.buhl;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Iterator;

import javax.swing.JPanel;

/**
 * @author Anh Le
 *
 */
public class Diagram extends JPanel {

	ScriptExplorer se;
	
	Diagram(ScriptExplorer se_) {
		super();
		se = se_;
	}
	
	public void paintComponent( Graphics g ) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;

        //Line2D line = new Line2D.Double(10, 10, 40, 40);
        //g2.setColor(Color.blue);
        //g2.setStroke(new BasicStroke(10));
        //g2.draw(line);
        
        
        Iterator it = se.allNodes.keySet().iterator();        
        while(it.hasNext()){        
            se.allNodes.get(it.next()).draw(g2, false);
        }        
	}

}
