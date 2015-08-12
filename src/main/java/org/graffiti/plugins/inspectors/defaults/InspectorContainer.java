// ==============================================================================
//
// InspectorContainer.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: InspectorContainer.java,v 1.12 2010/12/22 13:05:58 klukas Exp $

package org.graffiti.plugins.inspectors.defaults;

import java.awt.Dimension;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.ImageIcon;
import javax.swing.JTabbedPane;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.graffiti.plugin.gui.GraffitiContainer;
import org.graffiti.plugin.inspector.InspectorTab;
import org.graffiti.plugin.inspector.SubtabHostTab;

/**
 * Represents the central gui component of the inspector plugin.
 * 
 * @version $Revision: 1.12 $
 */
public class InspectorContainer
extends JTabbedPane
implements GraffitiContainer {

	static Logger logger = Logger.getLogger(InspectorContainer.class);
	static {
		logger.setLevel(Level.INFO); //Adjust for debugging purposes
	}
	// ~ Instance fields ========================================================

	/** The tabbed pane for the edge-, node- and graph-tab. */

	// private JTabbedPane tabbedPane;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** The list of the inspector's tabs. */
	private List<InspectorTab> tabs;

	/** The id of this GraffitiContainer. */
	private String id = "inspector";

	/** DOCUMENT ME! */
	private String name = "Inspector";

	/** The id of the component this component wants to be added to. */
	private String preferredComponent = "pluginPanel";


	private final LinkedHashSet<InspectorTab> hiddenTabs = new LinkedHashSet<InspectorTab>();

	// ~ Constructors ===========================================================

	/**
	 * Creates a new InspectorContainer object.
	 */
	public InspectorContainer() {
		tabs = new LinkedList<InspectorTab>();
		this.revalidate();
	}

	// ~ Methods ================================================================

	/**
	 * @see org.graffiti.plugin.gui.GraffitiContainer#getId()
	 */
	public String getId() {
		return id;
	}

	/**
	 * @see org.graffiti.plugin.gui.GraffitiComponent#getPreferredComponent()
	 */
	public String getPreferredComponent() {
		return preferredComponent;
	}

	public synchronized List<InspectorTab> getTabs() {
		return tabs;
	}

	public synchronized String getTitle() {
		return name;
	}

	/**
	 * Adds a tab to the inspector.
	 * 
	 * @param tab
	 *           the tab to add to the inspector.
	 */
	public synchronized void addTab(InspectorTab tab, ImageIcon icon) {
		addTabInHierarchy(tab, icon);
	}

	/**
	 * adds a given tab into the hierarchy of tabs.
	 * If this hierachy doesn't exist yet it is created
	 * 
	 * The hierarchy is given by the tabs category string. This category string
	 * is a '.' separated multi token string where each token represents a hierarchy level
	 *
	 */
	private synchronized void addTabInHierarchy(InspectorTab tab, ImageIcon icon) {

		String nestedTabPath = tab.getTabParentPath();

		if(nestedTabPath == null) {


			tabs.add(tab);


			//			addTab(tab.getTitle(), icon, tab);
			switch(tab.getPreferredTabPosition()) {

			case InspectorTab.TAB_LEADING:
				insertTab(tab.getTitle(), null, tab, null, 0);
				break;
			case InspectorTab.TAB_TRAILING:
			default:
				addTab(tab.getTitle(), null, tab);
			}

		} else {
			JTabbedPane curTabbedPane = this;
			SubtabHostTab curHierarchySubHostTab = null; //Inspectortab, that can have subtabs
			Collection<InspectorTab> curListInspectorTabs = tabs;
			StringTokenizer pathtokenizer = new StringTokenizer(nestedTabPath,".");

			while(pathtokenizer.hasMoreTokens()) {
				curHierarchySubHostTab = null;
				String curHierarchyTabName = pathtokenizer.nextToken();

				for(InspectorTab curTab : curListInspectorTabs){ //check all tabs of the current level
					if(curTab.getTitle().equals(curHierarchyTabName)) {
						if(curTab instanceof SubtabHostTab) {
							curHierarchySubHostTab = (SubtabHostTab)curTab;
							curTabbedPane = curHierarchySubHostTab.getTabbedPane();
							break;
						} else {
							throw new RuntimeException("Cannot add a "+tab.getTitle()+" into hierarchy, since "+curTab.getTitle()+" is not of type SubtabHostTab");
						}

					}
				}
				/* sub host tab not found, create one */
				if(curHierarchySubHostTab == null) {
					curHierarchySubHostTab = new SubtabHostTab(curHierarchyTabName);
					curListInspectorTabs.add(curHierarchySubHostTab); //add new subtabhosttab to current list of of tabs of current level
					logger.debug("adding subhosttab:"+curHierarchySubHostTab.getTitle()+" to "+curTabbedPane.toString());
					curTabbedPane.addTab(curHierarchySubHostTab.getTitle(), curHierarchySubHostTab.getIcon(), curHierarchySubHostTab);
					curTabbedPane = curHierarchySubHostTab.getTabbedPane();
				}
				curListInspectorTabs = curHierarchySubHostTab.getTabs(); //for the next 'recursive' call into the hierarchy
			}

			if(curHierarchySubHostTab != null) {
				logger.debug("adding tab:"+tab.getTitle()+" to "+curHierarchySubHostTab.getTitle());
				curHierarchySubHostTab.addTab(tab, icon);
			}

		}

	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param g
	 *           DOCUMENT ME!
	 */
	@Override
	public synchronized void paintComponent(java.awt.Graphics g) {
		super.paintComponent(g);
	}

	/**
	 * Removes a tab from the inspector.
	 * 
	 * @param tab
	 *           the tab to remove from the inspector.
	 */
	public void removeTab(InspectorTab tab) {
		int idx = indexOfTab(tab.getTitle());
		if (idx >= 0) {
			removeTabAt(idx);
		}
		if (tabs.contains(tab))
			tabs.remove(tab);
	}

	public void hideTab(InspectorTab tab) {
		int idx = indexOfTab(tab.getName());
		if (idx >= 0) {
			removeTabAt(idx);
			hiddenTabs.add(tab);
			tab.setVisible(false);
		}
	}
	public void showTab(InspectorTab tab) {
		if (hiddenTabs.contains(tab)) {
			addTab(tab.getTitle(), tab);
			hiddenTabs.remove(tab);
//			tab.setVisible(true);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.Component#getPreferredSize()
	 */
	@Override
	public synchronized Dimension getPreferredSize() {
		return getParent().getSize();
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
