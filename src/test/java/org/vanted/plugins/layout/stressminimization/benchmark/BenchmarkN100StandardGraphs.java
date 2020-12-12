package org.vanted.plugins.layout.stressminimization.benchmark;

public class BenchmarkN100StandardGraphs extends BenchmarkStandardGraphs {

	public static void main(String[] args) {

		BenchmarkN100StandardGraphs b = new BenchmarkN100StandardGraphs();
		b.benchmark();

	}

	@Override
	protected int getN() {
		return 100;
	}

}
