package org.vanted.animation.interpolators;

import org.vanted.animation.LoopType;
import org.vanted.animation.data.TimePoint;

/**
 * 
 * @author - Patrick Shaw
 * 
 */
public class LinearInterpolator extends Interpolator {
	public LinearInterpolator(LoopType loopType)
	{
		super(loopType);
	}
	@Override
	protected double interpolate(double x, double...y)
	{
		return linearInterpolation(x,y[0],y[1]);
	}
	@Override
	protected int getPointsBefore()
	{
		return 0;
	}
	@Override
	protected int getPointsAfter()
	{
		return 1;
	}
	@Override
	public String toString()
	{
		return "Linear Interpolator";
	}
}
