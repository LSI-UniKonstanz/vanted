package org.vanted.addons.stressminaddon;

import org.Vector2d;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealMatrixImpl;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.junit.Before;
import org.junit.Test;
import org.vanted.addons.stressminaddon.util.NodeValueMatrix;
import org.vanted.addons.stressminaddon.util.gui.EnableableNumberParameter;

import javax.swing.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static data.TestGraphs.GRAPH_1_DISTANCES;
import static data.TestGraphs.GRAPH_1_NODES;
import static org.junit.Assert.*;


/**
 * Tests the {@link PivotMDS} class
 * @author Jannik, theo
 */
public class PivotMDSTest {

    /**
     * Object to test with.
     */
    private PivotMDS pivotMDS;
    /**
     * Random used to restrict randomness.
     */
    private Random random;

    /**
     * Set up the mock random.
     *
     * @throws NoSuchFieldException   if the random could not be replaced
     * @throws IllegalAccessException if the random could not be replaced
     */
    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        pivotMDS = new PivotMDS();
        pivotMDS.doSquaring = true; // compatibility with old tests
        random = new Random(42);
        // overwrite internal random for consistency
        final Field rand = PivotMDS.class.getDeclaredField("RAND");
        rand.setAccessible(true);
        final Field randModifiers = Field.class.getDeclaredField("modifiers");
        randModifiers.setAccessible(true);

