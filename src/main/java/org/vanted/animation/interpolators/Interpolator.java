package org.vanted.animation.interpolators;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.vanted.animation.LoopType;
import org.vanted.animation.data.ColourPoint;
import org.vanted.animation.data.DoublePoint;
import org.vanted.animation.data.Point2DPoint;
import org.vanted.animation.data.TimePoint;
import org.vanted.animation.data.VectorPoint;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public abstract class Interpolator
{
	private LoopType loopType;
	/**
	 * 
	 * @param isCircularData
	 * See {@link #setIsCircularData(boolean) setIsCircularData(boolean)}
	 */
	public Interpolator(LoopType loopType)
	{
		setLoopType(loopType);
	}
	protected abstract int getPointsBefore();
	protected abstract int getPointsAfter();
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
		protected <DataPointType,T extends TimePoint<DataPointType>>int[] getPointIndexes(List<T> dataPoints, int previousIndex)
		{
			int indexes[] = new int[getPointsBefore() + 1 + getPointsAfter()];
			int k = previousIndex;
			for(int i = 0; i < getPointsBefore();i++)
			{
				k--;
				switch(loopType)
				{
				// TODO: Do something for 'reverse'
				case reverse:
				case none:
					indexes[i] = k < 0 ? 0 : k;
					break;
				case forward:
					indexes[i] = k < 0 ? dataPoints.size() + (k) : k;
					break;
				}
			}
			indexes[getPointsBefore()] = previousIndex;
			int j = previousIndex;
			for(int i = 0; i  < getPointsAfter();i++)
			{
				j++;
				switch(loopType)
				{
				// TODO: Figure something out for 'reverse'
				case reverse:
				case none:
					indexes[getPointsBefore()+1+i] = j >= dataPoints.size() ? dataPoints.size() - 1 : j;
				case forward:
					indexes[getPointsBefore()+1+i] = j % dataPoints.size();
				}
			}
			return indexes;
		}
		/** 
		 * @param time
		 * The amount of time that has elapsed in the animation.
		 * @param duration
		 * The total time of the animation
		 * @param previousPoint
		 * The data point closest to {@code time} where {@code previousPoint.getTime() <= time}.
		 * @param nextPoint
		 * The data point closest to {@code time} where {@code previousPoint.getTime() > time}.
		 * @returns
		 * A value between 1 and 0.
		 * Eg.
		 * Where previousPoint's time = 3
		 * Where nextPoint's time = 5
		 * Where time = 3.2
		 * Returns 0.1
		 */
		protected double getNormalizedTime(double time,double duration, TimePoint previousPoint, TimePoint nextPoint)
		{
			if(previousPoint.getTime() > nextPoint.getTime())
			{
				double totalTime = duration - previousPoint.getTime();
				totalTime += nextPoint.getTime();
				double result = (time - previousPoint.getTime())/ totalTime;
				return result;
			}
			return (time - previousPoint.getTime()) / (nextPoint.getTime() - previousPoint.getTime());
		}
		/**
		 * In the event that you need to perform linear interpolation, you can call this method to
		 * save some time.
		 */
		protected double linearInterpolation(double x,double y1, double y2)
		{
			return y1 + (y2 - y1) * x;
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
		protected <DataValueType,T extends TimePoint<DataValueType>>List<T> getPointsUsed(List<T> dataPoints,int previousIndex)
		{
			int[] indexes = getPointIndexes(dataPoints, previousIndex);
			List<T> points = new ArrayList<T>();
			for(int i = 0; i < indexes.length;i++)
			{
				points.add(dataPoints.get(indexes[i]));
			}
			return points;
		}
		/**
		 * Should call {@link #getNormalizedTime(double, double, TimePoint, TimePoint) getNormalizedTime(double,TimePoint,TimePoint)}
		 * This method exists to specify which two points should be used to get the normalized time.
		 * Eg. If there are 4 points with times: 3,6,7,9 and {@code time} = 6.5 then this method should call
		 * getNormalizedTime(time,duration,dataPoints[1],dataPoints[2])
		 * @param time
		 * The time has elapsed since the start of the animation.
		 * @param duration
		 * The duration of the animation being interpolated.
		 * @param pointsUsed
		 * The points being used for the interpolation.
		 * @return
		 * A value between 0 and 1.
		 */
		protected <DataValueType,T extends TimePoint<DataValueType>> double getNormalizedTime(double time,double duration, List<T> pointsUsed)
		{
			return getNormalizedTime(time,duration,pointsUsed.get(getPointsBefore()), pointsUsed.get(getPointsBefore() + 1));
		}
		/**
		 * Implement your interpolation in this algorithm
		 * @param x
		 * A value between 0 and 1. Typically represents how close you are to a given point.
		 * 1 Means the x value matches the x value of the previous data point.
		 * 0 Means the x value matches the x value of the next data point.
		 * @param y
		 * The y values that you need interpolate between.
		 * @return
		 */
		protected abstract double interpolate(double x, double...y);	
		public <ReturnType,DataPointType extends TimePoint<ReturnType>> ReturnType interpolate(double time, double duration,
				int previousIndex, List<DataPointType> dataPoints)
		{
			List<DataPointType> pointsUsed = getPointsUsed(dataPoints,previousIndex);
			double normalizedTime = getNormalizedTime(time,duration,pointsUsed);

			return (ReturnType) interpolate(normalizedTime,pointsUsed);
		}
		public <ReturnType,DataPointType extends TimePoint<ReturnType>> ReturnType interpolate(double x, List<DataPointType> y)
		{
			DataPointType firstPoint = y.get(0);
			double firstValues[] = firstPoint.getDoubleValues();
			double interpolationStructure[][] = new double[firstValues.length][];
			for(int q = 0; q < firstValues.length;q++)
			{
				interpolationStructure[q] = new double[y.size()];
				interpolationStructure[q][0] = firstValues[q];
			} 
			for(int i = 1; i <y.size();i++)
			{
				double values[] = y.get(i).getDoubleValues();
				for(int q = 0;q < values.length;q++)
					interpolationStructure[q][i] = values[q];
			}
			double newValues[] = new double[firstValues.length];
			for(int q = 0; q < firstValues.length;q++)
			{
				newValues[q] = interpolate(x, interpolationStructure[q]);
			}
			return firstPoint.toDataValue(newValues);
		}
		/**
		 * @param isCircularData
		 * If true: At the end of the dataPoints/animation, the interpolator
		 * will interpolate between the end and start of the dataPoints array.
		 * (Creates a seamless transition between the end of the animation and restarting the animation)
		 * If false: At the end of the dataPoints/animation, the interpolator
		 * will interpolate between the end and the end of the dataPoints array
		 * (Creates an abrupt change when the animation restarts)
		 */
		public void setLoopType(LoopType loopType)
		{
			this.loopType = loopType;
		}
		public LoopType getLoopType()
		{
			return this.loopType;
		}
	}