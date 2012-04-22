/**
 * 
 */
package org.carnegiesciencecenter.buhl;

import java.util.ArrayList;

/**
 * @author Anh Le
 *
 */
public class Slew extends AbstractDevice {

	SlewProjector currentProjector;
	Slew peerSlew;
	double minPosition, maxPosition;
	double minValue, maxValue;
	double tripTime = 1000; 	// time (hundredth of sec) to rotate from 0 to 4095
	double currentV;

	Slew(String name, String ch, ArrayList<DeviceStatus> l) {
		super(name, ch, new SlewStatus(), l);
		type = DeviceTypes.SLEW;
	}

	public SlewStatus getStatus() {
		return (SlewStatus) status;
	}
	
	private double fracPos(double pos) {
		return (pos-minPosition)/(maxPosition-minPosition);
	}
	
	private double fracVal(double val) {
		return (val-minValue)/(maxValue-minValue);
	}
	
	// return degree
	public double toValue(double position) {
		return minValue + (maxValue-minValue)*fracPos(position);
	}
		
	// return a number from 0 to 4095
	public double toPosition(double value) {
		return minPosition + (maxPosition-minPosition)*fracVal(value);
	}
	
	public double getValue() {
		return currentV;
	}
		
	public void setPosition(int S, int n, boolean waitForPeer) {
		getStatus().state = DeviceState.BEGIN_TRANSITION;
		this.recordStatus();

		if (S == 0)
			S = 100;
		int T = (int) (Math.abs(getStatus().position-n)/((maxPosition-minPosition)/tripTime*(((double)S)/100)));
		getStatus().position = n;
		currentV = toValue(n);
		if (!waitForPeer)
			currentProjector.repaint(getStatus().atTime, T);
		getStatus().atTime += T;
		
		getStatus().state = DeviceState.END_TRANSITION;
		this.recordStatus();
	}

	public void flyTo(int T, int n, boolean waitForPeer) {
		getStatus().state = DeviceState.BEGIN_TRANSITION;
		this.recordStatus();

		getStatus().position = n;
		currentV = toValue(n);
		if (!waitForPeer)
			currentProjector.repaint(getStatus().atTime, T);
		getStatus().atTime += T;
		
		getStatus().state = DeviceState.END_TRANSITION;
		this.recordStatus();
	}

	public void flyToV(int T, double v, boolean waitForPeer) {
		getStatus().state = DeviceState.BEGIN_TRANSITION;
		this.recordStatus();

		getStatus().position = toPosition(v);
		currentV = v;
		if (!waitForPeer)
			currentProjector.repaint(getStatus().atTime, T);
		getStatus().atTime += T;
		
		getStatus().state = DeviceState.END_TRANSITION;
		this.recordStatus();
	}

	@Override
	public void loadConfiguration() {
		setRange(-120,120);
		setMinMax(0,4095);
		setTripTime(1000);
		String position = ScriptExplorer.globalConf.getParam(getName(), "RANGE");
		if (position.length() != 0) {
			String pos[] = position.split("\\s");
			double p0 = Double.parseDouble(pos[0]);
			double p1 = Double.parseDouble(pos[1]);
			setRange(p0, p1);
		}
		position = ScriptExplorer.globalConf.getParam(getName(), "MIN_MAX");
		if (position.length() != 0) {
			String pos[] = position.split("\\s");
			double p0 = Double.parseDouble(pos[0]);
			double p1 = Double.parseDouble(pos[1]);
			setMinMax(p0, p1);
		}
		position = ScriptExplorer.globalConf.getParam(getName(), "TRIP_TIME");
		if (position.length() != 0) {
			double p0 = Double.parseDouble(position);
			setTripTime(p0);
		}
	}

	public void setRange(double minV, double maxV) {
		minValue = minV;
		maxValue = maxV;
	}

	public void setMinMax(double minP, double maxP) {
		minPosition = minP;
		maxPosition = maxP;
	}
	
	public void setTripTime(double T) {
		tripTime = T;
	}
}
