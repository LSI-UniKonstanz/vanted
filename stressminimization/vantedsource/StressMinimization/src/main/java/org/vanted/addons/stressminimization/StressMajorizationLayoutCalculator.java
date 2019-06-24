package org.vanted.addons.stressminimization;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.linear.BlockRealMatrix;
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

/**
 * Provides stress calculation and stress optimization functionality on basis of a stored layout.
 * Based on the paper
 * "Graph Drawing by Stress Majorization"
 * Emden R. Gansner, Yehuda Koren and Stephen North
 * at AT&T Labs — Research, Florham Park, NJ 07932, 2005
 */
class StressMajorizationLayoutCalculator {

	// matrix dimensions required for input and output matrices
	// fixed here, since needed very often.
	private final int n;
	private final int d;

	// the constructor ensures that these constants
	// all have the required dimension matches
	private final RealMatrix weights;
	private final RealMatrix distances;

	// layout is updated after each optimization round
	private RealMatrix layout;

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

		this.n = weights.getRowDimension();
		this.d = layout.getColumnDimension();

		this.weights = weights;
		this.distances = distances;

		// Initialize constants through entire process
		this.LW = calcWeightedLaplacian(weights);

		setLayout(layout);

	}

	private void setLayout(RealMatrix layout) {

		// all registered nodes needs to be contained in the new layout
		if (this.n != layout.getRowDimension() && this.d != layout.getColumnDimension()) {
			throw new IllegalArgumentException("layout matrix and weight matrix need to have the exact same number of rows.");
		}

		this.layout = layout;
		this.LZ = calcLZ(weights, distances, layout);

	}

	/**
	 * Returns the current layout
	 */
	public RealMatrix getLayout() {
		return this.layout;
	}

	// =====================
	// MARK: "cached" values
	// =====================
	// this section contains variables that are constant through the entire optimization process
	// or during one optimization iteration.
	// naming is according to paper

	// constant through whole process:
	private final RealMatrix LW;

	// updated for each new layout:
	private RealMatrix LZ;

	// ========================
	// MARK: stress calculation
	// ========================

	/**
	 * Calculates the stress for the stored layout using the formula
	 * $$
	 * stress(X) = \sum_{i<j} wij * (||Xi - Xj|| - dij)^2
	 * $$
	 * @return The stress of the current layout.
	 */
	public double calcStress() {

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

	/**
	 * Optimizes the stored layout and updates it to the optimized version.
	 * @return
	 */
	public RealMatrix calcOptimizedLayout() {
		// RealMatrix optimizedLayout = localizedOptimizationLayout();
		RealMatrix optimizedLayout = conjugateGradientLayout();
		setLayout(optimizedLayout);
		return optimizedLayout;
	}

	// ===========================================
	// Implementation using localized optimization
	// ===========================================

	private RealMatrix localizedOptimizationLayout() {

		RealMatrix X = layout.copy();

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

				// if no node was visisted, denominator will be 0,
				// but we don't want to set the coordinate to NaN (result of division by zero)
				double value = denominator != 0 ? numerator / denominator : 0;
				Xi.setEntry(a, value);
			}
			X.setRowVector(i, Xi);
		}

		return X;
	}

	// =======================================
	// Implementation using Conjugate Gradient
	// =======================================
	// IMPORTANT: buggy implementation!

	private RealMatrix conjugateGradientLayout() {

		// the values were chosen based on experience,
		// mainly with sierpinsky 6
		final double CONVERGENCE_EPSILON = 1e-3;
		final double LINE_SEARCH_EPSILON = 1e-2;

		NonLinearConjugateGradientOptimizer optimizer = new NonLinearConjugateGradientOptimizer(
				NonLinearConjugateGradientOptimizer.Formula.POLAK_RIBIERE,
				// these values
				new SimpleValueChecker(CONVERGENCE_EPSILON, CONVERGENCE_EPSILON),
				LINE_SEARCH_EPSILON, LINE_SEARCH_EPSILON, LINE_SEARCH_EPSILON
				);

		RealMatrix newLayout = layout.createMatrix(n, d);
		EquationSystemOptimizationFunctionSupplier supplier = new EquationSystemOptimizationFunctionSupplier();

		for (int a = 0; a < d; a += 1) {

			PointValuePair minimum = optimizer.optimize(
					new MaxEval(Integer.MAX_VALUE),
					new MaxIter(Integer.MAX_VALUE),
					new InitialGuess(layout.getColumn(a)),
					new ObjectiveFunction(supplier.getObjectiveFunction(a)),
					new ObjectiveFunctionGradient(supplier.getGradient(a)),
					GoalType.MINIMIZE
					);

			newLayout.setColumn(a, minimum.getPoint());

		}

		return newLayout;
	}

	/**
	 * This class implements a function used to solve the equations system
	 * LW*X = LZ * Z
	 * in each dimension of X using the conjugate gradient optimization method.
	 */
	private class EquationSystemOptimizationFunctionSupplier {

		private RealMatrix LZZ;
		public EquationSystemOptimizationFunctionSupplier() {
			this.LZZ = LZ.multiply(layout);
		}

		public MultivariateFunction getObjectiveFunction(int dimension) {
			return new ObjectiveFunction(dimension);
		}

		public MultivariateVectorFunction getGradient(int dimension) {
			return new Gradient(dimension);
		}

		private class ObjectiveFunction implements MultivariateFunction {

			private RealMatrix LZZaT;

			public ObjectiveFunction(int a) {
				this.LZZaT = LZZ.getColumnMatrix(a).transpose();
			}

			/**
			 * Calculates the value of a function whose gradient is LW * Xa - LZ^T * Za
			 */
			@Override
			public double value(double[] suggestedLayout) {

				// variable naming after paper, see class javadoc
				RealMatrix Xa = new BlockRealMatrix(n, 1);
				Xa.setColumn(0, suggestedLayout);

				// 1/2 * Xa^T * LW * Xa - (LZ*Za)^T * Xa
				return 0.5 * (Xa.transpose().multiply(LW).multiply(Xa)).getEntry(0, 0) - ( LZZaT.multiply(Xa) ).getEntry(0, 0);

			}

		}

		// MARK: Gradient function
		private class Gradient implements MultivariateVectorFunction {

			private RealMatrix LZZa;

			public Gradient(int a) {
				this.LZZa = LZZ.getColumnMatrix(a);
			}

			@Override
			public double[] value(double[] suggestedLayout) throws IllegalArgumentException {

				// variable naming after paper, see class javadoc
				RealMatrix Xa = new BlockRealMatrix(n, 1);
				Xa.setColumn(0, suggestedLayout);

				RealMatrix result = ( LW.multiply(Xa) ).subtract( LZZa );
				return result.getColumn(0);
			}

		}

	}

	// ================
	// MARK: Primitives
	// ================

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
		RealMatrix LW = w.createMatrix(n, n);

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
		RealMatrix LZ = w.createMatrix(n, n);

		for (int i = 0; i < LZ.getRowDimension(); i += 1) {
			double sumOverAllNonDiagonal = 0; // will be used for the diagonal element
			for (int j = 0; j < LZ.getColumnDimension(); j += 1) {
				if (i != j) {
					double distance = Z.getRowVector(i).getDistance(Z.getRowVector(j));
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

