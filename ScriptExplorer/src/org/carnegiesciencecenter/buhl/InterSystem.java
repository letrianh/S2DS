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
	}
	
	public void page(int n) {
		getStatus().currentPage = n;
		getStatus().state = DeviceState.STABLE;
		this.recordStatus();
	}

	public void load(String s) {
		getStatus().fileName = s;
		getStatus().state = DeviceState.STABLE;
		this.recordStatus();
	}

}
