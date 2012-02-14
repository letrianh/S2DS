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
public class DsScript {
	String id;
	String name;
	String text;
	String title;
	String modified;
	int pageNum;
	boolean inUse;
	ArrayList<Integer> stopPositions;
	ArrayList<DsCmd> commands;
	boolean loadNextButton;
	int loadTime;
	boolean reachedTheEnd = false;
	String uniqueCmds;
	
	int repNum = 0;
	int clock = 0;
	int sectionNum = 0;
	int currentSection = 0;
	
	void resetStop() {
		currentSection = 0;
	}
	
	ArrayList<DsCmd> runNextAt(int timePoint, int superOrder, int tapePoint, boolean tapeRunning) {
		ArrayList<DsCmd> list = new ArrayList<DsCmd>();
		int idxBegin, idxEnd;
		if (reachedTheEnd)
			reachedTheEnd = false;
		if (currentSection == 0)
			idxBegin = 0;
		else
			idxBegin = stopPositions.get(currentSection-1);
		
		if (currentSection < sectionNum)
			idxEnd = stopPositions.get(currentSection)-2;
		else
			idxEnd = commands.size() - 1;
		
		this.loadNextButton = false;
		while (idxBegin <= idxEnd) {
			DsCmd c = new DsCmd(commands.get(idxBegin));
			c.currentTapeRunning = tapeRunning;
			if (tapeRunning)
				c.setCurrentTapeValue(tapePoint + c.timeBegin);
			else
				c.setCurrentTapeValue(tapePoint);
			c.addToExecTime(timePoint);
			c.setSuperOrder(superOrder);
			
			if (c.type == DsCmdTypes.NEXT) {
				this.loadTime = c.timeBegin;	// load time for this Show Next
				this.loadNextButton = true;
			}
			list.add(c);
			idxBegin++;
		}
		currentSection++;
		if (currentSection == sectionNum+1) {
			currentSection = 0;
			repNum++;
			reachedTheEnd = true;
		}
		return list;
	}
	
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
	
	void loadDelay(String delay) {
		if (delay.trim().length() == 0)
			return;
		int v = (int) Math.floor(Double.parseDouble(delay)*100);
		addClock(v);
	}
	
	static String nameToId(String name) {
		return numberToId(nameToNumber(name));
	}
	
	static String numberToId(String number) {
		return String.format("%4s", number).replace(" ", "0");
	}

	static String nameToNumber(String name) {
		return name.substring(1,name.indexOf("."));
	}

	DsScript(File f) {
		stopPositions = new ArrayList<Integer>();
		commands = new ArrayList<DsCmd>();
		inUse = false;
		title = name;
		loadNextButton = false;
		uniqueCmds = "";
		if (f == null)
			return;
		try {
			name = f.getName();
			text = readFile(f);
			id = nameToId(name);
			pageNum = (int) ((Math.floor(Integer.parseInt(id))-1) / (8*12));
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

	int parseDS() {
		BufferedReader reader = new BufferedReader(new StringReader(text));
		String line;
		setClock(0);
		int headerMarkers = 0;
		try {
			while ((line = reader.readLine()) != null) {
				String origLine = line;
				line = line.trim();
				DsCmd c;
				if (line.length() > 0) {
					
					if (line.startsWith(";") || line.startsWith("'")) {
						if (line.contains("; Title")) {
							//System.out.println(line);
							title = line.substring(line.indexOf('"')+1);
							if (title.indexOf('"') != -1)
								title = title.substring(0,title.indexOf('"'));
						}
						else if (line.contains("; Modified")) {
							modified = line.substring(line.indexOf(':')+1);
						}
					}
					
					if (headerMarkers>=2) {
						c = new DsCmd(origLine, sectionNum);
						commands.add(c);
						System.out.println(c.wholeLine);
						loadDelay(c.waitTime);
						c.setExecTime(clock);
						
						if (c.type == DsCmdTypes.STOP) {
							stopPositions.add(commands.size());
							setClock(0);
							sectionNum++;
							System.out.println("SecNum = " + Integer.toString(sectionNum));
						}
						
						if (c.type == DsCmdTypes.NEXT) {
							loadNextButton = true;
						}
						
						//String CatAc = c.category + "-" + c.action.toUpperCase();
						String CatAc = c.category;
				    	if (c.type == DsCmdTypes.OTHER && uniqueCmds.indexOf(CatAc) == -1)
				    		uniqueCmds += CatAc+"; ";
					}

					if (line.startsWith(";-"))
						headerMarkers++;

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
		Iterator<DsCmd> itr = commands.iterator();
	    while(itr.hasNext()) {
	    	DsCmd c = itr.next();
	    	sb.append(c.wholeLine);
	    	sb.append("\n");
	    }
	    return sb.toString();
	}
}
