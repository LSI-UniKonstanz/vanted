package org.vanted.addons.MultilevelFramework.Coarsening;

import org.graffiti.attributes.AbstractAttribute;
import org.graffiti.attributes.Attribute;

/**
 * Attribute containing the distance(number of edges between) between a node and
 * its sun
 */
public class SolarDistanceAttribute extends AbstractAttribute {

	public static final String path = "SunDistance";
	public static final String name = "value";
	public static final String fullpath = path + Attribute.SEPARATOR + name;

	protected int distance;

	protected SolarDistanceAttribute(String id) {
		super(id);
	}

	public SolarDistanceAttribute(String id, int distance) {
		super(id);
		this.distance = distance;
	}

	public SolarDistanceAttribute() {
		super();
	}

	@Override
	public void setDefaultValue() {
		this.distance = 0;

	}

	@Override
	public Object getValue() {
		return this.distance;
	}

	@Override
	public Object copy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void doSetValue(Object v) throws IllegalArgumentException {
		this.distance = (int) v;

	}

}
