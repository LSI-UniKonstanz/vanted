/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 17.01.2005 by Christian Klukas
 * (c) 2005 IPK Gatersleben, Group Network Analysis
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.database_processing.hierarchies;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.AttributeHelper;
import org.Vector2d;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.IntegerParameter;
import org.graffiti.plugin.parameter.ObjectListParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.selection.Selection;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg.BriteEntry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg.KeggBriteService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.sib_enzymes.EnzymeService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.KeggGmlHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.graph_to_origin_mover.CenterLayouterAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.grid.GridLayouterAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.rt_tree.RTTreeLayout;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * This class replaces the CreateFuncatGraphAlgorithm, 
 * 
 * The first class created KEGG hierarchies based on direct given hierarchies (using alternative labels in the nodes),
 * or 
 * 
 * 
 * 
 * @author Matthias Klapperstueck
 */
public class CreateKEGGOrthologyGraphAlgorithm extends AbstractAlgorithm {
	
	private final String settingCreateKO = "KEGG Orthology";
	private final String settingCreateGO = "Gene Ontology";
	
	private final String settingOntologyIdentifiersGiven = "Create Ontology from given Ontology Identifiers";
	private final String settingEnzymeIdGiven = "Create Ontology from given Enzyme ID";
	
	private final String settingSearchMainLabel = "Search ID in Node Label";
	private final String settingSearchAlternativeLabel = "Search ID in Alternative Labels";
	
	
	/*
	 * the following fields are represent the current settings of the parameters
	 */
	private String selectedOntology;
	
	private String selectedIdentifierType;
	
	private Boolean selectedSearchMainLabel;
	private Boolean selectedSearchAlternativeLabel;
	
	int startX = 100;
	int stepX = 200;
	int startY = 40;
	int stepY = 0;

	
	
	public CreateKEGGOrthologyGraphAlgorithm() {
		/*
		 * set standard values for the selectedParameter fields, in case the algorithms is called without setting parameters
		 */
		selectedOntology = settingCreateKO;
		selectedIdentifierType = settingOntologyIdentifiersGiven;
		selectedSearchMainLabel = true;
		selectedSearchAlternativeLabel =  true; 
		
		startX = 100;
		stepX = 200;
		startY = 40;
		stepY = 0;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		return "Create KEGG Orthology Tree";
	}
	
	@Override
	public String getDescription() {
		return "<html>"
				+ "This command creates a hierarchy-tree. The hierarchy information may be given by the node labels,<br>"
				+ "by alternative substance identifiers (for nodes with mapped data) or by a data annotation, provided<br>"
				+ "directly in the input template (type 2)."
				+ "<br>"
				+ "The hierarchy information may be evaluated directly, in case it is given by a '.' or ';' divided text<br>"
				+ "annotation. It is also possible to look-up a KEGG Pathway hierarchy, in case the selected identifier<br>"
				+ "is recognized as a enzyme name or ID and it is found in the KO database. Another possibility is to<br>"
				+ "lookup and interpret the given identifers as gene names, listed in the KO database. In this case the<br>"
				+ "gene data is also put in context to the KEGG Pathway hierarchy.<br>";
	}
	
