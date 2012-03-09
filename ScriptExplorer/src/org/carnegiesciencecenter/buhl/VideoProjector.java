/**
 * 
 */
package org.carnegiesciencecenter.buhl;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Anh Le
 *
 */
public class VideoProjector extends AbstractDevice {

	static private HashMap<Integer,AbstractDevice> sources = null;
	static String DEFAULT_IMAGE_PATH = "ShowPath\\IMAGES\\";
	static String DEFAULT_IMAGE_EXT = "TGA";
	static String DEFAULT_VIDEO_PATH = "ShowPath\\VIDEOS\\";
	static String DEFAULT_VIDEO_EXT = "AVI";

	private double DEFAULT_AZIMUTH;
	private double DEFAULT_ELEVATION;
	private double DEFAULT_ROTATION;
	private double DEFAULT_WIDTH;
	private double DEFAULT_HEIGHT;
	
	public VideoProjectorStatus getStatus() {
		return (VideoProjectorStatus) status;
	}

	VideoProjector(String name, String ch, ArrayList<DeviceStatus> l) {
		super(name, ch, new VideoProjectorStatus(), l);
		type = DeviceTypes.VIDEO_PROJECTOR;
		if (sources == null)
			sources = new HashMap<Integer,AbstractDevice>();
	}
	
	static void addSource(int id, AbstractDevice d) {
		sources.put(id, d);
	}

	void selectSource(int id) {
		getStatus().sourceId = id;
		getStatus().currentSource = sources.get(id);
		sources.get(id).getStatus().linkedDevice = this;
	}

	public void fade(int T, int n) {
		getStatus().state = DeviceState.BEGIN_TRANSITION;
		this.recordStatus();
		getStatus().brightness = n;

		if (n != 0) {
			if (getStatus().currentSource.type == DeviceTypes.INTERACTIVE) {
				DeviceManager.equivCmds.add(DsCmd.cmdAddImage(getStatus().clockId, getStatus().atTime, objName(), 
						DEFAULT_IMAGE_PATH+objName()+DEFAULT_IMAGE_EXT));
			}
			else if (getStatus().currentSource.type == DeviceTypes.PLAYER) { 
				DeviceManager.equivCmds.add(DsCmd.cmdAddImage(getStatus().clockId, getStatus().atTime, objName(), 
						DEFAULT_VIDEO_PATH+objName()+DEFAULT_VIDEO_EXT));
				DeviceManager.equivCmds.add(DsCmd.cmdGoto(getStatus().clockId, getStatus().atTime, objName(), 
						((Player)getStatus().currentSource).getStatus().position));
			}
			DeviceManager.equivCmds.add(DsCmd.cmdLocate(getStatus().clockId, getStatus().atTime, objName(), 0, 
					DEFAULT_AZIMUTH, DEFAULT_ELEVATION, DEFAULT_ROTATION, DEFAULT_WIDTH, DEFAULT_HEIGHT));
		}
		DeviceManager.equivCmds.add(DsCmd.cmdView(getStatus().clockId, getStatus().atTime, objName(), T, n));
		if (n == 0)
			DeviceManager.equivCmds.add(DsCmd.cmdRemove(getStatus().clockId, getStatus().atTime, objName()));

		getStatus().atTime += T;
		getStatus().state = DeviceState.END_TRANSITION;
		this.recordStatus();
}
	
	public void alt(int T, int n) {
		getStatus().state = DeviceState.BEGIN_TRANSITION;
		this.recordStatus();
		if (getStatus().brightness == 0) {
			getStatus().brightness = n;

			if (getStatus().currentSource.type == DeviceTypes.INTERACTIVE) {
				DeviceManager.equivCmds.add(DsCmd.cmdAddImage(getStatus().clockId, getStatus().atTime, objName(), 
						DEFAULT_IMAGE_PATH+objName()+DEFAULT_IMAGE_EXT));
			}
			else if (getStatus().currentSource.type == DeviceTypes.PLAYER) { 
				DeviceManager.equivCmds.add(DsCmd.cmdAddImage(getStatus().clockId, getStatus().atTime, objName(), 
						DEFAULT_VIDEO_PATH+objName()+DEFAULT_VIDEO_EXT));
				DeviceManager.equivCmds.add(DsCmd.cmdGoto(getStatus().clockId, getStatus().atTime, objName(), 
						((Player)getStatus().currentSource).getStatus().position));
			}
			DeviceManager.equivCmds.add(DsCmd.cmdLocate(getStatus().clockId, getStatus().atTime, objName(), 0, 
					DEFAULT_AZIMUTH, DEFAULT_ELEVATION, DEFAULT_ROTATION, DEFAULT_WIDTH, DEFAULT_HEIGHT));
			DeviceManager.equivCmds.add(DsCmd.cmdView(getStatus().clockId, getStatus().atTime, objName(), T, n));
		}
		else {
			getStatus().brightness = 0;

			DeviceManager.equivCmds.add(DsCmd.cmdView(getStatus().clockId, getStatus().atTime, objName(), T, 0));
		}
		getStatus().atTime += T;
		getStatus().state = DeviceState.END_TRANSITION;
		this.recordStatus();
	}
	
	public void fanOn() {
		getStatus().isFanRunning = true;
		getStatus().state = DeviceState.STABLE;
		this.recordStatus();
	}
	
	public void fanOff() {
		getStatus().isFanRunning = false;
		getStatus().state = DeviceState.STABLE;
		this.recordStatus();
	}
	
	public void contrast(int n) {
		getStatus().contrast = n;
		getStatus().state = DeviceState.STABLE;
		this.recordStatus();
	}
	
	public String objName() {
		if (getStatus().currentSource != null)
			return getStatus().currentSource.objName();
		else {
			System.out.println("Unknown source for " + getStatus().deviceName + "-" + getStatus().channelName);
			return "ERROR";
		}
	}

	public void setViewPosition(double A, double E, double R, double W, double H) {		
		DEFAULT_AZIMUTH = A;
		DEFAULT_ELEVATION = E;
		DEFAULT_ROTATION = R;
		DEFAULT_WIDTH = W;
		DEFAULT_HEIGHT = H;
	}
	
	@Override
	public int loadConfiguration(String fileName) {
		if (super.loadConfiguration(fileName) != 0)
			return -1;
		if (conf.size() != 0) {
			String pos[] = conf.get(0).split("\\s");
			double p0 = Double.parseDouble(pos[0]);
			double p1 = Double.parseDouble(pos[1]);
			double p2 = Double.parseDouble(pos[2]);
			double p3 = Double.parseDouble(pos[3]);
			double p4 = Double.parseDouble(pos[4]);
			setViewPosition(p0, p1, p2, p3, p4);
			return 0;
		}
		return -1;
	}
	
}
