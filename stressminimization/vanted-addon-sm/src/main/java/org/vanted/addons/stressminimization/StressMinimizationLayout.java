package org.vanted.addons.stressminimization;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.connected_components.ConnectedComponentLayout;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.graph_to_origin_mover.CenterLayouterAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.random.RandomLayouterAlgorithm;
import info.clearthought.layout.SingleFiledLayout;
import org.FolderPanel;
import org.Vector2d;
import org.graffiti.editor.GravistoService;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.DoubleParameter;
import org.graffiti.plugin.parameter.IntegerParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.view.View;
import org.vanted.addons.indexednodes.IndexedComponent;
import org.vanted.addons.indexednodes.IndexedGraphOperations;
import org.vanted.addons.indexednodes.IndexedNodeSet;
import org.vanted.addons.stressminimization.parameters.LandmarkParameter;
import org.vanted.addons.stressminimization.parameters.SliderParameter;

import javax.swing.*;
import java.util.*;
import java.util.List;

/**
 * Layout algorithm performing a stress minimization layout process.
 */
public class StressMinimizationLayout extends BackgroundAlgorithm {

	private static final int PREPROCESSING_NUMBER_OF_LANDMARKS = 100;

	/**
	 * Creates a new StressMinimizationLayout instance.
	 */
	public StressMinimizationLayout() {
		super();
	}

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

	private static final String NUMBER_OF_LANDMARKS_NAME = "Landmarks Count";
	private static final int NUMBER_OF_LANDMARKS_DEFAULT_VALUE = 100;
	private final int numberOfLandmarks = NUMBER_OF_LANDMARKS_DEFAULT_VALUE;

	private static final String RANDOMIZE_INPUT_LAYOUT_PARAMETER_NAME = "Randomize initial layout.";
	private static final boolean RANDOMIZE_INPUT_LAYOUT_DEFAULT_VALUE = false;
	private final boolean randomizeInputLayout = RANDOMIZE_INPUT_LAYOUT_DEFAULT_VALUE;

	private static final String ALPHA_PARAMETER_NAME = "Weight Factor";
	private static final int ALPHA_DEFAULT_VALUE = 2;
	private final int alpha = ALPHA_DEFAULT_VALUE;
	private static final String STRESS_CHANGE_EPSILON_PARAMETER_NAME = "Stress Change Threshold";

	/**
	 * This is the stateful object representing the current configuration of the parameters. In this sense, it
	 * is akin to ThreadSafeOptions.
	 */
	private final StressMinParamModel parameterModel = new StressMinParamModel();

