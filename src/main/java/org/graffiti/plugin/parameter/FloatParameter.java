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
 * Parameter that can contain <code>Float/float</code> values.
 * 
 * @version 2.6.5
 * @vanted.revision 2.6.5
 */
public class FloatParameter extends AbstractLimitableParameter {
	// ~ Instance fields ========================================================
	
	private Float max = null;
	
	private Float min = null;
	
	/** The value of this parameter. */
	private Float value = null;
	
	/** The distance between two values. If unset, the default value is 0.5. */
	private Float ball = .5f;
	
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
	
	/**
	 * Constructs a new Float parameter.
	 * 
	 * @param value
	 *           the new Float value. May be null.
	 * @param name
	 *           the name of the parameter.
	 * @param description
	 *           the description of the parameter.
	 */
	public FloatParameter(Float value, String name, String description) {
		super(name, description);
		this.value = value;
	}
	
	/**
	 * Constructs a new Float parameter.
	 * 
	 * @param value
	 *           the new Float value. May be null.
	 * @param min
	 *           the minimum value.
	 * @param max
	 *           the maximum value.
	 * @param name
	 *           the name of the parameter.
	 * @param description
	 *           the description of the parameter.
	 */
	public FloatParameter(Float value, Float min, Float max, String name, String description) {
		this(value, name, description);
		this.min = min;
		this.max = max;
	}
	
	/**
	 * Constructs a new Float parameter.
	 * 
	 * @param value
	 *           the new Float value. May be null.
	 * @param min
	 *           the minimum value.
	 * @param max
	 *           the maximum value.
	 * @param distance
	 *           the distance between any two points in the [min, max] interval.
	 * @param name
	 *           the name of the parameter.
	 * @param description
	 *           the description of the parameter.
	 */
	public FloatParameter(Float value, Float min, Float max, Float distance, String name, String description) {
		this(value, name, description);
		this.min = min;
		this.max = max;
		this.ball = distance;
	}
	
	/**
	 * Constructs a new Float parameter.
	 * 
	 * @param value
	 *           the new Float value. May be null.
	 * @param distance
	 *           the distance between any two points in the open interval.
	 * @param name
	 *           the name of the parameter.
	 * @param description
	 *           the description of the parameter.
	 */
	public FloatParameter(Float value, Float distance, String name, String description) {
		this(value, name, description);
		this.ball = distance;
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
	 * Returns the max defined value of this Float parameter.
	 * 
	 * @return maximal Float value of this parameter or in general
	 */
	@Override
	public Comparable<Float> getMax() {
		return max == null ? Float.MAX_VALUE : max;
	}
	
	/**
	 * Returns the min defined value of this Float parameter.
	 * 
	 * @return manimal Float value of this parameter or in general
	 */
	@Override
	public Comparable<Float> getMin() {
		return min == null ? Float.MIN_VALUE : min;
	}
	
	@Override
	public boolean isValid() {
		boolean valid = true;
		if (value == null)
			return false;
		
		if (min != null && min.compareTo(value) > 0)
			valid = false;
		if (max != null && max.compareTo(value) < 0)
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
		if (value instanceof Float)
			this.value = (Float) value;
		if (value instanceof String) {
			try {
				this.value = Float.parseFloat((String) value);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void setMax(Float max) {
		this.max = max;
	}
	
	public void setMin(Float min) {
		this.min = min;
	}
	
	/**
	 * Set new intervalue distance.
	 * 
	 * @param ball
	 *           the change-size between two valid values
	 */
	public void setValuesBallSize(Float ball) {
		this.ball = ball;
	}
	
	@Override
	public Number getValuesBall() {
		return ball;
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
