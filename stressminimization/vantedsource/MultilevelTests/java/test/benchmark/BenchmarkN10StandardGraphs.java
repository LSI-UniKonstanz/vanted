package test.benchmark;

public class BenchmarkN10StandardGraphs extends BenchmarkStandardGraphs {

	@Override
	protected int getN() {
		return 10;
	}

	public static void main(String[] args) {
		
		BenchmarkN10StandardGraphs b = new BenchmarkN10StandardGraphs();
		b.benchmark();
		
	}
	
}
