/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

/*
 * $Id$
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control;

import org.graffiti.plugin.inspector.InspectorTab;

import de.ipk_gatersleben.ag_nw.graffiti.DBE_EditorPluginAdapter;

/**
 * @author Christian Klukas
 */
public class PatternTabsForClusterAnalysis extends DBE_EditorPluginAdapter {
	
	/**
	 * Creates a new PatternTabsForInspector object.
	 */
	public PatternTabsForClusterAnalysis() {
		super();
		this.tabs = new InspectorTab[] { new TabPluginControl() };
	}
}
