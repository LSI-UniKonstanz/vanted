/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg;


import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.ws.rs.core.MediaType;
import javax.xml.rpc.ServiceException;

import keggapi.KEGGLocator;
import keggapi.KEGGPortType;

import org.AlignmentSetting;
import org.AttributeHelper;
import org.BackgroundTaskStatusProvider;
import org.ErrorMsg;
import org.JMButton;
import org.Release;
import org.ReleaseInfo;
import org.StringManipulationTools;
import org.apache.log4j.Logger;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.inspector.InspectorTab;
import org.graffiti.plugin.view.GraphView;
import org.graffiti.plugin.view.View;
import org.graffiti.selection.SelectionEvent;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;

import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.sib_enzymes.EnzymeEntry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.sib_enzymes.EnzymeService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.IndexAndString;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.KeggGmlHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.web.RestService;

/**
 * @author $author$
 * @version $Revision$
 */
public class TabKegg extends InspectorTab
					implements
					ActionListener, BackgroundTaskStatusProvider {
	private static final long serialVersionUID = 1L;
	
	Logger logger = Logger.getLogger(TabKegg.class);
	private static JCheckBox prettifyLabels = null;
	
	private static final String restURL = "http://rest.kegg.jp/";
	
	Client client;
	
	JButton getOrganismListFromKegg;

	JTree organismTree;
	DefaultMutableTreeNode organismRootNode;
	DefaultTreeModel organismTreeModel;
	JScrollPane organismTreeScroll;
	DefaultMutableTreeNode selectedOrganismNode;
	HashMap<String, String> treeNodeToKeggCode = new HashMap<String,String>();//Name | Buchstabencode
	
	JTree allPathwayTree;
	DefaultMutableTreeNode allPathwayRootNode;
	
	JTree pathwayTree;
	DefaultMutableTreeNode pathwayRootNode;
	DefaultTreeModel pathwayTreeModel;
	JScrollPane pathwayTreeScroll;
	DefaultMutableTreeNode selectedPathwayNode;
	HashMap<String,String> pathwayNameToPathway = new HashMap<String,String>();//Name | Code
		
	JSplitPane splitPane;
	
	Graph graph;
	
	RestService restService;
	
	
	String status1 = "";
	String status2 = "";
	double statusValue = 0; 
	boolean stopTask = false;
		
//	/**
//	 * Constructs a <code>PatternTab</code> and sets the title.
//	 /
	public TabKegg() {
		super();
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			this.title = "Load";
		else
			this.title = "KEGG";
		initComponents();
	}

	
	/**
	 * Aufbau des Kegg-Tabs.
	 */
	private void initComponents() {
		client = Client.create();
		double border = 5;
		double[][] size = { { border, TableLayoutConstants.FILL, border }, // Columns
				{ border,
												TableLayout.PREFERRED,
												TableLayout.PREFERRED,
												TableLayoutConstants.FILL,
												TableLayout.PREFERRED,
												border } }; // Rows
		this.setLayout(new TableLayout(size));
		
		getOrganismListFromKegg = new JMButton("<html>Download Organism List");
		
		getOrganismListFromKegg.addActionListener(this);
		getOrganismListFromKegg.setOpaque(false);
		getOrganismListFromKegg.setToolTipText("Downloads the list of organisms from the KEGG database. (needs internet connection");
		
		
		
		
		organismRootNode = new DefaultMutableTreeNode("Please download the organism list");
		allPathwayRootNode = new DefaultMutableTreeNode("Pathways");
		pathwayRootNode = new DefaultMutableTreeNode("Please download the organism list");

		organismTreeModel = new DefaultTreeModel(organismRootNode);
		organismTree = new JTree(organismTreeModel);
		organismTreeScroll = new JScrollPane(organismTree);		
		selectedOrganismNode = null;
		
		MouseListener organismml = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getClickCount() == 2) {
						downloadPathwayList();
				}
			}
		};
		organismTree.addMouseListener(organismml);
		
		TreeSelectionListener tsl = new TreeSelectionListener(){ 
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)
				e.getPath().getLastPathComponent();
				if (node == null) return;
				if (node.isLeaf()) {
					selectedOrganismNode = node;
				} 
				else
					selectedOrganismNode = null;
		}};
		organismTree.addTreeSelectionListener(tsl);		
		
		pathwayTreeModel = new DefaultTreeModel(pathwayRootNode);
		pathwayTree = new JTree(pathwayTreeModel);
		pathwayTreeScroll = new JScrollPane(pathwayTree);
		selectedPathwayNode = null;

		TreeSelectionListener tsl2 = new TreeSelectionListener(){ 
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)
				e.getPath().getLastPathComponent();
				if (node == null) return;
				if (node.isLeaf()) {
					selectedPathwayNode = node;
				} 
				else
					selectedPathwayNode = null;
		}};
		pathwayTree.addTreeSelectionListener(tsl2);
		
		
		MouseListener pathwayml = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getClickCount() == 2) {
						downloadPathway();
				}
			}
		};
		pathwayTree.addMouseListener(pathwayml);

		organismTreeScroll.setMinimumSize(new Dimension(0,0));
		pathwayTreeScroll.setMinimumSize(new Dimension(0,0));
		
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                organismTreeScroll, pathwayTreeScroll);
		
		splitPane.setOneTouchExpandable(true);
		splitPane.setMinimumSize(new Dimension(5,5));

		
		prettifyLabels = new JCheckBox("replace cpd ID with names on startup", false);
		prettifyLabels.setOpaque(false);
		prettifyLabels.setToolTipText("If selected, upon loading numeric IDs are replaced by database entry names.");
		
		JButton replaceLabels = new JMButton("<< Modify Labels");
		replaceLabels.setOpaque(false);
		replaceLabels.setToolTipText("Click to modify active graph node labels");
		replaceLabels.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				replaceLabelDialog();
