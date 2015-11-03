package org.vanted.animation.loopers;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.vanted.animation.data.InterpolatableTimePoint;
import org.vanted.animation.data.TimePoint;
/**
 * 
 * @author - Patrick Shaw
 * The looper class which points the Interpolator classes need to perform an interpolation.
 * It also works out what the previousIndex is for Animation classes.
 * 
 */
public abstract class Looper {
	/**
	 * @param time
	 * @param dataPoints
	 * @param previousIndex
	 * The last data point that {@code time} has past with respect to time.
	 * @param pointsBefore
	 * The number of points to return that are before {@code dataPoints[previousIndex]}.
	 * @param pointsAfter
	 * The number of points to return that are after {@code dataPoints[previousIndex]}.
	 * @return
	 * Returns a set of indexes relative to the {@code previousIndex}.
	 */
	public <V,T extends TimePoint<V>>int[] getPointIndexes(List<T> dataPoints, int previousIndex
			, int pointsBefore, int pointsAfter)
	{
		int indexes[] = new int[pointsBefore + 1 + pointsAfter];
		int k = previousIndex;
		for(int i = 0; i < pointsBefore;i++)
		{
			k--; 
			indexes[i] = getIndexBeforePreviousPoint(k, dataPoints.size(), pointsBefore, pointsAfter); 
		}
		indexes[pointsBefore] = previousIndex;
		int j = previousIndex;
		for(int i = 0; i  < pointsAfter;i++)
		{
			j++; 
			indexes[pointsBefore+1+i] = getIndexAfterPreviousPoint(j,dataPoints.size(), pointsBefore, pointsAfter);
		}
		//System.out.println(Arrays.toString(indexes));
		return indexes;
	}
	/**
	 * @param x
	 * A value between 0 and 1 that represents where a particular 
	 * values is relative to the two data points it is between.
	 * Eg. If you have two points, (1,5) and (4,5) and you want to
	 * interpolate (2,?) then x=0.25
	 * @param y
	 * The y values used for the particular interpolation.
	 * @returns
	 * An interpolated value.
	 */
	public <V,T extends TimePoint<V>>List<T> getPointsUsed(List<T> dataPoints,int previousIndex,int pointsBefore,int pointsAfter)
	{
		int[] indexes = getPointIndexes(dataPoints, previousIndex, pointsBefore, pointsAfter);
		List<T> points = new ArrayList<T>();
		for(int i = 0; i < indexes.length;i++)
		{
			points.add(dataPoints.get(indexes[i]));
		}
		return points;
	}
	/**
	 * (Provided this method is not overridden) Finds the largest index,
	 * from a set of data points, who's getTime() method is <= time.
	 * @param <V>
	 * The type of data value that the data point holds.
	 * @param <T>
	 * The type of data point that the interpolator is interpolating for.
	 * @param dataPoints
	 * The set of data points that the interpolator is interpolating on
	 * @param oldPreviousIndex
	 * The old index that was being used before this method was called
	 * @param currentLoopNumber
	 * The current loop number of the animation
	 * @param time
	 * The time that has elapsed since the start of the animation loop.
	 * @return
	 * The largest index from a set of data points, who's getTime() return 
	 * value is <= time.
	 * @see org.vanted.animation.data.TimePoint#getTime()
	 */
	public <V,T extends TimePoint<V>> int findPreviousIndex(List<T> dataPoints,int oldPreviousIndex, int currentLoopNumber, double time)
	{ 
		int newPreviousIndex = oldPreviousIndex;
		for(int nextIndex = oldPreviousIndex + 1; nextIndex < dataPoints.size();nextIndex++)
		{
			if(dataPoints.get(nextIndex).getTime() <= time)
			{
				newPreviousIndex = nextIndex;
			}
			else
			{
				break;
			}
		}
		
		return newPreviousIndex;
	}
	/**
	 * Specifies what the previousIndex animation should be set to
	 * when it starts a new loop.
	 * @param dataPoints
	 * The set of data points that the interpolator is interpolating on.
	 * @param newLoopNumber
	 * The animation's new loop number.
	 * @return
	 * The new previousIndex value of the Animation.
	 */
	public <V,T extends TimePoint<V>> int getNextLoopPreviousIndex(List<T> dataPoints,int newLoopNumber)
	{
		return 0;
	}
	/**
	 * Calculates the time since the start of the loop
	 * @param currentLoopNumber
	 * The animation's loop number.
	 * @return
	 * The time since the start of the animation's current loop.
	 */
	public double getTimeSinceStartOfLoop(int currentLoopNumber,double startTime, double loopDuration, double time)
	{ 
		return (time - startTime) % loopDuration;
	}
	/**
	 * This method typically handles how the looper behaves when kthIndex
	 * is below 0<br>
	 * Eg. For a StandardLooper, getIndexBeforePreviousIndex(-1, 5, 2, 2) =  0<br>
	 * Eg. For a ForwardLooper, getIndexBeforePreviousIndex(-1, 5, 2, 2) = 4
	 * @param kthIndex
	 * An index that has not had any range handling/limitations performed on it.
	 * @param dataPointsSize
	 * The number of data points that the Animation class is dealing with
	 * @param pointsBefore
	 * The number of points the interpolator needs before the previousIndex
	 * @param pointsAfter
	 * The number of points the interpolator needs after the previousIndex
	 * @return
	 * The index of the data point set.
	 */
	protected abstract int getIndexBeforePreviousPoint(int kthIndex, int dataPointsSize, int pointsBefore, int pointsAfter);
	/**
	 * This method typically handles how the looper behaves when kthIndex
	 * is equal to or above dataPointsSize<br>
	 * Eg. For a StandardLooper, getIndexBeforePreviousIndex(5, 5, 2, 2) =  4<br>
	 * Eg. For a ForwardLooper, getIndexBeforePreviousIndex(5, 5, 2, 2) = 0
	 * @param kthIndex
	 * An index that has not had any range handling/limitations performed on it.
	 * @param dataPointsSize
	 * The number of data points that the Animation class is dealing with
	 * @param pointsBefore
	 * The number of points the interpolator needs before the previousIndex
	 * @param pointsAfter
	 * The number of points the interpolator needs after the previousIndex
	 * @return
	 * The index of the data point set.
	 */
	protected abstract int getIndexAfterPreviousPoint(int kthIndex, int dataPointsSize, int pointsBefore, int pointsAfter);

