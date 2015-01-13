// ==============================================================================
//
// LoadSaveOptionsPane.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: LoadSaveOptionsPane.java,v 1.7 2010/12/22 13:05:54 klukas Exp $

package org.graffiti.editor.options;

import javax.swing.JComponent;

import org.graffiti.options.AbstractOptionPane;

/**
 * Handles some loading and saving stuff, e.g.: autosave, backups,
 * backupDirectory, backupSuffix.
 * 
 * @version $Revision: 1.7 $
 */
public class LoadSaveOptionsPane
					extends AbstractOptionPane {
	// ~ Constructors ===========================================================
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructor for LoadSaveOptionsPane.
	 */
	public LoadSaveOptionsPane() {
		super("loadsave");
		
		// TODO
	}
	
	// ~ Methods ================================================================
	
	/*
	 * @see org.graffiti.options.AbstractOptionPane#initDefault()
	 */
	@Override
	protected void initDefault() {
		// TODO
	}
	
	/*
	 * @see org.graffiti.options.AbstractOptionPane#saveDefault()
	 */
	@Override
	protected void saveDefault() {
		// TODO
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.options.OptionPane#getCategory()
	 */
	public String getCategory() {
		//
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.options.OptionPane#getOptionName()
	 */
	public String getOptionName() {
		//
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.options.OptionPane#init(javax.swing.JComponent)
	 */
	public void init(JComponent options) {
		//
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.options.OptionPane#save(javax.swing.JComponent)
	 */
	public void save(JComponent options) {
		//
		
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
