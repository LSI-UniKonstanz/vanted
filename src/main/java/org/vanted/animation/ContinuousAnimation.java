package org.vanted.animation;
import java.util.List;

import org.graffiti.attributes.Attributable;
import org.graffiti.graph.Node;
import org.vanted.animation.data.TimePoint;
import org.vanted.animation.interpolators.Interpolator;
import org.vanted.animation.loopers.Looper;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public abstract class ContinuousAnimation<T extends TimePoint> extends Animation<T>
{
	protected Interpolator interpolator;
	/**
	 * @param interpolator
	 * Determines how values will be interpolated between data points.
	 * @param duration
	 * The total duration of the animation.
	 */
	public ContinuousAnimation(Attributable attributable,double startTime,double duration,
			Interpolator interpolator, List<T> dataPoints,int noLoops, Looper looper)
	{
		super(attributable,startTime,duration,dataPoints,noLoops,looper);
		this.interpolator = interpolator;
	}
	@Override
	public void update(double time)
	{
		if(isFinished(time))
		{
			onFinish();
			return;
		}
		if (startTime > time)
		{
			return;
		}
		updateLoopNumber(time);
		time = looper.getTimeSinceStartOfLoop(currentLoopNumber, startTime, loopDuration, time);
		System.out.println(time); 
		previousIndex = looper.findPreviousIndex(dataPoints, previousIndex,currentLoopNumber, time);
		animate(time);
	}
	@Override
	protected void animate(double time)
	{
		animate(time,getInterpolatedValue(time));
	} 
	protected <T> T getInterpolatedValue(double time)
	{
		return (T)interpolator.interpolate(time, loopDuration, previousIndex, dataPoints, looper);
	}
	protected abstract <T> void animate(double time, T interpolatedValue); 
}