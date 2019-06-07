package org.vanted.addons.stressminimization.benchmark;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.graffiti.graph.Graph;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.selection.Selection;
import org.vanted.addons.stressminimization.StartVantedWithStressMinAddon;
import org.vanted.addons.stressminimization.StressMinimizationLayout;

/**
 * Benchmark suite that tests performance scaling for barabasi albert networks.
 */
public class BenchmarkBarabasiAlbertNetwork {
	
	private static final int WARMUP_ROUNDS = 20;
	private static final int ROUNDS = 50;
	
	private static final int START_SIZE = 100;
	private static final int STEP_SIZE = 100;
	private static final int END_SIZE = 1000;
	
	private List<Graph> graphs;

	StressMinimizationLayout algorithm = new StressMinimizationLayout();
	
	private void setUp() {
		
		GraphGeneration gen = new GraphGeneration();

		graphs = new ArrayList<>();
		for (int n = START_SIZE; n <= END_SIZE; n += STEP_SIZE) {
			Graph g = gen.generateBarabasiAlbertNetwork(n);
			graphs.add(g);
		}
		
	}
	
	public void benchmark() {

		System.out.println("Starting VANTED...");
		StartVantedWithStressMinAddon.main(new String[0]);
		
		System.out.println("================================================================================");
		System.out.println("Barabasi Albert Network Benchmark Suite");
		System.out.println("Setting up...");
		setUp();
		
		for (Graph g : graphs) {
			
			Benchmarking.benchmark(() -> {
				
				try {

					algorithm.attach(g, new Selection(""));
					algorithm.check();
					algorithm.setParameters( algorithm.getParameters() );
					
					algorithm.execute();
					algorithm.reset();
					
				} catch (PreconditionException e) {
					e.printStackTrace();
				}
				
			}, WARMUP_ROUNDS, ROUNDS, "Barabasi Albert Network (n = " + g.getNumberOfNodes() + "; e = " + g.getNumberOfEdges() + ")"); 
			
			
		}
		
		System.exit(0);
		
	}

	public static void main(String[] args) {
		
		BenchmarkBarabasiAlbertNetwork b = new BenchmarkBarabasiAlbertNetwork();
		b.benchmark();
		
	}
}
