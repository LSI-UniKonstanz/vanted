package org.vanted.addons.stressminimization.benchmark;

public class BenchmarkN500StandardGraphs extends BenchmarkStandardGraphs {

	@Override
	protected int getN() {
		return 500;
	}

	public static void main(String[] args) {

		BenchmarkN500StandardGraphs b = new BenchmarkN500StandardGraphs();
		b.benchmark();

	}

}
