package org.vanted.addons.stressminaddon;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.random.RandomLayouterAlgorithm;
import org.graffiti.editor.LoadSetting;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.selection.Selection;
import org.junit.Test;
import org.vanted.addons.stressminaddon.util.NullPlacer;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Run benchmarks for multi component graphs in VANTED.
 * @author Rene
 */
public class SM_Benchmark_MultiComponents {

    private final static int REPEAT_TIMES = 3;
    private final static int START_DEEP = 7;
    private final static int END_DEEP = 7;
    private final static int MIN_COMPONENT = 1;
    private final static int MAX_COMPONENT = 20;
    private final static boolean RANDOM_ON = false;
    private final static String outPath = "benchmark/java/test_graphs/MultiComponents_Graphs/output_MultiComponents/";

    /**
     * create a sierpinski graph and save it as .gml
     *
     * @param deep - deep of sierpinki
     * @param components - number of duplicates
     *
     */
    public static void sierpinskiGraph(int deep, int components) {
        StringBuilder graph = new StringBuilder().append("graph [\n").append(" Creator \"makegml\" directed 0 label \"\"\n");

        int counter = 0;
        int node = counter+1;
        for(int unit = 0; unit<components;unit++) {
            int[] startArray = {counter + 1, counter + 2, counter + 3};
            counter = counter + 3;
            for (int i = 1; i <= deep; i++) {

                int[] newArray = new int[3 * ((int) (Math.pow(3, i)))];

                for (int j = 0; j < ((int) (Math.pow(3, i))); j += 3) {

                    newArray[j * 3] = startArray[j];
                    newArray[j * 3 + 1] = counter + 1;
                    newArray[j * 3 + 2] = counter + 2;
                    newArray[j * 3 + 3] = startArray[j + 1];
                    newArray[j * 3 + 4] = counter + 1;
                    newArray[j * 3 + 5] = counter + 3;
                    newArray[j * 3 + 6] = startArray[j + 2];
                    newArray[j * 3 + 7] = counter + 2;
                    newArray[j * 3 + 8] = counter + 3;
                    counter = counter + 3;


                }
                startArray = newArray;
            }

            for (; node <= counter; node++) {
                graph.append("  node [ id ").append(node).append(" ]\n");
            }


            for (int i = 0; i < startArray.length; i += 3) {
                graph.append("edge [ source ").append(startArray[i]).append(" target ").append(startArray[i + 1]).append(" ]\n");
                graph.append("edge [ source ").append(startArray[i + 1]).append(" target ").append(startArray[i + 2]).append(" ]\n");
                graph.append("edge [ source ").append(startArray[i + 2]).append(" target ").append(startArray[i]).append(" ]\n");
            }
        }
        graph.append("]");

        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get("benchmark/java/test_graphs/MultiComponents_Graphs/sierpinski_"+deep+"_"+components+".gml"))){
            bw.write(graph.toString());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }

    /**
     *
     * @param csvString - the path for saving it
     * @param dataName - how the csv is named
     */
    public void save_csv(String csvString, String dataName){
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get("benchmark/java/csv_data/datasets/"+dataName+".csv"))) {
            bw.write(csvString);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }

    @Test
    public void benchmark() throws InterruptedException{
        if (MainFrame.getInstance() == null) {
            Thread vanted = new Thread(()->StartVantedWithAddon.main(new String [0]));
            vanted.start();
            Thread.sleep(7*1000);
        }
        boolean pivotOn = true;
        RandomLayouterAlgorithm rla = new RandomLayouterAlgorithm();
        do {
            StringBuilder csv_singleSteps = new StringBuilder("\"time_ms\",\"iterations\",\"stress\",\"line_crossings\",\"class\"\n");
            StringBuilder csv_average = new StringBuilder("\"time_ms\",\"iterations\",\"stress\",\"line_crossings\",\"class\"\n");

            for (int deep = START_DEEP; deep <= END_DEEP; deep++) {
                try {
                    for (int duplicate = MIN_COMPONENT; duplicate <= MAX_COMPONENT; duplicate++) {
                        sierpinskiGraph(deep, duplicate);
                        Graph graph = null;

                        try {
                            graph = MainFrame.getInstance().getGraph(Paths.get("benchmark/java/test_graphs/MultiComponents_Graphs/sierpinski_" + deep + "_" + duplicate + ".gml").toFile());
                            final Graph finalGraph = graph;
                            SwingUtilities.invokeAndWait(() -> {
                                MainFrame.getInstance().showGraph(finalGraph, null, LoadSetting.VIEW_CHOOSER_NEVER);
                                finalGraph.setModified(false); // prevent VANTED from asking if the user wants to save
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Thread.sleep(2 * 1000);
                        StressMinimizationLayout.debugModeDefault = true;
                        StressMinimizationLayout sml = new StressMinimizationLayout();

                        sml.attach(MainFrame.getInstance().getActiveEditorSession().getGraph(), MainFrame.getInstance().getActiveEditorSession().getSelectionModel().getActiveSelection());

                        StressMinimizationLayout.State state;


                        long averageLineCrossings = 0;
                        double averageStress = 0;
                        long averageIterations = 0;
                        long averageRunningTimeMs = 0;

                        for (int repeat = 0; repeat < REPEAT_TIMES; repeat++) {
                            System.err.printf("--- RUN %d/%d ----", repeat + 1, REPEAT_TIMES);
                            state = sml.state;
                            state.doAnimations = false;
                            state.backgroundTask = false;
                            state.moveIntoView = false;
                            state.debugOutput = false;


                            if (pivotOn) {
                                state.initialPlacer = new PivotMDS();
                                ((PivotMDS) state.initialPlacer).percentPivots = 5; // !!!!

                            } else {
                                state.initialPlacer = new NullPlacer();
                            }
                            state.positionAlgorithm = new IntuitiveIterativePositionAlgorithm();

                            if (!pivotOn) {
                                rla.attach(graph, new Selection());
                                rla.execute();
                            }
                            //long startTime = System.currentTimeMillis();

                            sml.execute();
                            long runningTimeMs = state.startTime;
                            int lineCrossings = QualityMetrics.lineCrossings(graph);
                            int iterations = state.debugIterations;
                            double stress = state.debugCumulativeStress;

                            averageRunningTimeMs += runningTimeMs;
                            averageLineCrossings += lineCrossings;
                            averageStress += stress;
                            averageIterations += iterations;
                            csv_singleSteps.append(runningTimeMs).append(",").append(iterations).append(",").append(stress)
                                    .append(",").append(lineCrossings).append(",\"sierpinski_").append(deep).append("_")
                                    .append(duplicate).append("\"\n");

                            //  save the calculated graph
                            try {
                                MainFrame.getInstance().saveGraphAs(graph, outPath +
                                        repeat + "_" + duplicate + "_" + deep + "_" + "sierpinski.gml", graph.getFileTypeDescription());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }


                        }

                        csv_average.append(averageRunningTimeMs / REPEAT_TIMES).append(",").append(averageIterations / REPEAT_TIMES)
                                .append(",").append(averageStress / REPEAT_TIMES).append(",").append(averageLineCrossings / REPEAT_TIMES)
                                .append(",\"sierpinski_").append(deep).append("_").append(duplicate).append("\"\n");
                        graph.setModified(false); // prevent VANTED from asking if the user wants to save
                        SwingUtilities.invokeAndWait(() -> {
                            MainFrame.getInstance().closeSession(MainFrame.getInstance().getActiveSession());
                        });


                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }

            if(pivotOn) {
                save_csv(csv_singleSteps.toString(), "csv_singleSteps_MultiComponents");
                save_csv(csv_average.toString(), "csv_average_MultiComponents");
            }else{
                save_csv(csv_singleSteps.toString(), "csv_singleSteps_MultiComponents_withoutPivot");
                save_csv(csv_average.toString(), "csv_average_MultiComponents_withoutPivot");

            }
            System.err.println("FINISHED!");

            if(pivotOn && RANDOM_ON){
                pivotOn = false;
            }else{
                pivotOn = true;
            }

        } while (!pivotOn);
    }
}