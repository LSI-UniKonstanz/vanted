/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.cluster_colors;

import java.util.HashMap;
import java.util.Map;

import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.StringAttribute;
import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.EditorPlugin;
import org.graffiti.plugin.editcomponent.ValueEditComponent;
import org.graffiti.plugin.gui.GraffitiComponent;
import org.graffiti.plugin.mode.Mode;
import org.graffiti.plugin.tool.Tool;
import org.graffiti.plugin.view.AttributeComponent;
import org.graffiti.plugin.view.GraffitiShape;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;

public class ClusterColorAttributePlugin extends IPK_PluginAdapter implements EditorPlugin {

	private HashMap<Class<? extends Displayable>, Class<? extends ValueEditComponent>> valueEditComponents;
	private Map<Class<? extends Attribute>, Class<? extends AttributeComponent>> attributeComponents;

	public ClusterColorAttributePlugin() {
		this.attributes = new Class[1];
		this.attributes[0] = ClusterColorAttribute.class;

		StringAttribute.putAttributeType(ClusterColorAttribute.attributeName, ClusterColorAttribute.class);

		valueEditComponents = new HashMap<>();
		valueEditComponents.put(ClusterColorAttribute.class, ClusterColorAttributeEditor.class);
		valueEditComponents.put(ClusterColorParameter.class, ClusterColorAttributeEditor.class);
	}

	public Map<Class<? extends Attribute>, Class<? extends AttributeComponent>> getAttributeComponents() {
		return attributeComponents;
	}

	public GraffitiComponent[] getGUIComponents() {
		return null;
	}

	public Mode[] getModes() {
		return null;
	}

	public GraffitiShape[] getShapes() {
		return null;
	}

	public Tool[] getTools() {
		return null;
	}

	public Map<Class<? extends Displayable>, Class<? extends ValueEditComponent>> getValueEditComponents() {
		return valueEditComponents;
	}
}