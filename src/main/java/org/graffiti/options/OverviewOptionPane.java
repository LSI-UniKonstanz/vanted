// ==============================================================================
//
// OverviewOptionPane.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: OverviewOptionPane.java,v 1.7 2010/12/22 13:05:35 klukas Exp $

package org.graffiti.options;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

import org.vanted.scaling.DPIHelper;
import org.vanted.scaling.scaler.component.JTextComponentScaler;

/**
 * The overview pane for the options dialog.
 * 
 * @version $Revision: 1.7 $
 */
public class OverviewOptionPane
					extends AbstractOptionPane {
	// ~ Constructors ===========================================================
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructor for OverviewOptionPane.
	 */
	public OverviewOptionPane() {
		super(sBundle.getString("options.overview.title"));
	}
	
	// ~ Methods ================================================================
	
	/*
	 * @see org.graffiti.options.AbstractOptionPane#initDefault()
	 */
	@Override
	protected void initDefault() {
		setLayout(new BorderLayout());
		
		// add a JEditorPane, which contains an overview html page.
		JEditorPane ep = new JEditorPane();
		
		//scale the newly initialized component
		JTextComponentScaler epScaler = new JTextComponentScaler(DPIHelper.getDPIScalingRatio());
		epScaler.scaleComponents(ep);
		
		try {
			ep.setPage(sBundle.getRes("options.overview.html"));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		ep.setBackground(new java.awt.Color(200,221,242)); //#C8DDF2
		ep.setEditable(false);
		
		JScrollPane scroller = new JScrollPane(ep);
		scroller.setPreferredSize(new Dimension(400, 0));
		
		add(BorderLayout.CENTER, scroller);
	}
	
	/*
	 * @see org.graffiti.options.AbstractOptionPane#saveDefault()
	 */
	@Override
	protected void saveDefault() {
		/* do nothing */
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.options.OptionPane#getCategory()
	 */
	public String getCategory() {
		return "Gravisto Passau";
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.options.OptionPane#getOptionName()
	 */
	public String getOptionName() {
		return "Default Option";
	}


}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
