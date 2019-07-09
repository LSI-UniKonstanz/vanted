package org.vanted.addons.MultilevelFramework;

import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.AttributeHelper;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.IntegerParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.view.View;
import org.graffiti.selection.Selection;
import org.graffiti.util.PluginHelper;
import org.vanted.addons.MultilevelFramework.BackgroundExecution.BackgroundAlgorithm;
import org.vanted.addons.MultilevelFramework.BackgroundExecution.BackgroundStatus;
import org.vanted.addons.MultilevelFramework.Coarsening.CoarseningAlgorithm;
import org.vanted.addons.MultilevelFramework.GUI.AlgorithmListParameter;
import org.vanted.addons.MultilevelFramework.MultilevelGraph.MultilevelGraph;
import org.vanted.addons.MultilevelFramework.MultilevelGraph.MultilevelParentGraphAttribute;
import org.vanted.addons.MultilevelFramework.MultilevelGraph.MultilevelParentNodeAttribute;
import org.vanted.addons.MultilevelFramework.Placement.PlacementAlgorithm;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.PatternSpringembedder;

/**
 * Layout algorithm for large graphs. Uses a merging, placing and layouting
 * algorithm. Merges nodes together level for level until a certain graph size
 * is reached. Then layouts the levels beginning with the smallest level and
 * uses the layout of previous level as input.
 */
public class MultilevelLayoutAlgorithm extends BackgroundAlgorithm {
	protected Graph graph;
	protected Selection selection;

	protected boolean showLevels;
	protected boolean scaleLevels;
	protected boolean initializeAllNodesAtSamePosition;
	protected int maxRecursionDepth;
	protected int targetNodeCount;

	protected AlgorithmWithParameters merger;
	protected AlgorithmWithParameters placer;
	protected AlgorithmWithParameters layouter;

	/**
	 * Initializes a MultilevelLayoutAlgorithm with default parameters.
	 */
	public MultilevelLayoutAlgorithm() {
		super();
		showLevels = false;
		scaleLevels = true;
		initializeAllNodesAtSamePosition = true;
		maxRecursionDepth = 30;
		targetNodeCount = 20;
	}

	@Override
	public String getName() {
		return "Multilevel Layout";
	}

	@Override
	public String getCategory() {
		return "Layout";
	}

	@Override
	public Set<Category> getSetCategory() {
		Set<Category> categories = new HashSet<>();
		categories.add(Category.LAYOUT);
		categories.add(Category.GRAPH);
		return categories;
	}

	@Override
	public String getMenuCategory() {
		return null;
	}

	@Override
	public boolean isLayoutAlgorithm() {
		return true;
	}

	@Override
	public String getDescription() {
		return null;
	}

	public final static String SHOWLEVELSNAME = "Show Levels";
	public final static String SHOWLEVELSDESC = "Opens a window for the final layout of every level.";
	public final static String TARGETNODENAME = "Minimum Level Size";
	public final static String TARGETNODEDESC = "Stops merging levels when this number of nodes is reached.";
	public final static String MAXRECURSIONNAME = "Maximum Number of Levels";
	public final static String MAXRECURSIONDESC = "Stops merging levels when this number of levels is reached.";
	public final static String SCALELEVELSNAME = "Scaling After Placement";
	public final static String SCALELEVELSDESC = "Multiplies the coordinates of every node by square root of f after the placement step"
			+ " where f is the fraction of nodes in the level below and nodes in the current level.";
	public final static String INITIALIZENAME = "Initialize Nodes";
	public final static String INITIALIZEDESC = "Initializes all nodes at the same position";
	public final static String CHOOSEMERGERNAME = "Choose Merger";
	public final static String CHOOSEMERGERDESC = "Determines which nodes will be merged into parent nodes.";
	public final static String CHOOSEPLACERNAME = "Choose Placer";
	public final static String CHOOSEPLACERDESC = "Determines where nodes will be placed in relation to their parent node.";
	public final static String CHOOSELAYOUTERNAME = "Choose Layouter";
	public final static String CHOOSELAYOUTERDESC = "Determines how the nodes will be layouted on every level.";

