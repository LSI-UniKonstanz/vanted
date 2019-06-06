package org.vanted.addons.stressminimization.benchmark;

import java.io.File;
import java.util.List;

import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.selection.Selection;
import org.vanted.addons.stressminimization.StartVantedWithStressMinAddon;
import org.vanted.addons.stressminimization.StressMinimizationLayout;

/**
 * Abstract class for standard benchmarks. Number of nodes can be specified by sub class;
 */
public abstract class BenchmarkHachulGraphs {

	protected static final int WARMUP_ROUNDS = 3;
	protected static final int ROUNDS = 3;
	
	private List<Pair<String, Graph>> graphs;
	
	StressMinimizationLayout algorithm = new StressMinimizationLayout();
	
	private void setUp() {
		
		String resourcesDirectory = "src/main/resources/";
		// file names of graphs to benchmark
		String[] names = new String[] {
				"3elt.gml", 
				"4elt.gml", 
				"add20.gml",
				"add32.gml",
				"crack.gml",
				"cs4.gml",
				// sierpinsky 8 is benchmarked in other suite
		};
		
		for (int i = 0; i < names.length; i += 1) {
			
			try {
				Graph graph = MainFrame.getInstance().getGraph(new File(resourcesDirectory + names[i]));
				graphs.add(new Pair<>(names[i], graph));
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
	}
	
	public void benchmark() {

		System.out.println("Starting VANTED...");
		StartVantedWithStressMinAddon.main(new String[0]);
		
		System.out.println("==================================================");
		System.out.println("Hachul Graphs Benchmark Suite");
		System.out.println("Setting up...");
		setUp();
		
		for (Pair<String, Graph> pair : graphs) {
			String name = pair.first;
			Graph graph = pair.second;
			
			singleBenchmark(graph, name);
			
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
