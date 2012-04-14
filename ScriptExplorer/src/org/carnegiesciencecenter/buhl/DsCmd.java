package org.carnegiesciencecenter.buhl;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

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
	TIME_FLEX,	// fake command 'TIME_FLEX
	TIME_CUT	// to mark to cmd where the sequence was cut
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
	static HashMap<String,Dimension> imageList = null;
	static boolean isAutoOrientation = false;
	
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
				if (ScriptExplorer.globalConf.getParam("COMMON", "ALLOW_TIME_ADJUST").toUpperCase().compareTo("YES") == 0) {
					type = DsCmdTypes.TIME_ADJUST;
					action = "01" + line.substring(4, 13);
					return;
				}
				else {
					type = DsCmdTypes.COMMENT;
					return;
				}
			}
			else if (line.startsWith("'TIME_FLEX")) {	// ex: 'TIME_FLEX
				if (ScriptExplorer.globalConf.getParam("COMMON", "ALLOW_TIME_ADJUST").toUpperCase().compareTo("YES") == 0) {
					type = DsCmdTypes.TIME_FLEX;
					return;
				}
				else {
					type = DsCmdTypes.COMMENT;
					return;
				}
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
	
	//======================================
	
	static public String formatView(String name, double T, double N) {
		return String.format("Text View \"%s\" %.2f %.2f 100 100 100\n", name, ((double)T)/100, N); 
	}

	static public String formatLocate(String name, double T, double A, double E, double R, double W, double H) {
		
		return String.format("Text Locate \"%s\" %.2f %.2f %.2f %.2f %.2f %.2f\n", name, ((double)T)/100, A, E, R, W, H); 
	}

	static public String formatAddImage(String name, String fileName, double W, double H, double M, double XO, double YO) {
		return String.format("Text Add \"%s\" \"%s\" %.2f %.2f \"local\" %.2f %.2f %.2f %.2f %.2f black\n", name, fileName, W, H, M, XO, YO, 0.0, 0.0); 
	}

	static public String formatAddImage(String name, String fileName, double W, double H, double M) {
		return String.format("Text Add \"%s\" \"%s\" %.2f %.2f \"local\" %.2f %.2f %.2f %.2f %.2f black\n", name, fileName, W, H, M, 0.0, 0.0, 0.0, 0.0); 
	}

	static public String formatAddImage(String name, String fileName) {
		return String.format("Text Add \"%s\" \"%s\" %.2f %.2f \"local\" %.2f %.2f %.2f %.2f %.2f black\n", name, fileName, 0.0, 0.0, 90.0, 0.0, 0.0, 0.0, 0.0); 
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
	
	static public String formatJboxAdd(String objName, String fileName) {
		return String.format("Jbox1 Add \"%s\" \"%s\"", objName, fileName); 
	}
	
	static public String formatJboxVol(int percent) {
		return String.format("Jbox1 Volume \"audio\" 0 %d", percent); 
	}
	
	static public String formatJboxGoto(double position) {
		return String.format("Jbox1 Goto \"audio\" %.2f", ((double)position)/100); 
	}
	
	static public String formatJboxPlay() {
		return String.format("Jbox1 Play \"audio\""); 
	}
	
	//========================================

	static public DsCmd cmdView(int num, int time, String name, double T, double N) {
		if (N>0 && ScriptExplorer.isView100)
			N = 100;
		return new DsCmd(num, time, "TEXT", "VIEW", DsCmdTypes.OTHER, formatView(name, T, N));
	}

	static public DsCmd cmdLocate(int num, int time, String name, double T, double A, double E, double R, double W, double H) {
		if (isAutoOrientation) {
			Dimension dim = imageList.get(name);
			if (dim != null && dim.width<dim.height) {
				double tmp = W;
				W = H;
				H = tmp;
			}
		}
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

	static public DsCmd cmdJboxAdd(int num, int time, String objName, String fileName) {
		return new DsCmd(num, time, "JBOX1", "ADD", DsCmdTypes.OTHER, formatJboxAdd(objName, fileName));
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
	
	//=======================================

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
	
    static Dimension getImageDim(final String path) {
	    Dimension result = null;
	    String suffix = DsCmd.getFileSuffix(path);
	    Iterator<ImageReader> iter = ImageIO.getImageReadersBySuffix(suffix);
	    if (iter.hasNext()) {
	        ImageReader reader = iter.next();
	        try {
	            ImageInputStream stream = new FileImageInputStream(new File(path));
	            reader.setInput(stream);
	            int width = reader.getWidth(reader.getMinIndex());
	            int height = reader.getHeight(reader.getMinIndex());
	            result = new Dimension(width, height);
	        } catch (IOException e) {
	            System.out.println(e.getMessage());
	        } finally {
	            reader.dispose();
	        }
	    } 
	    else {
	        System.out.println("No reader found for given format: " + suffix);
	    }
	    return result;
    }

    static String getFileSuffix(final String path) {
        String result = null;
        if (path != null) {
            result = "";
            if (path.lastIndexOf('.') != -1) {
                result = path.substring(path.lastIndexOf('.'));
                if (result.startsWith(".")) {
                    result = result.substring(1);
                }
            }
        }
        return result;
    }
    
    static void loadImgList() {
    	if (ScriptExplorer.globalConf.getParam("COMMON", "AUTO_IMAGE_ORIENTATION").startsWith("YES")) {
    		imageList = new HashMap<String,Dimension>();
    		String strDir=ScriptExplorer.globalConf.getParam("COMMON", "AUTO_IMAGE_ORIENTATION_PATH");
    		String strList=ScriptExplorer.globalConf.getParam("COMMON", "AUTO_IMAGE_ORIENTATION_LIST");
    		String fileName = strDir + strList;
    		try {
    			FileReader f = new FileReader(fileName);
    			BufferedReader b = new BufferedReader(f);
    			String s;
    			while((s = b.readLine()) != null) {
    				imageList.put(s, DsCmd.getImageDim(strDir + s +
    						ScriptExplorer.globalConf.getParam("COMMON","IMAGE_EXT")));
    			}
    			b.close();
    			f.close();
    			isAutoOrientation = true;
    		}
    		catch (Exception e) {
    			System.out.println("Cannot read list from " + fileName);
    		}
    		
    	}
    }

}
