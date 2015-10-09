package org.vanted.animation.interpolators;
import org.vanted.animation.LoopType;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public class CubicInterpolator extends Interpolator {
	public CubicInterpolator()
	{ 
	}
	
	@Override
	protected int getPointsBefore()
	{
		return 1;
	}
	@Override
	protected int getPointsAfter()
	{
		return 2;
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
	public String toString()
	{
		return "Cubic Interpolator";
	}
}


