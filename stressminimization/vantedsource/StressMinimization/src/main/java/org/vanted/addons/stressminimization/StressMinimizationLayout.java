package org.vanted.addons.stressminimization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import javax.swing.JLabel;

import org.Vector2d;
import org.graffiti.editor.GravistoService;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.DoubleParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.view.View;
import org.vanted.addons.stressminimization.parameters.LandmarkParameter;
import org.vanted.addons.stressminimization.parameters.SliderParameter;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.connected_components.ConnectedComponentLayout;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.graph_to_origin_mover.CenterLayouterAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.random.RandomLayouterAlgorithm;

/**
 * Layout algorithm performing a stress minimization layout process.
 */
public class StressMinimizationLayout extends BackgroundAlgorithm {

	/**
	 * Creates a new StressMinimizationLayout instance.
	 */
	public StressMinimizationLayout() {
		super();
	}

	// MARK: presentation control methods

	@Override
	public String getName() {
		return "Stress Minimization";
	}

	@Override
	public String getDescription() {
		return "Layouting Algorithm based on graph theoretical distances. Tries to minimize a global stress function and thereby tries to fit distances in the layout to the graph theoretical distances of the nodes.";
	}

	@Override
	public String getCategory() {
		return "Layout";
	}

	@Override
	public boolean isLayoutAlgorithm() {
		return true;
	}

	@Override
	public boolean activeForView(View v) {
		return v != null;
	}

	// MARK: algorithm execution

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void check() throws PreconditionException {
		super.check();

		if (graph.isEmpty()) {
			throw new PreconditionException("Stress Minimization Layout cannot work on empty graphs");
		}

	}


	// ================
	// MARK: parameters
	// ================

	// NOTE: we do not use the parameters field
	// because using an array for storing parameters
	// seemed very uncomfortable to us.

	private static final String NUMBER_OF_LANDMARKS_NAME = "Number of Landmarks";
	private static final int NUMBER_OF_LANDMARKS_DEFAULT_VALUE = 100;
	private int numberOfLandmarks = NUMBER_OF_LANDMARKS_DEFAULT_VALUE;

	private static final String RANDOMIZE_INPUT_LAYOUT_PARAMETER_NAME = "Randomize initial layout.";
	private static final boolean RANDOMIZE_INPUT_LAYOUT_DEFAULT_VALUE = false;
	private boolean randomizeInputLayout = RANDOMIZE_INPUT_LAYOUT_DEFAULT_VALUE;

	private static final String ALPHA_PARAMETER_NAME = "Weight Factor";
	private static final int ALPHA_DEFAULT_VALUE = 2;
	private int alpha = ALPHA_DEFAULT_VALUE;

	private static final String STRESS_CHANGE_EPSILON_PARAMETER_NAME = "Stress Change Termination Threshold";
	private double stressChangeEpsilon = 1e-4;

	private static final String MINIMUM_NODE_MOVEMENT_PARAMETER_NAME = "Minimum Node Movement Termination Threshold";
	private static final double MINIMUM_NODE_MOVEMENT_DEFAULT_VALUE = 0.0;
	private double minimumNodeMovementThreshold = MINIMUM_NODE_MOVEMENT_DEFAULT_VALUE;

	private static final String INITIAL_STRESS_PERCENTAGE_THERESHOLD_PARAMETER_NAME = "Initial Stress Termination Percentage";
	private static final double INITIAL_STRESS_PERCENTAGE_DEFAULT_VALUE = 0.0;
	private double initialStressPercentage = INITIAL_STRESS_PERCENTAGE_DEFAULT_VALUE;

