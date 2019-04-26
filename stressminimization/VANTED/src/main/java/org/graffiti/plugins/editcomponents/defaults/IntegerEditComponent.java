// ==============================================================================
//
// IntegerEditComponent.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: IntegerEditComponent.java,v 1.4 2010/12/22 13:05:57 klukas Exp $

package org.graffiti.plugins.editcomponents.defaults;

import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.NumberEditComponent;

/**
 * Represents a GUI component, which handles integer values. Can be empty since
 * superclass handles all primitive types.
 * 
 * @see NumberEditComponent
 * @vanted.revision 2.6.5
 */
public class IntegerEditComponent extends NumberEditComponent {
	// ~ Constructors ===========================================================

	/**
	 * Creates a new IntegerEditComponent object.
	 * 
	 * @param disp
	 *            containing the attributes to be displayed
	 */
	public IntegerEditComponent(Displayable disp) {
		super(disp);
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
