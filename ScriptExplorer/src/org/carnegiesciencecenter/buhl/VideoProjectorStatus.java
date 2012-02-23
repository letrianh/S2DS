/**
 * 
 */
package org.carnegiesciencecenter.buhl;

import java.util.HashMap;

/**
 * @author Anh Le
 *
 */
public class VideoProjectorStatus extends DeviceStatus {

	int sourceId;
	AbstractDevice currentSource;
	boolean isFanRunning;
	int brightness;
	int contrast;
	
}
