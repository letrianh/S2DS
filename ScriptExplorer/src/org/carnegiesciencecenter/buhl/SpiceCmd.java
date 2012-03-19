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
	STOP, 
	RUN, 
	COMMENT, 
	COMMENT_ABS, 
	EMPTY, 
	RUNSCRIPT, 
	TAPE_SEARCH, 
	TAPE_PLAY, 
	TAPE_PAUSE, 
	TAPE_JUMP, 
	AUDIO_JUMP, 
	OTHER, 
	TIME_DEBUG, 
	UNKNOWN
}


public class SpiceCmd {
	static final int DEFAULT_WAIT = 5;
	static final int DEFAULT_DURATION = 0;
	SpiceCmdTypes type;
	int sectionNum;
	int timeBegin, timeDuration;
	String commentAbove, commentBelow;
	int timeDelay;
	String timecode;
	String duration;
	String action;
	String numericParam;
	String deviceName;
	String channelNames;
	String otherParams;
	String wholeLine;
	DsCmd dsEquiv;
	int currentTapeValue = 0;
	boolean currentTapeRunning = false;
	int currentTapeAudioDiff = 0;

	int VSRC_positions[] = new int [8];
	
	SpiceCmd(String line, int currentSection) {
		wholeLine = line;
		commentAbove = "";
		commentBelow = "";
		duration = "";
		action = "";
		numericParam = "";
		deviceName = "";
		channelNames = "";
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
		if (action.toUpperCase().startsWith("STOP")) {
			type = SpiceCmdTypes.STOP;
			return;
		}
		else if (action.startsWith("'TIME_DEBUG")) {
			type = SpiceCmdTypes.TIME_DEBUG;
			return;
		}
		else if (action.startsWith("'TAPE_JUMP:")) {
			type = SpiceCmdTypes.TAPE_JUMP;
			return;
		}
		else if (action.startsWith("'WAVE_JUMP:")) {
			type = SpiceCmdTypes.AUDIO_JUMP;
			return;
		}
		else
			type = SpiceCmdTypes.OTHER;
		
		if (action.startsWith("'")) { // this is a comment line
			type = SpiceCmdTypes.COMMENT;
			commentAbove = line.substring(line.indexOf("'")+1);
			action = "";
			timeDuration = 0;
			//if (commentAbove.length() == 0) { // this is empty line
			//	type = SpiceCmdTypes.EMPTY;
			//	}
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
				if (action.toUpperCase().startsWith("INTER")) {	// special case for Interactive System
					deviceName = "INTER";
					channelNames = "A";
				}
				if (!st.hasMoreTokens())
					return;
			}

			if (action.toUpperCase().startsWith("INTER")) {	// special case for Interactive System
				deviceName = "INTER";
				channelNames = "A";
			}

			// now get parameters
			if (!st.hasMoreTokens()) {
				return;
			}
			deviceName = st.nextToken();

			if (action.toUpperCase().startsWith("INTER")) {	// special case for Interactive System
				numericParam = deviceName;
				deviceName = "INTER";
				channelNames = "A";
			}

			if ("0123456789".indexOf(deviceName.substring(0,1)) != -1) { // this is percentage/script number
				numericParam = deviceName;
				if (!st.hasMoreTokens())
					return;
				deviceName = st.nextToken();
			}
			else {
				// special case: SEarch Ch 21 	VSRC	D
				// Ch is not a device
				if (action.toUpperCase().startsWith("SEARCH") &&
						deviceName.toUpperCase().startsWith("CH")) {
					numericParam = deviceName;
					if (!st.hasMoreTokens())
						return;
					numericParam += st.nextToken();
					if (!st.hasMoreTokens())
						return;
					deviceName = st.nextToken();
				}
			}
			if (!st.hasMoreTokens())
				return;
			channelNames = st.nextToken();
			otherParams = "";
			while (st.hasMoreTokens())
				otherParams = otherParams.concat(st.nextToken());
			if (action.toUpperCase().startsWith("STOP"))
				type = SpiceCmdTypes.STOP;
			else if (action.toUpperCase().startsWith("RUNSCRIPT"))
				type = SpiceCmdTypes.RUNSCRIPT;
			else if (action.toUpperCase().startsWith("RUN"))
				type = SpiceCmdTypes.RUN;
			else if (action.toUpperCase().startsWith("SEARCH") && deviceName.startsWith("SRC2") && channelNames.startsWith("D"))
				type = SpiceCmdTypes.TAPE_SEARCH;
			else if (action.toUpperCase().startsWith("PLAY") && deviceName.startsWith("SRC2") && channelNames.startsWith("D"))
				type = SpiceCmdTypes.TAPE_PLAY;
			else if (action.toUpperCase().startsWith("STILL") && deviceName.startsWith("SRC2") && channelNames.startsWith("D"))
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
		return String.format("'###REMOVED: Spice Cue Exec \"%s %s %s %s %s %s\"", duration, action, numericParam, deviceName, channelNames, otherParams); 
	}
	
