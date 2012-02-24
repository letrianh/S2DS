/**
 * 
 */
package org.carnegiesciencecenter.buhl;

import java.util.ArrayList;

/**
 * @author Anh Le
 *
 */
public class InterSystem extends AbstractDevice {

	public InterStatus getStatus() {
		return (InterStatus) status;
	}
	
	InterSystem(String name, String ch, ArrayList<DeviceStatus> l) {
		super(name, ch, new InterStatus(), l);
		getStatus().currentPage = 0;
	}
	
	public void page(int n) {
		DeviceManager.equivCmds.add(DsCmd.cmdRemove(getStatus().clockId, getStatus().atTime, objName()));
		getStatus().currentPage = n;
		getStatus().state = DeviceState.STABLE;
		this.recordStatus();
		DeviceManager.equivCmds.add(DsCmd.cmdAddImage(getStatus().clockId, getStatus().atTime, objName(), "ShowPath\\IMAGES\\"+objName()+".TGA"));
		DeviceManager.equivCmds.add(DsCmd.cmdLocate(getStatus().clockId, getStatus().atTime, objName(), 0, 
				((VideoProjector)getStatus().linkedDevice).DEFAULT_AZIMUTH, 
				((VideoProjector)getStatus().linkedDevice).DEFAULT_ELEVATION, 
				((VideoProjector)getStatus().linkedDevice).DEFAULT_WIDTH, 
				((VideoProjector)getStatus().linkedDevice).DEFAULT_HEIGHT));
	}

	public void load(String s) {
		getStatus().fileName = s;
		getStatus().state = DeviceState.STABLE;
		this.recordStatus();
	}

	public void run() {
		page(0);
	}

	public String objName() {
		return String.format("%s_%d_%d", getStatus().deviceName, getStatus().fileName, getStatus().currentPage); 
	}
}
