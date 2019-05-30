package org.vanted.addons.stressminaddon.util;

import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;

/**
 * An object that stores a specific double value to a pair of nodes.
 * The matrix is always symmetrical, a square matrix and has a 0 value diagonal.
 *
 * @author Jannik
 */
public class NodeValueMatrix implements Cloneable {

    /**
     * The values stored for a specific node pair.
     * Nodes are accessed by their index.
     */
    private final double[][] values;

    /**
     * The external number of rows and columns
     * of the matrix.
     */
    private final int dimension;

    /**
     * Creates a value matrix. It is symmetrical and the diagonal is 0 by default.
     *
     * @param dimension
     *      the dimension of the square matrix. This value must be positive (>0)
     * @throws IllegalArgumentException
     *      if {@code dimension} is non-positive.
     * @author Jannik
     */
    public NodeValueMatrix(final int dimension) {
        if ((this.dimension = dimension) <= 0)
            throw new IllegalArgumentException("Dimension must be positive!");
        // initialize the values (internally only a (d-1)*(d-1) triangular matrix is saved
        this.values = new double[dimension-1][];; // the first row is always empty
        for (int row = 0; row < dimension - 1; row++) {
            this.values[row] = new double[row+1];
        }
    }

    /**
     * Creates a clone.
     * @param other
     *      the matrix to be cloned. May not be {@code null}.
     * @author Jannik
     */
    private NodeValueMatrix(final NodeValueMatrix other) {
        this.dimension = other.dimension;
        this.values = new double[this.dimension-1][];
        for (int row = 0; row < dimension - 1; row++) {
            this.values[row] = Arrays.copyOf(other.values[row], row+1);
        }
    }

    /**
     * @return the dimension of the matrix.
     * @author Jannik
     */
    public int getDimension() {
        return dimension;
    }

    /**
     * Get a specific cell of the matrix.
     *
     * @param row the row beginning from 0 and a maximum of {@code dimension-1}
     * @param col the column beginning from 0 and a maximum of {@code dimension-1}
     *
     * @return
     *      the value at this cell or {@code 0} if {@code row} equals {@code col}.
     *
     * @throws IndexOutOfBoundsException if {@code row} or {@code col} are out of bounds.
     * @author Jannik
     */
    public double get(final int row, final int col) {
        if (row < 0 || row > dimension-1)
            throw new IndexOutOfBoundsException("Index of 'row' out of bounds: 0 <= row <= " + (dimension-1));
        if (col < 0 || col > dimension-1)
            throw new IndexOutOfBoundsException("Index of 'col' out of bounds: 0 <= col <= " + (dimension-1));
        // get the value
        if (row == col)     return 0; // the diagonal is always 0
        else if (row > col) return this.values[row-1][col]; // the first row is not saved
        else                return this.values[col-1][row]; // the matrix is symmetrical
    }

    /**
     * Set a specific non-diagonal cell of the matrix.
     *
     * @param row the row beginning from 0 and a maximum of {@code dimension-1}
     * @param col the column beginning from 0 and a maximum of {@code dimension-1}
     * @param value the value to set.
     *
     * @throws IndexOutOfBoundsException if {@code row} or {@code col} are out of bounds.
     * @throws UnsupportedOperationException if {@code row} and {@code col} are equal.
     * @author Jannik
     */
    public synchronized void set(final int row, final int col, final double value) {
        if (row < 0 || row > dimension-1)
            throw new IndexOutOfBoundsException("Index 'row' out of bounds: 0 <= row <= " + (dimension-1));
        if (col < 0 || col > dimension-1)
            throw new IndexOutOfBoundsException("Index 'col' out of bounds: 0 <= col <= " + (dimension-1));
        if (row == col)
            throw new UnsupportedOperationException("The diagonal may not be set (row==col)");
        // set the value
        else if (row > col) this.values[row-1][col] = value; // the first row is not saved
        else                this.values[col-1][row] = value; // the matrix is symmetrical
    }

    /**
     * Applies a specified operation on every non-diagonal cell of the matrix
     * and updates it.
     *
     * @param operator
     *      the operator to be applied. May not be {@code null}.
     *
     * @return
     *      the matrix itself.
     * @author Jannik
     */
    public NodeValueMatrix apply(final DoubleUnaryOperator operator) {
        if (null == operator) throw new NullPointerException("Operator may not be null!");
        for (int row = 0; row < dimension - 1; row++) {
            for (int col = 0; col < row + 1; col++) {
                this.values[row][col] = operator.applyAsDouble(this.values[row][col]);
            }
        }
        return this;
    }

    /**
     * @see Object#clone()
     * @return
     *      a clone of the current NodeValueMatrix.
     * @author Jannik
     */
    @Override
    public NodeValueMatrix clone() {
        return new NodeValueMatrix(this);
    }
}
