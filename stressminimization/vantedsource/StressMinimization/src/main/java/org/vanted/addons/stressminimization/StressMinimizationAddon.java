
package org.vanted.addons.stressminimization;

import org.graffiti.plugin.algorithm.Algorithm;
import org.vanted.addons.stressminimization.parameters.LandmarkParameter;
import org.vanted.addons.stressminimization.parameters.LandmarkSliderComponent;
import org.vanted.addons.stressminimization.parameters.SliderComponent;
import org.vanted.addons.stressminimization.parameters.SliderParameter;

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
		valueEditComponents.put(LandmarkParameter.class, LandmarkSliderComponent.class);

		// other fields are initialized with empty arrays by default
	}

}
