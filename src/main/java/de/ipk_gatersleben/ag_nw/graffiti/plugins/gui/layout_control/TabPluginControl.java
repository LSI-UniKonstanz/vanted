/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import javax.swing.JPanel;

import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.event.AttributeEvent;
import org.graffiti.event.TransactionEvent;
import org.graffiti.plugin.inspector.InspectorTab;
import org.graffiti.plugin.view.GraphView;
import org.graffiti.plugin.view.View;
import org.graffiti.selection.SelectionEvent;
import org.graffiti.selection.SelectionListener;
import org.graffiti.session.EditorSession;
import org.graffiti.session.Session;
import org.graffiti.session.SessionListener;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.plugin_settings.PreferencesDialog;

/**
 * Represents the tab, which contains the functionality to edit the attributes
 * of the current graph object.
 * 
 * @version $Revision$
 */
public class TabPluginControl extends InspectorTab implements SelectionListener, SessionListener {

	private static final long serialVersionUID = 1L;
	/**
	 * DOCUMENT ME!
	 */
	// JComboBox pluginSelection;
	private PreferencesDialog pd;

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

		pd = new PreferencesDialog();
		GravistoService.getInstance().getMainFrame().getPluginManager().addPluginManagerListener(pd);
		JPanel newPanel = new JPanel();
		pd.initializeGUIforGivenContainer(newPanel, null, true, false, true, false, true, false, true, null, null, null,
				false);
		this.add(newPanel, "1,1");
		this.revalidate();
	}

	/**
	 * Constructs a <code>PatternTab</code> and sets the title.
	 */
	public TabPluginControl() {
		super();
		this.title = "Layout";
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
		if (isVisible() && s != null) {
			pd.updateSettingsPanel(null, s.getGraph(), ((EditorSession) s).getSelectionModel().getActiveSelection(),
					null, false);
		}
	}

	public void sessionDataChanged(Session s) {
	}

	@Override
	public void selectionChanged(SelectionEvent arg0) {
		if (isVisible() && arg0 != null && MainFrame.getInstance().getActiveSession() != null) {
			pd.updateSettingsPanel(null, MainFrame.getInstance().getActiveSession().getGraph(), arg0.getSelection(),
					null, false);
		}

	}

	@Override
	public void selectionListChanged(SelectionEvent e) {
	}

	@Override
	public boolean visibleForView(View v) {
		return v != null && v instanceof GraphView;
	}

}
