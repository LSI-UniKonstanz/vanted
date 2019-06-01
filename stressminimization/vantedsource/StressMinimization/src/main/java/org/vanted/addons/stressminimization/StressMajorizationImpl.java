package org.vanted.addons.stressminimization;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.OptimizationData;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleValueChecker;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunctionGradient;
import org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizer;
import org.graffiti.graph.Graph;

/**
 * This class implements the stress majorization process 
 * described in "Graph Drawing by Stress Majorization” by 
 * Emden R. Gansner, Yehuda Koren and Stephen North at 
 * AT&T Labs — Research, Florham Park, NJ 07932, 2005
 * that uses the original stress function that
 * takes the distances between all nodes into account.
 */
class StressMajorizationImpl {

	private final double EPSILON = 1E-4;
	
	private final Graph g;
	private BackgroundExecutionAlgorithm bea;
	public StressMajorizationImpl(Graph g) {
		this.g = g;
	}
	
	void calculateLayout() {

		int n = g.getNumberOfNodes();
		int d = 2; // TODO where can we get this information from?
		
		VectorMappingMatrix distances = calcDistances();
		VectorMappingMatrix weights = getWeightsForDistances(distances, 2); // TODO make alpha selectable by user
		
		VectorMappingMatrix layout = new VectorMappingMatrix(n, d); // TODO
		// variable naming according to paper 
		AllDistancesBoundedStressFunction F = new AllDistancesBoundedStressFunction(layout, distances, weights);
		double prevStress = F.getInitialLayoutStress();
		double newStress;

		NonLinearConjugateGradientOptimizer optimizer = new NonLinearConjugateGradientOptimizer(
				NonLinearConjugateGradientOptimizer.Formula.POLAK_RIBIERE, 
				new SimpleValueChecker(EPSILON, EPSILON) // TODO choose better one, probably set maxItter
		);

		do {
			
			PointValuePair minimum = optimizer.optimize(
					// further data might be registered
					// probably interesting: MaxEval, MaxIter, 
					new InitialGuess(layout.getVector()),
					new ObjectiveFunction(F), 
					new ObjectiveFunctionGradient(F.getGradient()),
					GoalType.MINIMIZE
			);
			
			layout = new VectorMappingMatrix(d, minimum.getPoint());
			
			F = new AllDistancesBoundedStressFunction(layout, distances, weights);
			newStress = F.getInitialLayoutStress();

		} while ( (prevStress - newStress) / prevStress >= EPSILON );
		
		// TODO update layout
		
	}
	
	/**
	 * Calculates the distance matrix of the given graph.
	 * @param g The graph which distance matrix will be calculated
	 * @return the distance matrix
	 */
	private VectorMappingMatrix calcDistances() {
		// TODO implement, for now we are just returning uniform distances (which is really bad)
		int n = g.getNumberOfNodes();
		VectorMappingMatrix distances = new VectorMappingMatrix(n, n);
		for (int i = 0; i < distances.getRowDimension(); i += 1) {
			for (int j = 0; j < distances.getColumnDimension(); j += 1) {
				if (i == j) {
					distances.setEntry(i, j, 0);
				} else {
					distances.setEntry(i, j, 1);
				}
			}
		}
		return distances;
	}
	
	/**
	 * Computes default weights from the given distance matrix, using the simple formula <br>
	 * $$
	 * w_{ij} := d_{ij}^{-\alpha}
	 * $$
	 * @return the calculated weight matrix
	 */
	private VectorMappingMatrix getWeightsForDistances(VectorMappingMatrix distances, int alpha) {
		VectorMappingMatrix weights = new VectorMappingMatrix(distances.getRowDimension(), distances.getColumnDimension());
		for (int i = 0; i < weights.getRowDimension(); i += 1) {
			for (int j = 0; j < weights.getColumnDimension(); j += 1) {
				double wij = Math.pow(distances.getEntry(i, j), -alpha);
				weights.setEntry(i, j, wij);
			}
		}
		return weights;
	}
	
}
