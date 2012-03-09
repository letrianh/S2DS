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
	
	public static String DEFAULT_CONFIG_FILE = "/home/lion/Downloads/CSC/SHOW/devices.conf"; 
	static public ArrayList<DsCmd> equivCmds;
	
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
	
	public VideoProjector getVideoProjector(String name, String channel) {
		return ((VideoProjector)getDevice(name, channel));
	}
	
	public SlideProjector getSlideProjector(String name, String channel) {
		return ((SlideProjector)getDevice(name, channel));
	}
	
	public Player getPlayer(String name, String channel) {
		return ((Player)getDevice(name, channel));
	}
	
	public InterSystem getInterSystem(String name, String channel) {
		return ((InterSystem)getDevice(name, channel));
	}
	
	public ArrayList<AbstractDevice> getOtherDeviceSet(String name, String channels) {
		return getDeviceSet(name, getOthers(name, channels));
	}
	
	public ArrayList<AbstractDevice> getDeviceSet(String name, String channels) {
		ArrayList<AbstractDevice> list = new ArrayList<AbstractDevice>();
		for (int i=0; i<channels.length(); i++)
			list.add(getDevice(name, channels.substring(i, i+1)));
		return list;
	}
	
	public AbstractDevice getDevice(String name, String channel) {
		AbstractDevice d = allDevices.get(name+"-"+channel);
		if (d == null)
			System.out.println("Device not found: " + name + "-" + channel);
		return d;
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
		else if (name.startsWith("SRC2") || name.startsWith("VSRC"))
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
			if (type == DeviceTypes.SLIDE_PROJECTOR) {
				dev = new SlideProjector(name,ch, allEvents);
				((SlideProjector) dev).loadConfiguration(DEFAULT_CONFIG_FILE);
				String path = SlideProjector.loadParam("IMAGES", DEFAULT_CONFIG_FILE, "PATH");
				if (path.length() != 0)
					SlideProjector.DEFAULT_IMAGE_PATH = path;
				String ext = SlideProjector.loadParam("IMAGE_EXT", DEFAULT_CONFIG_FILE, "PATH");
				if (ext.length() != 0)
					SlideProjector.DEFAULT_IMAGE_EXT = ext;
			}
			else if (type == DeviceTypes.PLAYER) {
				dev = new Player(name,ch, allEvents);
			}
			else if (type == DeviceTypes.VIDEO_PROJECTOR) {
				dev = new VideoProjector(name,ch, allEvents);
				((VideoProjector) dev).loadConfiguration(DEFAULT_CONFIG_FILE);
				String path = VideoProjector.loadParam("IMAGES", DEFAULT_CONFIG_FILE, "PATH");
				if (path.length() != 0)
					VideoProjector.DEFAULT_IMAGE_PATH = path;
				String ext = VideoProjector.loadParam("IMAGE_EXT", DEFAULT_CONFIG_FILE, "PATH");
				if (ext.length() != 0)
					VideoProjector.DEFAULT_IMAGE_EXT = ext;
			}
			else if (type == DeviceTypes.INTERACTIVE) {
				dev = new InterSystem(name,ch, allEvents);
				((InterSystem) dev).loadConfiguration(DEFAULT_CONFIG_FILE);
			}
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
		initBank("SRC2","D");
		((PlayerStatus) getDevice("SRC2","D").getStatus()).speed = 100;
		
		// init VSRC
		initBank("VSRC","ABCDEFGH");
		
		// init INTER
		initBank("INTER","A");

		// init VPRJ
		initBank("VPRJ","ABCDEF");
		
		VideoProjector.addSource(1, getDevice("VSRC","A"));
		VideoProjector.addSource(2, getDevice("VSRC","B"));
		VideoProjector.addSource(3, getDevice("VSRC","C"));
		VideoProjector.addSource(4, getDevice("VSRC","D"));
		VideoProjector.addSource(7, getDevice("INTER","A"));
	}
	
	private int toTime(String s) {
		return 100*Integer.parseInt(s);
	}
	
	int executeCommand(SpiceCmd c) {
		//for debug
		System.out.println(c.wholeLine);
		
		ArrayList<AbstractDevice> list = getDeviceSet(c.deviceName, c.channelNames);
		if (devTypeByName(c.deviceName) == DeviceTypes.SLIDE_PROJECTOR) {
			Iterator<AbstractDevice> itr = list.iterator();
			while (itr.hasNext()) {
				SlideProjector p = (SlideProjector) itr.next();
				p.setClock(c.sectionNum, c.timeBegin);
				if (c.action.toUpperCase().startsWith("FADE"))
					p.fade(toTime(c.duration), Integer.parseInt(c.numericParam));
				else if (c.action.toUpperCase().startsWith("DISSOLVE"))
					p.dissolve(toTime(c.duration), Integer.parseInt(c.numericParam));
				else if (c.action.toUpperCase().startsWith("ALT"))
					p.alt(toTime(c.duration), Integer.parseInt(c.numericParam));
				else if (c.action.toUpperCase().startsWith("LOCATE"))
					p.locate(Integer.parseInt(c.numericParam));
				else if (c.action.toUpperCase().startsWith("FORWARD"))
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
				p.setClock(c.sectionNum, c.timeBegin);
				if (c.action.toUpperCase().startsWith("PLAY")) {
					if (c.numericParam.length() != 0)
						p.play(Integer.parseInt(c.numericParam));
					else
						p.play();
				}
				else if (c.action.toUpperCase().startsWith("STILL")) {
					p.still();
				}
				else if (c.action.toUpperCase().startsWith("SEARCH")) {
					if (c.numericParam.toUpperCase().startsWith("CH"))
						p.searchCh(Integer.parseInt(c.numericParam.substring(2)));
					else
						p.search(Integer.parseInt(c.numericParam));
				}
				else {
					System.out.println("Unimplemented command " + c.action + " for device " + c.deviceName);
					return -1;
				}
			}
		}
		else if (devTypeByName(c.deviceName) == DeviceTypes.VIDEO_PROJECTOR) {
			Iterator<AbstractDevice> itr = list.iterator();
			while (itr.hasNext()) {
				VideoProjector p = (VideoProjector) itr.next();
				p.setClock(c.sectionNum, c.timeBegin);
				if (c.action.toUpperCase().startsWith("FADE"))
					p.fade(toTime(c.duration), Integer.parseInt(c.numericParam));
				else if (c.action.toUpperCase().startsWith("ALT"))
					p.alt(toTime(c.duration), Integer.parseInt(c.numericParam));
				else if (c.action.toUpperCase().startsWith("SELECTSOURCE"))
					p.selectSource(Integer.parseInt(c.numericParam));
				else if (c.action.toUpperCase().startsWith("FANON"))
					p.fanOn();
				else if (c.action.toUpperCase().startsWith("FANOFF"))
					p.fanOff();
				else if (c.action.toUpperCase().startsWith("CONTRAST"))
					p.contrast(Integer.parseInt(c.numericParam));
				else {
					System.out.println("Unimplemented command " + c.action + " for device " + c.deviceName);
					return -1;
				}
			}
		}
		else if (devTypeByName(c.deviceName) == DeviceTypes.INTERACTIVE) {
			Iterator<AbstractDevice> itr = list.iterator();
			while (itr.hasNext()) {
				InterSystem r = (InterSystem) itr.next();
				r.setClock(c.sectionNum, c.timeBegin);
				if (r.getStatus().usingSVID) {
					DeviceManager.equivCmds.add(DsCmd.cmdCueExec(c.sectionNum, c.timeBegin, c.wholeLine.substring(12).trim()));
				}
				else {
					if (c.action.toUpperCase().startsWith("INTERLOAD")) {
						r.load(c.numericParam);
					} 
					else if (c.action.toUpperCase().startsWith("INTERRUN")) {
						r.run();
					} 
					else if (c.action.toUpperCase().startsWith("INTERPAGE")) {
						r.page(Integer.parseInt(c.numericParam));
					} 
					else {
						System.out.println("Unimplemented command " + c.action + " for device " + c.deviceName);
						return -1;
					}
				}
			}
		}
		else {
			System.out.println("Unimplemented device type: " + c.deviceName);
			return -1;
		}
		return 0;
	}
	
	public void resetEquivCmds() {
		equivCmds = new ArrayList<DsCmd>();
	}
}
