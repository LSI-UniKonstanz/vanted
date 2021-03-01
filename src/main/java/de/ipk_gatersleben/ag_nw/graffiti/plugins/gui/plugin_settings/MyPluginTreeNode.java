/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.plugin_settings;

import javax.swing.tree.DefaultMutableTreeNode;

public class MyPluginTreeNode extends DefaultMutableTreeNode {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7308527676284643518L;
	String nodeText;
	Class<?> identifyAs;
	
	public MyPluginTreeNode(String nodeText, Object data, Class<?> identifyAs) {
		super(data);
		this.nodeText = nodeText;
		this.identifyAs = identifyAs;
	}
	
	@Override
	public String toString() {
		return nodeText;
	}
	
	public Class<?> getClassType() {
		return identifyAs;
	}
	
}