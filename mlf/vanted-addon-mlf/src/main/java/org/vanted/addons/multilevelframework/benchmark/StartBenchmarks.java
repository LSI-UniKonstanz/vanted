package org.vanted.addons.multilevelframework.benchmark;

import org.graffiti.editor.LoadSetting;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.selection.Selection;
import org.vanted.addons.multilevelframework.*;
import org.vanted.addons.multilevelframework.pse_hack.BlockingForceDirected;

import javax.swing.*;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Run some benchmarks for the MLF in VANTED.
 * @author Gordian
 */
public class StartBenchmarks {
    /**
     * How many times each benchmark will be repeated.
     */
    private final static int REPEAT_TIMES = 1;
    /**
     * File where the benchmark results are written.
     */
    private final static Path OUTPUT = Paths.get("/tmp/mlf.csv");
    /**
     * Don't execute force directed on graphs larger than this number of nodes.
     */
    private final static int FORCE_DIRECTED_MAX = 1500;
    /**
     * Run some benchmarks for the MLF in VANTED.
     * @param args
     *      Ignored.
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
        try (OutputStream os = Files.newOutputStream(OUTPUT)) {
            final PrintStream out = new PrintStream(os);
            runBenchmarks(paths, out);
        }
        vanted.stop();
    }

    /**
     * Run all benchmarks on all specified graphs.
     * @param paths
     *      The {@link Path}s to run benchmarks on.
     * @param out
     *      Where the results should be written.
     * @author Gordian
     */
    private static void runBenchmarks(Path[] paths, PrintStream out) throws Exception {
        out.print("Name");
        for (Path path : paths) {
            out.print(",");
            out.print(path.getFileName().toString());
        }
        out.println();
        final Object[][] configurations = {
                {new RandomMerger(), new RandomPlacer(), "Stress Minimization"},
                {new SolarMerger(),  new RandomPlacer(), "Stress Minimization"},
//                {new SolarMerger(),  new SolarPlacer(), "Stress Minimization"},
                {new RandomMerger(), new RandomPlacer(), new BlockingForceDirected().getName()},
                {new SolarMerger(),  new RandomPlacer(), new BlockingForceDirected().getName()},
//                {new SolarMerger(),  new SolarPlacer(), new BlockingForceDirected().getName()},
        };
        for (Object[] config : configurations) {
            out.print(((Merger)config[0]).getName());
            out.print("/");
            out.print(((Placer)config[1]).getName());
            out.print("/");
            out.print(config[2].toString());
            for (Path path : paths) {
                out.print(",");
                long runningTimeMs = 0;
                for (int i = 0; i < REPEAT_TIMES; i++) {
                    final MultilevelFrameworkLayouter mfl = new MultilevelFrameworkLayouter();
                    mfl.benchmarkMode = true;
                    final Graph graph = MainFrame.getInstance().getGraph(path.toFile());
                    if (config[2].toString().matches("Force") && graph.getNumberOfNodes() > FORCE_DIRECTED_MAX) {
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
