package org.vanted.animation.data;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public abstract class TimePoint<T>
{
	protected T dataValue;
	protected double time;
	public TimePoint(double time, T dataValue)
	{
		this.time = time;
		this.dataValue = dataValue;
	} 
	public double getTime()
	{
		return time;
	} 
	public void setTime(double time) 
	{
		this.time = time;
	}
	public abstract double[] getDoubleValues();
	public abstract T toDataValue(double[] doubleValues);
	public T getDataValue() {
		return dataValue;
	}
	public void setDataValue(T dataValue) {
		this.dataValue = dataValue;
	}
	@Override
	public String toString()
	{
		return "Time: " + Double.toString(time) + "\n"
				+ "Data Value: " + dataValue.toString();
	}
}