package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.r;

import org.graffiti.plugin.EditorPluginAdapter;
import org.graffiti.plugin.inspector.InspectorTab;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.addons.AddonAdapter;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.r.TabRControl;

public class RPlugin extends EditorPluginAdapter{

	/**
	 * 
	 */
	public RPlugin() {
	
		this.tabs = new InspectorTab[] { new TabRControl() };
	}
}
