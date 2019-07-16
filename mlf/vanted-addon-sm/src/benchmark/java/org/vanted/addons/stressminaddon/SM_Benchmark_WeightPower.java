package org.vanted.addons.stressminaddon;

import org.graffiti.editor.LoadSetting;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.junit.Test;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Run benchmarks for WeightPower in VANTED.
 * @author Rene
 */

public class SM_Benchmark_WeightPower {

    /**
     *Sets the parameter for the test
     */
    private final static int REPEAT_TIMES = 5;
    private final static String getPath = "benchmark/java/test_graphs/delta_Graphs/";
    private final static String outPath = "benchmark/java/test_graphs/delta_Graphs/output_delta/";
    private final static String csv_singleName = "csv_singleSteps_delta_Graphs";
    private final static String csv_averageName = "csv_average_delta_Graphs";



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
        System.out.println(Paths.get(getPath).toAbsolutePath());
        Graph graph = null;

        /**
         * output String as csv-format with parameter
         */
        StringBuilder csv_singleSteps =
                new StringBuilder("\"time_ms\",\"iterations\",\"stress\",\"line_crossings\",\"class\"\n");
        StringBuilder csv_average =
                new StringBuilder("\"time_ms\",\"iterations\",\"stress\",\"line_crossings\",\"class\"\n");

        String[] graphen = {"sierpinski_04.gml","3elt.gml","sierpinski_07.gml","sierpinski_06.gml"};

        /**
         * the loop that iterate trough all the given graphs
         */
        for(int graphIndex = 0; graphIndex <graphen.length ;graphIndex++) {
            try {

                //get the graph and load it in Vanted
                try {
                    graph = MainFrame.getInstance().getGraph(
                            Paths.get(getPath + graphen[graphIndex]).toFile());
                    final Graph finalGraph = graph;

                    //set the selectView off
                    SwingUtilities.invokeAndWait(() -> {
                        MainFrame.getInstance().showGraph(finalGraph, null, LoadSetting.VIEW_CHOOSER_NEVER);
                        finalGraph.setModified(false); // prevent VANTED from asking if the user wants to save
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Thread.sleep(2 * 1000);

                //create Object StressMinimizationLayout and connect it with the working Vanted
                StressMinimizationLayout.debugModeDefault = true;
                StressMinimizationLayout sml = new StressMinimizationLayout();
                sml.attach(MainFrame.getInstance().getActiveEditorSession().getGraph(),
                        MainFrame.getInstance().getActiveEditorSession().getSelectionModel().getActiveSelection());

                //create state for getting and setting the parameters
                StressMinimizationLayout.State state;
                for (int deltaIterator = -3; deltaIterator <= 1; deltaIterator++) {

                    long averageLineCrossings = 0;
                    double averageStress = 0;
                    long averageIterations = 0;
                    long averageRunningTimeMs = 0;

                    //loop for the amount of repeats with the same graph
                    for (int repeat = 0; repeat < REPEAT_TIMES; repeat++) {
                        System.err.printf("--- RUN %d/%d ----", repeat + 1, REPEAT_TIMES);
                        state = sml.state;
                        state.doAnimations = false;
                        state.backgroundTask = false;
                        state.moveIntoView = false;
                        state.debugOutput = false;
                        //state.maxIterations = 0;
                        state.weightPower = deltaIterator;

                        state.initialPlacer = new PivotMDS();
                        ((PivotMDS) state.initialPlacer).percentPivots = 5;


                        //starts the algorithm
                        sml.execute();

                        long runningTimeMs = state.startTime;
                        int lineCrossings = QualityMetrics.lineCrossings(graph);
                        int iterations = state.debugIterations;
                        double stress = state.debugCumulativeStress;

                        //for the average values
                        averageRunningTimeMs += runningTimeMs;
                        averageLineCrossings += lineCrossings;
                        averageStress += stress;
                        averageIterations += iterations;

                        //fill the csv-string with the values that got calculated (single step)
                        csv_singleSteps.append(runningTimeMs).append(",").append(iterations).append(",").append(stress)
                                .append(",").append(lineCrossings).append(",\"").append(deltaIterator).append(graphen[graphIndex]).append("\"\n");

                        //  save the calculated graph
                        try {
                            MainFrame.getInstance().saveGraphAs(graph, outPath +
                                    repeat + "_" + deltaIterator + "_" + graphen[graphIndex], graph.getFileTypeDescription());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }


                    //fill the csv-string with the values that got calculated (average step)
                    csv_average.append(averageRunningTimeMs / REPEAT_TIMES).append(",").append(averageIterations / REPEAT_TIMES)
                            .append(",").append(averageStress / REPEAT_TIMES).append(",").append(averageLineCrossings / REPEAT_TIMES)
                            .append(",\"").append(deltaIterator).append(graphen[graphIndex]).append("\"\n");

                }
                // prevent VANTED from asking if the user wants to save
                graph.setModified(false);
                SwingUtilities.invokeAndWait(() -> {
                    MainFrame.getInstance().closeSession(MainFrame.getInstance().getActiveSession());
                });
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        save_csv(csv_singleSteps.toString(), csv_singleName);
        save_csv(csv_average.toString(), csv_averageName);


    }

}
