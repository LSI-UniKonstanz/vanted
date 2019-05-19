package org.vanted.addons.stressminimization;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.Main;

/**
 * Helper method for starting vanted with the addon. 
 */
public class StartVantedWithAddon {

	public static void main(String[] args) {
		
		final String addonConfigFileName = "Stress-Minimization-Addon.xml";
		Main.startVanted(args, addonConfigFileName);
		
	}

}
