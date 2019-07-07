package org.vanted.addons.MultilevelFramework;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.Main;

/**
 * Class enabling the starting of VANTED with the Multilevel Framework
 */
public class StartVantedWithMultilevelFramework {

	/**
	 * Starts Vanted with the Multilevel Framework.
	 * 
	 * @param args usually empty
	 */
	public static void main(String[] args) {
		System.out.println("Starting VANTED with Multilevel Add-On.");
		Main.startVantedExt(args, new String[] { "MultilevelFramework.xml" });
	}
}