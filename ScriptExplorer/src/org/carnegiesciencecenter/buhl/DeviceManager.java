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
	
	public SlewProjector getSlewProjector(String name, String channel) {
		return ((SlewProjector)getDevice(name, channel));
	}
	
	public Player getPlayer(String name, String channel) {
		return ((Player)getDevice(name, channel));
	}
	
	public Slew getSlew(String name, String channel) {
		return ((Slew)getDevice(name, channel));
	}
	
	public Motor getMotor(String name, String channel) {
		return ((Motor)getDevice(name, channel));
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
		if (name.startsWith("ANIM") || name.startsWith("ASKY") || name.startsWith("PANS"))
			return DeviceTypes.SLIDE_PROJECTOR;
		else if (name.startsWith("VPRJ"))
			return DeviceTypes.VIDEO_PROJECTOR;
		else if (name.startsWith("PROJ"))
			return DeviceTypes.SLEW_PROJECTOR;
		else if (name.startsWith("SRC2") || name.startsWith("VSRC"))
			return DeviceTypes.PLAYER;
		else if (name.startsWith("INTER"))
			return DeviceTypes.INTERACTIVE;
		else if (name.startsWith("SLEW"))
			return DeviceTypes.SLEW;
		else if (name.startsWith("MOTR"))
			return DeviceTypes.MOTOR;
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
				dev.loadConfiguration();
			}
			else if (type == DeviceTypes.PLAYER) {
				dev = new Player(name,ch, allEvents);
			}
			else if (type == DeviceTypes.VIDEO_PROJECTOR) {
				dev = new VideoProjector(name,ch, allEvents);
				dev.loadConfiguration();
			}
			else if (type == DeviceTypes.INTERACTIVE) {
				dev = new InterSystem(name,ch, allEvents);
				dev.loadConfiguration();
			}
			else if (type == DeviceTypes.SLEW) {
				dev = new Slew(name,ch, allEvents);
				dev.loadConfiguration();
			}
			else if (type == DeviceTypes.MOTOR) {
				dev = new Motor(name,ch, allEvents);
				dev.loadConfiguration();
			}
			else if (type == DeviceTypes.SLEW_PROJECTOR) {
				dev = new SlewProjector(name,ch, allEvents);
				dev.loadConfiguration();
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

		// init ASKY
		initBank("ASKY","ABCDEFG");

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

		// init SLEW
		initBank("SLEW","ABCD");

		// init MOTR
		initBank("MOTR","AB");

		// init PROJ
		initBank("PROJ","AB");
		
		getSlewProjector("PROJ","A").setup(getSlew("SLEW","A"), getSlew("SLEW","B"), 
				getMotor("MOTR","A"));
		getSlewProjector("PROJ","B").setup(getSlew("SLEW","C"), getSlew("SLEW","D"), 
				getMotor("MOTR","B"));

		// init PANS
		initBank("PANS","ABCDEFGHIJKLMN");

}
	
	private int toTime(String s) {
		return 100*Integer.parseInt(s);
	}
	
	private int makeFly(int sec, int t0, int T, String action) {
		String param[] = action.split(",");
		String src[] = param[0].split(" ");
		String proj = src[0];
		String projCh = src[1];
		String zoom = param[2];
		String pos[] = param[1].split(" ");
		String dest = pos[0];
		String destCh = "";
		
		SlewProjector p = getSlewProjector(proj, projCh);
		p.setClock(sec, t0);
		p.zoomMotor.setClock(sec, t0);
		p.xSlew.setClock(sec, t0);
		p.ySlew.setClock(sec, t0);
		int z;
		if (zoom.compareTo("?") == 0)
			z = p.zoomMotor.getStatus().zoomLevel;
		else
			z = Integer.parseInt(zoom);
		
		// format: MAKE_FLY(<fly time>,<slew projector>,SLEW <x> <y> [<projector> <channel>],<zoom>)
		if (dest.compareTo("SLEW") == 0 && pos.length > 3) {
			dest = pos[3];
			destCh = pos[4];
		}
		
		if (dest.compareTo("SLEW") == 0) {	// emulate SLEW and MOTOR
			if (pos[1].compareTo("?") != 0)
				p.xSlew.flyTo(T, Integer.parseInt(pos[1]), true);
			if (pos[2].compareTo("?") != 0)
				p.ySlew.flyTo(T, Integer.parseInt(pos[2]), true);
			p.zoomMotor.setZoomNaturally(T, z);
		}
		else if (dest.compareTo("COOR") == 0) {	// fly to a specified position
			p.xSlew.flyToV(T, Double.parseDouble(pos[1]), true);
			p.ySlew.flyToV(T, Double.parseDouble(pos[2]), true);
			p.zoomMotor.setZoomNaturally(T, z);
		}
		else {	// fly to the position of a projector
			Projector d = (Projector)getDevice(dest, destCh);
			p.xSlew.flyToV(T, d.DEFAULT_AZIMUTH, true);
			p.ySlew.flyToV(T, d.DEFAULT_ELEVATION, true);
			p.zoomMotor.setZoomNaturally(T, z);
		}
		return 0;
	}
	
	int executeCommand(SpiceCmd c) {
		//for debug
		System.out.println("DeviceManager: " + c.wholeLine);
		
		if (c.deviceName == "FLYER") {
			makeFly(c.sectionNum, c.timeBegin, toTime(c.duration), c.action);
			return 0;
		}
		
		ArrayList<AbstractDevice> list = getDeviceSet(c.deviceName, c.channelNames);
		if (devTypeByName(c.deviceName) == DeviceTypes.SLIDE_PROJECTOR ||
				devTypeByName(c.deviceName) == DeviceTypes.SLEW_PROJECTOR) {
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
		else if (devTypeByName(c.deviceName) == DeviceTypes.SLEW) {
			for (int i=0; i<list.size(); i++) {
				Slew p = (Slew) list.get(i);
				boolean wait = false;
				for (int j=i+1; j<list.size(); j++)
					if (p.peerSlew == list.get(j)) {
						wait = true;
						break;
					}
				p.setClock(c.sectionNum, c.timeBegin);
				if (c.action.toUpperCase().startsWith("RUNFWD")) {
					p.setPosition(Integer.parseInt(c.duration),Integer.parseInt(c.numericParam), wait);
				}
				else {
					System.out.println("Unimplemented command " + c.action + " for device " + c.deviceName);
					return -1;
				}
			}
		}
		else if (devTypeByName(c.deviceName) == DeviceTypes.MOTOR) {
			Iterator<AbstractDevice> itr = list.iterator();
			while (itr.hasNext()) {
				Motor p = (Motor) itr.next();
				p.setClock(c.sectionNum, c.timeBegin);
				if (c.action.toUpperCase().startsWith("FWDMOTOR") ||
						c.action.toUpperCase().startsWith("REVMOTOR")) {
					p.setZoom(toTime(c.duration),Integer.parseInt(c.numericParam));
				}
				else {
					System.out.println("Unimplemented command " + c.action + " for device " + c.deviceName);
					return -1;
				}
			}
		}
		else if (devTypeByName(c.deviceName) == DeviceTypes.VIDEO_PROJECTOR) {
			// if 2 projectors are fed with the same source, we need only one Text Add
			if (c.action.toUpperCase().startsWith("FADE") || c.action.toUpperCase().startsWith("ALT"))
				OnlyUniqueSources(list);
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
	
	private void OnlyUniqueSources(ArrayList<AbstractDevice> list) {
		int i=1; 
		while (i<list.size()) {
			VideoProjector x = (VideoProjector) list.get(i);
			boolean isUnique = true;
			for (int j=0; j<i; j++) {
				VideoProjector y = (VideoProjector) list.get(j);
				if (x.getStatus().sourceId == y.getStatus().sourceId) {
					list.remove(i);
					isUnique = false;
					break;
				}
			}
			if (isUnique)
				i++;
		}
	}
	
	public void resetEquivCmds() {
		equivCmds = new ArrayList<DsCmd>();
	}
}
