package org.vanted.addons.stressminimization;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Supplier;

import org.AttributeHelper;
import org.Vector2d;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.graph.Node;

class StressMinimizationImplementation {

	private final int n; // fixed here to make clear that this number is always constant
	private final List<Node> nodes;
	private final StressMinimizationLayout callingLayout;

	private final int numberOfLandmarks;
	private final int alpha;
	private final double stressChangeEpsilon;
	private final double initialStressPercentage;
	private final double minimumNodeMovementThreshold;
	private final double iterationsThreshold;

	// used to differentiate this instance from the other instances
	// used for indexing
	private final int stressMinimizationImplementationID;

	/**
	 * Creates a new StressMinimizationImplementation working on the given list of nodes
	 * and using callingLayout for pausing and stopping behavior
	 * @param nodes The nodes to layout.
	 * @param callingLayout The StressMinimizationLayout instance creating this instance
	 * @param numberOfLandmarks
	 * @param alpha alpha exponent for weighting.
	 * @param stressChangeEpsilon Stress change threshold below which execution will be terminated
	 * @param initialStressPercentage The initial stress percentage below which execution will be terminated
	 * @param minimumNodeMovementThreshold The minimum node movement threshold below which execution will be terminated
	 * @param iterationsThreshold The maximal number of iterations above which execution will be terminated
	 */
	public StressMinimizationImplementation(Set<Node> nodes, StressMinimizationLayout callingLayout,
			int numberOfLandmarks, int alpha, double stressChangeEpsilon, double initialStressPercentage,
			double minimumNodeMovementThreshold, double iterationsThreshold) {
		super();

		this.stressMinimizationImplementationID = getStressMinimizationImplementationID();
		this.nodeIndexAttributePath = NODE_INDEX_ATTRIBUTE_START_PATH + stressMinimizationImplementationID;

		this.callingLayout = callingLayout;

		this.n = nodes.size();
		this.nodes = new ArrayList<>(nodes);
		indexNodes();

		this.numberOfLandmarks = numberOfLandmarks;
		this.alpha = alpha;
		this.stressChangeEpsilon = stressChangeEpsilon;
		this.initialStressPercentage = initialStressPercentage;
		this.minimumNodeMovementThreshold = minimumNodeMovementThreshold;
		this.iterationsThreshold = iterationsThreshold;
	}



	/**
	 * Calculates an optimized layout for this instances nodes.
	 */
	void calculateLayout() {

		final int d = 2; // only implemented for two dimensional space
		boolean noLandmarking = this.numberOfLandmarks >= n;
		int numberOfLandmarks = noLandmarking ? n : this.numberOfLandmarks;

		if (callingLayout.waitIfPausedAndCheckStop()) { return; }

		List<Node> landmarks = new ArrayList<>(numberOfLandmarks);
		RealMatrix landmarkToLandmarkDistances = new BlockRealMatrix(numberOfLandmarks, numberOfLandmarks);
		RealMatrix landmarkToAllDistances = new BlockRealMatrix(numberOfLandmarks, n);

		if (noLandmarking) {

			callingLayout.setStatusDescription("Stress Minimization: calculating distances");

			landmarks = nodes;
			landmarkToLandmarkDistances = calcDistances(nodes);
			landmarkToAllDistances = landmarkToLandmarkDistances;

		} else {

			callingLayout.setStatusDescription("Stress Minimization: selecting landmarks");

			int[] landmarkNodeIndices = new int[numberOfLandmarks];

			for (int i = 0; i < numberOfLandmarks; i += 1) {

				int newLandmarkIndex = selectNextLandmark(i, landmarkToAllDistances);
				RealVector toAllDistances = calcDistances(nodes.get(newLandmarkIndex));

				landmarkToAllDistances.setRowVector(i, toAllDistances);
				landmarkNodeIndices[i] = newLandmarkIndex;

				callingLayout.setDistancesProgress(i / numberOfLandmarks);
			}

			int[] from0ToNumberOfLandmarks = new int[numberOfLandmarks];
			for (int i = 0; i < numberOfLandmarks; i += 1) {
				from0ToNumberOfLandmarks[i] = i;
			}
			landmarkToLandmarkDistances = landmarkToAllDistances.getSubMatrix(from0ToNumberOfLandmarks, landmarkNodeIndices);

			for (int i = 0; i < landmarkNodeIndices.length; i += 1) {
				int landmarkIndex = landmarkNodeIndices[i];
				landmarks.add(nodes.get(landmarkIndex));
			}

		}

		if (callingLayout.waitIfPausedAndCheckStop()) { return; }
		callingLayout.setStatusDescription("Stress Minimization: calculating weights...");

		RealMatrix weights = getStressMinimizationWeightsForDistances(landmarkToLandmarkDistances);

		if (callingLayout.waitIfPausedAndCheckStop()) { return; }
		callingLayout.setStatusDescription("Stress Minimization: copying layout...");

		RealMatrix landmarkLayout = new BlockRealMatrix(landmarks.size(), d);
		for (int i = 0; i < landmarks.size(); i += 1) {
			Point2D position = AttributeHelper.getPosition(landmarks.get(i));
			landmarkLayout.setRow(i, new double[] { position.getX(), position.getY() });
		}

		// remove the scaling that is done at the end of the layout process
		// layouts in VANTED look good with distances at about 100
		// but our algorithm works with distances around 1.0
		// scaling down the positions by the scale factor also
		// makes this algorithm work better with results from other algorithms
		landmarkLayout = unscaleLayout(landmarkLayout);

		if (callingLayout.waitIfPausedAndCheckStop()) { return; }
		callingLayout.setStatusDescription("Stress Minimization: optimizing layout - preprocessing...");

		StressMajorizationLayoutCalculator optim = new StressMajorizationLayoutCalculator(landmarkLayout, landmarkToLandmarkDistances, weights);

		final double initialStress = optim.calcStress();
		final double stressThreshold = initialStress * (initialStressPercentage / 100);
		int iterationCount = 0;
		double newStress, prevStress = initialStress;
		RealMatrix prevLayout = landmarkLayout;
		boolean terminate = false;
		do {

			iterationCount += 1;

			if (callingLayout.waitIfPausedAndCheckStop()) { return; }

			landmarkLayout = optim.calcOptimizedLayout();
			setLayout(noLandmarking, landmarks, landmarkToAllDistances, landmarkLayout);
			newStress = optim.calcStress();

			terminate = checkTerminationCriteria(prevStress, newStress, prevLayout, landmarkLayout, iterationCount, stressThreshold);

			callingLayout.setIterationProgress(newStress, initialStress, iterationCount);

			prevLayout = landmarkLayout;
			prevStress = newStress;

		} while (!terminate);

		callingLayout.setIterationProgress(newStress, initialStress, iterationCount);
		setLayout(noLandmarking, landmarks, landmarkToAllDistances, landmarkLayout);

	}

