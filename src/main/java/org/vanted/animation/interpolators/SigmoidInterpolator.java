package org.vanted.animation.interpolators;

/**
 * 
 * @author - Patrick Shaw Interpolates data between -6 and 6 on a basic sigmoid
 *         function.<br>
 *         In position animations, it will cause the animation to start off
 *         slow, speed up and slow down again as it goes from one data point to
 *         another.<br>
 * 
 */
public class SigmoidInterpolator extends Interpolator {
	public SigmoidInterpolator() {
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
	protected double interpolate(double t, double... y) {
		double x2 = 1 / (1 + Math.exp(-(12 * (t - 0.5d))));
		return linearInterpolation(x2, y[0], y[1]);
	}

	@Override
	public String toString() {
		return "Sigmoid Interpolator";
	}
}
