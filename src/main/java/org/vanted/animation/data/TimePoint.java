package org.vanted.animation.data;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public abstract class TimePoint
{
	private double time;
	public TimePoint(double time)
	{
		this.time = time;
	}
	public double getTime()
	{
		return time;
	}
	public void setTime(double time) 
	{
		this.time = time;
	}
}