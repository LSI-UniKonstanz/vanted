package org.vanted.addons.stressminimization;

import java.util.Arrays;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.linear.AbstractRealMatrix;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizer;

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

	// TODO define methods that return n and d + replace w.getRowDimension(), etc...
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
	 */
	public AllDistancesBoundedStressFunction(RealMatrix layout, RealMatrix distances, RealMatrix weights) {
		this.weights = weights;
		this.distances = distances;
		this.initialLayout = layout;
	}
	
	/**
	 * Calculates 
	 * 
	 * There is a little workaround in this function
	 * since {@link org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizer the conjugate gradient optimizer}
	 * works on vectors, but the function from the paper is 
	 * defined for matrices. 
	 * To work around this, the input vector is transformed into 
	 * a (n*d) matrix. 
	 * The nodes' coordinates are seen as written in a row
	 * in the input vector.
	 * This means concretely, that the input 
	 * {@code 1, 2, 3, 4, 5, 6, 7, 8} will be transformed into this matrix
	 * {@code
	 * 1, 2
	 * 3, 4
	 * 5, 6
	 * 7, 8
	 * }
	 * if the input layout is two-dimensional
	 */
	@Override
	public double value(double[] suggestedLayout) {
		
		int n = initialLayout.getRowDimension();
		int d = initialLayout.getColumnDimension();
		
		// variable naming after paper, see class javadoc
		RealMatrix X = new VectorMatrix(d, suggestedLayout);
		RealMatrix XT = X.transpose();
		RealMatrix Z = initialLayout;
		RealMatrix LW = calcWeightedLaplacian(weights);// TODO
		RealMatrix LZ = calcLZ(weights, distances, Z); // TODO
		
		// TODO M3: optimize
		// - cache constant values
		// - replace trace by untransformed equations from paper (probably faster?)
		
		double value = 0;
		for (int j = 0; j < d; j += 1) {
			for (int i = 0; i < j; i += 1) {
				double wij = weights.getEntry(i, j);
				double dij = distances.getEntry(i, j);
				value +=  wij* Math.pow(dij, 2);
				value += (XT.multiply(LW).multiply(X)).getTrace();
				value -= 2 * (XT.multiply(LZ).multiply(Z)).getTrace();
			}
		}
		
		return value;
	}

	/**
	 * TODO
	 * @param w
	 * @return
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
	 * TODO
	 * @param weights2
	 * @param distances2
	 * @param z
	 * @return
	 */
	private RealMatrix calcLZ(RealMatrix w, RealMatrix d, RealMatrix Z) {
		// delta = wij*dij
		RealMatrix LZ = new Array2DRowRealMatrix(w.getRowDimension(), w.getColumnDimension());
		
		// fill entries that are not on the diagonal first
		for (int i = 0; i < LZ.getRowDimension(); i += 1) {
			for (int j = 0; j < LZ.getColumnDimension(); j += 1) {
				if (i != j) {
					double value = -weights.getEntry(i, j) * distances.getEntry(i, j) * inv(distance(Z.getRowMatrix(i).subtract(Z.getRowMatrix(j))));
					LZ.setEntry(i, j, value);
				}
			}
		}
		
	}

	// the input needs to have a single row. all other rows will be ignored
	private double distance(RealMatrix vector) {
		double value = 0;
		// TODO 
	}

	private double inv(double x) {
		return x != 0 ? 1/x : 0;
	}
	
	/**
	 * TODO
	 */
	private class VectorMatrix extends AbstractRealMatrix {

		int n, d;
		/*
		 * This vector is seen as a (n*d) matrix. 
		 * With the rows put behind each other.
		 * This means concretely, that the input 
		 * {@code 1, 2, 3, 4, 5, 6, 7, 8} is seen as the matrix
		 * {@code
		 * 1, 2
		 * 3, 4
		 * 5, 6
		 * 7, 8
		 * }
		 * if d = 2.
		 */
		double[] vector;
		
		/**
		 * TODO length requirement
		 * @param d
		 * @param vector
		 */
		public VectorMatrix(int d, double[] vector) {
			super();
			this.n = vector.length / d;
			this.d = d;
			this.vector = vector;
		}

		@Override
		public RealMatrix copy() {
			return new VectorMatrix(d, Arrays.copyOf(vector, vector.length));
		}

		@Override
		public RealMatrix createMatrix(int arg0, int arg1) throws NotStrictlyPositiveException {
			if (arg0 < 1) {
				throw new NotStrictlyPositiveException(arg0);
			}
			if (arg1 < 1) {
				throw new NotStrictlyPositiveException(arg1);
			}
			
			return new VectorMatrix(d, new double[n * d]);
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
