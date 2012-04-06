/**
 * 
 */
package org.carnegiesciencecenter.buhl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Anh Le
 *
 */
public class Settings {
	public String DEFAULT_CONFIG_FILE = "settings.conf";
	public String DEFAULT_XML_CONFIG_FILE = "master.xml";
	private HashMap<String,HashMap<String,String>> conf = null;
	private HashMap<String, Bank> banks;

	public void loadSettings() {
		loadSettings(DEFAULT_CONFIG_FILE);
	}
	
	public void loadSettings(String fileName) {
		String sectionName;
		try {
			File f = new File(fileName);
			System.out.println("Reading file: " + f.getAbsolutePath());
			FileReader fr = new FileReader(f);
			BufferedReader b = new BufferedReader(fr);
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
			fr.close();
		}
		catch (Exception e) {
			System.out.println("Error reading file: " + fileName);
		}
	}
	
	public void openXMLConfigFile()
	{
		openXMLConfigFile(DEFAULT_XML_CONFIG_FILE);
	}
	
	/**
	 * Given a filename, reads in the XML planetarium information and makes it 
	 * available for use.
	 * @param filename	The filename of the XML data file
	 */
	public void openXMLConfigFile(String filename)
	{
		SAXBuilder builder = new SAXBuilder();
		
		try 
		{
			File file = new File(filename);
			Document doc = builder.build(file);
			
			Element root = doc.getRootElement();
			List elements = root.getChildren();
			Iterator iter = elements.iterator();
			
			banks = new HashMap<String, Bank>();	// Reset banks
			
			while (iter.hasNext())	// For each Bank
			{
				// Read Bank
				Element elem = (Element) iter.next();
				String sAction = elem.getAttributeValue(Bank.ACTION);
				Bank.CommandAction cmdAction;
				if (sAction.equals(Bank.KEEPING_MOST))
					cmdAction = Bank.CommandAction.KEEP;
				else if (sAction.equals(Bank.DISCARD_MOST))
					cmdAction = Bank.CommandAction.DISCARD;
				else	// sAction.equals(Bank.CONVERT_MOST)
					cmdAction = Bank.CommandAction.CONVERT_TO_DS;
				
				String deviceType = elem.getAttributeValue(Bank.DEVICE_TYPE);
				Bank bank = new Bank(elem.getAttributeValue(Bank.NAME), deviceType, cmdAction);
				// Read any special-case commands or units
				List commands = elem.getChildren();
				Iterator cIter = commands.iterator();
				while (cIter.hasNext())
				{
					Element cElem = (Element) cIter.next();
					// Find out what to do with the action
					String elementType = cElem.getName();	// Get name of thing
					if (elementType.equals("command"))
					{
						Bank.CommandAction ca = null;
						String actionName = cElem.getAttributeValue(Bank.ACTION);
						if (actionName.equals(Bank.DISCARD))
							ca = Bank.CommandAction.DISCARD;
						else if (actionName.equals(Bank.CONVERT))
							ca = Bank.CommandAction.CONVERT_TO_DS;
						else	// Keep
							ca = Bank.CommandAction.KEEP;
						// Add the new action to the bank
						bank.setCommand(cElem.getAttributeValue(Bank.NAME), ca);
					}
					else if (elementType.equals("unit"))
					{
						Unit unit;
						String unitName = cElem.getAttributeValue(Unit.NAME);
						
						if (deviceType.equals(UnitMotor.MOTOR))
						{
							unit = new UnitMotor(
									unitName,
									cElem.getAttributeValue(UnitMotor.MIN_WIDTH),
									cElem.getAttributeValue(UnitMotor.MAX_WIDTH),
									cElem.getAttributeValue(UnitMotor.MIN_HEIGHT),
									cElem.getAttributeValue(UnitMotor.MAX_HEIGHT),
									cElem.getAttributeValue(UnitMotor.MIN_POS),
									cElem.getAttributeValue(UnitMotor.MAX_POS));
						}
						else if (deviceType.equals(UnitSlew.SLEW))
						{
							// String name, double MinPos, double MaxPos, double MinValue, double MaxValue
							unit = new UnitSlew(
									unitName,
									cElem.getAttributeValue(UnitSlew.MIN_POS),
									cElem.getAttributeValue(UnitSlew.MAX_POS),
									cElem.getAttributeValue(UnitSlew.MIN_VAL),
									cElem.getAttributeValue(UnitSlew.MAX_VAL),
									cElem.getAttributeValue(UnitSlew.TRIP_TIME));
						}
						else if (deviceType.equals(UnitSlideProjector.SLIDE_PROJ))
						{
							unit = new UnitSlideProjector(
									unitName,
									cElem.getAttributeValue(UnitSlideProjector.AZIMUTH),
									cElem.getAttributeValue(UnitSlideProjector.ELEVATION),
									cElem.getAttributeValue(UnitSlideProjector.ROTATION),
									cElem.getAttributeValue(UnitSlideProjector.WIDTH),
									cElem.getAttributeValue(UnitSlideProjector.HEIGHT));
						}
						else if (deviceType.equals(UnitVideoProjector.VIDEO_PROJ))
						{
							unit = new UnitVideoProjector(
									unitName,
									cElem.getAttributeValue(UnitSlideProjector.AZIMUTH),
									cElem.getAttributeValue(UnitSlideProjector.ELEVATION),
									cElem.getAttributeValue(UnitSlideProjector.ROTATION),
									cElem.getAttributeValue(UnitSlideProjector.WIDTH),
									cElem.getAttributeValue(UnitSlideProjector.HEIGHT));
						}
						else
							unit = new Unit(unitName, deviceType);
						bank.setUnit(unit);
					}
				}
				addBank(bank);
			}
			
		} 
		catch (Exception e)
		{
			System.out.println("Couldn't read file");
		}
		
		// Now, convert to Andy's format
		convertToAndysFormat();
	}
	
