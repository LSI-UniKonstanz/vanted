package org.vanted.addons.stressminaddon;

import org.Vector2d;
import org.apache.commons.math.linear.RealMatrix;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static data.TestGraphs.GRAPH_1_DISTANCES;
import static data.TestGraphs.GRAPH_1_NODES;



public class PivotMDSTest {

    //object to test on
    PivotMDS pivotMDS = new PivotMDS();

    @Test
    public void calculateInitialPositionsTest() {
        final List<Vector2d> positions = pivotMDS.calculateInitialPositions(GRAPH_1_NODES, GRAPH_1_DISTANCES);

    }


    @Test
    public void doubleCenterTest() {

        final int amountPivots = 3;

        final int[] pivotTranslation = new int[GRAPH_1_NODES.size()];
        final int[] inversePivotTranslation = new int[GRAPH_1_NODES.size()];
        int[] distanceTranslation = pivotMDS.getPivots(GRAPH_1_DISTANCES, amountPivots, pivotTranslation, inversePivotTranslation);

        RealMatrix testC = pivotMDS.doubleCenter(GRAPH_1_DISTANCES, amountPivots, distanceTranslation);
        for (int i = 0; i < amountPivots; i++) {
            System.out.println(Arrays.toString(testC.getColumn(i)));
        }
    }


    @Test
    public void powerIterateTest() {

        final int amountPivots = 3;
        final int[] pivotTranslation = new int[GRAPH_1_NODES.size()];
        final int[] inversePivotTranslation = new int[GRAPH_1_NODES.size()];

        int[] distanceTranslation = pivotMDS.getPivots(GRAPH_1_DISTANCES, amountPivots, pivotTranslation, inversePivotTranslation);
        RealMatrix testC = pivotMDS.doubleCenter(GRAPH_1_DISTANCES, amountPivots, distanceTranslation);

        RealMatrix eigenVecsTest = pivotMDS.powerIterate(testC);

        for (int i = 0; i < 2; i++) {
            System.out.println(Arrays.toString(eigenVecsTest.getColumn(i)));
        }
    }

}


