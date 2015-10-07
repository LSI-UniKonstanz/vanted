package org.vanted.garuda;

import org.graffiti.plugin.gui.GraffitiComponent;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_EditorPluginAdapter;

public class GarudaPlugin extends IPK_EditorPluginAdapter {

	public GarudaPlugin() {
	
//		this.algorithms = new Algorithm[]{
//				new VantedGarudaExtension()
//		};

		this.guiComponents = new GraffitiComponent[] {
				new GarudaToolbar("defaultToolbar")
		};
	}
}
