package org.vanted.animation.data;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public class BooleanTimePoint extends TimePoint<Boolean> {
	public BooleanTimePoint(double time, boolean value) {
		super(time, value);
	}
	@Override
	public double[] getDoubleValues() { 
		return null;
	}
	@Override
	public Boolean toDataValue(double[] doubleValues) { 
		return null;
	}
}
