package org.vanted.animation.data;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public abstract class TimePoint<DataValue>
{
	protected DataValue dataValue;
	protected double time;
	public TimePoint(double time, DataValue dataValue)
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
	public abstract DataValue toDataValue(double[] doubleValues);
	public DataValue getDataValue() {
		return dataValue;
	}
	public void setDataValue(DataValue dataValue) {
		this.dataValue = dataValue;
	}
	@Override
	public String toString()
	{
		return "Time: " + Double.toString(time) + "\n"
				+ "Data Value: " + dataValue.toString();
	}
}