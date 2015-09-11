package org.vanted.animation.data;

public class StringPoint extends TimePoint {
	String value;
	public StringPoint(double time, String value) {
		super(time);
		this.value = value;
	}
	public String getValue()
	{
		return value;
	}
	public void setValue(String value)
	{
		this.value = value;
	}
}
