
package org.vanted.addons.stressminimization;

import org.graffiti.attributes.AttributeDescription;
import org.graffiti.plugin.algorithm.Algorithm;
import org.vanted.addons.stressminimization.parameters.LandmarkParameter;
import org.vanted.addons.stressminimization.parameters.LandmarkSliderComponent;
import org.vanted.addons.stressminimization.parameters.SliderComponent;
import org.vanted.addons.stressminimization.parameters.SliderParameter;
import org.vanted.addons.stressminimization.primitives.IndexAttribute;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.addons.AddonAdapter;

/**
 * Plugin container for the Stress-Minimization-Addon
 */
public class StressMinimizationAddon extends AddonAdapter {

	@Override
	protected void initializeAddon() {

		this.algorithms = new Algorithm[] {
				new BackgroundExecutionAlgorithm(new StressMinimizationLayout())
		};

		valueEditComponents.put(SliderParameter.class, SliderComponent.class);
		valueEditComponents.put(LandmarkParameter.class, LandmarkSliderComponent.class);

		this.attributes = new Class[1];
		this.attributes[0] = IndexAttribute.class;

		this.attributeDescriptions = new AttributeDescription[] {
				new AttributeDescription(
						"StressMinimizationIndexAttribute",
						IndexAttribute.class,
						"",
						false,
						false,
						null
						),
		};

		// valueEditComponents.put(IndexAttribute.class, null);

		// other fields are initialized with empty arrays by default
	}

}