	@Override
	public void setParameters(Parameter[] params) {
		for (Parameter p : params) {
			switch (p.getName()) {
			case SHOWLEVELSNAME:
				showLevels = (boolean) p.getValue();
				break;
			case TARGETNODENAME:
				targetNodeCount = (int) p.getValue();
				break;
			case MAXRECURSIONNAME:
				maxRecursionDepth = (int) p.getValue();
				break;
			case SCALELEVELSNAME:
				scaleLevels = (boolean) p.getValue();
				break;
			case INITIALIZENAME:
				initializeAllNodesAtSamePosition = (boolean) p.getValue();
				break;
			case CHOOSEMERGERNAME:
				merger = (AlgorithmWithParameters) p.getValue();
				break;
			case CHOOSEPLACERNAME:
				placer = (AlgorithmWithParameters) p.getValue();
				break;
			case CHOOSELAYOUTERNAME:
				layouter = (AlgorithmWithParameters) p.getValue();
				break;
			}
		}
	}

	@Override
	public Parameter[] getParameters() {
		BooleanParameter showLevelsParameter = new BooleanParameter(showLevels, SHOWLEVELSNAME, SHOWLEVELSDESC);
		IntegerParameter targetNodeCountParameter = new IntegerParameter(targetNodeCount, 1, 100, TARGETNODENAME,
				TARGETNODEDESC);
		IntegerParameter maxRecursionDepthParameter = new IntegerParameter(maxRecursionDepth, 1, 100, MAXRECURSIONNAME,
				MAXRECURSIONDESC);
		BooleanParameter scaleLevelsParameter = new BooleanParameter(scaleLevels, SCALELEVELSNAME, SCALELEVELSDESC);

		// BooleanParameter initializeAllNodesAtSamePositionParameter = new
		// BooleanParameter(initializeAllNodesAtSamePosition, INITIALIZENAME,
		// INITIALIZEDESC);

		// We want to find all available mergers, placers and layouters.
		// Layouters can be identified with the isLayoutAlgorithm method.
		// This obviously does not exist for mergers and placers,
		// so we identify them by testing if they implement the respective interfaces.
		List<? extends Algorithm> algorithms = PluginHelper.getAvailableAlgorithms();
		List<Algorithm> mergers = new ArrayList<Algorithm>();
		List<Algorithm> placers = new ArrayList<Algorithm>();
		List<Algorithm> layouters = new ArrayList<Algorithm>();
		for (Algorithm a : algorithms) {
			if (a instanceof CoarseningAlgorithm) {
				mergers.add(a);
			} else if (a instanceof PlacementAlgorithm) {
				placers.add(a);
			} else if (a.isLayoutAlgorithm() && LayoutersWhitelist.getLayouters().contains(a.getName())) {
				if (a instanceof PatternSpringembedder) {
					layouters.add(new ForceDirectedWrapper());
				} else {
					layouters.add(a);
				}
			}
		}

		// Special type of parameter connected to a component which allows to select an
		// algorithm from a list and specify it's parameters.
		AlgorithmListParameter CoarseningParameter = new AlgorithmListParameter(CHOOSEMERGERNAME, CHOOSEMERGERDESC,
				mergers);
		AlgorithmListParameter PlacementParameter = new AlgorithmListParameter(CHOOSEPLACERNAME, CHOOSEPLACERDESC,
				placers);
		AlgorithmListParameter LayoutParameter = new AlgorithmListParameter(CHOOSELAYOUTERNAME, CHOOSELAYOUTERDESC,
				layouters);

		// removed initializeAllNodesAtSamePositionParameter because there it should
		// always be left on true
		return new Parameter[] { showLevelsParameter, /* initializeAllNodesAtSamePositionParameter, */
				targetNodeCountParameter, maxRecursionDepthParameter, scaleLevelsParameter, CoarseningParameter,
				PlacementParameter, LayoutParameter };
	}

