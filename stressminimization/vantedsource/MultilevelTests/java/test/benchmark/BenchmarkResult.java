package test.benchmark;

import java.util.Arrays;

public class BenchmarkResult {
	public String graphName;
	public String algorithmName;
	public long[] runtimes;
	public long[] crossings;
	
	public BenchmarkResult(String graphName, String algorithmName, long[] runtimes, long[] crossings)
	{
		this.graphName = graphName;
		this.algorithmName = algorithmName;
		this.runtimes = runtimes;
		this.crossings = crossings;
	}
	
	public String benchmarkName()
	{
		return graphName+" with "+algorithmName;
	}
	
	public String printResult()
	{
		return "--------------------------------------------------------------------------------"+"\n"
		+"Benchmarked: " + benchmarkName()+"\n"
		+"Runtimes: " + Arrays.toString(runtimes)+"\n"
		+"Overall runtime: " + overall(runtimes) + "ms"+"\n"
		+"Mean runtime: " + mean(runtimes) + "ms; standard deviation: " + standardDeviation(runtimes) + "ms"+"\n"
		+"Min: " + min(runtimes) + "ms; Median: " + median(runtimes) + "ms; Max: " + max(runtimes) + "ms"+"\n"
		+"Crossings: " + Arrays.toString(crossings)+"\n"
		+"Overall crossings: " + overall(crossings) +"\n"
		+"Mean crossings: " + mean(crossings) + "; standard deviation: " + standardDeviation(crossings) +"\n"
		+"Min: " + min(crossings) + "; Median: " + median(crossings) + "; Max: " + max(crossings) +"\n"
		+"--------------------------------------------------------------------------------";
	}
	
	public double overall(long[] input)
	{
		return Arrays.stream(input).sum();
	}
	public Double mean(long[] input)
	{
		return Arrays.stream(input).average().getAsDouble();
	}
	public double median(long[] input)
	{
		long[] sorted = Arrays.copyOf(input, input.length);
		Arrays.sort(sorted);
		int middle = sorted.length / 2;
		double median = 0;
		if (sorted.length%2 == 1) {
		   median = sorted[middle];
		} else {
		   median = (sorted[middle - 1] + sorted[middle]) / 2.0;
		}
		return median;
	}
	public double standardDeviation(long[] input)
	{
		double variance = 0.0;
		for (int i = 0; i < input.length; i += 1) {
			variance += Math.pow(input[i] - mean(input), 2);
		}
		variance /= input.length;
		return Math.sqrt(variance);
	}
	public Long min(long[] input)
	{
		return Arrays.stream(input).min().getAsLong();
	}
	public Long max(long[] input)
	{
		return Arrays.stream(input).max().getAsLong();
	}

}
