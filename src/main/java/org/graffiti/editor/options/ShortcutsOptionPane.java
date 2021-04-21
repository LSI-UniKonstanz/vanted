// ==============================================================================
//
// ShortcutsOptionPane.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: ShortcutsOptionPane.java,v 1.7 2010/12/22 13:05:54 klukas Exp $

package org.graffiti.editor.options;

import java.util.List;

import javax.swing.JComponent;
import javax.swing.table.AbstractTableModel;

import org.graffiti.options.AbstractOptionPane;

/**
 * An option pane for shortcuts.
 * 
 * @author flierl
 * @version $Revision: 1.7 $
 */
public class ShortcutsOptionPane extends AbstractOptionPane {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6449091056036042475L;
	
	/**
	 * Constructor for ShortcutsOptionPane.
	 */
	public ShortcutsOptionPane() {
		super("shortcuts");
	}
	
	@Override
	protected void initDefault() {
		// TODO
	}
	
	@Override
	protected void saveDefault() {
		// TODO
	}
	
	// ~ Inner Classes ==========================================================
	
	/**
	 * The table of shortcuts.
	 */
	protected class ShortcutsModel extends AbstractTableModel {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -6780864845253368682L;
		/** Contains a list of key bindings. */
		private List<?> bindings;
		
		/**
		 * Constructs a new shortcuts model.
		 * 
		 * @param name
		 *           the name of the model.
		 * @param bindings
		 *           list of keybindings.
		 */
		ShortcutsModel(String name, List<?> bindings) {
			// TODO
		}
		
		/**
		 * Return the number of columns for this table (3).
		 * 
		 * @return DOCUMENT ME!
		 */
		public int getColumnCount() {
			return 3;
		}
		
		/**
		 * Returns the number of rows of this table.
		 * 
		 * @return the number of rows of this table.
		 */
		public int getRowCount() {
			return 0;
			// return bindings.size();
		}
		
		/**
		 * DOCUMENT ME!
		 * 
		 * @param row
		 *           DOCUMENT ME!
		 * @param col
		 *           DOCUMENT ME!
		 * @return DOCUMENT ME!
		 */
		public Object getValueAt(int row, int col) {
			return null; // TODO
		}
	}
	
	public String getCategory() {
		//
		return null;
	}
	
	public String getOptionName() {
		//
		return null;
	}
	
	public void init(JComponent options) {
		//
	}
	
	public void save(JComponent options) {
		//
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
