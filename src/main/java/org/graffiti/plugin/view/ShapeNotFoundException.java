// ==============================================================================
//
// ShapeNotFoundException.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: ShapeNotFoundException.java,v 1.5 2010/12/22 13:05:53 klukas Exp $

package org.graffiti.plugin.view;

/**
 * DOCUMENT ME!
 * 
 * @author schoeffl To change this generated comment edit the template variable
 *         "typecomment": Window>Preferences>Java>Templates. To enable and
 *         disable the creation of type comments go to
 *         Window>Preferences>Java>Code Generation.
 */
public class ShapeNotFoundException extends Exception {
	// ~ Constructors ===========================================================
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5984889433963582264L;
	
	/**
	 * Constructs a ShapeNotFoundException.
	 * 
	 * @param msg
	 *           the message to set.
	 */
	public ShapeNotFoundException(String msg) {
		super(msg);
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
