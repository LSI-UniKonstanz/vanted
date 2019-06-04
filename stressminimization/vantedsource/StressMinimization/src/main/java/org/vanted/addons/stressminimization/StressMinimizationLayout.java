package org.vanted.addons.stressminimization;

import java.util.Collection;

import org.graffiti.editor.GravistoService;
import org.graffiti.graph.Graph;
import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.view.View;
import org.graffiti.selection.Selection;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.graph_to_origin_mover.CenterLayouterAlgorithm;

/**
 * Layout algorithm performing a stress minimization layout process.
 * @author David Boetius
 */
public class StressMinimizationLayout extends AbstractEditorAlgorithm {

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
		
		Collection<Graph> components = GraphHelper.getConnectedComponentsAsCopy(graph);
		
		for (Graph component : components) {
			new StressMajorizationImpl(component).doLayout();
		}
		
		// center graph layout
		GravistoService.getInstance().runAlgorithm(
				new CenterLayouterAlgorithm(), 
				graph,	
				new Selection(""), 
				null
		);
		
	}
	
	@Override
	public void reset() {
		// TODO Auto-generated method stub
		super.reset();
	}

}
