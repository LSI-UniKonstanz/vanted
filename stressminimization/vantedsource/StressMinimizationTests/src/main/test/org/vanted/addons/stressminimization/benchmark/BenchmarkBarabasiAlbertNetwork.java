package org.vanted.addons.stressminimization.benchmark;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.graffiti.graph.Graph;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.selection.Selection;
import org.vanted.addons.stressminimization.StartVantedWithStressMinAddon;
import org.vanted.addons.stressminimization.StressMinimizationLayout;

/**
 * Benchmark suite that tests performance scaling for barabasi albert networks.
 */
public class BenchmarkBarabasiAlbertNetwork extends AlgorithmBenchmarkSuite {

	private static final int WARMUP_ROUNDS = 20;
	private static final int ROUNDS = 50;
	
	private static final int START_SIZE = 100;
	private static final int STEP_SIZE = 100;
	private static final int END_SIZE = 1000;

	@Override
	protected String getBenchmarkSuiteName() {
		return "Barabasi Albert Network Benchmark Suite";
	}
	
	@Override
	protected Algorithm createAlgorithm() {
		return new StressMinimizationLayout();
	}

	@Override
	protected List<Quadtuple<Graph, String, Integer, Integer>> createGraphs() {

		GraphGeneration gen = new GraphGeneration();

		List<Quadtuple<Graph, String, Integer, Integer>> cases = new ArrayList<>();
		for (int n = START_SIZE; n <= END_SIZE; n += STEP_SIZE) {
			Graph g = gen.generateBarabasiAlbertNetwork(n);
			cases.add(new Quadtuple<Graph, String, Integer, Integer>(
					g, 
					"Barabasi Albert Network (n = " + g.getNumberOfNodes() + "; e = " + g.getNumberOfEdges() + ")", 
					WARMUP_ROUNDS, 
					ROUNDS
			));
		}
		
		return cases;
		
	}

	public static void main(String[] args) {
		
		BenchmarkBarabasiAlbertNetwork b = new BenchmarkBarabasiAlbertNetwork();
		b.benchmark();
		
	}

}
