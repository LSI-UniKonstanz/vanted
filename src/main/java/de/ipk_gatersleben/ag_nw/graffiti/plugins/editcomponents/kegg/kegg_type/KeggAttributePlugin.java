/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.kegg.kegg_type;

import java.util.HashMap;
import java.util.Map;

import org.graffiti.attributes.Attribute;
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

public class KeggAttributePlugin extends IPK_PluginAdapter implements EditorPlugin {
	private HashMap<Class<? extends Displayable>, Class<? extends ValueEditComponent>> valueEditComponents;
	private Map<Class<? extends Attribute>, Class<? extends AttributeComponent>> attributeComponents;
	
	@SuppressWarnings("unchecked")
	public KeggAttributePlugin() {
		
		this.algorithms = new Algorithm[] {};
		
		this.attributes = new Class[] { KeggTypeAttribute.class, KeggRelationTypeAttribute.class,
				KeggRelationSubTypeAttribute.class, KeggReactionTypeAttribute.class, KeggIdAttribute.class,
				KeggReactionIdAttribute.class, KeggGroupPartAttribute.class, KeggRelationSrcTgtAttribute.class };
		
		StringAttribute.putAttributeType("grouppart", KeggGroupPartAttribute.class);
		
		StringAttribute.putAttributeType("relation_src_tgt", KeggRelationSrcTgtAttribute.class);
		for (int index = 0; index < 100; index++)
			StringAttribute.putAttributeType("relation_src_tgt" + index, KeggRelationSrcTgtAttribute.class);
		
		StringAttribute.putAttributeType("kegg_type", KeggTypeAttribute.class);
		for (int index = 0; index < 100; index++)
			StringAttribute.putAttributeType("kegg_type" + index, KeggTypeAttribute.class);
		
		StringAttribute.putAttributeType("kegg_reaction", KeggTypeAttribute.class);
		for (int index = 0; index < 100; index++) {
			StringAttribute.putAttributeType("kegg_reaction" + index, KeggReactionIdAttribute.class);
			StringAttribute.putAttributeType("kegg_reaction_product" + index, KeggReactionIdAttribute.class);
			StringAttribute.putAttributeType("kegg_reaction_substrate" + index, KeggReactionIdAttribute.class);
		}
		
		StringAttribute.putAttributeType("kegg_name", KeggIdAttribute.class);
		StringAttribute.putAttributeType("kegg_name_old", KeggIdAttribute.class);
		for (int index = 0; index < 100; index++)
			StringAttribute.putAttributeType("kegg_name" + index, KeggIdAttribute.class);
		
		StringAttribute.putAttributeType("relation_type", KeggRelationTypeAttribute.class);
		for (int index = 0; index < 100; index++)
			StringAttribute.putAttributeType("relation_type" + index, KeggRelationTypeAttribute.class);
		
		StringAttribute.putAttributeType("relation_subtype", KeggRelationSubTypeAttribute.class);
		for (int index = 0; index < 100; index++)
			StringAttribute.putAttributeType("relation_subtype" + index, KeggRelationSubTypeAttribute.class);
		
		StringAttribute.putAttributeType("kegg_reaction_type", KeggReactionTypeAttribute.class);
		for (int index = 0; index < 100; index++)
			StringAttribute.putAttributeType("kegg_reaction_type" + index, KeggReactionTypeAttribute.class);
		
		valueEditComponents = new HashMap<>();
		attributeComponents = new HashMap<>();
		
		valueEditComponents.put(KeggTypeAttribute.class, KeggTypeAttributeEditor.class);
		valueEditComponents.put(KeggRelationTypeAttribute.class, KeggRelationTypeAttributeEditor.class);
		valueEditComponents.put(KeggRelationSubTypeAttribute.class, KeggRelationSubTypeAttributeEditor.class);
		valueEditComponents.put(KeggReactionTypeAttribute.class, KeggReactionTypeAttributeEditor.class);
		valueEditComponents.put(KeggReactionIdAttribute.class, KeggReactionIdAttributeEditor.class);
		valueEditComponents.put(KeggGroupPartAttribute.class, KeggGroupPartAttributeEditor.class);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graffiti.plugin.EditorPlugin#getAttributeComponents()
	 */
	public Map<Class<? extends Attribute>, Class<? extends AttributeComponent>> getAttributeComponents() {
		return attributeComponents;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graffiti.plugin.EditorPlugin#getGUIComponents()
	 */
	public GraffitiComponent[] getGUIComponents() {
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graffiti.plugin.EditorPlugin#getModes()
	 */
	public Mode[] getModes() {
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graffiti.plugin.EditorPlugin#getShapes()
	 */
	public GraffitiShape[] getShapes() {
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graffiti.plugin.EditorPlugin#getTools()
	 */
	public Tool[] getTools() {
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graffiti.plugin.EditorPlugin#getValueEditComponents()
	 */
	public Map<Class<? extends Displayable>, Class<? extends ValueEditComponent>> getValueEditComponents() {
		return valueEditComponents;
	}
}