	@Override
	public JComponent getParameterUI() {

		// for debug purposes, the below line can be replaced with
		// StressMinParamModel paramUIElems = new StressMinParamModel();
		// then, hot swapping can be used to update the display of the UI.
		StressMinParamModel paramUIElems = this.parameterModel;

		JPanel mainPanel = new JPanel();

		SingleFiledLayout sfl = new SingleFiledLayout(SingleFiledLayout.COLUMN, SingleFiledLayout.FULL, 1);
		mainPanel.setLayout(sfl);

		final FolderPanel preprocessingFolder = new FolderPanel("Preprocessing", false,
				true, false, null);
		preprocessingFolder.addComp(paramUIElems.initialLayoutRadioGroup);
		preprocessingFolder.layoutRows();
		mainPanel.add(preprocessingFolder);

		final FolderPanel methodFolder = new FolderPanel("Method", false, true, false, null);
		methodFolder.addComp(paramUIElems.methodRadioGroup);
		methodFolder.addComp(paramUIElems.methodAlphaGroup);
		methodFolder.layoutRows();
		mainPanel.add(methodFolder);

		final FolderPanel terminationFolder = new FolderPanel("Termination criteria", false, true, false,
				null);
		terminationFolder.addComp(paramUIElems.terminationStressChangeGroup);
		terminationFolder.addComp(paramUIElems.terminationCheckboxGroup);
		terminationFolder.layoutRows();
		mainPanel.add(terminationFolder);

		return mainPanel;
	}


	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	@Deprecated
	public Parameter[] getParameters() {
		List<Parameter> params = new ArrayList<>();

		params.add(new LandmarkParameter(
				numberOfLandmarks,
				10, // values below this give no more significant speed up
				1000, // values above this consume to much time in the current implementation
				NUMBER_OF_LANDMARKS_NAME,
				"The number of nodes that will be mainly layouted. All remaining nodes will be positioned relatively to these nodes. You may turn off this option by selecting all nodes as landmarks."
		));

		params.add(new BooleanParameter(
				true,
				"disable landmark preproc",
				"Turn of landmark preprocessing. In general this preprocessing speeds up execution, but will slow it down if you have an optimized initial layout (since that may be destroyed) or if you are using this layouter in a multilevel framework."
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
				"Change in stress of succeeding layouts threshold. If the change is below this threshold the layouter will terminate. Low values will give better layouts, but computation will consume more time.",
				-8, -1, false, true, dict
		));

		params.add(new DoubleParameter(
				0.0,
				0.0,
				1.0,
				0.05,
				"node mov thresh",
				"Minimum required movement of any node for continuation of computation. If all nodes move less than this value in an iteration, execution is terminated."
		));


		Dictionary stressDict = new Hashtable();
		stressDict.put(0, new JLabel("0%"));
		stressDict.put(50, new JLabel("50%"));
		stressDict.put(100, new JLabel("100%"));
		params.add(new SliderParameter(
				0,
				"stress thresh",
				"If the stress of a layout falls below this percentage of the stress on the beginning of the core process (after any preprocessing), execution is terminated.",
				0, 100, false, false, stressDict
		));

		Dictionary iterDict = new Hashtable();
		iterDict.put(25, new JLabel("25"));
		iterDict.put(50, new JLabel("50"));
		iterDict.put(75, new JLabel("75"));
		iterDict.put(100, new JLabel("100"));
		iterDict.put(125, new JLabel("\u221e"));
		params.add(new SliderParameter(
				75,
				"max iters",
				"Maximum number of iterations after which algorithm excution will be terminated.",
				25, 124, true, false, iterDict
		));

		params.add(new SliderParameter(
				alpha,
				ALPHA_PARAMETER_NAME,
				"Sets the importance of correct distance placement of more distanced nodes. A high value indicates that nodes with a high distance to one individual node will have less impact of the placement of this node, while nodes with a low distance will have a bigger impact.",
				0, 2
		));

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
	@Deprecated
	public void setParameters(Parameter[] params) {
		return;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void reset() {
		this.stop();
		super.reset();
		this.graph = null;
		this.selection = null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute() {

		setStatus(BackgroundStatus.RUNNING);
		setStatusDescription("Stress Minimization: starting...");

		StressMinParamModel params = this.parameterModel;

		if (params.initialLayoutRadioGroup.getSelected() == StressMinParamModel.InitialLayoutOption.useRandom) {
			setStatusDescription("Stress Minimization: randomizing input layout");
			RandomLayouterAlgorithm rla = new RandomLayouterAlgorithm();
			rla.attach(graph, selection);
			rla.execute();
		}

		Collection<Node> workNodesCollection;

		if (selection.isEmpty()) {
			workNodesCollection = graph.getNodes();
		} else {
			workNodesCollection = selection.getNodes();
		}

		IndexedNodeSet workNodes = IndexedNodeSet.setOfAllIn(workNodesCollection);

		setStatusDescription("Stress Minimization: calculating components");
		List<IndexedComponent> components = IndexedGraphOperations.getComponents(workNodes);

		// NOTE: applying the final nodes position updates in this class
		// has the benefit that the algorithm is more easy usable by the Multilevel Framework
		HashMap<Node, Vector2d> nodes2NewPositions = new HashMap<Node, Vector2d>();

        for (IndexedComponent component : components) {
			if (waitIfPausedAndCheckStop()) {
				return;
			}
			setStatusDescription("Stress Minimization: layouting next component");

			// todo (review bm) weird control flow; should be split up into more methods

			boolean useLandmarks = params.methodRadioGroup.useLandmarks();

			int numberOfLandmarks =
					(int) Math.floor(component.size() * params.methodRadioGroup.getSliderValuePos());
			numberOfLandmarks = Math.min(component.size(), numberOfLandmarks);
			if (numberOfLandmarks >= component.size()) {
				// do not do landmarked layout if all nodes are selected as landmarks
				useLandmarks = false;
			}
			numberOfLandmarks = Math.max(2, numberOfLandmarks);

			int nodeMovementThreshold = (params.terminationCheckboxGroup.nodeThresholdActive()) ?
					params.terminationCheckboxGroup.getNodeMovementThreshold() : 0;
			int iterThreshold = (params.terminationCheckboxGroup.iterThresholdActive()) ?
					params.terminationCheckboxGroup.getMaxIterations() : Integer.MAX_VALUE;

			// todo (review bm) pretty bad style...
			StressMinimizationImplementation impl = new StressMinimizationImplementation(
					component.nodes,
					this,
					useLandmarks,
					numberOfLandmarks,
					params.methodAlphaGroup.getAlpha(),
					params.terminationStressChangeGroup.getValue(),
					0, // todo
					nodeMovementThreshold,
					iterThreshold
			);

			impl.calculateLayout();

			if (getLayout() != null) {
				// == null might happen if algorithm was stopped
				nodes2NewPositions.putAll( getLayout().get() );
			}

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

	// todo (review bm) does this still make sense?
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
