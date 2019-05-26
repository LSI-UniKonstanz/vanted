package org.vanted.addons.stressminimization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.Vector2d;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.OptimizationData;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleValueChecker;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunctionGradient;
import org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizer;
import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.sbml.jsbml.ext.arrays.test.ArraysWriteTest;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;

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
	public StressMajorizationImpl(Graph g) {
		this.g = g;
	}
	
	void doLayout() {

		List<Node> nodes = g.getNodes();
		int n = nodes.size();
		final int d = 2; // only implemented for two dimensional space
		
		RealMatrix distances = calcDistances();
		RealMatrix weights = getWeightsForDistances(distances, 2); // TODO make alpha selectable by user
		
		// TODO get the current layout. We now just use a random layout...
		RealMatrix layout = getRandomMatrix(n, d); 
		
		double prevStress, newStress;
		do {

			StressMajorizationLayoutCalculator c = new StressMajorizationLayoutCalculator(layout, distances, weights);
			prevStress = c.calcStress(layout);
			
			layout = c.calcOptimizedLayout();
			newStress = c.calcStress(layout);
			
			System.out.println("p: " + prevStress);
			System.out.println("n: " + newStress);
			System.out.println("d: " + ((prevStress - newStress) / prevStress) + "; " + ((prevStress - newStress) / prevStress >= EPSILON));
			
		} while ( (prevStress - newStress) / prevStress >= EPSILON );
		
		// guarantee that no nodes are outside the window
		double minX = Double.POSITIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		for (int i = 0; i < n; i += 1) {
			double[] pos = layout.getRow(i);
			minX = pos[0] < minX ? pos[0] : minX;
			minY = pos[1] < minY ? pos[1] : minY;
		}

		double offsetX = -minX + 50;
		double offsetY = -minY + 50;
		double scaleFactor = 100;
		
		HashMap<Node, Vector2d> nodes2newPositions = new HashMap<Node, Vector2d>();
		for (int i = 0; i < n; i += 1) {
			double[] pos = layout.getRow(i);
			Vector2d position = new Vector2d(offsetX + pos[0] * scaleFactor, 
											 offsetY + pos[1] * scaleFactor);
			nodes2newPositions.put(nodes.get(i), position);
		}
		
		GraphHelper.applyUndoableNodePositionUpdate(nodes2newPositions, "Stress Majorization");
		
	}
	
	/**
	 * Calculates the distance matrix of the given graph.
	 * @param g The graph which distance matrix will be calculated
	 * @return the distance matrix
	 */
	private RealMatrix calcDistances() {
		
		int n = g.getNumberOfNodes();
		
		Map<Node, Integer> node2Index = new HashMap<>();
		int nextFreeIndex = 0;
		for (Node node : g.getNodes()) {
			node2Index.put(node, nextFreeIndex);
			nextFreeIndex += 1;
		}
		
		RealMatrix distances = new Array2DRowRealMatrix(n, n);
		
		// Floyd–Warshall algorithm
		
		for (int i = 0; i < n; i += 1) {
			for (int j = 0; j < n; j += 1) {
				distances.setEntry(i, j, Double.POSITIVE_INFINITY);
			}
		}

		// all edges are guaranteed to be undirected (algorithm precondition)
		for (Edge edge : g.getEdges()) {
			double edgeWeight;
			try {
				// TODO find out how to really get the edge weight
				edgeWeight = edge.getDouble("weight");
			} catch (AttributeNotFoundException ex) {
				edgeWeight = 1; // use uniform weight
			}
			
			int i = node2Index.get(edge.getSource());
			int j = node2Index.get(edge.getTarget());
			
			distances.setEntry(i, j, edgeWeight);
			distances.setEntry(j, i, edgeWeight); // non directed edges
		}
		
		for (int i = 0; i < n; i += 1) {
			distances.setEntry(i, i, 0);
		}
		
		for (int k = 0; k < n; k += 1) {
			for (int i = 0; i < n; i += 1) {
				for (int j = 0; j < n; j += 1) {
					double dij = distances.getEntry(i, j);
					double comp = distances.getEntry(i, k) + distances.getEntry(k, j);
					if (dij > comp) {
						distances.setEntry(i, j, comp);
					}
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
	private RealMatrix getWeightsForDistances(RealMatrix distances, int alpha) {
		RealMatrix weights = new Array2DRowRealMatrix(distances.getRowDimension(), distances.getColumnDimension());
		for (int i = 0; i < weights.getRowDimension(); i += 1) {
			for (int j = 0; j < weights.getColumnDimension(); j += 1) {
				double wij = Math.pow(distances.getEntry(i, j), -alpha);
				weights.setEntry(i, j, wij);
			}
		}
		return weights;
	}
	
	// FIXME pure testing utilities
	
	private RealMatrix getRandomMatrix(int n, int d) {
		RealMatrix m = new Array2DRowRealMatrix(n, d);
		for (int i = 0; i < n; i += 1) {
			for (int j = 0; j < d; j += 1) {
				double value = Math.random();
				m.setEntry(i, j, value);
			}
		}
		return m;
	}
	
	private RealMatrix getRandomSymetrixMatrixWith0Diagonal(int n) {
		RealMatrix m = new Array2DRowRealMatrix(n, n);
		for (int i = 0; i < n; i += 1) {
			for (int j = 0; j <= i; j += 1) {
				if (i == j) {
					m.setEntry(i, j, 0.0);
				} else {
					double value = Math.random();
					m.setEntry(i, j, value);
					m.setEntry(j, i, value);
				}
			}
		}
		return m;
	}
	
}
