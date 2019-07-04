package org.vanted.addons.stressminaddon;

import org.graffiti.editor.LoadSetting;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.junit.Test;
import org.vanted.addons.stressminaddon.util.NullPlacer;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.Collection;

/**
 * Benchamark that tests different deep class off sierpinski triangle.
 * Save the data in a csv-format(runtime_ms,linecrossing,iterations,stress)
 *
 * @author rene
 */

public class SM_Benchmark {

    private final static int REPEAT_TIMES = 1;
    private final static int START_DEEP = 4;
    private final static int END_DEEP = 4;
    private final static int MIN_COMPONENT = 1;
    private final static int MAX_COMPONENT = 1;
    /**
     * create a sierpinski graph and save it as .gml
     *
     * @param deep - deep of sierpinki
     * @param components - number of duplicates
     *
     */
    public static void sierpinskiGraph(int deep, int components) {
        String graph = "graph [\n" +
                " Creator \"makegml\" directed 0 label \"\"\n";

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

                graph = graph + "  node [ id " + node + " ]\n";

            }


            for (int i = 0; i < startArray.length; i += 3) {
                graph = graph + "edge [ source " + startArray[i] + " target " + startArray[i + 1] + " ]\n";
                graph = graph + "edge [ source " + startArray[i + 1] + " target " + startArray[i + 2] + " ]\n";
                graph = graph + "edge [ source " + startArray[i + 2] + " target " + startArray[i] + " ]\n";
            }
        }
        graph =graph +"]";

        PrintWriter pWriter = null;
        try {
            pWriter = new PrintWriter(new BufferedWriter(new FileWriter("benchmark/java/test_graphs/sierpinski_"+deep+"_"+components+".gml")));
            pWriter.println(graph);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (pWriter != null){
                pWriter.flush();
                pWriter.close();
            }
        }

    }

    public void save_csv(String csvString, String dataName){
        PrintWriter pWriter = null;
        try {
            pWriter = new PrintWriter(new BufferedWriter(new FileWriter("benchmark/java/csv_data/datasets/"+dataName+".csv")));
            pWriter.println(csvString);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (pWriter != null){
                pWriter.flush();
                pWriter.close();
            }
        }

    }

    @Test
    public void benchmark() throws InterruptedException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException, IllegalAccessException {
        Thread vanted = new Thread(()->StartVantedWithAddon.main(new String [0]));
        vanted.start();
        Thread.sleep(15*1000);
        boolean pivotOn = true;
        do {
            String csv_singleSteps = "\"time_ms\",\"iterations\",\"stress\",\"line_crossings\",\"class\"\n";
            String csv_average = "\"time_ms\",\"iterations\",\"stress\",\"line_crossings\",\"class\"\n";

            for (int deep = START_DEEP; deep <= END_DEEP; deep++) {
                for (int duplicate = MIN_COMPONENT; duplicate <= MAX_COMPONENT; duplicate++) {
                    sierpinskiGraph(deep, duplicate);
                    Thread.sleep(15 * 1000);
                    System.out.println(Paths.get("benchmark/java/test_graphs/sierpinski_" + deep + "_" + duplicate + ".gml").toAbsolutePath());
                    Graph graph = null;

                    try {
                        graph = MainFrame.getInstance().getGraph(Paths.get("benchmark/java/test_graphs/sierpinski_" + deep + "_" + duplicate + ".gml").toFile());
                        final Graph finalGraph = graph;
                        SwingUtilities.invokeAndWait(() -> {
                            MainFrame.getInstance().showGraph(finalGraph, null, LoadSetting.VIEW_CHOOSER_NEVER);
                            finalGraph.setModified(false); // prevent VANTED from asking if the user wants to save
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Thread.sleep(3 * 1000);
                    StressMinimizationLayout sml = new StressMinimizationLayout();

                    sml.attach(MainFrame.getInstance().getActiveEditorSession().getGraph(), MainFrame.getInstance().getActiveEditorSession().getSelectionModel().getActiveSelection());

                    StressMinimizationLayout.State state;



                    long runningLineCrossing = 0;
                    long runningStress = 0;
                    long runningIteration = 0;
                    long runningTimeMs = 0;
                    for (int repeat = 0; repeat < REPEAT_TIMES; repeat++) {
                        state = sml.state;
                        state.doAnimations = false;
                        state.backgroundTask = false;
                        state.moveIntoView = false;
                        if (pivotOn) {
                            state.initialPlacer = new PivotMDS();
                        } else {
                            state.initialPlacer = new NullPlacer();
                        }
                        state.positionAlgorithm = new IntuitiveIterativePositionAlgorithm();

                        long startTime = System.currentTimeMillis();
                        sml.execute();
                        System.err.println("---REPITITION----");
                        long endTime = System.currentTimeMillis();
                        long thisTimeMs = endTime - startTime;
                        int lineCrossing = QualityMeasures.lineCrossing(graph);
                        int iterations = 0;
                        long stress = 0;
                        runningTimeMs += thisTimeMs;
                        runningLineCrossing += lineCrossing;
                        runningStress += stress;
                        runningIteration += iterations;
                        csv_singleSteps = csv_singleSteps + thisTimeMs + "," + iterations + "," + stress + "," + lineCrossing + ",\"sierpinski_" + deep + "_" + duplicate + "\"\n";


                    }

                    graph.setFileTypeDescription("benchmark/java/test_graphs/sierpinski.gml");
                    csv_average = csv_average + runningTimeMs / REPEAT_TIMES + "," + runningIteration / REPEAT_TIMES + "," + runningStress / REPEAT_TIMES + "," + runningLineCrossing / REPEAT_TIMES + ",\"sierpinski_" + deep + "_" + duplicate + "\"\n";
                    graph.setModified(false); // prevent VANTED from asking if the user wants to save
                    SwingUtilities.invokeAndWait(() -> {
                        MainFrame.getInstance().closeSession(MainFrame.getInstance().getActiveSession());
                    });


                }
            }
            if(pivotOn) {
                save_csv(csv_singleSteps, "csv_singleSteps");
                save_csv(csv_average, "csv_average");
            }else{
                save_csv(csv_singleSteps, "csv_singleSteps_withoutPivot");
                save_csv(csv_average, "csv_average_withoutPivot");

            }
            System.err.println("FINISHED!");


            Thread.sleep(8 * 1000);
        }while (!pivotOn);
    }
}