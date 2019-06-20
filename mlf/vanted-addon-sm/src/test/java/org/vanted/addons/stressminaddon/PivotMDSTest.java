package org.vanted.addons.stressminaddon;

import org.Vector2d;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealMatrixImpl;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static data.TestGraphs.GRAPH_1_DISTANCES;
import static data.TestGraphs.GRAPH_1_NODES;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;



public class PivotMDSTest {

    //object to test on
    private PivotMDS pivotMDS;

    private Random random;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        pivotMDS = new PivotMDS();
        random = new Random(42);
        // overwrite internal random for consistency
        final Field rand = PivotMDS.class.getDeclaredField("RAND");
        rand.setAccessible(true);
        final Field randModifiers = Field.class.getDeclaredField("modifiers");
        randModifiers.setAccessible(true);

        randModifiers.setInt(rand, rand.getModifiers() & ~Modifier.FINAL);
        rand.set(null, random);
    }

    @Test
    public void calculateInitialPositions() {
        random.setSeed(42);
        // TODO test correctly
        final List<Vector2d> positions = pivotMDS.calculateInitialPositions(GRAPH_1_NODES, GRAPH_1_DISTANCES);
    }


    @Test
    public void doubleCenter() {
        random.setSeed(42);

        final int amountPivots = 3;

        final int[] pivotTranslation = new int[GRAPH_1_NODES.size()];
        final int[] inversePivotTranslation = new int[GRAPH_1_NODES.size()];
        int[] distanceTranslation = pivotMDS.getPivots(GRAPH_1_DISTANCES, amountPivots, pivotTranslation, inversePivotTranslation);
        assertEquals(4, GRAPH_1_NODES.size());

        RealMatrixImpl expected = new RealMatrixImpl(GRAPH_1_NODES.size(), amountPivots); // 4x3!
        double[][] expectedVals = expected.getDataRef();
        expectedVals[0] = new double[] {+5/6.0, -7/6.0, +1/3.0};
        expectedVals[1] = new double[] {-7/6.0, +5/6.0, +1/3.0};
        expectedVals[2] = new double[] {-1/6.0, -1/6.0, +1/3.0};
        expectedVals[3] = new double[] {+1/2.0, +1/2.0,     -1};

        double[][] actualVals = pivotMDS.doubleCenter(GRAPH_1_DISTANCES, amountPivots, distanceTranslation).getData();
        for (int col = 0; col < GRAPH_1_NODES.size(); col++) {
            assertArrayEquals("Row" + col, expectedVals[col], actualVals[col], 0.0001);
        }
    }


    @Test
    public void powerIterate() {
        random.setSeed(42);

        final int amountPivots = 3;
        final int[] pivotTranslation = new int[GRAPH_1_NODES.size()];
        final int[] inversePivotTranslation = new int[GRAPH_1_NODES.size()];
        assertEquals(4, GRAPH_1_NODES.size());

        int[] distanceTranslation = pivotMDS.getPivots(GRAPH_1_DISTANCES, amountPivots, pivotTranslation, inversePivotTranslation);
        RealMatrix testC = pivotMDS.doubleCenter(GRAPH_1_DISTANCES, amountPivots, distanceTranslation);

        // the matrix should have eigen values 4, 2, 0
        // with eigen vectors (1, -1, 0); (1, 1, -2); (1, 1, 1)
        RealMatrixImpl randomVec = new RealMatrixImpl(amountPivots, 1);
        double[][] tmp = randomVec.getDataRef();
        tmp[0][0] =4358;
        tmp[1][0] =2478;
        tmp[2][0] =6543;

        RealMatrix ctc = testC.transpose().multiply(testC);
        RealMatrix eigenVecTest = pivotMDS.powerIterate(ctc, randomVec).scalarMultiply(1);
        double[] expectedVectors = {1, -1, 0};

        System.out.println(Arrays.toString(eigenVecTest.getColumn(0)));


        for (int row = 0; row < amountPivots; row++) {
            assertEquals("Eigen vector " + row, expectedVectors[row],
                    eigenVecTest.getEntry(row, 0), 0.3);
        }
    }

    @Test
    public void getPivots() {
        random.setSeed(42);
        final int amountPivots = 2;
        final int amountNodes = GRAPH_1_NODES.size();
        assertEquals(4, amountNodes);

        int[] translation = new int[amountNodes], translationInverse = new int[amountNodes];

        pivotMDS.getPivots(GRAPH_1_DISTANCES, amountPivots, translation, translationInverse);

        // test inverse
        for (int i = 0; i < amountNodes; i++) {
            assertEquals("Inverse", i, translationInverse[translation[i]]);
        }

        int[] expected = new int[]{2, 0, 1, 3};
        assertArrayEquals("Pivots", expected, translation);
    }


}


