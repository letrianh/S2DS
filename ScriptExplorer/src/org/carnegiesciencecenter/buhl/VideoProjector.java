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

	public int DEFAULT_AZIMUTH;
	public int DEFAULT_ELEVATION;
	public int DEFAULT_WIDTH;
	public int DEFAULT_HEIGHT;
	
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
		getStatus().atTime += T;
		getStatus().state = DeviceState.END_TRANSITION;
		this.recordStatus();
		DeviceManager.equivCmds.add(DsCmd.cmdView(getStatus().clockId, getStatus().atTime, objName(), T, n));
	}
	
	public void alt(int T, int n) {
		getStatus().state = DeviceState.BEGIN_TRANSITION;
		this.recordStatus();
		if (getStatus().brightness == 0) {
			getStatus().brightness = n;
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
	
	public String objName() {
		return getStatus().currentSource.objName();
	}

	public void setViewPosition(int A, int E, int W, int H) {		
		DEFAULT_AZIMUTH = A;
		DEFAULT_ELEVATION = E;
		DEFAULT_WIDTH = W;
		DEFAULT_HEIGHT = H;
	}
}
