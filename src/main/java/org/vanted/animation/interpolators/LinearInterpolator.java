package org.vanted.animation.interpolators;

/**
 * 
 * @author - Patrick Shaw
 * 
 */
public class LinearInterpolator extends Interpolator {
	public LinearInterpolator() {
	}

	@Override
	protected double interpolate(double t, double... y) {
		return linearInterpolation(t, y[0], y[1]);
	}

	@Override
	protected int getPointsBefore() {
		return 0;
	}

	@Override
	protected int getPointsAfter() {
		return 1;
	}

	@Override
	public String toString() {
		return "Linear Interpolator";
	}
}
