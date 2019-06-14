package org.vanted.addons.stressminimization;

import java.awt.geom.Point2D;
import java.beans.PropertyChangeListener;
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
import org.graffiti.editor.GravistoService;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.DoubleParameter;
import org.graffiti.plugin.parameter.EnumParameter;
import org.graffiti.plugin.parameter.IntegerParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.view.View;
import org.graffiti.selection.Selection;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.connected_components.ConnectedComponentLayout;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.graph_to_origin_mover.CenterLayouterAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.graph_to_origin_mover.CenterLayouterPlugin;
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
	
	private static final String ALPHA_PARAMETER_NAME = "Weight Factor";
	private static final int ALPHA_DEFAULT_VALUE = 2;
	private int alpha = ALPHA_DEFAULT_VALUE;

	private static final String EPSILON_PARAMETER_NAME = "Stress Change Termination Threshold: 10^{-x} Choose x:";
	private static final double EPSILON_DEFAULT_VALUE = -4;
	private double epsilon = EPSILON_DEFAULT_VALUE;
	
	private static final String RANDOMIZE_INPUT_LAYOUT_PARAMETER_NAME = "Randomize initial layout.";
	private static final boolean RANDOMIZE_INPUT_LAYOUT_DEFAULT_VALUE = false;
	private boolean randomizeInputLayout = RANDOMIZE_INPUT_LAYOUT_DEFAULT_VALUE;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Parameter[] getParameters() {
		List<Parameter> params = new ArrayList<>();
		
		params.add(new IntegerParameter(
				alpha, 
				0,
				2, 
				ALPHA_PARAMETER_NAME, 
				"Determines how important correct distancing of nodes is that are far away to each other in an itteration step. High values mean placing of far away nodes is less important."
		));
		
		params.add(new DoubleParameter(
				-4.0, 
				Double.NEGATIVE_INFINITY,
				0.0, 
				EPSILON_PARAMETER_NAME, 
				"Termination criterion of the stress minimization process. Low values will give better layouts, but computation will consume more time."
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
	public void setParameters(Parameter[] params) {
		for (Parameter p : params) {
			
			switch (p.getName()) {
			case ALPHA_PARAMETER_NAME:
				this.alpha = (Integer) p.getValue();
				break;
			case EPSILON_PARAMETER_NAME:
				this.epsilon = (Double) p.getValue();
				// TODO: parameter currently not working
				this.epsilon = Math.pow(10, (Double) p.getValue());
				break;
			case RANDOMIZE_INPUT_LAYOUT_PARAMETER_NAME:
				this.randomizeInputLayout = (Boolean) p.getValue();
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

		if (randomizeInputLayout) {
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
		
		// IMPORTANT: components will add nodes that are not in workNodes to single components!
		Set<Set<Node>> components = GraphHelper.getConnectedComponents(workNodes);
		
		for (Set<Node> component : components) {
			
			if (!selection.isEmpty()) {
				component.retainAll(workNodes);
			}
			
			if (waitIfPausedAndCheckStop()) { return; }
			
			List<Node> nodes = new ArrayList<>(component);
			calculateLayoutForNodes(nodes);
			
		}
		
		setEndLayout();
		
		// center graph layout
		GravistoService.getInstance().runAlgorithm(
				new CenterLayouterAlgorithm(), 
				graph,	
				selection, 
				null
		);

		// remove space between components / remove overlapping
		// do not run as regular algorithm, since that triggers a gui dialogue
		// however, then there is no direct way to work only on the selection
		ConnectedComponentLayout.layoutConnectedComponents(graph);

		setStatus(BackgroundStatus.FINISHED);
	}

	/**
	 * Calculates an optimized layout for specific nodes of a graph. The nodes are seen as a subgraph. 
	 * The other nodes of the original graph are not touched.
	 * @param nodes The nodes a layout is calculated for.
	 */
	private void calculateLayoutForNodes(List<Node> nodes) {

		// enable or disable console logging
		final boolean LOG = true;
		
		final int n = nodes.size();
		final int d = 2; // only implemented for two dimensional space

		if (waitIfPausedAndCheckStop()) { return; }
		
		if (LOG) { System.out.println("Calculating distances..."); }
		RealMatrix distances = calcDistances(nodes);

		if (waitIfPausedAndCheckStop()) { return; }
		
		if (LOG) { System.out.println("Calculating weights..."); }
		RealMatrix weights = getWeightsForDistances(distances, alpha); // TODO make alpha selectable by user

		if (waitIfPausedAndCheckStop()) { return; }
		
		if (LOG) { System.out.println("Copying layout..."); }
		
		RealMatrix layout = new Array2DRowRealMatrix(n, d); 
		for (int i = 0; i < n; i += 1) {
			Point2D position = AttributeHelper.getPosition(nodes.get(i));
			layout.setRow(i, new double[] { position.getX(), position.getY() });
		}
		
		// remove the scaling that is done at the end of the layout process
		// layouts in VANTED look good with distances at about 100
		// but our algorithm works with distances around 1.0
		// scaling down the positions by the scale factor also
		// makes this algorithm work better with results from other algorithms
		layout = unscaleLayout(layout);

		// TODO: parameter: randomize input layout
		// TODO: always randomize if null layout?
		
		if (LOG) { System.out.println("Optimizing layout..."); }
		double prevStress, newStress;
		do {

			if (waitIfPausedAndCheckStop()) { return; }
			
			StressMajorizationLayoutCalculator c = new StressMajorizationLayoutCalculator(layout, distances, weights);
			prevStress = c.calcStress(layout);
			
			layout = c.calcOptimizedLayout();
			newStress = c.calcStress(layout);

			//update GUI layout
			setLayout(layout, nodes);
			// inverse displaying: high values get close to 0, values close to EPSILON get close to 1
			setProgress( 1 - Math.sqrt( (prevStress - newStress) / prevStress + epsilon) );

			if (LOG) { 
				System.out.println("===============================");
				System.out.println("prev: " + prevStress);
				System.out.println("new:  " + newStress);
				System.out.println("diff: " + ((prevStress - newStress) / prevStress));
			}
			
		} while ( (prevStress - newStress) / prevStress >= epsilon); // TODO: offer choice between change limit and number of iterations, offer choices of epsilon
		

		System.out.println("Updating layout...");
		setLayout(layout, nodes);
		
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
		RealMatrix newLayout = new Array2DRowRealMatrix(layout.getRowDimension(), layout.getColumnDimension());
		for (int i = 0; i < layout.getRowDimension(); i += 1) {
			for (int d = 0; d < layout.getColumnDimension(); d += 1) {
				double value = layout.getEntry(i, d) * LAYOUT_SCALE_FACTOR;
				newLayout.setEntry(i, d, value);
			}
		}
		return newLayout;
	}

	/**
	 * Reverses the scaling done in scaleLayout
	 */
	private RealMatrix unscaleLayout(final RealMatrix layout) {
		RealMatrix newLayout = new Array2DRowRealMatrix(layout.getRowDimension(), layout.getColumnDimension());
		for (int i = 0; i < layout.getRowDimension(); i += 1) {
			for (int d = 0; d < layout.getColumnDimension(); d += 1) {
				double value = layout.getEntry(i, d) / LAYOUT_SCALE_FACTOR;
				newLayout.setEntry(i, d, value);
			}
		}
		return newLayout;
	}
	
	private void setLayout(final RealMatrix layout, final List<Node> nodes) {

		RealMatrix newLayout = scaleLayout(layout);
		
		HashMap<Node, Vector2d> nodes2newPositions = new HashMap<Node, Vector2d>();
		for (int i = 0; i < nodes.size(); i += 1) {
			double[] pos = newLayout.getRow(i);
			Vector2d position = new Vector2d(pos[0], pos[1]);
			nodes2newPositions.put(nodes.get(i), position);
		}
		
		//update GUI layout
		setLayout(nodes2newPositions);
		
	}

	// =========================
	// MARK: threading utilities
	// =========================
	
	/**
	 * Helper function that combines waiting and checking if stopped. 
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
	
	// =====================================
	// MARK: distance and weight calculation
	// =====================================
	
	/**
	 * Calculates the distance matrix of the given nodes set.
	 * @param nodesSet A set of nodes. For these nodes the distances to the other nodes in the set will be calculated.
	 * @return the distance matrix
	 */
	private RealMatrix calcDistances(List<Node> nodes) {
		
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
					Integer indexIfPresent = node2Index.get(node);
					
					// due to the selection, we may get nodes here, that are not present in the node2Index map
					// these nodes will be ignored
					if (indexIfPresent == null) {
						continue;
					}
					
					int j = indexIfPresent;
					
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

}
