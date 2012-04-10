/**
 * 
 */
package org.carnegiesciencecenter.buhl;

/**
 * @author Anh Le
 *
 */
public class Clock {
	private int clock = 0;
	
	Clock(int t) {
		clock = t;
	}
	
	Clock(String s) {
		clock = Clock.timeValue(s);
	}
	
	@Override
	public String toString() {
		return timeString(clock);
	}
	
	public void setClock(int t) {
		clock = t;
	}
	public int getClock() {
		return clock;
	}

	int addClock(int value) {
		clock += value;
		return clock;
	}

	static int timeValue(int h, int m, int s, int ms) {
		return (ms + 100*(s+60*(m+60*h)));
	}
	
	// timecode can be in different formats:
	// 12:34:56.78
	// 34:56.78
	// 56.78
	// may start with +
	static int timeValue(String timecode) {
		// remove all whitespace
		String s = timecode.replaceAll("\\s", "");
		
		if (s.startsWith("+"))
			s = s.substring(1);
		
		if (!s.contains(":")) {
			return (int) (Double.parseDouble(s) * 100);
		}
		else {
			if (s.length()==7 || s.length()==10)
				s = "0" + s;
			if (s.length()==8)
				s = "00:" + s;
			String h_ = s.substring(0, 2);
			String m_ = s.substring(3, 5);
			String s_ = s.substring(6, 8);
			String ms_ = s.substring(9, 11);
			int v = timeValue(Integer.parseInt(h_),Integer.parseInt(m_),Integer.parseInt(s_),Integer.parseInt(ms_));
			return v;
		}
	}
	
	static String timeString(int t) {
		int h, m, s;
		h = t / (60*60*100);
		t -= h*(60*60*100);
		m = t / (60*100);
		t -= m*(60*100);
		s = t/100;
		t -= s*100;
		return String.format("%02d:%02d:%02d.%02d", h, m, s, t);
	}
}
