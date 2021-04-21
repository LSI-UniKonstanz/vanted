// ==============================================================================
//
// EditComponentManager.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: EditComponentManager.java,v 1.8 2010/12/22 13:05:54 klukas Exp $

package org.graffiti.managers;

import java.util.HashMap;
import java.util.Map;

import org.graffiti.editor.EditComponentNotFoundException;
import org.graffiti.managers.pluginmgr.PluginDescription;
import org.graffiti.managers.pluginmgr.PluginManagerListener;
import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.EditorPlugin;
import org.graffiti.plugin.GenericPlugin;
import org.graffiti.plugin.editcomponent.ValueEditComponent;
import org.graffiti.util.InstanceCreationException;
import org.graffiti.util.InstanceLoader;

/**
 * Contains the mapping between displayable classes and their representation as
 * <code>AttributeComponent</code> classes.
 * 
 * @author ph
 * @version $Revision: 1.8 $
 */
public class EditComponentManager implements PluginManagerListener {
	// ~ Instance fields ========================================================
	
	/** Maps displayable classes to ValueEditComponent classes. */
	private Map<Class<? extends Displayable>, Class<? extends ValueEditComponent>> valueEditComponents;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs an EditComponentManager.
	 */
	public EditComponentManager() {
		this.valueEditComponents = new HashMap<Class<? extends Displayable>, Class<? extends ValueEditComponent>>();
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Returns the map of value edit components.
	 * 
	 * @return DOCUMENT ME!
	 */
	public Map<Class<? extends Displayable>, Class<? extends ValueEditComponent>> getEditComponents() {
		return valueEditComponents;
	}
	
	/**
	 * Returns an instance of the ValueEditComponent that is capable of providing a
	 * possibility to alter the value of the displayable with type
	 * <code>aType</code>.
	 * 
	 * @param aType
	 *           the class of the displayable to retrieve a component for.
	 * @return an instance of an ValueEditComponent.
	 * @throws EditComponentNotFoundException
	 *            DOCUMENT ME!
	 */
	public ValueEditComponent getValueEditComponent(Displayable aType) throws EditComponentNotFoundException {
		if (!(valueEditComponents.containsKey(aType.getClass()))) {
			throw new EditComponentNotFoundException("No registered ValueEditComponent for displayable type " + aType);
		}
		
		Class<? extends ValueEditComponent> ac = valueEditComponents.get(aType.getClass());
		
		// ValueEditComponent component = ac.newInstance();
		ValueEditComponent component;
		try {
			component = (ValueEditComponent) InstanceLoader.createInstance(ac, "org.graffiti.plugin.Displayable",
					aType);
			// component = (ValueEditComponent) InstanceLoader.createInstance(ac,
			// aType);
			component.setDisplayable(aType);
			component.setEditFieldValue();
			
			return component;
		} catch (InstanceCreationException e) {
			
			e.printStackTrace();
		}
		
		throw new EditComponentNotFoundException("No registered ValueEditComponent for displayable type " + aType);
	}
	
	/**
	 * Called by the plugin manager, iff a plugin has been added.
	 * 
	 * @param plugin
	 *           the added plugin.
	 * @param desc
	 *           the description of the new plugin.
	 */
	public void pluginAdded(GenericPlugin plugin, PluginDescription desc) {
		// System.out.println("putting: " + plugin.getAttributeComponents());
		if (plugin instanceof EditorPlugin) {
			if (((EditorPlugin) plugin).getValueEditComponents() != null
					&& ((EditorPlugin) plugin).getValueEditComponents().size() > 0) {
				valueEditComponents.putAll(((EditorPlugin) plugin).getValueEditComponents());
			}
		}
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
