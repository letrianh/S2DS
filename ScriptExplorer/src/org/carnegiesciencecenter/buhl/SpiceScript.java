/**
 * 
 */
package org.carnegiesciencecenter.buhl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

import javax.swing.JOptionPane;

/**
 * @author Anh Le
 *
 */
public class SpiceScript {
	String name;
	String title;
	String text;
	String uniqueCmds;
	int nStop;
	ArrayList<Integer> stopPositions;
	ArrayList<SpiceCmd> commands;
	ArrayList<String> buttons;

	int tapeValue = 0;
	int tapeStartPoint;
	int clock = 0;
	int sectionNum = 0;
	String interactiveChannel;
	
	void setClock(int value) {
		clock = value;
	}
	
	int addClock(int value) {
		clock += value;
		return clock;
	}

	int timeValue(int h, int m, int s, int ms) {
		return (ms + 100*(s+60*(m+60*h)));
	}
	
	int timeValue(String timecode) {
		if (timecode.length()==8)
			timecode = "00:" + timecode;
		else if (timecode.length()==7)
			timecode = "00:0" + timecode;
		String h_ = timecode.substring(0, 2);
		String m_ = timecode.substring(3, 5);
		String s_ = timecode.substring(6, 8);
		String ms_ = timecode.substring(9, 11);
		int v = timeValue(Integer.parseInt(h_),Integer.parseInt(m_),Integer.parseInt(s_),Integer.parseInt(ms_));
		return v;
	}
	
	void loadTimecode(String timecode) {
		boolean relative = timecode.startsWith("+");
		if (relative) {
			timecode = timecode.substring(1);
			addClock(timeValue(timecode));
		}
		else {
			if (timeValue(timecode) < tapeValue)
				System.out.println("SERIOUS PROBLEM: absolute time is less than current value of the tape");
			else
				setClock( (timeValue(timecode) - tapeValue) + tapeStartPoint );
		}
	}

	enum LineTypes {
		SEC_BEG, SEC_END, SEC_MID, UNKNOWN
	}
	
	enum SectionTypes {
		DESCRIPTION, SHOW, VERSIONS, BANKLIST, INITIALSTATUS, TIMELINE, DEFAULTS, UNKNOWN
	}
	
	SpiceScript(File f) {
		try {
			name = f.getName();
			text = readFile(f);
			nStop = 0;
			stopPositions = new ArrayList<Integer>();
			commands = new ArrayList<SpiceCmd>();
			buttons = new ArrayList<String>();
			uniqueCmds = "";
			interactiveChannel = "";
		} catch (IOException e) {
			System.out.println("Error in reading file: " + f.getName());
		}
	}

	private String readFile(File f) throws IOException {
		StringBuffer text = new StringBuffer();
		Scanner in = null;
		try {
			in = new Scanner(f);
			String line;
			while (in.hasNextLine()) {
				line = in.nextLine();
				text.append(line);
				text.append("\n");
			}
			//System.out.println(text.toString());
		}
		catch (FileNotFoundException e) {
			System.out.println("Cannot open file:"+f.getName());
		} 
		finally {
			if (in != null)
				in.close();
		}
		return text.toString();
	}
	
	LineTypes getLineType(String line) {
		if (line.startsWith("SECTION") || line.startsWith("INITIALSTATUS") || line.startsWith("TIMELINE")) {
			return LineTypes.SEC_BEG; 
		}
		else if (line.startsWith("END"))
			return LineTypes.SEC_END;
		else
			return LineTypes.UNKNOWN;
	}
	
	SectionTypes getSectionType(String line) {
		if (line.trim().startsWith("INITIALSTATUS"))
			return SectionTypes.INITIALSTATUS;
		if (line.trim().startsWith("TIMELINE"))
			return SectionTypes.TIMELINE;
		
		int startIndex = line.indexOf(" ");
		if (startIndex == -1)
			startIndex = 0;
		String sec = line.substring(startIndex).trim(); 
		if (sec.startsWith("DESCRIPTION")) 
			return SectionTypes.DESCRIPTION;
		else if (sec.startsWith("SHOW"))
			return SectionTypes.SHOW;
		else if (sec.startsWith("BANKLIST"))
			return SectionTypes.BANKLIST;
		else if (sec.startsWith("DEFAULTS"))
			return SectionTypes.DEFAULTS;
		else
			return SectionTypes.UNKNOWN;
	}
	
