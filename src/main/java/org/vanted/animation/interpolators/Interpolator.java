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
import org.vanted.animation.data.InterpolatableTimePoint;
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
	 * Interpolator classes specify how data is interpolated between datapoints.
	 * 
	 */
	public Interpolator()
	{ 
	}
	/**
	 * Specifies the number of points to retrieve before the previous-index point
	 * Typically overridden to return a constant value.
	 */
	protected abstract int getPointsBefore();
	/**
	 * Specifies the number of points to retrieve after the previous-index point
	 * Typically overridden to return a constant value.
	 */
	protected abstract int getPointsAfter(); 
		/**
		 * In the event that you need to perform linear interpolation,
		 * you can call this method to save you from implementing the method yourself
		 */
		protected double linearInterpolation(double x,double y1, double y2)
		{
			return y1 + (y2 - y1) * x;
		}
		/**
		 * Calculates how far the {@code time} is from the next data point relative to the previous pooint
		 * as a percentage.
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
		protected <V,T extends InterpolatableTimePoint<V>> double getNormalizedTime(
				double time,double loopDuration, List<T> dataPoints, List<T> pointsUsed, Looper looper)
		{
			return looper.getNormalizedTime(time, loopDuration, dataPoints, pointsUsed,
					pointsUsed.get(getPointsBefore()), pointsUsed.get(getPointsBefore() + 1));
		}
		/**
		 * Colors can interpolate in special ways.<br>
		 * It is recommended that you call this method instead of interpolate() 
		 * to interpolate between values.
		 * @param <V>
		 * The type of data value that the data point holds.
		 * @param <T>
		 * The type of data point that the interpolator is interpolating for.
		 * @param time
		 * The time that has elapsed since the start of the animation.
		 * @param loopDuration
		 * How long each loop takes in milliseconds.
		 * @param dataPoints
		 * All the dataPoints that the interpolator needs to interpolate between.
		 * @param looper
		 * Used to figure out which points are needed at a particular time for interpolation.
		 * @param transitionMode
		 * Specifies what kind of the color interpolation the interpolator is using.
		 * @return
		 * The interpolated color.
		 */
		public Color interpolateColor(double time, double loopDuration, int previousIndex,
				List<ColorTimePoint> dataPoints, Looper looper, ColorMode transitionMode)
		{
			List<ColorTimePoint> pointsUsed = looper.getPointsUsed(dataPoints,previousIndex,getPointsBefore(),getPointsAfter());
			ColorTimePoint firstPoint = pointsUsed.get(0);
			double normalizedTime = getNormalizedTime(time,loopDuration, dataPoints, pointsUsed, looper);
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
					// Calculate hue difference
					double hD = interpolationStructure[0][i+1]-interpolationStructure[0][i];
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
		 * Override this method with an interpolation algorithm.
		 * @param t
		 * A value between 0 and 1. Typically represents how close you are to a given point 
		 * with respect to time.<br>
		 * t=0 Means the elapsed time value is equal to the time value of the previous data point. <br>
		 * t=1 Means the elapsed time value is equal the time value of the next data point. <br>
		 * @param y
		 * The y values that you need interpolate between.
		 * @return
		 * An interpolated value.
		 */
		protected abstract double interpolate(double t, double...y);	
		public <V,T extends InterpolatableTimePoint<V>> V interpolate(double time, double duration,
				int previousIndex, List<T> dataPoints, Looper looper)
		{
			List<T> pointsUsed = looper.getPointsUsed(dataPoints,previousIndex, getPointsBefore(), getPointsAfter());
			double normalizedTime = getNormalizedTime(time, duration, dataPoints, pointsUsed, looper);
			return (V) interpolate(normalizedTime,pointsUsed);
		}
		/**
		 * This method calls {@link #interpolate(double, double...)} for each array in the 
		 * interpolation structure
		 * @param <V>
		 * The type of data value that the data point holds.
		 * @param <T>
		 * The type of data point that the interpolator is interpolating for.
		 * @param t
		 * A value between 0 and 1. Typically represents how close you are to a given point 
		 * with respect to time.<br>
		 * t=0 Means the elapsed time value is equal to the time value of the previous data point. <br>
		 * t=1 Means the elapsed time value is equal the time value of the next data point. <br>
		 * @param pointsUsed
		 * The data points being used for interpolation at this instance in time.
		 * @return
		 * An interpolated value.
		 */
		protected <V,T extends InterpolatableTimePoint<V>> V interpolate(double t, List<T> pointsUsed)
		{
			T firstPoint = pointsUsed.get(0);
			double firstValues[] = firstPoint.getDoubleValues();
			double interpolationStructure[][] = toDataValues(pointsUsed);
			double newValues[] = new double[firstValues.length];
			for(int q = 0; q < firstValues.length;q++)
			{
				newValues[q] = interpolate(t, interpolationStructure[q]);
			}
			return firstPoint.toDataValue(newValues);
		}
		/**
		 * 
		 * @param <V>
		 * The type of data value that the data point holds.
		 * @param <T>
		 * The type of data point that the interpolator is interpolating for.
		 * @param y
		 * A list of data points that the interpolator needs to interpolate between
		 * @return
		 * A 2D array:<br>
		 * Each array accessed in the first dimension represents an individual set of
		 * data values to interpolate between.<br>
		 * Eg. If we are interpolating RGB color values. array[0] would be all the 
		 * red values, array[1] would be all the blue values, etc<br>
		 */
		private <V,T extends InterpolatableTimePoint<V>> double[][] toDataValues(List<T> y)
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