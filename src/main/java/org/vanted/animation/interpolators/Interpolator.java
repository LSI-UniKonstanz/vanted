package org.vanted.animation.interpolators;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.vanted.animation.LoopType;
import org.vanted.animation.data.ColorMode;
import org.vanted.animation.data.ColorPoint;
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
	/**
	 * 
	 * @param isCircularData
	 * See {@link #setIsCircularData(boolean) setIsCircularData(boolean)}
	 */
	public Interpolator()
	{ 
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
	static String oldString = "";
		protected <DataPointType,T extends TimePoint<DataPointType>>int[] getPointIndexes(List<T> dataPoints, int previousIndex, LoopType loopType)
		{
			int pointsBefore = getPointsBefore();
			int pointsAfter = getPointsAfter();
			int indexes[] = new int[pointsBefore + 1 + pointsAfter];
			int k = previousIndex;
			for(int i = 0; i < pointsBefore;i++)
			{
				k--;
				switch(loopType)
				{
				case none:
					indexes[i] = k < 0 ? 0 : k;
					break;
				case swing:
				case forward:
					indexes[i] = k < 0 ? dataPoints.size() + (k) : k;
					break;
				}
			}
			indexes[pointsBefore] = previousIndex;
			int j = previousIndex;
			for(int i = 0; i  < pointsAfter;i++)
			{
				j++;
				switch(loopType)
				{
				case none:
					indexes[pointsBefore+1+i] = j >= dataPoints.size() ? dataPoints.size() - 1 : j;
					break;
				case swing:
				case forward:
					indexes[pointsBefore+1+i] = j % dataPoints.size();
					break;
				}
			}
			//String newString =Arrays.toString(indexes);
			//if (newString.equals(oldString) == false){
			//System.out.println(newString);}
			//oldString = newString;
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
		protected <DataValueType> double getNormalizedTime(double time,double duration, 
				TimePoint<DataValueType> previousPoint, TimePoint<DataValueType> nextPoint)
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
		protected <DataValueType,T extends TimePoint<DataValueType>>List<T> getPointsUsed(List<T> dataPoints,int previousIndex, LoopType loopType)
		{
			int[] indexes = getPointIndexes(dataPoints, previousIndex, loopType);
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
		
		public Color interpolateColor(double time, double duration, int previousIndex,
				List<ColorPoint> dataPoints, LoopType loopType, ColorMode transitionMode)
		{
			List<ColorPoint> pointsUsed = getPointsUsed(dataPoints,previousIndex,loopType);
			ColorPoint firstPoint = pointsUsed.get(0);
			double normalizedTime = getNormalizedTime(time,duration,pointsUsed);
			double[][] interpolationStructure = toDataValues(pointsUsed); 
			double firstValues[] = firstPoint.getDoubleValues(); 
			for(int q = 0; q < firstValues.length;q++)
			{
				interpolationStructure[q] = new double[pointsUsed.size()];
				interpolationStructure[q][0] = firstValues[q];
			} 
			for(int i = 1; i <pointsUsed.size();i++)
			{
				double values[] = pointsUsed.get(i).getDoubleValues();
				for(int q = 0;q < values.length;q++)
					interpolationStructure[q][i] = values[q];
			}
			double newValues[] = new double[firstValues.length];
			switch(transitionMode)
			{
			case hsb:
				double hsbValues[] = new double[interpolationStructure.length];
				for(int i = 0; i < interpolationStructure[0].length - 1;i++)
				{
					interpolationStructure[0][i+1] += (int)interpolationStructure[0][i]/1;
					//System.out.println((int)interpolationStructure[0][i]);
					double hD = interpolationStructure[0][i+1]-interpolationStructure[0][i];
					//System.out.println(hD);
					if(hD > 0.5)
					{
						interpolationStructure[0][i+1] -= 1;
						//System.out.println("RAWRAWR");
					}
					else if(hD < -0.5)
					{
						interpolationStructure[0][i+1] += 1;
					}
				}
				newValues[0] =interpolate(normalizedTime, interpolationStructure[0])%1;
				newValues[0] = newValues[0] < 0 ? 1+newValues[0]:newValues[0];
				for(int q = 1; q < firstValues.length;q++)
				{
					newValues[q] = interpolate(normalizedTime, interpolationStructure[q]);
				}
				break;
			default:
				for(int q = 0; q < firstValues.length;q++)
			{
				newValues[q] = interpolate(normalizedTime, interpolationStructure[q]);
			}
			break;
			}
			return firstPoint.toDataValue(newValues);
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
				int previousIndex, List<DataPointType> dataPoints, LoopType loopType)
		{
			List<DataPointType> pointsUsed = getPointsUsed(dataPoints,previousIndex, loopType);
			double normalizedTime = getNormalizedTime(time,duration,pointsUsed);
			return (ReturnType) interpolate(normalizedTime,pointsUsed);
		}
		protected <ReturnType,DataPointType extends TimePoint<ReturnType>> ReturnType interpolate(double t, List<DataPointType> y)
		{
			DataPointType firstPoint = y.get(0);
			double firstValues[] = firstPoint.getDoubleValues();
			double interpolationStructure[][] = toDataValues(y);
			double newValues[] = new double[firstValues.length];
			for(int q = 0; q < firstValues.length;q++)
			{
				newValues[q] = interpolate(t, interpolationStructure[q]);
			}
			return firstPoint.toDataValue(newValues);
		}
		private <DataValueType,DataPointType extends TimePoint<DataValueType>> double[][] toDataValues(List<DataPointType> y)
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
			return interpolationStructure;
		} 
	}