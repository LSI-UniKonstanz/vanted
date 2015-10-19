package org.vanted.animation.interpolators;
/**
 * 
 * @author - Patrick Shaw
 *
 */
public class SineInterpolator extends Interpolator {

	public SineInterpolator() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected int getPointsBefore() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected int getPointsAfter() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	protected double interpolate(double t, double... y) { 
		double x2 = (1-Math.sin(t * Math.PI ))*0.5;
		return linearInterpolation(x2,y[0],y[1]);
	}

	@Override
	public String toString()
	{
		return "Sine Interpolator";
	}
}
