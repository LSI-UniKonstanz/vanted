package org.vanted.animation.data;

import java.awt.Color;

/**
 * @author - Patrick Shaw
 */
public class ColorTimePoint extends InterpolatableTimePoint<Color> {
	public ColorTimePoint(double time, Color color) {
		super(time, color);
	}
	
	public Color getColor() {
		return dataValue;
	}
	
	public void setColor(Color color) {
		setDataValue(color);
	}
	
	public int validateRGB(double colorValue) {
		return (int) ((255 < colorValue) ? 255 : (0 > colorValue) ? 0 : colorValue);
	}
	
	@Override
	public double[] getDoubleValues() {
		return new double[] { dataValue.getRed(), dataValue.getGreen(), dataValue.getBlue(), dataValue.getAlpha() };
	}
	
	@Override
	public Color toDataValue(double[] doubleValues) {
		return new Color(validateRGB(doubleValues[0]), validateRGB(doubleValues[1]), validateRGB(doubleValues[2]),
				validateRGB(doubleValues[3]));
	}
}