	@Override
	public void attach(Graph g, Selection selection) {
		this.graph = g;
		if (selection.isEmpty()) {
			Selection selectEverything = new Selection();
			selectEverything.addAll(g.getGraphElements());
			this.selection = selectEverything;
		} else {
			this.selection = selection;
		}
	}

	@Override
	public void check() throws PreconditionException {
	}

	private static double MERGINGCALCSHARE = 0.05;// calculation time assumed to be spent on merging (used for progress
													// bar)
	private static double CONSTANTCALCSHARE = 0.2;// somewhat arbitrary but the progress bar jumps around nicely with
													// these numbers
	private static double LAYOUTINGCALCSHARE = 1 - MERGINGCALCSHARE - CONSTANTCALCSHARE;

	@Override
	public void execute() {
		// PREPROCESSING
		setStatus(BackgroundStatus.RUNNING);
		if (initializeAllNodesAtSamePosition) {
			setAllNodesToPosition(new Point2D.Double(100.0, 100.0));
		}
		MultilevelGraph multilevelGraph = new MultilevelGraph(graph, selection);
		setProgress(0.001);
		// MERGING PHASE
		// Merge until either targetNodeCount or the cap on the number of levels is
		// reached
		while (targetNodeCount < multilevelGraph.getTopLevelNodeCount()
				&& maxRecursionDepth > multilevelGraph.getLevelCount()) {
			if (waitIfPausedAndCheckStop()) {
				return;
			}
			setProgressDescription("Merging level " + Integer.toString(multilevelGraph.getLevelCount() - 1));
			multilevelGraph.addEmptyLevel();
			executeAlgorithmOnGraph(merger, multilevelGraph.getLevels().get(multilevelGraph.getLevelCount() - 2),
					multilevelGraph.getSelectionLevels().get(multilevelGraph.getLevelCount() - 2));
		}
		setProgress(MERGINGCALCSHARE);
		// PLACEMENT AND LAYOUT PHASE
		// Layout level n, place level n-1, layout level n-1,...,place level 0, layout
		// level 0
		// so there is one more call of layout than calls of place
		int levelCount = multilevelGraph.getLevelCount();
		int totalNodes = multilevelGraph.getTotalNodeCount();
		int nodesLayouted = 0;
		List<Graph> levels = multilevelGraph.getLevels();
		setProgressDescription(
				"Layouting level " + Integer.toString(levelCount - 1) + "       Nodes layouted: " + nodesLayouted);
		executeAlgorithmOnGraph(layouter, levels.get(levelCount - 1),
				multilevelGraph.getSelectionLevels().get(levelCount - 1));
		nodesLayouted += levels.get(levelCount - 1).getNumberOfNodes();
		for (int i = levelCount - 2; i >= 0; i--) {
			if (waitIfPausedAndCheckStop()) {
				return;
			}
			Graph currentLevel = levels.get(i);
			int currentNodeCount = currentLevel.getNumberOfNodes();
			setProgress(MERGINGCALCSHARE + CONSTANTCALCSHARE * ((double) levelCount - 2 - i) / (levelCount - 1)
					+ LAYOUTINGCALCSHARE * ((double) nodesLayouted + 0.25 * currentNodeCount) / totalNodes);
			setProgressDescription("Placing level " + Integer.toString(i));
			// place level
			executeAlgorithmOnGraph(placer, currentLevel, multilevelGraph.getSelectionLevels().get(i));
			// We found scaling the graph to be useful after each placement step.
			// The basic idea is that a level with x-times more nodes than it's predecessor
			// will occupy about x-times the area after it's layouted.
			// So the coordinates will have to be scaled by a factor of sqrt(x).
			// This obviously only makes sense for graphs and layout algorithms where
			// occupied area scales linearly with node numbers.
			// There are types of graphs however where this is not the case like a line
			// graph.
			if (scaleLevels) {
				int previousNodeCount = levels.get(i + 1).getNumberOfNodes();
				if (previousNodeCount != 0) {
					double scaleFactor = Math.pow(((double) currentNodeCount) / previousNodeCount, 0.5);
					scaleAllNodePositions(multilevelGraph.getSelectionLevels().get(i), scaleFactor);
				}
			}
			setProgressDescription(
					"Layouting level " + Integer.toString(i) + "       Nodes layouted " + currentNodeCount);
			setProgress(MERGINGCALCSHARE + CONSTANTCALCSHARE * ((double) levelCount - 1 - i) / (levelCount - 1)
					+ LAYOUTINGCALCSHARE * ((double) nodesLayouted + 0.25 * currentNodeCount) / totalNodes);
			// layout level
			executeAlgorithmOnGraph(layouter, currentLevel, multilevelGraph.getSelectionLevels().get(i));
			nodesLayouted += currentLevel.getNumberOfNodes();
		}
		// clean up attributes
		AttributeHelper.deleteAttribute(graph, MultilevelParentGraphAttribute.PATH,
				MultilevelParentGraphAttribute.NAME);
		for (Node n : graph.getNodes()) {
			AttributeHelper.deleteAttribute(n, MultilevelParentNodeAttribute.PATH, MultilevelParentNodeAttribute.NAME);
		}
		// Show levels in separate window if the user selected the option
		if (showLevels) {
			for (Graph l : levels.subList(1, multilevelGraph.getLevelCount())) {
				showLevel(l);
			}
		}
		setProgress(1);
		setStatus(BackgroundStatus.FINISHED);
	}

