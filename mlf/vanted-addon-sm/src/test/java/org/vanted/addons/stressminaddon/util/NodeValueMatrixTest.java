package org.vanted.addons.stressminaddon.util;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Random;
import java.util.function.DoubleUnaryOperator;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

/**
 * Test the class {@link NodeValueMatrix}.
 * @author Jannik
 */
public class NodeValueMatrixTest {

    /** The dimension of the test matrix. */
    public static final int DIMENSION = 10;
    /** The matrix to test with. */
    NodeValueMatrix matrix;

    /** Used for random indices. */
    public static final Random rand = new Random();

    /**
     * Create a matrix for testing.
     * @author Jannik
     */
    @Before
    public void setUp() {
        this.matrix = new NodeValueMatrix(DIMENSION);
        matrix.apply(x -> Math.random()*21+21);
    }

    /**
     * Test the method {@link NodeValueMatrix#get(int, int)}
     * @author Jannik
     */
    @Test
    public void get() {
        int[] pos = rand.ints(0, DIMENSION).distinct().limit(2).toArray();
        assertEquals(0.0, matrix.get(pos[0],pos[0]), 0.001); // check diagonal
        assertEquals(matrix.get(pos[0], pos[1]), matrix.get(pos[1],pos[0]), 0.001); // check symmetry
    }

    /**
     * Test the method {@link NodeValueMatrix#get(int, int, int[])}
     * @author Jannik
     */
    @Test
    public void getTranslated() {
        int[] pos = rand.ints(0, DIMENSION).distinct().limit(2).toArray();
        int[] translation = rand.ints(0, DIMENSION).distinct().limit(DIMENSION).toArray();

        assertEquals(matrix.get(translation[pos[0]], translation[pos[1]]), matrix.get(pos[1],pos[0], translation),
                0.001); // check for right translation
    }

    /**
     * Test the method {@link NodeValueMatrix#set(int, int, double)}
     * @author Jannik
     */
    @Test
    public void set() {
        int[] pos = rand.ints(0, DIMENSION).distinct().limit(2).toArray(); // get two different indices
        matrix.set(pos[0], pos[1], Double.NEGATIVE_INFINITY); // set value
        assertEquals(Double.NEGATIVE_INFINITY, matrix.get(pos[0],pos[1]), 0); // check if it's there + symmetry
        matrix.set(pos[1], pos[0], Double.POSITIVE_INFINITY); // set value
        assertEquals(Double.POSITIVE_INFINITY, matrix.get(pos[0],pos[1]), 0); // check if it's there + symmetry

    }

    /**
     * Test the method {@link NodeValueMatrix#apply(DoubleUnaryOperator)}
     * @author Jannik
     */
    @Test
    public void apply() {
        matrix.apply(x -> 42);
        matrix.apply(x -> 15*x);
        for (int row = 0; row < DIMENSION; row++) {
            for (int col = 0; col < row +1; col++) {
                if (row == col) assertEquals(0.0, matrix.get(row, col), 0);
                else assertEquals(42*15, matrix.get(row, col), 0.0001);
            }
        }
    }

    /**
     * Test whether the right exceptions are thrown.
     * @author Jannik
     */
    @Test
    public void exceptions() {
        // test constructor with illegal dimension
        try {
            new NodeValueMatrix(0);
            fail("No exception thrown");
        } catch (IllegalArgumentException e) {
        } catch (Throwable t) {
            if ("No exception thrown".equals(t.getMessage())) {
                throw t;
            }
            fail("Wrong exception thrown: " + t.getClass().getSimpleName());
        }
        // test index out of bounds
        try {
            try {matrix.get(-1,0); fail("No exception thrown");} catch (IndexOutOfBoundsException e) {}
            try {matrix.set(-1,0, Double.NaN); fail("No exception thrown");} catch (IndexOutOfBoundsException e) {}
            try {matrix.apply(x -> x, -1,0); fail("No exception thrown");} catch (IndexOutOfBoundsException e) {}
            try {matrix.apply(x -> x, 0, DIMENSION); fail("No exception thrown");} catch (IndexOutOfBoundsException e) {}
            try {matrix.apply(x -> x, -1,0, new int[DIMENSION]); fail("No exception thrown");} catch (IndexOutOfBoundsException e) {}
            try {matrix.apply(x -> x, 0, DIMENSION, new int[DIMENSION]); fail("No exception thrown");} catch (IndexOutOfBoundsException e) {}
            try {matrix.get(0, DIMENSION); fail("No exception thrown");} catch (IndexOutOfBoundsException e) {}
            try {matrix.set(0, DIMENSION, Double.NaN); fail("No exception thrown");} catch (IndexOutOfBoundsException e) {}
        } catch (Throwable t) {
            if ("No exception thrown".equals(t.getMessage())) {
                throw t;
            }
            fail("Wrong exception thrown: " + t.getClass().getSimpleName());
        }
        // test setting diagonal
        try{
            int i = rand.nextInt(DIMENSION);
            matrix.set(i, i, Double.NaN);
            fail("No exception thrown");
        } catch (UnsupportedOperationException e) {
        } catch (Throwable t) {
            if ("No exception thrown".equals(t.getMessage())) {
                throw t;
            }
            fail("Wrong exception thrown: " + t.getClass().getSimpleName());
        }

        // non-null
        try {
            matrix.apply(null);
            fail("No exception thrown");
        } catch (NullPointerException e) {
        } catch (Throwable t) {
            if ("No exception thrown".equals(t.getMessage())) {
                throw t;
            }
            fail("Wrong exception thrown: " + t.getClass().getSimpleName());
        }
        try {
            matrix.apply(null, 0, 0);
            fail("No exception thrown");
        } catch (NullPointerException e) {
        } catch (Throwable t) {
            if ("No exception thrown".equals(t.getMessage())) {
                throw t;
            }
            fail("Wrong exception thrown: " + t.getClass().getSimpleName());
        }
        try {
            matrix.apply(null, 0, 0, new int[DIMENSION]);
            fail("No exception thrown");
        } catch (NullPointerException e) {
        } catch (Throwable t) {
            if ("No exception thrown".equals(t.getMessage())) {
                throw t;
            }
            fail("Wrong exception thrown: " + t.getClass().getSimpleName());
        }
    }

