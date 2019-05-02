package org.vanted.animation.interpolators;

/**
 * Provides true continuity throughout the animation. <br>
 * Note: Cubic interpolations can 'overshoot' in terms of their interpolated
 * value. <br>
 * Note: This is not a cubic spline interpolation.
 * 
 * @author - Patrick Shaw
 * 
 */
public class CubicInterpolator extends Interpolator {
	public CubicInterpolator() {
	}

	@Override
	protected int getPointsBefore() {
		return 1;
	}

	@Override
	protected int getPointsAfter() {
		return 2;
	}

	@Override
	protected double interpolate(double t, double... y) {
		double x2 = t * t;
		double a0 = (y[3] - y[2]) - (y[0] - y[1]);
		double a1 = (y[0] - y[1]) - a0;
		double a2 = y[2] - y[0];
		double a3 = y[1];
		return (a0 * t * x2 + a1 * x2 + a2 * t + a3);
	}

	@Override
	public String toString() {
		return "Cubic Interpolator";
	}
}
