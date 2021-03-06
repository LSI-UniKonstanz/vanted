/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.parameter.IntegerParameter;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;

public class AddRandomClusterInformationAlgorithm extends AbstractAlgorithm {
	
	int maxClusterID = 10;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		return "Apply random Cluster-ID";
	}
	
	@Override
	public String getCategory() {
		return null;
		// return "Analysis";
	}
	
	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(Category.GRAPH, Category.ANNOTATION, Category.CLUSTER));
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	public void execute() {
		graph.getListenerManager().transactionStarted(this);
		for (Iterator<?> it = selection.getNodes().iterator(); it.hasNext();) {
			Node n = (Node) it.next();
			int clusterId = 1 + (int) (Math.random() * maxClusterID);
			NodeTools.setClusterID(n, Integer.valueOf(clusterId).toString());
		}
		graph.getListenerManager().transactionFinished(this);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graffiti.plugin.algorithm.Algorithm#getParameters()
	 */
	@Override
	public Parameter[] getParameters() {
		IntegerParameter maxId = new IntegerParameter(maxClusterID, "Cluster-Count", "Number of Clusters");
		return new Parameter[] { maxId };
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graffiti.plugin.algorithm.Algorithm#reset()
	 */
	@Override
	public void reset() {
		
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graffiti.plugin.algorithm.Algorithm#setParameters(org.graffiti.plugin.
	 * parameter.Parameter[])
	 */
	@Override
	public void setParameters(Parameter[] params) {
		IntegerParameter maxId = (IntegerParameter) params[0];
		maxClusterID = maxId.getInteger().intValue();
	}
}