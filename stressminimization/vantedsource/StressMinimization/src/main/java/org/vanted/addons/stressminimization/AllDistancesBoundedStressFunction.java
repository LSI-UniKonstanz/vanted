package org.vanted.addons.stressminimization;

import java.util.Arrays;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.linear.AbstractRealMatrix;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

/**
 * This class implements the function F^{X} 
 * described in "Graph Drawing by Stress Majorization” by 
 * Emden R. Gansner, Yehuda Koren and Stephen North at 
 * AT&T Labs — Research, Florham Park, NJ 07932, 2005
 * 
 * This function is used to minimize the original stress-function
 * that takes all distances into account.
 * 
 * For the configuration X, passed on construction, 
 * the value is equal to the stress of that configuration.
 * The next better configuration is obtained by minimizing this function,
 * however this function is NOT identical to the actual stress-function.
 * 
 * There is a little workaround in this implementation since F actually 
 * works on matrices, but math3.optim.nonlinear.scalar works with
 * simple vectors. See {@link #value(double[]) value(double[])}} 
 * for further details.
 */
public class AllDistancesBoundedStressFunction implements MultivariateFunction {

	// matrix dimensions required for input and output matrices
	// the values are actually duplicate, since they may be obtained
	// from the intitialLayout matrix, for example
	// but to manifest that these values are the same for all matrices
	// and checked by preconditions they are 
	// stored as private constants here
	private final int n;
	private final int d;
	
	// the constructor ensures that these constants
	// all have the required dimension matches
	private final RealMatrix weights;
	private final RealMatrix distances;
	private final RealMatrix initialLayout;
	
	/**
	 * Creates a new AllDistancesBoundedStressFunction instance,
	 * with the given weights, distances and the current layout.
	 * @param layout The current layout: A two dimensional n*d matrix 
	 * where n is the number of nodes and d is the dimension (typically two).
	 * @param distances The distance matrix (n*n matrix) with distance 
	 * of the nodes i and j at position i,j
	 * @param weights The weight matrix (n*n matrix) with the weight for 
	 * the nodes i and j and position i,j
	 * @throws IllegalArgumentException 
	 */
	public AllDistancesBoundedStressFunction(RealMatrix layout, RealMatrix distances, RealMatrix weights) throws IllegalArgumentException {
		
		// check dimensions match and other required properties
		if (!weights.isSquare()) {
			throw new IllegalArgumentException("weight matrix must be a square matrix.");
		}
		if (!distances.isSquare()) {
			throw new IllegalArgumentException("distance matrix must be a square matrix.");
		}
		if (weights.getRowDimension() != distances.getRowDimension()) {
			throw new IllegalArgumentException("weight and distance matrices need to have the same dimensions.");
		}
		if (weights.getRowDimension() != layout.getRowDimension()) {
			throw new IllegalArgumentException("layout matrix and weight matrix need to have the exact same number of rows.");
		}
		
		this.weights = weights;
		this.distances = distances;
		this.initialLayout = layout;
		
		this.n = weights.getRowDimension();
		this.d = layout.getColumnDimension();
	}

	/**
	 * Calculates the value of the bounded stress function 
	 * described in the paper referenced in the class javadoc.
	 * 
	 * This class is provided for use with the 
	 * apache math optimizer package, therefore it works 
	 * on an input vector. However, the underlying 
	 * bounded stress function is defined for input matrices.
	 * We are currently working around this by viewing the input
	 * vector as a matrix. 
	 * From the beginning of the vector every d elements 
	 * (where d is the column dimension of the initial layout 
	 * matrix passed to the constructor) are seen as a row.
	 * 
	 * As an example: the input <pre> 1, 2, 3, 4, 5, 6, 7, 8 </pre> will be seen as this matrix:
	 * <pre>
	 * 1, 2 <br>
	 * 3, 4 <br>
	 * 5, 6 <br>
	 * 7, 8 
	 * </pre> if the initial layout has two dimensions.
	 */
	@Override
	public double value(double[] suggestedLayout) {
		
		// variable naming after paper, see class javadoc
		RealMatrix X = new VectorMappingMatrix(d, suggestedLayout);
		RealMatrix XT = X.transpose();
		RealMatrix Z = initialLayout;
		RealMatrix LW = calcWeightedLaplacian(weights);
		RealMatrix LZ = calcLZ(weights, distances, Z);
		
		// check input length / transformed matrix dimensions
		if (X.getRowDimension() != n || X.getColumnDimension() != d) {
			throw new IllegalArgumentException("Input vector does not have the required length: input.lenght = " + suggestedLayout.length + " != " + n * d);
		}
		
		// TODO Milestone 3: optimize
		// - cache constant values
		// - replace trace by untransformed equations from paper (probably faster, since not all matrix elements need to be calculated?)
		
		double value = 0;
		for (int j = 0; j < d; j += 1) {
			for (int i = 0; i < j; i += 1) {
				double wij = weights.getEntry(i, j);
				double dij = distances.getEntry(i, j);
				value +=  wij* Math.pow(dij, 2);
				value += (XT.multiply(LW).multiply(X)).getTrace();
				value += -2 * (XT.multiply(LZ).multiply(Z)).getTrace();
			}
		}
		
		return value;
	}

