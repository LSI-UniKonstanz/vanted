// ==============================================================================
//
// DoubleParameter.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: DoubleParameter.java,v 1.8 2010/12/22 13:05:34 klukas Exp $

package org.graffiti.plugin.parameter;

import java.util.ArrayList;
import java.util.Collection;

import scenario.ProvidesScenarioSupportCommand;

/**
 * Parameter that can contain <code>Double/double</code> values.
 * 
 * @version 2.6.5
 * @vanted.revision 2.6.5
 */
public class DoubleParameter extends AbstractLimitableParameter implements ProvidesScenarioSupportCommand {
	// ~ Instance fields ========================================================

	/* Limits of this parameter. */
	private Double max = null;
	private Double min = null;

	/** The value of this parameter. */
	private Double value = null;

	/** The distance between two values. If unset, the default value is 0.5. */
	private Double ball = .5;

	// ~ Constructors ===========================================================

	/**
	 * Constructs a new double parameter.
	 * 
	 * @param name
	 *            the name of the parameter.
	 * @param description
	 *            the description of the parameter.
	 */
	public DoubleParameter(String name, String description) {
		super(name, description);
	}

	/**
	 * Constructs a new Double parameter.
	 * 
	 * @param value
	 *            the new Double value of the parameter
	 * @param name
	 *            the name of the parameter.
	 * @param description
	 *            the description of the parameter.
	 */
	public DoubleParameter(Double value, String name, String description) {
		super(name, description);
		this.value = value;
	}

	/**
	 * Constructs a new Double parameter.
	 * 
	 * @param value
	 *            the new Double value. May be null.
	 * @param min
	 *            the minimum value.
	 * @param max
	 *            the maximum value.
	 * @param name
	 *            the name of the parameter.
	 * @param description
	 *            the description of the parameter.
	 */
	public DoubleParameter(Double value, Double min, Double max, String name, String description) {
		this(value, name, description);
		this.min = min;
		this.max = max;
	}

	/**
	 * Constructs a new Double parameter.
	 * 
	 * @param value
	 *            the new Double value. May be null.
	 * @param min
	 *            the minimum value.
	 * @param max
	 *            the maximum value.
	 * @param distance
	 *            the distance between any two points in the [min, max] interval.
	 * @param name
	 *            the name of the parameter.
	 * @param description
	 *            the description of the parameter.
	 */
	public DoubleParameter(Double value, Double min, Double max, Double distance, String name, String description) {
		this(value, name, description);
		this.min = min;
		this.max = max;
		this.ball = distance;
	}

	/**
	 * Constructs a new Double parameter.
	 * 
	 * @param value
	 *            the new Double value. May be null.
	 * @param distance
	 *            the distance between any two points in the open interval.
	 * @param name
	 *            the name of the parameter.
	 * @param description
	 *            the description of the parameter.
	 */
	public DoubleParameter(Double value, Double distance, String name, String description) {
		this(value, name, description);
		this.ball = distance;
	}

	// ~ Methods ================================================================

	/**
	 * Sets this parameter's value.
	 * 
	 * @param val
	 *            a Double value
	 */
	public void setDouble(Double val) {
		this.value = val;
	}

	/**
	 * Sets this parameter's value.
	 * 
	 * @param val
	 *            a double value
	 */
	public void setDouble(double val) {
		this.value = Double.valueOf(val);
	}

	/**
	 * Returns the value of this parameter as a <code>Double</code>.
	 * 
	 * @return the value of this parameter as a <code>Double</code>.
	 */
	public Double getDouble() {
		return value;
	}

	/**
	 * Returns the max defined value of this Double parameter.
	 * 
	 * @return maximal Double value of this parameter or in general
	 */
	@Override
	public Comparable<Double> getMax() {
		return max == null ? Double.MAX_VALUE : max;
	}

	/**
	 * Returns the min defined value of this Double parameter.
	 * 
	 * @return manimal Double value of this parameter or in general
	 */
	@Override
	public Comparable<Double> getMin() {
		return min == null ? Double.MIN_VALUE : min;
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
	 *            the new value of the <code>AttributeParameter</code>.
	 */
	@Override
	public void setValue(Object value) {
		if (value instanceof Double)
			this.value = (Double) value;
		if (value instanceof String) {
			try {
				this.value = Double.parseDouble((String) value);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
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

	public void setMax(Double max) {
		this.max = max;
	}

	public void setMin(Double min) {
		this.min = min;
	}

	/**
	 * Set new intervalue distance.
	 * 
	 * @param ball
	 *            the change-size between two valid values
	 */
	public void setValuesBallSize(Double ball) {
		this.ball = ball;
	}

	@Override
	public Number getValuesBall() {
		return ball;
	}

	public String getScenarioCommand() {
		return "new DoubleParameter(" + getDouble() + ", \"" + getName() + "\", \"" + getDescription() + "\")";
	}

	public Collection<String> getScenarioImports() {
		ArrayList<String> res = new ArrayList<String>();
		res.add("import org.graffiti.plugin.parameter.DoubleParameter;");
		return res;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
