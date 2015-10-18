package org.vanted.animation;

import java.util.ArrayList; 
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.graffiti.attributes.Attributable;
import org.vanted.animation.data.TimePoint;
import org.vanted.animation.loopers.Looper;
/**
 * 
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
	protected double originalPointTimes[];
	protected double endTime;
	protected int currentLoopNumber;
	protected Looper looper;
	/**
	 * 
	 * @param attributable
	 * The attributable to be animated.
	 * @param startTime
	 * The time at which this animation will start 
	 * @param duration
	 * @param dataPoints
	 */
	public Animation(
			Attributable attributable, double startTime,
			double duration, List<T> dataPoints, 
			int noLoops, Looper looper
			) 
	{
		this.attributable = attributable;
		this.setLoopDuration(duration); 
		this.setDataPoints(dataPoints);
		this.setNoLoops(noLoops);
		this.setLooper(looper);
	}  
	public void setLoopDuration(double duration)
	{
		this.loopDuration = duration;
		updateEndTime();
	}
	public void setLoopDuration(long duration, TimeUnit timeUnit)
	{
		this.loopDuration = timeUnit.toMillis(duration);
		updateEndTime();
	}
	public double getLoopDuration()
	{
		return this.loopDuration;
	}
	public void setNoLoops(int noLoops)
	{
		this.noLoops = noLoops;
		updateEndTime();
	}
	protected void updateEndTime()
	{
		this.endTime = startTime + noLoops * loopDuration;
	}
	protected void updateLoopNumber(double time)
	{
		int oldLoopNumber = currentLoopNumber; 
		currentLoopNumber = (int)((time - startTime) / loopDuration);
		if(oldLoopNumber != currentLoopNumber)
		{
			previousIndex = looper.getNextLoopPreviousIndex(dataPoints,currentLoopNumber);
			onNextLoop();
		}
	}
	protected void onNextLoop()
	{
		
	}
	protected void onFinish()
	{
		
	}
	public double getEndTime()
	{
		return endTime;
	}
	public void setLooper(Looper looper)
	{
		this.looper = looper;  
	}
	
	public void setDataPoints(List<T> dataPoints)
	{
		this.dataPoints = new ArrayList<T>();
		Iterator<T> iterator = dataPoints.iterator();
		while(iterator.hasNext())
		{
			this.dataPoints.add(iterator.next());
		}
		originalPointTimes = new double[dataPoints.size()];
		for(int i =0; i < originalPointTimes.length; i++)
		{
			originalPointTimes[i] = dataPoints.get(i).getTime();
		}
	} 
	public long getDuration(TimeUnit timeUnit)
	{
		return TimeUnit.MILLISECONDS.convert((long)loopDuration, timeUnit);
	}
	public void setStartTime(long startTime, TimeUnit timeUnit)
	{
		this.startTime = timeUnit.toMillis(startTime);
	}
	public long getStartTime(TimeUnit timeUnit)
	{
		return TimeUnit.MILLISECONDS.convert((long)loopDuration, timeUnit);
	}
	public boolean isFinished(double time)
	{
		if (noLoops == -1){return false;}
		if (this.endTime > time){return false;}
		return true;
	}
	/**
	 * Modifies a particular attribute in a node relative to the time elapsed.
	 * @param time\
	 * The amount of time that has elapsed so far.
	 */
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

		int oldIndex = previousIndex;
		previousIndex = looper.findPreviousIndex(dataPoints, previousIndex,currentLoopNumber, time);
		if(previousIndex != oldIndex)
		{
			animate(time);
		}
	} 
	protected abstract void animate(double time);
	public void reset()
	{ 
		previousIndex = 0;
		currentLoopNumber = 0;
	}
}
