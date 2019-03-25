/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 * Copyright (c) 2019 Computational Life Sciences, Konstanz University
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.ratioview;

import java.awt.geom.Point2D;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.KeyStroke;
import javax.swing.undo.CannotUndoException;

import org.AttributeHelper;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.algorithm.PreconditionException;

/**
 * @author Christian Klukas, Dimitar Garkov
 * @since 27.02.2006
 * @version 2.0
 * @vanted.revision 2.7.0 Overall revision to optimize; add support for built-in
 *                  algorithm undo/redo operations.
 */
public class ExchangeNodePositionsAlgorithm extends AbstractAlgorithm {

	/**
	 * A stack of changes containing previous positions, before the change.
	 */
	private ArrayDeque<HashMap<Node, Point2D>> undoStack = new ArrayDeque<>();

	@Override
	public String getName() {
		return "Exchange Positions";
	}

	@Override
	public String getCategory() {
		return "Network.Nodes";
	}

	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(Category.GRAPH, Category.LAYOUT));
	}

	@Override
	public void check() throws PreconditionException {
		super.check();
		if (graph == null)
			throw new PreconditionException("No graph available");
		if (selection.getNodes().size() < 2)
			throw new PreconditionException("Minimum 2 nodes must be selected");
	}

	@Override
	public boolean isLayoutAlgorithm() {
		return false;
	}

	@Override
	public KeyStroke getAcceleratorKeyStroke() {
		return KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F8, 0);
	}

	/**
	 * It swaps node positions in counter-clockwise direction.
	 */
	public void execute() {
		graph.getListenerManager().transactionStarted(this);
		try {
			HashMap<Node, Point2D> oldPositions = new HashMap<>();
			final Iterator<Node> nodes = selection.getNodes().iterator();
			Node first = selection.getNodes().iterator().next();
			while (nodes.hasNext()) {
				Node n1 = nodes.next();
				Point2D n1Pos = AttributeHelper.getPosition(n1);
				if (!oldPositions.containsKey(n1))
					oldPositions.put(n1, n1Pos);
				
				Node n2 = first;
				if (nodes.hasNext()) {
					n2 = nodes.next();
					if (!oldPositions.containsKey(n2))
						oldPositions.put(n2, AttributeHelper.getPosition(n2));
				}
				
				AttributeHelper.setPosition(n1, AttributeHelper.getPosition(n2));
				AttributeHelper.setPosition(n2, n1Pos);
			}
			undoStack.addFirst(oldPositions);
		} finally {
			graph.getListenerManager().transactionFinished(this);
		}
	}

	@Override
	public boolean doesUndo() {
		return true;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();

		HashMap<Node, Point2D> oldPositions = undoStack.removeFirst();
		Collection<Node> nodes = recycledSelection.getNodes();
		Graph recycledGraph = nodes.iterator().next().getGraph();
		recycledGraph.getListenerManager().transactionStarted(this);
		try {
			for (Node n : nodes) {
				Point2D oldPos = oldPositions.get(n);
				if (oldPos != null)
					AttributeHelper.setPosition(n, oldPos);
			}
		} finally {
			recycledGraph.getListenerManager().transactionFinished(this);
		}
	}
}
