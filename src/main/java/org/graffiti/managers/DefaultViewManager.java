// ==============================================================================
//
// DefaultViewManager.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: DefaultViewManager.java,v 1.11 2010/12/22 13:05:54 klukas Exp $

package org.graffiti.managers;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.graffiti.managers.pluginmgr.PluginDescription;
import org.graffiti.plugin.GenericPlugin;
import org.graffiti.plugin.view.View;
import org.graffiti.plugin.view.ViewListener;
import org.graffiti.util.InstanceCreationException;
import org.graffiti.util.InstanceLoader;

/**
 * Manages a list of view types.
 * 
 * @version $Revision: 1.11 $
 */
public class DefaultViewManager implements ViewManager {
	// ~ Instance fields ========================================================

	/** Contains the list of listeners. */
	private LinkedHashSet<ViewManagerListener> listeners;

	/** Contains the list of listeners. */
	private LinkedHashSet<ViewListener> viewListeners;

	/** Contains the class names of the available views. */
	private Set<String> views;

	// ~ Constructors ===========================================================

	/**
	 * Constructs a new view manager.
	 */
	public DefaultViewManager() {
		views = new TreeSet<String>();
		listeners = new LinkedHashSet<ViewManagerListener>();
		viewListeners = new LinkedHashSet<ViewListener>();
	}

	// ~ Methods ================================================================

	public String[] getViewNames() {
		Object[] names = views.toArray();
		String[] stringNames = new String[names.length];

		for (int i = 0; i < stringNames.length; i++) {
			stringNames[i] = (String) names[i];
		}

		return stringNames;
	}

	public String[] getViewDescriptions() {
		Object[] names = views.toArray();
		String[] stringNames = new String[names.length];

		for (int i = 0; i < stringNames.length; i++) {
			View v;
			try {
				v = createView((String) names[i]);
				stringNames[i] = v.getViewName();
				v.close();
			} catch (InstanceCreationException e) {
				stringNames[i] = (String) names[i] + " (invalid)";
			}
			// stringNames[i] = (String) names[i];
		}

		return stringNames;
	}

	public void addListener(ViewManagerListener viewManagerListener) {
		listeners.add(viewManagerListener);
	}

	public void addView(String viewType) {
		views.add(viewType);
		// logger.info("new view registered: " + viewType);

		fireViewTypeAdded(viewType);
	}

	public void addViewListener(ViewListener viewListener) {
		viewListeners.add(viewListener);
	}

	public void addViews(String[] views) {
		for (int i = 0; i < views.length; i++) {
			addView(views[i]);
		}
	}

	public View createView(String name) throws InstanceCreationException {
		return (View) InstanceLoader.createInstance(name);
	}

	public boolean hasViews() {
		return !views.isEmpty();
	}

	public void pluginAdded(GenericPlugin plugin, PluginDescription desc) {
		addViews(plugin.getViews());
		if (plugin.getDefaultView() != null)
			setDefaultView(plugin.getDefaultView());
	}

	public boolean removeListener(ViewManagerListener l) {
		return listeners.remove(l);
	}

	public boolean removeViewListener(ViewListener l) {
		return viewListeners.remove(l);
	}

	public void viewChanged(View newView) {
		for (ViewListener vl : viewListeners) {
			vl.viewChanged(newView);
		}
	}

	/**
	 * Informs all view manager listeners, that the given view type is available.
	 * 
	 * @param viewType
	 *            the new view type.
	 */
	private void fireViewTypeAdded(String viewType) {
		for (Iterator<ViewManagerListener> i = listeners.iterator(); i.hasNext();) {
			ViewManagerListener l = i.next();
			l.viewTypeAdded(viewType);
		}
	}

	public void removeViews() {
		views.clear();
	}

	String defaultView;

	public String getDefaultView() {
		return defaultView;
	}

	public void setDefaultView(String defaultView) {
		this.defaultView = defaultView;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
