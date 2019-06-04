package org.vanted.addons.stressminaddon.util;


import org.junit.Test;

import static data.TestGraphs.*;

import static org.junit.Assert.*;

public class ShortestDistanceAlgorithmTest {

    /**
     * object to test with
     */
    ShortestDistanceAlgorithm SDA;

    @Test
    public void getShortestPaths() {

        SDA = new ShortestDistanceAlgorithm();
        NodeValueMatrix result = SDA.getShortestPaths(GRAPH_1_NODES);

        //compares all cells
        for(int row = 0; row < GRAPH_1_NODES.size(); row++) {
            for (int col = 0; col < row + 1; col++) {
                assertEquals(GRAPH_1_DISTANCES.get(row,col), result.get(row, col), 0.001);
            }
        }
    }
}