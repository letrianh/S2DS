/**
 * 
 */
package org.carnegiesciencecenter.buhl;

import java.util.ArrayList;

/**
 * @author Anh Le
 *
 */
public class Projector extends AbstractDevice {

	public String DEFAULT_IMAGE_PATH = "ShowPath\\IMAGES\\";
	public String DEFAULT_IMAGE_EXT = ".TGA";
	public String DEFAULT_VIDEO_PATH = "ShowPath\\IMAGES\\";
	public String DEFAULT_VIDEO_EXT = ".MOV";
	public double DEFAULT_AZIMUTH = 0;
	public double DEFAULT_ELEVATION = 0;
	public double DEFAULT_ROTATION = 0;
	public double DEFAULT_WIDTH = 0;
	public double DEFAULT_HEIGHT = 0;

	Projector(String name, String ch, DeviceStatus s, ArrayList<DeviceStatus> l) {
		super(name, ch, s, l);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void loadConfiguration() {
		String position = ScriptExplorer.globalConf.getParam(getName(), "POSITION");
		if (position.length() != 0) {
			String pos[] = position.split("\\s");
			double p0 = Double.parseDouble(pos[0]);
			double p1 = Double.parseDouble(pos[1]);
			double p2 = Double.parseDouble(pos[2]);
			double p3 = Double.parseDouble(pos[3]);
			double p4 = Double.parseDouble(pos[4]);
			setViewPosition(p0, p1, p2, p3, p4);
		}
		DEFAULT_IMAGE_PATH = updateParam("COMMON", "IMAGE_PATH", DEFAULT_IMAGE_PATH);
		DEFAULT_VIDEO_PATH = updateParam("COMMON", "VIDEO_PATH", DEFAULT_VIDEO_PATH);
		DEFAULT_IMAGE_EXT = updateParam("COMMON", "IMAGE_EXT", DEFAULT_IMAGE_EXT);
		DEFAULT_VIDEO_EXT = updateParam("COMMON", "VIDEO_EXT", DEFAULT_VIDEO_EXT);
	}
	
	public void setViewPosition(double A, double E, double R, double W, double H) {		
		DEFAULT_AZIMUTH = A;
		DEFAULT_ELEVATION = E;
		DEFAULT_ROTATION = R;
		DEFAULT_WIDTH = W;
		DEFAULT_HEIGHT = H;
	}
}
