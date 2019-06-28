package org.vanted.addons.stressminimization.primitives;

import org.graffiti.attributes.AbstractAttribute;

/**
 * Stores indices for nodes to allow fast index finding.
 */
public class IndexAttribute extends AbstractAttribute {

	private final int index;

	IndexAttribute(int index, String id) {
		this.index = index;
		this.idd = id;
	}

	@Override
	public void setDefaultValue() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getValue() {
		return index;
	}

	@Override
	public Object copy() {
		return new IndexAttribute(index, idd);
	}

	@Override
	protected void doSetValue(Object v) throws IllegalArgumentException {
		throw new UnsupportedOperationException();
	}

}
