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
 * Abstract class for standard benchmarks. Number of nodes can be specified by sub class;
 */
public class BenchmarkSierpinskyTriangles {

	private static final int START_DEPTH = 3;
	private static final int GENERATE_UP_TO_DEPTH = 8;
	
	private List<Pair<Integer, Graph>> triangles;

	StressMinimizationLayout algorithm = new StressMinimizationLayout();
	
	private void setUp() {
		
		GraphGeneration gen = new GraphGeneration();

		triangles = new ArrayList<>();
		for (int levels = START_DEPTH; levels <= GENERATE_UP_TO_DEPTH; levels += 1) {
			Graph triangle = gen.generateSierpinskyTriangle(levels);
			triangles.add(new Pair<Integer, Graph>(levels, triangle));
		}
		
	}
	
	public void benchmark() {

		System.out.println("Starting VANTED...");
		StartVantedWithStressMinAddon.main(new String[0]);
		
		System.out.println("================================================================================");
		System.out.println("Sierpisky Triangle Benchmark Suite");
		System.out.println("Setting up...");
		setUp();
		
		for (Pair<Integer, Graph> levelsAndGraph : triangles) {
			int levels = levelsAndGraph.first;
			Graph triangle = levelsAndGraph.second;
			
			// we don't want to wait forever: we use less rounds if the graph is big.
			int warmupRounds = (int) Math.ceil((20.0 * START_DEPTH) / levels);
			int rounds = (int) Math.ceil((50.0 * START_DEPTH) / levels);
			

			Benchmarking.benchmark(() -> {
				
				try {

					algorithm.attach(triangle, new Selection(""));
					algorithm.check();
					algorithm.setParameters( algorithm.getParameters() );
					
					algorithm.execute();
					algorithm.reset();
					
				} catch (PreconditionException e) {
					e.printStackTrace();
				}
				
			}, warmupRounds, rounds, "Sierpisky triangle (depth: " + levels + "; n = " + triangle.getNumberOfNodes() + "; e = " + triangle.getNumberOfEdges() + ")"); 
			
			
		}
		
		System.exit(0);
		
	}

	public static void main(String[] args) {
		
		BenchmarkSierpinskyTriangles b = new BenchmarkSierpinskyTriangles();
		b.benchmark();
		
	}
}
