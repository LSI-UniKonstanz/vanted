package org.vanted.addons.stressminimization.benchmark;

public class BenchmarkN300StandardGraphs extends BenchmarkStandardGraphs {

	@Override
	protected int getN() {
		return 300;
	}

	public static void main(String[] args) {
		
		BenchmarkN300StandardGraphs b = new BenchmarkN300StandardGraphs();
		b.benchmark();
		
	}
	
}
