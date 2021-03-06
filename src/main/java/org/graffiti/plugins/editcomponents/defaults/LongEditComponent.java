// ==============================================================================
//
// LongEditComponent.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: LongEditComponent.java,v 1.4 2010/12/22 13:05:57 klukas Exp $

package org.graffiti.plugins.editcomponents.defaults;

import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.NumberEditComponent;

/**
 * Represents a gui component, which handles long values. Can be empty since
 * superclass handles all primitive types.
 * 
 * @see org.graffiti.plugin.editcomponent.NumberEditComponent
 */
public class LongEditComponent extends NumberEditComponent {
	// ~ Constructors ===========================================================
	
	/**
	 * Creates a new LongEditComponent object.
	 * 
	 * @param disp
	 *           DOCUMENT ME!
	 */
	public LongEditComponent(Displayable disp) {
		super(disp);
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
