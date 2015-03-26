// ==============================================================================
//
// Tool.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: Tool.java,v 1.6 2010/12/22 13:05:55 klukas Exp $

package org.graffiti.plugin.tool;

import java.util.prefs.Preferences;

import javax.swing.event.MouseInputListener;

import org.graffiti.graph.Graph;

/**
 * A <code>Tool</code> executes a specified action on a <code>ConstrainedGraph</code>.
 * 
 * @see MouseInputListener
 */
public interface Tool
					extends MouseInputListener {
	// ~ Methods ================================================================
	
	/**
	 * Returns true if the tool is active.
	 * 
	 * @return DOCUMENT ME!
	 */
	public boolean isActive();
	
	/**
	 * States whether this class wants to be registered as a <code>SelectionListener</code>.
	 * 
	 * @return DOCUMENT ME!
	 */
	public boolean isSelectionListener();
	
	/**
	 * States whether this class wants to be registered as a <code>SessionListener</code>.
	 * 
	 * @return DOCUMENT ME!
	 */
	public boolean isSessionListener();
	
	/**
	 * States whether this class wants to be registered as a <code>ViewListener</code>, i.e. if it wants to get informed when
	 * another view in the same session becomes active. This method is not
	 * called when another session is activated. Implement <code>SessionListener</code> if you are interested in session changed
	 * events.
	 * 
	 * @return DOCUMENT ME!
	 */
	public boolean isViewListener();
	
	/**
	 * Called when the tool is activated.
	 */
	public void activate();
	
	/**
	 * Resets the state of the tool. Called when another tool is activated.
	 */
	public void deactivate();
	
	/**
	 * Sets the graph this tool works on.
	 * 
	 * @param g
	 *           the graph this tool should work on.
	 */
	void setGraph(Graph g);
	
	/**
	 * Sets the preferences of this tool.
	 * 
	 * @param p
	 *           the preferences of this tool.
	 */
	void setPrefs(Preferences p);
	
	public void deactivateAll();
	
	public void preProcessImageCreation();
	
	public void postProcessImageCreation();
	
	public String getToolName();
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
