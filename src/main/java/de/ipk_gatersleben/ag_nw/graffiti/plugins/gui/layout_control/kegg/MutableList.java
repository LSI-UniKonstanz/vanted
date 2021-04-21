/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg;

import javax.swing.DefaultListModel;
import javax.swing.JList;

public class MutableList<T> extends JList<T> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4487868931153791436L;
	
	public MutableList(DefaultListModel<T> model) {
		super(model);
	}
	
	public DefaultListModel<T> getContents() {
		return (DefaultListModel<T>) getModel();
	}
}