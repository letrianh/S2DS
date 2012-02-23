/**
 * 
 */
package org.carnegiesciencecenter.buhl;

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
}
