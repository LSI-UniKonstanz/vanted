package org.vanted.addons.stressminimization.benchmark;

import java.util.Arrays;

public final class Benchmarking {

	private Benchmarking() {}
	
	@FunctionalInterface
	public interface Action {
		void perform();
	}
	
	public static double benchmark(Action action, int warmupRounds, int rounds, String benchmarkName) {
		
		System.out.println("Starting Benchmark: " + benchmarkName);
		
		System.out.println("Running carbage collection.");
		// try to create comparable conditions for execution: clean up memory
		System.gc();

		System.out.println("Warmup: " + warmupRounds + " rounds.");
		// try to create comparable conditions for execution: warmup
		for (int i = 0; i < warmupRounds; i += 1) {
			System.out.println("Starting warmup round: " + i);
			action.perform();
		}
		
		System.out.println("Benchmark: " + rounds + " rounds.");
		
		long[] runtimes = new long[rounds];
		for (int i = 0; i < rounds; i += 1) {
			
			System.out.println("Starting round: " + i);
			long timeBefore = System.currentTimeMillis();
			action.perform();
			long timeAfter = System.currentTimeMillis();
			
			runtimes[i] = timeAfter - timeBefore;
			
		}
		
		// calculate metrics 
		double overallTime = 0;
		for (int i = 0; i < runtimes.length; i += 1) {
			overallTime += runtimes[i];
		}
		
		double mean = overallTime / ((double) runtimes.length);
		
		long max = Long.MIN_VALUE;
		for (int i = 0; i < runtimes.length; i += 1) {
			if (runtimes[i] > max) {
				max = runtimes[i];
			}
		}
		
		long min = Long.MAX_VALUE;
		for (int i = 0; i < runtimes.length; i += 1) {
			if (runtimes[i] < min) {
				min = runtimes[i];
			}
		}
		
		long[] sortedRuntimes = Arrays.copyOf(runtimes, runtimes.length);
		Arrays.sort(sortedRuntimes);
		int middle = sortedRuntimes.length / 2;
		double median = 0;
		if (sortedRuntimes.length%2 == 1) {
		   median = sortedRuntimes[middle];
		} else {
		   median = (sortedRuntimes[middle - 1] + sortedRuntimes[middle]) / 2.0;
		}
		
		double variance = 0.0;
		for (int i = 0; i < runtimes.length; i += 1) {
			variance += Math.pow(runtimes[i] - mean, 2);
		}
		variance /= runtimes.length;
		double standardDeviation = Math.sqrt(variance);
		

		System.out.println("Benchmarked: " + benchmarkName);
		System.out.println("OS Name: " + System.getProperty("os.name") + ", Total available Memory: " + Runtime.getRuntime().totalMemory() + ", Available CPUs: " + Runtime.getRuntime().availableProcessors());
		System.out.println("Warmup rounds: " + warmupRounds + ", Rounds: " + rounds);
		System.out.println("Results: " + Arrays.toString(runtimes));
		System.out.println("--------------------------------------------------------------------------------");
		System.out.println("Overall runtime: " + overallTime);
		System.out.println("Mean runtime: " + mean + "; standard deviation: " + standardDeviation);
		System.out.println("Min: " + min + "; Median: " + median + "; Max: " + max);
		System.out.println("===============================================================================");
		
		return mean;
		
	}
	
}
