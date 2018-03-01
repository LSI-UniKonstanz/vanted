// ==============================================================================
//
// AbstractLimitableParameter.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: AbstractLimitableParameter.java,v 1.6 2010/12/22 13:05:34 klukas Exp $

package org.graffiti.plugin.parameter;

/**
 * This abstract class provides a default implementation for the
 * <code>toXMLString</code> method and specifies an interval size between the
 * limitable parameter's values to be implemented for finer input control.
 * 
 * @version 2.6.5
 * @vanted.revision 2.6.5
 */
public abstract class AbstractLimitableParameter extends AbstractSingleParameter implements LimitableParameter {

	/**
	 * Constructs a new abstract limitable parameter.
	 * 
	 * @param name
	 *            the name of the parameter.
	 * @param description
	 *            the description of the parameter.
	 */
	public AbstractLimitableParameter(String name, String description) {
		super(name, description);
	}

	@Override
	public String toXMLString() {
		return getStandardXML(getValue().toString());
	}

	/**
	 * The closed ball diameter exclusive for the current parameter value, such that<br/>
	 * <br/>
	 * 
	 * v(t) = val<br/>
	 * v(t+1) = (val +|- ballDiameter)<br/>
	 * </br/>
	 *
	 * whereas <i>t</i> stands for time point.
	 * 
	 * @return a Number instance
	 */
	abstract public Number getValuesBall();

}
