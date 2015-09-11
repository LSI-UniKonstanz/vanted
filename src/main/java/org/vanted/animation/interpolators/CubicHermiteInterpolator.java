package org.vanted.animation.interpolators;
import java.awt.Color;
import java.awt.geom.Point2D;

import org.vanted.animation.data.TimePoint;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public class CubicHermiteInterpolator extends Interpolator{
	public CubicHermiteInterpolator(boolean isLooping) {
		super(isLooping);
	}

	@Override
	public double interpolate(double x, double...y)
	{	    
		double c0 = y[1];
		double c1 = 0.5 * (y[2] - y[0]);
		double c2 = y[0] - (2.5 * y[1]) + (2 * y[2]) - (0.5 * y[3]);
		double c3 = (0.5 * (y[3] - y[0])) + (1.5 * (y[1] - y[2]));
		return (((((c3 * x) + c2) * x) + c1) * x) + c0;
	}
	
	@Override
	protected TimePoint[] getPointsUsed(double time, TimePoint[] dataPoints, int previousIndex) {
		return getPointsUsed(time,dataPoints,previousIndex,0, 3);
	}

	@Override
	protected double getNormalizedTime(double time, double duration, TimePoint[] pointsUsed) {
		return getNormalizedTime(time, duration, pointsUsed[0], pointsUsed[1]);
	}
	
	@Override
	public String toString()
	{
		return "Cubic Hermite Interpolation";
	}
}