	private static final String ITERATIONS_THRESHOLD_PARAMETER_NAME = "Interations Termination Maximum";
	private static final int ITERATIONS_THRESHOLD_DEFAULT_VALUE = 75;
	private double iterationsThreshold = ITERATIONS_THRESHOLD_DEFAULT_VALUE;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Parameter[] getParameters() {
		List<Parameter> params = new ArrayList<>();

		params.add(new LandmarkParameter(
				numberOfLandmarks,
				NUMBER_OF_LANDMARKS_NAME,
				"The number of nodes that will be mainly layouted. All remaining nodes will be positioned relatively to these nodes."
				));

		//MAKE STUFF HERE
		Dictionary dict = new Hashtable();
		dict.put(-9, new JLabel("0"));
		dict.put(-8, new JLabel("10\u207b\u2078"));
		dict.put(-6, new JLabel("10\u207b\u2076"));
		dict.put(-4, new JLabel("10\u207b\u2074"));
		dict.put(-2, new JLabel("10\u207b\u00B2"));
		params.add(new SliderParameter(
				-4,
				STRESS_CHANGE_EPSILON_PARAMETER_NAME,
				"Change in stress of succeeding layouts termination criterion. Low values will give better layouts, but computation will consume more time.",
				-8, -1, false, true, dict));

		params.add(new DoubleParameter(
				minimumNodeMovementThreshold,
				0.0,
				Double.POSITIVE_INFINITY,
				MINIMUM_NODE_MOVEMENT_PARAMETER_NAME,
				"Minimum required movement of any node for continuation of termination. If all nodes move less than this value in an interation, execution is terminated."
				));

		params.add(new DoubleParameter(
				initialStressPercentage,
				0.0,
				100.0,
				INITIAL_STRESS_PERCENTAGE_THERESHOLD_PARAMETER_NAME,
				"If the stress of an layout falls below this percentage of the initial stress, executions is terminated."
				));

		Dictionary iterDict = new Hashtable();
		iterDict.put(25, new JLabel("25"));
		iterDict.put(50, new JLabel("50"));
		iterDict.put(75, new JLabel("75"));
		iterDict.put(100, new JLabel("100"));
		iterDict.put(125, new JLabel("\u221e"));
		params.add(new SliderParameter(
				ITERATIONS_THRESHOLD_DEFAULT_VALUE,
				ITERATIONS_THRESHOLD_PARAMETER_NAME,
				"Number of iterations after which algorithm excution will be terminated.",
				25, 124, true, false, iterDict
				));

		params.add(new SliderParameter(
				alpha,
				ALPHA_PARAMETER_NAME,
				"Determines how important correct distancing of nodes is that are far away to each other in an itteration step. High values mean placing of far away nodes is less important.",
				0,2));

		params.add(new BooleanParameter(
				randomizeInputLayout,
				RANDOMIZE_INPUT_LAYOUT_PARAMETER_NAME,
				"Use a random layout as initial layout rather than the present layout."
				));

		return params.toArray(new Parameter[0]);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setParameters(Parameter[] params) {
		for (Parameter p : params) {

			switch (p.getName()) {
			case ALPHA_PARAMETER_NAME:
				this.alpha = (int) ((double) p.getValue());
				break;
			case STRESS_CHANGE_EPSILON_PARAMETER_NAME:
				this.stressChangeEpsilon = Math.pow(10, (Double) p.getValue());
				break;
			case MINIMUM_NODE_MOVEMENT_PARAMETER_NAME:
				this.minimumNodeMovementThreshold = (Double) p.getValue();
				break;
			case INITIAL_STRESS_PERCENTAGE_THERESHOLD_PARAMETER_NAME:
				this.initialStressPercentage = (Double) p.getValue();
				break;
			case ITERATIONS_THRESHOLD_PARAMETER_NAME:
				this.iterationsThreshold = (double) p.getValue();
				break;
			case RANDOMIZE_INPUT_LAYOUT_PARAMETER_NAME:
				this.randomizeInputLayout = (Boolean) p.getValue();
				break;
			case NUMBER_OF_LANDMARKS_NAME:
				this.numberOfLandmarks = (Integer) p.getValue();
				break;
			}

		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void reset() {
		super.reset();
		this.graph = null;
		this.selection = null;
		this.parameters = null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute() {

		setStatus(BackgroundStatus.RUNNING);
		setStatusDescription("Stress Minimization: starting...");

		if (randomizeInputLayout) {

			setStatusDescription("Stress Minimization: randomizing input layout");

			RandomLayouterAlgorithm rla = new RandomLayouterAlgorithm();
			rla.attach(graph, selection);
			rla.execute();
		}

		Collection<Node> workNodes;

		if (selection.isEmpty()) {
			workNodes = graph.getNodes();
		} else {
			workNodes = selection.getNodes();
			// for all selected edges, add source and target nodes
			for (Edge e : selection.getEdges()) {
				workNodes.add(e.getSource());
				workNodes.add(e.getTarget());
			}
		}

		setStatusDescription("Stress Minimization: calculating components");
		// IMPORTANT: components will add nodes that are not in workNodes to single components!
		Set<Set<Node>> components = GraphHelper.getConnectedComponents(workNodes);

		// NOTE: applying the final nodes position updates in this class
		// has the benefit, that the algorithm is more easy usable by the Multilevel Framework
		HashMap<Node, Vector2d> nodes2NewPositions = new HashMap<Node, Vector2d>();

		for (Set<Node> component : components) {

			setStatusDescription("Stress Minimization: layouting next component");

			if (!selection.isEmpty()) {
				component.retainAll(workNodes);
			}

			if (waitIfPausedAndCheckStop()) { return; }

			StressMinimizationImplementation impl = new StressMinimizationImplementation(component, this, numberOfLandmarks, alpha, stressChangeEpsilon, initialStressPercentage, minimumNodeMovementThreshold, iterationsThreshold);
			impl.calculateLayout();

			nodes2NewPositions.putAll( getLayout().get() );

		}

		GraphHelper.applyUndoableNodePositionUpdate(nodes2NewPositions, "Stress Minimization");

		// center graph layout
		GravistoService.getInstance().runAlgorithm(
				new CenterLayouterAlgorithm(),
				graph,
				selection,
				null
				);

		// remove space between components / remove overlapping
		// do not run as regular algorithm, since that triggers a gui dialogue
		// this means however, that we are also changing the positions of non selected nodes
		ConnectedComponentLayout.layoutConnectedComponents(graph);

		setStatus(BackgroundStatus.FINISHED);
	}

	// =========================
	// MARK: threading utilities
	// =========================

	/**
	 * Helper function that combines waiting and checking if stopped.
	 * @return Will return true if execution was stopped
	 */
	boolean waitIfPausedAndCheckStop() {

		waitIfPaused();

		// if stopped, terminate immediately
		if (this.isStopped()) {
			setStatus(BackgroundStatus.FINISHED);
			setProgress(1);
			return true;
		}

		return false;
	}

	/**
	 * If this algorithm was paused, this method waits until resume is called.
	 */
	private void waitIfPaused() {
		boolean hasPaused = false;
		while (isPaused()) {
			setStatus(BackgroundStatus.IDLE);
			hasPaused = true;
			try {
				synchronized(this) {
					this.wait();
				}
			} catch (InterruptedException e) {
				// never mind, wait again
			}
		}

		if (hasPaused) {
			setStatus(BackgroundStatus.RUNNING);
		}
	}

	@Override
	public void resume() {
		super.resume();
		// wakes up waitIfPaused
		synchronized(this) {
			this.notifyAll();
		}
	}

	@Override
	public void stop() {
		// necessary to make thread terminate if currently paused
		this.resume();
		super.stop();
	}

	// ======================
	// MARK: progress display
	// ======================

	// the first 10% are reserved for distance calculation
	// the remaining 90% are used for stress progress

	void setDistancesProgress(double nodesVisitedPercentage) {
		setProgress(nodesVisitedPercentage * 0.10);
	}

	void setIterationProgress(double newStress, double initialStress, int iterationCount) {

		double stressProgress = calcStressProgress(newStress, initialStress);

		setProgress(0.10 + stressProgress * 0.90);
		setStatusDescription("Stress Minimization: optimizing layout - iteration: " + iterationCount + "; current stress reduction: " + stressProgress * 100 + "%");

	}

	void setFinalProgress(double finalStress, double initialStress, int iterationCount) {

		double stressProgress = calcStressProgress(finalStress, initialStress);

		setProgress(1.0);
		setStatusDescription("Stress Minimization: optimization finished - iterations: " + iterationCount + "; total stress reduction: " + stressProgress * 100 + "%");

	}

	private double calcStressProgress(double newStress, double initialStress) {
		return 1 - newStress / initialStress;
	}

}
