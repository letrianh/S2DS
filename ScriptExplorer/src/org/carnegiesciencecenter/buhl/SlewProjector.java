package org.carnegiesciencecenter.buhl;

import java.util.ArrayList;

public class SlewProjector extends SlideProjector {

	Motor zoomMotor;
	Slew xSlew, ySlew;

	SlewProjector(String name, String ch, ArrayList<DeviceStatus> l, int max) {
		super(name, ch, l, max);
		type = DeviceTypes.SLEW_PROJECTOR;
		zoomMotor = null;
		xSlew = null;
		ySlew = null;
	}

	SlewProjector(String name, String ch, ArrayList<DeviceStatus> l) {
		this(name, ch, l, DEFAULT_MAX_NUM);
	}

	public void updateViewPosition() {
		setViewPosition(xSlew.getValue(), ySlew.getValue(), 0, 
				zoomMotor.getWidth(), zoomMotor.getHeight());
	}
	
	public void repaint(int t0, int T) {
		updateViewPosition();
		getStatus().atTime = t0;
		DeviceManager.equivCmds.add(DsCmd.cmdLocate(getStatus().clockId, getStatus().atTime, objName(), T, 
				DEFAULT_AZIMUTH, DEFAULT_ELEVATION, DEFAULT_ROTATION, DEFAULT_WIDTH, DEFAULT_HEIGHT));
	}
	
	public void repaintNaturally(int t0, int T) {
		updateViewPosition();
		getStatus().atTime = t0;
		DeviceManager.equivCmds.add(DsCmd.cmdLocateNaturally(getStatus().clockId, getStatus().atTime, objName(), T, 
				DEFAULT_AZIMUTH, DEFAULT_ELEVATION, DEFAULT_ROTATION, DEFAULT_WIDTH, DEFAULT_HEIGHT));
	}
	
	public void setup(Slew x, Slew y, Motor m) {
		zoomMotor = m;
		xSlew = x;
		ySlew = y;
		x.currentProjector = this;
		y.currentProjector = this;
		m.currentProjector = this;
		x.peerSlew = y;
		y.peerSlew = x;
	}
	
}
