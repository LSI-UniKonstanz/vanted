// ==============================================================================
//
// CutAction.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: CutAction.java,v 1.6.4.1 2012/12/13 12:51:14 klapperipk Exp $

package org.graffiti.editor.actions;

import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ErrorMsg;
import org.graffiti.editor.MainFrame;
import org.graffiti.event.ListenerManager;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.help.HelpContext;
import org.graffiti.managers.IOManager;
import org.graffiti.plugin.actions.GraffitiAction;
import org.graffiti.plugin.actions.SelectionAction;
import org.graffiti.plugin.io.OutputSerializer;
import org.graffiti.selection.Selection;

/**
 * Represents a cut of graph elements action.
 * 
 * @version $Revision: 1.6.4.1 $
 */
public class CutAction extends SelectionAction {
	// ~ Constructors ===========================================================

	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new cut action.
	 * 
	 * @param mainFrame
	 *            DOCUMENT ME!
	 */
	public CutAction(MainFrame mainFrame) {
		super("edit.cut", mainFrame);
	}

	// ~ Methods ================================================================

	/**
	 * Returns the help context for the action.
	 * 
	 * @return HelpContext, the help context for the action
	 */
	@Override
	public HelpContext getHelpContext() {
		return null; // TODO
	}

	/**
	 * Executes this action.
	 * 
	 * @param e
	 *            DOCUMENT ME!
	 */
	public void actionPerformed(ActionEvent e) {
		Graph sourceGraph = getGraph();

		Selection selection = getSelection();
		if (selection.isEmpty())
			return;
		AdjListGraph copyGraph = new AdjListGraph(sourceGraph, new ListenerManager());

		try {
			String ext = "gml";
			IOManager ioManager = MainFrame.getInstance().getIoManager();
			OutputSerializer os = ioManager.createOutputSerializer("." + ext);
			new StringBuffer();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			if (selection.getElements().size() > 0 /* selection.getNodes().size() > 0 */) {

				// remove all other nodes from copied graph
				ArrayList<Long> validNodeIds = new ArrayList<Long>();
				for (org.graffiti.graph.Node n : selection.getNodes())
					validNodeIds.add(Long.valueOf(n.getID()));

				ArrayList<org.graffiti.graph.Node> toBeDeleted = new ArrayList<org.graffiti.graph.Node>();
				for (org.graffiti.graph.Node n : copyGraph.getNodes()) {
					if (!validNodeIds.contains(Long.valueOf(n.getID()))) {
						toBeDeleted.add(n);
					}
				}
				for (org.graffiti.graph.Node n : toBeDeleted) {
					copyGraph.deleteNode(n);
				}

				// remove all other edges from copied graph
				ArrayList<Long> validEdgeIds = new ArrayList<Long>();
				for (Edge curEdge : copyGraph.getEdges())
					validEdgeIds.add(Long.valueOf(curEdge.getID()));

				ArrayList<Edge> toBeDeletedEdges = new ArrayList<Edge>();
				for (Edge curEdge : copyGraph.getEdges()) {
					if (!validEdgeIds.contains(Long.valueOf(curEdge.getID()))) {
						toBeDeletedEdges.add(curEdge);
					}
				}
				for (Edge curEdge : toBeDeletedEdges) {
					copyGraph.deleteEdge(curEdge);
				}
			}
			// removed cut-action, when no elements are selected
			/*
			 * else selection.addAll(getGraph().getGraphElements());
			 */

			os.write(baos, copyGraph);
			ClipboardService.writeToClipboardAsText(baos.toString());
			/*
			 * this code didn't use the Undo Manager so it's outcommented Now CTRL+X and a
			 * following CTRL+Z (undo) works
			 */
			/*
			 * ArrayList<GraphElement> del = new ArrayList<GraphElement>();
			 * del.addAll(selection.getElements()); selection.clear();
			 * getGraph().deleteAll(del);
			 * MainFrame.getInstance().getSessionManager().getActiveSession().getActiveView(
			 * ).repaint(null);
			 */
			GraffitiAction.performAction("edit.delete");

		} catch (IOException ioe) {
			ErrorMsg.addErrorMessage(ioe.getLocalizedMessage());
		} catch (IllegalAccessException iae) {
			ErrorMsg.addErrorMessage(iae.getLocalizedMessage());
		} catch (InstantiationException ie) {
			ErrorMsg.addErrorMessage(ie.getLocalizedMessage());
		}

	}

	/**
	 * Sets the internal <code>enable</code> flag, which depends on the given list
	 * of selected items.
	 * 
	 * @param items
	 *            the items, which determine the internal state of the
	 *            <code>enable</code> flag.
	 */
	@Override
	protected void enable(List<?> items) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graffiti.plugin.actions.SelectionAction#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		try {
			Graph sourceGraph = MainFrame.getInstance().getActiveEditorSession().getGraph();
			return sourceGraph.getNumberOfNodes() > 0;
		} catch (NullPointerException npe) {
			return false;
		}
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
