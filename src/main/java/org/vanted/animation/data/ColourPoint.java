package org.vanted.animation.data;
import java.awt.Color;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public class ColourPoint extends TimePoint {
	Color colour;
	public ColourPoint(double time, Color colour)
	{
		super(time);
		this.colour = colour;
	}
	public Color getColour()
	{
		return colour;
	}
}
