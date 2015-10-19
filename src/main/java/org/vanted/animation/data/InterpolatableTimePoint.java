package org.vanted.animation.data;
/**
 * 
 * Inherit from this class instead of TimePoint if
 * the data point is capable of being interpolated
 * 
 */
public abstract class InterpolatableTimePoint<T> extends TimePoint<T> {

	public InterpolatableTimePoint(double time, T dataValue) {
		super(time, dataValue); 
	}
	/**
	 * Specifies which points are being used to 
	 */
	public abstract double[] getDoubleValues();
	public abstract T toDataValue(double[] doubleValues);

}
