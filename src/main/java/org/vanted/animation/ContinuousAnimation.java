package org.vanted.animation;
import java.util.List;

import org.graffiti.attributes.Attributable;
import org.graffiti.graph.Node;
import org.vanted.animation.data.InterpolatableTimePoint;
import org.vanted.animation.data.TimePoint;
import org.vanted.animation.interpolators.Interpolator;
import org.vanted.animation.interpolators.LinearInterpolator;
import org.vanted.animation.loopers.Looper;
/**
 * 
 * Similar to a normal animation but contains an Interpolator so that the animation can
 * transition from one data point value to another data point value in a progressive manner.
 * @author - Patrick Shaw
 * 
 */
public abstract class ContinuousAnimation<T extends InterpolatableTimePoint> extends Animation<T>
{
	protected Interpolator interpolator;
	/**
	 * @param interpolator
	 * Determines how values will be interpolated between data points.
	 */
	public ContinuousAnimation(
			Attributable attributable, List<T> dataPoints,
			double loopDuration, double startTime, 
			int noLoops, Looper looper, Interpolator interpolator)
	{
		super(attributable,dataPoints,loopDuration,startTime,noLoops,looper);
		this.interpolator = interpolator;
	}
	/**
	 * Uses a LinearInterpolator
	 */
	public ContinuousAnimation(
			Attributable attributable, List<T> dataPoints,
			double loopDuration, double startTime, 
			int noLoops, Looper looper)
	{
		super(attributable,dataPoints,loopDuration,startTime,noLoops,looper);
		this.interpolator = new LinearInterpolator();
	}
	/**
	 * Uses a LinearInterpolator.
	 */
	public ContinuousAnimation(
			Attributable attributable, List<T> dataPoints,
			double loopDuration, double startTime, 
			int noLoops)
	{
		super(attributable,dataPoints,loopDuration,startTime,noLoops);
		this.interpolator = new LinearInterpolator();
	}
	/**
	 * Uses a LinearInterpolator
	 */
	public ContinuousAnimation(
			Attributable attributable, List<T> dataPoints,
			double loopDuration, double startTime)
	{
		super(attributable,dataPoints,loopDuration,startTime);
		this.interpolator = new LinearInterpolator();
	}
	/**
	 * Uses a LinearInterpolator
	 * @param interpolator
	 * Determines how values will be interpolated between data points.
	 */
	public ContinuousAnimation(
			Attributable attributable, List<T> dataPoints,
			double loopDuration)
	{
		super(attributable,dataPoints,loopDuration);
		this.interpolator = new LinearInterpolator();
	}
	@Override
	public void update(double time, boolean animatorFinished)
	{
		if(isFinished(time))
		{
			forceFinishAnimation();
			return;
		}
		if (startTime > time)
		{
			return;
		}
		updateLoopNumber(time);
		time = looper.getTimeSinceStartOfLoop(currentLoopNumber, startTime, loopDuration, time);
		if (animatorFinished)
		{
			time = time == 0? loopDuration: time;
		}
		previousIndex = looper.findPreviousIndex(dataPoints, previousIndex,currentLoopNumber, time);
		animate(time);
	}
	@Override
	protected void animate(double time)
	{
		animate(time,getInterpolatedValue(time));
	} 
	/**
	 * Get's the interpolated value from there interpolated. <br>
	 * The method does serve any purpose other than to save the developer from
	 * having to specify all of the interpolation parameters
	 * @param time
	 * The time since the start of the animation loop in milliseconds.
	 * @return
	 * An interpolated value.
	 */
	protected <T> T getInterpolatedValue(double time)
	{
		return (T)interpolator.interpolate(time, loopDuration, previousIndex, dataPoints, looper);
	}

	/**
	 * Implements what the animation is actually animating.<br>
	 * In this method the graphical interface will actually be modified.
	 * @param time
	 * The time since the start of the loop in milliseconds.
	 * @param interpolatedValue
	 * The value that the interpolator object has decided is the value that an the
	 * animated attribute needs to be set to.
	 * @see
	 * #getInterpolatedValue(double)
	 */
	protected abstract <T> void animate(double time, T interpolatedValue); 
}