
package org.vanted.plugins.layout.stressminimization;

import org.graffiti.attributes.AttributeDescription;
import org.graffiti.plugin.EditorPluginAdapter;
import org.graffiti.plugin.algorithm.Algorithm;
import org.vanted.indexednodes.IndexAttribute;
import org.vanted.plugins.layout.stressminimization.parameters.LandmarkParameter;
import org.vanted.plugins.layout.stressminimization.parameters.LandmarkSliderComponent;
import org.vanted.plugins.layout.stressminimization.parameters.SliderComponent;
import org.vanted.plugins.layout.stressminimization.parameters.SliderParameter;

/**
 * Since 2.8.0 the Stress Minimization add-on is integrated into the core of
 * Vanted as Vanted plug-in.
 * 
 * @since 2.8.0
 */
public class StressMinimizationPlugin extends EditorPluginAdapter {
	
	@SuppressWarnings("unchecked")
	public StressMinimizationPlugin() {
		super();
		
		this.algorithms = new Algorithm[] { new BackgroundExecutionAlgorithm(new StressMinimizationLayout()) };
		
		valueEditComponents.put(SliderParameter.class, SliderComponent.class);
		valueEditComponents.put(LandmarkParameter.class, LandmarkSliderComponent.class);
		
		this.attributes = new Class[1];
		this.attributes[0] = IndexAttribute.class;
		
		this.attributeDescriptions = new AttributeDescription[] { new AttributeDescription(
				"StressMinimizationIndexAttribute", IndexAttribute.class, "", false, false, null), };
		
		// other fields are initialized with empty arrays by default
	}
}
