package org.vanted.addons.stressminimization.benchmark;

import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.parameter.Parameter;
import org.vanted.addons.stressminimization.StressMinimizationLayout;

public abstract class StressMinimizationBenchmarkSuite extends AlgorithmBenchmarkSuite {

	/**
	 * Set the number of landmarks to use for benchmarking. Set to Integer.MAX_VALUE
	 * to turn of landmarking.
	 */
	protected int numberOfLandmarks = Integer.MAX_VALUE;

	@Override
	protected Algorithm createAlgorithm() {
		return new StressMinimizationLayout();
	}

	@Override
	protected Parameter[] getAlgorithmParameters(Algorithm algorithm) {
		Parameter[] parameters = algorithm.getParameters();
		for (Parameter p : parameters) {
			switch (p.getName()) {
			case "Landmarks Count":
				p.setValue(numberOfLandmarks);
			}
		}
		return parameters;
	}

}
