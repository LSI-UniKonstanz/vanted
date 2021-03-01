package org.vanted.plugins.layout.stressminimization.visualtesting;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.Main;

/**
 * Helper method for starting vanted with the stress minimization addon.
 */
public class StartVantedForVisualTesting {
	
	public static void main(String[] args) {
		
		final String addonConfigFileName = "Visual-Testing-Addon.xml";
		Main.startVanted(args, addonConfigFileName);
		
	}
	
}
