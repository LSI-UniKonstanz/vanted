package org.vanted.addons.stressminimization;

import java.awt.geom.Point2D;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.AttributeHelper;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.view.View;

/**
 * This class implements the stress majorization process 
 * described in "Graph Drawing by Stress Majorization” by 
 * Emden R. Gansner, Yehuda Koren and Stephen North at 
 * AT&T Labs — Research, Florham Park, NJ 07932, 2005
 * that uses the original stress function that
 * takes the distances between all nodes into account.
 */
class StressMajorizationImpl extends BackgroundAlgorithm {

	private final double EPSILON = 1E-4;
	
	private final Graph g;
	public StressMajorizationImpl(Graph g, PropertyChangeListener[] pcl) {
		this.g = g;
		this.addPropertyChangeListeners(pcl);
	}
	
	void doLayout() {

		List<Node> nodes = g.getNodes();
		int n = nodes.size();
		final int d = 2; // only implemented for two dimensional space

		System.out.println("Calculating distances...");
		RealMatrix distances = calcDistances();
		System.out.println("Calculating weights...");
		RealMatrix weights = getWeightsForDistances(distances, 2); // TODO make alpha selectable by user
		
		System.out.println("Copying layout...");
		RealMatrix layout = new Array2DRowRealMatrix(n, d); //getRandomMatrix(n, d); 
		for (int i = 0; i < n; i += 1) {
			Point2D position = AttributeHelper.getPosition(nodes.get(i));
			layout.setRow(i, new double[] {position.getX(), position.getY()});
		}

		System.out.println("Optimizing layout...");
		double prevStress, newStress;
		do {

			StressMajorizationLayoutCalculator c = new StressMajorizationLayoutCalculator(layout, distances, weights);
			prevStress = c.calcStress(layout);
			
			layout = c.calcOptimizedLayout();
			newStress = c.calcStress(layout);

			System.out.println("===============================");
			System.out.println("prev: " + prevStress);
			System.out.println("new:  " + newStress);
			System.out.println("diff: " + ((prevStress - newStress) / prevStress) + "; " + ((prevStress - newStress) / prevStress >= EPSILON));
			
			//update GUI layout
			setLayout(layout);
			
			//TODO stop algorithm if button stop clicked
			//BackgroundExecutionAlgorithm b =(BackgroundExecutionAlgorithm)((Object) getPropertyChangeListener()[0]);
			//b.isStopButtonClick();
			
		} while ( (prevStress - newStress) / prevStress >= EPSILON ); // TODO: offer choice between change limit and number of iterations, offer choices of epsilon
		

		System.out.println("Updating layout...");
		setEndLayout(layout);
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

	@Override
	public boolean activeForView(View v) {
		return false;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public void execute() {
		doLayout();
	}
}
