package org.vanted.animation.interpolators;

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
	protected double interpolate(double x, double... y) { 
		double x2 = (1-Math.sin(x * Math.PI ))*0.5;
		return linearInterpolation(x2,y[0],y[1]);
	}

}
