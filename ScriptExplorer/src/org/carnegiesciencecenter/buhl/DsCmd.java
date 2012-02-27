package org.carnegiesciencecenter.buhl;

import java.util.StringTokenizer;

enum DsCmdTypes {
	STOP,		// DS Stop 
	SPICESTOP,	// translated from SPICE Stop
	COMMENT,	// started with ' or ;
	COMMENT_ABS,// comment with absolute time stamp
	EMPTY,		// empty line
	WAIT,		// containing only + and wait time
	NEXT,		// Show Next
	OTHER,		// regular commands
	TIME_DEBUG,	// fake command 'TIME_DEBUG
	TIME_ADJUST,// fake command '#
	TIME_FLEX	// fake command 'TIME_FLEX
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
	int currentTapeAudioDiff;		// Time difference between timecode and audio file. NEED for TIME_ADJUST
	boolean currentTapeRunning;		// Status of the tape
	boolean isNative = true;		// This cmd is from DS file or was translated from SPICE
	
	static int GuardTime = 10;			// Adjust Text Add and Text Remove time
	
	// This constructor is used only when we need to insert COMMENT or EMPTY line to the generated script
	DsCmd(int runTime, String comment, int o) {
		waitTime = "";
		timeBegin = runTime;
		action = "";
		category = "";
		superOrder = o;
		currentTapeValue = -1;	// this is IMPORTANT to indicate that this is a DS-native cmd
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
		currentTapeAudioDiff = c.currentTapeAudioDiff;
	}
	
