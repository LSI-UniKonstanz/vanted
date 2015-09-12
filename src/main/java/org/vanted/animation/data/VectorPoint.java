package org.vanted.animation.data;

import java.util.Vector;

public class VectorPoint<DimensionType extends Number> extends TimePoint<Vector<DimensionType>> {
	public VectorPoint(double time, Vector<DimensionType> dataValue) {
		super(time,dataValue);
	}
	@Override
	public double[] getDoubleValues() {
		double values[] = new double[dataValue.size()];
		for(int i = 0 ;i < values.length;i++)
			values[i] = (double) dataValue.elementAt(i);
		return values;
	}

	@Override
	public Vector<DimensionType> toDataValue(double[] doubleValues) {
		Vector<Number> vector = new Vector<Number>();
		for(int i =0;i< doubleValues.length;i++)
			vector.addElement(doubleValues[i]);
		return (Vector<DimensionType>)vector;
	}
}