    /**
     * Test the method {@link NodeValueMatrix#clone()}
     * @author Jannik
     */
    @Test
    public void testClone() {
        matrix.apply(x->42);
        NodeValueMatrix clone = matrix.clone();
        assertNotSame(clone, matrix);
        clone.set(1, 0, Double.POSITIVE_INFINITY); // create an outlier
        assertEquals(matrix.getDimension(), clone.getDimension());

        for (int row = 0; row < DIMENSION; row++) {
            for (int col = 0; col < row +1; col++) {
                if (row != 1 || col != 0) {
                    assertEquals(matrix.get(row, col), clone.get(row, col), 0.0001);
                } else {
                    assertThat(matrix.get(row, col), not(equalTo(clone.get(row, col))));
                }
            }
        }
    }

    /**
     * Test the method {@link NodeValueMatrix#apply(DoubleUnaryOperator, int, int, int[])}
     * @author Jannik
     */
    @Test
    public void applyLimitedTranslated() {
        // test no apply
        for (int tries = 0; tries < 20; tries++) {
            int[] pos = rand.ints(0, DIMENSION).distinct().limit(2).toArray();
            int[] translation = rand.ints(0, DIMENSION).distinct().limit(DIMENSION).toArray();
            matrix.apply(x->1);
            matrix.apply(x -> 42, pos[0], pos[1], translation);
            for (int row = 0; row < DIMENSION; row++) {
                for (int col = 0; col < DIMENSION; col++) {
                    if (row == col) {
                        assertEquals(0, matrix.get(row, col, translation), 0.0001);
                    } else if  ( row <= pos[0] && col <= pos[1] || col <= pos[0] && row <= pos[1]) {
                        assertEquals(42, matrix.get(row, col,translation), 0.0001);
                    } else {
                        assertEquals(1, matrix.get(row, col, translation), 0.0001);
                    }
                }
            }
        }
    }

    /**
     * Test the method {@link NodeValueMatrix#apply(DoubleUnaryOperator, int, int)}
     * @author Jannik
     */
    @Test
    public void applyLimited() {
        for (int tries = 0; tries < 20; tries++) {
            int[] pos = rand.ints(0, DIMENSION).distinct().limit(2).toArray();
            matrix.apply(x->1);
            matrix.apply(x -> 42, pos[0], pos[1]);
            for (int row = 0; row < DIMENSION; row++) {
                for (int col = 0; col < DIMENSION; col++) {
                    if (row == col) {
                        assertEquals(0, matrix.get(row, col), 0.0001);
                    } else if  ( row <= pos[0] && col <= pos[1] || col <= pos[0] && row <= pos[1]) {
                        assertEquals(42, matrix.get(row, col), 0.0001);
                    } else {
                        assertEquals(1, matrix.get(row, col), 0.0001);
                    }
                }
            }
        }
    }

    /**
     * Test the method {@link NodeValueMatrix#collectRows(int...)}
     * @author Jannik
     */
    @Test
    public void collectRows() {
        int[] rows = rand.ints(0, DIMENSION).limit(rand.nextInt(DIMENSION)).toArray();
        int val = 1;
        for (int row = 0; row < DIMENSION; row++) {
            for (int col = 0; col <= row; col++) {
                if (row == col) continue;
                matrix.set(row, col, val++);
            }
        }

        double[][] result = matrix.collectRows(rows);
        for (int i = 0; i < rows.length; i++) {
            int row = rows[i];
            for (int col = 0; col < DIMENSION; col++) {
                assertEquals(matrix.get(row, col), result[i][col], 0.0001);
            }
        }
    }

    /**
     * Test the method {@link NodeValueMatrix#collectColumns(int...)}
     * @author Jannik
     */
    @Test
    public void collectColumns() {
        int[] cols = rand.ints(0, DIMENSION).limit(rand.nextInt(DIMENSION)).toArray();
        int val = 1;
        for (int row = 0; row < DIMENSION; row++) {
            for (int col = 0; col <= row; col++) {
                if (row == col) continue;
                matrix.set(row, col, val++);
            }
        }

        double[][] result = matrix.collectColumns(cols);
        for (int i = 0; i < cols.length; i++) {
            int col = cols[i];
            for (int row = 0; row < DIMENSION; row++) {
                assertEquals(matrix.get(row, col), result[row][i], 0.0001);
            }
        }
    }

    /**
     * Test the method {@link NodeValueMatrix#print()}
     * @author Jannik
     */
    @Test
    public void print() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();

        System.setOut(new PrintStream(outContent));
        matrix.print();
        assertEquals(DIMENSION, outContent.toString().split("\n").length);
        System.setOut(originalOut);
    }

    /**
     * Test the method {@link NodeValueMatrix#toString()}
     * @author Jannik
     */
    @Test
    public void testToString() {
        assertEquals("NodeValueMatrix{#values="+((DIMENSION-1)*DIMENSION/2)+", dimension="+DIMENSION+"}", matrix.toString());
    }

    /**
     * Test the method {@link NodeValueMatrix#getDimension()}
     * @author Jannik
     */
    @Test
    public void getDimension() {
        assertEquals(DIMENSION, matrix.getDimension());
    }
}