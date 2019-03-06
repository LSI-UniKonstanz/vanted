// ==============================================================================
//
// SwitchSelectionsPlugin.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: SwitchSelectionsPlugin.java,v 1.5 2010/12/22 13:05:58 klukas Exp $

package org.graffiti.plugins.guis.switchselections;

import org.graffiti.plugin.EditorPluginAdapter;
import org.graffiti.plugin.gui.GraffitiComponent;
import org.graffiti.selection.SelectionEvent;
import org.graffiti.selection.SelectionListener;

/**
 * Provides a spring embedder algorithm a la KK.
 * 
 * @version $Revision: 1.5 $
 * @vanted.revision 2.7
 */
public class SwitchSelectionsPlugin extends EditorPluginAdapter implements SelectionListener {

	private final SelectionMenu selMenu = new SelectionMenu();

	/**
	 * Creates a new TrivialGridRestrictedPlugin object.
	 */
	public SwitchSelectionsPlugin() {
		this.guiComponents = new GraffitiComponent[1];
		this.guiComponents[0] = selMenu;
	}

	@Override
	public boolean isSelectionListener() {
		return true;
	}

	public void selectionChanged(SelectionEvent e) {
		((SelectionListener) selMenu).selectionChanged(e);
	}

	public void selectionListChanged(SelectionEvent e) {
		((SelectionListener) selMenu).selectionListChanged(e);
	}
}
