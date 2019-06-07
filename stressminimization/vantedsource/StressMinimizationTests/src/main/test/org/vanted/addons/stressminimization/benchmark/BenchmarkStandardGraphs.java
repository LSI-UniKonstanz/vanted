package org.vanted.addons.stressminimization.benchmark;

import org.graffiti.graph.Graph;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.selection.Selection;
import org.vanted.addons.stressminimization.StartVantedWithStressMinAddon;
import org.vanted.addons.stressminimization.StressMinimizationLayout;

/**
 * Abstract class for standard benchmarks. Number of nodes can be specified by sub class;
 */
public abstract class BenchmarkStandardGraphs {

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
	
	private Graph starGraph;
	private Graph wheelGraph;
	private Graph completeGraph;
	private Graph lineGraph;
	private Graph barabasiAlbertGraph;
	private Graph wattsStrogatzGraph;

	StressMinimizationLayout algorithm = new StressMinimizationLayout();
	
	private void setUp(int n) {
		
		GraphGeneration gen = new GraphGeneration();

		starGraph = gen.generateStar(n);
		wheelGraph = gen.generateWheel(n);
		completeGraph = gen.generateComplete(n);
		lineGraph = gen.generateLine(n);
		barabasiAlbertGraph = gen.generateBarabasiAlbertNetwork(n);
		wattsStrogatzGraph = gen.generateWattsStrogatzNetwork(n);
		
	}
	
	public void benchmark() {

		int n = getN();
		
		System.out.println("Starting VANTED...");
		StartVantedWithStressMinAddon.main(new String[0]);
		
		System.out.println("==================================================");
		System.out.println("Standard Graph Benchmark Suite (n = " + n + ")");
		System.out.println("Setting up...");
		setUp(n);
		
		String starGraphName = "star graph (n = " + n + ")";
		if (benchmarkStarGraph()) {
			singleBenchmark(starGraph, starGraphName);
		} else {
			System.out.println("Ommited benchmark: " + starGraphName);
		}
		
		String wheelGraphName = "wheel graph (n = " + n + ")";
		if (benchmarkWheelGraph()) {
			singleBenchmark(wheelGraph, wheelGraphName);
		} else {
			System.out.println("Ommited benchmark: " + wheelGraphName);
		}

		String completeGraphName = "complete graph (n = " + n + ")";
		if (benchmarkCompleteGraph()) {
			singleBenchmark(completeGraph, completeGraphName);
		} else {
			System.out.println("Ommited benchmark: " + completeGraphName);
		}
		
		String lineGraphName = "line graph (n = " + n + ")";
		if (benchmarkLineGraph()) {
			singleBenchmark(lineGraph, lineGraphName);
		} else {
			System.out.println("Ommited benchmark: " + lineGraphName);
		}
		
		String barabasisAlbertGraphName = "barabasis albert graph (n = " + n + ")";
		if (benchmarkBarabasisAlbertGraph()) {
			singleBenchmark(barabasiAlbertGraph, barabasisAlbertGraphName);
		} else {
			System.out.println("Ommited benchmark: " + barabasisAlbertGraphName);
		}

		String wattsStrogatzGraphName = "watts strogatz graph (n = " + n + ")";
		if (benchmarkWattsStrogatzGraph()) {
			singleBenchmark(wattsStrogatzGraph, wattsStrogatzGraphName);
		} else {
			System.out.println("Ommited benchmark: " + wattsStrogatzGraphName);
		}
		
		System.exit(0);
		
	}
	
	protected void singleBenchmark(Graph graph, String name) {
		Benchmarking.benchmark(() -> {
			
			try {

				algorithm.attach(graph, new Selection(""));
				algorithm.check();
				algorithm.setParameters( algorithm.getParameters() );
				
				algorithm.execute();
				algorithm.reset();
				
			} catch (PreconditionException e) {
				e.printStackTrace();
			}
			
		}, WARMUP_ROUNDS, ROUNDS, name); 
	}
	
}
