package org.vanted.addons.stressminimization;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Supplier;

import org.AttributeHelper;
import org.Vector2d;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.graffiti.graph.Node;
import org.vanted.addons.indexednodes.IndexedGraphOperations;
import org.vanted.addons.indexednodes.IndexedNodeSet;

class StressMinimizationImplementation {

	// number of dimensions. Fixed to 2 in the current implementation
	private final int d = 2;
	private final int n; // fixed here to make clear that this number is always constant
	private final IndexedNodeSet nodes;
	private final StressMinimizationLayout callingLayout;

	private final int numberOfLandmarks;
	private final int alpha;
	private final double stressChangeEpsilon;
	private final double initialStressPercentage;
	private final double minimumNodeMovementThreshold;
	private final double iterationsThreshold;

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
	public StressMinimizationImplementation(IndexedNodeSet nodes, StressMinimizationLayout callingLayout,
			int numberOfLandmarks, int alpha, double stressChangeEpsilon, double initialStressPercentage,
			double minimumNodeMovementThreshold, double iterationsThreshold) {
		super();

		this.callingLayout = callingLayout;

		this.nodes = nodes.setOfContainedNodesWithOwnIndices();
		this.n = this.nodes.size();

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

		boolean noLandmarking = this.numberOfLandmarks >= n;

		if (callingLayout.waitIfPausedAndCheckStop()) { return; }

		if (noLandmarking) {

			calcFullStressModel();

		} else {

			calcLandmarked(this.numberOfLandmarks);

		}
	}

	private void calcFullStressModel() {

		callingLayout.setStatusDescription("Stress Minimization: calculating distances");

		RealMatrix allDistances = calcDistances(nodes);
		if (allDistances == null) { return; } // calcDistances did abort because algorithm was stopped

		calcLayoutForSelectedNodes(nodes, allDistances, allDistances);

	}

	// todo (review bm) landmarks variant lacks documentation or citation
	private void calcLandmarked(int numberOfLandmarks) {

		callingLayout.setStatusDescription("Stress Minimization: selecting landmarks");

		RealMatrix landmarkToLandmarkDistances = new BlockRealMatrix(numberOfLandmarks, numberOfLandmarks);
		RealMatrix landmarkToAllDistances = new BlockRealMatrix(numberOfLandmarks, n);
		List<Node> landmarkNodes = new ArrayList<>();

		int[] landmarkIndicesInGenerationOrder = new int[numberOfLandmarks];

		for (int i = 0; i < numberOfLandmarks; i += 1) {

			if (callingLayout.waitIfPausedAndCheckStop()) { return; }

			int newLandmarkIndex = selectNextLandmark(i, landmarkToAllDistances);
			landmarkIndicesInGenerationOrder[i] = newLandmarkIndex;

			Node landmark = nodes.get(newLandmarkIndex);
			landmarkNodes.add(landmark);

			RealVector toAllDistances = calcDistances(newLandmarkIndex);

			landmarkToAllDistances.setRowVector(i, toAllDistances);

			callingLayout.setDistancesProgress(i / numberOfLandmarks);
		}

		int[] from0ToNumberOfLandmarks = new int[numberOfLandmarks];
		for (int i = 0; i < numberOfLandmarks; i += 1) {
			from0ToNumberOfLandmarks[i] = i;
		}
		landmarkToLandmarkDistances = landmarkToAllDistances.getSubMatrix(from0ToNumberOfLandmarks, landmarkIndicesInGenerationOrder);

		IndexedNodeSet landmarks = IndexedNodeSet.setOfAllIn(landmarkNodes);

		calcLayoutForSelectedNodes(landmarks, landmarkToLandmarkDistances, landmarkToAllDistances);

	}

