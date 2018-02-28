// ==============================================================================
//
// ExitAction.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: ExitAction.java,v 1.6 2010/12/22 13:05:53 klukas Exp $

package org.graffiti.editor.actions;

import java.awt.event.ActionEvent;

import org.graffiti.editor.MainFrame;
import org.graffiti.help.HelpContext;
import org.graffiti.plugin.actions.GraffitiAction;

/**
 * Exits the editor.
 */
public class ExitAction extends GraffitiAction {
	// ~ Constructors ===========================================================

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new ExitAction object.
	 * 
	 * @param mainFrame
	 *            DOCUMENT ME!
	 */
	public ExitAction(MainFrame mainFrame) {
		super("file.exit", mainFrame, "filemenu_exit");
	}

	// ~ Methods ================================================================

	/**
	 * @see javax.swing.Action#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		return true;
	}

	/**
	 * @see org.graffiti.plugin.actions.GraffitiAction#getHelpContext()
	 */
	@Override
	public HelpContext getHelpContext() {
		return null;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param e
	 *            DOCUMENT ME!
	 */
	public void actionPerformed(ActionEvent e) {
		mainFrame.closeGravisto();
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
