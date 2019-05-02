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
package org.vanted.addons.exampleaddon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.AttributeHelper;
import org.Vector2d;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.DoubleParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.view.View;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;

/**
 * Just a small class to show how you can add layout add-ons. Implements the
 * usual {@link AbstractEditorAlgorithm}.
 * 
 * @author Christian Klukas
 */
public class MyCircleLayout extends AbstractEditorAlgorithm {
	
	private double defaultRadius = 250;
	
	/**
	 * Creates a new CircleLayouterAlgorithm object.
	 */
	public MyCircleLayout() {
		super();
	}
	
	@Override
	public String getDescription() {
		return "<html>" + "Specify the radius of the circle. "
							+ "Layouter works on current selection "
							+ "or whole graph, if selection is empty.";
	}
	
	/**
	 * Creates a new CircleLayouterAlgorithm object.
	 * 
	 * @param defaultRadius
	 *           a value for the radius
	 */
	public MyCircleLayout(double defaultRadius) {
		super();
		this.defaultRadius = defaultRadius;
	}
	
	@Override
	public void reset() {
		super.reset();
	}
	
	/**
	 * Returns the name of the algorithm.
	 * 
	 * @return the name of the algorithm
	 */
	public String getName() {
		return "MyCircle Layouter (Add-on Example)";
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
			throw new PreconditionException(
								"The graph is empty. Cannot run layouter.");
		}
	}
	
	public void setRadius(double radius) {
		this.defaultRadius = radius;
	}
	
	/**
	 * Performs the layout.
	 */
	public void execute() {
		Collection<Node> workNodes = new ArrayList<Node>();
		if (selection.getNodes().size() > 0)
			workNodes.addAll(selection.getNodes());
		else
			workNodes.addAll(graph.getNodes());
		
		workNodes = GraphHelper.getVisibleNodes(workNodes);
		
		int numberOfNodes = workNodes.size();
		double singleStep = 2 * Math.PI / numberOfNodes;
		
		Vector2d ctr = NodeTools.getCenter(workNodes);
		HashMap<Node, Vector2d> nodes2newPositions = new HashMap<Node, Vector2d>();
		
		int i = 0;
		for (Node n : workNodes) {
			double newX = Math.sin(singleStep * i) * defaultRadius + ctr.x;
			double newY = Math.cos(singleStep * i) * defaultRadius + ctr.y;
			nodes2newPositions.put(n, new Vector2d(newX, newY));
			i++;
		}
		
		GraphHelper.applyUndoableNodePositionUpdate(nodes2newPositions,
							getName());
	}
	
	public static double energyOfNode(Node node, double newX, double newY) {
		double distanceToOtherNodes = 0;
		for (Node n : node.getNeighbors())
			distanceToOtherNodes += getDistance(newX, newY, n);
		return distanceToOtherNodes;
	}
	
	public static double getDistance(double x, double y, Node b) {
		Vector2d posA = new Vector2d(x, y);
		Vector2d posB = AttributeHelper.getPositionVec2d(b);
		return Math.sqrt((posA.x - posB.x) * (posA.x - posB.x)
							+ (posA.y - posB.y) * (posA.y - posB.y));
	}
	
	/**
	 * Returns the parameter object for the radius.
	 * 
	 * @return the parameter array
	 */
	@Override
	public Parameter[] getParameters() {
		DoubleParameter radiusParam = new DoubleParameter("Radius",
							"The radius of the circle.");
		
		radiusParam.setDouble(defaultRadius);
		
		return new Parameter[] { radiusParam };
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
		defaultRadius = ((DoubleParameter) params[0]).getDouble().doubleValue();
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
