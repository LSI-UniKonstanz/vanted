package org.vanted.indexednodes;

import org.graffiti.attributes.AbstractAttribute;

/**
 * Stores indices for nodes to allow fast index finding.
 * 
 * @since 2.8
 * @author Benjamin Moser
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
