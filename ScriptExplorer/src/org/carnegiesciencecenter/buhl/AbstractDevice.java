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
	
	ArrayList<DeviceStatus> list;
	ArrayList<String> conf;
	
	AbstractDevice(String name, String ch, DeviceStatus s, ArrayList<DeviceStatus> l) {
		status = s;
		status.deviceName = name;
		status.channelName = ch.toUpperCase().trim();
		status.state = DeviceState.STABLE;
		status.atTime = 0;
		status.prevTime = 0;
		list = l;
	}

	public DeviceStatus getStatus() {
		return status;
	}
	
	public void useList(ArrayList<DeviceStatus> l) {
		list = l;
	}
	
	public void recordStatus() {
		if (list != null)
			list.add(status);
	}
	
	public void setClock(int id, int t) {
		status.clockId = id;
		status.prevTime = status.atTime;
		status.atTime = t;
	}
	
	public void setTime(int t) {
		status.atTime = t;
	}
	
	public int loadConfiguration(String fileName) {
		conf = new ArrayList<String>();
		String dName = "[" + getStatus().deviceName + "-" + getStatus().channelName + "]";
		System.out.println("Reading position config for " + dName);
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
						System.out.println(s);
						conf.add(s);
					}
					if (s.startsWith(dName))
						flag = true;
				}
			}
			b.close();
			f.close();
		}
		catch (Exception e) {
			System.out.println("Error reading config file: " + fileName);
			return 1;
		}
		return 0;
	}
	
	public String getParam(String param) {
		for (String s: conf) {
			String pos[] = s.split("[\\s=]+");
			if (param.toUpperCase().compareTo(pos[0].toUpperCase()) == 0) {
				return pos[1];
			}
		}
		return "";
	}
	
	public String objName() {
		return String.format("%s_%s", getStatus().deviceName, getStatus().channelName);
	}
	
}
