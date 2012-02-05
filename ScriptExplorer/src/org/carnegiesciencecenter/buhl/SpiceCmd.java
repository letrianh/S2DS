/**
 * 
 */
package org.carnegiesciencecenter.buhl;

import java.util.StringTokenizer;

/**
 * @author Anh Le
 *
 */
enum SpiceCmdTypes {
	STOP, RUN, COMMENT, EMPTY, RUNSCRIPT, TAPE_SEARCH, TAPE_PLAY, TAPE_PAUSE, SELECT_SOURCE, OTHER, UNKNOWN
}


public class SpiceCmd {
	static final int DEFAULT_WAIT = 5;
	static final int DEFAULT_DURATION = 0;
	SpiceCmdTypes type;
	int sectionNum;
	int timeBegin, timeDuration;
	String commentAbove, commentBelow;
	String timecode;
	String duration;
	String action;
	String numericParam;
	String deviceName;
	String channelName;
	String otherParams;
	String wholeLine;
	static String extraInfo = ""; // actually not a good solution
	DsCmd dsEquiv;
	
	int VSRC_positions[] = new int [8];
	
	SpiceCmd(String line, int currentSection) {
		wholeLine = line;
		commentAbove = "";
		commentBelow = "";
		duration = "";
		action = "";
		numericParam = "";
		deviceName = "";
		channelName = "";
		otherParams = "";
		sectionNum = currentSection;
		dsEquiv = null;
		
		StringTokenizer st = new StringTokenizer(line, " \t\n");
		type = SpiceCmdTypes.UNKNOWN;
		if (!st.hasMoreTokens())
			return;
		timecode = st.nextToken();
		type = SpiceCmdTypes.EMPTY;
		if (!st.hasMoreTokens())
			return;
		action = st.nextToken();
		type = SpiceCmdTypes.OTHER;
		
		if (action.startsWith("'")) { // this is a comment line
			type = SpiceCmdTypes.COMMENT;
			commentAbove = line.substring(line.indexOf("'")+1);
			action = "";
			timeDuration = 0;
			if (commentAbove.length() == 0) { // this is empty line
				type = SpiceCmdTypes.EMPTY;
				}
		}
		else {
			if ("0123456789".indexOf(action.substring(0,1)) != -1) { // starts with time parameter
				timeDuration = Integer.parseInt(action);
				duration = action;
				if (!st.hasMoreTokens())
					return;
				action = st.nextToken();
			}
			else { // other cmd with no time parameter at the beginning
				timeDuration = DEFAULT_DURATION;
				if (!st.hasMoreTokens())
					return;
			}
			// now get parameters
			if (!st.hasMoreTokens()) {
				return;
			}
			deviceName = st.nextToken();
			if ("0123456789".indexOf(deviceName.substring(0,1)) != -1) { // this is percentage/script number
				numericParam = deviceName;
				if (!st.hasMoreTokens())
					return;
				deviceName = st.nextToken();
			}
			if (!st.hasMoreTokens())
				return;
			channelName = st.nextToken();
			otherParams = "";
			while (st.hasMoreTokens())
				otherParams = otherParams.concat(st.nextToken());
			if (action.toUpperCase().startsWith("STOP"))
				type = SpiceCmdTypes.STOP;
			else if (action.toUpperCase().startsWith("RUNSCRIPT"))
				type = SpiceCmdTypes.RUNSCRIPT;
			else if (action.toUpperCase().startsWith("RUN"))
				type = SpiceCmdTypes.RUN;
			else if (action.toUpperCase().startsWith("SELECTSOURCE"))
				type = SpiceCmdTypes.SELECT_SOURCE;
			else if (action.toUpperCase().startsWith("SEARCH") && deviceName.startsWith("SRC2") && channelName.startsWith("D"))
				type = SpiceCmdTypes.TAPE_SEARCH;
			else if (action.toUpperCase().startsWith("PLAY") && deviceName.startsWith("SRC2") && channelName.startsWith("D"))
				type = SpiceCmdTypes.TAPE_PLAY;
			else if (action.toUpperCase().startsWith("STILL") && deviceName.startsWith("SRC2") && channelName.startsWith("D"))
				type = SpiceCmdTypes.TAPE_PAUSE;
		}
	}
	
	String formatComment(String cmt) {
		String tmp;
		cmt = cmt.trim();
		if (cmt == null || cmt.length() == 0)
			tmp = "";
		else
			tmp = "'\t" + cmt.replaceAll("\n", "\n'\t") + "\n";
		return tmp;
	}
	
	String formatOld() {
		return String.format("'###REMOVED: Spice Cue Exec \"%s %s %s %s %s %s\"", duration, action, numericParam, deviceName, channelName, otherParams); 
	}
	
	String formatExec() {
		return String.format("Spice Cue Exec \"%s %s %s %s %s %s\"", duration.trim(), action.trim(), numericParam.trim(), deviceName.trim(), channelName.trim(), otherParams.trim());
	}
	
