/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.compound_image;

import java.util.HashMap;
import java.util.Map;

import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.AttributeDescription;
import org.graffiti.attributes.DoubleAttribute;
import org.graffiti.attributes.StringAttribute;
import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.EditorPlugin;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.editcomponent.ValueEditComponent;
import org.graffiti.plugin.gui.GraffitiComponent;
import org.graffiti.plugin.mode.Mode;
import org.graffiti.plugin.tool.Tool;
import org.graffiti.plugin.view.AttributeComponent;
import org.graffiti.plugin.view.GraffitiShape;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;

public class CompoundImageAttributePlugin extends IPK_PluginAdapter implements EditorPlugin {
	
	/**
	 * Maps from an attribute class to an AttributeComponent class. old comment: A
	 * <code>java.util.Map</code> from <code>Attribute</code> to the corresponding
	 * <code>LabelValueRow</code>-instance.
	 */
	protected Map<Class<? extends Attribute>, Class<? extends AttributeComponent>> attributeComponents;
	
	/** The mapping between attribute classes and attributeComponent classes. */
	protected HashMap<Class<? extends Displayable>, Class<? extends ValueEditComponent>> valueEditComponents;
	
	public CompoundImageAttributePlugin() {
		this.attributes = new Class[1];
		this.attributes[0] = CompoundAttribute.class;
		
		this.algorithms = new Algorithm[] { new ImageAssignmentCommand() };
		
		StringAttribute.putAttributeType("image_url", CompoundAttribute.class);
		StringAttribute.putAttributeType("image_position", CompoundPositionAttribute.class);
		
		valueEditComponents = new HashMap<>();
		attributeComponents = new HashMap<>();
		
		valueEditComponents.put(CompoundAttribute.class, CompoundImageAttributeEditor.class);
		valueEditComponents.put(CompoundPositionAttribute.class, CompoundImagePositionAttributeEditor.class);
		
		attributeComponents.put(CompoundAttribute.class, CompoundImageAttributeComponent.class);
		
		this.attributeDescriptions = new AttributeDescription[] { new AttributeDescription("image_border",
				DoubleAttribute.class, "Image:<html>&nbsp;Border", true, false, null) };
		
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