package org.vanted.addons.MultilevelFramework.MultilevelGraph;

import org.graffiti.attributes.AbstractAttribute;
import org.graffiti.attributes.Attribute;

public class MultiLevelChildNodeAttribute extends AbstractAttribute {
	public static final String PATH = "childNodeNumber";
	public static final String NAME = "value";
	public static final String FULLPATH = PATH + Attribute.SEPARATOR + NAME;

	protected int numberOfChildren;

	public MultiLevelChildNodeAttribute(String id, int value) {
		super(id);
		this.numberOfChildren = value;
	}

	public MultiLevelChildNodeAttribute(String id) {
		super(id);
	}

	public MultiLevelChildNodeAttribute() {
		super();
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
