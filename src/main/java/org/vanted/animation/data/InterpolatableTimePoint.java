package org.vanted.animation.data;

/**
 * Inherit from this class instead of TimePoint if the data point is capable of
 * being interpolated
 * 
 * @author - Patrick Shaw
 */
public abstract class InterpolatableTimePoint<T> extends TimePoint<T> {
	
	public InterpolatableTimePoint(double time, T dataValue) {
		super(time, dataValue);
	}
	
	/**
	 * Unpacks the data value and turns it into an array of double values. <br>
	 * Any data values that are put in the returned array will be interpolated by an
	 * interpolator. This is how you specify which fields in the data value are
	 * manipulated.
	 */
	public abstract double[] getDoubleValues();
	
	/**
	 * Reverses {@link #getDoubleValues()} method. Converts an array of interpolated
	 * double values back into the data value type.
	 */
	public abstract T toDataValue(double[] doubleValues);
	
}
