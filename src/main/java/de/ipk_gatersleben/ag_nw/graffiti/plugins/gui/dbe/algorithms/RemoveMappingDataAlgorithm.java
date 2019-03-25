/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 17.01.2005 by Christian Klukas
 * (c) 2005 IPK Gatersleben, Group Network Analysis
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.algorithms;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.AttributeHelper;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.algorithm.PreconditionException;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.GraphElementHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * @author Christian Klukas (c) 2005 IPK Gatersleben, Group Network Analysis
 */
public class RemoveMappingDataAlgorithm extends AbstractAlgorithm {

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
		BackgroundTaskHelper.issueSimpleTask(getName(), "Initialize...", new Runnable() {
			public void run() {
				status.setCurrentStatusValue(-1);
				status.setCurrentStatusText1("Removing mapped data...");
				status.setCurrentStatusText2("");
				int workload = workNodes.size();
				int progress = 0;
				Graph g = workNodes.iterator().next().getGraph();
				g.getListenerManager().transactionStarted(this);
				try {
					for (GraphElement ge : workNodes) {
						if (status.wantsToStop())
							break;
						progress++;
						status.setCurrentStatusText2("Processing element " + progress + "/" + workload);
						status.setCurrentStatusValueFine(100d * ((double) progress) / (double) workload);
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
					g.getListenerManager().transactionFinished(this, false, status);
					status.setCurrentStatusValue(100);
					// GraphHelper.issueCompleteRedrawForGraph(g);
				}
				if (status.wantsToStop())
					status.setCurrentStatusText1("Processing aborted");
				else
					status.setCurrentStatusText1("Finished");
				status.setCurrentStatusText2("");
			}
		}, null, status);
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

	public boolean mayWorkOnMultipleGraphs() {
		return true;
	}
}
