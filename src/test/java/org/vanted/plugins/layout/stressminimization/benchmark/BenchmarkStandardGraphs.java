package org.vanted.plugins.layout.stressminimization.benchmark;

import java.util.ArrayList;
import java.util.List;

import org.graffiti.graph.Graph;

/**
 * Abstract class for standard benchmarks. Number of nodes can be specified by sub class;
 */
public abstract class BenchmarkStandardGraphs extends StressMinimizationBenchmarkSuite {

	protected static final int WARMUP_ROUNDS = 20;
	protected static final int ROUNDS = 50;

	protected abstract int getN();

	protected boolean benchmarkStarGraph() {
		return true;
	}

	protected boolean benchmarkWheelGraph() {
		return true;
	}

	protected boolean benchmarkCompleteGraph() {
		return true;
	}

	protected boolean benchmarkLineGraph() {
		return true;
	}

	protected boolean benchmarkBarabasisAlbertGraph() {
		return true;
	}

	protected boolean benchmarkWattsStrogatzGraph() {
		return true;
	}

	@Override
	protected String getBenchmarkSuiteName() {
		return "Standard graphs benchmark suite: (n = " + getN() + ")";
	}

	@Override
	protected List<Quadtuple<Graph, String, Integer, Integer>> createGraphs() {

		int n = getN();

		GraphGeneration gen = new GraphGeneration();

		List<Quadtuple<Graph, String, Integer, Integer>> cases = new ArrayList<>();

		if (benchmarkStarGraph()) {

			Graph starGraph = gen.generateStar(n);

			cases.add(new Quadtuple<Graph, String, Integer, Integer>(
					starGraph,
					"star graph (n = " + n + ")",
					WARMUP_ROUNDS,
					ROUNDS
			));

		} else {
			System.out.println("Ommitting benchmark: star graph");
		}

		if (benchmarkWheelGraph()) {

			Graph wheelGraph = gen.generateWheel(n);

			cases.add(new Quadtuple<Graph, String, Integer, Integer>(
					wheelGraph,
					"wheel graph (n = " + n + ")",
					WARMUP_ROUNDS,
					ROUNDS
			));

		} else {
			System.out.println("Ommitting benchmark: wheel graph");
		}

		if (benchmarkCompleteGraph()) {

			Graph completeGraph = gen.generateComplete(n);

			cases.add(new Quadtuple<Graph, String, Integer, Integer>(
					completeGraph,
					"complete graph (n = " + n + ")",
					WARMUP_ROUNDS,
					ROUNDS
			));

		} else {
			System.out.println("Ommitting benchmark: complete graph");
		}

		if (benchmarkLineGraph()) {

			Graph lineGraph = gen.generateLine(n);

			cases.add(new Quadtuple<Graph, String, Integer, Integer>(
					lineGraph,
					"line graph (n = " + n + ")",
					WARMUP_ROUNDS,
					ROUNDS
			));

		} else {
			System.out.println("Ommitting benchmark: line graph");
		}

		if (benchmarkBarabasisAlbertGraph()) {

			Graph barabasiAlbertGraph = gen.generateBarabasiAlbertNetwork(n);

			cases.add(new Quadtuple<Graph, String, Integer, Integer>(
					barabasiAlbertGraph,
					"barabasis albert graph (n = " + n + ")",
					WARMUP_ROUNDS,
					ROUNDS
			));

		} else {
			System.out.println("Ommitting benchmark: barabasis albert graph");
		}

		if (benchmarkWattsStrogatzGraph()) {

			Graph wattsStrogatzGraph = gen.generateWattsStrogatzNetwork(n);

			cases.add(new Quadtuple<Graph, String, Integer, Integer>(
					wattsStrogatzGraph,
					"watts strogatz graph (n = " + n + ")",
					WARMUP_ROUNDS,
					ROUNDS
			));

		} else {
			System.out.println("Ommitting benchmark: watts strogatz graph");
		}

		return cases;

	}

}