	/**
	 * Takes the HashMap of bank information and converts it into
	 * the sort of information that Andy's application expects
	 */
	private void convertToAndysFormat()
	{
		String sectionName;
		if (conf == null)
			conf = new HashMap<String,HashMap<String,String>>();
		HashMap<String,String> section = null;
		
		String unusedDevices  = "";
		String spiceDevices   = "";
		String managedDevices = "";
		
		Object bankArray[] = banks.values().toArray();
		for (Object ob : bankArray)
		{
			Bank b = (Bank) ob;
			Unit units[] = b.getUnits();
			SpiceCommand commands[] = b.getSpecialCommands();
			
			switch(b.checkIfKeepingMost())
			{
			case CONVERT_TO_DS:
				managedDevices += " " + b.toString();
				break;
			case DISCARD:
				unusedDevices += " " + b.toString();
				break;
			default:	// KEEP
				spiceDevices += " " + b.toString();
				break;
			}
			
			for (Unit u : units)
			{
				sectionName = b.toString() + "-" + u.getName();
				section = new HashMap<String,String>();
				conf.put(sectionName, section);
				
				String deviceType = u.getDeviceType();
				if (deviceType.equals(UnitMotor.MOTOR))
				{
					UnitMotor un = (UnitMotor) u;
					section.put("WIDTH_RANGE", "" + un.getMinWidth() + " " + un.getMaxWidth());
					section.put("HEIGHT_RANGE", "" + un.getMinHeight() + " " + un.getMaxHeight());
					section.put("MIN_MAX", un.getMinPos() + " " + un.getMaxPos());
				}
				else if (deviceType.equals(UnitSlew.SLEW))
				{
					UnitSlew un = (UnitSlew) u;
					section.put("RANGE", "" + un.getMinValue() + " " + un.getMaxValue());
					section.put("MIN_MAX", + un.getMinPos() + " " + un.getMaxPos());
					section.put("TRIP_TIME", "" + un.getTripTime());
				}
				else if (deviceType.equals(UnitSlideProjector.SLIDE_PROJ))
				{
					UnitSlideProjector un = (UnitSlideProjector) u;
					section.put("POSITION", 	"" + un.getAzimuth() 
											+ " " + un.getElevation() 
											+ " " + un.getRotation() 
											+ " " + un.getWidth() 
											+ " " + un.getHeight());
				}
				else if (deviceType.equals(UnitVideoProjector.VIDEO_PROJ))
				{
					UnitVideoProjector un = (UnitVideoProjector) u;
					section.put("POSITION", 	"" + un.getAzimuth() 
											+ " " + un.getElevation() 
											+ " " + un.getRotation() 
											+ " " + un.getWidth() 
											+ " " + un.getHeight());
				}
				else
				{
					
				}
				
			}
			
			for (SpiceCommand sc : commands)
			{
				
			}
			
			// Add in whether the different devices are used, unused, or managed
			if (!conf.containsKey("COMMON"))
				conf.put("COMMON", new HashMap<String,String>());
			HashMap<String,String> common = conf.get("COMMON");
			
			common.put("UNUSED_DEVICES", unusedDevices);
			common.put("SPICE_DEVICES", spiceDevices);
			common.put("MANAGED_DEVICES", managedDevices);
		}
	}
	
	/**
	 * Adds the given bank to the list of Banks.
	 * @param bank	The Bank to be added
	 */
	private void addBank(Bank bank)
	{
		banks.put(bank.toString(), bank);
	}
	
	public void overwriteSettings(String srcSection, String desSection) {
		if (conf == null)
			return;
		HashMap<String,String> src = conf.get(srcSection);
		HashMap<String,String> des = conf.get(desSection);
		if (src == null || des == null)
			return;
		des.putAll(src);
		System.out.println("New settings:");
		System.out.println(des.toString());
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
