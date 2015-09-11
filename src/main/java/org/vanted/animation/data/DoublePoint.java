package org.vanted.animation.data;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public class DoublePoint extends TimePoint {
	private double value;
	public DoublePoint(double time, double value) {
		super(time);
		this.value = value;
	}
	public double getValue()
	{
		return value;
	}
	public void setValue(double value)
	{
		this.value = value;
	}

}
