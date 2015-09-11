package org.vanted.animation.interpolators;
import java.awt.Color;

import org.vanted.animation.data.TimePoint;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public class CosignInterpolator extends Interpolator{
	public CosignInterpolator(boolean isCircularData)
	{
		super(isCircularData);
	}
	
	@Override
	protected TimePoint[] getPointsUsed(double time, TimePoint[] dataPoints, int previousIndex) {
		return getPointsUsed(time,dataPoints,previousIndex,0,1);
	}

	@Override
	protected double getNormalizedTime(double time, double duration, TimePoint[] pointsUsed) {
		return getNormalizedTime(time, duration,pointsUsed[0], pointsUsed[1]);
	}
	
	@Override
	protected double interpolate(double x, double...y)
	{
		double x2 = (1-Math.cos(x * Math.PI ))*0.5;
		return linearInterpolation(x2,y[0],y[1]);
	}
	
	@Override
	public String toString()
	{
		return "Cosign Interpolator";
	}
}
