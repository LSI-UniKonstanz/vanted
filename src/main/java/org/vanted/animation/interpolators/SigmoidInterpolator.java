package org.vanted.animation.interpolators;
import java.awt.Color;
import java.awt.geom.Point2D;

import org.vanted.animation.data.TimePoint;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public class SigmoidInterpolator extends Interpolator {
	public SigmoidInterpolator(boolean isLooping)
	{
		super(isLooping);
	}

	@Override
	protected double interpolate(double x, double... y) {
		double x2 = 1 / (1 + Math.exp(-(12*(x-0.5d))));
		return linearInterpolation(x2,y[0],y[1]);
	}
	
	@Override
	protected TimePoint[] getPointsUsed(double time, TimePoint[] dataPoints, int previousIndex) {
		return getPointsUsed(time, dataPoints, previousIndex, 0, 1);
	}

	@Override
	protected double getNormalizedTime(double time, double duration, TimePoint[] pointsUsed) {
		return getNormalizedTime(time,duration,pointsUsed[0], pointsUsed[1]);
	}
	
	@Override
	public String toString()
	{
		return "Sigmoid Interpolator";
	}
}