	@Override
	public String getCategory() {
		return "Network.Hierarchy";
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Parameter[] getParameters() {
		/* 
		 * create context sensitive menu
		 * Check, if network has alternative identifiers and only show the option to search alternative labels, if there are some
		 */
		boolean hasAlternativeLabels = false;
		for(Node node : getSelectedOrAllNodes() ) {
			if(AttributeHelper.getLabels(node).size() > 1) { // At least one alternative label additional to the main label
				hasAlternativeLabels = true;
				break;
			}
		}
		
		
		
		Collection<String> possibleOntologies = new ArrayList<String>();
		possibleOntologies.add(settingCreateKO);
//		possibleOntologies.add(settingCreateGO);
		ObjectListParameter objectListParameterPossibleOntologies = new ObjectListParameter(settingCreateKO, "Choose Ontology",
				"Select one of the supported Ontologies", possibleOntologies);	
		
		Collection<String> possibleEvaluations = new ArrayList<String>();
		possibleEvaluations.add(settingOntologyIdentifiersGiven);
		possibleEvaluations.add(settingEnzymeIdGiven);
		ObjectListParameter objectListParameterPossibleEvaluations = new ObjectListParameter(settingCreateKO, "Choose Id Type",
				"Select type of ID, which is either the direct Ontology Identifier or the Enzyme ID", possibleEvaluations);			
		
		
		BooleanParameter boolParameterSearchLabel = new BooleanParameter(true, settingSearchMainLabel, "Search the node label for the ID");
		
		int numParams = 3;
		
		
		BooleanParameter boolParameterSearchAlternativeLabel = null;
		if(hasAlternativeLabels) {
			boolParameterSearchAlternativeLabel = new BooleanParameter(true, settingSearchAlternativeLabel, "Search the alternative node labels for the ID");
			
			numParams++;
		}
		
		Parameter[] parameters = new Parameter[numParams];
		int i = 0;
		parameters[i++] = objectListParameterPossibleOntologies;
		parameters[i++] = objectListParameterPossibleEvaluations;
		parameters[i++] = boolParameterSearchLabel;
		if(hasAlternativeLabels) 
			parameters[i++] = boolParameterSearchAlternativeLabel;
		
		return parameters;
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		selectedOntology = (String) ((ObjectListParameter) params[i++]).getValue();
		selectedIdentifierType = (String) ((ObjectListParameter) params[i++]).getValue();
		selectedSearchMainLabel = (Boolean) ((BooleanParameter) params[i++]).getValue();
		if(params.length == 4)
			selectedSearchAlternativeLabel = (Boolean) ((BooleanParameter) params[i++]).getValue();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	public void execute() {
		final Collection<Node> workNodes = new ArrayList<Node>(getSelectedOrAllNodes());
		final HashSet<Node> newNodes = new HashSet<Node>();
		final HashSet<Edge> newEdges = new HashSet<Edge>();
		
		final Graph fgraph = graph;
		
		final BackgroundTaskStatusProviderSupportingExternalCallImpl status = new BackgroundTaskStatusProviderSupportingExternalCallImpl(
				"Create Hierarchy", "");
		Runnable task = new Runnable() {
			public void run() {
				
				String curLabelToCheck;
				
				fgraph.getListenerManager().transactionStarted(this);
				try {
					HashMap<String, Node> hierarchy_tree_itemName2node = new HashMap<String, Node>();
					int nn = 0;
					int nn_max = workNodes.size();
					for (Node graphNode : workNodes) {
						nn++;
						status.setCurrentStatusText1("Created " + newNodes.size() + " nodes and " + newEdges.size()
								+ " edges ");
						status.setCurrentStatusText2("Process node " + nn + " / " + nn_max);
						if (status.wantsToStop())
							break;
						status.setCurrentStatusValueFine((double) nn / (double) nn_max * 100d);
						
						
						ArrayList<String> labels = AttributeHelper.getLabels(graphNode);
						
						// skip node, if it has no label
						if(labels == null || labels.isEmpty())
							continue;
						
						/*
						 * KEGG orthology selected
						 */
						if (selectedOntology.equals(settingCreateKO)) {
							HashSet<BriteEntry> briteEntries = new HashSet<>();
							// direct KO identifiers given
							if(selectedIdentifierType.equals(settingOntologyIdentifiersGiven)) {
								
								if (selectedSearchMainLabel) {
									curLabelToCheck = KeggBriteService.extractKeggKOId(labels.get(0));
									if(curLabelToCheck != null)
										briteEntries.addAll(KeggBriteService.getInstance().getBriteHierarchy("ko00001").getEntriesMap().get(curLabelToCheck));
									
									
								}
								if (selectedSearchAlternativeLabel){
									for(int i = 1; i < labels.size(); i++) {
										curLabelToCheck = KeggBriteService.extractKeggKOId(labels.get(i));
										if(curLabelToCheck != null)
											briteEntries.addAll(KeggBriteService.getInstance().getBriteHierarchy("ko00001").getEntriesMap().get(curLabelToCheck));
									}
									
									
								}
							}
							
							//EC numbers given
							if(selectedIdentifierType.equals(settingEnzymeIdGiven)) {
								
								if (selectedSearchMainLabel) {
									curLabelToCheck = EnzymeService.extractECId(labels.get(0));
									if(curLabelToCheck != null) {
										HashSet<BriteEntry> hashSet = KeggBriteService.getInstance().getBriteHierarchy("ko00001").getECEntriesMap().get(curLabelToCheck);
										if(hashSet != null)
											briteEntries.addAll(hashSet);
//										else
//											System.err.println();
									}
									
								}
								if (selectedSearchAlternativeLabel){
									for(int i = 1; i < labels.size(); i++) {
										curLabelToCheck = EnzymeService.extractECId(labels.get(i));
										if(curLabelToCheck != null) {
										HashSet<BriteEntry> hashSet = KeggBriteService.getInstance().getBriteHierarchy("ko00001").getECEntriesMap().get(curLabelToCheck);
										if(hashSet != null)
											briteEntries.addAll(hashSet);
//										else
//											System.err.println();
										}
									}
									
								}
							}	
							
							// create / extend hierarchy graph for each returned brite entry
							createBriteHierarchyGraphNodes(
										fgraph,
										graphNode,
										briteEntries,
										hierarchy_tree_itemName2node,
										newNodes,
										newEdges
										);

						}
						
						/*
						 * Gene Ontology selected
						 */
						if (selectedOntology.equals(settingCreateGO)) {
							
						}		
						
						
					
					}
					
				} catch(IOException e) {
					System.err.println(e);
					
				}
				finally {
					if (!status.wantsToStop()) {
						fgraph.getListenerManager().transactionFinished(this, false);
						status.setCurrentStatusText1("Created " + newNodes.size() + " nodes and " + newEdges.size()
								+ " edges ");
						status.setCurrentStatusText2("Update view. Please wait.");
						MainFrame.showMessage("Added " + newNodes.size() + " nodes and " + newEdges.size()
								+ " edges to the network", MessageType.INFO);
						GraphHelper.postUndoableNodeAndEdgeAdditions(fgraph, newNodes, newEdges, getName());
						if (newNodes.size() > 0) {
							// layout new nodes using tree layout
							RTTreeLayout tree = new RTTreeLayout();
							Parameter[] parameters2 = tree.getParameters();
							for(Parameter curParam : parameters2) {
								if(curParam.getName().equals("Tree Direction (0,90,180,270)")) {
									((ObjectListParameter)curParam).setValue(0);
								}
							}
							tree.setParameters(parameters2);
							
							tree.attach(fgraph, new Selection(newNodes));
							tree.execute();
							
							// layout gene nodes using grid layout (no resize)
							Collection<Node> geneNodes = new ArrayList<Node>(workNodes);
							geneNodes.removeAll(newNodes);
							
							double minX = Double.MAX_VALUE;
							double maxX = Double.MIN_VALUE;
							double maxY = Double.MIN_VALUE;
							for(Node curNode : newNodes) {
								Point2D position = AttributeHelper.getPosition(curNode);
								Vector2d size = AttributeHelper.getSize(curNode);
								if(position.getX() < minX)
									minX = position.getX();
								if(position.getX() > maxX)
									maxX = position.getX();
								if(size.y + position.getY() > maxY)
									maxY = size.y + position.getY();
							}
							GridLayouterAlgorithm.layoutOnGrid(geneNodes, 1, 20, 20, 30, new Point((int)minX, (int)maxY + 50));
//							GraphHelper.moveGraph(fgraph, offX, offY);
							CenterLayouterAlgorithm.moveGraph(fgraph, getName(), true, 50, 50);
						}
					} else {
						for (Edge e : newEdges)
							fgraph.deleteEdge(e);
						for (Node n : newNodes)
							fgraph.deleteNode(n);
						status.setCurrentStatusText1("No elements will be added.");
						status.setCurrentStatusText2("Update view. Please wait.");
						fgraph.getListenerManager().transactionFinished(this);
					}
				}
			}
		};
		BackgroundTaskHelper.issueSimpleTask("Create Hierarchy", "Initialize...", task, null, status);
	}
	
	
	private void createBriteHierarchyGraphNodes(
			Graph graph, 
			Node graphNode, 
			HashSet<BriteEntry> hierarchyInformationsForCurrentNode, 
			HashMap<String, Node> hierarchy_tree_itemName2node,
			HashSet<Node> newNodes,
			HashSet<Edge> newEdges) {

		
		Node lastNode = null;
		lastNode = graphNode;
		for (BriteEntry hierarchyInformation : hierarchyInformationsForCurrentNode) {
			
			// skip actual enzyme and get right to the path elements
			hierarchyInformation = hierarchyInformation.getParent();
			
			do {
				
				String hierarchyEntityName = hierarchyInformation.getName();
				if(hierarchyEntityName.contains("KEGG Orthology")){
//					System.err.println();
				}
//						for (String hierarchyEntityNameLE : graphH) {
				if (!hierarchy_tree_itemName2node.containsKey(hierarchyEntityName)) {
					// System.out.println("not found: "+hierarchyEntityName);
					Node n = GraphHelper.addNodeToGraph(graph, startX
							+ hierarchy_tree_itemName2node.size() * stepX, startY
							+ hierarchy_tree_itemName2node.size() * stepY, 1, 150, 30, Color.BLACK,
							Color.WHITE);
					AttributeHelper.setLabel(n, hierarchyEntityName);
					
					if (hierarchyInformation.getPathId() != null) {
						// this should be a map-node
						String id = hierarchyInformation.getPathId();
//								id = hierarchyEntityNameLE.substring(hierarchyEntityNameLE.toLowerCase()
//													.indexOf("path:"), hierarchyEntityNameLE.indexOf("]", hierarchyEntityNameLE
//													.toLowerCase().indexOf("path:")));
							KeggGmlHelper.setKeggId(n, id.toLowerCase());
						KeggGmlHelper.setKeggType(n, "map");
						AttributeHelper.setRoundedEdges(n, 15);
					} else if(hierarchyInformation.getId() != null)
						KeggGmlHelper.setKeggId(n, hierarchyInformation.getId().toLowerCase());
					
					hierarchy_tree_itemName2node.put(hierarchyEntityName, n);
					
					newNodes.add(n);
				}
				Node hierarchy_tree_node = hierarchy_tree_itemName2node.get(hierarchyEntityName);
				if (lastNode != null) {
					String lastnodename = AttributeHelper.getLabel(lastNode, "");
					// connect nodes
					if (!lastNode.getNeighbors().contains(hierarchy_tree_node)) {
						Edge edge = graph.addEdge(hierarchy_tree_node, lastNode, true, AttributeHelper
								.getDefaultGraphicsAttributeForEdge(Color.BLACK, Color.BLACK, true));
						newEdges.add(edge);
					}
				}
				
				lastNode = hierarchy_tree_node;
				
			} while((hierarchyInformation = hierarchyInformation.getParent()) != null);
			lastNode = graphNode;
		}
	}
}
