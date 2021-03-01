// ==============================================================================
//
// IntegerParameter.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: IntegerParameter.java,v 1.8 2010/12/22 13:05:34 klukas Exp $

package org.graffiti.plugin.parameter;

import java.util.ArrayList;
import java.util.Collection;

import scenario.ProvidesScenarioSupportCommand;

/**
 * Parameter that can contain <code>Integer/int</code> values.
 * 
 * @version 2.6.5
 * @vanted.revision 2.6.5
 */
public class IntegerParameter extends AbstractLimitableParameter implements ProvidesScenarioSupportCommand {
	// ~ Instance fields ========================================================
	
	/** The maximum valid value of this parameter. */
	private Integer max = null;
	
	/** The minimum valid value of this parameter. */
	private Integer min = null;
	
	/** The value of this parameter. */
	private Integer value = null;
	
	/** The distance between two values. If unset, the default value is 1. */
	private Integer ball = 1;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new Integer parameter.
	 * 
	 * @param value
	 *           the new Integer value. May be null.
	 * @param name
	 *           the name of the parameter.
	 * @param description
	 *           the description of the parameter.
	 */
	public IntegerParameter(Integer value, String name, String description) {
		super(name, description);
		this.value = value;
	}
	
	/**
	 * Constructs a new Integer parameter.
	 * 
	 * @param value
	 *           the new Integer value. May be null.
	 * @param min
	 *           the minimum value.
	 * @param max
	 *           the maximum value.
	 * @param name
	 *           the name of the parameter.
	 * @param description
	 *           the description of the parameter.
	 */
	public IntegerParameter(Integer value, Integer min, Integer max, String name, String description) {
		this(value, name, description);
		this.min = min;
		this.max = max;
	}
	
	/**
	 * Constructs a new Integer parameter.
	 * 
	 * @param value
	 *           the new Integer value. May be null.
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
	public IntegerParameter(Integer value, Integer min, Integer max, Integer distance, String name,
			String description) {
		this(value, name, description);
		this.min = min;
		this.max = max;
		this.ball = distance;
	}
	
	/**
	 * Constructs a new Integer parameter.
	 * 
	 * @param value
	 *           the new Integer value. May be null.
	 * @param distance
	 *           the distance between any two points in the open interval.
	 * @param name
	 *           the name of the parameter.
	 * @param description
	 *           the description of the parameter.
	 */
	public IntegerParameter(Integer value, Integer distance, String name, String description) {
		this(value, name, description);
		this.ball = distance;
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Returns the value of this parameter as an <code>Integer</code>.
	 * 
	 * @return the value of this parameter as an <code>Integer</code>.
	 */
	public Integer getInteger() {
		return value;
	}
	
	/**
	 * Returns the maximum of the interval.
	 * 
	 * @return the maximum of the interval.
	 */
	@Override
	public Comparable<Integer> getMax() {
		return max == null ? Integer.MAX_VALUE : max;
	}
	
	/**
	 * Returns the minimum of the interval.
	 * 
	 * @return the minimum of the interval.
	 */
	@Override
	public Comparable<Integer> getMin() {
		return min == null ? Integer.MIN_VALUE : min;
	}
	
	@Override
	public boolean isValid() {
		if (value == null) {
			return false;
		}
		
		if ((min == null) && (max == null)) {
			return true;
		}
		boolean valid = true;
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
	 * @exception IllegalArgumentException
	 *               thrown if <code>value</code> is not of the correct type.
	 */
	@Override
	public void setValue(Object value) {
		if (value instanceof Integer)
			this.value = (Integer) value;
		else if (value instanceof String)
			try {
				this.value = Integer.parseInt((String) value);
			} catch (NumberFormatException e) {
				
				throw new IllegalArgumentException(e.getMessage());
				// e.printStackTrace();
			}
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
	
	/**
	 * Set new intervalue distance.
	 * 
	 * @param ball
	 *           the change-size between two valid values
	 */
	public void setValuesBallSize(Integer ball) {
		this.ball = ball;
	}
	
	@Override
	public Number getValuesBall() {
		return ball;
	}
	
	public String getScenarioCommand() {
		return "new IntegerParameter(" + getInteger().intValue() + ", \"" + getName() + "\", \"" + getDescription()
				+ "\")";
	}
	
	public Collection<String> getScenarioImports() {
		ArrayList<String> res = new ArrayList<String>();
		res.add("import org.graffiti.plugin.parameter.IntegerParameter;");
		return res;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
