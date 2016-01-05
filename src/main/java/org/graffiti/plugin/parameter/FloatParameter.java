// ==============================================================================
//
// FloatParameter.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: FloatParameter.java,v 1.6 2010/12/22 13:05:34 klukas Exp $

package org.graffiti.plugin.parameter;

/**
 * Parameter that contains a float value.
 * 
 * @version $Revision: 1.6 $
 */
public class FloatParameter
					extends AbstractLimitableParameter {
	// ~ Instance fields ========================================================
	
	
	private Float max = null;
	
	private Float min = null;
	
	/** The value of this parameter. */
	private Float value = null;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new float parameter.
	 * 
	 * @param name
	 *           the name of the parameter.
	 * @param description
	 *           the description of the parameter.
	 */
	public FloatParameter(String name, String description) {
		super(name, description);
	}
	
	public FloatParameter(Float value, String name, String description) {
		super(name, description);
		this.value = value;
	}

	public FloatParameter(Float value, Float min, Float max, String name, String description) {
		this(value, name, description);
		this.min = min;
		this.max = max;
	}
	// ~ Methods ================================================================
	
	/**
	 * Returns the value of this parameter as a <code>Float</code>.
	 * 
	 * @return the value of this parameter as a <code>Float</code>.
	 */
	public Float getFloat() {
		return value;
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	@Override
	public Comparable<Float> getMax() {
		return max == null ? Float.MAX_VALUE : max; 
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	@Override
	public Comparable<Float> getMin() {
		return min == null ? Float.MIN_VALUE : min; 
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	@Override
	public boolean isValid() {
		boolean valid = true;
		if(value == null)
			return false;
		
		if(min != null && min.compareTo(value) > 0)
			valid = false;
		if(max != null && max.compareTo(value) < 0)
			valid = false;
		return valid;
	}
	
	/**
	 * Sets the value of the <code>AttributeParameter</code>.
	 * 
	 * @param value
	 *           the new value of the <code>AttributeParameter</code>.
	 */
	@Override
	public void setValue(Object value) {
		// TODO
	}
	
	
	
	public void setMax(Float max) {
		this.max = max;
	}

	public void setMin(Float min) {
		this.min = min;
	}

	/**
	 * Returns the value of this parameter.
	 * 
	 * @return the value of this parameter.
	 */
	@Override
	public Object getValue() {
		return value;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
