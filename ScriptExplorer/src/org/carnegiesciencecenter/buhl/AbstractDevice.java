/**
 * 
 */
package org.carnegiesciencecenter.buhl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * @author Anh Le
 *
 */

enum DeviceTypes {
	SLIDE_PROJECTOR,
	VIDEO_PROJECTOR,
	INTERACTIVE,
	PLAYER,
	OTHER
}

public abstract class AbstractDevice {
	
	DeviceStatus status;
	DeviceTypes type;
	
	ArrayList<DeviceStatus> dList;
	ArrayList<String> conf;
	
	AbstractDevice(String name, String ch, DeviceStatus s, ArrayList<DeviceStatus> l) {
		status = s;
		status.deviceName = name;
		status.channelName = ch.toUpperCase().trim();
		status.state = DeviceState.STABLE;
		status.atTime = 0;
		status.prevTime = 0;
		dList = l;
	}

	public DeviceStatus getStatus() {
		return status;
	}
	
	public void useList(ArrayList<DeviceStatus> l) {
		dList = l;
	}
	
	public void recordStatus() {
		if (dList != null)
			dList.add(status);
	}
	
	public void setClock(int id, int t) {
		status.clockId = id;
		status.prevTime = status.atTime;
		status.atTime = t;
	}
	
	public void setTime(int t) {
		status.atTime = t;
	}

	public static ArrayList<String> loadSection(String fileName, String sectionName) {
		System.out.println("Reading file " + fileName + ", section " + sectionName);
		ArrayList<String> list = new ArrayList<String>();
		String dName = "[" + sectionName.trim() + "]";
		try {
			FileReader f = new FileReader(fileName);
			BufferedReader b = new BufferedReader(f);
			String s;
			boolean flag = false;
			while((s = b.readLine()) != null) {
				s = s.trim();
				if (s.length() != 0 && !s.startsWith(";") && !s.startsWith("'")) {
					if (flag) {
						if (s.startsWith("["))
							break;
						list.add(s);
						System.out.println(s);
					}
					if (s.startsWith(dName))
						flag = true;
				}
			}
			b.close();
			f.close();
		}
		catch (Exception e) {
			System.out.println("Error reading file: " + fileName);
		}
		return list;
	}
	
	public int loadConfiguration(String fileName) {
		String dName = getStatus().deviceName + "-" + getStatus().channelName;
		System.out.println("Reading position config for " + dName);
		conf = loadSection(fileName, dName);
		if (conf.size() != 0)
			return 0;
		else
			return -1;
	}
	
	public String getParam(String param) {
		return loadParam(param, conf);
	}
	
	static public String loadParam(String param, ArrayList<String> list) {
		for (String s: list) {
			String pos[] = s.split("[\\s=]+");
			if (param.toUpperCase().compareTo(pos[0].toUpperCase()) == 0) {
				return pos[1];
			}
		}
		return "";
	}
	
	static public String loadParam(String param, String fname, String sname) {
		return loadParam(param, loadSection(fname, sname));
	}

	public String objName() {
		return String.format("%s_%s", getStatus().deviceName, getStatus().channelName);
	}
	
}
