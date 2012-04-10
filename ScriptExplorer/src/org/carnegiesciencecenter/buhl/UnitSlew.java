package org.carnegiesciencecenter.buhl;

import org.jdom.Element;

public class UnitSlew extends Unit
{
	private double 	minPosition, maxPosition, 
					minValue, maxValue, 
					tripTime;
	public static final String SLEW = "Slew";
	public static final String MIN_POS = "minpos";
	public static final String MAX_POS = "maxpos";
	public static final String MIN_VAL = "minval";
	public static final String MAX_VAL = "maxval";
	public static final String TRIP_TIME = "triptime";
	
	public UnitSlew(String name, double MinPos, double MaxPos, double MinValue, double MaxValue, double TripTime)
	{
		super(name, SLEW);
		
		setMinPos(MinPos);
		setMaxPos(MaxPos);
		setMinValue(MinValue);
		setMaxValue(MaxValue);
		setTripTime(TripTime);
	}
	
	public UnitSlew(String name, String MinPos, String MaxPos, String MinValue, String MaxValue, String TripTime)
	{
		super(name, SLEW);
		
		setMinPos(maxToZeroOrOriginal(HelperFunctions.StringToDouble(MinPos)));
		setMaxPos(maxToZeroOrOriginal(HelperFunctions.StringToDouble(MaxPos)));
		setMinValue(maxToZeroOrOriginal(HelperFunctions.StringToDouble(MinValue)));
		setMaxValue(maxToZeroOrOriginal(HelperFunctions.StringToDouble(MaxValue)));
		setTripTime(maxToZeroOrOriginal(HelperFunctions.StringToDouble(TripTime)));
	}
	
	public void setMinPos(double minPos)
	{
		minPosition = minPos;
	}
	
	public void setMaxPos(double maxPos)
	{
		maxPosition = maxPos;
	}
	
	public void setMinValue(double MinValue)
	{
		minValue = MinValue;
	}
	
	public void setMaxValue(double MaxValue)
	{
		maxValue = MaxValue;
	}

	public double getMinPos()
	{
		return minPosition;
	}
	
	public double getMaxPos()
	{
		return maxPosition;
	}

	public double getMinValue()
	{
		return minValue;
	}
	
	public double getMaxValue()
	{
		return maxValue;
	}
	
	public double getTripTime()
	{
		return tripTime;
	}
	
	public void setTripTime(double TripTime)
	{
		tripTime = TripTime;
	}
	
	/**
	 * Gets the JDOM XML Element that represents this Unit.
	 * @return	The JDOM XML Element that describes this Unit
	 */
	@Override
	public Element getXML()
	{
		Element unitElement = super.getXML();
		
		unitElement.setAttribute(MIN_POS, String.valueOf(minPosition));
		unitElement.setAttribute(MAX_POS, String.valueOf(maxPosition));
		unitElement.setAttribute(MIN_VAL, String.valueOf(minValue));
		unitElement.setAttribute(MAX_VAL, String.valueOf(maxValue));
		unitElement.setAttribute(TRIP_TIME, String.valueOf(tripTime));
		
		return unitElement;
	}
}
