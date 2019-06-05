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

	protected abstract int getN();
	
	private Graph starGraph;
	private Graph wheelGraph;
	private Graph completeGraph;
	private Graph lineGraph;
	private Graph naturalGraph;

	StressMinimizationLayout algorithm = new StressMinimizationLayout();
	
	private void setUp(int n) {
		
		GraphGeneration gen = new GraphGeneration();

		starGraph = gen.generateStar(n);
		wheelGraph = gen.generateWheel(n);
		completeGraph = gen.generateComplete(n);
		lineGraph = gen.generateLine(n);
		naturalGraph = gen.generateNatural(n);
		
	}
	
	public void benchmark() {

		System.out.println("Starting VANTED...");
		StartVantedWithStressMinAddon.main(new String[0]);
		
		System.out.println("Setting up...");
		int n = getN();
		setUp(n);
		
		Benchmarking.benchmark(() -> {
			
			try {

				algorithm.attach(starGraph, new Selection(""));
				algorithm.check();
				algorithm.setParameters( algorithm.getParameters() );
				
				algorithm.execute();
				algorithm.reset();
				
			} catch (PreconditionException e) {
				e.printStackTrace();
			}
			
		}, 10, 100, "star graph (n = " + n + ")"); 
		
		Benchmarking.benchmark(() -> {
			
			try {

				algorithm.attach(wheelGraph, new Selection(""));
				algorithm.check();
				algorithm.setParameters( algorithm.getParameters() );
				
				algorithm.execute();
				algorithm.reset();
				
			} catch (PreconditionException e) {
				e.printStackTrace();
			}
			
		}, 10, 100, "wheel graph (n = " + n + ")"); 

		Benchmarking.benchmark(() -> {
			
			try {

				algorithm.attach(completeGraph, new Selection(""));
				algorithm.check();
				algorithm.setParameters( algorithm.getParameters() );
				
				algorithm.execute();
				algorithm.reset();
				
			} catch (PreconditionException e) {
				e.printStackTrace();
			}
			
		}, 10, 100, "complete graph (n = " + n + ")"); 
		
		Benchmarking.benchmark(() -> {
			
			try {

				algorithm.attach(lineGraph, new Selection(""));
				algorithm.check();
				algorithm.setParameters( algorithm.getParameters() );
				
				algorithm.execute();
				algorithm.reset();
				
			} catch (PreconditionException e) {
				e.printStackTrace();
			}
			
		}, 10, 100, "line graph (n = " + n + ")"); 
		
		Benchmarking.benchmark(() -> {
			
			try {

				algorithm.attach(naturalGraph, new Selection(""));
				algorithm.check();
				algorithm.setParameters( algorithm.getParameters() );
				
				algorithm.execute();
				algorithm.reset();
				
			} catch (PreconditionException e) {
				e.printStackTrace();
			}
			
		}, 10, 100, "\"natural\" graph (n = " + n + ")"); 
		
		System.exit(0);
		
	}
	
}
