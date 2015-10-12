package org.vanted.animation.data;
import java.awt.Color;
import java.awt.color.ColorSpace;
import java.util.Arrays;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public class ColorPoint extends TimePoint<Color> {
	public ColorPoint(double time, Color colour)
	{
		super(time,colour);
	} 
	public Color getColor()
	{
		return dataValue;
	}
	public void setColor(Color colour)
	{
		setDataValue(colour);
	}
	@Override
	public double[] getDoubleValues() {
		return new double[]{dataValue.getRed(), dataValue.getGreen(),dataValue.getBlue(),dataValue.getAlpha()};
	}
	public int ValidateRGB(double colourValue)
	{
		return (int) ((255 < colourValue) ? 255 : (0 > colourValue) ? 0 : colourValue); 
	}
	public float convertHSB(double hsbValue)
	{
		return (float)Math.min(1, Math.max(0, hsbValue)); 
	}
	@Override
	public Color toDataValue(double[] doubleValues) {
		return new  Color(ValidateRGB(doubleValues[0]),ValidateRGB(doubleValues[1]),ValidateRGB(doubleValues[2]),ValidateRGB(doubleValues[3]));
	}
}
