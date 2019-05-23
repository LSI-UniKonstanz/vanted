package org.vanted.addons.stressminaddon.util;

import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class NodeValueMatrixTest {

    /** The dimension of the test matrix. */
    public static final int DIMENSION = 10;
    /** The matrix to test with. */
    NodeValueMatrix matrix;

    public static final Random rand = new Random();

    @Before
    public void setUp() {
        this.matrix = new NodeValueMatrix(DIMENSION);
        matrix.apply(x -> Math.random()*21+21);
    }

    @Test
    public void get() {
        int[] pos = rand.ints(0, DIMENSION).distinct().limit(2).toArray();
        assertEquals(0.0, matrix.get(pos[0],pos[0]), 0.001); // check diagonal
        assertEquals(matrix.get(pos[0], pos[1]), matrix.get(pos[1],pos[0]), 0.001); // check symmetry
    }

    @Test
    public void set() {
        int[] pos = rand.ints(0, DIMENSION).distinct().limit(2).toArray(); // get two different indices
        matrix.set(pos[0], pos[1], Double.NEGATIVE_INFINITY); // set value
        assertEquals(Double.NEGATIVE_INFINITY, matrix.get(pos[0],pos[1]), 0); // check if it's there + symmetry
        matrix.set(pos[1], pos[0], Double.POSITIVE_INFINITY); // set value
        assertEquals(Double.POSITIVE_INFINITY, matrix.get(pos[0],pos[1]), 0); // check if it's there + symmetry

    }

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

    @Test
    public void exceptions() {
        // test constructor with illegal dimension
        try {
            new NodeValueMatrix(0);
            fail("No exception thrown");
        } catch (IllegalArgumentException e) {
        } catch (Throwable t) {
            fail("Wrong exception thrown: " + t.getClass().getSimpleName());
        }
        // test index out of bounds
        try {
            try {matrix.get(-1,0); fail("No exception thrown");} catch (IndexOutOfBoundsException e) {}
            try {matrix.set(-1,0, Double.NaN); fail("No exception thrown");} catch (IndexOutOfBoundsException e) {}
            try {matrix.get(0, DIMENSION); fail("No exception thrown");} catch (IndexOutOfBoundsException e) {}
            try {matrix.set(0, DIMENSION, Double.NaN); fail("No exception thrown");} catch (IndexOutOfBoundsException e) {}
        } catch (Throwable t) {
            fail("Wrong exception thrown: " + t.getClass().getSimpleName());
        }
        try{
            int i = rand.nextInt(DIMENSION);
            matrix.set(i, i, Double.NaN);
            fail("No exception thrown");
        } catch (UnsupportedOperationException e) {
        } catch (Throwable t) {
            fail("Wrong exception thrown: " + t.getClass().getSimpleName());
        }
        // non-null
        try {
            matrix.apply(null);
            fail("No exception thrown");
        } catch (NullPointerException e) {
        } catch (Throwable t) {
            fail("Wrong exception thrown: " + t.getClass().getSimpleName());
        }
    }

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
}