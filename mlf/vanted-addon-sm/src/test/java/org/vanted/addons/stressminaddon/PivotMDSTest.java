package org.vanted.addons.stressminaddon;

import org.Vector2d;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealMatrixImpl;
import org.junit.Before;
import org.junit.BeforeClass;
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
    public  void setUp() throws NoSuchFieldException, IllegalAccessException {
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
        final List<Vector2d> positions = pivotMDS.calculateInitialPositions(GRAPH_1_NODES, GRAPH_1_DISTANCES, 0.3, true);
        // calculated for eigenvalues 4, 4, 1, 0
        // with eigenvectors (0,0,-1,1),(-1,1,0,0),(-1,-1,1,1),(1,1,1,1)
        double[] expectedXPos = new double[]{-1.1574392460783163,-0.8126096182256408, 1.157439246092345, 0.8126096182116118};
        double[] expectedYPos = new double[]{ 0.8126096182117397,-1.157439246078444 ,-0.8126096182255129,1.1574392460922174};

        for (int idx = 0; idx < positions.size(); idx++) {
            Vector2d position = positions.get(idx);
            assertEquals("X pos", expectedXPos[idx], position.x, 0.0001);
            assertEquals("Y pos", expectedYPos[idx], position.y, 0.0001);
        }
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

        RealMatrixImpl randomVec = new RealMatrixImpl(amountPivots, 1);
        double[][] tmp = randomVec.getDataRef();
        tmp[0][0] =random.nextInt();
        tmp[1][0] =random.nextInt();
        tmp[2][0] =random.nextInt();

        RealMatrix ctc = testC.transpose().multiply(testC);
        RealMatrix eigenVecTest = pivotMDS.powerIterate(ctc, randomVec);

        // the matrix should have eigen values 4, 2, 0
        // with eigen vectors (1, -1, 0); (1, 1, -2); (1, 1, 1)
        double[] expectedVectors = {1, -1, 0};
        // the expected and the actual vector should be linearly dependant
        eigenVecTest = eigenVecTest.scalarMultiply(1/eigenVecTest.getEntry(0, 0)); // normalize to first entry

        for (int row = 0; row < amountPivots; row++) {
            assertEquals("Eigen vector " + row, expectedVectors[row],
                    eigenVecTest.getEntry(row, 0), 0.0001);
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


