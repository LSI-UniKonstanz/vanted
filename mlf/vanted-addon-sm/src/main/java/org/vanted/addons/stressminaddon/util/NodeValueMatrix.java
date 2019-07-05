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
    public final double get(final int row, final int col) {
        //assert 0 <= row && row < dimension : "Index of 'row' out of bounds: 0 <= " + row + "<= " + (dimension-1);
        //assert 0 <= col && col < dimension : "Index of 'col' out of bounds: 0 <= " + col + "<= " + (dimension-1);
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
     * Get a specific cell of the matrix via a translated index.<br>
     * This just calls {@code get(translation[row], translation[col])}.
     *
     * @param row the row beginning from 0 and a maximum of {@code dimension-1}
     * @param col the column beginning from 0 and a maximum of {@code dimension-1}
     * @param translation
     *      the translation table to be used. Values in the array represent the
     *      actual index, the index the translated index. This array must have
     *      the same length as the dimension of this matrix.
     *
     * @return
     *      the value at this cell or {@code 0} if {@code row} equals {@code col}.
     *
     * @throws IndexOutOfBoundsException if {@code row} or {@code col} are out of bounds.
     * @author Jannik
     */
    public final double get(final int row, final int col, final int[] translation) {
        assert translation.length == this.dimension;
        return this.get(translation[row], translation[col]); // Hope that the compiler inlines this method.
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
        //assert 0 <= row && row < dimension : "Index of 'row' out of bounds: 0 <= " + row + "<= " + (dimension-1);
        //assert 0 <= col && col < dimension : "Index of 'col' out of bounds: 0 <= " + col + "<= " + (dimension-1);
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
            for (int col = 0; col <= row; col++) {
                this.values[row][col] = operator.applyAsDouble(this.values[row][col]);
            }
        }
        return this;
    }

    /**
     * Applies a specified operation on every non-diagonal cell of the matrix
     * and updates it, in the rectangle given by using {@code maxRow} lower and
     * {@code maxCol} right “line” (including) with the width {@code maxRow+1} and
     * height of {@code maxCol+1}.<br>
     * Node that these bounds act as indices so {@code 0} refers to the first
     * row/column.<br>
     *
     * @param operator
     *      the operator to be applied. May not be {@code null}.
     * @param maxRow
     *      the maximum row to update. The function will not be applied to any row
     *      past this number. Range {@code 0} to {@code dimension-1}.
     * @param maxCol
     *      the maximum column to update. The function will not be applied to any column
     *      past this number. Range {@code 0} to {@code dimension-1}.
     *
     * @return
     *      the matrix itself.
     *
     * @throws IndexOutOfBoundsException if {@code row} or {@code col} are out of bounds.
     * @author Jannik
     */
    public NodeValueMatrix apply(final DoubleUnaryOperator operator, final int maxRow, final int maxCol) {
        //assert 0 <= maxRow && maxRow < dimension : "Index of 'maxRow' out of bounds: 0 <= " + maxRow + "<= " + (dimension-1);
        //assert 0 <= maxCol && maxCol < dimension : "Index of 'maxCol' out of bounds: 0 <= " + maxCol + "<= " + (dimension-1);
        if (maxRow < 0 || maxRow > dimension-1)
            throw new IndexOutOfBoundsException("Index 'maxRow' out of bounds: 0 <= maxRow <= " + (dimension-1));
        if (maxCol < 0 || maxCol > dimension-1)
            throw new IndexOutOfBoundsException("Index 'maxCol' out of bounds: 0 <= maxCol <= " + (dimension-1));
        if (null == operator) throw new NullPointerException("Operator may not be null!");

        // apply on first rows
        for (int row = 0; row < maxRow; row++) {
            for (int col = 0; col <= row && col <= maxCol; col++) {
                this.values[row][col] = operator.applyAsDouble(this.values[row][col]);
            }
        }
        // apply on the rest of the rows to set the mirrored column size, if necessary
        for (int row = maxRow; row < maxCol; row++) {
            for (int col = 0; col <= row && col <= maxRow; col++) {
                this.values[row][col] = operator.applyAsDouble(this.values[row][col]);
            }
        }
        return this;
    }

    /**
     * Applies a specified operation on every non-diagonal cell of the matrix
     * and updates it, in the rectangle given by using {@code maxRow} lower and
     * {@code maxCol} right “line” (including) with the width {@code maxRow+1} and
     * height of {@code maxCol+1}.<br>
     * Node that these bounds act as indices so {@code 0} refers to the first
     * row/column.<br>
     * Before setting every value that would be in this rectangle is set it is
     * translated using the provided translation table ({@code translation}).
     *
     * @param operator
     *      the operator to be applied. May not be {@code null}.
     * @param maxRow
     *      the maximum row to update. The function will not be applied to any row
     *      past this number. Range {@code 0} to {@code dimension-1}.<br>
     *      This will not be translated.
     * @param maxCol
     *      the maximum column to update. The function will not be applied to any column
     *      past this number. Range {@code 0} to {@code dimension-1}.<br>
     *      This will not be translated.
     * @param translation
     *      the translation table to be used. Values in the array represent the
     *      actual index, the index the translated index. This array must have
     *      the same length as the dimension of this matrix.
     *
     * @return
     *      the matrix itself.
     *
     * @throws IndexOutOfBoundsException if {@code row} or {@code col} are out of bounds.
     * @see #get(int, int, int[])
     * @author Jannik
     */
    @SuppressWarnings("Duplicates") // wanted for speed
    public NodeValueMatrix apply(final DoubleUnaryOperator operator, final int maxRow, final int maxCol,
                                 final int[] translation) {
        assert translation.length == this.dimension;
        //assert 0 <= maxRow && maxRow         // apply on the rest of the rows to set the mirrored column size, if necessary< dimension : "Index of 'maxRow' out of bounds: 0 <= " + maxRow + "<= " + (dimension-1);
        //assert 0 <= maxCol && maxCol < dimension : "Index of 'maxCol' out of bounds: 0 <= " + maxCol + "<= " + (dimension-1);
        if (maxRow < 0 || maxRow > dimension-1) {
            throw new IndexOutOfBoundsException("Index 'maxRow' out of bounds (translation): 0 <= maxRow <= " + (dimension-1));
        }
        if (maxCol < 0 || maxCol > dimension-1) {
            throw new IndexOutOfBoundsException("Index 'maxCol' out of bounds (translation): 0 <= maxCol <= " + (dimension-1));
        }
        if (null == operator) {
            throw new NullPointerException("Operator may not be null!");
        }

        // apply on first rows
        for (int row = 0; row < maxRow; row++) {
            for (int col = 0; col <= row && col <= maxCol; col++) {
                final int actualRow = translation[row+1], actualCol = translation[col];
                if (actualCol < actualRow) {
                    this.values[actualRow-1][actualCol] =
                            operator.applyAsDouble(this.values[actualRow-1][actualCol]);
                } else if (actualRow < actualCol) {
                    this.values[actualCol-1][actualRow] =
                            operator.applyAsDouble(this.values[actualCol-1][actualRow]);
                }
            }
        }
        // apply on the rest of the rows to set the mirrored column size, if necessary
        for (int row = maxRow; row < maxCol; row++) {
            for (int col = 0; col <= row && col <= maxRow; col++) {
                final int actualRow = translation[row+1], actualCol = translation[col];
                if (actualCol < actualRow) {
                    this.values[actualRow-1][actualCol] =
                            operator.applyAsDouble(this.values[actualRow-1][actualCol]);
                } else if (actualRow < actualCol) {
                    this.values[actualCol-1][actualRow] =
                            operator.applyAsDouble(this.values[actualCol-1][actualRow]);
                }
            }
        }
        return this;
    }


    /**
     * Creates a multidimensional array containing all the rows specified in the array.
     * The first array in the result will contain the selected rows at the position in
     * [@code rows}.
     *
     * @param rows
     *      the rows to get from this matrix.
     *
     * @return an multidimensional array containing the accumulated rows.
     *
     * @author Jannik
     */
    public double[][] collectRows(final int ... rows) {
        double[][] result = new double[rows.length][this.dimension];

        for (int writeRow = 0, rowsLength = rows.length; writeRow < rowsLength; writeRow++) {
            final int readRow = rows[writeRow];
            for (int col = 0; col < this.dimension; col++) {
                if (readRow == col) {
                    continue; // skip zero field
                }
                result[writeRow][col] = (col < readRow) ? // swap if necessary
                        (this.values[readRow-1][col]) : (this.values[col-1][readRow]);
            }
        }
        return result;
    }

    /**
     * Creates a multidimensional array containing all the columns specified in the array.
     * The second arrays in the result will contain the selected columns at the position in
     * [@code rows}.
     *
     * @param cols
     *      the columns to get from this matrix.
     *
     * @return an multidimensional array containing the accumulated columns.
     *
     * @author Jannik
     */
    public double[][] collectColumns(final int ... cols) {
        double[][] result = new double[this.dimension][cols.length];

        for (int row = 0; row < this.dimension; row++) {
            for (int writeCol = 0, colsLength = cols.length; writeCol < colsLength; writeCol++) {
                final int readCol = cols[writeCol];
                if (row == readCol) {
                    continue; // skip zero field
                }
                result[row][writeCol] = (readCol < row) ? // swap if necessary
                        (this.values[row-1][readCol]) : (this.values[readCol-1][row]);
            }
        }
        return result;
    }

    /**
     * @return
     *      the biggest value in this matrix.
     *
     * @author Jannik
     */
    public double getMaximumValue() {
        double result = 0;
        for (int row = 0; row < dimension - 1; row++) {
            for (int col = 0; col <= row; col++) {
                result = Math.max(result, this.values[row][col]);
            }
        }
        return result;
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

    @Override
    public String toString() {
        return "NodeValueMatrix{" +
                "#values=" + (dimension*(dimension-1)/2) +
                ", dimension=" + dimension +
                '}';
    }

    /**
     * Prints the values of this matrix to stdout. For debugging purposes.
     * @author Jannik
     */
    public void print() {
        for (int row = 0; row < dimension; row++) {
            for (int col = 0; col < dimension; col++) {
                System.out.printf("%05.2f ", this.get(row, col));
            }
            System.out.println();
        }

    }
}
