package test.benchmark;

import java.util.Arrays;
import java.util.List;

import javax.swing.JPanel;

import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.parameter.Parameter;
import org.vanted.addons.MultilevelFramework.AlgorithmWithParameters;
import org.vanted.addons.MultilevelFramework.ForceDirectedWrapper;
import org.vanted.addons.MultilevelFramework.MultilevelLayoutAlgorithm;
import org.vanted.addons.MultilevelFramework.Coarsening.MatchingCoarseningAlgorithm;
import org.vanted.addons.MultilevelFramework.Coarsening.RandomNeighborCoarseningAlgorithm;
import org.vanted.addons.MultilevelFramework.Coarsening.SolarMergerCoarsening;
import org.vanted.addons.MultilevelFramework.Placement.SolarPlacement;
import org.vanted.addons.MultilevelFramework.Placement.ZeroPlacementAlgorithm;
import org.vanted.addons.stressminimization.BackgroundExecutionAlgorithm;
import org.vanted.addons.stressminimization.StressMinimizationLayout;

public final class MultilevelBenchmark {
	public static List<AlgorithmWithParameters> benchmarkAlgorithms() {
		return Arrays.asList(forceDirected(), stressMin(-2, false), stressMin(-4, false), stressMin(-4, true),
				multilevel(randomNeighborCoarsening(), zeroPlacement(), forceDirected()),
				multilevel(randomNeighborCoarsening(), zeroPlacement(), forceDirected(), false),
				multilevel(matchingCoarsening(), zeroPlacement(), forceDirected()),
				multilevel(solarCoarsening(), solarPlacement(), forceDirected()),
				multilevel(randomNeighborCoarsening(), zeroPlacement(), stressMin(-2, false)),
				multilevel(randomNeighborCoarsening(), zeroPlacement(), stressMin(-4, false)),
				multilevel(matchingCoarsening(), zeroPlacement(), stressMin(-2, false)),
				multilevel(matchingCoarsening(), zeroPlacement(), stressMin(-4, false)),
				multilevel(solarCoarsening(), solarPlacement(), stressMin(-2, false)),
				multilevel(solarCoarsening(), solarPlacement(), stressMin(-4, false)));
	}

	public static AlgorithmWithParameters multilevel(AlgorithmWithParameters merger, AlgorithmWithParameters placer,
			AlgorithmWithParameters layouter) {
		return multilevel(merger, placer, layouter, true);
	}

	public static AlgorithmWithParameters multilevel(AlgorithmWithParameters merger, AlgorithmWithParameters placer,
			AlgorithmWithParameters layouter, boolean scaling) {
		MultilevelLayoutAlgorithm algorithm = new MultilevelLayoutAlgorithm();
		Parameter[] parameters = algorithm.getParameters();
		for (Parameter p : parameters) {
			switch (p.getName()) {
			case MultilevelLayoutAlgorithm.CHOOSEMERGERNAME:
				p.setValue(merger);
				break;
			case MultilevelLayoutAlgorithm.CHOOSEPLACERNAME:
				p.setValue(placer);
				break;
			case MultilevelLayoutAlgorithm.CHOOSELAYOUTERNAME:
				p.setValue(layouter);
				break;
			case MultilevelLayoutAlgorithm.SCALELEVELSNAME:
				p.setValue(scaling);
			}
		}
		if (scaling) {
			return new AlgorithmWithParameters(algorithm, parameters,
					"Multilevel(" + merger.name + ", " + placer.name + ", " + layouter.name + ") with scaling");
		}
		return new AlgorithmWithParameters(algorithm, parameters,
				"Multilevel(" + merger.name + ", " + placer.name + ", " + layouter.name + ")");
	}

	// Mergers

	public static AlgorithmWithParameters randomNeighborCoarsening() {
		Algorithm a = new RandomNeighborCoarseningAlgorithm();
		return new AlgorithmWithParameters(a, a.getParameters());
	}

	public static AlgorithmWithParameters matchingCoarsening() {
		Algorithm a = new MatchingCoarseningAlgorithm();
		return new AlgorithmWithParameters(a, a.getParameters());
	}

	public static AlgorithmWithParameters solarCoarsening() {
		Algorithm a = new SolarMergerCoarsening();
		return new AlgorithmWithParameters(a, a.getParameters());
	}

	// Placers

	public static AlgorithmWithParameters zeroPlacement() {
		Algorithm a = new ZeroPlacementAlgorithm();
		return new AlgorithmWithParameters(a, a.getParameters());
	}

	public static AlgorithmWithParameters solarPlacement() {
		Algorithm a = new SolarPlacement();
		return new AlgorithmWithParameters(a, a.getParameters());
	}

	// Layouters

	public static AlgorithmWithParameters forceDirected() {
		Algorithm a = new ForceDirectedWrapper();
		return new AlgorithmWithParameters(a, a.getParameters());
	}

	public static AlgorithmWithParameters stressMin(double threshold, boolean landmarkPreprocessing) {
		BackgroundExecutionAlgorithm a = new BackgroundExecutionAlgorithm(new StressMinimizationLayout());
		Parameter[] parameters = a.getParameters();
		for (Parameter p : parameters) {
			switch (p.getName()) {
			case "Landmarks Count":
				p.setValue(1000000); // disable landmark
				break;
			case "Stress Change Threshold":
				p.setValue(threshold);
				break;
			case "Disable Landmark Preprocessing":
				p.setValue(!landmarkPreprocessing); // disable landmark even more
				break;
			case "Iteration Maximum":
				p.setValue(10000); // disable this threshold
				break;
			}
		}
		a.setControlInterface(new ThreadSafeOptions(), new JPanel());
		String name = "StressMin with threshold = 10E" + threshold;
		if (landmarkPreprocessing) {
			name = name.concat(" with landmark preprocessing");
		}
		return new AlgorithmWithParameters(a, parameters, name);
	}

}