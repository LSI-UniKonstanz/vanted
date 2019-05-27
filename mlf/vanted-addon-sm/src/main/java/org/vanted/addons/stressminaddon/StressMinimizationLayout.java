package org.vanted.addons.stressminaddon;

import org.graffiti.attributes.Attribute;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.view.View;

import java.util.*;

/**
 * Implements a version of a stress minimization add-on that can be used
 * in VANTED.
 */
public class StressMinimizationLayout extends AbstractEditorAlgorithm {

	/**
	 * Path of an attribute that is set to the index of an node.
	 */
	public static final String INDEX_ATTRIBUTE =
			"StressMinimization" + Attribute.SEPARATOR + "index";
	
	/**
	 * Creates a new {@link StressMinimizationLayout} object.
	 */
	public StressMinimizationLayout() {
		super();
	}
	
	@Override
	public String getDescription() {
		return ""; // TODO
	}
	
	/**
	 * Returns the name of the algorithm.
	 * 
	 * @return the name of the algorithm
	 */
	public String getName() {
		return "Stress Minimization";
	}
	
	/**
	 * Checks, if a graph was given.
	 * 
	 * @throws PreconditionException
	 *            if no graph was given during algorithm invocation or the
	 *            radius is negative
	 */
	@Override
	public void check() throws PreconditionException {
		if (graph == null) {
			throw new PreconditionException("No graph available!");
		}
		if (graph.getNumberOfNodes() <= 0) {
			throw new PreconditionException(
								"The graph is empty. Cannot run layouter.");
		}
	}
	
	/**
	 * Performs the layout.
	 * @author Jannik
	 */
	public void execute() {
		ArrayList<Node> pureNodes;
		if (selection.isEmpty()) {
			pureNodes = new ArrayList<>(graph.getNodes());
		} else {
			pureNodes = new ArrayList<>(selection.getNodes());
		}

		List<Node> nodes = Collections.unmodifiableList(pureNodes);
		// Set positions attribute for hopefully better handling
		for (int pos = 0; pos < nodes.size(); pos++) {
			nodes.get(pos).setInteger(StressMinimizationLayout.INDEX_ATTRIBUTE, pos);
			System.out.println(nodes.get(pos).getAttribute(INDEX_ATTRIBUTE));
		}

		//////////////////////////////
		// TODO implement algorithm //
		//////////////////////////////


		// Reset attributes
		for (Node node : nodes) {
			node.removeAttribute(StressMinimizationLayout.INDEX_ATTRIBUTE);
		}
	}
	
	@Override
	public Parameter[] getParameters() {
		// TODO
	    return null;
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		// TODO
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getCategory()
	 */
	@Override
	public String getCategory() {
		return "Layout";
	}
	
	/**
	 * This method is important, because it will move the algorithm to the layout-tab of Vanted
	 */
	@Override
	public boolean isLayoutAlgorithm() {
		return true;
	}
	
	public boolean activeForView(View v) {
		return v != null;
	}
}