	// =================
	// MARK: termination
	// =================

	/**
	 * Checks all termination criteria.
	 * If one criterion meets the input values the method will return true,
	 * indicating the algorithm to terminate.
	 * @param prevStress Stress of the previous layout
	 * @param newStress Stress of the updated layout
	 * @param layout The new layout
	 * @param interationCount Number of iterations done so far
	 * @param stressThreshold stress threshold
	 * @return Whether one criterion was met.
	 */
	private boolean checkTerminationCriteria(double prevStress, double newStress, RealMatrix prevLayout, RealMatrix newLayout, long iterationCount, double stressThreshold) {

		boolean terminate = false;

		terminate |= (prevStress - newStress) / prevStress < stressChangeEpsilon;

		terminate |= newStress <= stressThreshold;

		terminate |= iterationCount >= iterationsThreshold;

		// only check minimum node movement criterion
		// if the threshold is != 0
		// (actually 1e-25; double comparison with threshold)
		// since the check has O(n) time complexity

		if (minimumNodeMovementThreshold > 1e-25) {
			double maxMovement = 0.0;
			for (int i = 0; i < newLayout.getRowDimension(); i += 1) {
				double movement = prevLayout.getRowVector(i).getDistance(newLayout.getRowVector(i));
				maxMovement = movement > maxMovement ? movement : maxMovement;
			}
			terminate |= maxMovement <= minimumNodeMovementThreshold;
		}

		return terminate;

	}

	// ===================
	// MARK: node indexing
	// ===================

	private static final String NODE_INDEX_ATTRIBUTE_START_PATH = "stressmin-index";
	private final String nodeIndexAttributePath;

	/**
	 * inits the index finding structures
	 */
	private void indexNodes() {
		for (int i = 0; i < n; i += 1) {
			nodes.get(i).setInteger(nodeIndexAttributePath, i);
		}
	}

	/**
	 * Returns the index in the nodes list, if the node is present in it, otherwise a NotIndexedException is thrown.
	 */
	private int getIndex(final Node node) {
		int index;
		try {
			index = node.getInteger(nodeIndexAttributePath);
		} catch (AttributeNotFoundException ex) {
			throw new NotIndexedException("the given nodes isn't indexed.", ex);
		}
		return index;
	}

	private class NotIndexedException extends RuntimeException {
		private static final long serialVersionUID = -4294175391400892274L;
		public NotIndexedException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	// ========================
	// MARK: landmark selection
	// ========================

