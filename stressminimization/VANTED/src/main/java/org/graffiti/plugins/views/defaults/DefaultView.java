// ==============================================================================
//
// DefaultView.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: DefaultView.java,v 1.4 2010/12/22 13:06:19 klukas Exp $

package org.graffiti.plugins.views.defaults;

import org.graffiti.plugin.EditorPluginAdapter;

/**
 * PlugIn for default view of graffiti graph editor
 * 
 * @version $Revision: 1.4 $
 */
public class DefaultView extends EditorPluginAdapter {
	// ~ Constructors ===========================================================

	/**
	 * Constructor for DefaultView.
	 */
	public DefaultView() {
		super();
		this.views = new String[1];
		this.views[0] = "org.graffiti.plugins.views.defaults.GraffitiView";
	}

	// probably the method configure(Preferences pref) will be overridden.
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
