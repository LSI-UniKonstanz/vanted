// ==============================================================================
//
// DeleteAttributeAction.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: DeleteAttributeAction.java,v 1.7 2010/12/22 13:05:53 klukas Exp $

package org.graffiti.editor.actions;

import java.awt.event.ActionEvent;
import java.util.List;

import org.graffiti.editor.MainFrame;
import org.graffiti.help.HelpContext;
import org.graffiti.plugin.actions.SelectionAction;
import org.graffiti.selection.SelectionEvent;

/**
 * DOCUMENT ME!
 * 
 * @author $Author: klukas $
 * @version $Revision: 1.7 $ $Date: 2010/12/22 13:05:53 $
 */
public class DeleteAttributeAction extends SelectionAction {
	// ~ Constructors ===========================================================

	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new delete attribute action.
	 * 
	 * @param mainFrame
	 *            DOCUMENT ME!
	 */
	public DeleteAttributeAction(MainFrame mainFrame) {
		super("action.delete.attribute", mainFrame);
	}

	// ~ Methods ================================================================

	/**
	 * Returns the help context of this action.
	 * 
	 * @return the help context of this action.
	 */
	@Override
	public HelpContext getHelpContext() {
		return null; // TODO
	}

	/**
	 * Returns the name of this action.
	 * 
	 * @return the name of this action.
	 */
	@Override
	public String getName() {
		return null;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param e
	 *            DOCUMENT ME!
	 */
	public void actionPerformed(ActionEvent e) {
		// TODO
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param e
	 *            DOCUMENT ME!
	 */
	public void selectionChanged(SelectionEvent e) {
		// TODO
	}

	/**
	 * Returns <code>true</code>, if this action should survive a focus change in
	 * the editor.
	 * 
	 * @return <code>true</code>, if this action should survive a focus chage in the
	 *         editor.
	 */
	@Override
	public boolean surviveFocusChange() {
		return true;
	}

	/**
	 * Sets the internal <code>enable</code> flag, which depends on the given list
	 * of selected items.
	 * 
	 * @param selectedItems
	 *            the items, which determine the internal state of
	 *            the<code>enable</code> flag.
	 */
	@Override
	protected void enable(List<?> selectedItems) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graffiti.plugin.actions.SelectionAction#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		//
		return false;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