//				GravistoService.getInstance().runAlgorithm(new InterpreteLabelNamesAlgorithm(), e);
			}
		});
		
		final JTextField searchBox = new JTextField("");
		searchBox.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						String searchText = searchBox.getText();
						if(searchText.length()>0)
						{
							breadthFirstSearch(pathwayRootNode, searchText, pathwayTree);
						// Organisms
							breadthFirstSearch(organismRootNode, searchText, organismTree);
						// Pathways
						}
						
					}
					private boolean breadthFirstSearch(DefaultMutableTreeNode node, String searchText, JTree tree)
					{
						for (int i = 0; i < node.getChildCount(); i++) {
							DefaultMutableTreeNode innerNode = (DefaultMutableTreeNode)node.getChildAt(i);//(DefaultMutableTreeNode) keggTree.getModel().getChild(myRootNode, i);
							if((((String)innerNode.getUserObject()).toLowerCase()).indexOf(searchText.toLowerCase())!=-1 && innerNode.isLeaf())
							{
								@SuppressWarnings("unchecked")
								Enumeration<DefaultMutableTreeNode> children = ((DefaultMutableTreeNode)tree.getModel().getRoot()).children();
								while(children.hasMoreElements())
								{
									tree.collapsePath(new TreePath(children.nextElement().getPath()));									
								}
								TreePath path = new TreePath(innerNode.getPath());
								tree.expandPath(path);
								tree.setSelectionPath(path);
								tree.scrollPathToVisible(path);
								tree.updateUI();
								return true;
							}
							if(!innerNode.isLeaf())
								if(breadthFirstSearch(innerNode, searchText, tree))
									return true;
						}
						
						return false;
					}
				});
			}
			
			public void keyPressed(KeyEvent e) {
			}
			
			public void keyReleased(KeyEvent e) {
			}
		});
						
		JComponent searchPathway = TableLayout.getSplit(new JLabel("Search"), searchBox, TableLayout.PREFERRED, TableLayout.FILL);
		this.add(getOrganismListFromKegg, "1,1");


		this.add(splitPane, "1,3");
        splitPane.setDividerLocation(1.0d);
		this.add(TableLayout.get3SplitVertical(searchPathway, null, TableLayout.getSplit(replaceLabels, prettifyLabels, TableLayout.PREFERRED, TableLayout.PREFERRED), TableLayout.PREFERRED, 3, TableLayout.PREFERRED),
							"1,4");
		
		this.validate();
	}
	
	
	protected void downloadOrganismList() {
		BackgroundTaskHelper.issueSimpleTask(
				"Retrieve Organism-List",
				"Please wait (REST call is issued)...",
				new Runnable() {
					public void run() {
						ArrayList<String> path = new ArrayList<String>();
						path.add("list/");
						path.add("organism");
						String response = (String)RestService.makeRequest(restURL+"list/organism", MediaType.TEXT_PLAIN, String.class);
						if (response!=null) {
							
							splitPane.setDividerLocation(1.0d);
							
							organismRootNode.removeAllChildren();
							organismRootNode.setUserObject("Organisms");
							
							pathwayRootNode.removeAllChildren();
							pathwayRootNode.setUserObject("Select an organism");
							
							DefaultMutableTreeNode reference = new DefaultMutableTreeNode("Reference pathways"); 
							organismRootNode.add(reference);
							reference.add(new DefaultMutableTreeNode("KO"));
							reference.add(new DefaultMutableTreeNode("Enzyme"));
//							reference.add(new DefaultMutableTreeNode("Reaction"));
							treeNodeToKeggCode.put("KO", "ko");
							treeNodeToKeggCode.put("Enzyme", "ec");
//							treeNodeToKeggCode.put("Reaction", "rn");
							
							
							boolean breakpoint = false;
							while(true)
							{
								if(response.length()==0 || response.indexOf("\t")==-1)
									break;
								String[] tmp = new String[3];
								response = response.substring(response.indexOf("\t")+1);
								tmp[0] = response.substring(0, response.indexOf("\t"));
								response = response.substring(response.indexOf("\t")+1);
								tmp[1] = response.substring(0, response.indexOf("\t"));
								response = response.substring(response.indexOf("\t")+1);
								
								treeNodeToKeggCode.put(tmp[1], tmp[0]);
								
								if(response.indexOf("\n")==-1)
								{
									tmp[2] = response;
									breakpoint = true;
								}
								else
								{
									tmp[2] = response.substring(0, response.indexOf("\n"));
									response = response.substring(response.indexOf("\n")+1);
								}
								
								Boolean weiter = true;
								DefaultMutableTreeNode currNode = organismRootNode;
								String tmp2 = tmp[2];
								while(weiter)
								{ 
									String temp;
									if(tmp2.indexOf(";")==-1)
									{
										temp = tmp2;
										weiter = false;
									}
									else
									{
										temp = tmp2.substring(0, tmp2.indexOf(";"));
										tmp2 = tmp2.substring(tmp2.indexOf(";")+1);
									}
									Boolean found = false;
									for(int i = 0; i<currNode.getChildCount();i++)
									{
										DefaultMutableTreeNode n = (DefaultMutableTreeNode)currNode.getChildAt(i);
										if(((String)n.getUserObject()).equals(temp))
										{
											found = true;
											currNode = n;
											
											break;
										}
									}
									if(!found)
									{
										DefaultMutableTreeNode tmpNode = new DefaultMutableTreeNode(temp);
										currNode.add(tmpNode);
										currNode = tmpNode;
									}
								}
								currNode.add(new DefaultMutableTreeNode(tmp[1]));
								if(breakpoint)
									break;
							}
						}


						response = (String)RestService.makeRequest(restURL+"get/br:br08901",MediaType.TEXT_PLAIN, String.class);//clientResponse.getEntity(String.class);
						if(response != null){
							allPathwayRootNode.removeAllChildren();
							allPathwayRootNode.setUserObject("Pathways");
							DefaultMutableTreeNode nodeA = null, nodeB = null;
	
							while(true)
							{
								response = response.substring(response.indexOf("\n")+1);
								if(response.startsWith("A"))
									break;
							}
							
							while(true)
							{										
								if(response.startsWith("!") || response.length()==0 || response.indexOf("\n")==-1)
									break;
								String nextLine = response.substring(0, response.indexOf("\n"));
								response = response.substring(response.indexOf("\n")+1);
								if(nextLine.startsWith("A"))
								{
									nextLine = nextLine.substring(1);
									nextLine.trim();
									nodeA = new DefaultMutableTreeNode(nextLine);
									allPathwayRootNode.add(nodeA);
									
								}
								else if(nextLine.startsWith("B"))
								{
									nextLine = nextLine.substring(1);
									nextLine = nextLine.trim();
									nodeB = new DefaultMutableTreeNode(nextLine);
									nodeA.add(nodeB);
								}
								else if(nextLine.startsWith("C"))
								{
									nextLine = nextLine.substring(1);
									nextLine = nextLine.trim();
									nextLine = nextLine.substring(5);
									nextLine = nextLine.trim();
									nodeB.add(new DefaultMutableTreeNode(nextLine));
								}
							}
						}
					}
				},
				new Runnable() {
					public void run() {
						organismTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
						organismTreeModel.reload();
						organismTree.setRootVisible(false);
						pathwayTreeModel.reload();
				}
			});		
	}
	
	/**
	 * Wird aufgerufen, wenn ein Organismus aus der Liste aufgerufen wird.
	 * L�dt die entsprechnde Pathway-Liste zum Organismus herunter.
	 */
	protected void downloadPathwayList() {
		if(selectedOrganismNode!=null)
		{
			BackgroundTaskHelper.issueSimpleTask(
					"Retrieve Pathway-List",
					"Please wait (REST call is issued)...",
					new Runnable() {
						public void run() {
							WebResource webResource = client.resource(restURL);
							WebResource webResourceListOrganisms = webResource.path("list/").path("pathway/").path(treeNodeToKeggCode.get(selectedOrganismNode.getUserObject()));
							Builder builder = webResourceListOrganisms.accept(MediaType.TEXT_PLAIN);
							ClientResponse clientResponse = null;
							try {
								clientResponse = builder.get(ClientResponse.class);
									if (clientResponse.getStatus() == 200 && clientResponse.hasEntity()) {
										
										logger.debug("status: " + clientResponse.toString());
										String response = clientResponse.getEntity(String.class);
//										String response = "path:hsa00010	Glycolysis / Gluconeogenesis - Homo sapiens (human)\npath:hsa00020	Citrate cycle (TCA cycle) - Homo sapiens (human)\npath:hsa00030	Pentose phosphate pathway - Homo sapiens (human)";
										boolean breakpoint = false;
										pathwayRootNode.removeAllChildren();
										pathwayRootNode.setUserObject("Pathways");
										
										while(true)
										{
											if(response.length()<=1 || response.indexOf(":")==-1)
											{
												break;
											}
											String[] tmp = new String[2];
											response = response.substring(response.indexOf(":")+1);
											tmp[0] = response.substring(0,response.indexOf("\t"));
											response = response.substring(response.indexOf("\t")+1);
											if(response.indexOf("\n")==-1)
											{
												tmp[1] = response;
												breakpoint = true;
											}
											else
											{
												if(response.indexOf(" - ")!=-1 && response.indexOf(" - ")<response.indexOf("\n"))
													tmp[1] = response.substring(0, response.indexOf(" - "));
												else
													tmp[1] = response.substring(0, response.indexOf("\n"));
												response = response.substring(response.indexOf("\n")+1);
											}
											pathwayNameToPathway.put(tmp[1], tmp[0]);
										
											@SuppressWarnings("unchecked")
											Enumeration<DefaultMutableTreeNode> leafEnum = allPathwayRootNode.depthFirstEnumeration();
											while(leafEnum.hasMoreElements())
											{
												DefaultMutableTreeNode leaf = leafEnum.nextElement();
												if(leaf.isLeaf())
												{
													DefaultMutableTreeNode activeNode = pathwayRootNode;
													if(tmp[1].equals(leaf.getUserObject()))
													{
														TreeNode[] path =  leaf.getPath();
														for(int i = 1; i < path.length-2; i++)
														{
															@SuppressWarnings("unchecked")
															Enumeration<DefaultMutableTreeNode> children = activeNode.children();
															boolean found = false;
															while(children.hasMoreElements())
															{
																DefaultMutableTreeNode child = children.nextElement();
																if(child.getUserObject().equals(((DefaultMutableTreeNode)path[i+1]).getUserObject()))
																{
																	found = true;
																	activeNode = child;
																	break;
																}
															}
															if(!found)
															{
																DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(((DefaultMutableTreeNode)path[i+1]).getUserObject());
																activeNode.add(newNode);
																activeNode = newNode;
																
															}
														}
														activeNode.add(new DefaultMutableTreeNode(tmp[1]));
													}
												}
											}
											
											if(breakpoint)
											{
												break;
											}
										}
									}
							}
							catch(Exception e)
							{
								e.printStackTrace();
							}
						}
					},
					new Runnable(){
						public void run(){
							pathwayTree.setRootVisible(false);
							pathwayTreeModel.reload();
							splitPane.setDividerLocation(0.2d);
						}
					}
			);
		}
	}
	
	
	/**
	 * Wird aufgerufen, wenn ein Pathway aus der Liste ausgewaehlt wird. Veranlasst den Download der entsprechenden Datei.
	 * Speichert Informationen �ber EC-Nummern und alternative Namen in den ersten beiden Hidden Labels der Knoten. 
	 */
	protected void downloadPathway(){
		if(selectedPathwayNode != null)
		{
			logger.debug("Rufe den Graphen www.genome.jp/kegg-bin/download?entry="+pathwayNameToPathway.get(selectedPathwayNode.getUserObject())+"&format=kgml auf.");
	
			BackgroundTaskHelper.issueSimpleTask(
					"Retrieve Pathway",
					"Please wait (Download in progress)...",
					new Runnable() {
						public void run() {
			
						try
						{
							graph = MainFrame.getInstance().getGraph(selectedPathwayNode.getUserObject().toString()+".xml", new URL("http://rest.kegg.jp/get/"+pathwayNameToPathway.get(selectedPathwayNode.getUserObject())+"/kgml"));
//							graph = MainFrame.getInstance().getGraph(new IOurl("http://www.genome.jp/kegg-bin/download?entry="+pathwayNameToPathway.get(selectedPathwayNode.getUserObject())+"&format=kgml"), pathwayNameToPathway.get(selectedPathwayNode.getUserObject())+".xml");
							ArrayList<Node> nodeList = (ArrayList<Node>) graph.getNodes();
							HashMap<String, String[]> keggIDToEntry = new HashMap<String, String[]>();
							String labels = "";
							ArrayList<String> labelList = new ArrayList<String>();
							int count = 0;
							
							for(Node n : nodeList)
							{
								ArrayList<String> ids = getKeggIDsFromNode(n);
								if(count + ids.size() > 100)
								{
									count = 0;
									labelList.add(labels);
									labels = "";
								}
								for(String s : ids)
									labels += "+"+s;
								count += ids.size();
							}
							labelList.add(labels);
							labels = "";
							RestService restService = new RestService(restURL+"list/");
							
							for(String l : labelList)
							{
								labels += restService.makeRequest(l, MediaType.TEXT_PLAIN_TYPE, String.class);
							}
							do
							{
								String [] entry = new String[2];
								entry[0]="";
								entry[1]="";
								String id = labels.substring(0,labels.indexOf("\t"));
								String currNode;
								if(labels.contains("\n"))
								{
									currNode = labels.substring(labels.indexOf("\t")+1,labels.indexOf("\n")).trim();
								}
								else
								{
									currNode = labels.substring(labels.indexOf("\t")+1).trim();
								}
								if(labels.startsWith("path"))
								{
									//tue nichts, da path-Eintr�ge schon richtige Namen haben
								}
								//Sammeln aller Namen der einzelnen Elemente
								else
								{
									if(currNode.contains("[EC"))
										entry[0] = currNode.substring(0,currNode.indexOf("[EC"));
									else
										entry[0] = currNode;
								
									if(!currNode.startsWith("cpd"))
									{
										//nicht alle Eintr�ge haben die EC Nummer in der Form [EC:X.X.X.X] enthalten
										//ebenso m�glich ist EX.X.X.X mit anschlie�endem Komma, Semicolon oder \n
										
										//der g�nstigste Fall ist:
										if(currNode.contains("[EC"))
										{
											String ec = currNode.substring(currNode.indexOf("[EC:")+4);
											ec = ec.substring(0, ec.indexOf("]"));
											String[] splitted = ec.split(" ");
											for(String ecs : splitted)
											{
												if(!entry[1].contains(ecs))
												{
													if(entry[1].length()>1)
													{
														entry[1] += ";";
													}
													entry[1] += ecs;
												}
											}
										}
										//sonst sucher nach Schreibweise EX.X.X.X
										else if(currNode.contains("E1")||currNode.contains("E2")||currNode.contains("E3")
												||currNode.contains("E4")||currNode.contains("E5")||currNode.contains("E6")
												||currNode.contains("E7")||currNode.contains("E8")||currNode.contains("E9"))
										{
											while(true)
											{
												currNode = currNode.substring(currNode.indexOf("E")+1);
												if(currNode.startsWith("1")||currNode.startsWith("2")||currNode.startsWith("3")
														||currNode.startsWith("4")||currNode.startsWith("5")||currNode.startsWith("6")
														||currNode.startsWith("7")||currNode.startsWith("8")||currNode.startsWith("9"))
												{
													break;
												}
											}
											if(entry[1].length()>1)
											{
												entry[1] += ";";
											}
											if(currNode.indexOf(",")<currNode.indexOf(";") && currNode.indexOf(",")>0)
											{
												entry[1] += currNode.substring(0,currNode.indexOf(","));
											}
											else if(currNode.indexOf(";")>0)
											{
												entry[1] += currNode.substring(0,currNode.indexOf(";"));
											}
											else
											{
												entry[1] = currNode.trim();
											}
										}
									}								
								}
								keggIDToEntry.put(id,entry);
								
								if(labels.contains("\n"))
									labels = labels.substring(labels.indexOf("\n")+1);
							}
							while(labels.contains("\n"));
							for( Node n : nodeList)
							{
								ArrayList<String> ids = getKeggIDsFromNode(n);
								AttributeHelper.setLabel(1, n, "", null, AlignmentSetting.HIDDEN.toGMLstring());
								AttributeHelper.setLabel(2, n, "", null, AlignmentSetting.HIDDEN.toGMLstring());
								for(int i = 0; i < ids.size(); i++)
								{
									String[] entry = keggIDToEntry.get(ids.get(i));
									if(entry != null)
									{
										if(!AttributeHelper.getLabel(1, n, "").contains(entry[0]))
										{
											if(!(i == 0))
											{
												AttributeHelper.setLabel(1, n, AttributeHelper.getLabel(1, n, "").trim()+"; ", null, AlignmentSetting.HIDDEN.toGMLstring());
											}
											AttributeHelper.setLabel(1, n, AttributeHelper.getLabel(1, n, "")+entry[0], null, AlignmentSetting.HIDDEN.toGMLstring());										
										}	
										if(!AttributeHelper.getLabel(2, n, "").contains(entry[1]))
										{
											if(!(i == 0))
											{
												AttributeHelper.setLabel(2, n, AttributeHelper.getLabel(2, n, "")+";", null, AlignmentSetting.HIDDEN.toGMLstring());										
											}
											AttributeHelper.setLabel(2, n, AttributeHelper.getLabel(2, n, "")+entry[1], null, AlignmentSetting.HIDDEN.toGMLstring());										
										}
									}
									else
										System.out.println("null?!");
								}
							}
							
						}
						catch(NullPointerException e)
						{
							e.printStackTrace();
							ErrorMsg.addErrorMessage("Graph not found. Check your internet connection.");
						}
						catch(Exception e){
							e.printStackTrace();
						}
					}
					},
						new Runnable(){public void run(){
							MainFrame.getInstance().showGraph(graph, null);
							if(getAutoPrettifyLabelSetting())
							{
								renameNodes(1);
							}
						}}
				);
		}					
	}

	
	private ArrayList<String> getKeggIDsFromNode(Node node)
	{
		ArrayList<String> ids = new ArrayList<String>();
		String id = KeggGmlHelper.getKeggId(node);
		while(id.trim().contains(" "))
		{
			ids.add(id.trim().substring(0,id.indexOf(" ")));
			id = id.trim().substring(id.indexOf(" ")+1);
		}
		ids.add(id.trim());
		return ids;
	}
	
	
	protected static boolean getAutoPrettifyLabelSetting() {
		if (prettifyLabels == null)
			return true;
		else
			return prettifyLabels.isSelected();
	}	
	
	/**
	 * Funktion zum Behandeln des "Download Organism List"-Buttons.
	 * Veranlasst den Download der Liste der Organismen.
	 */
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == getOrganismListFromKegg) {
			downloadOrganismList();
		}
	}
	
	public static OrganismEntry[] getKEGGorganismFromUser(final Collection<OrganismEntry> organisms) {
		final MutableList organismSelection = new MutableList(new DefaultListModel());
		
		organismSelection.setPrototypeCellValue("<html>ÄÖyz");
		organismSelection.setFixedCellWidth(580);
		organismSelection.setFixedCellHeight(new JLabel("<html>AyÖÄ").getPreferredSize().height);
		
		Collections.sort((List<OrganismEntry>) organisms,
							new Comparator<OrganismEntry>() {
								public int compare(final OrganismEntry arg0, OrganismEntry arg1) {
									if (arg0.toString().contains("Reference"))
										return -1;
									return arg0.toString().compareTo(arg1.toString());
								}
							});
		for (OrganismEntry oe : organisms) {
			organismSelection.getContents().addElement(oe);
		}
		organismSelection.setSelectedIndex(0);
		
		final JLabel searchResult = new JLabel("<html><small><font color='gray'>" + organisms.size() + " entries");
		
		JScrollPane organismSelectionScrollPane = new JScrollPane(organismSelection);
		
		organismSelectionScrollPane.setPreferredSize(new Dimension(600, 300));
		
		final JTextField filter = new JTextField("");
		
		filter.addKeyListener(new KeyListener() {
			
			public void keyPressed(KeyEvent e) {
				//
				
			}
			
			public void keyReleased(KeyEvent e) {
				//
				
			}
			
			public void keyTyped(KeyEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						String filterText = filter.getText().toUpperCase();
						
						organismSelection.getContents().clear();
						for (OrganismEntry oe : organisms) {
							if (oe.toString().toUpperCase().contains(filterText) || oe.getShortName().equals("map") || oe.getShortName().equals("ko"))
								organismSelection.getContents().addElement(oe);
						}
						searchResult.setText("<html><small><font color='gray'>" + organismSelection.getContents().size() + "/" + organisms.size() + " entries shown");
					};
				});
			}
		});
		
		// MyOrganismSelectionDialog osd = new MyOrganismSelectionDialog();
		Object[] result = MyInputHelper.getInput(
							"Please select the desired organisms.<br>" +
												"<small>You may use the Search-Field to locate the " +
												"desired organism.",
							"Select Organisms",
							new Object[] {
												"Select Organisms", organismSelectionScrollPane,
												"Search", filter,
												"", searchResult
				});
		if (result != null && organismSelection.getSelectedValue() != null) {
			Object[] ooo = organismSelection.getSelectedValues();
			ArrayList<OrganismEntry> res = new ArrayList<OrganismEntry>();
			for (Object o : ooo)
				res.add((OrganismEntry) o);
			OrganismEntry[] oe = res.toArray(new OrganismEntry[] {});
			return oe;
		}
		return null;
	}
	
	
	
	/**
	 * Ruft das Fenster zum �ndern der Label auf.
	 */
	private void replaceLabelDialog()
	{

		
		final JDialog frame = new JDialog(MainFrame.getInstance(),"Label replacement");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setModal(true);
		frame.setPreferredSize(new Dimension(310,250));
		frame.setLocationRelativeTo(MainFrame.getInstance());
		
		
		JPanel cp = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		cp.setOpaque(true);
		
		JLabel l = new JLabel("Compounds");
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		cp.add(l, gbc);
		
		final JRadioButton rbCID = new JRadioButton("Compound ID");
		gbc.gridy = 1;
		cp.add(rbCID, gbc);
		
		final JRadioButton rbCName = new JRadioButton("Name");
		gbc.gridy = 2;
		cp.add(rbCName, gbc);
		
		ButtonGroup bgCompounds = new ButtonGroup();
		bgCompounds.add(rbCID);
		bgCompounds.add(rbCName);
		
		
		l = new JLabel("Reactions");
		gbc.gridx = 1;
		gbc.gridy = 0;
		cp.add(l,gbc);
		
		final JRadioButton rbRID = new JRadioButton("Reaction ID");
		gbc.gridy = 1;
		cp.add(rbRID,gbc);
		
		final JRadioButton rbRName = new JRadioButton("Name");
		gbc.gridy = 2;
		cp.add(rbRName, gbc);
		
		final JRadioButton rbREC = new JRadioButton("EC number");
		gbc.gridy = 3;
		cp.add(rbREC, gbc);
		
		final JRadioButton rbREnzymeID = new JRadioButton("Enzyme ID (SOAP)");
		gbc.gridy = 4;
		cp.add(rbREnzymeID, gbc);
		
		final JRadioButton rbRECSib = new JRadioButton("EC number (SIB Enzyme Database)");
		gbc.gridy = 5;
		cp.add(rbRECSib,gbc);
		
		final JRadioButton rbRNameSib = new JRadioButton("Name (SIB Enzyme Database)");
		gbc.gridy = 6;
		cp.add(rbRNameSib,gbc);
		
		ButtonGroup bgReactions = new ButtonGroup();
		bgReactions.add(rbRID);
		bgReactions.add(rbRName);
		bgReactions.add(rbREC);
		bgReactions.add(rbREnzymeID);
		bgReactions.add(rbRECSib);
		bgReactions.add(rbRNameSib);
		
		
		
		JSeparator sep = new JSeparator();
		sep.setPreferredSize(new Dimension(100,4));
		gbc.gridx = 0;
		gbc.gridy = 7;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridwidth = 2;
		cp.add(sep,gbc);

		JPanel buttonPanel = new JPanel(new FlowLayout());
		JButton buttonOK = new JButton("OK");
		buttonOK.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				if(rbCID.isSelected())
				{
					renameNodes(0);
				}
				if(rbCName.isSelected())
				{
					renameNodes(1);
				}
				if(rbRID.isSelected())
				{
					renameNodes(2);
				}
				if(rbRName.isSelected())
				{
					renameNodes(3);
				}
				if(rbREC.isSelected())
				{
					renameNodes(4);
				}
				if(rbREnzymeID.isSelected())
				{
					renameNodes(5);
				}
				if(rbRECSib.isSelected())
				{
					renameNodes(6);
				}
				if(rbRNameSib.isSelected())
				{
					renameNodes(7);
				}
				
				frame.dispose();
			}			
		});
		
		buttonPanel.add(buttonOK);
		JButton buttonCanc = new JButton("Cancel");
		buttonCanc.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				frame.dispose();
			}
			
			});
		
		buttonPanel.add(buttonCanc);
		gbc.gridy = 8;
		cp.add(buttonPanel,gbc);
		
		frame.pack();
		JScrollPane scrollBar=new JScrollPane(cp,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		frame.add(scrollBar);
		frame.setVisible(true);
	}
	
	private void renameNodes(int mode)
	{
		if(mode == 0)
		{
			for(Node n : MainFrame.getInstance().getActiveSession().getGraph().getNodes())
			{
				if(KeggGmlHelper.getKeggId(n).startsWith("cpd"))
					AttributeHelper.setLabel(n, KeggGmlHelper.getKeggId(n).substring(KeggGmlHelper.getKeggId(n).indexOf(":")+1));
			}
		}
		else if(mode == 1)
		{
			for(Node n : MainFrame.getInstance().getActiveSession().getGraph().getNodes())
			{
				if(KeggGmlHelper.getKeggId(n).startsWith("cpd"))
				{
					if(AttributeHelper.getLabel(1, n, "").contains(";"))
						AttributeHelper.setLabel(n, AttributeHelper.getLabel(1, n, "").substring(0,AttributeHelper.getLabel(1, n, "").indexOf(";")));
					else
						AttributeHelper.setLabel(n, AttributeHelper.getLabel(1, n, ""));						
				}
			}
		}
		else if(mode == 2)
		{
			for(Node n : MainFrame.getInstance().getActiveSession().getGraph().getNodes())
			{
				if(!KeggGmlHelper.getKeggId(n).startsWith("cpd") && !KeggGmlHelper.getKeggId(n).startsWith("path"))
					AttributeHelper.setLabel(n, KeggGmlHelper.getKeggReactions(n).get(0).getValue().substring(KeggGmlHelper.getKeggReactions(n).get(0).getValue().indexOf(":")+1));
			}
		}
		else if(mode == 3)
		{
			for(Node n : MainFrame.getInstance().getActiveSession().getGraph().getNodes())
			{
				if(!KeggGmlHelper.getKeggId(n).startsWith("cpd") && !KeggGmlHelper.getKeggId(n).startsWith("path"))
				{
					if(AttributeHelper.getLabel(1, n, "").contains(";"))
						AttributeHelper.setLabel(n, AttributeHelper.getLabel(1, n, "").substring(0,AttributeHelper.getLabel(1, n, "").indexOf(";")));
					else
						AttributeHelper.setLabel(n, AttributeHelper.getLabel(1, n, ""));						
				}
			}
		}
		else if(mode == 4)
		{
			for(Node n : MainFrame.getInstance().getActiveSession().getGraph().getNodes())
			{
				if(!KeggGmlHelper.getKeggId(n).startsWith("cpd") && !KeggGmlHelper.getKeggId(n).startsWith("path"))
				{
					if(AttributeHelper.getLabel(2, n, "").contains(";"))
						AttributeHelper.setLabel(n, AttributeHelper.getLabel(2, n, "").substring(0,AttributeHelper.getLabel(2, n, "").indexOf(";")));
					else
						AttributeHelper.setLabel(n, AttributeHelper.getLabel(2, n, ""));						
				}
			}			
		}
		else if(mode == 5)
		{
			int numNodes = MainFrame.getInstance().getActiveSession().getGraph().getNodes().size();
			
			BackgroundTaskHelper bgth = new BackgroundTaskHelper( 
					new Runnable(){
						public void run(){
							int currNode = 0;
							KEGGPortType serv;
							stopTask = false;
							try {
								KEGGLocator locator = new KEGGLocator();
								serv = locator.getKEGGPort();
							} catch (ServiceException e) {
								ErrorMsg.addErrorMessage(e);
								serv = null;
							}
							int size = MainFrame.getInstance().getActiveSession().getGraph().getNodes().size();
							int index =0;
							for(Node n : MainFrame.getInstance().getActiveSession().getGraph().getNodes())
							{
								if(stopTask)
									break;
								index++;
								status1 = "Processing node: "+AttributeHelper.getLabel(n, "");
								status2 = (size-index)+" nodes left";
								statusValue = (double)index / (double)size * 100d;
								currNode++;
								if(!AttributeHelper.getLabel(n, "").startsWith("cpd"))
								{
									String myTargetName = "";
									for (IndexAndString ias : KeggGmlHelper.getKeggReactions(n)) {
										try {
											String[] enz = serv.get_enzymes_by_reaction(ias.getValue());
											for (int i = 0; i < enz.length; i++) {
												if (myTargetName.length() > 0)
													myTargetName = myTargetName + ", " + enz[i];
												else
													myTargetName = enz[i];
											}
											if (myTargetName.length() > 0) {
												myTargetName = StringManipulationTools.stringReplace(myTargetName, "ec:", "");
											}
											AttributeHelper.setLabel(n, myTargetName);
										} catch (RemoteException e) {
											ErrorMsg.addErrorMessage(e);
										}
									}
								}
							}
						}
					
					},
					this,
					"SOAP request", "SOAP request",
					true,false
			);
			bgth.startWork(MainFrame.getInstance());
			

		}
		else if(mode == 6 || mode == 7)
		{
			for(Node n : MainFrame.getInstance().getActiveSession().getGraph().getNodes())
			{
				String label = AttributeHelper.getLabel(2, n,"");
				if(label.contains(";"))
					label = label.substring(0, label.indexOf(";"));
				EnzymeEntry ee = EnzymeService.getEnzymeInformation(label, false);
				if (ee != null) {
					if (mode == 7)
						AttributeHelper.setLabel(n, ee.getDE());
					else
						AttributeHelper.setLabel(n, ee.getID());
				}
			}
		}
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.selection.SelectionListener#selectionListChanged(org.graffiti.selection.SelectionEvent)
	 */
	public void selectionListChanged(SelectionEvent e) {
		// empty
	}
	

	
	@Override
	public boolean visibleForView(View v) {
		return v == null || v instanceof GraphView;
	}
	public int getCurrentStatusValue() {
		return (int)statusValue;
	}
	public void setCurrentStatusValue(int value) {
		statusValue = value;
	}
	public double getCurrentStatusValueFine() {
		return statusValue;
	}
	public String getCurrentStatusMessage1() {
		return status1;
	}
	public String getCurrentStatusMessage2() {
		return status2;
	}
	public void pleaseStop() {
		stopTask = true;
	}
	public boolean pluginWaitsForUser() {
		return false;
	}
	public void pleaseContinueRun() {
	}
	

	@Override
	public String getTabParentPath() {
		return "External Pathways";
	}

	@Override
	public int getPreferredTabPosition() {
		return InspectorTab.TAB_LEADING;
	}
	

}

