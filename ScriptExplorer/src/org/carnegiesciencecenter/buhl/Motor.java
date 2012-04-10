/**
 * 
 */
package org.carnegiesciencecenter.buhl;

import java.util.ArrayList;

/**
 * @author Anh Le
 *
 */
public class Motor extends AbstractDevice {

	SlewProjector currentProjector;
	double minWidth, maxWidth;
	double minHeight, maxHeight;
	double minPosition, maxPosition;
	
	@Override
	public MotorStatus getStatus() {
		return (MotorStatus) status;
	}
		
	Motor(String name, String ch, ArrayList<DeviceStatus> l) {
		super(name, ch, new MotorStatus(), l);
		type = DeviceTypes.MOTOR;
		getStatus().zoomLevel = 0;
	}
	
	public void setZoom(int T, int n) {
		getStatus().state = DeviceState.BEGIN_TRANSITION;
		this.recordStatus();

		getStatus().zoomLevel = n;
		currentProjector.repaint(getStatus().atTime, T);
		getStatus().atTime += T;
		
		getStatus().state = DeviceState.END_TRANSITION;
		this.recordStatus();
	}
	
	public double getWidth() {
		return minWidth+(getStatus().zoomLevel-minPosition)/(maxPosition-minPosition)*(maxWidth-minWidth);
	}

	public double getHeight() {
		return minHeight+(getStatus().zoomLevel-minPosition)/(maxPosition-minPosition)*(maxHeight-minHeight);
	}

	@Override
	public void loadConfiguration() {
		setWidthRange(1,50);
		setHeightRange(1,70);
		setMinMax(0,100);
		
		String position = ScriptExplorer.globalConf.getParam(getName(), "WIDTH_RANGE");
		if (position.length() != 0) {
			String pos[] = position.split("\\s");
			double p0 = Double.parseDouble(pos[0]);
			double p1 = Double.parseDouble(pos[1]);
			setWidthRange(p0, p1);
		}

		position = ScriptExplorer.globalConf.getParam(getName(), "HEIGHT_RANGE");
		if (position.length() != 0) {
			String pos[] = position.split("\\s");
			double p0 = Double.parseDouble(pos[0]);
			double p1 = Double.parseDouble(pos[1]);
			setHeightRange(p0, p1);
		}

		position = ScriptExplorer.globalConf.getParam(getName(), "MIN_MAX");
		if (position.length() != 0) {
			String pos[] = position.split("\\s");
			double p0 = Double.parseDouble(pos[0]);
			double p1 = Double.parseDouble(pos[1]);
			setMinMax(p0, p1);
		}
	}

	public void setWidthRange(double min, double max) {
		minWidth = min;
		maxWidth = max;
	}

	public void setHeightRange(double min, double max) {
		minHeight = min;
		maxHeight = max;
	}

	public void setMinMax(double minP, double maxP) {
		minPosition = minP;
		maxPosition = maxP;
	}
}
