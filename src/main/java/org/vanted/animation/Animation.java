package org.vanted.animation;

import java.util.ArrayList; 
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.graffiti.attributes.Attributable;
import org.vanted.animation.data.TimePoint;
import org.vanted.animation.loopers.Looper;
import org.vanted.animation.loopers.StandardLooper;
/**
 * The Animation class represents DISCRETE Animation. This class DOES NOT have
 * an interpolator. To use an interpolator, inherit from the ContinousAnimation class.
 * @author - Patrick Shaw
 * 
 */
public abstract class Animation<T extends TimePoint> {
	protected int previousIndex = 0; 
	protected double loopDuration; 
	protected int noLoops; 
	protected double startTime;
	protected Attributable attributable; 
	protected List<T> dataPoints; 
	protected int currentLoopNumber;
	protected Looper looper;
	/**
	 * Creates an animation
	 * @param attributable
	 * The attributable to be animated.
	 * @param dataPoints
	 * The data points that the animation reads data values from to perform the animation.
	 * @param loopDuration
	 * The duration of a loop in milliseconds<br>
	 * <strong>NOTE:</strong> If the loopDuration value is smaller than the 
	 * largest time value of a data point, the animation will stop abruptly. 
	 * @param startTime
	 * The time at which this animation will start in milliseconds.
	 * @param looper
	 * Specifies how the animation handles loops
	 */
	public Animation(
			Attributable attributable, List<T> dataPoints,
			double loopDuration, double startTime, 
			int noLoops, Looper looper
			) 
	{
		this.attributable = attributable;
		this.setDataPoints(dataPoints);
		this.setLoopDuration(loopDuration); 
		this.setStartTime(startTime);
		this.setNoLoops(noLoops);
		this.setLooper(looper);
	}  		
	/**
	 * Creates an animation with a StandardLooper
	 * @param attributable
	 * The attributable to be animated.
	 * @param dataPoints
	 * The data points that the animation reads data values from to perform the animation.
	 * @param loopDuration
	 * The duration of a loop in milliseconds<br>
	 * <strong>NOTE:</strong> If the loopDuration value is smaller than the 
	 * largest time value of a data point, the animation will stop abruptly. 
	 * @param startTime
	 * The time at which this animation will start in milliseconds.
	 */
	public Animation(
			Attributable attributable, List<T> dataPoints,
			double loopDuration, double startTime, 
			int noLoops
			) 
	{
		this.attributable = attributable;
		this.setDataPoints(dataPoints);
		this.setLoopDuration(loopDuration); 
		this.setStartTime(startTime);
		this.setNoLoops(noLoops);
		this.setLooper(new StandardLooper());
	}  	
		
/**
 * Creates an animation with a StandardLooper that loops forever
 * @param attributable
 * The attributable to be animated.
 * @param dataPoints
 * The data points that the animation reads data values from to perform the animation.
 * @param loopDuration
 * The duration of a loop in milliseconds<br>
 * <strong>NOTE:</strong> If the loopDuration value is smaller than the 
 * largest time value of a data point, the animation will stop abruptly. 
 * @param startTime
 * The time at which this animation will start in milliseconds.
 */
public Animation(
		Attributable attributable, List<T> dataPoints,
		double loopDuration, double startTime
		) 
{
	this.attributable = attributable;
	this.setDataPoints(dataPoints);
	this.setLoopDuration(loopDuration); 
	this.setStartTime(startTime);
	this.setNoLoops(NumberOfLoops.INFINITY);
	this.setLooper(new StandardLooper());
}  	
/**
 * Creates an animation with a StandardLooper, infinite loops and starts immediately
 * @param attributable
 * The attributable to be animated.
 * @param dataPoints
 * The data points that the animation reads data values from to perform the animation.
 * @param loopDuration
 * The duration of a loop in milliseconds<br>
 * <strong>NOTE:</strong> If the loopDuration value is smaller than the 
 * largest time value of a data point, the animation will stop abruptly. 
 */
public Animation(
		Attributable attributable, List<T> dataPoints,
		double loopDuration
		) 
{
	this.attributable = attributable;
	this.setDataPoints(dataPoints);
	this.setLoopDuration(loopDuration); 
	this.setStartTime(0);
	this.setNoLoops(NumberOfLoops.INFINITY);
	this.setLooper(new StandardLooper());
}  	
	/**
	 * Sets the duration of a loop.
	 * @param duration
	 * The loop duration in milliseconds.
	 */
	public void setLoopDuration(double duration)
	{
		this.loopDuration = duration;
	}
	/**
	 * Sets the duration of a loop.
	 */
	public void setLoopDuration(LoopDuration duration)
	{
		this.loopDuration = duration.getValue();
	}
	/**
	 * Sets the duration of a loop.
	 * @param duration
	 * The time magnitude.
	 * @param
	 * The unit of time being used.
	 */
	public void setLoopDuration(long duration, TimeUnit timeUnit)
	{
		this.loopDuration = timeUnit.toMillis(duration);
	}
	/**
	 * Get's the loop duration of the animation.
	 * @return
	 * The duration of the loop in milliseconds.
	 */
	public double getLoopDuration()
	{
		return this.loopDuration;
	}
	/**
	 * Sets the number of times that the animation will perform the animation before stopping.
	 * @param noLoops
	 * -1 = Loop forever<br>
	 * 0 = The Animation object will not do anything.<br>
	 * 1 = The Animation object will perform the animation twice.<br>
	 * 2 = The Animation object will perform the animation three times.
	 */
	public void setNoLoops(int noLoops)
	{
		this.noLoops = noLoops;
	}
	/**
	 * Sets the number of times that the animation will perform the animation before stopping.
	 */
	public void setNoLoops(NumberOfLoops noLoops)
	{
		this.noLoops = noLoops.getValue();
	}
	protected void updateLoopNumber(double time)
	{
		int oldLoopNumber = currentLoopNumber; 
		currentLoopNumber = (int)((time - startTime) / loopDuration);
		if(oldLoopNumber != currentLoopNumber)
		{
			previousIndex = looper.getNextLoopPreviousIndex(dataPoints,currentLoopNumber);
		}
	}
	/**
	 * @return
	 * The time at which the animation will stop animating all together.
	 */
	public double getEndTime()
	{
		return startTime + noLoops * loopDuration;
	}
	/**
	 * Sets the looper of the animation: How the animation handles looping.
	 * @param looper
	 * A looper
	 */
	public void setLooper(Looper looper)
	{
		this.looper = looper;  
	}
	
