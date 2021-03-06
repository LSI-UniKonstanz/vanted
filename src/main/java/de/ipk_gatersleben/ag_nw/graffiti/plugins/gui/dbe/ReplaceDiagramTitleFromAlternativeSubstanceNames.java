/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 03.02.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JOptionPane;

import org.AttributeHelper;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.algorithm.PreconditionException;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.process_alternative_ids.ReplaceLabelFromAlternativeSubstanceNames;

public class ReplaceDiagramTitleFromAlternativeSubstanceNames extends AbstractAlgorithm {
	
	public String getName() {
		return "Set Diagram Title";
	}
	
	@Override
	public String getCategory() {
		return "Mapping.Alternative Identifiers";
	}
	
	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(Category.GRAPH, Category.CHART, Category.VISUAL));
	}
	
	@Override
	public void check() throws PreconditionException {
		PreconditionException errors = new PreconditionException();
		
		if (graph == null) {
			errors.add("No graph available!");
		}
		
		if (!errors.isEmpty()) {
			throw errors;
		}
	}
	
	public void execute() {
		int maxID = 0;
		HashMap<Integer, String> exampleValues = new HashMap<Integer, String>();
		maxID = ReplaceLabelFromAlternativeSubstanceNames.enumerateExistingAlternativeSubstanceIDsAndTheirExamples(
				getSelectedOrAllGraphElements(), maxID, exampleValues);
		if (maxID < 0) {
			MainFrame.showMessageDialog("No alternative identifiers available!", "No data available");
		} else {
			ArrayList<String> selvals = new ArrayList<String>();
			String all = "Show All (comma-delimited)";
			selvals.add(all);
			for (int i = 0; i <= maxID; i++) {
				String s = "" + i;
				String example = exampleValues.get(Integer.valueOf(i));
				if (example != null)
					s += " (e.g. " + example + ")";
				selvals.add(s);
			}
			Object result = JOptionPane.showInputDialog(MainFrame.getInstance(),
					"<html>Select the alternative identifier index to set the diagram title<br>"
							+ "for the data charts (0 is the default value from the input form).<br>"
							+ "You may also select &quot;Show All&quot; to display all identifiers.",
					"Select Identifier", JOptionPane.QUESTION_MESSAGE, null, selvals.toArray(), null);
			if (result == null)
				return; // Cancel
				
			@SuppressWarnings("unused")
			int workCnt = 0;
			String number = result.equals(all) ? "-1" : (String) result;
			if (number.indexOf(' ') != -1)
				number = number.substring(0, number.indexOf(' ')).trim();
			
			int idx = Integer.parseInt(number);
			graph.getListenerManager().transactionStarted(this);
			for (Node n : getSelectedOrAllNodes()) {
				NodeHelper nh = new NodeHelper(n);
				String newName = nh.getLabel();
				int mappingId = 0;
				for (SubstanceInterface md : nh.getMappings()) {
					String oMain = md.getName();
					if (idx < 0) {
						ArrayList<String> allNames = new ArrayList<String>();
						for (int i = 0; i < maxID; i++) {
							String otherName = md.getSynonyme(i);
							if (oMain != null && otherName != null) {
								workCnt++;
								allNames.add(otherName);
							}
						}
						if (oMain != null && allNames.size() > 0) {
							newName = "";
							for (int i = 0; i < allNames.size(); i++) {
								if (idx == -1)
									newName = newName + allNames.get(i) + ", ";
								// else
								// newName = newName + allNames.get(i) + "\n";// + "<br>";
							}
							if (newName.endsWith("\n"))
								newName = newName.substring(0, newName.length() - "\n".length());
							if (newName.endsWith(", "))
								newName = newName.substring(0, newName.length() - ", ".length());
						}
						// if (idx == -1 && !newName.startsWith("<html>"))
						// newName = "<html>" + newName;
					} else {
						String oAlternative = md.getSynonyme(idx);
						if (oMain != null && oAlternative != null) {
							workCnt++;
							md.setName(oAlternative);
							newName = oAlternative;
						}
					}
					AttributeHelper.setAttribute(n, "charting", "chartTitle" + (++mappingId), newName);
				}
			}
			graph.getListenerManager().transactionFinished(this, true);
			GraphHelper.issueCompleteRedrawForGraph(graph);
		}
	}
}