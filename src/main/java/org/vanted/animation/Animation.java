package org.vanted.animation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.graffiti.attributes.Attributable;
import org.vanted.animation.data.TimePoint;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public abstract class Animation<DataPointClass extends TimePoint> {
	protected int previousIndex = 0; 
	protected double loopDuration; 
	protected int noLoops; 
	protected double startTime;
	protected Attributable attributable; 
	protected List<DataPointClass> dataPoints; 
	protected double originalPointTimes[];
	protected double endTime;
	protected int currentLoopNumber;
	protected LoopType loopType;
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
			double duration, List<DataPointClass> dataPoints, 
			int noLoops, LoopType loopType) {
		this.attributable = attributable;
		this.setDuration(duration); 
		this.setDataPoints(dataPoints);
		this.setNoLoops(noLoops);
		this.setLoopType(loopType);
	}
	public void setDuration(double duration)
	{
		this.loopDuration = duration;
		updateEndTime();
	}
	public void setLoopDuration(long duration, TimeUnit timeUnit)
	{
		this.loopDuration = timeUnit.toMillis(duration);
		updateEndTime();
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
			previousIndex = 0;
			if(loopType == LoopType.swing)
			{ 
				reverseDataPointTimes();
			}
			onNextLoop();
		}
	}
	protected void onNextLoop()
	{
		
	}
	protected void onFinish()
	{
		
	}
	public double getLoopDuration()
	{
		return this.loopDuration;
	}
	public double getEndTime()
	{
		return endTime;
	}
	public void setLoopType(LoopType loopType)
	{
		this.loopType = loopType;  
	}
	
	public void setDataPoints(List<DataPointClass> dataPoints)
	{
		this.dataPoints = new ArrayList<DataPointClass>();
		Iterator<DataPointClass> iterator = dataPoints.iterator();
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
		if(isFinished(time)){onFinish();return;}
		if (startTime > time){return;}
		updateLoopNumber(time);
		time = getTimeSinceStartOfLoop(time);
		
		int oldIndex = previousIndex;
		recalcPreviousIndex(time);
		if(previousIndex != oldIndex)
			animate(time);
	}
	private void reverseDataPointTimes()
	{
		for(int i =0 ; i < dataPoints.size() / 2; i++)
		{
			DataPointClass d1 = dataPoints.get(i);
			DataPointClass d2 = dataPoints.get(dataPoints.size() - (i+1));
			double temp = d1.getTime();
			d1.setTime(d2.getTime());
			d2.setTime(temp);
		}
		Collections.reverse(dataPoints);
	}
	/*
	 * Calculates the time since the start of the loop
	 */
	protected double getTimeSinceStartOfLoop(double time)
	{ 
		return (time - startTime) % loopDuration;
	}
	protected abstract void animate(double time);
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
	public void reset()
	{ 
		for(int i = 0; i< dataPoints.size();i++)
		{
			dataPoints.get(i).setTime(originalPointTimes[i]);
		}
	}
}
