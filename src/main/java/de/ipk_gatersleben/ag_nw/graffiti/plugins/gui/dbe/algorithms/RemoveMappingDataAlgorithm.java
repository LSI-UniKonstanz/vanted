/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 17.01.2005 by Christian Klukas
 * (c) 2005 IPK Gatersleben, Group Network Analysis
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.algorithms;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.undo.CannotUndoException;

import org.AttributeHelper;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.algorithm.PreconditionException;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.data_mapping.DataMapping;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.data_mapping.DataMapping.ShowMappingResults;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.GraphElementHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * @author Christian Klukas (c) 2005 IPK Gatersleben, Group Network Analysis
 */
public class RemoveMappingDataAlgorithm extends AbstractAlgorithm {

	private ArrayDeque<Collection<ExperimentInterface>> undoStack = new ArrayDeque<>();

	@Override
	public String getName() {
		return "Remove Mapping Data";
	}

	@Override
	public String getCategory() {
		return "Mapping";
	}

	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(Category.DATA));
	}

	@Override
	public void check() throws PreconditionException {
		super.check();
		if (graph == null)
			throw new PreconditionException("No graph available");
		if (selection.getNodes().size() < 1)
			throw new PreconditionException("Graph is empty");
	}

	@Override
	public void execute() {
		final Collection<GraphElement> workNodes = getSelectedOrAllGraphElements();
		final BackgroundTaskStatusProviderSupportingExternalCall status = new BackgroundTaskStatusProviderSupportingExternalCallImpl(
				"Initialize...", "");
		BackgroundTaskHelper.issueSimpleTask(getName(), "Initialize...", () -> {
			status.setCurrentStatusValue(-1);
			status.setCurrentStatusText1("Removing mapping data...");
			status.setCurrentStatusText2("");
			int workload = workNodes.size();
			int progress = 0;
			graph.getListenerManager().transactionStarted(this);
			try {
				undoStack.addFirst(ExtractMappingDataAlgorithm.getExperiments(workNodes, true, null));
				for (GraphElement ge : workNodes) {
					if (status.wantsToStop())
						break;

					progress++;
					status.setCurrentStatusText2("Processing element " + progress + "/" + workload);
					status.setCurrentStatusValueFine(100d * progress / (double) workload);
					GraphElementHelper geh = new GraphElementHelper(ge);
					if (geh.getDataMappings().size() > 0) {
						geh.getDataMappings().clear();
						if (ge.getGraph() == null)
							continue;
						AttributeHelper.setToolTipText(ge, "");
					}
					removeMappingDataFrom(ge);
				}
				status.setCurrentStatusValue(-1);
			} finally {
				graph.getListenerManager().transactionFinished(this, false, status);
				status.setCurrentStatusValue(100);
				// GraphHelper.issueCompleteRedrawForGraph(g);
			}
			if (status.wantsToStop())
				status.setCurrentStatusText1("Processing aborted");
			else
				status.setCurrentStatusText1("Finished");
			status.setCurrentStatusText2("");
		}, null, status);
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		DataMapping dm = new DataMapping(true);
		dm.attach(recycledGraph, recycledSelection);
		dm.setDoShowResult(ShowMappingResults.DONT_SHOW_RESULTDIALOG);
		undoStack.removeFirst().forEach((e) -> {
			dm.setExperimentData(e);
			dm.execute();
		});
	}

	@Override
	public boolean mayWorkOnMultipleGraphs() {
		return true;
	}

	@Override
	public boolean doesUndo() {
		return true;
	}
	
	public synchronized static void removeMappingDataFrom(GraphElement n) {
		try {
			n.removeAttribute("dbe");
		} catch (AttributeNotFoundException anfe) {
			// empty
		}
		try {
			n.removeAttribute("mapping");
		} catch (AttributeNotFoundException anfe) {
			// empty
		}

		try {
			n.removeAttribute("charting");
		} catch (AttributeNotFoundException anfe) {
			// empty
		}
		try {
			n.removeAttribute("graphics.component");

		} catch (AttributeNotFoundException anfe) {
			// empty
		}
	}
}
