/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.chart_settings;

import java.util.HashMap;
import java.util.Map;

import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.AttributeDescription;
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

public class ChartAttributePlugin extends IPK_PluginAdapter implements EditorPlugin {
	private Map<Class<? extends Displayable>, Class<? extends ValueEditComponent>> valueEditComponents;
	private Map<Class<? extends Attribute>, Class<? extends AttributeComponent>> attributeComponents;
	
	public ChartAttributePlugin() {
		this.attributes = new Class[] { ChartAttribute.class, ChartsColumnAttribute.class };
		
		StringAttribute.putAttributeType("component", ChartAttribute.class);
		
		valueEditComponents = new HashMap<>();
		
		valueEditComponents.put(ChartAttribute.class, ChartAttributeEditor.class);
		valueEditComponents.put(ChartsColumnAttribute.class, ChartsColumnAttributeEditor.class);
		
		attributeComponents = new HashMap<>();
		
		attributeComponents.put(ChartAttribute.class, ChartAttributeComponent.class);
		
		for (GraffitiCharts c : GraffitiCharts.values())
			ChartComponentManager.getInstance().registerChartComponent(c);
		
		attributeDescriptions = new AttributeDescription[] { new AttributeDescription(ChartsColumnAttribute.name,
				ChartsColumnAttribute.class, null, true, true, null), };
		
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