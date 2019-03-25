// ==============================================================================
//
// AbstractAlgorithm.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: AbstractAlgorithm.java,v 1.9 2010/12/22 13:05:32 klukas Exp $

package org.graffiti.plugin.algorithm;

import java.awt.event.ActionEvent;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Set;

import javax.swing.KeyStroke;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import org.ErrorMsg;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.selection.Selection;

/**
 * Implements some empty versions of non-obligatory methods.
 * 
 * @version 2.0
 * @vanted.revision 2.7.0 Default support for Undo, Redo.
 */
public abstract class AbstractAlgorithm implements Algorithm, UndoableEdit {
	// ~ Instance fields ========================================================

	/** The graph on which the algorithm will work. */
	protected Graph graph;

	/** The selection on which the algorithm might work. */
	protected Selection selection;

	/** The parameters this algorithm can use. */
	protected Parameter[] parameters;

	protected ActionEvent actionEvent = null;

	// ~ Methods ================================================================

	/**
	 * @param params Parameter array
	 */
	public void setParameters(Parameter[] params) {
		this.parameters = params;
	}

	/**
	 * Default: no accelerator for the menu item, created for this algorithm.
	 */
	public KeyStroke getAcceleratorKeyStroke() {
		return null;
	}

	/**
	 * The algorithm description.
	 * 
	 * @return String, could be HTML-styled.
	 */
	public String getDescription() {
		return null;
	}

	/**
	 * Default: no icon next to the menu item, which is created for this algorithm.
	 */
	public boolean showMenuIcon() {
		return false;
	}

	protected Collection<Node> getSelectedOrAllNodes() {
		if (selection == null || selection.getNodes().size() <= 0)
			return graph.getNodes();
		else
			return selection.getNodes();
	}

	protected Collection<GraphElement> getSelectedOrAllGraphElements() {
		if (selection == null || selection.getElements().size() <= 0)
			return graph.getGraphElements();
		else
			return selection.getElements();
	}

	public Parameter[] getParameters() {
		return this.parameters;
	}

	public void attach(Graph graph, Selection selection) {
		this.graph = graph;
		this.selection = selection;
	}

	/**
	 * @throws PreconditionException
	 */
	public void check() throws PreconditionException {
		boolean v = false;
		if (v)
			throw new PreconditionException();
	}

	public String getCategory() {
		return null;
	}

	@Override
	public Set<Category> getSetCategory() {
		return null;
	}

	/**
	 * For backwards compatibility the standard implementation will return the
	 * Category
	 */
	@Override
	public String getMenuCategory() {
		return getCategory();
	}

	public void reset() {
		this.graph = null;
		this.parameters = null;
		this.actionEvent = null;
		this.selection = null;
	}

	public boolean isLayoutAlgorithm() {
		return false;
	}

	public ActionEvent getActionEvent() {
		return actionEvent;
	}

	public void setActionEvent(ActionEvent a) {
		actionEvent = a;
	}

	public boolean mayWorkOnMultipleGraphs() {
		return false;
	}

	/**
	 * Indicates, if an algorithm is always executable - even without an active
	 * session.
	 * 
	 * @return true, when there are no preconditions
	 */
	public boolean isAlwaysExecutable() {
		return false;
	}

	// ~ UnboableEdit Impl. ========================================================

	/**
	 * The number of undo operations in regards to stack selections. This is equal
	 * to the number of times <code>execute()</code> has run.
	 */
	private ArrayDeque<Selection> undoStack = new ArrayDeque<>();

	/**
	 * The number of redo operations in terms of selections. This is equal to the
	 * number of times <code>undo()</code> has run.
	 */
	private ArrayDeque<Selection> redoStack = new ArrayDeque<>();

	/**
	 * This is the selection handle for graph elements of the UndoableEdit - both
	 * Undo or Redo.
	 */
	protected Selection recycledSelection;

	/**
	 * <p>
	 * Must return <code>true</code> to allow Undo/Redo edits.
	 * </p>
	 * 
	 * <b>Important:</b> If you use {@linkplain GraphHelper} for undoable edits or
	 * any other UndoableEdit tools, do NOT return <code>true</code>, as those are
	 * handled in that given class.
	 * 
	 * @return <code>false</code> by default, meaning the algorithm is not undoable
	 */
	public boolean doesUndo() {
		return false;
	}

	@Override
	public boolean canUndo() {
		return !undoStack.isEmpty();
	}

	@Override
	public boolean canRedo() {
		return !redoStack.isEmpty();
	}

	/**
	 * Marks the performed algorithm execution as done, so that the operation can
	 * later undo.
	 */
	public void markExecutionDone() {
		undoStack.addFirst(new Selection(selection.getElements()));
	}

	/**
	 * Marks the performed algorithm undo as done, so that the operation can later
	 * redo.
	 */
	public void markUndoDone() {
		redoStack.addFirst(new Selection(recycledSelection.getElements()));
	}
	
	/**
	 * Marks the performed algorithm redo as done, so that the operation can later
	 * undo.
	 */
	public void markRedoDone() {
		undoStack.addFirst(new Selection(recycledSelection.getElements()));
	}

	@Override
	public void die() {
		undoStack.clear();
		redoStack.clear();
		recycledSelection = null;
		reset();
	}

	@Override
	public String getPresentationName() {
		return getName();
	}

	@Override
	public String getRedoPresentationName() {
		return "Redo " + getName().toLowerCase();
	}

	@Override
	public String getUndoPresentationName() {
		return "Undo " + getName().toLowerCase();
	}

	/**
	 * Whether this UndoableEdit should be considered on its own for Undo/Redo, when
	 * performing any of them. Otherwise, it will get undone/redone once a
	 * significant edit takes place. Default is true, as all algorithms represent
	 * complex atomic actions.
	 */
	@Override
	public boolean isSignificant() {
		return true;
	}

	/**
	 * <p>
	 * Default behaviour is to run <code>execute()</code> again.
	 * </p>
	 * 
	 * If implementing, don't forget to call <code>super()</code> at the top of your
	 * implementation.
	 */
	@Override
	public void redo() throws CannotRedoException {
		if (!canRedo())
			throw new CannotRedoException();
		recycledSelection = redoStack.removeFirst();
		// Default Redo behaviour occurs only here
		try {
			if (getClass().getMethod("redo").getDeclaringClass().equals(AbstractAlgorithm.class)) {
				final Selection cacheSelection = selection;
				final Graph cacheGraph = graph;
				selection = recycledSelection;
				graph = selection.getGraph().iterator().next();
				execute();
				selection = cacheSelection;
				graph = cacheGraph;
			}
		} catch (NoSuchMethodException | SecurityException e) {
			ErrorMsg.addErrorMessage(e);
		}
		// Ideally, it would be called last in redo(), but since it bares information
		// only to other methods, it is generally enough to call it anywhere in redo().
		markRedoDone();

		// your code
	}

	/**
	 * <p>
	 * Extend with your Undo functionality.
	 * </p>
	 * 
	 * Don't forget to call <code>super()</code> at the top of your implementation.
	 */
	@Override
	public void undo() throws CannotUndoException {
		if (!canUndo())
			throw new CannotUndoException();
		recycledSelection = undoStack.removeFirst();
		// Ideally, it would be called last in undo(), but since it bares information
		// only to other methods, it is generally enough to call it anywhere in undo().
		markUndoDone();

		// your code
	}

	@Override
	public boolean addEdit(UndoableEdit anEdit) {
		return false;
	}

	@Override
	public boolean replaceEdit(UndoableEdit anEdit) {
		return false;
	}

}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
