package org.vanted.animation;

import org.graffiti.attributes.Attributable;
import org.vanted.animation.data.TimePoint;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public abstract class Animation {
	protected int previousIndex = 0; 
	protected double duration;
	protected Attributable attributable;
	public Animation(Attributable attributable,double duration) {
		this.attributable = attributable;
		this.duration = duration;
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
	protected void recalcPreviousIndex(double time, TimePoint dataPoints[])
	{
		for(int nextIndex = previousIndex + 1; nextIndex < dataPoints.length;nextIndex++)
		{
			if(dataPoints[nextIndex].getTime() < time)
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
