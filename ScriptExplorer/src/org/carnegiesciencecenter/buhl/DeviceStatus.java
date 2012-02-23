package org.carnegiesciencecenter.buhl;

enum DeviceState {
	BEGIN_TRANSITION,
	END_TRANSITION,
	STABLE
}

public abstract class DeviceStatus {
	
	String deviceName;
	String channelName;
	DeviceState state;
	int atTime;
	int prevTime;
	int clockId;

	@Override
	public String toString() {
		return deviceName + "-" + channelName;
	}

}
