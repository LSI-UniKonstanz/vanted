/**
 * 
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.network;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import javax.swing.JPanel;

import org.graffiti.editor.GravistoService;
import org.graffiti.plugin.inspector.InspectorTab;
import org.graffiti.plugin.view.View;
import org.graffiti.plugins.views.defaults.GraffitiView;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.plugin_settings.PreferencesDialog;

/**
 * @author matthiak
 *
 */
public class TabNetworkAlgorithms extends InspectorTab{

	private static String NAME = "Algorithms";
	
	
	/**
	 * 
	 */
	public TabNetworkAlgorithms() {
		initComponents();
	}
	
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
		double[][] size =
		{
							{ border, TableLayoutConstants.FILL, border }, // Columns
				{ border, TableLayoutConstants.FILL, border }
		}; // Rows
		this.setLayout(new TableLayout(size));
		
		PreferencesDialog pd = new PreferencesDialog();
		GravistoService.getInstance().getMainFrame().getPluginManager().addPluginManagerListener(pd);
		JPanel newPanel = new JPanel();
		pd.initializeGUIforGivenContainer(
				newPanel, 
				null, 
				true, 
				false, 
				true, 
				false, 
				true, 
				false, 
				false, 
				null, 
				null, 
				null, 
				false);
		this.add(newPanel, "1,1");
		this.revalidate();
	}
	
	
	@Override
	public String getTitle() {
		return NAME;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public boolean visibleForView(View v) {
		return v != null && v instanceof GraffitiView;
	}

	@Override
	public String getTabParentPath() {
		return "Analysis.Graph";
	}

	@Override
	public int getPreferredTabPosition() {
		return InspectorTab.TAB_TRAILING;
	}

	
}
