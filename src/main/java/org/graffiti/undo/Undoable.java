// ==============================================================================
//
// Undoable.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: Undoable.java,v 1.4 2010/12/22 13:05:35 klukas Exp $

package org.graffiti.undo;

import javax.swing.undo.UndoableEditSupport;

/**
 * This interface should be implemented by all classes that provide <code>UndoableEdit</code>s for their actions.
 * 
 * @version $Revision: 1.4 $
 */
public interface Undoable {
	// ~ Methods ================================================================
	
	/**
	 * Sets the undo support object this object uses. The undo support object
	 * handles the <code>UndoableEditListeners</code>.
	 * 
	 * @param us
	 *           the undo support object this object uses.
	 */
	public void setUndoSupport(UndoableEditSupport us);
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
