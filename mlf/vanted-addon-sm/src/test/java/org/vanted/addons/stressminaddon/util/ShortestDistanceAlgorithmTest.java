package org.vanted.addons.stressminaddon.util;


import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.junit.Test;
import org.vanted.addons.stressminaddon.StressMinimizationLayout;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

import static data.TestGraphs.*;
import static org.junit.Assert.*;

/**
 * Test class {@link ShortestDistanceAlgorithm}.
 * @author theo, Jannik
 */
public class ShortestDistanceAlgorithmTest {

    /**
     * Test static method {@link ShortestDistanceAlgorithm#calculateShortestPaths(List, int, boolean)}
     * @author Jannik, theo
     */
    @Test
    public void getShortestPaths() {

        // test accuracy
        NodeValueMatrix result = ShortestDistanceAlgorithm.calculateShortestPaths(GRAPH_1_NODES, Integer.MAX_VALUE, true);

        //compares all cells
        for(int row = 0; row < GRAPH_1_NODES.size(); row++) {
            for (int col = 0; col < row + 1; col++) {
                assertEquals("Graph 1:", GRAPH_1_DISTANCES.get(row,col), result.get(row, col), 0.001);
            }
        }

        // test unconnected components
        long timeMultiThread = System.currentTimeMillis();
        result = ShortestDistanceAlgorithm.calculateShortestPaths(GRAPH_2_NODES, Integer.MAX_VALUE, true);
        timeMultiThread = System.currentTimeMillis() - timeMultiThread;

        //compares all cells
        for(int row = 0; row < GRAPH_2_NODES.size(); row++) {
            for (int col = 0; col < row + 1; col++) {
                assertEquals("Graph 2 (unconnected):", GRAPH_2_DISTANCES.get(row,col), result.get(row, col), 0.001);
            }
        }

        // test without multi threading
        long timeNoMultiThread = System.currentTimeMillis();
        // test accuracy
        result = ShortestDistanceAlgorithm.calculateShortestPaths(GRAPH_2_NODES, Integer.MAX_VALUE, false);
        timeNoMultiThread = System.currentTimeMillis() - timeNoMultiThread;

        //compares all cells
        for(int row = 0; row < GRAPH_2_NODES.size(); row++) {
            for (int col = 0; col < row + 1; col++) {
                assertEquals("Graph 2 (no multi threading):", GRAPH_2_DISTANCES.get(row,col), result.get(row, col), 0.001);
            }
        }

        System.out.println("ShortestDistanceAlgorithm: Threading: " + timeMultiThread + "   No Threading: " + timeNoMultiThread);

        // test with max depth
        result = ShortestDistanceAlgorithm.calculateShortestPaths(GRAPH_1_NODES, 1, true);

        //compares all cells
        for(int row = 0; row < GRAPH_1_NODES.size(); row++) {
            for (int col = 0; col < row + 1; col++) {
                final int expectedDist = (int) GRAPH_1_DISTANCES.get(row, col);
                if (expectedDist != 2) {
                    assertEquals("Graph 1 (max depth):", expectedDist, result.get(row, col), 0.001);
                } else {
                    assertEquals("Graph 1 (max depth):", Double.POSITIVE_INFINITY, result.get(row, col), 0.001);
                }
            }
        }

        // test with outside nodes
        Graph g = (Graph) GRAPH_1.copy();
        List<Node> nodes = g.getNodes();
        nodes.get(nodes.size()-1).removeAttribute(StressMinimizationLayout.INDEX_ATTRIBUTE);
        result = ShortestDistanceAlgorithm.calculateShortestPaths(nodes.subList(0, nodes.size()-1), Integer.MAX_VALUE, true);

        //compares all cells
        for(int row = 0; row < GRAPH_1_NODES.size()-1; row++) {
            for (int col = 0; col < row + 1; col++) {
                assertEquals("Graph 1 (sub graph):", GRAPH_1_DISTANCES.get(row, col), result.get(row, col), 0.001);
            }
        }
    }

    /**
     * Test possible thrown exceptions.
     * @author Jannik
     */
    @Test
    public void exceptions() throws InterruptedException {

        PrintStream originalErr = System.err;
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));

        List<Node> nodes = new LinkedList<>(GRAPH_2_NODES);
        // create a lot of nodes
        for (int count = 0; count < 10; count++) {
            nodes.addAll(nodes);
        }

        Thread t = new Thread(() -> ShortestDistanceAlgorithm.calculateShortestPaths(
                nodes, Integer.MAX_VALUE, true));
        t.start();
        Thread.sleep(10);
        t.interrupt(); // should throw an InterruptedException
        t.join();

        System.setErr(originalErr);

        // should be non-empty
        assertFalse("Exception printed", errContent.toString().trim().isEmpty());

    }
}