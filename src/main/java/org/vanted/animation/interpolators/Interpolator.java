package org.vanted.animation.interpolators;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector; 
import org.vanted.animation.data.ColorMode;
import org.vanted.animation.data.ColorTimePoint;
import org.vanted.animation.data.DoubleTimePoint;
import org.vanted.animation.data.Point2DTimePoint;
import org.vanted.animation.data.TimePoint;
import org.vanted.animation.data.VectorTimePoint;
import org.vanted.animation.loopers.Looper;
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
		protected <V> double getNormalizedTime(double time,double duration, 
				TimePoint<V> previousPoint, TimePoint<V> nextPoint)
		{
			double normalizedTime = 0;
			if(previousPoint.getTime() > nextPoint.getTime())
			{
				double totalTime = duration - previousPoint.getTime();
				totalTime += nextPoint.getTime();
				normalizedTime = (time - previousPoint.getTime())/ totalTime;
			}
			normalizedTime =  (time - previousPoint.getTime()) / (nextPoint.getTime() - previousPoint.getTime());
			System.out.println(normalizedTime);
			return Double.isInfinite(normalizedTime) ? 0 : Math.abs(normalizedTime);
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
		protected <V,T extends TimePoint<V>> double getNormalizedTime(double time,double duration, List<T> pointsUsed)
		{
			return getNormalizedTime(time,duration,pointsUsed.get(getPointsBefore()), pointsUsed.get(getPointsBefore() + 1));
		}
		
		public Color interpolateColor(double time, double duration, int previousIndex,
				List<ColorTimePoint> dataPoints, Looper looper, ColorMode transitionMode)
		{
			List<ColorTimePoint> pointsUsed = looper.getPointsUsed(dataPoints,previousIndex,getPointsBefore(),getPointsAfter());
			ColorTimePoint firstPoint = pointsUsed.get(0);
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
				{
					interpolationStructure[q][i] = values[q];
				}
			}
			double newValues[] = new double[firstValues.length];
			switch(transitionMode)
			{
			case HSB:
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
		 * @param t
		 * A value between 0 and 1. Typically represents how close you are to a given point.
		 * 1 Means the x value matches the x value of the previous data point.
		 * 0 Means the x value matches the x value of the next data point.
		 * @param y
		 * The y values that you need interpolate between.
		 * @return
		 */
		protected abstract double interpolate(double t, double...y);	
		public <V,T extends TimePoint<V>> V interpolate(double time, double duration,
				int previousIndex, List<T> dataPoints, Looper looper)
		{
			List<T> pointsUsed = looper.getPointsUsed(dataPoints,previousIndex, getPointsBefore(), getPointsAfter());
			double normalizedTime = getNormalizedTime(time,duration,pointsUsed);
			return (V) interpolate(normalizedTime,pointsUsed);
		}
		protected <V,T extends TimePoint<V>> V interpolate(double t, List<T> y)
		{
			T firstPoint = y.get(0);
			double firstValues[] = firstPoint.getDoubleValues();
			double interpolationStructure[][] = toDataValues(y);
			double newValues[] = new double[firstValues.length];
			for(int q = 0; q < firstValues.length;q++)
			{
				newValues[q] = interpolate(t, interpolationStructure[q]);
			}
			return firstPoint.toDataValue(newValues);
		}
		private <V,T extends TimePoint<V>> double[][] toDataValues(List<T> y)
		{
			T firstPoint = y.get(0);
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
				{
					interpolationStructure[q][i] = values[q];
				}
			}
			return interpolationStructure;
		} 
	}