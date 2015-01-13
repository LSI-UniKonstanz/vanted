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
 * Represents a gui component, which handles integer values. Can be empty since
 * superclass handles all primitive types.
 * 
 * @see NumberEditComponent
 */
public class IntegerEditComponent
					extends NumberEditComponent {
	// ~ Constructors ===========================================================
	
	/**
	 * Creates a new IntegerEditComponent object.
	 * 
	 * @param disp
	 *           DOCUMENT ME!
	 */
	public IntegerEditComponent(Displayable disp) {
		super(disp);
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
