package org.vanted.animation.interpolators;  
/**
 * 
 * Uses the same mathematical equation as a CubicInterpolator but with different 
 * constants. This provides a smoother interpolation and lessens the 'overshooting' 
 * effect that is apparent with the CubicInterpolator.
 * @author - Patrick Shaw
 * 
 */
public class SmoothCubicInterpolator extends Interpolator{
	public SmoothCubicInterpolator() { 
	}
	@Override
	public double interpolate(double t, double...y)
	{	    
		double c0 = y[1];
		double c1 = 0.5 * (y[2] - y[0]);
		double c2 = y[0] - (2.5 * y[1]) + (2 * y[2]) - (0.5 * y[3]);
		double c3 = (0.5 * (y[3] - y[0])) + (1.5 * (y[1] - y[2]));
		return (((((c3 * t) + c2) * t) + c1) * t) + c0;
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
		return "Smooth Cubic Interpolator";
	}
} 