        randModifiers.setInt(rand, rand.getModifiers() & ~Modifier.FINAL);
        rand.set(null, random);
    }

    /**
     * Test method {@link PivotMDS#calculateInitialPositions(List, NodeValueMatrix)}
     *
     * @author Jannik
     */
    @Test
    public void calculateInitialPositions() {
        random.setSeed(42);
        // test with squaring
        pivotMDS.doSquaring = true;
        pivotMDS.percentPivots = 100.0;
        // TODO Fix after scaling is a thing
        List<Vector2d> positions = pivotMDS.calculateInitialPositions(GRAPH_1_NODES, GRAPH_1_DISTANCES);

        // calculated for eigenvalues 4, 4, 1, 0
        // with eigenvectors (0,0,-1,1),(-1,1,0,0),(-1,-1,1,1),(1,1,1,1)
        double[] expectedXPos = new double[]{-1.1574392460783163, -0.8126096182256408, 1.157439246092345,  0.8126096182116118};
        double[] expectedYPos = new double[]{ 0.8126096182117397, -1.157439246078444, -0.8126096182255129, 1.1574392460922174};

        for (int idx = 0; idx < positions.size(); idx++) {
            Vector2d position = positions.get(idx);
            assertEquals("X pos", expectedXPos[idx], position.x, 0.0001);
            assertEquals("Y pos", expectedYPos[idx], position.y, 0.0001);
        }

        // test without squaring
        pivotMDS.doSquaring = false;
        positions = pivotMDS.calculateInitialPositions(GRAPH_1_NODES, GRAPH_1_DISTANCES);
        // calculated for eigenvalues 1, 1, 0, 0
        // with eigenvectors (0,0,-1,1),(-1,1,0,0),(0,0,1,1),(1,1,0,0)
        expectedXPos = new double[]{-0.7069120701306525, -0.016592923298666573, 0.7069120701306525,   0.016592923298666573};
        expectedYPos = new double[]{-0.01659292329866657, 0.7069120701306525,   0.01659292329866657, -0.7069120701306525};

        for (int idx = 0; idx < positions.size(); idx++) {
            Vector2d position = positions.get(idx);
            assertEquals("X pos", expectedXPos[idx], position.x, 0.0001);
            assertEquals("Y pos", expectedYPos[idx], position.y, 0.0001);
        }

        // test one dimensional matrix
        positions = pivotMDS.calculateInitialPositions(Collections.singletonList(GRAPH_1_NODES.get(0)), new NodeValueMatrix(1));
        expectedXPos = new double[]{0.0};
        expectedYPos = new double[]{0.0};
        for (int idx = 0; idx < positions.size(); idx++) {
            Vector2d position = positions.get(idx);
            assertEquals("X pos", expectedXPos[idx], position.x, 0);
            assertEquals("Y pos", expectedYPos[idx], position.y, 0);
        }
    }


    /**
     * Test method
     * {@link PivotMDS#doubleCenter(org.vanted.addons.stressminaddon.util.NodeValueMatrix, int, int[])}.
     *
     * @author Jannik
     */
    @Test
    public void doubleCenter() {
        random.setSeed(42);

        final int amountPivots = 3;

        final int[] pivotTranslation = new int[GRAPH_1_NODES.size()];
        final int[] inversePivotTranslation = new int[GRAPH_1_NODES.size()];
        int[] distanceTranslation = pivotMDS.getPivots(GRAPH_1_DISTANCES, amountPivots, pivotTranslation, inversePivotTranslation);
        assertEquals(4, GRAPH_1_NODES.size());

        // test with squaring
        pivotMDS.doSquaring = true;
        RealMatrixImpl expected = new RealMatrixImpl(GRAPH_1_NODES.size(), amountPivots); // 4x3!
        double[][] expectedVals = expected.getDataRef();
        expectedVals[0] = new double[]{+5 / 6.0, -7 / 6.0, +1 / 3.0};
        expectedVals[1] = new double[]{-7 / 6.0, +5 / 6.0, +1 / 3.0};
        expectedVals[2] = new double[]{-1 / 6.0, -1 / 6.0, +1 / 3.0};
        expectedVals[3] = new double[]{+1 / 2.0, +1 / 2.0,       -1};

        double[][] actualVals = pivotMDS.doubleCenter(GRAPH_1_DISTANCES, amountPivots, distanceTranslation).getData();
        for (int col = 0; col < GRAPH_1_NODES.size(); col++) {
            assertArrayEquals("Row (squared)" + col, expectedVals[col], actualVals[col], 0.0001);
        }

        // test without squaring
        pivotMDS.doSquaring = false;
        expectedVals[0] = new double[]{+1 / 2.0, -1 / 2.0,       +0};
        expectedVals[1] = new double[]{-1 / 2.0, +1 / 2.0,       +0};
        expectedVals[2] = new double[]{-1 / 6.0, -1 / 6.0, +1 / 3.0};
        expectedVals[3] = new double[]{+1 / 6.0, +1 / 6.0, -1 / 3.0};

        actualVals = pivotMDS.doubleCenter(GRAPH_1_DISTANCES, amountPivots, distanceTranslation).getData();
        for (int col = 0; col < GRAPH_1_NODES.size(); col++) {
            assertArrayEquals("Row (not squared)" + col, expectedVals[col], actualVals[col], 0.0001);
        }
    }


    /**
     * Test method {@link PivotMDS#powerIterate(RealMatrix, RealMatrix)}
     *
     * @author Jannik
     */
    @Test
    public void powerIterate() {
        random.setSeed(42);

        final int amountPivots = 3;
        final int[] pivotTranslation = new int[GRAPH_1_NODES.size()];
        final int[] inversePivotTranslation = new int[GRAPH_1_NODES.size()];
        assertEquals(4, GRAPH_1_NODES.size());

        int[] distanceTranslation = pivotMDS.getPivots(GRAPH_1_DISTANCES, amountPivots, pivotTranslation, inversePivotTranslation);
        // test with squaring
        pivotMDS.doSquaring = true;
        RealMatrix testC = pivotMDS.doubleCenter(GRAPH_1_DISTANCES, amountPivots, distanceTranslation);

        RealMatrixImpl randomVec = new RealMatrixImpl(amountPivots, 1);
        double[][] tmp = randomVec.getDataRef();
        tmp[0][0] = random.nextInt();
        tmp[1][0] = random.nextInt();
        tmp[2][0] = random.nextInt();

        RealMatrix ctc = testC.transpose().multiply(testC);
        RealMatrix eigenVecTest = pivotMDS.powerIterate(ctc, randomVec);

        // the matrix should have eigen values 4, 2, 0
        // with eigen vectors (1, -1, 0); (1, 1, -2); (1, 1, 1)
        double[] expectedVectors = {1, -1, 0};
        // the expected and the actual vector should be linearly dependant
        eigenVecTest = eigenVecTest.scalarMultiply(1 / eigenVecTest.getEntry(0, 0)); // normalize to first entry

        for (int row = 0; row < amountPivots; row++) {
            assertEquals("Eigen vector (squared)" + row, expectedVectors[row],
                    eigenVecTest.getEntry(row, 0), 0.0001);
        }

        // test without squaring
        pivotMDS.doSquaring = false;
        testC = pivotMDS.doubleCenter(GRAPH_1_DISTANCES, amountPivots, distanceTranslation);

        ctc = testC.transpose().multiply(testC);
        eigenVecTest = pivotMDS.powerIterate(ctc, randomVec);

        // the matrix should have eigen values 1, 1/3, 0
        // with eigen vectors (1, -1, 0); (1, 1, -2); (1, 1, 1) (The same as above, nice!)
        expectedVectors = new double[]{1, -1, 0};
        // the expected and the actual vector should be linearly dependant
        eigenVecTest = eigenVecTest.scalarMultiply(1 / eigenVecTest.getEntry(0, 0)); // normalize to first entry

        for (int row = 0; row < amountPivots; row++) {
            assertEquals("Eigen vector (nor squared)" + row, expectedVectors[row],
                    eigenVecTest.getEntry(row, 0), 0.0001);
        }
    }

    /**
     * Test the method {@link PivotMDS#getPivots(NodeValueMatrix, int, int[], int[])}
     *
     * @author Jannik
     */
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

        // test with missing values
        NodeValueMatrix noDistances = new NodeValueMatrix(4);
        noDistances.apply(x -> Double.POSITIVE_INFINITY);
        pivotMDS.getPivots(noDistances, amountPivots, translation, translationInverse);
        random.setSeed(42); // reset random
        expected = new int[]{0, 1, 2, 3};
        assertArrayEquals("Pivots", expected, translation);
    }

    /**
     * Test the method {@link PivotMDS#findEigenVal(RealMatrix, RealMatrix)}
     *
     * @author Jannik
     */
    @Test
    public void findEigenVal() {

        // test for known eigenvalues
        RealMatrixImpl matrix = new RealMatrixImpl(GRAPH_1_NODES.size(), 3); // 4x3!
        double[][] matrixVals = matrix.getDataRef();
        matrixVals[0] = new double[]{+5 / 6.0, -7 / 6.0, +1 / 3.0};
        matrixVals[1] = new double[]{-7 / 6.0, +5 / 6.0, +1 / 3.0};
        matrixVals[2] = new double[]{-1 / 6.0, -1 / 6.0, +1 / 3.0};
        matrixVals[3] = new double[]{+1 / 2.0, +1 / 2.0,       -1};
        matrix = (RealMatrixImpl) matrix.transpose().multiply(matrix);

        RealMatrixImpl eigenVec = new RealMatrixImpl(3, 1);
        matrixVals = eigenVec.getDataRef();
        matrixVals[0][0] =  1;
        matrixVals[1][0] = -1;
        matrixVals[2][0] =  0;

        double expected = 4;
        double actual   = pivotMDS.findEigenVal(matrix, eigenVec);

        assertEquals("Find eigen value", expected, actual, 0.001);

        // special divide by 0 behavior
        matrix = new RealMatrixImpl(1,1);
        eigenVec = new RealMatrixImpl(1, 1);
        expected = 0;
        actual   = pivotMDS.findEigenVal(matrix, eigenVec);
        assertEquals("Find divide by 0 eigen value", expected, actual, 0.001);
    }

    /**
     * Test method {@link PivotMDS#getEuclideanNorm(RealMatrix)}
     * @author Jannik
     */
    @Test
    public void getEuclideanNorm() {
       // with test vector
        RealMatrixImpl vec = new RealMatrixImpl(3, 1);
        double[][] matrixVals = vec.getDataRef();
        matrixVals[0][0] =  21;
        matrixVals[1][0] = -21;
        matrixVals[2][0] =  42;

        double expected = Math.sqrt((21+42)*42);
        double actual   = pivotMDS.getEuclideanNorm(vec);
        assertEquals("euclidean norm", expected, actual, 0.001);
    }

    /**
     * Test possible thrown exceptions.
     * @author Jannik
     */
    @Test
    public void exceptions() {
        try { // non-vector input for getEuclideanNorm
            pivotMDS.getEuclideanNorm(new RealMatrixImpl(2,6));
            fail("No exception thrown");
        } catch (IllegalArgumentException e) {
        } catch (Throwable t) {
            if ("No exception thrown".equals(t.getMessage())) {
                throw t;
            }
            fail("Wrong exception thrown! " + t.getClass().getSimpleName());
        }

    }

    /**
     * Test method {@link PivotMDS#getName()}
     * @author Jannik
     */
    @Test
    public void getName() {
        // only test whether the name would be readable for user
        assertNotNull(pivotMDS.getName());
        assertFalse(pivotMDS.getName().trim().isEmpty());
    }

    /**
     * Test method {@link PivotMDS#getDescription()}
     * @author Jannik
     */
    @Test
    public void getDescription() {
        // only test whether the description would be readable for user
        assertNotNull(pivotMDS.getDescription());
        assertFalse(pivotMDS.getDescription().trim().isEmpty());
    }

    /**
     * Test method {@link PivotMDS#getParameters()}}
     * @author Jannik
     */
    @Test
    public void getParameters() {
        // should not have any parameters
        Parameter[] parameters = pivotMDS.getParameters();
        assertNotNull(parameters);
        assertTrue(parameters.length > 0);
    }

    /**
     * Test method {@link PivotMDS#setParameters(Parameter[])}
     * @author Jannik
     */
    @Test
    public void setParameters() {
        Parameter[] parameters = pivotMDS.getParameters();
        pivotMDS.setParameters(parameters);

        assertEquals(pivotMDS.AMOUNT_PIVOTS_DEFAULT,pivotMDS.percentPivots, 0.00001 );
        assertEquals(pivotMDS.QUADRATIC_DOUBLECENTER_DEFAULT, pivotMDS.doSquaring);

        ((JSpinner) ((EnableableNumberParameter) parameters[0].getValue()).getComponent(0)).setValue(0.0);
        ((BooleanParameter) parameters[1]).setValue(true);
        pivotMDS.setParameters(parameters); // should still be executed correctly

        assertEquals(0.0,pivotMDS.percentPivots, 0.00001 );
        assertEquals(true, pivotMDS.doSquaring);



        ((JSpinner) ((EnableableNumberParameter) parameters[0].getValue()).getComponent(0)).setValue(100.0);
        pivotMDS.setParameters(parameters);

        assertEquals(100.0,pivotMDS.percentPivots, 0.00001 );

    }
}
