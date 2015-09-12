package org.vanted.animation.data;

public class StringPoint extends TimePoint<String> {
	String value;
	public StringPoint(double time, String value) {
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
