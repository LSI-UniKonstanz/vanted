package org.vanted.animation.interpolators;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.Vector;

import org.vanted.animation.data.ColourPoint;
import org.vanted.animation.data.DoublePoint;
import org.vanted.animation.data.Point2DPoint;
import org.vanted.animation.data.TimePoint;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public abstract class Interpolator
{
	private boolean isCircularData;
	/**
	 * 
	 * @param isCircularData
	 * See {@link #setIsCircularData(boolean) setIsCircularData(boolean)}
	 */
	public Interpolator(boolean isCircularData)
	{
		setIsCircularData(isCircularData);
	}
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
		protected int[] getPointIndexes(double time, TimePoint dataPoints[], int previousIndex, int pointsBefore, int pointsAfter)
		{
			int indexes[] = new int[pointsBefore + 1 + pointsAfter];
			
			int k = previousIndex;
			for(int i = 0 ; i < pointsBefore;i++)
					{
				k --;
				if(k < 0)
				{
					k = isCircularData ? dataPoints.length - 1 : 0;
				}
				indexes[pointsBefore - (i+1)] = k;
					}
			int j = previousIndex;
			for(int i = 0 ; i < pointsAfter;i++)
			{
				j++;
				if(j >= dataPoints.length)
				{
					j = isCircularData ? 0 : dataPoints.length - 1;
				}
				indexes[pointsBefore + (i + 1)] = j;
			}
			indexes[pointsBefore] = previousIndex;
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
		protected abstract TimePoint[] getPointsUsed(double time,TimePoint dataPoints[],int previousIndex);
		protected TimePoint[] getPointsUsed(double time, TimePoint dataPoints[],int previousIndex, int pointsBefore, int pointsAfter)
		{
			int[] indexes = getPointIndexes(time,dataPoints, previousIndex,pointsBefore,pointsAfter);
			TimePoint points[] = new TimePoint[indexes.length];
			for(int i = 0; i < indexes.length;i++)
			{
				points[i] = dataPoints[indexes[i]];
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
		protected abstract double getNormalizedTime(double time,double duration, TimePoint pointsUsed[]);
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
		public double interpolate(double time, double duration, int previousIndex, DoublePoint dataPoints[])
		{

			TimePoint timePoints[] = getPointsUsed(time, dataPoints,previousIndex);
			DoublePoint pointsUsed[] = new DoublePoint[timePoints.length];
			for(int i = 0; i < timePoints.length;i++)
			{
				pointsUsed[i] = (DoublePoint)timePoints[i];
			}
			double normalizedTime = getNormalizedTime(time,duration,pointsUsed);
			return interpolate(normalizedTime, pointsUsed);
		}
		
		public Color interpolate(double time, double duration, int previousIndex, ColourPoint dataPoints[])
		{
			TimePoint timePoints[] = getPointsUsed(time, dataPoints,previousIndex);
			ColourPoint pointsUsed[] = new ColourPoint[timePoints.length];
			for(int i = 0; i < timePoints.length;i++)
			{
				pointsUsed[i] = (ColourPoint)timePoints[i];
			}
			double normalizedTime = getNormalizedTime(time,duration,pointsUsed);
			return interpolate(normalizedTime, pointsUsed);
		}
		
		public Point2D interpolate(double time, double duration, int previousIndex, Point2DPoint dataPoints[])
		{
			TimePoint timePoints[] = getPointsUsed(time, dataPoints,previousIndex);
			Point2DPoint pointsUsed[] = new Point2DPoint[timePoints.length];
			for(int i = 0; i < timePoints.length;i++)
			{
				pointsUsed[i] = (Point2DPoint)timePoints[i];
			}
			double normalizedTime = getNormalizedTime(time,duration,pointsUsed);
			return interpolate(normalizedTime, pointsUsed);
		}
		/**
		 * This method exists so that you do not have to specify how the interpolator
		 * interpolates colors.
		 * Note: It is recommended that you override this method for performance reasons.
		 * @param time
		 * The time that has elapsed since the start of the animation.
		 * @param duration
		 * Total duration of the animation
		 * @param previousIndex
		 * @param dataPoints
		 * @return
		 * An interpolated colour value.
		 */
		protected Color interpolate(double x, ColourPoint...y)
		{
			double rValues[] = new double[y.length];
			double gValues[] = new double[y.length];
			double bValues[] = new double[y.length];
			for(int i = 0; i < y.length;i++)
			{
				Color colour = y[i].getColour();
				rValues[i] = colour.getRed();
				gValues[i] = colour.getGreen();
				bValues[i] = colour.getBlue();
			}
			double nR = Math.max(Math.min(interpolate(x,rValues),255),0);
			double nG = Math.max(Math.min(interpolate(x,gValues),255),0);
			double nB = Math.max(Math.min(interpolate(x,bValues),255),0);
			return new Color((int)nR, (int)nG, (int) nB);
		}
		/*
		protected Vector<T extends Number> interpolate(double x, Vector<? extends Number>...y)
		{
			double values[][] = new double[y.length][];
			for(int i = 0 ; i < values.length;i++)
			{
				values[i] = new double[y[i].size()];
				for(int q = 0; q < values[i].length;q++)
				{
					values[i][q] = y[i].elementAt(q);		
				}
			}
		}*/
		/**
		 * This method exists so that you do not have to specify how the interpolator
		 * interpolates 2D Points.
		 * Note: It is recommended that you override this method for performance reasons.
		 * @param time
		 * The time that has elapsed since the start of the animation.
		 * @param duration
		 * Total duration of the animation
		 * @param previousIndex
		 * @param dataPoints
		 * @return
		 * An interpolated Point2D value.
		 */
		protected Point2D interpolate(double x, Point2DPoint...y)
		{
			double xValues[] = new double[y.length];
			double yValues[] = new double[y.length];
			for(int i = 0; i < y.length;i++)
			{
				Point2D point = y[i].getPoint();
				xValues[i] = point.getX();
				yValues[i] = point.getY();
			}
			double nX = interpolate(x,xValues);
			double nY = interpolate(x,yValues);
			return new Point2D.Double(nX, nY);
		}
		/**
		 * This method exists so that you do not have to specify how the interpolator
		 * interpolates double values.
		 * Note: It is recommended that you override this method for performance reasons.
		 * @param time
		 * The time that has elapsed since the start of the animation.
		 * @param duration
		 * Total duration of the animation
		 * @param previousIndex
		 * @param dataPoints
		 * @return
		 * An interpolated d value.
		 */
		protected double interpolate(double x, DoublePoint...y)
		{
			double values[] = new double[y.length];
			for(int i = 0; i < y.length;i++)
			{
				values[i] = y[i].getValue();
			}
			return interpolate(x,values);
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
		public void setIsCircularData(boolean isCircularData)
		{
			this.isCircularData = isCircularData;
		}
		public boolean getIsCircularData()
		{
			return this.isCircularData;
		}
	}