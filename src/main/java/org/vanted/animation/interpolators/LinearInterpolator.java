package org.vanted.animation.interpolators;

import org.vanted.animation.data.TimePoint;

/**
 * 
 * @author - Patrick Shaw
 * 
 */
public class LinearInterpolator extends Interpolator {
	public LinearInterpolator(boolean isLooping)
	{
		super(isLooping);
	}
	@Override
	protected TimePoint[] getPointsUsed(double time, TimePoint dataPoints[], int previousIndex)
	{
		return  getPointsUsed(time,dataPoints,previousIndex,0,1);
	}
	@Override
	protected double getNormalizedTime(double time,double duration, TimePoint pointsUsed[])
	{
		return getNormalizedTime(time,duration,pointsUsed[0], pointsUsed[1]);
	}
	@Override
	protected double interpolate(double x, double...y)
	{
		return linearInterpolation(x,y[0],y[1]);
	}
	@Override
	public String toString()
	{
		return "Linear Interpolator";
	}
}
