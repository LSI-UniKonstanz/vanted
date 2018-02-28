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
 * This abstract class provides an implementation for the <code>isValid</code>
 * method, using the <code>compareTo</code> method of the
 * <code>Comparable</code> interface.
 * 
 * @version $Revision: 1.6 $
 */
public abstract class AbstractLimitableParameter extends AbstractSingleParameter implements LimitableParameter {
	// ~ Constructors ===========================================================

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

	// ~ Methods ================================================================

	/**
	 * @see org.graffiti.plugin.parameter.Parameter#toXMLString()
	 */
	@Override
	public String toXMLString() {
		return getStandardXML(getValue().toString());
	}

}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
