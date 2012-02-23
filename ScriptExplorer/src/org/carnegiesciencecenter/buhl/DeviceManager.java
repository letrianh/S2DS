/**
 * 
 */
package org.carnegiesciencecenter.buhl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author Anh Le
 *
 */
public class DeviceManager {
	
	public HashMap<String,AbstractDevice> allDevices;
	public HashMap<String,HashMap<String,AbstractDevice>> banks;
	public HashMap<String, String> allChannels;
	
	public ArrayList<DeviceStatus> allEvents;
	
	public String getOthers(String name, String channels) {
		String s = "";
		String all = allChannels.get(name);
		for (int i=0; i<all.length(); i++) {
			String one = all.substring(i, i+1);
			if (!channels.contains(one)) 
				s += one;
		}
		return s;
	}
	
	public ArrayList<AbstractDevice> getOtherDeviceSet(String name, String channels) {
		return getDeviceSet(name, getOthers(name, channels));
	}
	
	public ArrayList<AbstractDevice> getDeviceSet(String name, String channels) {
		ArrayList<AbstractDevice> list = new ArrayList<AbstractDevice>();
		for (int i=0; i<channels.length(); i++)
			list.add(allDevices.get(name+"-"+channels.substring(i, i+1)));
		return list;
	}
	
	public AbstractDevice getDevice(String name, String channel) {
		return allDevices.get(name+"-"+channel);
	}
	
	public void setClockAll(int id, int time) {
		Object[] devList = allDevices.entrySet().toArray();
		for (int i=0; i<devList.length; i++) {
			((AbstractDevice) devList[i]).setClock(id, time);
		}
	}
	
	static DeviceTypes devTypeByName(String name) {
		if (name.startsWith("ANIM") || name.startsWith("PROJ"))
			return DeviceTypes.SLIDE_PROJECTOR;
		else if (name.startsWith("VPRJ"))
			return DeviceTypes.VIDEO_PROJECTOR;
		else if (name.startsWith("SCR2"))
			return DeviceTypes.PLAYER;
		else if (name.startsWith("INTER"))
			return DeviceTypes.INTERACTIVE;
		else
			return DeviceTypes.OTHER;
	}
	
	public void initBank(String name, String channels) {
		DeviceTypes type = devTypeByName(name);
		HashMap<String,AbstractDevice> newBank = new HashMap<String,AbstractDevice>();
		banks.put(name, newBank);
		allChannels.put(name, channels);
		for (int i=0; i<channels.length(); i++) {
			String ch = channels.substring(i, i+1); 
			AbstractDevice dev;
			if (type == DeviceTypes.SLIDE_PROJECTOR)
				dev = new SlideProjector(name,ch, allEvents);
			else if (type == DeviceTypes.PLAYER)
				dev = new Player(name,ch, allEvents);
			else if (type == DeviceTypes.VIDEO_PROJECTOR)
				dev = new VideoProjector(name,ch, allEvents);
			else if (type == DeviceTypes.INTERACTIVE)
				dev = new InterSystem(name,ch, allEvents);
			else {
				System.out.println("Unimplemented device type: " + name);
				return;
			}
			allDevices.put(name + "-" + ch, dev);
			newBank.put(name + "-" + ch, dev);
		}
	}
	
	public void initDevices() {
		allDevices = new HashMap<String,AbstractDevice>();
		banks = new HashMap<String,HashMap<String,AbstractDevice>>();
		allChannels = new HashMap<String,String>();
		allEvents = new ArrayList<DeviceStatus>();

		// init ANIM
		initBank("ANIM","ABCDEFGHIJKLMNOPQR");

		// init PROJ
		initBank("PROJ","AB");

		// init TAPE
		initBank("SRC2","A");
		((PlayerStatus) getDevice("SRC2","A").getStatus()).speed = 1;
		
		// init VSRC
		initBank("VSRC","ABCD");
		
		// init INTER
		initBank("INTER","A");

		// init VPRJ
		initBank("VPRJ","ABCD");
		VideoProjector.addSource(1, getDevice("VSRC","A"));
		VideoProjector.addSource(2, getDevice("VSRC","B"));
		VideoProjector.addSource(3, getDevice("VSRC","C"));
		VideoProjector.addSource(4, getDevice("VSRC","D"));
		VideoProjector.addSource(7, getDevice("INTER","A"));
	}
	
	int executeCommand(SpiceCmd c) {
		ArrayList<AbstractDevice> list = getDeviceSet(c.deviceName, c.channelNames);
		if (devTypeByName(c.deviceName) == DeviceTypes.SLIDE_PROJECTOR) {
			Iterator<AbstractDevice> itr = list.iterator();
			while (itr.hasNext()) {
				SlideProjector p = (SlideProjector) itr.next();
				p.setClock(c.sectionNum, c.timeBegin);
				if (c.action.startsWith("FADE"))
					p.fade(Integer.parseInt(c.duration), Integer.parseInt(c.numericParam));
				else if (c.action.startsWith("DISSOLVE"))
					p.dissolve(Integer.parseInt(c.duration), Integer.parseInt(c.numericParam));
				else if (c.action.startsWith("ALT"))
					p.alt(Integer.parseInt(c.duration), Integer.parseInt(c.numericParam));
				else if (c.action.startsWith("LOCATE"))
					p.locate(Integer.parseInt(c.numericParam));
				else if (c.action.startsWith("FORWARD"))
					p.forward(Integer.parseInt(c.numericParam));
				else {
					System.out.println("Unimplemented command " + c.action + " for device " + c.deviceName);
					return -1;
				}
			}
		}
		else if (devTypeByName(c.deviceName) == DeviceTypes.PLAYER) {
			Iterator<AbstractDevice> itr = list.iterator();
			while (itr.hasNext()) {
				Player p = (Player) itr.next();
			}
		}
		else if (devTypeByName(c.deviceName) == DeviceTypes.VIDEO_PROJECTOR) {
			Iterator<AbstractDevice> itr = list.iterator();
			while (itr.hasNext()) {
				VideoProjector p = (VideoProjector) itr.next();
			}
		}
		else if (devTypeByName(c.deviceName) == DeviceTypes.INTERACTIVE) {
			Iterator<AbstractDevice> itr = list.iterator();
			while (itr.hasNext()) {
				InterSystem r = (InterSystem) itr.next();
			}
		}
		else {
			System.out.println("Unimplemented device type: " + c.deviceName);
			return -1;
		}
		return 0;
	}
}
