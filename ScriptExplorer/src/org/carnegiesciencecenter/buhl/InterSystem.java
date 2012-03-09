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
		getStatus().usingSVID = false;
	}
	
	@Override
	public int loadConfiguration(String fileName) {
		if (super.loadConfiguration(fileName) != 0)
			return -1;
		if (conf.size() != 0) {
			getStatus().usingSVID = (getParam("SVID").toUpperCase().compareTo("YES") == 0);
			return 0;
		}
		return -1;
	}

	public void page(int n) {
		DeviceManager.equivCmds.add(DsCmd.cmdRemove(getStatus().clockId, getStatus().atTime, objName()));
		getStatus().currentPage = n;
		getStatus().state = DeviceState.STABLE;
		this.recordStatus();
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
		if (getStatus().usingSVID)
			return "AVStream.LIVE:SVid";
		else
			return String.format("%s_%s_%02d", getStatus().deviceName, getStatus().fileName, getStatus().currentPage); 
	}
}