	/**
	 * Executes an Algorithm on a graph
	 * 
	 * @param a the algorithm to execute
	 * @param g the input graph for the execution
	 * @param s the selection for the execution
	 * @throws InterruptedException
	 */
	private void executeAlgorithmOnGraph(AlgorithmWithParameters a, Graph g, Selection s) {
		try {
			a.algorithm.attach(g, s);
			a.algorithm.setParameters(a.parameters);
			a.algorithm.check();
			a.algorithm.execute();
			a.algorithm.reset();
		} catch (PreconditionException e) {

		}
	}

	/**
	 * Sets all nodes to the same position. Starting positions are somewhat
	 * irrelevant for multilevel layouts, however all nodes positions have to be
	 * initialized.
	 * 
	 * @param position position the nodes will have
	 */
	private void setAllNodesToPosition(Point2D position) {
		for (Node n : selection.getNodes()) {
			AttributeHelper.setPosition(n, position);
		}
	}

	/**
	 * Scales the graph by a predetermined factor.
	 * 
	 * @param g
	 * @param factor
	 */
	private void scaleAllNodePositions(Selection s, double factor) {
		System.out.println("Scaling by factor " + factor);
		for (Node n : s.getNodes()) {
			Point2D currentPosition = AttributeHelper.getPosition(n);
			Point2D newPosition = new Point2D.Double(currentPosition.getX() * factor, currentPosition.getY() * factor);
			AttributeHelper.setPosition(n, newPosition);
		}
	}

	@Override
	public void setActionEvent(ActionEvent a) {
		// TODO Auto-generated method stub

	}

	@Override
	public ActionEvent getActionEvent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean activeForView(View v) {
		// TODO Auto-generated method stub
		return false;
	}

	// =========================
	// MARK: threading utilities
	// =========================

	/**
	 * Helper function that combines waiting and checking if stopped.
	 * 
	 * @return Will return true if execution was stopped
	 */
	private boolean waitIfPausedAndCheckStop() {

		waitIfPaused();

		// if stopped, terminate immediately
		if (this.isStopped()) {
			setStatus(BackgroundStatus.FINISHED);
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
				synchronized (this) {
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
		synchronized (this) {
			this.notifyAll();
		}
	}

	@Override
	public void stop() {
		// necessary to make thread terminate if currently paused
		this.resume();
		super.stop();
	}

}
