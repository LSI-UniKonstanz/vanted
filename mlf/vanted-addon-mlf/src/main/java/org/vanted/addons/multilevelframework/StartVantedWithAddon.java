package org.vanted.addons.multilevelframework;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.Main;

public class StartVantedWithAddon {

    public static void main(String[] args) {
        String addOnName = "MLF-Add-On.xml";
        System.out.println("Starting VANTED with Add-on " + addOnName + " for development...");
        Main.startVanted(args, addOnName);
    }
}
