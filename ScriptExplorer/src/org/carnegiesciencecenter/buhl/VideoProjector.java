/**
 * 
 */
package org.carnegiesciencecenter.buhl;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Anh Le
 *
 */
public class VideoProjector extends AbstractDevice {

	static private HashMap<Integer,AbstractDevice> sources = null;

	public VideoProjectorStatus getStatus() {
		return (VideoProjectorStatus) status;
	}

	VideoProjector(String name, String ch, ArrayList<DeviceStatus> l) {
		super(name, ch, new VideoProjectorStatus(), l);
		type = DeviceTypes.VIDEO_PROJECTOR;
		if (sources == null)
			sources = new HashMap<Integer,AbstractDevice>();
	}
	
	static void addSource(int id, AbstractDevice d) {
		sources.put(id, d);
	}

	void selectSource(int id) {
		getStatus().sourceId = id;
		getStatus().currentSource = sources.get(id);
	}

}
