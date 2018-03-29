// ==============================================================================
//
// GraphTab.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: GraphTab.java,v 1.11 2010/12/22 13:05:58 klukas Exp $

package org.graffiti.plugins.inspectors.defaults;

import java.util.ArrayList;

import org.graffiti.attributes.Attributable;
import org.graffiti.event.AttributeEvent;
import org.graffiti.plugin.inspector.InspectorTab;
import org.graffiti.selection.SelectionEvent;
import org.graffiti.session.Session;

/**
 * Represents the tab, which contains the functionality to edit the attributes
 * of the current graph object.
 * 
 * @version $Revision: 1.11 $
 */
public class GraphTab extends AbstractTab {
	// ~ Instance fields ========================================================

	private static final long serialVersionUID = 1L;

	private static GraphTab instance = null;

	// ~ Constructors ===========================================================

	/**
	 * Constructs a <code>GraphTab</code> and sets the title.
	 */
	public GraphTab() {
		super();
		this.title = "Network";
		instance = this;
	}

	@Override
	public String getEmptyDescription() {
		return "Properties of active network are editable at this place.";
	}

	@Override
	public String getTabNameForAttributeDescription() {
		return "Network";
	}

	public static GraphTab getInstance() {
		return instance;
	}

	// ~ Methods ================================================================

	/**
	 * If the path to the changed attribute is ".directed", then the directed
	 * property of the graph (attributable of the attribute) is updated.
	 * 
	 * @see org.graffiti.event.AttributeListener#postAttributeChanged(org.graffiti.event.AttributeEvent)
	 */
	@Override
	public void postAttributeChanged(AttributeEvent e) {
		super.postAttributeChanged(e);
	}

	public void selectionChanged(SelectionEvent e) {
		// empty
	}

	@Override
	public void sessionChanged(Session s) {
		ArrayList<Attributable> ge = new ArrayList<Attributable>();
		if (s != null)
			ge.add(s.getGraph());
		attributables = ge;
		super.sessionChanged(s);

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
