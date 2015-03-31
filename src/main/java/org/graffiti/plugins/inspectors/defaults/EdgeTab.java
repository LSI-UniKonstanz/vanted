// ==============================================================================
//
// EdgeTab.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: EdgeTab.java,v 1.10 2010/12/22 13:05:58 klukas Exp $

package org.graffiti.plugins.inspectors.defaults;

import org.graffiti.plugin.inspector.InspectorTab;
import org.graffiti.selection.SelectionEvent;
import org.graffiti.session.EditorSession;
import org.graffiti.session.Session;

/**
 * Represents a tabulator in the inspector, which handles the properties of
 * edges.
 * 
 * @version $Revision: 1.10 $
 */
public class EdgeTab
					extends AbstractTab {
	// ~ Constructors ===========================================================
	
	private static final long serialVersionUID = 1L;
	private static EdgeTab instance = null;
	
	/**
	 * Constructs a <code>EdgeTab</code> and sets the title.
	 */
	public EdgeTab() {
//		super();
		this.title = "Edge";
		EdgeTab.instance = this;
	}
	
	@Override
	public String getEmptyDescription() {
		return "Properties of active edge selection are editable at this place.";
	}
	
	public static EdgeTab getInstance() {
		return instance;
	}
	
	@SuppressWarnings("unchecked")
	public void selectionChanged(SelectionEvent e) {
		attributables = e.getSelection().getEdges();
		if( ! isShowing())
			return;
		rebuildTreeAction();
		
	}


	@Override
	public void sessionChanged(Session s) {
		super.sessionChanged(s);

		EditorSession editorSession = null;
		
		try {
			editorSession = (EditorSession) s;
		} catch (ClassCastException cce) {
			// No selection is made if no EditorSession is active (?)
			throw new RuntimeException("WARNING: should rarely happen " + cce);
		}
		
		setEditPanelGraphElementMap(editorSession != null ? editorSession.getGraphElementsMap() : null);
		

	}

	@Override
	public void sessionDataChanged(Session s) {
		super.sessionDataChanged(s);
		EditorSession editorSession = null;
		
		try {
			editorSession = (EditorSession) s;
		} catch (ClassCastException cce) {
			// No selection is made if no EditorSession is active (?)
			throw new RuntimeException("WARNING: should rarely happen " + cce);
		}
		
		setEditPanelGraphElementMap(editorSession != null ? editorSession.getGraphElementsMap() : null);

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
