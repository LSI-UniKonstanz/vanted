package org.vanted.animation.data;

public class StringTimePoint extends TimePoint<String> {
	String value;
	public StringTimePoint(double time, String value) {
		super(time,value);
	}
	public String getValue()
	{
		return value;
	}
	public void setValue(String value)
	{
		this.value = value;
	}
	@Override
	public double[] getDoubleValues() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String toDataValue(double[] doubleValues) {
		// TODO Auto-generated method stub
		return null;
	}
}
