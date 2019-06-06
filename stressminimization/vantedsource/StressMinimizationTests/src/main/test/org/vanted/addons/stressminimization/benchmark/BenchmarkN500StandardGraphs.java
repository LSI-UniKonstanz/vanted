package org.vanted.addons.stressminimization.benchmark;

public class BenchmarkN500StandardGraphs extends BenchmarkStandardGraphs {

	@Override
	protected int getN() {
		return 500;
	}

	
	@Override
	protected boolean benchmarkLineGraph() {
		// calculating a layout for a 500 line takes 
		// too much time for a quick benchmark 
		return false;
	}
	
	public static void main(String[] args) {
		
		BenchmarkN500StandardGraphs b = new BenchmarkN500StandardGraphs();
		b.benchmark();
		
	}
	
}
