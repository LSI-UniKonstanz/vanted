package test.benchmark;

public class BenchmarkN100StandardGraphs extends BenchmarkStandardGraphs {

	@Override
	protected int getN() {
		return 100;
	}

	public static void main(String[] args) {
		
		BenchmarkN100StandardGraphs b = new BenchmarkN100StandardGraphs();
		b.benchmark();
		
	}
	
}
