// ==============================================================================
//
// ShortEditComponent.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: ShortEditComponent.java,v 1.4 2010/12/22 13:05:57 klukas Exp $

package org.graffiti.plugins.editcomponents.defaults;

import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.NumberEditComponent;

/**
 * Represents a gui component, which handles short values. Can be empty since
 * NumberEditComponent handles all primitive types.
 * 
 * @see NumberEditComponent
 */
public class ShortEditComponent extends NumberEditComponent {
	// ~ Constructors ===========================================================
	
	/**
	 * Creates a new ShortEditComponent object.
	 * 
	 * @param disp
	 *           DOCUMENT ME!
	 */
	public ShortEditComponent(Displayable disp) {
		super(disp);
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
