/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.label_alignment;

import org.graffiti.attributes.StringAttribute;

public class LabelAlignmentAttribute extends StringAttribute {
	// private String myValue;
	
	public LabelAlignmentAttribute() {
		super();
	}
	
	public LabelAlignmentAttribute(String id) {
		super(id);
		setDescription("Label-alignment relative to a graph-node or edge"); // tooltip
	}
	
	public LabelAlignmentAttribute(String id, String value) {
		super(id);
		setString(value);
	}
	
	@Override
	public void setDefaultValue() {
		value = null;
	}
	
	// @Override
	// public void setString(String value) {
	// assert value != null;
	// if (getString().equals(value))
	// return;
	// AttributeEvent ae = new AttributeEvent(this);
	// callPreAttributeChanged(ae);
	// myValue = value;
	// callPostAttributeChanged(ae);
	// }
	
	// @Override
	// public String getString() {
	// return myValue;
	// }
	
	@Override
	public Object getValue() {
		return getString();
	}
	
	@Override
	public Object copy() {
		return new LabelAlignmentAttribute(this.getId(), this.getString());
	}
	
	@Override
	public String toString(int n) {
		return getSpaces(n) + getId() + " = \"" + getString() + "\"";
	}
	
	@Override
	public String toXMLString() {
		return getStandardXML(getString());
	}
	
	@Override
	protected void doSetValue(Object o) throws IllegalArgumentException {
		assert o != null;
		
		try {
			setString((String) o);
		} catch (ClassCastException cce) {
			throw new IllegalArgumentException("Invalid value type.");
		}
	}
}