	String formatExec() {
		return String.format("Spice Cue Exec \"%s %s %s %s %s %s\"", duration.trim(), action.trim(), numericParam.trim(), deviceName.trim(), channelNames.trim(), otherParams.trim());
	}
	
	int translate() {
		String UnusedDevices = ScriptExplorer.globalConf.getParam("COMMON", "UNUSED_DEVICES");
		String SpiceDevices = ScriptExplorer.globalConf.getParam("COMMON", "SPICE_DEVICES");
		dsEquiv = new DsCmd("",0);
		dsEquiv.isNative = false;
		dsEquiv.timeBegin = this.timeBegin;
		dsEquiv.superOrder = this.sectionNum;
		dsEquiv.currentTapeValue = this.currentTapeValue;
		dsEquiv.currentTapeRunning = this.currentTapeRunning;
		dsEquiv.currentTapeAudioDiff = this.currentTapeAudioDiff;
		if (type == SpiceCmdTypes.COMMENT) {
			dsEquiv.wholeLine = formatComment(commentAbove);
			dsEquiv.type = DsCmdTypes.COMMENT;
		}
		else if (type == SpiceCmdTypes.COMMENT_ABS) {
			dsEquiv.wholeLine = formatComment(commentAbove);
			dsEquiv.type = DsCmdTypes.COMMENT_ABS;
		}
		else if (type == SpiceCmdTypes.EMPTY) {
			dsEquiv.wholeLine = "\n";
			dsEquiv.type = DsCmdTypes.EMPTY;
		}
		else if (type == SpiceCmdTypes.RUN) {
			dsEquiv.wholeLine = formatOld();;
			dsEquiv.type = DsCmdTypes.OTHER;
		}
		else if (type == SpiceCmdTypes.TIME_DEBUG) {
			dsEquiv.wholeLine = "'TIME_DEBUG\n";
			dsEquiv.type = DsCmdTypes.TIME_DEBUG;
		}
		else if (type == SpiceCmdTypes.TAPE_JUMP) {	// ex: 'TAPE_JUMP:01:05:40.45
			dsEquiv.type = DsCmdTypes.OTHER;
			dsEquiv.wholeLine = "'SPECIAL: " + this.wholeLine;
		}
		else if (type == SpiceCmdTypes.AUDIO_JUMP) {	// ex: 'AUDIO_JUMP:00:05:40.45
			dsEquiv.wholeLine = "'SPECIAL: " + this.wholeLine +"\n" +
						String.format("Jbox1 Goto \"audio\" %6.2f\n", ((double)SpiceScript.timeValue("00"+action.substring(13,22)))/100);
			dsEquiv.type = DsCmdTypes.OTHER;
		}
		else if (type == SpiceCmdTypes.STOP) {
			dsEquiv.wholeLine = "\n\n\n'ButtonText \"ADD LABEL HERE\"" + 
								"\n\nSTOP\n\n" +
								";========STOPPED========\n\n\n";
			dsEquiv.type = DsCmdTypes.SPICESTOP;
			dsEquiv.action = "STOP";
			dsEquiv.timeBegin = 1000*60*60*100;	// use a big constant to make sure that STOP happens after everything else   
		}
		else {
			dsEquiv.type = DsCmdTypes.OTHER;
			
			// for VOLM, SPFX, LAMP, AMIX
			if (SpiceDevices.contains(deviceName.toUpperCase())) 
				dsEquiv.wholeLine = formatExec();

			// get rid of MISC, ASKY
			else if (UnusedDevices.contains(deviceName.toUpperCase())) 
				dsEquiv.wholeLine = formatOld();
			
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
	
	
	void setExecTime(int v, int oldClock) {
		timeBegin = v;
		timeDelay = v - oldClock;
	}
}
