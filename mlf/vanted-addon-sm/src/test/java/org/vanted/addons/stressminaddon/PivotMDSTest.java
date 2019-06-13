package org.vanted.addons.stressminaddon;

import org.apache.commons.math.linear.RealMatrix;
import org.junit.Test;
import java.util.Arrays;
import static data.TestGraphs.*;



public class PivotMDSTest {

    //object to test on
    PivotMDS pivotMDS = new PivotMDS();

    @Test
    public void calculateInitialPositionsTest() {

    }


    @Test
    public void doubleCenterTest() {

        final int amountPivots = 3;

        int[] distanceTranslation = pivotMDS.getPivots(GRAPH_1_DISTANCES, amountPivots);

        RealMatrix testC = pivotMDS.doubleCenter(GRAPH_1_DISTANCES, amountPivots, distanceTranslation);
        for (int i = 0; i < amountPivots; i++) {
            System.out.println(Arrays.toString(testC.getColumn(i)));
        }
    }


    @Test
    public void powerIterateTest() {

        final int amountPivots = 3;
        int[] distanceTranslation = pivotMDS.getPivots(GRAPH_1_DISTANCES, amountPivots);
        RealMatrix testC = pivotMDS.doubleCenter(GRAPH_1_DISTANCES, amountPivots, distanceTranslation);

        RealMatrix eigenVecsTest = pivotMDS.powerIterate(testC);

        for (int i = 0; i < 2; i++) {
            System.out.println(Arrays.toString(eigenVecsTest.getColumn(i)));
        }
    }

}


