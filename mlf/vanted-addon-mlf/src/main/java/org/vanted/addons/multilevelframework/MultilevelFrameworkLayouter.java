/*************************************************************************************
 * The Exemplary Add-on is (c) 2008-2011 Plant Bioinformatics Group, IPK Gatersleben,
 * http://bioinformatics.ipk-gatersleben.de
 * The source code for this project, which is developed by our group, is
 * available under the GPL license v2.0 available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html. By using this
 * Add-on and VANTED you need to accept the terms and conditions of this
 * license, the below stated disclaimer of warranties and the licenses of
 * the used libraries. For further details see license.txt in the root
 * folder of this project.
 ************************************************************************************/
package org.vanted.addons.multilevelframework;

import java.util.Arrays;

import org.AttributeHelper;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.JComponentParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.view.View;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;

import javax.swing.*;

public class MultilevelFrameworkLayouter extends AbstractEditorAlgorithm {
	
	public MultilevelFrameworkLayouter() {
		super();
	}

	@Override
	public String getDescription() {
		return "<html>" + "Multilevel Framework";
	}

	/**
	 * @see super#reset()
	 */
	@Override
	public void reset() {
		super.reset();
	}
	
	/**
	 * @return the algorithm's name
	 */
	public String getName() {
		return "Multilevel Framework Layouter";
	}
	
	/**
	 * Checks, if a graph was given and that the radius is positive.
	 * 
	 * @throws PreconditionException
	 *            if no graph was given during algorithm invocation or the
	 *            radius is negative
	 */
	@Override
	public void check() throws PreconditionException {
		PreconditionException errors = new PreconditionException();
		
		if (graph == null) {
			errors.add("No graph available!");
		}
		
		if (!errors.isEmpty()) {
			throw errors;
		}
		
		if (graph.getNumberOfNodes() <= 0) {
			throw new PreconditionException("The graph is empty. Cannot run layouter.");
		}
	}
	
	/**
	 * Performs the layout.
	 */
	public void execute() {
		AdjListGraph alg = new AdjListGraph();
		Node n1 = alg.addNode();
		Node n2 = alg.addNode();
		Node n3 = alg.addNode();
		Node n4 = alg.addNode();
		AttributeHelper.setLabel(n1, "1");
		AttributeHelper.setLabel(n1, "1");
		MultilevelGraph mlg = new MultilevelGraph(alg);
		mlg.newCoarseningLevel();
		MergedNode mn1 = mlg.addNode(Arrays.asList(n1,n2));
		MergedNode mn2 = mlg.addNode(Arrays.asList(n3,n4));
		mlg.addEdge(mn1, mn2);
		assert mlg.isComplete();
		GraphHelper.diplayGraph(mlg.getTopLevel());

//		Collection<Node> workNodes = new ArrayList<Node>();
//		if (selection.getNodes().size() > 0)
//			workNodes.addAll(selection.getNodes());
//		else
//			workNodes.addAll(graph.getNodes());
//
//		workNodes = GraphHelper.getVisibleNodes(workNodes);
//
//		int numberOfNodes = workNodes.size();
//		double singleStep = 2 * Math.PI / numberOfNodes;
//
//		Vector2d ctr = NodeTools.getCenter(workNodes);
//		HashMap<Node, Vector2d> nodes2newPositions = new HashMap<Node, Vector2d>();
//
//		int i = 0;
//		for (Node n : workNodes) {
//			double newX = Math.sin(singleStep * i) * defaultRadius + ctr.x;
//			double newY = Math.cos(singleStep * i) * defaultRadius + ctr.y;
//			nodes2newPositions.put(n, new Vector2d(newX, newY));
//			i++;
//		}
//
//		GraphHelper.applyUndoableNodePositionUpdate(nodes2newPositions,
//							getName());
	}
	
	/**
	 * @return the parameter array
	 */
	@Override
	public Parameter[] getParameters() {
		// TODO
		JComponentParameter param =
				new JComponentParameter(new JButton("text"), "name", "description");

		return new Parameter[] { param };
	}
	
	/**
	 * Sets the radius parameter to the given value.
	 * 
	 * @param params
	 *           An array with exact one DoubleParameter.
	 */
	@Override
	public void setParameters(Parameter[] params) {
		this.parameters = params;
	}
	
	/*
	 * @see org.graffiti.plugin.algorithm.Algorithm#getCategory()
	 */
	@Override
	public String getCategory() {
		return "Layout";
	}
	
	/**
	 * This makes sure the algorithm is moved to the layout-tab of VANTED
	 */
	@Override
	public boolean isLayoutAlgorithm() {
		return true;
	}
	
	public boolean activeForView(View v) {
		return v != null;
	}
}
