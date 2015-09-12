package org.vanted.animation;

import java.util.List;

import org.graffiti.attributes.Attributable;
import org.vanted.animation.data.TimePoint;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public abstract class Animation<DataPointClass extends TimePoint> {
	protected int previousIndex = 0; 
	protected double duration;
	protected Attributable attributable;
	protected List<DataPointClass> dataPoints;
	public Animation(Attributable attributable,double duration, List<DataPointClass> dataPoints) {
		this.attributable = attributable;
		this.duration = duration;
		this.dataPoints = dataPoints;
	}
	/**
	 * Modifies a particular attribute in a node relative to the time elapsed.
	 * @param time
	 * The amount of time that has elapsed so far.
	 */
	public abstract void animate(double time);
	/**
	 * You will most likely want to call this in the {@link #animate(double) animate} method.
	 */
	protected void recalcPreviousIndex(double time)
	{
		for(int nextIndex = previousIndex + 1; nextIndex < dataPoints.size();nextIndex++)
		{
			if(dataPoints.get(nextIndex).getTime() < time)
			{
				previousIndex = nextIndex;
			}
			else
			{
				break;
			}
		}
	}
	/**
	 * Use this when you want to restart the animation.
	 */
	public void reset()
	{
		previousIndex = 0;
	}
}
