// ==============================================================================
//
// RunAlgorithm.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: RunAlgorithm.java,v 1.13 2010/12/22 13:05:53 klukas Exp $

package org.graffiti.editor.actions;

import java.awt.event.ActionEvent;

import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.help.HelpContext;
import org.graffiti.managers.EditComponentManager;
import org.graffiti.plugin.actions.GraffitiAction;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.algorithm.EditorAlgorithm;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.kegg_bar.SelectWindowsAlgorithm;

/**
 * Runs an algorithm.
 * 
 * @version $Revision: 1.13 $
 */
public class RunAlgorithm extends GraffitiAction {
	// ~ Instance fields ========================================================
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6118584661754407756L;
	
	/** The class name of the algorithm to run. */
	private final String algorithmClassName;
	
	private final Algorithm algorithm;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructor for RunAlgorithm.
	 * 
	 * @param algorithmClassName
	 *           DOCUMENT ME!
	 * @param name
	 * @param mainFrame
	 * @param editComponentManager
	 *           DOCUMENT ME!
	 */
	public RunAlgorithm(String algorithmClassName, String name, MainFrame mainFrame,
			EditComponentManager editComponentManager, Algorithm instance) {
		super(name, mainFrame, null);
		this.algorithmClassName = algorithmClassName;
		this.algorithm = instance;
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Get the name of the algorithm that this action will run.
	 * 
	 * @return the name of the algorithm that this action will run.
	 */
	public String getAlgorithmClassName() {
		return algorithmClassName;
	}
	
	/**
	 * @see javax.swing.Action#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		if (algorithm instanceof EditorAlgorithm) {
			// editor algorithm can decide by himself whether to be active for view
			EditorAlgorithm ea = (EditorAlgorithm) algorithm;
			if (mainFrame.isSessionActive())
				return ea.activeForView(mainFrame.getActiveSession().getActiveView());
			else
				return ea.activeForView(null);
		} else if (algorithm instanceof AbstractAlgorithm) {
			boolean alwaysExecutable = ((AbstractAlgorithm) algorithm).isAlwaysExecutable();
			if (alwaysExecutable)
				return true;
			else {
				/**
				 * A special case, because this given algorithm is strictly dependent on the
				 * #DesktopFrames. Thus, not entirely session-dependent and is to be disabled,
				 * when e.g. one uses only detached frames.
				 */
				if (algorithm instanceof SelectWindowsAlgorithm && mainFrame.getDesktop().getAllFrames().length < 1)
					return false;
				
				if (!mainFrame.isSessionActive())
					return false;
			}
			return true;
			
		} else {
			if (!mainFrame.isSessionActive())
				return false;
			return true;
		}
	}
	
	/**
	 * @see org.graffiti.plugin.actions.GraffitiAction#getHelpContext()
	 */
	@Override
	public HelpContext getHelpContext() {
		return null;
	}
	
	/**
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent a) {
		algorithm.setActionEvent(a);
		try {
			GravistoService.getInstance().runAlgorithm(algorithm, true, a);
		} catch (Exception err) {
			MainFrame.showMessageDialog("Unexpected exception: " + err.toString(), "Error");
			err.printStackTrace();
		} catch (Error err) {
			MainFrame.showMessageDialog("Unexpected error: " + err.toString(), "Error");
			err.printStackTrace();
		}
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
