package org.vanted.animation.data;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public class DoublePoint extends TimePoint<Double> {
	private double value;
	public DoublePoint(double time, Double value) {
		super(time, value);
	}
	public double getValue()
	{
		return value;
	}
	public void setValue(double value)
	{
		this.value = value;
	}
	@Override
	public double[] getDoubleValues()
	{
		return new double[]{value};
	}
	@Override
	public Double toDataValue(double[] doubleValues)
	{
		return (Double)doubleValues[0];
	}
}
