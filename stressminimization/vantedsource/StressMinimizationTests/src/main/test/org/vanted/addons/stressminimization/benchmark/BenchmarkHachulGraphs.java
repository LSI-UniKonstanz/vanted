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
 * Test suite for graphs in Graphs/Hachul.
 * This test suite is currently not functional, 
 * since the graphs can not be loaded.
 */
public class BenchmarkHachulGraphs {

	// no serious benchmark with these values...
	private static final int WARMUP_ROUNDS = 0;
	private static final int ROUNDS = 1;
	
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
				System.out.println("Loading: " + names[i]);
				Graph graph = MainFrame.getInstance().getGraph(new File(resourcesDirectory + names[i]));
				graphs.add(new Pair<>(names[i], graph));
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
	}
	
	public static void main(String[] args) {
		BenchmarkHachulGraphs b = new BenchmarkHachulGraphs();
		b.benchmark();
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