	// MARK: Primitives
	
	/**
	 * Calculates the weighted laplacian matrix as specified
	 * in the paper referenced in the class javadoc.
	 * @param w the weight matrix that should be the basis for the calculated weighted laplacian.
	 * @return the weighted laplacian matrix of the input
	 */
	private RealMatrix calcWeightedLaplacian(RealMatrix w) {
		RealMatrix LW = new Array2DRowRealMatrix(w.getRowDimension(), w.getColumnDimension());
		
		for (int i = 0; i < w.getRowDimension(); i += 1) {
			for (int j = 0; j < w.getColumnDimension(); j += 1) {
				double value = 0;
				if (i != j) {
					value = -w.getEntry(i, j);
				} else {
					for (int k = 0; k < w.getColumnDimension(); k += 1) {
						if (k != i) {
							value += w.getEntry(i, k);
						}
					}
				}
				LW.setEntry(i, j, value);
			}
		}
		
		return LW;
	}

	/**
	 * Calculates the matrix $L^Z$ from a weight matrix, 
	 * a distance matrix and a "Z" matrix.
	 * This kind of matrix is only useful in the context of the paper 
	 * referenced in the class javadoc.
	 * @param w the weight matrix the output matrix will be calculated from.
	 * @param d the distance matrix the output matrix will be calculated from.
	 * @param Z the "Z" matrix the output matrix will be calculated from.
	 * @return The LZ matrix calculated from w,d and Z
	 */
	private RealMatrix calcLZ(RealMatrix w, RealMatrix d, RealMatrix Z) {
		RealMatrix LZ = new Array2DRowRealMatrix(w.getRowDimension(), w.getColumnDimension());
		
		// fill entries that are not on the diagonal first
		double sumOverAllNonDiagonal = 0; // will be used for the diagonal elements
		for (int i = 0; i < LZ.getRowDimension(); i += 1) {
			for (int j = 0; j < LZ.getColumnDimension(); j += 1) {
				if (i != j) {
					double distance = Z.getRowVector(i).subtract(Z.getRowVector(j)).getNorm();
					double value = -weights.getEntry(i, j) * distances.getEntry(i, j) * inv(distance);
					
					LZ.setEntry(i, j, value);
					sumOverAllNonDiagonal += value;
				}
			}
		}
		
		// now fill the entries that are on the diagonal
		// these values are computed from the other values in the matrix
		for (int i = 0; i < LZ.getRowDimension(); i += 1) {
			LZ.setEntry(i, i, -sumOverAllNonDiagonal);
		}
		
		return LZ;
	}

	private double inv(double x) {
		return x != 0 ? 1/x : 0;
	}
	
	// MARK: Utilities
	
	/**
	 * A helper class that makes an input vector accessible as a matrix.
	 * The input vector is seen as a (n*d) matrix, by virtually grouping
	 * every d elements into a row in the matrix.
	 * For further details, please refer to {@link #value(double[]) the value method}.
	 * 
	 * This class only does index transformations and 
	 * does not copy the vector entries.
	 */
	private class VectorMappingMatrix extends AbstractRealMatrix {

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

		@Override
		public RealMatrix copy() {
			return new VectorMappingMatrix(d, Arrays.copyOf(vector, vector.length));
		}

		@Override
		public RealMatrix createMatrix(int arg0, int arg1) throws NotStrictlyPositiveException {
			if (arg0 < 1) {
				throw new NotStrictlyPositiveException(arg0);
			}
			if (arg1 < 1) {
				throw new NotStrictlyPositiveException(arg1);
			}
			
			return new VectorMappingMatrix(d, new double[n * d]);
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
		
	}
	
}
