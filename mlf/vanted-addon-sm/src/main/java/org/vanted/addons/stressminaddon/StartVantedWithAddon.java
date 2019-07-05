package org.vanted.addons.stressminaddon;


import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.Main;

import java.util.Arrays;

/**
 * A class used for Debugging. VANTED can be started from this class with the addon already loaded.
 */
public class StartVantedWithAddon {

    /**
     * Start VANTED with the addon loaded. <code>--debug</code> can be passed as first argument
     * to enable debug mode. Every other argument will be forwarded to VANTED.
     */
    public static void main(String[] args) {
        System.out.println("Starting VANTED with Add-on " + getAddonName()
                + " for development...");
        StressMinimizationLayout.debugModeDefault = args.length > 0 && args[0].equals("--debug"); // enable debug mode
        String[] arguments = args;
        if (StressMinimizationLayout.debugModeDefault) {
            arguments = Arrays.copyOfRange(args, 1, args.length);
        }
        Main.startVanted(arguments, getAddonName());
    }

    /**
     * @return
     *      the name of the addon xml file.
     */
    public static String getAddonName() {
        return "Add-on-Stress-Minimization.xml";
    }

}
