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
	}
	
	public void fade(int T, int n) {
		getStatus().state = DeviceState.BEGIN_TRANSITION;
		this.recordStatus();
		getStatus().brightness = n;
		getStatus().atTime += T;
		getStatus().state = DeviceState.END_TRANSITION;
		this.recordStatus();
	}
	
	public void dissolve(int T, int n) {
		getStatus().state = DeviceState.BEGIN_TRANSITION;
		this.recordStatus();
		if (getStatus().brightness == 0)
			getStatus().brightness = n;
		else {
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
		if (getStatus().brightness == 0)
			getStatus().brightness = n;
		else
			getStatus().brightness = 0;
		getStatus().atTime += T;
		getStatus().state = DeviceState.END_TRANSITION;
		this.recordStatus();
	}

	public void forward(int n) {
		getStatus().currentSlide += n;
		this.recordStatus();
	}
}
