
package org.vanted.addons.stressminimization;

import org.graffiti.plugin.algorithm.Algorithm;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.addons.AddonAdapter;

/**
 * Plugin container for the Stress-Minimization-Addon
 * @author David Boetius
 */
public class StressMinimizationAddon extends AddonAdapter {

	@Override
	protected void initializeAddon() {

		this.algorithms = new Algorithm[] {
				new BackgroundExecutionAlgorithm(new StressMinimizationLayout())
		};

		valueEditComponents.put(SliderParameter.class, SliderComponent.class);

		// other fields are initialized with empty arrays by default
	}

}
