/**
 * 
 */
package org.carnegiesciencecenter.buhl;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

/**
 * @author Anh Le
 *
 */
enum CmdNodeTypes {
	TAPE,
	SPICE,
	DS
}

public class CmdNode {
	static int boxWidth = 10;
	static int boxHeight = 3;
	static int dotsPerTimeUnit = 1;
	static int posTAPE = 100;
	static int posSPICE = 300;
	static int posDS = 500;
	static int spaceToDurationLine = 5;
	static int spaceToNextLevel = boxWidth + 5;
	static int sectionMargin = 20;
	static int lastStop = 0;
	static int section = 0;
	static int originSPICE;
	static int originTAPE;		// y coordinate for startTAPE
	static int startTAPE = 0;	// tape value of nearest Play
	static int lastTAPE = -1;
	static Font FONT = new Font("Arial", Font.PLAIN, 12);
	   	
	int x,y;
	int level;
	
	Color highlightColor;
	
	String absTimeStamp;
	String tapeValue;
	String label;
	int lineNumber;
	String text;
	CmdNodeTypes type;
	int delayTime;
	int execTime;
	int runTime;

	String legend;
	CmdNode pointTo;
	//CmdNode pointFrom;
	CmdNode previousLevelNode;
	CmdNode nextLevelNode;
	
	CmdNode(int execT, int tapeVal) {
		this(0, CmdNodeTypes.TAPE, execT, 0, 0);
		setTapeValue(SpiceScript.timeString(tapeVal));
		lastTAPE = this.y;
	}
	
	CmdNode(int lineNum, SpiceCmd c) {
		this(lineNum, CmdNodeTypes.SPICE, c.timeBegin, c.timeDelay, c.timeDuration);
		setTapeValue(SpiceScript.timeString(c.currentTapeValue));
		if (c.type == SpiceCmdTypes.TAPE_PLAY) {
			startTAPE = SpiceScript.timeValue(tapeValue);
			originTAPE = this.y;
			lastTAPE = this.y;
		}
		else if (c.type == SpiceCmdTypes.TAPE_PAUSE) {
			lastTAPE = -1;
		}
	}
	
	CmdNode(int lineNum, CmdNodeTypes t, int execT, int delayT, int runT) {
		lineNumber = lineNum;
		type = t;
		execTime = execT;
		delayTime = delayT;
		runTime = runT;
		legend = "";
		tapeValue = "00:00:00.00";
		level = 0;
		absTimeStamp = SpiceScript.timeString(execT);
		
		if (t == CmdNodeTypes.TAPE) {
			x = posTAPE;
			label = tapeValue;
			runTime = 0;
			delayTime = 0;
		} 
		else if (t == CmdNodeTypes.SPICE) {
			x = posSPICE + level * spaceToNextLevel;
			y = originSPICE + execTime * dotsPerTimeUnit;
			label = String.format("%6d", lineNumber);
			if (y > lastStop)
				lastStop = y;
		}
		else if (t == CmdNodeTypes.DS) {
			x = posDS + level * spaceToNextLevel;
			y = originSPICE + execTime * dotsPerTimeUnit;
			label = String.format("%6d", lineNumber);
		}
	}
	
	String getNodeId() {
		return String.format("%03d-%03d-%s", section, type.ordinal(), absTimeStamp);
	}
	
	void setPreviousLevelNode(CmdNode pLevelNode) {
		previousLevelNode = pLevelNode;
		if (pLevelNode != null)
			level = pLevelNode.level + 1;
		else
			level = 0;
	}
	
	void setPointTo(CmdNode to) {
		pointTo = to;
	}
	
	void setTapeValue(String tape) {
		tapeValue = tape;
		if (type == CmdNodeTypes.TAPE) {
			y = originTAPE + (SpiceScript.timeValue(tapeValue) - startTAPE) * dotsPerTimeUnit;
		}
	}
	
	void setOriginTAPE(int o) {
		originTAPE = o;
	}
	
	void setStartTAPE(int o) {
		startTAPE = o;
	}
	
	void setLegend(String l) {
		legend = l;
	}
	
	static void newSection() {
		originSPICE = lastStop + sectionMargin;
	}
	
	void draw(Graphics2D g2, boolean highlight) {
		Color cl; 
		if (highlight)
			cl = highlightColor;
		else {
			if (type == CmdNodeTypes.TAPE) {
				cl = Color.RED;
			} 
			else if (type == CmdNodeTypes.SPICE) {
				cl = Color.MAGENTA;
			}
			else if (type == CmdNodeTypes.DS) {
				cl = Color.BLUE;
			}
			else
				cl = highlightColor;
		}
		if (delayTime > 0) {
			Line2D line = new Line2D.Double(x, y, x, y - delayTime * dotsPerTimeUnit);
		    g2.setPaint(Color.DARK_GRAY);
			g2.draw(line);
		}
		if (runTime > 0) {
			Line2D line = new Line2D.Double(x + spaceToDurationLine, y, x + spaceToDurationLine, y + runTime * dotsPerTimeUnit);
		    g2.setPaint(cl);
			g2.draw(line);
		}
		if (type == CmdNodeTypes.TAPE) {
			g2.setFont(FONT);
		    FontRenderContext frc = g2.getFontRenderContext();
		    Rectangle2D textBound = FONT.getStringBounds(tapeValue, frc);
		    int w2 = (int) textBound.getWidth()/2;
		    int h2 = (int) textBound.getHeight()/2;
		    g2.setPaint(Color.BLACK);
		    g2.drawString(tapeValue, 0, y + h2);
	    	Line2D line = new Line2D.Double(x - boxWidth/2, y, x + boxWidth/2, y);
	    	g2.draw(line);
	    	if (lastTAPE != -1) {
		    	Line2D line2 = new Line2D.Double(x, y, x, lastTAPE);
		    	g2.draw(line2);
	    	}
		}
		else {
		    g2.setPaint(cl);
		    g2.drawRect(x - boxWidth/2, y - boxHeight/2, boxWidth, boxHeight);
		}
	    if (pointTo != null) {
	    	Line2D line = new Line2D.Double(x, y, pointTo.x, pointTo.y);
	    	g2.draw(line);
	    	//Stroke oldStroke = g2.getStroke();
	    	//g2.setStroke(new BasicStroke(10));
	    }
	}
}
