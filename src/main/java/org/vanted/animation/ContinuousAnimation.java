package org.vanted.animation;
import java.util.List;

import org.graffiti.attributes.Attributable;
import org.graffiti.graph.Node;
import org.vanted.animation.data.TimePoint;
import org.vanted.animation.interpolators.Interpolator;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public abstract class ContinuousAnimation<DataPointType extends TimePoint> extends Animation<DataPointType>
{
	protected Interpolator interpolator;
	/**
	 * @param interpolator
	 * Determines how values will be interpolated between data points.
	 * @param duration
	 * The total duration of the animation.
	 */
	public ContinuousAnimation(Attributable attributable,double duration,
			Interpolator interpolator, List<DataPointType> dataPoints,int noLoops, LoopType loopType)
	{
		super(attributable,duration,dataPoints,noLoops,loopType);
		this.interpolator = interpolator;
	}
	@Override
	public void update(double time)
	{
		if(isFinished(time)){onFinish();return;}
		if (startTime > time){return;}
		updateLoopNumber(time);
		time = getTimeSinceStartOfLoop(time);
		recalcPreviousIndex(time);
		animate(time);
	}
	@Override
	protected void animate(double time)
	{
		animate(time,getInterpolatedValue(time));
	}
	protected <T> T getInterpolatedValue(double time)
	{
		return (T)interpolator.interpolate(time, duration, previousIndex, dataPoints,loopType);
	}
	protected abstract <T> void animate(double time, T interpolatedValue); 
}