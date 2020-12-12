// ==============================================================================
//
// EditComponentNotFoundException.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: EditComponentNotFoundException.java,v 1.5 2010/12/22 13:05:53 klukas Exp $

package org.graffiti.editor;

/**
 * Thrown if no EditComponent could be found.
 */
public class EditComponentNotFoundException extends Exception {
	// ~ Constructors ===========================================================

	/**
	 * 
	 */
	private static final long serialVersionUID = -3292699801893305791L;

	/**
	 * Constructor for AttributeComponentNotFoundException.
	 * 
	 * @param message
	 */
	public EditComponentNotFoundException(String message) {
		super(message);
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
