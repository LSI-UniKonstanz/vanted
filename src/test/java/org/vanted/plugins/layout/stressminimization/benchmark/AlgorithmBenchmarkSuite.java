package org.vanted.plugins.layout.stressminimization.benchmark;

import java.util.List;

import org.graffiti.graph.Graph;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.selection.Selection;
import org.vanted.plugins.layout.stressminimization.StartVantedWithStressMinAddon;

/**
 * Abstract superclass that provides the basic infrastructure for testing an algorithm
 */
public abstract class AlgorithmBenchmarkSuite {
	
	private Algorithm algorithm;
	private List<Quadtuple<Graph, String, Integer, Integer>> graphs;
	
	/**
	 * Sets up the benchmark suite. super.setUp needs to be executed in subclasses.
	 */
	protected void setUp() {
		algorithm = createAlgorithm();
		graphs = createGraphs();
	}
	
	/**
	 * Runs the benchmarks of this test suite
	 */
	protected void benchmark() {
		
		System.out.println("Starting VANTED...");
		StartVantedWithStressMinAddon.main(new String[0]);
		
		System.out.println("================================================================================");
		System.out.println(getBenchmarkSuiteName());
		System.out.println("Setting up...");
		setUp();
		
		for (Quadtuple<Graph, String, Integer, Integer> tuple : graphs) {
			
			Graph graph = tuple.first;
			String name = tuple.second;
			int warmupRounds = tuple.third;
			int rounds = tuple.fourth;
			
			singleBenchmark(graph, name, warmupRounds, rounds);
			
		}
		
		System.exit(0);
		
	}
	
	/**
	 * Creates an instance of the algorithm that shawl be benchmarked in this test suite
	 *
	 * @return An instance of the algorithm for this test suite.
	 */
	protected abstract Algorithm createAlgorithm();
	
	/**
	 * Returns parameters for one execution of the algorithm. This method is executed during each
	 * single benchmark. You should call algorithm.getParameters() in your implementation if you
	 * decide to override this method.
	 *
	 * @return An array of parameters for algorithm execution
	 */
	protected Parameter[] getAlgorithmParameters(Algorithm algorithm) {
		return algorithm.getParameters();
	}
	
	/**
	 * Creates the graphs that shawl be benchmarked.
	 *
	 * @return A list of graphs, names, warmup rounds and rounds for each graph.
	 */
	protected abstract List<Quadtuple<Graph, String, Integer, Integer>> createGraphs();
	
	/**
	 * Returns the name of this benchmark suite.
	 *
	 * @return
	 */
	protected abstract String getBenchmarkSuiteName();
	
	/**
	 * Executes a single benchmark round with the graph. Override to change the benchmarking
	 * procedure.
	 *
	 * @param graph
	 *           The graph that shawl be benchmarked
	 * @param name
	 *           The name of the benchmark that will be executed
	 * @param warmupRounds
	 *           Number of warumup rounds for benchmark
	 * @param rounds
	 *           Number of rounds for benchmark
	 */
	protected void singleBenchmark(Graph graph, String name, int warmupRounds, int rounds) {
		Benchmarking.benchmark(() -> {
			
			try {
				
				algorithm.attach(graph, new Selection(""));
				algorithm.check();
				algorithm.setParameters(getAlgorithmParameters(algorithm));
				
				algorithm.execute();
				
			} catch (PreconditionException e) {
				e.printStackTrace();
			}
			
		}, () -> {
			algorithm.reset();
			// reset node positions
			GraphGeneration.positionNodesRandom(graph);
		}, warmupRounds, rounds, name);
		
	}
	
}
