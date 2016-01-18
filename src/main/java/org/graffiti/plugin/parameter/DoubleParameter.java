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
 * Represents a double parameter.
 * 
 * @version $Revision: 1.8 $
 */
public class DoubleParameter
					extends AbstractLimitableParameter
					implements ProvidesScenarioSupportCommand {
	// ~ Instance fields ========================================================
	
	private Double max = null;
	
	private Double min = null;
	
	/** The value of this parameter. */
	private Double value = null;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new double parameter.
	 * 
	 * @param name
	 *           the name of the parameter.
	 * @param description
	 *           the description of the parameter.
	 */
	public DoubleParameter(String name, String description) {
		super(name, description);
	}
	
	/**
	 * Constructs a new double parameter.
	 * 
	 * @param val
	 *           the value of the parameter
	 * @param name
	 *           the name of the parameter.
	 * @param description
	 *           the description of the parameter.
	 */
	public DoubleParameter(Number val, String name, String description) {
		super(name, description);
		value = new Double(val.doubleValue());
	}
	
	public DoubleParameter(Number val, Double min, Double max, String name, String description) {
		this(val, name, description);
		this.min = min;
		this.max = max;
	}

	// ~ Methods ================================================================
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param val
	 *           DOCUMENT ME!
	 */
	public void setDouble(Double val) {
		this.value = val;
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param val
	 *           DOCUMENT ME!
	 */
	public void setDouble(double val) {
		this.value = new Double(val);
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
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	@Override
	public Comparable<Double> getMax() {
		return max == null ? Double.MAX_VALUE : max; 
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	@Override
	public Comparable<Double> getMin() {
		return min == null ? Double.MIN_VALUE : min;
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
		if(value instanceof Double)
			this.value = (Double) value;
		if(value instanceof String){
			try {
				this.value = Double.parseDouble((String)value);
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
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

	public String getScenarioCommand() {
		return "new DoubleParameter(" +
							getDouble() + ", \"" + getName() + "\", \"" + getDescription() + "\")";
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
