package org.vanted.addons.stressminimization;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.AttributeHelper;
import org.Vector2d;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
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
	
	private final List<Node> nodes;
	
	/**
	 * Constructs a new StressMajorizationImpl instance that works on the given set of nodes
	 * @param nodes The nodes this instance will work on.
	 */
	public StressMajorizationImpl(Set<Node> nodes) {
		this.nodes = new ArrayList<>(nodes);
	}
	
	@Override
	public void execute() {
		calculateLayout();
	}
	
	/**
	 * Calculates a approximately optimal layout for the set of nodes that was passed on construction 
	 * in regard to layout stress.
	 * @return A new layout of the nodes of this instance that has close to minimal stress.
	 */
	public Map<Node, Vector2d> calculateLayout() {

		// enable or disable console logging
		final boolean LOG = false;
		
		int n = nodes.size();
		final int d = 2; // only implemented for two dimensional space

		if (LOG) { System.out.println("Calculating distances..."); }
		RealMatrix distances = calcDistances();
		if (LOG) { System.out.println("Calculating weights..."); }
		RealMatrix weights = getWeightsForDistances(distances, 2); // TODO make alpha selectable by user
		
		if (LOG) { System.out.println("Copying layout..."); }
		RealMatrix layout = new Array2DRowRealMatrix(n, d); //getRandomMatrix(n, d); 
		for (int i = 0; i < n; i += 1) {
			Point2D position = AttributeHelper.getPosition(nodes.get(i));
			layout.setRow(i, new double[] {position.getX(), position.getY()});
		}

		if (LOG) { System.out.println("Optimizing layout..."); }
		double prevStress, newStress;
		do {

			StressMajorizationLayoutCalculator c = new StressMajorizationLayoutCalculator(layout, distances, weights);
			prevStress = c.calcStress(layout);
			
			layout = c.calcOptimizedLayout();
			newStress = c.calcStress(layout);

			if (LOG) { 
				System.out.println("===============================");
				System.out.println("prev: " + prevStress);
				System.out.println("new:  " + newStress);
				System.out.println("diff: " + ((prevStress - newStress) / prevStress) + "; " + ((prevStress - newStress) / prevStress >= EPSILON));
			}
			
			
			double scaleFactor = 100;
			HashMap<Node, Vector2d> nodes2newPositions = new HashMap<Node, Vector2d>();
			for (int i = 0; i < n; i += 1) {
				double[] pos = layout.getRow(i);
				Vector2d position = new Vector2d(pos[0] * scaleFactor, 
												 pos[1] * scaleFactor);
				nodes2newPositions.put(nodes.get(i), position);
			}
			
			//update GUI layout
			setLayout(nodes2newPositions);
			setStressValue(newStress);
			
		} while ( (prevStress - newStress) / prevStress >= EPSILON && !isPauseButtonPressed()); // TODO: offer choice between change limit and number of iterations, offer choices of epsilon
		

		System.out.println("Updating layout...");
		double scaleFactor = 100;
		HashMap<Node, Vector2d> nodes2newPositions = new HashMap<Node, Vector2d>();
		for (int i = 0; i < n; i += 1) {
			double[] pos = layout.getRow(i);
			Vector2d position = new Vector2d(pos[0] * scaleFactor, 
											 pos[1] * scaleFactor);
			nodes2newPositions.put(nodes.get(i), position);
		}
		
		setEndLayout(nodes2newPositions);
		return nodes2newPositions;
		
	}
	
	/**
	 * Calculates the distance matrix of the given graph.
	 * @param g The graph which distance matrix will be calculated
	 * @return the distance matrix
	 */
	private RealMatrix calcDistances() {
		
		int n = nodes.size();
		Map<Node, Integer> node2Index = new HashMap<>();
		for (int i = 0; i < n; i += 1) {
			node2Index.put(nodes.get(i), i);
		}
		
		RealMatrix distances = new Array2DRowRealMatrix(n, n);
		
		//Breadth first Search
		for (int i = 0; i < n; i += 1) {
			for (int j = 0; j < n; j += 1) {
				distances.setEntry(i, j, Double.POSITIVE_INFINITY);
			}
		}
		
		for (int i = 0; i < n; i += 1) {
			distances.setEntry(i, i, 0);
		}
		
		for(int i = 0; i < n; i++) {
			
			Collection<Node> nodesToVisit = nodes.get(i).getNeighbors();
			
			int dist = 1;
			
			boolean[] visited = new boolean[n];
			Arrays.fill(visited, false);
			
			Collection<Node> nodesToVisitNext;
			
			while(nodesToVisit.size() != 0) {
				//next layer is empty at first
				nodesToVisitNext = new ArrayList<Node>();
				
				for(Node node : nodesToVisit) {
					int j = node2Index.get(node);
					if(!visited[j]) {
						if(distances.getEntry(i, j) > dist) {
							distances.setEntry(i, j, dist);
							distances.setEntry(j, i, dist);							
						}
						visited[j] = true;
						//Add neighbors of node to next layer
						nodesToVisitNext.addAll(node.getNeighbors());
						nodesToVisitNext.remove(node);
					}
				}
				//current layer is done
				nodesToVisit = nodesToVisitNext;
				dist++;
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

}
