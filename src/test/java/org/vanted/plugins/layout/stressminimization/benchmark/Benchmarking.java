package org.vanted.plugins.layout.stressminimization.benchmark;

import java.util.Arrays;

public final class Benchmarking {
	
	private Benchmarking() {
	}
	
	/**
	 * Executes a benchmark of the first argument. The benchmark is executed rounds times with
	 * warmupRounds executions before time measurement. The prepare action is performed before each
	 * execution but execution time is not measured.
	 *
	 * @param action
	 *           The action whose execution time shawl be measured
	 * @param prepare
	 *           A preperation action. Executed before each execution of action. Use this
	 *           action to reset any data / layout used by action.
	 * @param warmupRounds
	 *           number of executions of action before time measurements
	 * @param rounds
	 *           number of executions for which execution time will be measured
	 * @param benchmarkName
	 *           The name of the benchmark
	 * @return The median execution time.
	 */
	public static double benchmark(Action action, Action prepare, int warmupRounds, int rounds, String benchmarkName) {
		
		System.out.println("--------------------------------------------------------------------------------");
		System.out.println("Starting Benchmark: " + benchmarkName);
		System.out.println("Warmup rounds: " + warmupRounds + ", Rounds: " + rounds);
		
		System.out.println("Running carbage collection.");
		// try to create comparable conditions for execution: clean up memory
		System.gc();
		
		System.out.println("Warmup: " + warmupRounds + " rounds.");
		// try to create comparable conditions for execution: warmup
		for (int i = 0; i < warmupRounds; i += 1) {
			prepare.perform();
			action.perform();
		}
		
		// memory may adapt. Therefore measure after warmup
		System.out.println("OS Name: " + System.getProperty("os.name") + ", Total available Memory: " + Runtime.getRuntime().totalMemory() / (1024.0 * 1024.0)
				+ " MiB, Available CPUs: " + Runtime.getRuntime().availableProcessors());
		System.out.println("Benchmark: " + rounds + " rounds.");
		
		long[] runtimes = new long[rounds];
		for (int i = 0; i < rounds; i += 1) {
			
			prepare.perform();
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
		
		double mean = overallTime / (runtimes.length);
		
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
		if (sortedRuntimes.length % 2 == 1) {
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
		
		System.out.println("--------------------------------------------------------------------------------");
		System.out.println("Benchmarked: " + benchmarkName);
		System.out.println("Results: " + Arrays.toString(runtimes));
		System.out.println("Overall runtime: " + overallTime + "ms");
		System.out.println("Mean runtime: " + mean + "ms; standard deviation: " + standardDeviation + "ms");
		System.out.println("Min: " + min + "ms; Median: " + median + "ms; Max: " + max + "ms");
		System.out.println("--------------------------------------------------------------------------------");
		
		return median;
		
	}
	
	@FunctionalInterface
	public interface Action {
		void perform();
	}
	
}
