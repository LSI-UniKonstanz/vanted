// ==============================================================================
//
// ZoomPlugin.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: ZoomPlugin.java,v 1.6 2010/12/22 13:06:20 klukas Exp $

package org.graffiti.plugins.tools.enhancedzoomtool;

import org.graffiti.plugin.EditorPluginAdapter;
import org.graffiti.plugin.gui.GraffitiComponent;

/**
 * This plug-in is contained in the standard editing tools.
 * 
 * @version $Revision: 1.6 $
 * @vanted.revision 2.7
 */
public class ZoomPlugin extends EditorPluginAdapter {

	/** The button for the zoom tool */
	private final GraffitiComponent zoomButton;

	/**
	 * Creates a new StandardTools object.
	 */
	public ZoomPlugin() {
		zoomButton = new ZoomChangeComponent("defaultToolbar");
		guiComponents = new GraffitiComponent[1];
		guiComponents[0] = zoomButton;
	}
}
