package org.vanted.addons.stressminimization;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.CholeskyDecomposition;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleValueChecker;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunctionGradient;
import org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizer;


class StressMajorizationLayoutCalculator {

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
	 * Creates a new StressMajorizationLayoutCalculator instance,
	 * with the given weights, distances and the current layout.
	 * @param layout The current or initial layout: A two dimensional n*d matrix 
	 * where n is the number of nodes and d is the dimension (typically two).
	 * @param distances The distance matrix (n*n matrix) with distance 
	 * of the nodes i and j at position i,j
	 * @param weights The weight matrix (n*n matrix) with the weight for 
	 * the nodes i and j and position i,j
	 * @throws IllegalArgumentException 
	 */
	public StressMajorizationLayoutCalculator(RealMatrix layout, RealMatrix distances, RealMatrix weights) throws IllegalArgumentException {
		
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
	 * Calculates the stress for the given layout using the formula
	 * $$
	 * stress(X) = \sum_{i<j} wij * (||Xi - Xj|| - dij)^2
	 * $$
	 * @return The layout the stress will be calculated for.
	 */
	public double calcStress(RealMatrix layout) {

		double stress = 0;
		for (int i = 0; i < n; i += 1) {
			for (int j = i + 1; j < n; j += 1) {
				double wij = weights.getEntry(i, j);
				double dij = distances.getEntry(i, j);
				double norm = layout.getRowVector(i).subtract(layout.getRowVector(j)).getNorm();
				stress += wij * Math.pow(norm - dij, 2);
			}
		}
		return stress;
		
	}

	// MARK: Layout calculation
	
	public RealMatrix calcOptimizedLayout() {
		// to define test, we use the localized process to define test cases, etc.
		return localizedOptimizationLayout();
	}
	
	// ===========================================
	// Implementation using localized optimization
	// ===========================================
	
	private RealMatrix localizedOptimizationLayout() {
		RealMatrix X = initialLayout.copy();


		for (int i = 0; i < n; i += 1) {
			RealVector Xi = X.getRowVector(i);
			for (int a = 0; a < d; a += 1) {
				double numerator = 0;
				double denominator = 0;
				for (int j = 0; j < n; j += 1) {
					if (j != i) {
						
						// v = wij * (Xja + ((dij * (Xia - Xja)) * inv(||Xi - Xj||)))
						double v;
						v  = distances.getEntry(i, j);
						v *= X.getEntry(i, a) - X.getEntry(j, a);
						v *= inv( X.getRowVector(i).subtract(X.getRowVector(j)).getNorm() ); // getNorm calculates the L2 Norm in this case
						v += X.getEntry(j, a);
						v *= weights.getEntry(i, j);
						
						numerator += v;
						denominator += weights.getEntry(i, j);
						
					}
				}
				
				double value = numerator / denominator;
				Xi.setEntry(a, value);
			}
			X.setRowVector(i, Xi);
		}
		
		return X;
	}
	
	// ===========================================
	// Implementation using Cholesky Factorization
	// ===========================================
	// IMPORTANT: buggy implementation!
	
	private RealMatrix choleskyFactorizationLayout() {

		/*
		// fix first row as in paper (X_1 := 0)
		layout.setRow(0, new double[] {0, 0});
		*/
		
		RealMatrix Z = initialLayout;
		RealMatrix LW = calcWeightedLaplacian(weights);
		RealMatrix LZ = calcLZ(weights, distances, Z);
		
		RealMatrix X = new VectorMappingMatrix(n-1, d); // X1/X0 is fixed
		
		// as in paper, we remove the first row and column of LW
		// and the first row of LZ
		
		LW = LW.getSubMatrix(1, n-1, 1, n-1);
		
		// solve equation system LW*X = LZ*Z for each dimension
		for (int a = 0; a < d; a += 1) {
			
			DecompositionSolver solver = new CholeskyDecomposition(LW).getSolver();
			RealMatrix Za = Z.getColumnMatrix(a);
			RealMatrix LZZa = LZ.multiply(Za);
			LZZa = LZZa.getSubMatrix(1, n-1, 0, 0);
			RealVector Xa = solver.solve(LZZa.getColumnVector(0));
			
			X.setColumnVector(a, Xa);
		}
		
		// we add the first vector again (which has value 0)
		RealMatrix fullX = Z.copy();
		fullX.setSubMatrix(X.getData(), 1, 0);
		return fullX;
		
	}

	// =======================================
	// Implementation using Conjugate Gradient
	// =======================================
	// IMPORTANT: buggy implementation!
	
	private VectorMappingMatrix conjugateGradientLayout() {

		/*
		// fix first row as in paper (X_1 := 0)
		layout.setRow(0, new double[] {0, 0});
		*/
		
		final double EPSILON = 1e-4;
		
		NonLinearConjugateGradientOptimizer optimizer = new NonLinearConjugateGradientOptimizer(
				NonLinearConjugateGradientOptimizer.Formula.POLAK_RIBIERE, 
				new SimpleValueChecker(EPSILON, EPSILON) // TODO choose better one, probably set maxItter
		);

		BoundedStressFunction F = new BoundedStressFunction();
		
		PointValuePair minimum = optimizer.optimize(
				new MaxEval(Integer.MAX_VALUE),
				new MaxIter(Integer.MAX_VALUE),
				new InitialGuess(VectorMappingMatrix.asVectorMappingMatrix(initialLayout).getVector()),
				new ObjectiveFunction(F), 
				new ObjectiveFunctionGradient(F.getGradient()),
				GoalType.MINIMIZE
		);
		
		return new VectorMappingMatrix(d, minimum.getPoint());
	}
	
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
	private class BoundedStressFunction implements MultivariateFunction {

		/**
		 * Calculates the stress of the initial layout passed to the constructor.
		 * @return stress of the initial layout.
		 */
		public double getInitialLayoutStress() {
			// stress of initial layout is the bounded layout function
			// of the initial layout, see paper, page 4.
			return calc(VectorMappingMatrix.asVectorMappingMatrix(initialLayout));
		}

		// TODO check up the whole math in the below
		// I'm unsure about a few things
		// 1) Not differentiating by dimensions but just putting everything into one vector
		// 2) the gradient... is this the used function really the gradient?
		
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
			RealMatrix Z = initialLayout;
			RealMatrix LW = calcWeightedLaplacian(weights);
			RealMatrix LZ = calcLZ(weights, distances, Z);
			
			// TODO Milestone 3: optimize
			// - cache constant values
			// - replace trace by untransformed equations from paper (probably faster, since not all matrix elements need to be calculated?)
			
			double value = 0;
			for (int i = 0; i < n; i += 1) {
				for (int j = i + 1; j < d; j += 1) {
					double wij = weights.getEntry(i, j);
					double dij = distances.getEntry(i, j);
					value +=  wij* Math.pow(dij, 2);
				}
			}
			value += (XT.multiply(LW).multiply(X)).getTrace();
			value += -2 * (XT.multiply(LZ).multiply(Z)).getTrace();
			
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

				RealMatrix Z = initialLayout;
				RealMatrix LW = calcWeightedLaplacian(weights);
				RealMatrix LZ = calcLZ(weights, distances, Z);
				
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
		
	}
	
	// MARK: Primitives

	private double inv(double x) {
		return x != 0 ? 1/x : 0;
	}
	
	/**
	 * Calculates the weighted laplacian matrix as specified
	 * in the paper referenced in the class javadoc.
	 * @param w the weight matrix that should be the basis for the calculated weighted laplacian.
	 * @return the weighted laplacian matrix of the input
	 */
	private RealMatrix calcWeightedLaplacian(RealMatrix w) {
		RealMatrix LW = new Array2DRowRealMatrix(n, n);
		
		for (int i = 0; i < LW.getRowDimension(); i += 1) {
			for (int j = 0; j < LW.getColumnDimension(); j += 1) {
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
		RealMatrix LZ = new Array2DRowRealMatrix(n, n);
		
		// IMPORTANT
		// this formula deviates from the formula in the paper referenced in class javadoc
		// the updates formula is from https://www.sciencedirect.com/science/article/pii/S0012365X08000083
		// and matches with the implementation in graphviz,
		// referenced in the paper referenced in class javadoc
		
		// TODO check this in paper!
		
		// fill entries that are not on the diagonal first
		for (int i = 0; i < LZ.getRowDimension(); i += 1) {
			double sumOverAllNonDiagonal = 0; // will be used for the diagonal element
			for (int j = 0; j < LZ.getColumnDimension(); j += 1) {
				if (i != j) {
					double distance = Z.getRowVector(i).subtract(Z.getRowVector(j)).getNorm();
					double value = -w.getEntry(i, j) * d.getEntry(i, j) * inv(distance);
					
					LZ.setEntry(i, j, value);
					sumOverAllNonDiagonal += value;
				}
			}
			LZ.setEntry(i, i, -sumOverAllNonDiagonal);
		}
		
		return LZ;
	}

}

