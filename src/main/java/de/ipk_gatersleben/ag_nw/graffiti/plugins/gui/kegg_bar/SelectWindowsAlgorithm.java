/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 13.02.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.kegg_bar;

import java.beans.PropertyVetoException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JInternalFrame;

import org.ErrorMsg;
import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.ObjectListParameter;
import org.graffiti.plugin.parameter.Parameter;

/**
 * @author klukas
 */
public class SelectWindowsAlgorithm extends AbstractAlgorithm {
	
	@Override
	public boolean isAlwaysExecutable() {
		// do not execute when no internal frames present
		if (MainFrame.getInstance().getDesktop().getAllFrames().length < 1)
			return false;
		
		return super.isAlwaysExecutable();
	}
	
	private JInternalFrame desiredWindow;
	
	public String getName() {
		return "Select Window";
	}
	
	@Override
	public Parameter[] getParameters() {
		return new Parameter[] { new ObjectListParameter(MainFrame.getInstance().getDesktop().getAllFrames()[0],
				"Editor Window", "Select the window which will be put to front.",
				MainFrame.getInstance().getDesktop().getAllFrames()) };
	}
	
	@Override
	public void check() throws PreconditionException {
		int cnt = MainFrame.getInstance().getDesktop().getAllFrames().length;
		if (cnt < 1)
			throw new PreconditionException("No editor window open.");
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int idx = 0;
		this.desiredWindow = (JInternalFrame) ((ObjectListParameter) params[idx++]).getValue();
	}
	
	@Override
	public String getCategory() {
		return "menu.window";
	}
	
	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(Category.UI, Category.SELECTION));
	}
	
	public void execute() {
		try {
			desiredWindow.setIcon(false);
			desiredWindow.setMaximum(true);
			desiredWindow.setSelected(true);
		} catch (PropertyVetoException e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
}
