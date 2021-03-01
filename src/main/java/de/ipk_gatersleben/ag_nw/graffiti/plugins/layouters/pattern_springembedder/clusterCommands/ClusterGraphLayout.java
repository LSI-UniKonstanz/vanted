/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 27.08.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.Timer;

import org.AttributeHelper;
import org.FolderPanel;
import org.Release;
import org.ReleaseInfo;
import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.AlgorithmWithComponentDescription;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.view.View;
import org.graffiti.session.EditorSession;

import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.services.RunAlgorithmDialog;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

/**
 * @author Christian Klukas (c) 2006 IPK-Gatersleben
 * @vanted.revision <html>2.7.0 Layout G from G<sub>O</sub> too.</html>
 */
public class ClusterGraphLayout extends AbstractAlgorithm implements AlgorithmWithComponentDescription {
	
	public String getName() {
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			return "Apply Pathway-Overview Layout";
		else
			return "Layout Source Graph via Overview Graph";// Re-Layout based on Cluster-Graph Layout";
	}
	
	@Override
	public String getDescription() {
		String cluster = "overview";
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			cluster = "pathway-overview";
		String cluster2 = "cluster";
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			cluster2 = "pathway";
		return "<html>" + "Using this command<br>" + "an " + cluster + " graph is<br>" + "created (2) from<br>"
				+ "the source graph (1), " + "<br>which needs to<br>" + "contain nodes with<br>" + "different "
				+ cluster2 + " IDs.<br>" + "<br>The " + cluster + " graph<br>" + "may be automatically<br>"
				+ "layouted, or manually.<br>" + "For that you need to<br>" + "select the<br>"
				+ "&quot;Null-Layout&quot;.<br>" + "<br>After layouting the<br>" + cluster + " graph (3),<br>"
				+ "apply the layout of<br>" + "the " + cluster + " graph to<br>" + "the source graph (4),<br>"
				+ "by choosing <b>OK</b><br>" + "from the progress<br>" + "panel in the<br>" + "lower-right of the<br>"
				+ "application window.<br>";
	}
	
	public JComponent getDescriptionComponent() {
		ClassLoader cl = this.getClass().getClassLoader();
		String path = this.getClass().getPackage().getName().replace('.', '/');
		ImageIcon icon = new ImageIcon(cl.getResource(path + "/images/clusterrelayout.png"));
		return FolderPanel.getBorderedComponent(new JLabel(icon), 5, 5, 5, 5);
	}
	
	@Override
	public boolean isLayoutAlgorithm() {
		return false; // recursive run should be avoided, thus it is not labeled as a layout algorithm
	}
	
	@Override
	public void reset() {
	}
	
	@Override
	public void setParameters(Parameter[] params) {
	}
	
	@Override
	public String getCategory() {
		return "Cluster";
	}
	
	@Override
	public String getMenuCategory() {
		return "Network.Cluster.Process Cluster Overview-Graph";
	}
	
	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(Category.CLUSTER, Category.GRAPH));
	}
	
	@Override
	public void check() throws PreconditionException {
		super.check();
		if (graph == null)
			throw new PreconditionException("No graph available!");
		Set<String> clusters = new TreeSet<String>();
		for (Node n : graph.getNodes()) {
			String clusterId = NodeTools.getClusterID(n, "");
			if (!clusterId.equals(""))
				clusters.add(clusterId);
		}
		String cluster = "overview";
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			cluster = "cluster/pathway";
		if (clusters.size() <= 0)
			throw new PreconditionException("No " + cluster + " information available for this graph!");
	}
	
	public void execute() {
		String cluster = "overview-graph";
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			cluster = "Pathway-Overview Graph";
		
		// If the selected graph is the overview graph, switch to its cluster graph
		try {
			AttributeHelper.getAttribute(graph, "cluster" + AttributeHelper.attributeSeparator + "clustergraph");
		} catch (AttributeNotFoundException anfe) {
			MainFrame.getInstance();
			for (EditorSession es : MainFrame.getEditorSessions()) {
				for (View v : es.getViews()) {
					if (v != null) {
						try {
							AttributeHelper
									.getAttribute(v.getGraph(),
											"cluster" + AttributeHelper.attributeSeparator + "clustergraph")
									.getName().contains(graph.getName());
							graph = v.getGraph();
							break;
						} catch (AttributeNotFoundException anfe2) {
							continue;
						}
						
					}
				}
			}
		}
		
		RunAlgorithmDialog rad = new RunAlgorithmDialog("Select " + cluster + " Layout", graph, selection, true, false);
		rad.setAlwaysOnTop(true);
		rad.setVisible(true);
		rad.requestFocusInWindow();
		ActionListenerForClusterGraphBasedLayout al = new ActionListenerForClusterGraphBasedLayout();
		final Timer t = new Timer(100, al);
		al.setAlgorithmDialog(rad);
		al.setOptions(getName(), t, graph);
		
		t.setRepeats(true);
		t.start();
	}
	
}

class ActionListenerForClusterGraphBasedLayout implements ActionListener {
	RunAlgorithmDialog rad = null;
	
	Timer tref = null;
	
	String name;
	
	private Graph graph;
	
	public void actionPerformed(ActionEvent e) {
		if (!rad.isVisible() && rad.getAlgorithm() != null) {
			tref.stop();
			MyClusterGraphBasedReLayoutService mcs = new MyClusterGraphBasedReLayoutService(true, graph);
			mcs.setAlgorithm(rad.getAlgorithm(), null);
			BackgroundTaskHelper bth = new BackgroundTaskHelper(mcs, mcs, name, name, true, false);
			bth.startWork(this);
		} else
			rad.setAlwaysOnTop(true);
	}
	
	public void setOptions(String name, Timer t, Graph graph) {
		this.name = name;
		this.graph = graph;
		tref = t;
	}
	
	public void setAlgorithmDialog(RunAlgorithmDialog rad) {
		this.rad = rad;
	}
}