	int parseSPICE() {
		BufferedReader reader = new BufferedReader(new StringReader(text));
		String line;
		LineTypes status;
		int nextAction = 0; // 1: get title; 2: get command; 3: ignore;
		setClock(0);
		try {
			while ((line = reader.readLine()) != null) {
				if (line.length() > 0) {
					System.out.println(line);
					status = getLineType(line);
					if (status == LineTypes.UNKNOWN && nextAction != 0)
						status = LineTypes.SEC_MID;
					
					if (status == LineTypes.SEC_BEG) {
						if (getSectionType(line) == SectionTypes.SHOW)
							nextAction = 1;
						else if (getSectionType(line) == SectionTypes.TIMELINE)
							nextAction = 2;
						else
							nextAction = 3;
					} 
					else if (status == LineTypes.SEC_END) {
						nextAction = 0;
					}
					else if (status == LineTypes.SEC_MID) {
						
						if (nextAction == 1) // get show name here
							title = line;
						
						// get new command here
						else if (nextAction == 2) {
							
							SpiceCmd c = new SpiceCmd(line, sectionNum);
							
							if (c.type == SpiceCmdTypes.UNKNOWN) {
								System.out.println("ERROR: Found unknown command!");
								return 1;
							}
							
							if (c.type == SpiceCmdTypes.EMPTY) {
								continue; // we don't add empty commands
							}
							
							if (c.type == SpiceCmdTypes.RUNSCRIPT)
								if (buttons.isEmpty() || !buttons.contains(c.numericParam)) {
									System.out.println("FOUND RunScript: " + c.numericParam);
									buttons.add(c.numericParam);
								}

							boolean adjustClock = true;
							// we also consider delay for comments
//							if (c.type == SpiceCmdTypes.COMMENT || c.type == SpiceCmdTypes.EMPTY)
//								if (c.timecode.startsWith("+00:00:00.05"))
//									adjustClock = false;
							if (adjustClock)
								loadTimecode(c.timecode);
							c.setExecTime(clock);

							commands.add(c);
							
							if (c.type == SpiceCmdTypes.TAPE_SEARCH) {
								tapeValue = timeValue(c.numericParam);
							}
							
							if (c.type == SpiceCmdTypes.TAPE_PLAY) {
								tapeStartPoint = clock;
							}
							
							if (c.type == SpiceCmdTypes.TAPE_PAUSE) {
								tapeValue += (clock-tapeStartPoint);
								tapeStartPoint = clock;
							}
							
							if (c.type == SpiceCmdTypes.SELECT_SOURCE) {
								if (c.numericParam.startsWith("7") &&
									c.deviceName.toUpperCase().startsWith("VPRJ"))
									interactiveChannel = c.channelName;
									SpiceCmd.extraInfo = interactiveChannel; 
							}
							
							if (c.type == SpiceCmdTypes.STOP) {
								System.out.println("SectionNum = " + Integer.toString(sectionNum));
								stopPositions.add(commands.size());
								sectionNum++;
								nStop++;
								setClock(0);
							}
						}
					}
				}
			}
		} 
		catch(IOException e) {
			return 1;
		}
		return 0;
	}
	
	String reformat() {
		StringBuilder sb = new StringBuilder();
		Iterator<SpiceCmd> itr = commands.iterator();
	    while(itr.hasNext()) {
	    	//sb.append("\nBLOCK:\n");
	    	SpiceCmd c = itr.next();
	    	sb.append(c.wholeLine);
	    	sb.append("\n");
	    	if (c.type == SpiceCmdTypes.OTHER && uniqueCmds.indexOf(c.action.toUpperCase()) == -1)
	    		uniqueCmds += c.action.toUpperCase()+"\n";
	    }
	    sb.append("All different commands used in this SPICE files:\n");
	    sb.append(uniqueCmds);
    	sb.append("\n");
	    return sb.toString();
	}
}
