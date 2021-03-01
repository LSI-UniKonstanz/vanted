/**
 * 
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.biomodels;

import org.graffiti.plugin.EditorPluginAdapter;
import org.graffiti.plugin.inspector.InspectorTab;

/**
 * @author matthiak
 */
public class BiomodelsDBAccessPlugin extends EditorPluginAdapter {
	
	/**
	 * 
	 */
	public BiomodelsDBAccessPlugin() {
		
		tabs = new InspectorTab[] { new TabBiomodels() };
	}
	
}
