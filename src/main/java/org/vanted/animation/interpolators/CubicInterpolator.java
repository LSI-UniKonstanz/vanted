package org.vanted.animation.interpolators;
import java.awt.Color;
import java.awt.geom.Point2D;

import org.vanted.animation.data.TimePoint;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public class CubicInterpolator extends Interpolator {
	public CubicInterpolator(boolean isLooping)
	{
		super(isLooping);
	}
	
	@Override
	protected double interpolate(double x, double... y) {
		double x2 = x * x;
		double a0 = (y[3] - y[2]) - (y[0] - y[1]);
		double a1 = (y[0] - y[1]) - a0;
		double a2 = y[2] - y[0];
		double a3 = y[1];
		return (a0 * x * x2 + a1 * x2 + a2 *x + a3);
	}

	@Override
	protected TimePoint[] getPointsUsed(double time, TimePoint[] dataPoints, int previousIndex) {
		return getPointsUsed(time, dataPoints, previousIndex, 1, 2);
	}

	@Override
	protected double getNormalizedTime(double time, double duration, TimePoint[] pointsUsed) {
		return getNormalizedTime(time, duration, pointsUsed[1], pointsUsed[2]);
	}
	
	@Override
	public String toString()
	{
		return "Cubic Interpolator";
	}
}
