/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.url_attribute;

import java.util.HashMap;
import java.util.Map;

import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.StringAttribute;
import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.EditorPlugin;
import org.graffiti.plugin.actions.URLattributeAction;
import org.graffiti.plugin.editcomponent.ValueEditComponent;
import org.graffiti.plugin.gui.GraffitiComponent;
import org.graffiti.plugin.mode.Mode;
import org.graffiti.plugin.tool.Tool;
import org.graffiti.plugin.view.AttributeComponent;
import org.graffiti.plugin.view.GraffitiShape;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;

public class URLAttributePlugin extends IPK_PluginAdapter implements EditorPlugin {
	private HashMap<Class<? extends Displayable>, Class<? extends ValueEditComponent>> valueEditComponents;
	private Map<Class<? extends Attribute>, Class<? extends AttributeComponent>> attributeComponents;
	
	public URLAttributePlugin() {
		this.attributes = new Class[] { URLAttribute.class };
		
		StringAttribute.putAttributeType("kegg_link", URLAttribute.class);
		StringAttribute.putAttributeType("kegg_map_link", URLAttribute.class);
		StringAttribute.putAttributeType("kegg_link_reaction", URLAttribute.class);
		StringAttribute.putAttributeType("kegg_image", URLAttribute.class);
		StringAttribute.putAttributeType("xml_url", URLAttribute.class);
		StringAttribute.putAttributeType("kegg_link_os", URLAttribute.class);
		StringAttribute.putAttributeType("kegg_map_link_os", URLAttribute.class);
		StringAttribute.putAttributeType("kegg_link_reaction_os", URLAttribute.class);
		StringAttribute.putAttributeType("kegg_image_os", URLAttribute.class);
		StringAttribute.putAttributeType("url", URLAttribute.class);
		StringAttribute.putAttributeType("pathway_ref_url", URLAttribute.class);
		StringAttribute.putAttributeType("xml_url_os", URLAttribute.class);
		
		for (int index = 0; index < 100; index++)
			StringAttribute.putAttributeType("kegg_link" + index, URLAttribute.class);
		
		for (int index = 0; index < 100; index++)
			StringAttribute.putAttributeType("pathway_ref_url" + index, URLAttribute.class);
		
		valueEditComponents = new HashMap<>();
		attributeComponents = new HashMap<>();
		
		valueEditComponents.put(URLAttribute.class, URLAttributeEditor.class);
	}
	
	@Override
	public URLattributeAction[] getURLattributeActions() {
		return new URLattributeAction[] { new LoadGraphFileAttributeAction(), new LoadURLattributeAction() };
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