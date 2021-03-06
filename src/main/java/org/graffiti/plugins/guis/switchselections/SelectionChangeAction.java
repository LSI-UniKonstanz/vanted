// ==============================================================================
//
// SelectionChangeAction.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: SelectionChangeAction.java,v 1.6 2010/12/22 13:05:58 klukas Exp $

package org.graffiti.plugins.guis.switchselections;

import java.awt.event.ActionEvent;

import org.graffiti.help.HelpContext;
import org.graffiti.plugin.actions.GraffitiAction;
import org.graffiti.selection.Selection;
import org.graffiti.session.EditorSession;

/**
 * DOCUMENT ME!
 * 
 * @author $Author: klukas $
 * @version $Revision: 1.6 $ $Date: 2010/12/22 13:05:58 $
 */
public class SelectionChangeAction extends GraffitiAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5776600348691541324L;
	
	// ~ Instance fields ========================================================
	/** DOCUMENT ME! */
	private EditorSession session;
	
	/** DOCUMENT ME! */
	private Selection selection;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Creates a new SelectionChangeAction object.
	 * 
	 * @param sel
	 *           DOCUMENT ME!
	 * @param sess
	 *           DOCUMENT ME!
	 */
	public SelectionChangeAction(Selection sel, EditorSession sess) {
		super(sel.getName(), null, null);
		this.session = sess;
		this.selection = sel;
	}
	
	// ~ Methods ================================================================
	
	/**
	 * @see javax.swing.Action#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		return super.enabled;
	}
	
	/**
	 * @see org.graffiti.plugin.actions.GraffitiAction#getHelpContext()
	 */
	@Override
	public HelpContext getHelpContext() {
		return null;
	}
	
	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		Selection clonedSel = (Selection) selection.clone();
		clonedSel.setName(selection.getName());
		this.session.getSelectionModel().setActiveSelection(clonedSel);
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