	/**
	 * Selects the next landmark on basis of the MaxMin distance method.
	 * The first landmark is selected as the node with the highest degree
	 * @param numberOfAlreadySelectedLandmarks Number of already selected landmarks
	 * @param landmarkToAllDistances the distances from the previously selected landmarks to all other nodes
	 * @return the index of the node that should be selected as landmark
	 */
	private int selectNextLandmark(final int numberOfAlreadySelectedLandmarks, final RealMatrix landmarkToAllDistances) {

		// select first landmark as the node with the highest degree
		if (numberOfAlreadySelectedLandmarks == 0) {
			Node highestDegreeNode = nodes.stream().max( (Node n1, Node n2) -> { return n1.getDegree() - n2.getDegree(); } ).get();
			return getIndex(highestDegreeNode);
		}

		// MaxMin strategy:
		// landmarks are selected as the nodes with the maximal minimum
		// distance to the already selected landmarks
		// time complexity: O(n * l + n) where l is the number of landmarks already selected

		double[] minimumDistances = new double[n];
		for (int i = 0; i < n; i += 1) {

			minimumDistances[i] = Double.POSITIVE_INFINITY;

			for (int l = 0; l < numberOfAlreadySelectedLandmarks; l += 1) {
				double lToIDistance = landmarkToAllDistances.getEntry(l, i);
				if (lToIDistance < minimumDistances[i]) {
					minimumDistances[i] = lToIDistance;
				}
			}
		}

		int nextLandmark = 0;
		double maxMinDistance = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < n; i += 1) {

			double iMinDistance = minimumDistances[i];

			// if minimum distance is 0, the node is already a landmark
			if (iMinDistance == 0) {
				continue;
			}

			if (iMinDistance > maxMinDistance) {
				maxMinDistance = iMinDistance;
				nextLandmark = i;
			}

		}

		if (maxMinDistance == Double.NEGATIVE_INFINITY) {
			throw new RuntimeException("All nodes were already selected as landmarks.");
		}

		return nextLandmark;

	}

	// =====================================
	// MARK: distance and weight calculation
	// =====================================

	/**
	 * Calculates the distance matrix of the given nodes set.
	 * @param nodes A list of nodes. For these nodes the distances to the other nodes in the set will be calculated.
	 * @return the distance matrix
	 */
	public RealMatrix calcDistances(final List<Node> nodes) {

		RealMatrix distances = new BlockRealMatrix(n, n);

		for(int i = 0; i < n; i++) {
			RealVector dist = calcDistances(nodes.get(i));
			distances.setRowVector(i, dist);
			distances.setColumnVector(i, dist);

			callingLayout.setDistancesProgress(i / n);

		}

		return distances;
	}

	/**
	 * Calculates the distance vector from the specified node to all other nodes.
	 * @param from the distances from this node to all others will be calculated.
	 * @return the distance vector from the from node to all others
	 */
	public RealVector calcDistances(final Node from) {

		int fromIndex = getIndex(from);

		RealVector distances = new ArrayRealVector(n);

		//Breadth first Search
		for (int i = 0; i < n; i += 1) {
			distances.setEntry(i, Double.POSITIVE_INFINITY);
		}

		distances.setEntry(fromIndex, 0);

		Collection<Node> nodesToVisit = from.getNeighbors();

		int dist = 1;

		boolean[] visited = new boolean[n];
		Arrays.fill(visited, false);

		while(nodesToVisit.size() != 0) {
			//next layer is empty at first
			Collection<Node> nodesToVisitNext = new ArrayList<Node>();

			for(Node node : nodesToVisit) {

				// due to the selection, we may get nodes here, that are not present in the nodes list
				// these nodes will be ignored
				int j;
				try {
					j = getIndex(node);
				} catch (NotIndexedException ex) {
					continue;
				}

				if(!visited[j]) {
					if(distances.getEntry(j) > dist) {
						distances.setEntry(j, dist);
					}
					visited[j] = true;
					//Add neighbors of node to next layer
					nodesToVisitNext.addAll(node.getNeighbors());
				}
			}
			//current layer is done
			nodesToVisit = nodesToVisitNext;
			dist++;
		}

		return distances;

	}

	/**
	 * Computes default stress minimization weights from the given distance matrix, using the formula <br>
	 * $$
	 * w_{ij} := d_{ij}^{-\alpha}
	 * $$
	 * @return the calculated weight matrix
	 */
	private RealMatrix getStressMinimizationWeightsForDistances(final RealMatrix distances) {
		return calcWeightsForDistances(distances, d -> Math.pow(d, -alpha));
	}

	/**
	 * Computes barycentering weights from the given distance matrix, using the formula <br>
	 * $$
	 * w_{ij} := e^{d_{ij}}
	 * $$
	 * @return the calculated weight matrix
	 */
	private RealMatrix getBarycenterWeightsForDistances(final RealMatrix distances) {
		return calcWeightsForDistances(distances, d -> Math.exp(-d));
	}

