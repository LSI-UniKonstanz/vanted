package org.vanted.animation.interpolators;
import org.vanted.animation.LoopType;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public class CosineInterpolator extends Interpolator{
	public CosineInterpolator()
	{ 
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
