package org.vanted.addons.stressminimization;

import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.Vector2d;
import org.graffiti.editor.GravistoService;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.view.View;
import org.graffiti.selection.Selection;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.connected_components.ConnectedComponentLayout;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.graph_to_origin_mover.CenterLayouterAlgorithm;

/**
 * Layout algorithm performing a stress minimization layout process.
 */
public class StressMinimizationLayout extends BackgroundAlgorithm{
	
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
		// TODO write description, may also be left empty
		return "";
	}

	@Override
	public String getCategory() {
		return "Layout";
	}
	
	@Override
	public boolean isLayoutAlgorithm() {
		return false;
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
		
		/* 
		if (graph.isDirected()) {
			throw new PreconditionException("Stress Minimization Layout cannot work on directed graphs");
		}
		*/
		
	}
	
	@Override
	public Parameter[] getParameters() {
		// TODO Auto-generated method stub
		return super.getParameters();
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		// TODO Auto-generated method stub
		super.setParameters(params);
	}
	
	@Override
	public void execute() {

		setStatus(BackgroundStatus.RUNNING);
		
		List<Node> nodes = graph.getNodes();
		Set<Set<Node>> components = GraphHelper.getConnectedComponents(nodes);
		HashMap<Node, Vector2d> newPositions = new HashMap<>();
		
		for (Set<Node> component : components) {
			
			StressMajorizationImpl impl = new StressMajorizationImpl(component);
			
			// TODO: change this
			for (PropertyChangeListener pcl : Arrays.asList(this.getPropertyChangeListener())) {
				impl.pcs.addPropertyChangeListener(pcl);
			}
			
			Map<Node, Vector2d> newComponentPositions = impl.calculateLayout();
			
			newPositions.putAll(newComponentPositions);
			
		}

		GraphHelper.applyUndoableNodePositionUpdate(newPositions, "Stress Majorization");
		
		// center graph layout
		GravistoService.getInstance().runAlgorithm(
				new CenterLayouterAlgorithm(), 
				graph,	
				new Selection(""), 
				null
		);

		// remove space between components / remove overlapping
		ConnectedComponentLayout.layoutConnectedComponents(graph);
		
		setStatus(BackgroundStatus.FINISHED);
	}
	
	@Override
	public void reset() {
		// TODO Auto-generated method stub
		super.reset();
	}
}
