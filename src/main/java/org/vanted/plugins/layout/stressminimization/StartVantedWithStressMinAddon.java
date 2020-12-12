package org.vanted.plugins.layout.stressminimization;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.Main;

/**
 * Helper method for starting vanted with the stress minimization addon. 
 */
public class StartVantedWithStressMinAddon {

	public static void main(String[] args) {
		
		//final String addonConfigFileName = "Stress-Minimization-Addon.xml";
		Main.startVanted(args, null); // add-on is now part of the core
		
	}

}