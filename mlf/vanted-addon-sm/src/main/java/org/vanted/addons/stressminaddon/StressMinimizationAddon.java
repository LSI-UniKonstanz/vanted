package org.vanted.addons.stressminaddon;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.addons.AddonAdapter;
import org.graffiti.plugin.algorithm.Algorithm;

/**
 * @author Hendrik Rohn
 *         Use {@link AddonAdapter} to indicate, that you are writing an addon.
 *         If an exception occurs during instantiation, a proper error message
 *         will be thrown and a standard addon icon will be used.
 */
public class StressMinimizationAddon extends AddonAdapter {

	/**
	 * This class will automatically start all implemented Algorithms, views and
	 * other extensions written in your Add-on. A code formatting template
	 * (save_action_format.xml) is available in the "make" project of the VANTED
	 * CVS.
	 */
	@Override
	protected void initializeAddon() {
		this.algorithms = new Algorithm[] {
				new StressMinimizationLayout(),
		};
	}
	
}
