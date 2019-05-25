package org.vanted.addons.stressminimization;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.linear.RealMatrix;

/**
 * This class implements the function F^{X} 
 * described in "Graph Drawing by Stress Majorization” by 
 * Emden R. Gansner, Yehuda Koren and Stephen North at 
 * AT&T Labs — Research, Florham Park, NJ 07932, 2005
 * 
 * This function is used to minimize the original stress function
 * that takes all distances into account.
 * 
 * For the configuration X(t), passed on construction, 
 * the value is equal to the stress of that configuration.
 * The next better configuration is obtained by minimizing this function,
 * however this function is NOT identical to the actual stress-function.
 * 
 * There is a little workaround in this implementation since F actually 
 * works on matrices, but math3.optim.nonlinear.scalar works with
 * simple vectors. See {@link #value(double[]) value(double[])}} 
 * for further details.
 */
class AllDistancesBoundedStressFunction implements MultivariateFunction {

	// TODO check up the whole math in this class
	// I'm unsure about a few things
	// 1) Not differentiating by dimensions but just putting everything into one vector
	// 2) the gradient... is this the used function really the gradient?
	
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
	private final VectorMappingMatrix weights;
	private final VectorMappingMatrix distances;
	private final VectorMappingMatrix initialLayout;
	
	/**
	 * Creates a new AllDistancesBoundedStressFunction instance,
	 * with the given weights, distances and the current layout.
	 * @param layout The current or initial layout: A two dimensional n*d matrix 
	 * where n is the number of nodes and d is the dimension (typically two).
	 * @param distances The distance matrix (n*n matrix) with distance 
	 * of the nodes i and j at position i,j
	 * @param weights The weight matrix (n*n matrix) with the weight for 
	 * the nodes i and j and position i,j
	 * @throws IllegalArgumentException 
	 */
	public AllDistancesBoundedStressFunction(VectorMappingMatrix layout, VectorMappingMatrix distances, VectorMappingMatrix weights) throws IllegalArgumentException {
		
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
	 * Calculates the stress of the initial layout passed to the constructor.
	 * @return stress of the initial layout.
	 */
	public double getInitialLayoutStress() {
		// stress of initial layout is the bounded layout function
		// of the initial layout, see paper, page 4.
		return calc(initialLayout);
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
		VectorMappingMatrix X = new VectorMappingMatrix(d, suggestedLayout);
		
		try {
			return calc(X);
		} catch (IllegalArgumentException ex) {
			throw new IllegalArgumentException("Input vector does not have the required length: input.lenght = " + suggestedLayout.length + " != " + n * d, ex);
		}
	}

	/**
	 * Calculates the bounded stress function for the given layout X.
	 * @param X the input layout matrix.
	 * @return $F^Z$(X) (see paper referenced in class javadoc, page 3).
	 * @throws IllegalArgumentException if the input matrix isn't a n*d matrix.
	 */
	private double calc(VectorMappingMatrix X) throws IllegalArgumentException {

		// check input length / transformed matrix dimensions
		if (X.getRowDimension() != n || X.getColumnDimension() != d) {
			throw new IllegalArgumentException("Misdimensioned input matrix. Expected Dimensions: " + n + "*" + d + ", actuall dimensions:" + X.getRowDimension() + "*" + X.getColumnDimension());
		}

		RealMatrix XT = X.transpose();
		VectorMappingMatrix Z = initialLayout;
		VectorMappingMatrix LW = calcWeightedLaplacian(weights);
		VectorMappingMatrix LZ = calcLZ(weights, distances, Z);
		
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
	
	// MARK: Gradient function
	private class Gradient implements MultivariateVectorFunction {

		@Override
		public double[] value(double[] suggestedLayout) throws IllegalArgumentException {

			// variable naming after paper, see class javadoc
			VectorMappingMatrix X = new VectorMappingMatrix(d, suggestedLayout);
			
			try {
				// TODO we need to return the result the same form as the input.
				return calc(X).getVector();
			} catch (IllegalArgumentException ex) {
				throw new IllegalArgumentException("Input vector does not have the required length: input.lenght = " + suggestedLayout.length + " != " + n * d, ex);
			}
		}
		
		private VectorMappingMatrix calc(VectorMappingMatrix X) throws IllegalArgumentException {

			// check input length / transformed matrix dimensions
			if (X.getRowDimension() != n || X.getColumnDimension() != d) {
				throw new IllegalArgumentException("Misdimensioned input matrix. Expected Dimensions: " + n + "*" + d + ", actuall dimensions:" + X.getRowDimension() + "*" + X.getColumnDimension());
			}

			VectorMappingMatrix Z = initialLayout;
			VectorMappingMatrix LW = calcWeightedLaplacian(weights);
			VectorMappingMatrix LZ = calcLZ(weights, distances, Z);
			
			// result is a n*d matrix
			RealMatrix value = ( LZ.multiply(Z) ).subtract( LW.multiply(X) );
			
			// without knowing the internals of AbstractRealMatrix,
			// blind casting does not seem safe for me.
			// therefore we eventually copy the matrix.
			return VectorMappingMatrix.asVectorMappingMatrix(value);
			
		}
		
	}

	public MultivariateVectorFunction getGradient() {
		return new Gradient();
	}
	
	// MARK: Primitives
	
	/**
	 * Calculates the weighted laplacian matrix as specified
	 * in the paper referenced in the class javadoc.
	 * @param w the weight matrix that should be the basis for the calculated weighted laplacian.
	 * @return the weighted laplacian matrix of the input
	 */
	private VectorMappingMatrix calcWeightedLaplacian(VectorMappingMatrix w) {
		VectorMappingMatrix LW = new VectorMappingMatrix(w.getRowDimension(), w.getColumnDimension());
		
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
	private VectorMappingMatrix calcLZ(VectorMappingMatrix w, VectorMappingMatrix d, VectorMappingMatrix Z) {
		VectorMappingMatrix LZ = new VectorMappingMatrix(w.getRowDimension(), w.getColumnDimension());
		
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

	
}
