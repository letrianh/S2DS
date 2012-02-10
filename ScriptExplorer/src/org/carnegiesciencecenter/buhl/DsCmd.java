package org.carnegiesciencecenter.buhl;

import java.util.StringTokenizer;

enum DsCmdTypes {
	STOP, SPICESTOP, COMMENT, EMPTY, WAIT, NEXT, OTHER, OTHERWAIT, TIME_DEBUG
}

public class DsCmd implements Comparable<DsCmd> {
	static final int DEFAULT_DURATION = 0;
	DsCmdTypes type;
	int sectionNum;
	int timeBegin, timeDuration;
	int order;
	int superOrder;
	String waitTime;
	String wholeLine;
	String category, action;
	int currentTapeValue;
	boolean currentTapeRunning;
	
	DsCmd(int runTime, String comment, int o) {
		if (comment.length() != 0) {
			type = DsCmdTypes.COMMENT;
			timeBegin = runTime;
			action = "";
			wholeLine = comment;
			superOrder = o;
		} 
		else {
			type = DsCmdTypes.EMPTY;
			timeBegin = runTime;
			action = "";
			wholeLine = "\n";
			superOrder = o;
		}
	}
	
	DsCmd(DsCmd c) {
		type = c.type;
		sectionNum = c.sectionNum;
		timeBegin = c.timeBegin;
		timeDuration = c.timeDuration;
		waitTime = c.waitTime;
		wholeLine = c.wholeLine;
		category = c.category;
		action = c.action;
		superOrder = c.superOrder;
	}
	
	DsCmd(String line, int secNum) {
		wholeLine = line;
		line = line.trim();
		sectionNum = secNum;
		waitTime = "";
		type = DsCmdTypes.EMPTY;
		category = "";
		action = "";
		StringTokenizer st = new StringTokenizer(line, " +\t\n");
		if (!st.hasMoreTokens())
			return;
		
		if (line.startsWith(";") || line.startsWith("'")) {
			if (line.startsWith("'TIME_DEBUG")) {
				type = DsCmdTypes.TIME_DEBUG;
				return;
			}
			else
				type = DsCmdTypes.COMMENT;
		}
		else if (line.startsWith("+")) {
			waitTime = st.nextToken();
			if (st.hasMoreTokens()) 
				type = DsCmdTypes.OTHERWAIT;
			else {
				type = DsCmdTypes.WAIT;
				return;
			}
		}
		else
			type = DsCmdTypes.OTHER;
		
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
	
	void setSectionBeginTime(int v) {
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
