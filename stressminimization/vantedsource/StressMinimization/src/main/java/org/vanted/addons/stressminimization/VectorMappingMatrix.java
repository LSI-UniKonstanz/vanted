package org.vanted.addons.stressminimization;

import java.util.Arrays;

import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.linear.AbstractRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

/**
 * A helper class that makes an input vector accessible as a matrix.
 * The input vector is seen as a (n*d) matrix, by virtually grouping
 * every d elements into a row in the matrix.
 * 
 * As an example: the input <pre> 1, 2, 3, 4, 5, 6, 7, 8 </pre> will be seen as this matrix:
 * <pre>
 * 1, 2 <br>
 * 3, 4 <br>
 * 5, 6 <br>
 * 7, 8 
 * </pre> if the initial layout has two dimensions.
 * 
 * This class only does index transformations and 
 * does not copy the vector entries.
 */
class VectorMappingMatrix extends AbstractRealMatrix {

	// the virtual matrix's dimensions
	private final int n, d;
	private final double[] vector;
	
	/**
	 * Creates a new VectorMappingMatrix from the given input vector
	 * with the specified number of columns. 
	 * No length checking is performed, if the length of the input vector
	 * is not divisible by the given number of columns,
	 * some vector elements will be fully ignored.
	 * @param d the number of columns this matrix should have
	 * @param vector
	 */
	public VectorMappingMatrix(int d, double[] vector) {
		super();
		this.n = vector.length / d;
		this.d = d;
		this.vector = vector;
	}

	public VectorMappingMatrix(int n, int d) {
		this.n = n;
		this.d = d;
		this.vector = new double[n * d];
	}
	
	public static VectorMappingMatrix asVectorMappingMatrix(RealMatrix m) {
		if (m instanceof VectorMappingMatrix) {
			return (VectorMappingMatrix) m;
		} else {
			VectorMappingMatrix cpy = new VectorMappingMatrix(m.getRowDimension(), m.getColumnDimension());
			cpy.setSubMatrix(m.getData(), 0, 0);
			return cpy;
		}
	}
	
	@Override
	public RealMatrix copy() {
		return new VectorMappingMatrix(d, Arrays.copyOf(vector, vector.length));
	}

	@Override
	public RealMatrix createMatrix(int rows, int columns) throws NotStrictlyPositiveException {
		if (rows < 1) {
			throw new NotStrictlyPositiveException(rows);
		}
		if (columns < 1) {
			throw new NotStrictlyPositiveException(columns);
		}
		
		return new VectorMappingMatrix(rows, columns);
	}

	@Override
	public int getColumnDimension() {
		return d;
	}

	@Override
	public int getRowDimension() {
		return n;
	}

	@Override
	public double getEntry(int row, int col) throws OutOfRangeException {
		int index = row * d + col;
		return vector[index];
	}

	@Override
	public void setEntry(int row, int col, double val) throws OutOfRangeException {
		int index = row * d + col;
		vector[index] = val;
	}
	
	public double[] getVector() {
		return vector;
	}
	
}