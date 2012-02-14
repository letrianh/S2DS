package org.carnegiesciencecenter.buhl;

import java.util.StringTokenizer;

enum DsCmdTypes {
	STOP,		// DS Stop 
	SPICESTOP,	// translated from SPICE Stop
	COMMENT,	// started with ' or ;
	EMPTY,		// empty line
	WAIT,		// containing only + and wait time
	NEXT,		// Show Next
	OTHER,		// regular commands
	TIME_DEBUG,	// fake command 'TIME_DEBUG
	TIME_ADJUST	// fake command '@
}

public class DsCmd implements Comparable<DsCmd> {
	DsCmdTypes type;
	int sectionNum;					// Which section (defined by STOP) the command belongs to. Start with 0.
	int timeBegin;					// Time from the beginning of the script
	String waitTime;				// Delay time specified with leading "+". If not specified, MUST be ""
	String wholeLine;				// Original line from script file
	String category;				// Command group, for ex. Text
	String action;					// Command action, for ex. View
	int order;						// Order in which cmd was added. Used for sorting commands
	int superOrder;					// Order inherited from SPICE
	int currentTapeValue;			// DA-88 tape position when this cmd is executed
	boolean currentTapeRunning;		// Status of the tape
	boolean isNative = true;				// This cmd is from DS file or was translated from SPICE
	
	// This constructor is used only when we need to insert COMMENT or EMPTY line to the generated script
	DsCmd(int runTime, String comment, int o) {
		waitTime = "";
		timeBegin = runTime;
		action = "";
		superOrder = o;
		if (comment.length() != 0) {
			type = DsCmdTypes.COMMENT;
			wholeLine = comment;
		} 
		else {
			type = DsCmdTypes.EMPTY;
			wholeLine = "\n";
		}
	}
	
	// Copy constructor
	DsCmd(DsCmd c) {
		type = c.type;
		sectionNum = c.sectionNum;
		timeBegin = c.timeBegin;
		waitTime = c.waitTime;
		wholeLine = c.wholeLine;
		category = c.category;
		action = c.action;
		order = c.order;
		superOrder = c.superOrder;
		currentTapeValue = c.currentTapeValue;
		currentTapeRunning = c.currentTapeRunning;
	}
	
	// Standard constructor. Called when the script file is read the first time.
	// At this point, we have no idea about currentTapeValue, currentTapeRunning, or timeBegin
	DsCmd(String line, int secNum) {
		type = DsCmdTypes.EMPTY;
		sectionNum = secNum;
		waitTime = "";
		wholeLine = line;
		line = line.trim();
		category = "";
		action = "";
		StringTokenizer st = new StringTokenizer(line, " +\t\n");
		if (!st.hasMoreTokens())
			return;
		
		// check if we need delay
		if (line.startsWith("+")) {
			waitTime = st.nextToken();
			if (!st.hasMoreTokens()) { 
				type = DsCmdTypes.WAIT;
				return;
			}
		}
		
		// check if it is a comment 
		if (line.startsWith(";") || line.startsWith("'")) {
			if (line.startsWith("'TIME_DEBUG")) {
				type = DsCmdTypes.TIME_DEBUG;
				return;
			}
			else if (line.startsWith("'@")) {	// ex: '@01:23:45.67
				type = DsCmdTypes.TIME_ADJUST;
				action = line.substring(2, 13);
				return;
			}
			else
				type = DsCmdTypes.COMMENT;
		}
		else
			type = DsCmdTypes.OTHER;	// type can be changed to STOP or NEXT. See below.
		
		category = st.nextToken().toUpperCase();
		if (category.startsWith("STOP")) {
			type = DsCmdTypes.STOP;
			return;
		}
		if (st.hasMoreElements())
			action = st.nextToken().toUpperCase();
		if (category.startsWith("SHOW") && action.startsWith("NEXT")) {
			type = DsCmdTypes.NEXT;
			wholeLine = "'REMOVED:" + wholeLine;
		}
	}
	
	void addToExecTime(int v) {
		timeBegin += v;
	}

	void setExecTime(int v) {
		timeBegin = v;
	}
	
	void setOrder(int o) {
		order = o;
	}

	void setSuperOrder(int o) {
		superOrder = o;
	}

	void addToCurrentTapeValue(int v) {
		currentTapeValue += v;
	}

	void setCurrentTapeValue(int t) {
		currentTapeValue = t;
	}
	
	String removeDelay() {
		String w = this.wholeLine.trim();
		System.out.println(w);
		if (w.startsWith("+")) {
			int i=1;
			while (" .0123456789".contains(w.substring(i, i+1)))
					i++;
			w = w.substring(i);
		}
		return w;
	}

	@Override
	public int compareTo(DsCmd o) {
		if (this.superOrder != o.superOrder)
			return (this.superOrder - o.superOrder);
		else {
			if (this.timeBegin != o.timeBegin)
				return (this.timeBegin - o.timeBegin);
			else
				return (this.order - o.order);
		}
	}
}
