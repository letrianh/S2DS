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
	SLEW_PROJECTOR,
	INTERACTIVE,
	PLAYER,
	MOTOR,
	SLEW,
	OTHER
}

public abstract class AbstractDevice {
	
	DeviceStatus status;
	DeviceTypes type;
	
	ArrayList<DeviceStatus> dList;
	
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
	
	public String getName() {
		return (getStatus().deviceName + "-" + getStatus().channelName);
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

	public void loadConfiguration() {
		
	}
	
	public String objName() {
		return String.format("%s_%s", getStatus().deviceName, getStatus().channelName);
	}
	
	public String updateParam(String sec, String key, String oldValue) {
		String s = ScriptExplorer.globalConf.getParam(sec, key);
		return (s.length() != 0 ? s : oldValue);
	}

}
