package org.vanted.addons.stressminimization.benchmark;

public class BenchmarkN300StandardGraphs extends BenchmarkStandardGraphs {

	@Override
	protected int getN() {
		return 300;
	}

	@Override
	protected boolean benchmarkLineGraph() {
		// layouting a 300 nodes line graph currently takes a veeeeeery long time 
		return false;
	}
	
	public static void main(String[] args) {
		
		BenchmarkN300StandardGraphs b = new BenchmarkN300StandardGraphs();
		b.benchmark();
		
	}
	
}
