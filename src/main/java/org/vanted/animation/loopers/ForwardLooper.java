package org.vanted.animation.loopers;

import java.util.List;

import org.vanted.animation.data.InterpolatableTimePoint;

/**
 * Similar to a StandardLooper but allows interpolators to interpolate from the
 * end of the animation to the start of the animation. <br>
 * Note: This looper should only be used with that are continuous. Use a
 * StandardLooper for animations that do not have a continuous element.
 * (continuous animations usually inherit from ContinousAnimation. <br>
 * Note: A ForwardLooper should only be used if the animation's loopDuration is
 * longer than the largest time value in the animation's set of data points.
 * 
 * @author - Patrick Shaw
 */
public class ForwardLooper extends Looper {
	public ForwardLooper() {
	}
	
	@Override
	protected int getIndexBeforePreviousPoint(int kthIndex, int dataPointsSize, int pointsBefore, int pointsAfter) {
		return kthIndex < 0 ? dataPointsSize + (kthIndex) : kthIndex;
	}
	
	@Override
	protected int getIndexAfterPreviousPoint(int kthIndex, int dataPointsSize, int pointsBefore, int pointsAfter) {
		return kthIndex % dataPointsSize;
	}
	
	@Override
	public <V, T extends InterpolatableTimePoint<V>> double getNormalizedTime(double time, double loopDuration,
			List<T> dataPoints, List<T> pointsUsed, T previousPoint, T nextPoint) {
		double normalizedTime = Double.NaN;
		if (dataPoints.get(dataPoints.size() - 1).getTime() <= time) {
			double totalTime = loopDuration - previousPoint.getTime();
			totalTime += nextPoint.getTime();
			normalizedTime = (time - previousPoint.getTime()) / totalTime;
		} else {
			normalizedTime = (time - previousPoint.getTime()) / (nextPoint.getTime() - previousPoint.getTime());
		}
		// If you get rid of this the StandardLooper will act strange when the
		// time value is larger than the last point's time value
		if (Double.isInfinite(normalizedTime) || Double.isNaN(normalizedTime)) {
			normalizedTime = 1;
		}
		// If you get rid of this the SwingLooper (or any looper that goes backward)
		// will behave strangely
		if (normalizedTime < 0) {
			normalizedTime += 1;
		}
		return normalizedTime;
	}
}
