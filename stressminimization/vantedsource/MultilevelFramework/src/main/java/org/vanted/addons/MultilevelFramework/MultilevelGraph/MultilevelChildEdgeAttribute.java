package org.vanted.addons.MultilevelFramework.MultilevelGraph;

import org.graffiti.attributes.AbstractAttribute;
import org.graffiti.attributes.Attribute;

public class MultilevelChildEdgeAttribute extends AbstractAttribute {

	public static final String PATH = "childEdgeNumber";
	public static final String NAME = "value";
	public static final String FULLPATH = PATH + Attribute.SEPARATOR + NAME;

	protected int numberOfChildren;

	public MultilevelChildEdgeAttribute() {
		super();
	}

	public MultilevelChildEdgeAttribute(String id) {
		super(id);
	}

	public MultilevelChildEdgeAttribute(String id, int value) {
		super(id);
		this.numberOfChildren = value;
	}

	@Override
	public void setDefaultValue() {
		this.numberOfChildren = 0;

	}

	@Override
	public Object getValue() {
		return this.numberOfChildren;
	}

	@Override
	public Object copy() {
		return null;
	}

	@Override
	protected void doSetValue(Object v) throws IllegalArgumentException {
		this.numberOfChildren = (int) v;
	}

}
