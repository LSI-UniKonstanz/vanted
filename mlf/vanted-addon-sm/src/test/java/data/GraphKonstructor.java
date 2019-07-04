package data;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class GraphKonstructor {

    public static String starGraph(int nodes, int components) {
        int i = 1;
        String graph = "graph [\n" +
                " Creator \"makegml\" directed 0 label \"\"\n";

        for (int j = 1; j <= components; j++) {
            int root = i;
            i++;
            graph = graph + "  node [ id " + root + " ]\n";

            for (; i <= j * nodes; i++) {
                graph = graph + "  node [ id " + i + " ]\n";
                graph = graph + "edge [ source " + root + " target " + i + " ]\n";
            }

        }

        graph = graph + "]";

        return graph;
    }

    public static String circleGraph(int nodes, int components) {
        int i = 1;
        String graph = "graph [\n" +
                " Creator \"makegml\" directed 0 label \"\"\n";

        for (int j = 1; j <= components; j++) {
            int root = i;
            i++;
            graph = graph + "  node [ id " + root + " ]\n";

            for (; i <= j * nodes; i++) {
                graph = graph + "  node [ id " + i + " ]\n";
                graph = graph + "edge [ source " + (i - 1) + " target " + i + " ]\n";
            }
            graph = graph + "edge [ source " + (i - 1) + " target " + root + " ]\n";

        }
        graph = graph + "]";

        return graph;
    }

    public static String pathGraph(int nodes, int components) {
        int i = 1;
        String graph = "graph [\n" +
                " Creator \"makegml\" directed 0 label \"\"\n";

        for (int j = 1; j <= components; j++) {
            int root = i;
            i++;
            graph = graph + "  node [ id " + root + " ]\n";

            for (; i <= j * nodes; i++) {
                graph = graph + "  node [ id " + i + " ]\n";
                graph = graph + "edge [ source " + (i - 1) + " target " + i + " ]\n";
            }
        }

        graph = graph + "]";

        return graph;
    }

    public static String completedGraph(int nodes, int components) {

        int i = 1;
        String graph = "graph [\n" +
                " Creator \"makegml\" directed 0 label \"\"\n";

        for (int j = 1; j <= components; j++) {
            int root = i;
            i++;
            graph = graph + "  node [ id " + root + " ]\n";

            for (; i <= j * nodes; i++) {
                graph = graph + "  node [ id " + i + " ]\n";

                for (int k = root; k < i; k++)

                    graph = graph + "edge [ source " + k + " target " + i + " ]\n";
            }
        }
        graph = graph + "]";

        return graph;
    }

    public static String sierpinskiGraph(int deep, int components) {
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

        return graph;
    }



    public static void main(String[] args) {


        PrintWriter pWriter = null;
        try {
            pWriter = new PrintWriter(new BufferedWriter(new FileWriter("testGraph1.gml")));
            pWriter.println(sierpinskiGraph(4,4));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (pWriter != null){
                pWriter.flush();
                pWriter.close();
            }
        }


    }
}
