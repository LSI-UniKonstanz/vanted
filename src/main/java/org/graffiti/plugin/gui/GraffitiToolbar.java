// ==============================================================================
//
// GraffitiToolbar.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: GraffitiToolbar.java,v 1.6 2010/12/22 13:05:54 klukas Exp $

package org.graffiti.plugin.gui;

import javax.swing.JToolBar;

/**
 * TODO
 */
public class GraffitiToolbar extends JToolBar implements GraffitiContainer {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4954611265049333475L;
	
	// ~ Instance fields ========================================================
	/** The id of the toolbar. */
	protected String id;
	
	/** The id of the component the toolbar prefers to be inserted in. */
	protected String preferredComponent;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Standardconstructor for <code>GraffitiToolbar</code>.
	 */
	public GraffitiToolbar() {
		this("[not named toolbar]");
	}
	
	/**
	 * Constructor that sets the id of this <code>GraffitiToolbar</code>.
	 * 
	 * @param name
	 *           DOCUMENT ME!
	 */
	public GraffitiToolbar(String name) {
		super(name);
		this.id = name;
		this.preferredComponent = "toolbarPanel";
		
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Returns the id of this toolbar.
	 * 
	 * @return the id of this toolbar.
	 */
	public String getId() {
		return this.id;
	}
	
	/**
	 * Returns the id of the component the toolbar prefers to be inserted.
	 * 
	 * @return the id of the component the toolbar prefers to be inserted.
	 */
	public String getPreferredComponent() {
		return this.preferredComponent;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
