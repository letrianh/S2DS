package org.carnegiesciencecenter.buhl;

public class HelperFunctions
{	
	/**
	 * Converts a String to a double
	 * @param sNum  The String containing the double
	 * @return	The double if possible, or Double.MAX_VALUE otherwise
	 */
	public static double StringToDouble(String sNum)
	{
		sNum = sNum.trim();
		if (sNum.length() == 0)
			return Double.MAX_VALUE;

		try
		{
			double num = Double.parseDouble(sNum);
			return num;
		}
		catch (NumberFormatException e)
		{
			return Double.MAX_VALUE;
		}
	}
}
