package org.vanted.addons.MultilevelFramework;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.Main;

public class StartVantedWithAddon {

    public static void main(String[] args) {
        System.out.println("Starting VANTED with Add-on " + getAddonName()
                            + " for development...");
        Main.startVanted(args, getAddonName());
    }

	private static String getAddonName() {
		return "MultilevelFramework.xml";
	}
}