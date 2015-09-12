package org.vanted.animation.data;
import java.awt.Color;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public class ColourPoint extends TimePoint<Color> {
	public ColourPoint(double time, Color colour)
	{
		super(time,colour);
	}
	public Color getColour()
	{
		return dataValue;
	}
	@Override
	public double[] getDoubleValues() {
		return new double[]{dataValue.getRed(),dataValue.getBlue(),dataValue.getGreen()};
	}
	public int convert(double colourValue)
	{
		return (int)Math.min(255, Math.max(0, colourValue));
	}
	@Override
	public Color toDataValue(double[] doubleValues) {
		return new Color(convert(doubleValues[0]),convert(doubleValues[1]),convert(doubleValues[2]));
	}
}
