package test.benchmark;

import java.util.ArrayList;
import java.util.List;

import org.graffiti.graph.Graph;
import org.vanted.addons.MultilevelFramework.AlgorithmWithParameters;

/**
 * Benchmark suite that tests performance scaling for sierpisky triangles.
 */
public class BenchmarkSierpinskyTriangles extends AlgorithmBenchmarkSuite {

	private static final int START_DEPTH = 6;
	private static final int GENERATE_UP_TO_DEPTH = 6;

	@Override
	protected String getBenchmarkSuiteName() {
		return "Sierpisky Triangle Benchmark Suite";
	}

	@Override
	protected List<AlgorithmWithParameters> createAlgorithm() {
		return MultilevelBenchmark.benchmarkAlgorithms();
	}

	@Override
	protected List<Quadtuple<Graph, String, Integer, Integer>> createGraphs() {

		GraphGeneration gen = new GraphGeneration();

		List<Quadtuple<Graph, String, Integer, Integer>> cases = new ArrayList<>();
		for (int levels = START_DEPTH; levels <= GENERATE_UP_TO_DEPTH; levels += 1) {

			Graph triangle = gen.generateSierpinskyTriangle(levels);

			// we don't want to wait forever: we use less rounds if the graph is big.
			int warmupRounds = (int) Math.ceil((20.0 * START_DEPTH) / levels);
			warmupRounds = 0;
			int rounds = (int) Math.ceil((50.0 * START_DEPTH) / levels);
			rounds = 5;

			cases.add(new Quadtuple<Graph, String, Integer, Integer>(triangle, "Sierpisky triangle (depth: " + levels
					+ ", n = " + triangle.getNumberOfNodes() + ", e = " + triangle.getNumberOfEdges() + ")",
					warmupRounds, rounds));
		}

		return cases;

	}

	public static void main(String[] args) {

		BenchmarkSierpinskyTriangles b = new BenchmarkSierpinskyTriangles();
		b.benchmark();

	}
}
