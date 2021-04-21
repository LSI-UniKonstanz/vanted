package org.vanted.animation.data;

import java.util.Vector;

/**
 * @author - Patrick Shaw
 */
public class VectorTimePoint<T extends Number> extends InterpolatableTimePoint<Vector<T>> {
	public VectorTimePoint(double time, Vector<T> dataValue) {
		super(time, dataValue);
	}
	
	@Override
	public double[] getDoubleValues() {
		double values[] = new double[dataValue.size()];
		for (int i = 0; i < values.length; i++)
			values[i] = (Double) dataValue.elementAt(i);
		return values;
	}
	
	@Override
	public Vector<T> toDataValue(double[] doubleValues) {
		Vector<Number> vector = new Vector<Number>();
		for (int i = 0; i < doubleValues.length; i++)
			vector.addElement(doubleValues[i]);
		return (Vector<T>) vector;
	}
}
