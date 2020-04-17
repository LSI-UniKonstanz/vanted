package org.vanted.addons.stressminimization.benchmark;

import java.util.ArrayList;
import java.util.List;

import org.graffiti.graph.Graph;

/**
 * Benchmark suite that tests performance scaling for sierpisky triangles.
 */
public class BenchmarkSierpinskyTriangles extends StressMinimizationBenchmarkSuite {

	private static final int START_DEPTH = 3;
	private static final int GENERATE_UP_TO_DEPTH = 10;

	public static void main(String[] args) {

		BenchmarkSierpinskyTriangles b = new BenchmarkSierpinskyTriangles();
		b.benchmark();

	}

	@Override
	protected String getBenchmarkSuiteName() {
		return "Sierpisky Triangle Benchmark Suite";
	}

	@Override
	protected List<Quadtuple<Graph, String, Integer, Integer>> createGraphs() {

		GraphGeneration gen = new GraphGeneration();

		List<Quadtuple<Graph, String, Integer, Integer>> cases = new ArrayList<>();
		for (int levels = START_DEPTH; levels <= GENERATE_UP_TO_DEPTH; levels += 1) {

			Graph triangle = gen.generateSierpinskyTriangle(levels);

			// we don't want to wait forever: we use less rounds if the graph is big.
			int warmupRounds = (int) Math.ceil((20.0 * START_DEPTH) / levels);
			int rounds = (int) Math.ceil((50.0 * START_DEPTH) / levels);

			cases.add(new Quadtuple<Graph, String, Integer, Integer>(
					triangle,
					"Sierpisky triangle (depth: " + levels + "; n = " + triangle.getNumberOfNodes() + "; e = " + triangle.getNumberOfEdges() + ")",
					warmupRounds,
					rounds
			));
		}

		return cases;

	}
}
