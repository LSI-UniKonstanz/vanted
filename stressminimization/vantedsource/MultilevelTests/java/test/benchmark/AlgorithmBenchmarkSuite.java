package test.benchmark;

import java.util.List;

import org.graffiti.graph.Graph;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.selection.Selection;
import org.vanted.addons.MultilevelFramework.AlgorithmWithParameters;
import org.vanted.addons.stressminimization.StartVantedWithStressMinAddon;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;

/**
 * Abstract superclass that provides the basic infrastructure
 * for testing an algorithm
 */
public abstract class AlgorithmBenchmarkSuite {

	private List<AlgorithmWithParameters> algorithms;
	private List<Quadtuple<Graph, String, Integer, Integer>> graphs;
	
	private static char RESULTDELIMITER = ';';
	
	/**
	 * Sets up the benchmark suite.
	 * super.setUp needs to be executed in subclasses.
	 */
	protected void setUp() {
		algorithms = createAlgorithm();
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
		
		BenchmarkResult[][] results = new BenchmarkResult[graphs.size()][algorithms.size()];
		
		
		int i = 0;
		for (Quadtuple<Graph, String, Integer, Integer> tuple : graphs) {
			
			Graph graph = tuple.first;
			String graphName = tuple.second;
			int warmupRounds = tuple.third;
			int rounds = tuple.fourth;
			
			GraphHelper.diplayGraph(graph);
			
			int j=0;
			for(AlgorithmWithParameters a:algorithms)
			{
				
				results[i][j] = singleBenchmark(a, graph, graphName, a.name, warmupRounds, rounds);
				j++;
			}
			i++;
		}
		
		System.out.println("---------------------------");
		System.out.println("---------------------------");
		System.out.println("RESULT OVERVIEW");
		System.out.println("---------------------------");
		
		for(i=0; i<graphs.size(); i++)
		{
			for(int j=0; j<algorithms.size(); j++)
			{
				System.out.println(results[i][j].printResult());
			}
		}
		
		//runtimes table
		
		for(int j=0; j<algorithms.size(); j++)
		{
			System.out.print(RESULTDELIMITER);
			System.out.print(algorithms.get(j).name);
		}
		System.out.print("\n");
		for(i=0; i<graphs.size(); i++)
		{
			System.out.print(graphs.get(i).second);
			for(int j=0; j<algorithms.size(); j++)
			{
				System.out.print(RESULTDELIMITER);
				System.out.print(results[i][j].mean(results[i][j].runtimes));
			}
			System.out.print("\n");
		}
		
		//crossings-table
		
		for(int j=0; j<algorithms.size(); j++)
		{
			System.out.print(RESULTDELIMITER);
			System.out.print(algorithms.get(j).name);
		}
		System.out.print("\n");
		for(i=0; i<graphs.size(); i++)
		{
			System.out.print(graphs.get(i).second);
			for(int j=0; j<algorithms.size(); j++)
			{
				System.out.print(RESULTDELIMITER);
				System.out.print(results[i][j].mean(results[i][j].crossings));
			}
			System.out.print("\n");
		}
		
		System.exit(0);
		
	}
	
	/**
	 * Creates an instance of the algorithm that shawl be benchmarked 
	 * in this test suite
	 * @return An instance of the algorithm for this test suite.
	 */
	protected abstract List<AlgorithmWithParameters> createAlgorithm();
	
	/**
	 * Creates the graphs that shawl be benchmarked.
	 * @return A list of graphs, names, warmup rounds and rounds for each graph.
	 */
	protected abstract List<Quadtuple<Graph, String, Integer, Integer>> createGraphs();
	
	/**
	 * Returns the name of this benchmark suite.
	 * @return
	 */
	protected abstract String getBenchmarkSuiteName();
	
	/**
	 * Executes a single benchmark round with the graph.
	 * Override to change the benchmarking procedure.
	 * @param graph The graph that shawl be benchmarked
	 * @param name The name of the benchmark that will be executed
	 * @param warmupRounds Number of warumup rounds for benchmark
	 * @param rounds Number of rounds for benchmark
	 */
	protected BenchmarkResult singleBenchmark(AlgorithmWithParameters a, Graph graph, String graphName, String algorithmName, int warmupRounds, int rounds) {
		return Benchmarking.benchmark(() -> {
			
			try {
				a.algorithm.attach(graph, new Selection(""));
				a.algorithm.check();
				a.algorithm.setParameters( a.parameters );
				
				a.algorithm.execute();
				a.algorithm.reset();
				
				
			} catch (PreconditionException e) {
				e.printStackTrace();
			}
			
		}, warmupRounds, rounds, graph, graphName, algorithmName); 
	}
	
}
