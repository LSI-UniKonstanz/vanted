
package org.vanted.addons.stressminimization.visualtesting;

import org.graffiti.plugin.algorithm.Algorithm;
import org.vanted.addons.stressminimization.BackgroundExecutionAlgorithm;
import org.vanted.addons.stressminimization.StressMinimizationLayout;
import org.vanted.addons.stressminimization.visualtesting.algorithms.BarabasiAlbertNetworkGenerationAlgorithm;
import org.vanted.addons.stressminimization.visualtesting.algorithms.CompleteGraphGenerationAlgorithm;
import org.vanted.addons.stressminimization.visualtesting.algorithms.LineGraphGenerationAlgorithm;
import org.vanted.addons.stressminimization.visualtesting.algorithms.SierpinskyTriangleGenerationAlgorithm;
import org.vanted.addons.stressminimization.visualtesting.algorithms.StarGraphGenerationAlgorithm;
import org.vanted.addons.stressminimization.visualtesting.algorithms.WheelGraphGenerationAlgorithm;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.addons.AddonAdapter;

/**
 * Plugin container for visual testing of the stress minimization addon
 */
public class VisualTestingAddon extends AddonAdapter {

	@Override
	protected void initializeAddon() {
		this.algorithms = new Algorithm[] {
				new BackgroundExecutionAlgorithm(new StressMinimizationLayout()),
				new StarGraphGenerationAlgorithm(),
				new WheelGraphGenerationAlgorithm(),
				new CompleteGraphGenerationAlgorithm(),
				new LineGraphGenerationAlgorithm(),
				new SierpinskyTriangleGenerationAlgorithm(),
				new BarabasiAlbertNetworkGenerationAlgorithm(),
		};
		// other fields are initialized with empty arrays by default
	}

}