	int translate() {
		dsEquiv = new DsCmd("",0);
		dsEquiv.timeBegin = this.timeBegin;
		dsEquiv.superOrder = this.sectionNum;
		if (type == SpiceCmdTypes.COMMENT) {
			dsEquiv.wholeLine = formatComment(commentAbove);
			dsEquiv.type = DsCmdTypes.COMMENT;
		}
		else if (type == SpiceCmdTypes.EMPTY) {
			dsEquiv.wholeLine = "\n";
			dsEquiv.type = DsCmdTypes.EMPTY;
		}
		else if (type == SpiceCmdTypes.STOP) {
			dsEquiv.wholeLine = "\n";
			dsEquiv.type = DsCmdTypes.SPICESTOP;
		}
		else if (type == SpiceCmdTypes.RUN) {
			dsEquiv.wholeLine = "STOP\n";
			dsEquiv.type = DsCmdTypes.STOP;
			dsEquiv.action = "STOP";
		}
		else {
			//dsEquiv.wholeLine = formatComment(commentAbove) + action + "\n" + formatComment(commentBelow);
			dsEquiv.type = DsCmdTypes.OTHER;
			
			// for VOLM, SPFX, LAMP, AMIX
			if (deviceName.toUpperCase().startsWith("VOLM") 
					|| deviceName.toUpperCase().startsWith("SPFX") 
					|| deviceName.toUpperCase().startsWith("LAMP")
					|| deviceName.toUpperCase().startsWith("AMIX")) 
				dsEquiv.wholeLine = formatExec();

			// get rid of MISC, ASKY
			else if (deviceName.toUpperCase().startsWith("MISC") ||  deviceName.toUpperCase().startsWith("ASKY")) 
				dsEquiv.wholeLine = formatOld();
			
			// for VPRJ
			else if (deviceName.toUpperCase().startsWith("VPRJ")) {
				if (action.toUpperCase().startsWith("FADE") && channelName.contains(extraInfo))
					dsEquiv.wholeLine = String.format("'###TRANSLATED: %s\n", this.wholeLine) +
							String.format("\t Text View \"clip\" %s %s 100 100 100\n", this.duration, this.numericParam);
				else
					dsEquiv.wholeLine = formatOld();
			}

			// for VSRC
			else if (deviceName.toUpperCase().startsWith("VSRC")) {
				if (action.toUpperCase().startsWith("STILL"))
					dsEquiv.wholeLine = formatOld(); 
				else if (action.toUpperCase().startsWith("SEARCH")) {
					//VSRC_positions["ABCDEFGH".indexOf(channelName)] = Integer.parseInt(numericParam);
					dsEquiv.wholeLine = String.format("'###TRANSLATED: %s\n\t Text Goto \"%s\" %s", this.wholeLine, "VSRC_"+channelName, numericParam); 
				}
				else if (action.toUpperCase().startsWith("PLAY")) {
					dsEquiv.wholeLine = String.format("'###TRANSLATED: %s\n\t Text Play", this.wholeLine); 
				}
				else
					dsEquiv.wholeLine = formatOld();
			}
			
			// for TAPE
			else if (deviceName.toUpperCase().startsWith("SRC2")) {
				if (action.toUpperCase().startsWith("PLAY"))
					dsEquiv.wholeLine = String.format("'###TRANSLATED: %s\n\t Jbox1 Play \"audio\"", this.wholeLine);
				else if (action.toUpperCase().startsWith("STILL"))
					dsEquiv.wholeLine = String.format("'###TRANSLATED: %s\n\t Jbox1 Pause \"audio\"", this.wholeLine);
			}
			
			// others
			else
				dsEquiv.wholeLine = formatExec();
		}
		return 0;
	}
	
//	SpiceCmd combine(SpiceCmd nextCmd) {
//		if (this.type == SpiceCmdTypes.COMMENT && nextCmd.type == SpiceCmdTypes.COMMENT) {
//			commentAbove = commentAbove.concat("\n").concat(nextCmd.commentAbove);
//			wholeLine = wholeLine.concat("\n").concat(nextCmd.wholeLine);
//			return this;
//		}
//		else if (this.type == SpiceCmdTypes.COMMENT && nextCmd.type != SpiceCmdTypes.COMMENT) {
//			nextCmd.commentAbove = commentAbove.concat("\n").concat(nextCmd.commentAbove);
//			nextCmd.wholeLine = wholeLine.concat("\n").concat(nextCmd.wholeLine);
//			return nextCmd;
//		}
//		else if (this.type != SpiceCmdTypes.COMMENT && nextCmd.type == SpiceCmdTypes.COMMENT) {
//			commentBelow = nextCmd.commentAbove;
//			wholeLine = wholeLine.concat("\n").concat(nextCmd.wholeLine);
//			return this;
//		}
//		else {
//			System.out.println("ERROR: cannot combine these types of commands.");
//			System.out.println("Cmd1: ".concat(this.wholeLine));
//			System.out.println("Cmd2: ".concat(nextCmd.wholeLine));
//			return null;
//		}
//	}
	
	void setExecTime(int v) {
		timeBegin = v;
	}
}
