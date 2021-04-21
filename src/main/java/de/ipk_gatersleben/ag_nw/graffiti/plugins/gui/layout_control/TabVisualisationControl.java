/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import javax.swing.JLabel;

import org.graffiti.event.AttributeEvent;
import org.graffiti.event.TransactionEvent;
import org.graffiti.plugin.inspector.InspectorTab;
import org.graffiti.plugin.view.View;
import org.graffiti.session.Session;

/**
 * Represents the tab, which contains the functionality to edit the attributes
 * of the current graph object.
 * 
 * @version $Revision$
 * @deprecated Do not use, will be removed in future.
 * @vanted.revision 2.7.0 Deprecate class.
 */
public class TabVisualisationControl extends InspectorTab {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1822597920551426964L;
	
	/**
	 * DOCUMENT ME!
	 */
	private void initComponents() {
		// initOldDialog();
		initNewDialog();
	}
	
	/**
	 * 
	 */
	private void initNewDialog() {
		double border = 2;
		double[][] size = { { border, TableLayoutConstants.FILL, border }, // Columns
				{ border, TableLayoutConstants.FILL, border } }; // Rows
		this.setLayout(new TableLayout(size));
		this.add(new JLabel("ToDo"), "1,1");
		this.revalidate();
	}
	
	/**
	 * Constructs a <code>PatternTab</code> and sets the title.
	 */
	public TabVisualisationControl() {
		super();
		this.title = "Node Drawing";
		initComponents();
	}
	
	public void postAttributeAdded(AttributeEvent e) {
	}
	
	public void postAttributeChanged(AttributeEvent e) {
	}
	
	public void postAttributeRemoved(AttributeEvent e) {
	}
	
	public void preAttributeAdded(AttributeEvent e) {
	}
	
	public void preAttributeChanged(AttributeEvent e) {
	}
	
	public void preAttributeRemoved(AttributeEvent e) {
	}
	
	public void transactionFinished(TransactionEvent e) {
	}
	
	public void transactionStarted(TransactionEvent e) {
	}
	
	public void sessionChanged(Session s) {
	}
	
	public void sessionDataChanged(Session s) {
	}
	
	@Override
	public boolean visibleForView(View v) {
		return v != null;
	}
	
}