	/**
	 * @param dataPoints
	 * The set of data points that the animation will use to animate with
	 */
	public void setDataPoints(List<T> dataPoints)
	{
		this.dataPoints = new ArrayList<T>();
		Iterator<T> iterator = dataPoints.iterator();
		while(iterator.hasNext())
		{
			this.dataPoints.add(iterator.next());
		}
	}
	/**
	 * Sets the time at which the Animation object will begin its animation
	 * @param startTime
	 * The time at which the animation starts in milliseconds
	 */
	public void setStartTime(double startTime)
	{
		this.startTime = startTime;
	}
	/**
	 * Sets the time at which the Animation object will begin its animation.
	 * @param startTime
	 * The magnitude of the time.
	 * @param timeUnit
	 * The unit of time.
	 */
	public void setStartTime(long startTime, TimeUnit timeUnit)
	{
		this.startTime = timeUnit.toMillis(startTime);
	}
	/**
	 * Sets the time at which the Animation object will begin its animation.
	 * @param startTime
	 * The time at which the animation will start in milliseconds.
	 */
	/**
	 * 
	 * @return
	 */
	public double getStartTime()
	{
		return this.startTime;
	}
	/**
	 * @param time
	 * The time since the start of the loop.
	 * @return
	 * Whether the animation is finished animating all together.
	 */
	public boolean isFinished(double time)
	{
		if (noLoops == -1){return false;}
		if (getEndTime() > time){return false;}
		return true;
	}
	/**
	 * Forces the animation to finish its animation.
	 */
	protected void forceFinishAnimation()
	{ 
		animate(loopDuration);
	}
	/**
	 * Modifies a particular attribute in a node relative to the time elapsed.
	 * @param time
	 * The amount of time that has elapsed so far.
	 */
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
		int oldIndex = previousIndex;
		previousIndex = looper.findPreviousIndex(dataPoints, previousIndex,currentLoopNumber, time);
		if(previousIndex != oldIndex)
		{
			animate(time);
			
		}
	} 
	/**
	 * Implements what the animation is actually animating.<br>
	 * In this method the graphical interface will actually be modified.
	 * @param time
	 * The time since the start of the loop in milliseconds.
	 */
	protected abstract void animate(double time);
	/**
	 * Should be called whenever an animation wants to be restarted.
	 */
	public void reset()
	{ 
		currentLoopNumber = 0;
		previousIndex = looper.getNextLoopPreviousIndex(dataPoints, currentLoopNumber);
	}
}
