// ==============================================================================
//
// GraffitiContainer.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: GraffitiContainer.java,v 1.4 2010/12/22 13:05:54 klukas Exp $

package org.graffiti.plugin.gui;

/**
 * DOCUMENT ME!
 * 
 * @author $Author: klukas $
 * @version $Revision: 1.4 $
 */
public interface GraffitiContainer extends GraffitiComponent {
	// ~ Methods ================================================================
	
	/**
	 * Returns an unique identifier for this <code>GraffitiComponent</code>.
	 * 
	 * @return an unique identifier for this <code>GraffitiComponent</code>.
	 */
	public String getId();
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
