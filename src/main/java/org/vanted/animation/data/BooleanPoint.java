package org.vanted.animation.data;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public class BooleanPoint extends TimePoint {
	private boolean value;
	public BooleanPoint(double time, boolean value) {
		super(time);
		this.value = value;
	}
	public boolean getValue()
	{
		return this.value;
	}
	public void setValue(boolean value)
	{
		this.value = value;
	}
}
