package org.vanted.plugins.layout.multilevelframework;

import org.AttributeHelper;
import org.graffiti.editor.LoadSetting;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.selection.Selection;

import javax.swing.*;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Run some benchmarks for the MLF in VANTED.
 * The quality measures will be computed from the gml files.
 * 
 * @author Gordian
 */
public class StartBenchmarks {
	/**
	 * How many times each benchmark will be repeated.
	 */
	private final static int REPEAT_TIMES = 3;
	/**
	 * File where the benchmark results (running times) are written.
	 */
	private final static Path OUTPUT = Paths.get("mlf.csv");
	/**
	 * Don't execute force directed on graphs larger than this number of nodes.
	 */
	private final static int FORCE_DIRECTED_MAX = 4000;
	/**
	 * Benchmark configurations. Structure: Merger, Placer, Name of Algorithm
	 */
	private final static Object[][] configurations = {
			{ new RandomMerger(), new RandomPlacer(), "Null-Layout" },
			{ new SolarMerger(), new RandomPlacer(), "Null-Layout" },
			{ new SolarMerger(), new SolarPlacer(), "Null-Layout" },
			{ new RandomMerger(), new RandomPlacer(), "Stress Minimization" },
			{ new SolarMerger(), new RandomPlacer(), "Stress Minimization" },
			{ new SolarMerger(), new SolarPlacer(), "Stress Minimization" }
	};
	
	/**
	 * Run some benchmarks for the MLF in VANTED.
	 * 
	 * @param args
	 *           Ignored.
	 * @author Gordian
	 */
	public static void main(String[] args) throws Exception {
		final Thread vanted = new Thread(() -> StartVantedWithAddon.main(new String[0]));
		vanted.start();
		Thread.sleep(8000);
		final Path current = Paths.get("").toAbsolutePath();
		final Path benchmarks = current.resolve("benchmark_graphs");
		final PathMatcher graphMatcher = FileSystems.getDefault().getPathMatcher("glob:*.gml");
		final Path[] paths = Files.list(benchmarks)
				.filter(path -> Files.isReadable(path) && Files.isRegularFile(path) && graphMatcher.matches(path.getFileName()))
				.toArray(Path[]::new);
		Arrays.sort(paths, Comparator.comparing(Path::getFileName));
		try (OutputStream os = Files.newOutputStream(OUTPUT); final PrintStream out = new PrintStream(os)) {
			runBenchmarks(paths, out);
		}
		vanted.stop();
	}
	
	/**
	 * Run all benchmarks on all specified graphs.
	 * 
	 * @param paths
	 *           The {@link Path}s to run benchmarks on.
	 * @param out
	 *           Where the results should be written.
	 * @author Gordian
	 */
	private static void runBenchmarks(Path[] paths, PrintStream out) throws Exception {
		out.print("Name");
		for (Path path : paths) {
			out.print(",");
			out.print(path.getFileName().toString());
		}
		out.println();
		for (Object[] config : configurations) {
			out.print(((Merger) config[0]).getName());
			out.print("/");
			out.print(((Placer) config[1]).getName());
			out.print("/");
			out.print(config[2].toString());
			for (Path path : paths) {
				out.print(",");
				long runningTimeMs = 0;
				for (int i = 0; i < REPEAT_TIMES; i++) {
					final MultilevelFrameworkLayout mfl = new MultilevelFrameworkLayout();
					mfl.benchmarkMode = true;
					final Graph graph = MainFrame.getInstance().getGraph(path.toFile());
					graph.getNodes().forEach(n -> AttributeHelper.setPosition(n, 0, 0));
					if (config[2].toString().matches("Force.*") && graph.getNumberOfNodes() > FORCE_DIRECTED_MAX) {
						runningTimeMs = Long.MAX_VALUE;
						break;
					}
					SwingUtilities.invokeAndWait(() -> {
						MainFrame.getInstance().showGraph(graph, null, LoadSetting.VIEW_CHOOSER_NEVER);
					});
					mfl.setParameters(mfl.getParameters());
					mfl.attach(graph, new Selection());
					mfl.nonInteractiveMerger = (Merger) config[0];
					mfl.nonInteractivePlacer = (Placer) config[1];
					mfl.nonInteractiveAlgorithm = config[2].toString();
					long startTime = System.currentTimeMillis();
					mfl.execute();
					long endTime = System.currentTimeMillis();
					runningTimeMs += endTime - startTime;
					// store graph
					String fileName = config[0].getClass().getSimpleName() + "_"
							+ config[1].getClass().getSimpleName() + "_"
							+ config[2] + "_" + path.getFileName()
							+ (path.getFileName().toString().endsWith(".gml") ? "" : ".gml");
					fileName = fileName.replace(".gml", "_" + i + ".gml");
					MainFrame.getInstance().saveGraphAs(graph, fileName, graph.getFileTypeDescription());
					
					graph.setModified(false); // prevent VANTED from asking if the user wants to save
					SwingUtilities.invokeAndWait(() -> {
						MainFrame.getInstance().closeSession(MainFrame.getInstance().getActiveSession());
					});
				}
				out.print(runningTimeMs / REPEAT_TIMES);
			}
			out.println();
		}
	}
}
