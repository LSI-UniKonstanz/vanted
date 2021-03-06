// ==============================================================================
//
// AbstractGraffitiComponent.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: AbstractGraffitiComponent.java,v 1.5 2010/12/22 13:05:54 klukas Exp $

package org.graffiti.plugin.gui;

import javax.swing.JPanel;

import org.graffiti.editor.MainFrame;

/**
 * Abstract class for default containers.
 * 
 * @version $Revision: 1.5 $
 */
public abstract class AbstractGraffitiComponent extends JPanel implements GraffitiComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3884722947525157168L;
	// ~ Instance fields ========================================================
	/**
	 * The component wherer the current <code>AbstractGraffitiContainer</code>
	 * prefers to be inserted.
	 */
	protected String preferredComponent;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new <code>AbstractGraffitiContainer</code>.
	 */
	protected AbstractGraffitiComponent() {
		super();
	}
	
	/**
	 * Constructs a new <code>AbstractGraffitiContainer</code>.
	 * 
	 * @param prefComp
	 *           DOCUMENT ME!
	 */
	protected AbstractGraffitiComponent(String prefComp) {
		super();
		this.preferredComponent = prefComp;
	}
	
	// ~ Methods ================================================================
	
	/**
	 * @see org.graffiti.plugin.gui.GraffitiComponent#setMainFrame(org.graffiti.editor.MainFrame)
	 */
	public void setMainFrame(MainFrame mf) {
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public String getPreferredComponent() {
		return this.preferredComponent;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
