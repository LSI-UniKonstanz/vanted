package org.vanted.addons.stressminimization.benchmark;

public class BenchmarkN50StandardGraphs extends BenchmarkStandardGraphs {

	@Override
	protected int getN() {
		return 50;
	}

	public static void main(String[] args) {
		
		BenchmarkN50StandardGraphs b = new BenchmarkN50StandardGraphs();
		b.benchmark();
		
	}
	
}
