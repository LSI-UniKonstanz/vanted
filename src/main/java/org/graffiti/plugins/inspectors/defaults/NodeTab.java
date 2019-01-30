// ==============================================================================
//
// NodeTab.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: NodeTab.java,v 1.9 2010/12/22 13:05:58 klukas Exp $

package org.graffiti.plugins.inspectors.defaults;

import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.inspector.InspectorTab;
import org.graffiti.selection.SelectionEvent;

/**
 * Represents the tab of the inspector, which edits the properties of a node.
 * 
 * @version $Revision: 1.9 $
 */
public class NodeTab extends AbstractTab {
	// ~ Constructors ===========================================================

	private static final long serialVersionUID = 1L;

	private static NodeTab instance = null;

	/**
	 * Constructs a <code>NodeTab</code> and sets the title.
	 */
	public NodeTab() {
		super();
		this.title = "Node";
		NodeTab.instance = this;
	}

	@Override
	public String getEmptyDescription() {
		return "Properties of active node selection are editable at this place.";
	}

	public static NodeTab getInstance() {
		return instance;
	}

	public void selectionChanged(SelectionEvent e) {
		attributables = e.getSelection().getNodes();

		if (isShowing() && !e.getSelection().isEmpty() && attributables.isEmpty()) {
			EdgeTab.getInstance().selectionChanged(e);
			MainFrame.getInstance().showAndHighlightSidePanelTab("Edge", false);
			return;
		}

		super.selectionChanged(e);
	}

	@Override
	public String getTabParentPath() {
		return "Attributes";
	}

	@Override
	public int getPreferredTabPosition() {
		return InspectorTab.TAB_LEADING;
	}

}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
