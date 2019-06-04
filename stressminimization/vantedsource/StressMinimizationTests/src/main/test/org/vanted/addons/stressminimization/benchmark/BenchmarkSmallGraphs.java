package org.vanted.addons.stressminimization.benchmark;

import java.io.File;

import org.graffiti.graph.Graph;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.selection.Selection;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vanted.addons.stressminimization.StressMinimizationLayout;

public class BenchmarkSmallGraphs {

	// 10 nodes, 10 edges
	private static Graph starGraph;
	// 10 nodes, 20 edges
	private static Graph wheelGraph;
	// 20 nodes, 19 edges
	private static Graph lineGraph;
	// 50 nodes, 50 edges
	private static Graph randomSparseGraph;
	// 10 nodes, 45 edges
	private static Graph completeGraph;
	
	@BeforeClass
	public static void setUp() {
		
		File starGraphFile = new File("src/main/resources/org/vanted/addons/stressminimization/benchmark/starGraph1.gml");
		starGraph = GraphLoading.getInstance().loadGraph(starGraphFile);

		File wheelGraphFile = new File("src/main/resources/org/vanted/addons/stressminimization/benchmark/wheelGraph1.gml");
		wheelGraph = GraphLoading.getInstance().loadGraph(wheelGraphFile);

		File lineGraphFile = new File("src/main/resources/org/vanted/addons/stressminimization/benchmark/lineGraph1.gml");
		lineGraph = GraphLoading.getInstance().loadGraph(lineGraphFile);

		File randomSparseGraphFile = new File("src/main/resources/org/vanted/addons/stressminimization/benchmark/randomSparseGraph1.gml");
		randomSparseGraph = GraphLoading.getInstance().loadGraph(randomSparseGraphFile);

		File completeGraphFile = new File("src/main/resources/org/vanted/addons/stressminimization/benchmark/completeGraph1.gml");
		completeGraph = GraphLoading.getInstance().loadGraph(completeGraphFile);
	
		
	}
	
	@Test
	public void benchmark() {

		StressMinimizationLayout algorithm = new StressMinimizationLayout();
		
		Benchmarking.benchmark(() -> {
			
			try {

				algorithm.attach(starGraph, new Selection(""));
				algorithm.check();
				algorithm.setParameters( algorithm.getParameters() );
				
				algorithm.execute();
				algorithm.reset();
				
			} catch (PreconditionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}, 10, 100, "small star graph"); 
		
		Benchmarking.benchmark(() -> {
			
			try {

				algorithm.attach(wheelGraph, new Selection(""));
				algorithm.check();
				algorithm.setParameters( algorithm.getParameters() );
				
				algorithm.execute();
				algorithm.reset();
				
			} catch (PreconditionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}, 10, 100, "small wheel graph"); 
		
		Benchmarking.benchmark(() -> {
			
			try {

				algorithm.attach(lineGraph, new Selection(""));
				algorithm.check();
				algorithm.setParameters( algorithm.getParameters() );
				
				algorithm.execute();
				algorithm.reset();
				
			} catch (PreconditionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}, 10, 100, "small line graph"); 
		
		Benchmarking.benchmark(() -> {
			
			try {

				algorithm.attach(randomSparseGraph, new Selection(""));
				algorithm.check();
				algorithm.setParameters( algorithm.getParameters() );
				
				algorithm.execute();
				algorithm.reset();
				
			} catch (PreconditionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}, 10, 100, "small random sparse graph"); 
		
		Benchmarking.benchmark(() -> {
			
			try {

				algorithm.attach(completeGraph, new Selection(""));
				algorithm.check();
				algorithm.setParameters( algorithm.getParameters() );
				
				algorithm.execute();
				algorithm.reset();
				
			} catch (PreconditionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}, 10, 100, "small star graph"); 
		
	}
	
}
