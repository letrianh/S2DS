/**
 * 
 */
package org.carnegiesciencecenter.buhl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;

/**
 * @author Anh Le
 *
 */
public class Settings {
	public String DEFAULT_CONFIG_FILE = "/home/lion/Downloads/CSC/SHOW/settings.conf";
	private HashMap<String,HashMap<String,String>> conf = null;

	public void loadSettings() {
		loadSettings(DEFAULT_CONFIG_FILE);
	}
	
	public void loadSettings(String fileName) {
		System.out.println("Reading file: " + fileName);
		String sectionName;
		try {
			FileReader f = new FileReader(fileName);
			BufferedReader b = new BufferedReader(f);
			if (conf == null)
				conf = new HashMap<String,HashMap<String,String>>();
			String s;
			HashMap<String,String> section = null;
			while((s = b.readLine()) != null) {
				s = s.trim();
				if (s.length() == 0 || s.startsWith(";") || s.startsWith("'") || s.startsWith("//"))
					continue;
				if (s.startsWith("[")) {	// new section
					sectionName = s.substring(1, s.indexOf("]")).trim().toUpperCase();
					if (conf.containsKey(sectionName)) {
						System.out.println("Update existing section: [" + sectionName + "]");
						section = conf.get(sectionName);
					}
					else {
						System.out.println("Adding new section: [" + sectionName + "]");
						section = new HashMap<String,String>();
						conf.put(sectionName, section);
					}
				}
				else {
					if (section != null) {
						String pos[] = s.split("=");
						if (pos.length == 2) {
							String key = pos[0].trim().toUpperCase();
							if (section.containsKey(key))
								System.out.println("Update pair\t: " + key + "=" + pos[1]);
							else
								System.out.println("Add pair\t: " + key + "=" + pos[1]);
							section.put(key, pos[1]);
						}
						else
							System.out.println("Invalid configuration line: " + s);
					}
				}
			}
			b.close();
			f.close();
		}
		catch (Exception e) {
			System.out.println("Error reading file: " + fileName);
		}
	}
	
	public String getParam(String sectionName, String key) {
		HashMap<String,String> s;
		s = conf.get(sectionName);
		if (s == null)
			return "";
		else {
			String val = s.get(key);
			if (val == null)
				return "";
			else return val;
		}
	}

}