	// Use this constructor to manually create a DS cmd
	DsCmd(int num, int time, String cat, String ac, DsCmdTypes t, String line) {
		type = t;
		superOrder = num;
		timeBegin = time;
		waitTime = "";
		wholeLine = line;
		category = cat;
		action = ac;
		isNative = false;
		currentTapeValue = -1;
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
			else if (line.startsWith("'#")) {	// ex: '#01:23:45.67
				type = DsCmdTypes.TIME_ADJUST;
				action = "01" + line.substring(4, 13);
				return;
			}
			else if (line.startsWith("'TIME_FLEX")) {	// ex: 'TIME_FLEX
				type = DsCmdTypes.TIME_FLEX;
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
	
	static public String formatView(String name, double T, double N) {
		return String.format("Text View \"%s\" %.2f %.2f 100 100 100\n", name, T, N); 
	}

	static public String formatLocate(String name, double T, double A, double E, double R, double W, double H) {
		return String.format("Text Locate \"%s\" %.2f %.2f %.2f %.2f %.2f %.2f\n", name, T, A, E, R, W, H); 
	}

	static public String formatAddImage(String name, String fileName, double W, double H, double M, double XO, double YO) {
		return String.format("Text Add \"%s\" %s %.2f %.2f \"local\" %.2f %.2f %.2f %.2f %.2f\n", name, fileName, W, H, M, XO, YO, 0.0, 0.0); 
	}

	static public String formatAddImage(String name, String fileName, double W, double H, double M) {
		return String.format("Text Add \"%s\" %s %.2f %.2f \"local\" %.2f %.2f %.2f %.2f %.2f\n", name, fileName, W, H, M, 0.0, 0.0, 0.0, 0.0); 
	}

	static public String formatAddImage(String name, String fileName) {
		return String.format("Text Add \"%s\" %s %.2f %.2f \"local\" %.2f %.2f %.2f %.2f %.2f\n", name, fileName, 0.0, 0.0, 90.0, 0.0, 0.0, 0.0, 0.0); 
	}

	static public String formatRemove(String name) {
		return String.format("Text Remove \"%s\"", name); 
	}

	static public String formatPlay(String name) {
		return String.format("Text Play \"%s\"", name); 
	}

	static public String formatPause(String name) {
		return String.format("Text Pause \"%s\"", name); 
	}

	static public String formatGoto(String name, double T) {
		return String.format("Text Goto \"%s\" %.2f", name, ((double)T)/100); 
	}

	static public String formatCueExec(String oldCue) {
		return String.format("Spice Cue Exec \"%s\"", oldCue); 
	}
	
	static public String formatJboxAdd(String fileName) {
		return String.format("Jbox1 Add \"audio\" \"%s\"", fileName); 
	}
	
	static public String formatJboxVol(int percent) {
		return String.format("Jbox1 Volume \"audio\" 0 %d", percent); 
	}
	
	static public String formatJboxGoto(double position) {
		return String.format("Jbox1 Goto \"audio\" %.2f", position); 
	}
	
	static public String formatJboxPlay() {
		return String.format("Jbox1 Play \"audio\""); 
	}
	

	static public DsCmd cmdView(int num, int time, String name, double T, double N) {
		return new DsCmd(num, time, "TEXT", "VIEW", DsCmdTypes.OTHER, formatView(name, T, N));
	}

	static public DsCmd cmdLocate(int num, int time, String name, double T, double A, double E, double R, double W, double H) {
		return new DsCmd(num, time, "TEXT", "LOCATE", DsCmdTypes.OTHER, formatLocate(name, T, A, E, R, W, H));
	}

	static public DsCmd cmdAddImage(int num, int time, String name, String fileName, double W, double H, double M, double XO, double YO) {
		return new DsCmd(num, time-GuardTime, "TEXT", "ADD", DsCmdTypes.OTHER, formatAddImage(name, fileName, W, H, M, XO, YO));
	}

	static public DsCmd cmdAddImage(int num, int time, String name, String fileName, double W, double H, double M) {
		return new DsCmd(num, time-GuardTime, "TEXT", "ADD", DsCmdTypes.OTHER, formatAddImage(name, fileName, W, H, M));
	}

	static public DsCmd cmdAddImage(int num, int time, String name, String fileName) {
		return new DsCmd(num, time-GuardTime, "TEXT", "ADD", DsCmdTypes.OTHER, formatAddImage(name, fileName));
	}

	static public DsCmd cmdRemove(int num, int time, String name) {
		return new DsCmd(num, time+GuardTime, "TEXT", "REMOVE", DsCmdTypes.OTHER, formatRemove(name));
	}

	static public DsCmd cmdPlay(int num, int time, String name) {
		return new DsCmd(num, time, "TEXT", "PLAY", DsCmdTypes.OTHER, formatPlay(name));
	}

	static public DsCmd cmdPause(int num, int time, String name) {
		return new DsCmd(num, time, "TEXT", "PAUSE", DsCmdTypes.OTHER, formatPause(name));
	}

	static public DsCmd cmdGoto(int num, int time, String name, double T) {
		return new DsCmd(num, time, "TEXT", "GOTO", DsCmdTypes.OTHER, formatGoto(name, T));
	}

	static public DsCmd cmdCueExec(int num, int time, String oldCue) {
		return new DsCmd(num, time, "SPICE", "CUE", DsCmdTypes.OTHER, formatCueExec(oldCue));
	}

	static public DsCmd cmdJboxAdd(int num, int time, String fileName) {
		return new DsCmd(num, time, "JBOX1", "ADD", DsCmdTypes.OTHER, formatJboxAdd(fileName));
	}

	static public DsCmd cmdJboxVol(int num, int time, int percent) {
		return new DsCmd(num, time, "JBOX1", "VOLUME", DsCmdTypes.OTHER, formatJboxVol(percent));
	}

	static public DsCmd cmdJboxGoto(int num, int time, double position) {
		return new DsCmd(num, time, "JBOX1", "GOTO", DsCmdTypes.OTHER, formatJboxGoto(position));
	}

	static public DsCmd cmdJboxPlay(int num, int time) {
		return new DsCmd(num, time, "JBOX1", "GOTO", DsCmdTypes.OTHER, formatJboxPlay());
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
