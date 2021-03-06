// ==============================================================================
//
// Inspector.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: Inspector.java,v 1.23 2010/12/22 13:05:58 klukas Exp $

package org.graffiti.plugins.inspectors.defaults;

import java.awt.Component;
import java.util.Map;

import org.ErrorMsg;
import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.EditorPluginAdapter;
import org.graffiti.plugin.editcomponent.NeedEditComponents;
import org.graffiti.plugin.editcomponent.ValueEditComponent;
import org.graffiti.plugin.gui.GraffitiComponent;
import org.graffiti.plugin.inspector.InspectorPlugin;
import org.graffiti.plugin.inspector.InspectorTab;
import org.graffiti.plugin.view.View;
import org.graffiti.plugin.view.ViewListener;
import org.graffiti.selection.SelectionEvent;
import org.graffiti.selection.SelectionListener;
import org.graffiti.session.EditorSession;
import org.graffiti.session.Session;
import org.graffiti.session.SessionListener;

/**
 * Represents the main class of the inspector plugin.
 * 
 * @version $Revision: 1.23 $
 */
public class Inspector extends EditorPluginAdapter
		implements InspectorPlugin, SessionListener, SelectionListener, NeedEditComponents, ViewListener {
	// ~ Static fields/initializers =============================================
	
	/** The default width of the inspector components. */
	public static final int DEFAULT_WIDTH = 120;
	
	// ~ Instance fields ========================================================
	
	/** DOCUMENT ME! */
	private final InspectorContainer container;
	
	// private final HashMap<String, InspectorTab> rememberedTabs = new
	// HashMap<String, InspectorTab>();
	
	/** DOCUMENT ME! */
	private Session activeSession;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new inspector instance.
	 */
	public Inspector() {
		super();
		this.container = new InspectorContainer();
		
		// the container should be made visible, if the
		// session changed. See the sessionChanged method for details (jf).
		// this.container.setVisible(false);
		this.guiComponents = new GraffitiComponent[] { container };
		
		tabs = new InspectorTab[] { new EdgeTab(), new NodeTab(), new GraphTab() };
	}
	
	// ~ Methods ================================================================
	
	/**
	 * @see org.graffiti.plugin.editcomponent.NeedEditComponents#setEditComponentMap(Map)
	 */
	public void setEditComponentMap(Map<Class<? extends Displayable>, Class<? extends ValueEditComponent>> ecMap) {
		this.valueEditComponents = ecMap;
	}
	
	/**
	 * Returns the <code>InspectorContainer</code>.
	 * 
	 * @return DOCUMENT ME!
	 */
	public InspectorContainer getInspectorContainer() {
		return this.container;
	}
	
	/**
	 * @see org.graffiti.plugin.GenericPlugin#isSelectionListener()
	 */
	@Override
	public boolean isSelectionListener() {
		return true;
	}
	
	/**
	 * States whether this class wants to be registered as a
	 * <code>SessionListener</code>.
	 * 
	 * @return DOCUMENT ME!
	 */
	@Override
	public boolean isSessionListener() {
		return true;
	}
	
	@Override
	public boolean isViewListener() {
		return true;
	}
	
	/**
	 * Returns an array containing all the <code>InspectorTab</code>s of the
	 * <code>InspectorPlugin</code>.
	 * 
	 * @return an array containing all the <code>InspectorTab</code>s of the
	 *         <code>InspectorPlugin</code>.
	 */
	public synchronized InspectorTab[] getTabs() {
		return container.getTabs().toArray(new InspectorTab[0]);
	}
	
	/**
	 * Adds another <code>InspectorTab</code> to the current
	 * <code>InspectorPlugin</code>.
	 * 
	 * @param tab
	 *           the <code>InspectorTab</code> to be added to the
	 *           <code>InspectorPlugin</code>.
	 * @throws RuntimeException
	 *            DOCUMENT ME!
	 */
	public synchronized void addTab(InspectorTab tab) {
		InspectorTab[] tabs = getTabs();
		boolean found = false;
		if (tabs != null && tab != null && tab.getTitle() != null)
			for (int i = 0; i < tabs.length; i++) {
				if (tabs[i].getTitle() != null)
					if (tabs[i].getTitle().equals(tab.getTitle())) {
						found = true;
						break;
					}
			}
		if (found || tab == null || tab.getTitle() == null) {
			return;
		}
		
		EditorSession editorSession = null;
		
		try {
			editorSession = (EditorSession) activeSession;
		} catch (ClassCastException cce) {
			// No selection is made if no EditorSession is active (?)
			throw new RuntimeException("WARNING: should rarely happen " + cce);
		}
		
		tab.setEditPanelInformation(valueEditComponents,
				editorSession != null ? editorSession.getGraphElementsMap() : null);
		
		if (!container.getTabs().contains(tab)) {
			/*
			 * switch(tab.getPreferredTabPosition()) {
			 * 
			 * case InspectorTab.TAB_LEADING: container.insertTab(tab.getTitle(), null, tab,
			 * null, 0); break; case InspectorTab.TAB_TRAILING: default:
			 * container.addTab(tab.getTitle(), null, tab); }
			 */
			container.addTab(tab, tab.getIcon());
		}
		
		if (MainFrame.getInstance() != null && MainFrame.getInstance().getActiveSession() != null)
			viewChanged(MainFrame.getInstance().getActiveSession().getActiveView());
		else
			viewChanged(null);
	}
	
	/**
	 * Inspector relies on the edit components to be up-to-date.
	 */
	@Override
	public boolean needsEditComponents() {
		return true;
	}
	
	/**
	 * Is called, if something in the selection model changed.
	 * 
	 * @param e
	 *           DOCUMENT ME!
	 */
	public void selectionChanged(SelectionEvent e) {
		for (InspectorTab tab : getTabs()) {
			if (tab.isSelectionListener()) {
				SelectionListener sl = (SelectionListener) tab;
				sl.selectionChanged(e);
			}
		}
	}
	
	public void selectionListChanged(SelectionEvent e) {
		for (InspectorTab tab : getTabs()) {
			if (tab.isSelectionListener()) {
				SelectionListener sl = (SelectionListener) tab;
				sl.selectionListChanged(e);
			}
		}
	}
	
	/**
	 * This method is called when the session changes.
	 * 
	 * @param s
	 *           the new Session.
	 * @throws RuntimeException
	 *            DOCUMENT ME!
	 */
	public synchronized void sessionChanged(Session s) {
		for (InspectorTab tab : getTabs()) {
			if (tab instanceof SessionListener) {
				SessionListener sl = (SessionListener) tab;
				try {
					sl.sessionChanged(s);
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
				}
			}
		}
		if (s == null)
			viewChanged(null);
	}
	
	/**
	 * This method is called when the session data (but not the session's graph
	 * data) changed.
	 * 
	 * @param s
	 *           Session
	 */
	public void sessionDataChanged(Session s) {
		for (InspectorTab tab : getTabs()) {
			if (tab instanceof SessionListener) {
				SessionListener sl = (SessionListener) tab;
				sl.sessionDataChanged(s);
			}
		}
	}
	
	public void viewChanged(View newView) {
		
		for (InspectorTab tab : container.getTabs()) {
			if (!tab.visibleForView(newView) || (newView != null && !newView.worksWithTab(tab))) {
				container.hideTab(tab);
			} else {
				container.showTab(tab);
			}
		}
		
		for (InspectorTab tab : getTabs()) {
			if (tab instanceof ViewListener) {
				ViewListener sl = (ViewListener) tab;
				sl.viewChanged(newView);
			}
		}
		
	}
	
	public void setSelectedTab(InspectorTab tab) {
		if (tab != null && container != null && container.getTabs() != null && container.getTabs().contains(tab))
			container.setSelectedComponent(tab);
	}
	
	public InspectorTab getSelectedTab() {
		Component c = container.getSelectedComponent();
		if (c != null && c instanceof InspectorTab) {
			return (InspectorTab) c;
		} else
			return null;
	}
	
	/**
	 * gets called each time a tab is added, to figure out the order of tab layout
	 * as given by their preferredTab Position parameter in InspectorTab
	 */
	// private void sortTabs() {
	// Collections.sort(container.getTabs(), new Comparator<InspectorTab>() {
	//
	// @Override
	// public int compare(InspectorTab o1, InspectorTab o2) {
	// if(o1.getPreferredTabPosition() == o2.getPreferredTabPosition())
	// return 0;
	// else return o1.getPreferredTabPosition() < o2.getPreferredTabPosition() ? -1
	// : 1;
	// }
	//
	// });
	// }
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