	/**
	 * Computes  weights from the given distance matrix, using the transformation function
	 * @return the calculated weight matrix
	 */
	private RealMatrix calcWeightsForDistances(final RealMatrix distances, DoubleUnaryOperator transformation) {
		RealMatrix weights = distances.createMatrix(distances.getRowDimension(), distances.getColumnDimension());
		for (int i = 0; i < weights.getRowDimension(); i += 1) {
			for (int j = 0; j < weights.getColumnDimension(); j += 1) {
				double wij =  transformation.applyAsDouble(distances.getEntry(i, j));
				weights.setEntry(i, j, wij);
			}
		}
		return weights;
	}

	// ======================
	// MARK: layout utilities
	// ======================

	/**
	 * Layout scale factor, see scaleLayout.
	 */
	private final double LAYOUT_SCALE_FACTOR = 100;

	/**
	 * Layouts in VANTED look good with distances at about 100
	 * but the background algorithm works with distances around 1.0
	 * To make the resulting layouts look good, we scale up the results by a scale factor
	 */
	private RealMatrix scaleLayout(final RealMatrix layout) {
		RealMatrix newLayout = layout.createMatrix(layout.getRowDimension(), layout.getColumnDimension());
		for (int i = 0; i < layout.getRowDimension(); i += 1) {
			for (int a = 0; a < layout.getColumnDimension(); a += 1) {
				double value = layout.getEntry(i, a) * LAYOUT_SCALE_FACTOR;
				newLayout.setEntry(i, a, value);
			}
		}
		return newLayout;
	}

	/**
	 * Reverses the scaling done in scaleLayout
	 */
	private RealMatrix unscaleLayout(final RealMatrix layout) {
		RealMatrix newLayout = layout.createMatrix(layout.getRowDimension(), layout.getColumnDimension());
		for (int i = 0; i < layout.getRowDimension(); i += 1) {
			for (int a = 0; a < layout.getColumnDimension(); a += 1) {
				double value = layout.getEntry(i, a) / LAYOUT_SCALE_FACTOR;
				newLayout.setEntry(i, a, value);
			}
		}
		return newLayout;
	}

	private void setLayout(final boolean noLandmarking, final List<Node> landmarks, final RealMatrix landmarksToAllDistances, final RealMatrix landmarkLayout) {

		Supplier<HashMap<Node, Vector2d>> layoutSupplier = () -> {

			RealMatrix layout;
			if (noLandmarking) {
				layout = landmarkLayout.copy();
			} else {
				layout = positionNodesAtBarycentersOfLandmarks(landmarks, landmarksToAllDistances, landmarkLayout);
			}
			layout = scaleLayout(layout);

			HashMap<Node, Vector2d> nodes2NewPositions = new HashMap<Node, Vector2d>();
			for (int i = 0; i < nodes.size(); i += 1) {
				double[] pos = layout.getRow(i);
				Vector2d position = new Vector2d(pos[0], pos[1]);
				nodes2NewPositions.put(nodes.get(i), position);
			}

			return nodes2NewPositions;

		};

		callingLayout.setLayout(layoutSupplier);

	}

	/**
	 * Calculates the layout of all nodes that are not landmarks by barycentering.
	 * @return the layout matrix for all nodes
	 */
	private RealMatrix positionNodesAtBarycentersOfLandmarks(final List<Node> landmarks, final RealMatrix landmarksToAllDistances, final RealMatrix landmarkLayout) {

		RealMatrix barycenterLayout = landmarkLayout.createMatrix(n, landmarkLayout.getColumnDimension());

		RealMatrix nodesToLandmarksWeights = getBarycenterWeightsForDistances(landmarksToAllDistances).transpose();
		for (int i = 0; i < n; i += 1) {

			double sumOfWeights = 0;
			for (int l = 0; l < landmarks.size(); l += 1) {
				sumOfWeights += nodesToLandmarksWeights.getEntry(i, l);
			}

			RealMatrix nodeLayout =
					nodesToLandmarksWeights.getRowMatrix(i)
					.multiply(landmarkLayout)
					.scalarMultiply(1 / sumOfWeights);
			barycenterLayout.setRowMatrix(i, nodeLayout);

		}

		// now we also barycentered the landmarks, but that isn't intended
		for (int l = 0; l < landmarkLayout.getRowDimension(); l += 1) {
			int landmarkIndex = getIndex(landmarks.get(l));
			barycenterLayout.setRow(landmarkIndex, landmarkLayout.getRow(l));
		}

		return barycenterLayout;

	}

	// ===========================
	// MARK: identifier generation
	// ===========================

	private static volatile int nextID = 0;
	private static synchronized int getStressMinimizationImplementationID() {
		nextID += 1;
		return nextID;
	}

}
