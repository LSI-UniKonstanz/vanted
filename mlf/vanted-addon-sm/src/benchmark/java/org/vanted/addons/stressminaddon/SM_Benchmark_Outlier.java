package org.vanted.addons.stressminaddon;

import org.graffiti.editor.LoadSetting;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.junit.Test;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;

public class SM_Benchmark_Outlier {

    private final static int REPEAT_TIMES = 1;

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
        Thread vanted = new Thread(() -> StartVantedWithAddon.main(new String[0]));
        vanted.start();
        Thread.sleep(15 * 1000);
        System.out.println(Paths.get("benchmark/java/test_graphs/OutlierGraphs").toAbsolutePath());
        Graph graph = null;
        String csv_singleStepsBigGraphs = "\"time_ms\",\"iterations\",\"stress\",\"line_crossings\",\"class\"\n";
        String csv_averageBigGraphs = "\"time_ms\",\"iterations\",\"stress\",\"line_crossings\",\"class\"\n";
        String[] graphen = {"sierpinski_06.gml","sierpinski_07.gml","add20.gml"};

        for(int graphIndex = 0; graphIndex < graphen.length;graphIndex++) {
            try {
                graph = MainFrame.getInstance().getGraph(Paths.get("benchmark/java/test_graphs/OutlierGraphs/" + graphen[graphIndex]).toFile());
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
                state.initialPlacer = new PivotMDS();
                //state.maxIterations = 0;

                long startTime = System.currentTimeMillis();
                sml.execute();
                System.err.println("---REPITITION----");
                long endTime = System.currentTimeMillis();
                long thisTimeMs = endTime - startTime;
                int lineCrossing = QualityMeasures.lineCrossings(graph);
                int iterations = 0;
                long stress = 0;
                runningTimeMs += thisTimeMs;
                runningLineCrossing += lineCrossing;
                runningStress += stress;
                runningIteration += iterations;
                csv_singleStepsBigGraphs = csv_singleStepsBigGraphs + thisTimeMs + "," + iterations + "," + stress + "," + lineCrossing + ",\""+graphen[graphIndex]+"\"\n";

            }

            csv_averageBigGraphs = csv_averageBigGraphs + runningTimeMs / REPEAT_TIMES + "," + runningIteration / REPEAT_TIMES + "," + runningStress / REPEAT_TIMES + "," + runningLineCrossing / REPEAT_TIMES + ",\""+graphen[graphIndex]+"\"\n";
            graph.setModified(false); // prevent VANTED from asking if the user wants to save
            SwingUtilities.invokeAndWait(() -> {
                MainFrame.getInstance().closeSession(MainFrame.getInstance().getActiveSession());
            });
        }
        save_csv(csv_singleStepsBigGraphs, "csv_singleStepsOutlier");
        save_csv(csv_averageBigGraphs, "csv_averageOutlier");



}


}
