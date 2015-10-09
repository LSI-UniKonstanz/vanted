package org.vanted.animation.interpolators; 
import org.vanted.animation.LoopType; 
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public class SmoothCubicInterpolator extends Interpolator{
	public SmoothCubicInterpolator() { 
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
	protected int getPointsBefore()
	{
		return 0;
	}
	@Override
	protected int getPointsAfter()
	{
		return 3;
	}
	
	@Override
	public String toString()
	{
		return "Cubic Hermite Interpolation";
	}
} 