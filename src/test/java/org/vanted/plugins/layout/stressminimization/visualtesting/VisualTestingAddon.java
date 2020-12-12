
package org.vanted.plugins.layout.stressminimization.visualtesting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.graffiti.plugin.algorithm.Algorithm;
import org.vanted.plugins.layout.stressminimization.StressMinimizationPlugin;
import org.vanted.plugins.layout.stressminimization.visualtesting.algorithms.BarabasiAlbertNetworkGenerationAlgorithm;
import org.vanted.plugins.layout.stressminimization.visualtesting.algorithms.CompleteGraphGenerationAlgorithm;
import org.vanted.plugins.layout.stressminimization.visualtesting.algorithms.LineGraphGenerationAlgorithm;
import org.vanted.plugins.layout.stressminimization.visualtesting.algorithms.SierpinskyTriangleGenerationAlgorithm;
import org.vanted.plugins.layout.stressminimization.visualtesting.algorithms.StarGraphGenerationAlgorithm;
import org.vanted.plugins.layout.stressminimization.visualtesting.algorithms.WheelGraphGenerationAlgorithm;

/**
 * Plugin container for visual testing of the stress minimization addon
 */
public class VisualTestingAddon extends StressMinimizationPlugin {

    protected VisualTestingAddon() {
        super();

        List<Algorithm> algs = new ArrayList<>(Arrays.asList(this.algorithms));
        algs.add(new StarGraphGenerationAlgorithm());
        algs.add(new WheelGraphGenerationAlgorithm());
        algs.add(new CompleteGraphGenerationAlgorithm());
        algs.add(new LineGraphGenerationAlgorithm());
        algs.add(new SierpinskyTriangleGenerationAlgorithm());
        algs.add(new BarabasiAlbertNetworkGenerationAlgorithm());

        this.algorithms = algs.toArray(this.algorithms);

    }

}
