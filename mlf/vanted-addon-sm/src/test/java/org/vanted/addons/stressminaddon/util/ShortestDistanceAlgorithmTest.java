package org.vanted.addons.stressminaddon.util;


import org.junit.Test;

import static data.TestGraphs.*;

import static org.junit.Assert.*;

public class ShortestDistanceAlgorithmTest {

    @Test
    public void getShortestPaths() {

        // test accuracy
        NodeValueMatrix result = ShortestDistanceAlgorithm.calculateShortestPaths(GRAPH_1_NODES, Integer.MAX_VALUE);

        //compares all cells
        for(int row = 0; row < GRAPH_1_NODES.size(); row++) {
            for (int col = 0; col < row + 1; col++) {
                assertEquals("Graph 1:", GRAPH_1_DISTANCES.get(row,col), result.get(row, col), 0.001);
            }
        }

        // test unconnected components
        result = ShortestDistanceAlgorithm.calculateShortestPaths(GRAPH_2_NODES, Integer.MAX_VALUE);

        //compares all cells
        for(int row = 0; row < GRAPH_2_NODES.size(); row++) {
            for (int col = 0; col < row + 1; col++) {
                assertEquals("Graph 2 (unconnected):", GRAPH_2_DISTANCES.get(row,col), result.get(row, col), 0.001);
            }
        }
    }
}