	private void calcLayoutForSelectedNodes(IndexedNodeSet selectedNodes, RealMatrix selectedToSelectedDistances, RealMatrix selectedToAllDistances) {

		if (callingLayout.waitIfPausedAndCheckStop()) { return; }
		callingLayout.setStatusDescription("Stress Minimization: calculating weights...");

		RealMatrix weights = getStressMinimizationWeightsForDistances(selectedToSelectedDistances);

		if (callingLayout.waitIfPausedAndCheckStop()) { return; }
		callingLayout.setStatusDescription("Stress Minimization: copying layout...");

		RealMatrix selectedNodesLayout = getLayout(selectedNodes, d);

		// remove the scaling that is done at the end of the layout process
		// scaling down the positions also
		// makes this algorithm work better with results from other algorithms
		// todo (review bm) o.0
		selectedNodesLayout = unscaleLayout(selectedNodesLayout);

		// StressMajorizationLayoutCalculator isn't working well with layouts
		// in which too many nodes are placed to the same
		// position. We add some "noise" to avoid this case
		for (int i = 0; i < selectedNodes.size(); i += 1) {
			for (int a = 0; a < d; a += 1) {
				double noisyPosition = selectedNodesLayout.getEntry(i, a) + Math.random() * 0.01 - 0.005;
				selectedNodesLayout.setEntry(i, a, noisyPosition);
			}
		}

		// todo (review bm) need pause functionality?
		if (callingLayout.waitIfPausedAndCheckStop()) { return; }
		callingLayout.setStatusDescription("Stress Minimization: optimizing layout - preprocessing...");

		// todo (review bm) same bad style -- dont need to instantiate a class, set parameters in constructor and
		//   then call a method with "no" arguments -- this introduces unnecessary statefulness
		// todo (review bm) naming
		StressMajorizationLayoutCalculator optim = new StressMajorizationLayoutCalculator(selectedNodesLayout, selectedToSelectedDistances, weights);

		// todo (review bm) complete mess of local and instance variables
		final double initialStress = optim.calcStress();
		final double stressThreshold = initialStress * (initialStressPercentage / 100);
		int iterationCount = 0;
		double newStress, prevStress = initialStress;
		RealMatrix prevLayout = selectedNodesLayout;
		boolean terminate = false;

		do { // todo (review bm) main loop of iterative optimisation
			 // todo (review bm) formulation seems overly complicated
			iterationCount += 1;

			if (callingLayout.waitIfPausedAndCheckStop()) { return; }

			selectedNodesLayout = optim.calcOptimizedLayout();
			setLayout(selectedNodes, selectedToAllDistances, selectedNodesLayout);
			newStress = optim.calcStress();

			terminate = checkTerminationCriteria(prevStress, newStress, prevLayout, selectedNodesLayout, iterationCount, stressThreshold);

			callingLayout.setIterationProgress(newStress, initialStress, iterationCount);

			prevLayout = selectedNodesLayout;
			prevStress = newStress;

		} while (!terminate);

		callingLayout.setIterationProgress(newStress, initialStress, iterationCount);
		setLayout(selectedNodes, selectedToAllDistances, selectedNodesLayout);

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
			int maxDegreeNodeIndex = 0;
			int maxDegree = Integer.MIN_VALUE;
			for (Integer index : nodes) {
				Node node = nodes.get(index);
				if (node.getDegree() > maxDegree) {
					maxDegree = node.getDegree();
					maxDegreeNodeIndex = index;
				}
			}
			return maxDegreeNodeIndex;
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
	public RealMatrix calcDistances(final IndexedNodeSet nodes) {

		RealMatrix distances = new BlockRealMatrix(n, n);

		for(int i = 0; i < n; i++) {
			RealVector dist = calcDistances(i);
			distances.setRowVector(i, dist);
			distances.setColumnVector(i, dist);

			if (callingLayout.waitIfPausedAndCheckStop()) { return null; }
			callingLayout.setDistancesProgress(i / n);

		}

		return distances;
	}

	/**
	 * Calculates the distance vector from the node at the specified index to all other nodes.
	 * @param fromIndex the distances from this node to all others will be calculated.
	 * @return the distance vector from the from node to all others
	 */
	public RealVector calcDistances(final int fromIndex) {
		return IndexedGraphOperations.calcDistances(fromIndex, nodes);
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

	private RealMatrix getLayout(IndexedNodeSet nodes, int d) {

		RealMatrix layout = new BlockRealMatrix(nodes.size(), d);
		for (int i = 0; i < nodes.size(); i += 1) {
			Point2D position = AttributeHelper.getPosition(nodes.get(i));
			layout.setRow(i, new double[] { position.getX(), position.getY() });
		}

		return layout;

	}

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

	// todo (review bm) separation of concerns
	private void setLayout(final IndexedNodeSet landmarks, final RealMatrix landmarksToAllDistances, final RealMatrix landmarkLayout) {

		Supplier<HashMap<Node, Vector2d>> layoutSupplier = () -> {

			RealMatrix layout;
			// if all nodes are selected as landmarks
			if (landmarksToAllDistances.getRowDimension() == landmarksToAllDistances.getColumnDimension()) {
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
	private RealMatrix positionNodesAtBarycentersOfLandmarks(final IndexedNodeSet landmarks, final RealMatrix landmarksToAllDistances, final RealMatrix landmarkLayout) {

		RealMatrix barycenterLayout = landmarkLayout.createMatrix(n, landmarkLayout.getColumnDimension());
		RealMatrix nodesToLandmarksWeights = getBarycenterWeightsForDistances(landmarksToAllDistances).transpose();

		for (int i : nodes) {

			Node node = nodes.get(i);
			if (landmarks.isContainedBasisCollectionAndSet(node)) {

				int landmarkIndex = landmarks.getIndex(node);
				barycenterLayout.setRow(i, landmarkLayout.getRow(landmarkIndex));

			} else {

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


		}



		return barycenterLayout;

	}

}