	/**
	 * Calculates how far the {@code time} is from the next data point as a percentage.
	 * <br>
	 * Eg. If there are 4 points with times: 3,6,7,9 and {@code time} = 6.5 
	 * then this method will return 0.5 because 6.5-6/7-6 = 0.5
	 * @param <V>
	 * The type of data value that the data point holds.
	 * @param <T>
	 * The type of data point that the interpolator is interpolating for.
	 * @param time
	 * The time has elapsed since the start of the animation.
	 * @param loopDuration
	 * How long each loop takes in milliseconds
	 * @param dataPoints
	 * All the dataPoints that the interpolator needs to interpolate between
	 * @param pointsUsed
	 * The points being used for the interpolation. 
	 * @return
	 * A value between 0 and 1.
	 */
	public <V,T extends InterpolatableTimePoint<V>> double getNormalizedTime(
			double time,double loopDuration, List<T> dataPoints, List<T> pointsUsed,
			T previousPoint, T nextPoint)
	{
		double normalizedTime = 0;
		if(dataPoints.get(dataPoints.size() - 1).getTime() <= time)
		{
			return normalizedTime = 1;
		}
		else
		{
			normalizedTime =  (time - previousPoint.getTime()) / (nextPoint.getTime() - previousPoint.getTime());
		}
		// If you get rid of this the StandardLooper will act strange when the 
		// time value is larger than the last point's time value
		if (Double.isInfinite(normalizedTime) || Double.isNaN(normalizedTime))
		{
				normalizedTime = 1;
		}
		// If you get rid of this the SwingLooper (or any looper that goes backward)
		// will behave strangely
		if (normalizedTime < 0)
		{
			normalizedTime += 1;
		}
		return normalizedTime;
	}
}
