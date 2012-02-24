/**
 * 
 */
package org.carnegiesciencecenter.buhl;

import java.util.ArrayList;

/**
 * @author Anh Le
 *
 */
public class SlideProjector extends AbstractDevice {
	
	static int DEFAULT_MAX_NUM = 80;
	private int DEFAULT_AZIMUTH;
	private int DEFAULT_ELEVATION;
	private int DEFAULT_WIDTH;
	private int DEFAULT_HEIGHT;
	
	public SlideProjectorStatus getStatus() {
		return (SlideProjectorStatus) status;
	}
	
	SlideProjector(String name, String ch, ArrayList<DeviceStatus> l, int max) {
		super(name, ch, new SlideProjectorStatus(), l);
		type = DeviceTypes.SLIDE_PROJECTOR;
		getStatus().maxNumber = max;
	}

	SlideProjector(String name, String ch, ArrayList<DeviceStatus> l) {
		this(name, ch, l, DEFAULT_MAX_NUM);
	}
	
	public int getSlideNumber() {
		return getStatus().currentSlide;
	}
	
	public void locate(int n) {
		getStatus().currentSlide = n;
		getStatus().state = DeviceState.STABLE;
		this.recordStatus();
		DeviceManager.equivCmds.add(DsCmd.cmdAddImage(getStatus().clockId, getStatus().atTime, objName(), "ShowPath\\IMAGES\\"+objName()+".TGA"));
		DeviceManager.equivCmds.add(DsCmd.cmdLocate(getStatus().clockId, getStatus().atTime, objName(), 0, 
				DEFAULT_AZIMUTH, DEFAULT_ELEVATION, DEFAULT_WIDTH, DEFAULT_HEIGHT));
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
	
	public void dissolve(int T, int n) {
		getStatus().state = DeviceState.BEGIN_TRANSITION;
		this.recordStatus();
		if (getStatus().brightness == 0) {
			DeviceManager.equivCmds.add(DsCmd.cmdView(getStatus().clockId, getStatus().atTime, objName(), T, n));
			getStatus().brightness = n;
		}
		else {
			DeviceManager.equivCmds.add(DsCmd.cmdView(getStatus().clockId, getStatus().atTime, objName(), T, 0));
			DeviceManager.equivCmds.add(DsCmd.cmdRemove(getStatus().clockId, getStatus().atTime + T, objName()));
			getStatus().brightness = 0;
			getStatus().currentSlide++;
			DeviceManager.equivCmds.add(DsCmd.cmdAddImage(getStatus().clockId, getStatus().atTime, objName(), "ShowPath\\IMAGES\\"+objName()+".TGA"));
			DeviceManager.equivCmds.add(DsCmd.cmdLocate(getStatus().clockId, getStatus().atTime, objName(), 0, 
					DEFAULT_AZIMUTH, DEFAULT_ELEVATION, DEFAULT_WIDTH, DEFAULT_HEIGHT));

		}
		getStatus().atTime += T;
		getStatus().state = DeviceState.END_TRANSITION;
		this.recordStatus();
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

	public void forward(int n) {
		DeviceManager.equivCmds.add(DsCmd.cmdRemove(getStatus().clockId, getStatus().atTime, objName()));
		getStatus().currentSlide += n;
		this.recordStatus();
		DeviceManager.equivCmds.add(DsCmd.cmdAddImage(getStatus().clockId, getStatus().atTime, objName(), "ShowPath\\IMAGES\\"+objName()+".TGA"));
		DeviceManager.equivCmds.add(DsCmd.cmdLocate(getStatus().clockId, getStatus().atTime, objName(), 0, 
				DEFAULT_AZIMUTH, DEFAULT_ELEVATION, DEFAULT_WIDTH, DEFAULT_HEIGHT));
	}
	
	public String objName() {
		return String.format("%s_%d", super.objName(), getStatus().currentSlide); 
	}
	
	public void setViewPosition(int A, int E, int W, int H) {		
		DEFAULT_AZIMUTH = A;
		DEFAULT_ELEVATION = E;
		DEFAULT_WIDTH = W;
		DEFAULT_HEIGHT = H;
	}
}
