/*
 * This project is licensed under WTFPL (http://www.wtfpl.net/).
 */

package org.vanted.addons.stressminaddon;

import java.awt.*;

public class RunAll {

    public static void main(String[] args) throws InterruptedException {
        System.err.println("Running Sierpinski");
        new SM_Benchmark_Sierpinskirow().benchmark();
        System.err.println("Running PivotMDS");
        new SM_Benchmark_PivotMDS().benchmark();
        System.err.println("Running Weight Power");
        new SM_Benchmark_WeightPower().benchmark();
        System.err.println("Running Outlier");
        new SM_Benchmark_Outlier().benchmark();
        System.err.println("Running Multi Component");
        new SM_Benchmark_MultiComponents().benchmark();
        System.err.println("Running Big Graphs");
        new SM_Benchmark_BigGraphs().benchmark();

        System.err.println("Running Force directed (custom)");
        new SM_Benchmark_ForceDirected().benchmark();
        killAllWindows();
    }

    public static void killAllWindows() throws InterruptedException {
        for (Window window : Window.getWindows()) {
            window.setVisible(false);
            window.dispose();
        }
        Thread.sleep(2000);
    }
}
