/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.grid_placement;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JMenuItem;

import org.AttributeHelper;
import org.Release;
import org.ReleaseInfo;
import org.Vector2d;
import org.graffiti.editor.GravistoService;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.algorithm.ProvidesNodeContextMenu;
import org.graffiti.plugin.parameter.DoubleParameter;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;

/**
 * @author Christian Klukas
 * @vanted.revision 2.6.5
 */
public class GridPlacementAlgorithm extends AbstractAlgorithm implements ProvidesNodeContextMenu, ActionListener {
	
	private double xgrid = 10;
	private double ygrid = 10;
	
	/**
	 * Returns the name of the algorithm.
	 * 
	 * @return Move Nodes to Grid-Points
	 */
	public String getName() {
		return "Move Nodes to Grid-Points";
	}
	
	@Override
	public String getCategory() {
		return "Network";
	}
	
	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(Category.GRAPH, Category.LAYOUT));
	}
	
	@Override
	public boolean isLayoutAlgorithm() {
		return true;
	}
	
	/**
	 * Checks, if a graph was given.
	 * 
	 * @throws PreconditionException
	 *            if no graph was given during algorithm invocation
	 */
	@Override
	public void check() throws PreconditionException {
		PreconditionException errors = new PreconditionException();
		
		if (graph == null) {
			errors.add("No graph available!");
			throw errors;
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
		HashMap<Node, Vector2d> nodes2newPositions = new HashMap<Node, Vector2d>();
		for (Node n : getSelectedOrAllNodes()) {
			Point2D p = AttributeHelper.getPosition(n);
			nodes2newPositions.put(n, new Vector2d(p.getX() - (p.getX() % xgrid), p.getY() - (p.getY() % ygrid)));
		}
		GraphHelper.applyUndoableNodePositionUpdate(nodes2newPositions, getName());
	}
	
	@Override
	public Parameter[] getParameters() {
		DoubleParameter xDistanceParam = new DoubleParameter("Grid X",
				"Modifies the node placement in horizontal direction.");
		xDistanceParam.setValue(Double.valueOf(xgrid));
		DoubleParameter yDistanceParam = new DoubleParameter("Grid Y",
				"Modifies the node placement in vertical direction.");
		yDistanceParam.setValue(Double.valueOf(ygrid));
		return new Parameter[] { xDistanceParam, yDistanceParam };
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		xgrid = ((DoubleParameter) params[0]).getDouble().doubleValue();
		ygrid = ((DoubleParameter) params[1]).getDouble().doubleValue();
	}
	
	public JMenuItem[] getCurrentNodeContextMenuItem(Collection selectedNodes) {
		if (ReleaseInfo.getRunningReleaseStatus() == Release.DEBUG) {
			JMenuItem myMenuItem = new JMenuItem("Grid-Placement");
			myMenuItem.addActionListener(this);
			return new JMenuItem[] { myMenuItem };
		} else
			return null;
	}
	
	public void actionPerformed(ActionEvent e) {
		GravistoService.getInstance().runPlugin(getName(), null, e);
	}
}
