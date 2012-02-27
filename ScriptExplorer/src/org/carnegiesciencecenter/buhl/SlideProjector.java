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
	private double DEFAULT_AZIMUTH;
	private double DEFAULT_ELEVATION;
	private double DEFAULT_ROTATION;
	private double DEFAULT_WIDTH;
	private double DEFAULT_HEIGHT;
	
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
	
	public int getSlideNumber() {
		return getStatus().currentSlide;
	}
	
	public void locate(int n) {
		getStatus().currentSlide = n;
		getStatus().state = DeviceState.STABLE;
		this.recordStatus();

		DeviceManager.equivCmds.add(DsCmd.cmdAddImage(getStatus().clockId, getStatus().atTime, objName(), "ShowPath\\IMAGES\\"+objName()+".TGA"));
		DeviceManager.equivCmds.add(DsCmd.cmdLocate(getStatus().clockId, getStatus().atTime, objName(), 0, 
				DEFAULT_AZIMUTH, DEFAULT_ELEVATION, DEFAULT_ROTATION, DEFAULT_WIDTH, DEFAULT_HEIGHT));
	}
	
	public void fade(int T, int n) {
		getStatus().state = DeviceState.BEGIN_TRANSITION;
		this.recordStatus();
		getStatus().brightness = n;
		getStatus().atTime += T;
		getStatus().state = DeviceState.END_TRANSITION;
		this.recordStatus();

		if (n != 0) {
			DeviceManager.equivCmds.add(DsCmd.cmdAddImage(getStatus().clockId, getStatus().atTime, objName(), "ShowPath\\IMAGES\\"+objName()+".TGA"));
			DeviceManager.equivCmds.add(DsCmd.cmdLocate(getStatus().clockId, getStatus().atTime, objName(), 0, 
					DEFAULT_AZIMUTH, DEFAULT_ELEVATION, DEFAULT_ROTATION, DEFAULT_WIDTH, DEFAULT_HEIGHT));
		}
		DeviceManager.equivCmds.add(DsCmd.cmdView(getStatus().clockId, getStatus().atTime, objName(), T, n));
		if (n == 0)
			DeviceManager.equivCmds.add(DsCmd.cmdRemove(getStatus().clockId, getStatus().atTime, objName()));
}
	
	public void dissolve(int T, int n) {
		getStatus().state = DeviceState.BEGIN_TRANSITION;
		this.recordStatus();
		if (getStatus().brightness == 0) {
			getStatus().brightness = n;

			DeviceManager.equivCmds.add(DsCmd.cmdAddImage(getStatus().clockId, getStatus().atTime, objName(), "ShowPath\\IMAGES\\"+objName()+".TGA"));
			DeviceManager.equivCmds.add(DsCmd.cmdLocate(getStatus().clockId, getStatus().atTime, objName(), 0, 
					DEFAULT_AZIMUTH, DEFAULT_ELEVATION, DEFAULT_ROTATION, DEFAULT_WIDTH, DEFAULT_HEIGHT));
			DeviceManager.equivCmds.add(DsCmd.cmdView(getStatus().clockId, getStatus().atTime, objName(), T, n));
		}
		else {
			DeviceManager.equivCmds.add(DsCmd.cmdView(getStatus().clockId, getStatus().atTime, objName(), T, 0));
			DeviceManager.equivCmds.add(DsCmd.cmdRemove(getStatus().clockId, getStatus().atTime + T, objName()));

			getStatus().brightness = 0;
			getStatus().currentSlide++;
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

			DeviceManager.equivCmds.add(DsCmd.cmdAddImage(getStatus().clockId, getStatus().atTime, objName(), "ShowPath\\IMAGES\\"+objName()+".TGA"));
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

	public void forward(int n) {
		DeviceManager.equivCmds.add(DsCmd.cmdRemove(getStatus().clockId, getStatus().atTime, objName()));

		getStatus().currentSlide += n;
		this.recordStatus();

		DeviceManager.equivCmds.add(DsCmd.cmdAddImage(getStatus().clockId, getStatus().atTime, objName(), "ShowPath\\IMAGES\\"+objName()+".TGA"));
		DeviceManager.equivCmds.add(DsCmd.cmdLocate(getStatus().clockId, getStatus().atTime, objName(), 0, 
				DEFAULT_AZIMUTH, DEFAULT_ELEVATION, DEFAULT_ROTATION, DEFAULT_WIDTH, DEFAULT_HEIGHT));
		if (getStatus().brightness != 0)
			DeviceManager.equivCmds.add(DsCmd.cmdView(getStatus().clockId, getStatus().atTime, objName(), 0, 0));
	}
	
	public String objName() {
		return String.format("%s_%d", super.objName(), getStatus().currentSlide); 
	}
	
	public void setViewPosition(double A, double E, double R, double W, double H) {		
		DEFAULT_AZIMUTH = A;
		DEFAULT_ELEVATION = E;
		DEFAULT_ROTATION = R;
		DEFAULT_WIDTH = W;
		DEFAULT_HEIGHT = H;
	}
}
