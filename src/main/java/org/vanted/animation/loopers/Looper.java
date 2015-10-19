package org.vanted.animation.loopers;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List; 
import org.vanted.animation.data.TimePoint;
/*
 * The looper class handles the points needed for interpolation at any time
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
	public <V,T extends TimePoint<V>> int findPreviousIndex(List<T> dataPoints,int oldPreviousIndex, int currentLoopNumber, double time)
	{ 
		int newPreviousIndex = oldPreviousIndex;
		for(int nextIndex = oldPreviousIndex + 1; nextIndex < dataPoints.size();nextIndex++)
		{
			if(dataPoints.get(nextIndex).getTime() < time)
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
	public <V,T extends TimePoint<V>> int getNextLoopPreviousIndex(List<T> dataPoints,int newLoopNumber)
	{
		return 0;
	}
	/*
	 * Calculates the time since the start of the loop
	 */
	public double getTimeSinceStartOfLoop(int currentLoopNumber,double startTime, double loopDuration, double time)
	{ 
		return (time - startTime) % loopDuration;
	}
	protected abstract int getIndexBeforePreviousPoint(int kthIndex, int dataPointsSize, int pointsBefore, int pointsAfter);
	protected abstract int getIndexAfterPreviousPoint(int kthIndex, int dataPointsSize, int pointsBefore, int pointsAfter);
}
