package test.benchmark;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.AttributeHelper;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.selection.Selection;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.grid.GridLayouterAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.random.RandomLayouterAlgorithm;

public final class Benchmarking {

	private Benchmarking() {}
	
	@FunctionalInterface
	public interface Action {
		void perform();
	}
	
	public static BenchmarkResult benchmark(Action action, int warmupRounds, int rounds, Graph graph, String graphName, String algorithmName) {
		String benchmarkName = graphName+" with "+algorithmName;
		System.out.println("--------------------------------------------------------------------------------");
		System.out.println("Starting Benchmark: " + benchmarkName);
		System.out.println("Warmup rounds: " + warmupRounds + ", Rounds: " + rounds);

		System.out.println("Running garbage collection.");
		// try to create comparable conditions for execution: clean up memory
		System.gc();

		System.out.println("Warmup: " + warmupRounds + " rounds.");
		// try to create comparable conditions for execution: warmup
		for (int i = 0; i < warmupRounds; i += 1) {
			action.perform();
		}

		// memory may adapt. Therefore measure after warmup
		System.out.println("OS Name: " + System.getProperty("os.name") + ", Total available Memory: " + Runtime.getRuntime().totalMemory() / (1024.0 * 1024.0) + " MiB, Available CPUs: " + Runtime.getRuntime().availableProcessors());
		System.out.println("Benchmark: " + rounds + " rounds.");
		
		long[] runtimes = new long[rounds];
		long[] crossings = new long[rounds];
		for (int i = 0; i < rounds; i += 1) {
			randomLayout(graph);
			long timeBefore = System.currentTimeMillis();
			action.perform();
			long timeAfter = System.currentTimeMillis();
			
			runtimes[i] = timeAfter - timeBefore;
			crossings[i] = countCrossings(graph);
		}
		
		BenchmarkResult result = new BenchmarkResult(graphName, algorithmName, runtimes, crossings);
		System.out.println(result.printResult());
		return result;
	}
	
	private static void randomLayout(Graph g)
	{
		//we want to use the random layouter with the randomize current nodes min/max
		//in order to have the same min/max for every run we use the grid layouter
		Algorithm grid = new GridLayouterAlgorithm();
		Parameter[] parameters = grid.getParameters();
		parameters[0].setValue(1.0);
		parameters[1].setValue(1.0);
		parameters[2].setValue(1.0);
		grid.setParameters(parameters);
		grid.attach(g, new Selection(g.getGraphElements()));
		grid.execute();
		
		Algorithm random = new RandomLayouterAlgorithm();
		parameters = random.getParameters();
		parameters[0].setValue("Randomize using current nodes' min/max");
		random.setParameters(parameters);
		random.attach(g, new Selection(g.getGraphElements()));
		random.execute();
	}
	
	
	public static long countCrossings(Graph g)
	{
		//this is the cave man method with O(e^2), where e is the number of edges
		//if this is to slow we could implement the Bentley-Ottman algorithm
		
		long count = 0;
		
		List<Edge> remainingEdges = new ArrayList<Edge>(g.getEdges());
		
		for(Edge e1:g.getEdges())
		{
			remainingEdges.remove(0);
			Node e1Source = e1.getSource();
			Node e1Target = e1.getTarget();
			Point2D e1SourcePos = AttributeHelper.getPosition(e1Source);
			Point2D e1TargetPos = AttributeHelper.getPosition(e1Target);
			for(Edge e2:remainingEdges)
			{
				Node e2Source = e2.getSource();
				Node e2Target = e2.getTarget();
				Point2D e2SourcePos = AttributeHelper.getPosition(e2Source);
				Point2D e2TargetPos = AttributeHelper.getPosition(e2Target);
				
				//intersections only count if all nodes are distinct
				if(e1Source!=e2Source && e1Source!=e2Target && e1Target!=e2Source && e1Target!=e2Target)
				{
					boolean edgesIntersect = Line2D.linesIntersect(e1SourcePos.getX(), e1SourcePos.getY(), e1TargetPos.getX(), e1TargetPos.getY(), 
							e2SourcePos.getX(), e2SourcePos.getY(), e2TargetPos.getX(), e2TargetPos.getY());
					
					if(edgesIntersect)
					{
						count++;
					}
				}
				
				
			}
		}
		return count;
	}
	
}
