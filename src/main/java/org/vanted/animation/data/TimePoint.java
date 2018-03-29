package org.vanted.animation.data;

import java.util.concurrent.TimeUnit;

/**
 * 
 * @author - Patrick Shaw
 * 
 */
public abstract class TimePoint<T> {
	protected T dataValue;
	protected double time;

	public TimePoint(double time, T dataValue) {
		this.time = time;
		this.dataValue = dataValue;
	}

	/**
	 * 
	 * @return
	 * 
	 */
	public double getTime() {
		return time;
	}

	/**
	 * 
	 * @param time
	 *            Number of milliseconds at which the data point's data value should
	 *            be expressed.
	 */
	public void setTime(double time) {
		this.time = time;
	}

	/**
	 * Sets the time at which the data point's data value should be expressed.
	 * 
	 * @param time
	 *            The magnitude of the time value.
	 * @param The
	 *            unit of time.
	 */
	public void setTime(long time, TimeUnit timeUnit) {
		this.time = timeUnit.toMillis(time);
	}

	/**
	 * @return The value of the data point.
	 */
	public T getDataValue() {
		return dataValue;
	}

	/**
	 * @param dataValue
	 *            Sets the data value of the data point.
	 */
	public void setDataValue(T dataValue) {
		this.dataValue = dataValue;
	}

	@Override
	public String toString() {
		return "Time: " + Double.toString(time) + " - " + "Data Value: " + dataValue.toString();
	}
}