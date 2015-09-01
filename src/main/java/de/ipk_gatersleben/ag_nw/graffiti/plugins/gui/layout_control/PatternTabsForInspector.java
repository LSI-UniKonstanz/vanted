/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

/*
 * $Id$
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control;

import java.util.ArrayList;

import org.FeatureSet;
import org.ReleaseInfo;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.inspector.InspectorTab;

import de.ipk_gatersleben.ag_nw.graffiti.DBE_EditorPluginAdapter;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.data_mapping.DataMapping;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentDataProcessingManager;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.PutIntoSidePanel;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.TabDBE;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg.TabKegg;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.metacrop.RimasTab;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.metacrop.TabMetaCrop;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.network.TabNetworkAlgorithms;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.statistics.TabStatistics;

/**
 * DOCUMENT ME!
 * 
 * @author Christian Klukas Represents the main class of the
 *         InspectorLayoutControl plugin.
 * @version $Revision$
 */
public class PatternTabsForInspector
					extends DBE_EditorPluginAdapter {
	
	public PatternTabsForInspector() {
		super();
		
		ArrayList<InspectorTab> tablist = new ArrayList<InspectorTab>();
		
		// if (!ReleaseInfo.isRunningAsApplet())
//		tablist.add(new WorkflowHelper());

		ExperimentDataProcessingManager.addExperimentDataProcessor(new PutIntoSidePanel());
			tablist.add(new TabDBE());
			algorithms = new Algorithm[] { new DataMapping() };
		
		// pathway tabs KEGG and MetaCrop
//		Collection<InspectorTab> subtabsPathway = new ArrayList<InspectorTab>();
		tablist.add(new TabKegg());
		
		tablist.add(new TabMetaCrop());
		tablist.add(new RimasTab());
		
//		tablist.add(new TabBiomodels());
		
//		if (subtabsPathway.size() > 0)
//			tablist.add(getSubtab("Pathways", subtabsPathway));
		
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.TAB_LAYOUT))
			tablist.add(new TabPluginControl());
		
//		Collection<InspectorTab> subtabsTools = new ArrayList<InspectorTab>();
		if (!ReleaseInfo.isRunningAsApplet() && ReleaseInfo.getIsAllowedFeature(FeatureSet.STATISTIC_FUNCTIONS))
			tablist.add(new TabStatistics());

//		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.TAB_PATTERNSEARCH))
//			subtabsTools.add(new TabPatternLayout());
		
		tablist.add(new TabPatternLayout());
		
		tablist.add(new TabNetworkAlgorithms());
		
//		if (subtabsTools.size() > 0)
//			tablist.add(getSubtab("Tools", subtabsTools));
//		
		// GravistoService.getInstance().getMainFrame().addSelectionListener(new TabSubstrate());
		
		this.tabs = tablist.toArray(new InspectorTab[] {});
	}
	
	/*
	private InspectorTab getSubtab(String title,
						Collection<InspectorTab> subtabs) {
		if (subtabs.size() == 1)
			return subtabs.iterator().next();
		else
			return new SubtabHostTab(title, subtabs);
	}
	*/
	
}
