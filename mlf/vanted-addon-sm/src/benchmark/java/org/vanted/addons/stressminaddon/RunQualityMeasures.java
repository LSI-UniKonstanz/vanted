package org.vanted.addons.stressminaddon;

import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;

import java.awt.*;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RunQualityMeasures {
    public static void main(String[] args) throws Exception {
        final Thread vanted = new Thread(() -> StartVantedWithAddon.main(new String[0]));
        vanted.start();
        Thread.sleep(8000);
        final Path current = Paths.get("").toAbsolutePath();
        final Path benchmarks = current.resolve("measured_graphs");
        final PathMatcher graphMatcher = FileSystems.getDefault().getPathMatcher("glob:*.gml");
        final Path[] paths = Files.list(benchmarks)
                .filter(path -> Files.isReadable(path) && Files.isRegularFile(path) && graphMatcher.matches(path.getFileName()))
                .toArray(Path[]::new);
        Arrays.sort(paths, Comparator.comparing(Path::getFileName));

        final Pattern pattern = Pattern.compile("^(.+)_\\d.gml$");

        String prevName = "";
        long prevCrossings = 0;
        double prevStress = 0;

        try (OutputStream os = Files.newOutputStream(Paths.get("ogdf_qm.csv")); final PrintStream out = new PrintStream(os)) {
            out.println("Config, Line Crossings, Stress");
            for (int i = 0, pathsLength = paths.length; i < pathsLength; i++) {
                Path path = paths[i];
                final Graph graph = MainFrame.getInstance().getGraph(path.toFile());
                final String fname = path.getFileName().toString();
                System.out.printf("[%d/%d] [%d/%d] %s ", (i+3)/3, pathsLength/3, i+1, pathsLength, fname);
                try {
                    Matcher matcher = pattern.matcher(fname);
                    matcher.matches();
                    String newName = matcher.group(1);
                    if (!newName.equals(prevName) && !prevName.isEmpty()) {
                        out.println(prevName + "," + (prevCrossings < 0 ? "too many edges" : prevCrossings / 3) + "," +
                                (prevStress < 0.0 ? "too many edges" : prevStress / 3) );
                        prevCrossings = 0;
                        prevStress = 0;
                    }
                    if (graph.getNumberOfEdges() < 50_000) {
                        prevCrossings += QualityMetrics.lineCrossings(graph);
                        System.out.print("✓");
                        prevStress += QualityMetrics.stress(graph);
                        System.out.println(" ✓");
                    } else {
                        prevCrossings = -1;
                        prevStress = -1;
                        System.out.println(newName + " - too many edges: " + graph.getNumberOfEdges());
                    }
                    prevName = newName;
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }
            out.println(prevName + "," + (prevCrossings < 0 ? "too many edges" : prevCrossings / 3) + "," +
                    (prevStress < 0.0 ? "too many edges" : prevStress / 3) );
        }
        for (Window window : Window.getWindows()) {
            window.setVisible(false);
            window.dispose();
        }
        System.err.println("Finished");
        System.exit(0);
    }
}
