package org.vanted.addons.stressminaddon;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.random.RandomLayouterAlgorithm;
import org.AttributeHelper;
import org.graffiti.editor.LoadSetting;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.selection.Selection;
import org.vanted.addons.multilevelframework.*;
import org.vanted.addons.multilevelframework.pse_hack.BlockingForceDirected;
import org.vanted.addons.stressminaddon.StartVantedWithAddon;
import org.vanted.addons.stressminaddon.util.NullPlacer;

import javax.swing.*;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Run some benchmarks for the MLF in VANTED.
 * The quality measures will be computed from the gml files.
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
    private final static Path OUTPUT = Paths.get("sm.csv");
    /**
     * Benchmark configurations. Structure: InitialPlacer, IterativePositionAlgorithm
     */
    private final static Object[][] configurations = {
            {new PivotMDS(), new IntuitiveIterativePositionAlgorithm()},
            /*{new NullPlacer(), new IntuitiveIterativePositionAlgorithm()},
            {new RandomLayouterAlgorithm(), new IntuitiveIterativePositionAlgorithm()},*/
    };

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
        try (OutputStream os = Files.newOutputStream(OUTPUT); final PrintStream out = new PrintStream(os)) {
            runBenchmarks(paths, out);
        }
        System.err.println("Finished");
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
        for (Object[] config : configurations) {
            if (config[0] instanceof InitialPlacer) {
                out.print(((InitialPlacer)config[0]).getName() + " Fixed");
            } else {
                out.print(((Algorithm)config[0]).getName());
            }
            out.print("/");
            out.print(((IterativePositionAlgorithm)config[1]).getName());
            for (Path path : paths) {
                out.print(",");
                long runningTimeMs = 0;
                for (int i = 0; i < REPEAT_TIMES; i++) {
                    final StressMinimizationLayout sml = new StressMinimizationLayout();
                    final Graph graph = MainFrame.getInstance().getGraph(path.toFile());
                    graph.getNodes().forEach(n -> AttributeHelper.setPosition(n, 0, 0));
                    SwingUtilities.invokeAndWait(() -> {
                        MainFrame.getInstance().showGraph(graph, null, LoadSetting.VIEW_CHOOSER_NEVER);
                    });
                    sml.setParameters(sml.getParameters());
                    sml.attach(graph, new Selection());
                    final StressMinimizationLayout.State state = sml.state;
                    if (!(config[0] instanceof InitialPlacer)) {
                        Algorithm algorithm = (Algorithm) config[0];
                        algorithm.setParameters(algorithm.getParameters());
                        algorithm.attach(graph, new Selection());
                        algorithm.execute();
                        state.initialPlacer = new NullPlacer();
                    } else {
                        state.initialPlacer = ((InitialPlacer) config[0]);
                        PivotMDS p = (PivotMDS) state.initialPlacer;
                        p.percentPivots = 0.0;
                    }
                    state.positionAlgorithm = ((IterativePositionAlgorithm) config[1]);
                    state.intermediateUndoable = false;
                    state.doAnimations = false;
                    state.moveIntoView = false;
                    state.backgroundTask = false;

                    long startTime = System.currentTimeMillis();
                    sml.execute();
                    long endTime = System.currentTimeMillis();
                    runningTimeMs += endTime - startTime;
                    // store graph
                    String fileName = (config[0] == null ? "Random" : config[0].getClass().getSimpleName() ) + "_"
                            + config[1].getClass().getSimpleName() + path.getFileName()
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
