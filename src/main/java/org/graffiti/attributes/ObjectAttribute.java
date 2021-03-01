/*
 * Created on 15.01.2004
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.graffiti.attributes;

/**
 * @author Christian Klukas To change the template for this generated type
 *         comment go to Window>Preferences>Java>Code Generation>Code and
 *         Comments
 */
public class ObjectAttribute extends StringAttribute {
	Object myValue;
	
	@Override
	public void setString(String value) {
		myValue = value;
		super.setString(value);
	}
	
	@Override
	public String getString() {
		if (myValue == null)
			return null;
		return myValue.toString();
	}
	
	/**
	 * @param id
	 * @throws IllegalIdException
	 */
	public ObjectAttribute(String id) throws IllegalIdException {
		super(id);
	}
	
	@Override
	protected void doSetValue(Object v) throws IllegalArgumentException {
		myValue = v;
	}
	
	@Override
	public void setDefaultValue() {
		myValue = new Object();
	}
	
	@Override
	public Object getValue() {
		return myValue;
	}
	
	@Override
	public Object copy() {
		ObjectAttribute oa = new ObjectAttribute(getId());
		oa.setString(getString());
		return oa;
	}
	
}
