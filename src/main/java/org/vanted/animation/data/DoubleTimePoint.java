package org.vanted.animation.data;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public class DoubleTimePoint extends InterpolatableTimePoint<Double> { 
	public DoubleTimePoint(double time, Double value) {
		super(time, value);
	}
	@Override
	public double[] getDoubleValues()
	{
		return new double[]{this.dataValue};
	}
	@Override
	public Double toDataValue(double[] doubleValues)
	{
		return (Double)doubleValues[0];
	}
}
