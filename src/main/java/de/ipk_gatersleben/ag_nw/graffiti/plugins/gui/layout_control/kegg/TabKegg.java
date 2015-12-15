/*******************************************************************************
 * Copyright (c) 2003-2014 IPK Gatersleben, Germany
 * Copyright (c) 2014-2015 Monash University, Australia
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.ArrayList;
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

import org.AlignmentSetting;
import org.AttributeHelper;
import org.BackgroundTaskStatusProvider;
import org.ErrorMsg;
import org.JMButton;
import org.apache.log4j.Logger;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.inspector.InspectorTab;
import org.graffiti.plugin.view.GraphView;
import org.graffiti.plugin.view.View;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;

import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.sib_enzymes.EnzymeEntry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.sib_enzymes.EnzymeService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.KeggGmlHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.web.RestService;

/**
 * @author Christian Klukas, Torsten Vogt, Matthias Klapperstueck, Tobias Czauderna
 */
public class TabKegg extends InspectorTab implements ActionListener, BackgroundTaskStatusProvider {
	
	private static final long serialVersionUID = 1L;
	
	Logger logger = Logger.getLogger(TabKegg.class);
	private static JCheckBox prettifyLabels = null;
	
	private static final String restURL = "http://rest.kegg.jp/";
	private static final String initialUserInfo = "Please download the list of organisms!";
	
	Client client;
	
	JButton getOrganismListFromKegg;
	
	JTree organismTree;
	DefaultMutableTreeNode organismRootNode;
	DefaultTreeModel organismTreeModel;
	JScrollPane organismTreeScroll;
	DefaultMutableTreeNode selectedOrganismNode;
	HashMap<String, String> treeNodeToKeggCode = new HashMap<String, String>(); // name | id
	
	JTree allPathwayTree;
	DefaultMutableTreeNode allPathwayRootNode;
	
	JTree pathwayTree;
	DefaultMutableTreeNode pathwayRootNode;
	DefaultTreeModel pathwayTreeModel;
	JScrollPane pathwayTreeScroll;
	DefaultMutableTreeNode selectedPathwayNode;
	HashMap<String, String> pathwayNameToPathway = new HashMap<String, String>(); // name | id
	
	JSplitPane splitPane;
	boolean isSetDividerLocation = false;
	
	Graph graph = null;
	
	RestService restService;
	
	String status1 = "";
	String status2 = "";
	double statusValue = 0;
	boolean stopTask = false;
	
	public TabKegg() {
		
		super();
		this.title = "KEGG";
		initComponents();
		
	}
	
	/**
	 * KEGG Tab
	 */
	private void initComponents() {
		client = Client.create();
		double border = 5;
		double[][] size = { { border, TableLayoutConstants.FILL, border }, // columns
				{ border,
						TableLayout.PREFERRED,
						TableLayoutConstants.FILL,
						TableLayout.PREFERRED,
						TableLayout.PREFERRED,
						border } }; // rows
		this.setLayout(new TableLayout(size));
		
		getOrganismListFromKegg = new JMButton("<html>Download List of Organisms");
		
		getOrganismListFromKegg.addActionListener(this);
		getOrganismListFromKegg.setOpaque(false);
		getOrganismListFromKegg.setToolTipText("Downloads list of organisms from KEGG (needs internet connection).");
		
		organismRootNode = new DefaultMutableTreeNode(initialUserInfo);
		allPathwayRootNode = new DefaultMutableTreeNode("Pathways");
		pathwayRootNode = new DefaultMutableTreeNode(initialUserInfo);
		
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
		
		TreeSelectionListener tsl = new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)
						e.getPath().getLastPathComponent();
				if (node == null)
					return;
				if (node.isLeaf()) {
					selectedOrganismNode = node;
				}
				else
					selectedOrganismNode = null;
			}
		};
		organismTree.addTreeSelectionListener(tsl);
		
		pathwayTreeModel = new DefaultTreeModel(pathwayRootNode);
		pathwayTree = new JTree(pathwayTreeModel);
		pathwayTreeScroll = new JScrollPane(pathwayTree);
		selectedPathwayNode = null;
		
		TreeSelectionListener tsl2 = new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)
						e.getPath().getLastPathComponent();
				if (node == null)
					return;
				if (node.isLeaf()) {
					selectedPathwayNode = node;
				}
				else
					selectedPathwayNode = null;
			}
		};
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
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, organismTreeScroll, pathwayTreeScroll);
		splitPane.setOneTouchExpandable(true);
		prettifyLabels = new JCheckBox("Replace compound IDs by compound names", false);
		prettifyLabels.setOpaque(false);
		prettifyLabels.setToolTipText("Replaces KEGG compound IDs by compound names from the KEGG database during pathway download.");
		JButton replaceLabels = new JMButton("Modify Labels");
		replaceLabels.setOpaque(false);
		replaceLabels.setToolTipText("Click to modify node labels of the shown KEGG pathway.");
		replaceLabels.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				replaceLabelDialog();
			}
		});
		final JTextField searchBox = new JTextField("");
		searchBox.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						String searchText = searchBox.getText();
						if (searchText.length() > 0) {
							breadthFirstSearch(pathwayRootNode, searchText, pathwayTree);
							breadthFirstSearch(organismRootNode, searchText, organismTree);
						}
						
					}
					
					private boolean breadthFirstSearch(DefaultMutableTreeNode node, String searchText, JTree tree) {
						for (int i = 0; i < node.getChildCount(); i++) {
							DefaultMutableTreeNode innerNode = (DefaultMutableTreeNode) node.getChildAt(i);
							if (innerNode.getUserObject().toString().toLowerCase().indexOf(searchText.toLowerCase()) != -1 && innerNode.isLeaf()) {
								@SuppressWarnings("unchecked")
								Enumeration<DefaultMutableTreeNode> children = ((DefaultMutableTreeNode) tree.getModel().getRoot()).children();
								while (children.hasMoreElements())
									tree.collapsePath(new TreePath(children.nextElement().getPath()));
								TreePath path = new TreePath(innerNode.getPath());
								tree.expandPath(path);
								tree.setSelectionPath(path);
								tree.scrollPathToVisible(path);
								tree.updateUI();
								return true;
							}
							if (!innerNode.isLeaf())
								if (breadthFirstSearch(innerNode, searchText, tree))
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
		JComponent searchPathway = TableLayout.get3Split(new JLabel("Search"), null, searchBox, TableLayout.PREFERRED, 2, TableLayout.FILL);
		this.add(getOrganismListFromKegg, "1,1");
		this.add(splitPane, "1,2");
		splitPane.setDividerLocation((int) organismTreeScroll.getPreferredSize().getHeight());
		this.add(TableLayout.get3SplitVertical(searchPathway, prettifyLabels, replaceLabels,
				TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, 2, 2), "1,3");
		this.validate();
	}
	
	protected void downloadOrganismList() {
		BackgroundTaskHelper.issueSimpleTask(
				"Downloading List of Organisms", "Please wait ...",
				new Runnable() {
					public void run() {
						String response = (String) RestService.makeRequest(restURL + "list/organism", MediaType.TEXT_PLAIN, String.class);
						if (response != null) {
							if (!isSetDividerLocation) {
								splitPane.setDividerLocation(0.3d);
								isSetDividerLocation = true;
							}
							organismRootNode.removeAllChildren();
							organismRootNode.setUserObject("Organisms");
							pathwayRootNode.removeAllChildren();
							pathwayRootNode.setUserObject("Select an organism");
							DefaultMutableTreeNode reference = new DefaultMutableTreeNode("Reference pathways");
							organismRootNode.add(reference);
							reference.add(new DefaultMutableTreeNode("EC"));
							reference.add(new DefaultMutableTreeNode("KO"));
							reference.add(new DefaultMutableTreeNode("Reaction"));
							treeNodeToKeggCode.put("EC", "ec");
							treeNodeToKeggCode.put("KO", "ko");
							treeNodeToKeggCode.put("Reaction", "rn");
							boolean stop = false;
							while (true) {
								if (response.length() == 0 || response.indexOf("\t") == -1)
									break;
								String[] tmp = new String[3];
								response = response.substring(response.indexOf("\t") + 1);
								tmp[0] = response.substring(0, response.indexOf("\t"));
								response = response.substring(response.indexOf("\t") + 1);
								tmp[1] = response.substring(0, response.indexOf("\t"));
								response = response.substring(response.indexOf("\t") + 1);
								
								treeNodeToKeggCode.put(tmp[1], tmp[0]);
								
								if (response.indexOf("\n") == -1) {
									tmp[2] = response;
									stop = true;
								}
								else {
									tmp[2] = response.substring(0, response.indexOf("\n"));
									response = response.substring(response.indexOf("\n") + 1);
								}
								
								DefaultMutableTreeNode currNode = organismRootNode;
								String tmp2 = tmp[2];
								boolean cont = true;
								while (cont) {
									String temp;
									if (tmp2.indexOf(";") == -1) {
										temp = tmp2;
										cont = false;
									} else {
										temp = tmp2.substring(0, tmp2.indexOf(";"));
										tmp2 = tmp2.substring(tmp2.indexOf(";") + 1);
									}
									boolean hasFound = false;
									int idx = currNode.getChildCount();
									for (int i = 0; i < currNode.getChildCount(); i++) {
										DefaultMutableTreeNode n = (DefaultMutableTreeNode) currNode.getChildAt(i);
										if (n.getUserObject().toString().equals(temp)) {
											currNode = n;
											hasFound = true;
											break;
										} else
											if (idx == currNode.getChildCount() && n.getUserObject().toString().compareToIgnoreCase(temp) > 0)
												idx = i;
									}
									if (!hasFound) {
										DefaultMutableTreeNode tmpNode = new DefaultMutableTreeNode(temp);
										if (idx == currNode.getChildCount())
											currNode.add(tmpNode);
										else
											currNode.insert(tmpNode, idx);
										currNode = tmpNode;
									}
								}
								int idx = currNode.getChildCount();
								for (int i = 0; i < currNode.getChildCount(); i++) {
									DefaultMutableTreeNode n = (DefaultMutableTreeNode) currNode.getChildAt(i);
									if (idx == currNode.getChildCount() && n.getUserObject().toString().compareToIgnoreCase(tmp[1]) > 0)
										idx = i;
								}
								if (idx == currNode.getChildCount())
									currNode.add(new DefaultMutableTreeNode(tmp[1]));
								else
									currNode.insert(new DefaultMutableTreeNode(tmp[1]), idx);
								if (stop)
									break;
							}
						}
						response = (String) RestService.makeRequest(restURL + "get/br:br08901", MediaType.TEXT_PLAIN, String.class);
						if (response != null) {
							allPathwayRootNode.removeAllChildren();
							allPathwayRootNode.setUserObject("Pathways");
							DefaultMutableTreeNode nodeA = null, nodeB = null;
							
							while (true) {
								response = response.substring(response.indexOf("\n") + 1);
								if (response.startsWith("A"))
									break;
							}
							
							while (true) {
								if (response.startsWith("!") || response.length() == 0 || response.indexOf("\n") == -1)
									break;
								String nextLine = response.substring(0, response.indexOf("\n"));
								response = response.substring(response.indexOf("\n") + 1);
								if (nextLine.startsWith("A")) {
									nextLine = nextLine.substring(1);
									nodeA = new DefaultMutableTreeNode(nextLine.trim());
									allPathwayRootNode.add(nodeA);
									
								}
								else
									if (nextLine.startsWith("B")) {
										nextLine = nextLine.substring(1);
										nextLine = nextLine.trim();
										nodeB = new DefaultMutableTreeNode(nextLine);
										nodeA.add(nodeB);
									}
									else
										if (nextLine.startsWith("C")) {
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
	
	protected void downloadPathwayList() {
		if (selectedOrganismNode != null && !selectedOrganismNode.getUserObject().toString().equals(initialUserInfo))
		{
			BackgroundTaskHelper.issueSimpleTask(
					"Downloading List of Pathways", "Please wait ...",
					new Runnable() {
						public void run() {
							WebResource webResource = client.resource(restURL);
							WebResource webResourceListOrganisms = webResource.path("list/").path("pathway/")
									.path(treeNodeToKeggCode.get(selectedOrganismNode.getUserObject().toString()));
							Builder builder = webResourceListOrganisms.accept(MediaType.TEXT_PLAIN);
							ClientResponse clientResponse = null;
							try {
								clientResponse = builder.get(ClientResponse.class);
								if (clientResponse.getStatus() == 200 && clientResponse.hasEntity()) {
									logger.info("REST status: " + clientResponse.toString());
									String response = clientResponse.getEntity(String.class);
									boolean stop = false;
									pathwayRootNode.removeAllChildren();
									pathwayRootNode.setUserObject("Pathways");
									while (true) {
										if (response.length() <= 1 || response.indexOf(":") == -1)
											break;
										String[] tmp = new String[2];
										response = response.substring(response.indexOf(":") + 1);
										tmp[0] = response.substring(0, response.indexOf("\t"));
										response = response.substring(response.indexOf("\t") + 1);
										if (response.indexOf("\n") == -1) {
											tmp[1] = response;
											stop = true;
										} else {
											if (response.indexOf(" - ") != -1 && response.indexOf(" - ") < response.indexOf("\n"))
												tmp[1] = response.substring(0, response.indexOf(" - "));
											else
												tmp[1] = response.substring(0, response.indexOf("\n"));
											response = response.substring(response.indexOf("\n") + 1);
										}
										pathwayNameToPathway.put("[" + tmp[0] + "] " + tmp[1], tmp[0]);
										@SuppressWarnings("unchecked")
										Enumeration<DefaultMutableTreeNode> leafEnum = allPathwayRootNode.depthFirstEnumeration();
										while (leafEnum.hasMoreElements()) {
											DefaultMutableTreeNode leaf = leafEnum.nextElement();
											if (leaf.isLeaf()) {
												DefaultMutableTreeNode activeNode = pathwayRootNode;
												if (tmp[1].equals(leaf.getUserObject().toString())) {
													TreeNode[] path = leaf.getPath();
													for (int i = 1; i < path.length - 2; i++) {
														@SuppressWarnings("unchecked")
														Enumeration<DefaultMutableTreeNode> children = activeNode.children();
														boolean hasFound = false;
														while (children.hasMoreElements()) {
															DefaultMutableTreeNode child = children.nextElement();
															if (child.getUserObject().toString().equals(((DefaultMutableTreeNode) path[i + 1]).getUserObject().toString())) {
																activeNode = child;
																hasFound = true;
																break;
															}
														}
														if (!hasFound) {
															DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(((DefaultMutableTreeNode) path[i + 1])
																	.getUserObject().toString());
															activeNode.add(newNode);
															activeNode = newNode;
															
														}
													}
													activeNode.add(new DefaultMutableTreeNode("[" + tmp[0] + "] " + tmp[1]));
												}
											}
										}
										if (stop)
											break;
									}
								}
							} catch (Exception e) {
								ErrorMsg.addErrorMessage("Could not download list of pathways!\n" + e.getMessage());
							}
						}
					},
					new Runnable() {
						public void run() {
							pathwayTree.setRootVisible(false);
							pathwayTreeModel.reload();
						}
					}
					);
		}
	}
	
	/**
	 * Wird aufgerufen, wenn ein Pathway aus der Liste ausgewaehlt wird. Veranlasst den Download der entsprechenden Datei.
	 * Speichert Informationen ueber EC-Nummern und alternative Namen in den ersten beiden Hidden Labels der Knoten.
	 */
	protected void downloadPathway() {
		
		if (selectedPathwayNode != null && !selectedPathwayNode.getUserObject().toString().equals(initialUserInfo)) {
			final String keggPathwayID = pathwayNameToPathway.get(selectedPathwayNode.getUserObject().toString());
			
			if (keggPathwayID == null) {
				logger.error("No mapping from selected pathway to KEGG pathway ID found");
				return;
			}
			BackgroundTaskHelper.issueSimpleTask("Retrieve Pathway", "Please wait (Download in progress)...", new Runnable() {
				public void run() {
					try {
						String fileName = selectedPathwayNode.getUserObject().toString().replace("[" + keggPathwayID + "] ", "");
						fileName = fileName.replace("/", "-");
						graph = MainFrame.getInstance().getGraph(fileName + ".xml", new URL(restURL + "get/" + keggPathwayID + "/kgml"));
						if (graph == null) {
							ErrorMsg.addErrorMessage("Could not download KEGG pathway!");
							return;
						}
						ArrayList<Node> nodeList = (ArrayList<Node>) graph.getNodes();
						HashMap<String, String> keggIDToEntry = new HashMap<String, String>();
						String labels = "";
						ArrayList<String> labelList = new ArrayList<String>();
						int count = 0;
						for (Node n : nodeList) {
							ArrayList<String> ids = getKeggIDsFromNode(n);
							if (count + ids.size() > 100) {
								count = 0;
								labelList.add(labels);
								labels = "";
							}
							// ignore path, rc, rp
							for (String s : ids)
								if (!s.startsWith("path") && !s.startsWith("rc") && !s.startsWith("rp")) {
									labels += "+" + s;
									count += ids.size();
								}
						}
						labelList.add(labels);
						labels = "";
						RestService restService = new RestService(restURL + "list/");
						for (String l : labelList)
							labels += restService.makeRequest(l, MediaType.TEXT_PLAIN_TYPE, String.class);
						do {
							String entry = "";
							String id = labels.substring(0, labels.indexOf("\t"));
							String currNode;
							if (labels.contains("\n"))
								currNode = labels.substring(labels.indexOf("\t") + 1, labels.indexOf("\n")).trim();
							else
								currNode = labels.substring(labels.indexOf("\t") + 1).trim();
							if (labels.startsWith("ec") && !currNode.startsWith("Deleted entry")) {
								if (currNode.toLowerCase().startsWith("Transferred to ")) {
									currNode = currNode.replace("Transferred to ", "");
									currNode = currNode.replace(" and", ";");
								}
								entry = currNode;
							}
							else
								entry = currNode;
							if (entry.length() > 0)
								keggIDToEntry.put(id, entry);
							if (labels.contains("\n"))
								labels = labels.substring(labels.indexOf("\n") + 1);
						} while (labels.contains("\n"));
						for (Node n : nodeList) {
							AttributeHelper.setLabel(1, n, AttributeHelper.getLabel(n, ""), null, AlignmentSetting.HIDDEN.toGMLstring());
							ArrayList<String> ids = getKeggIDsFromNode(n);
							for (int i = 0; i < ids.size(); i++) {
								String entry = keggIDToEntry.get(ids.get(i));
								// for EC numbers like 1.1.1.- KEGG can't provide information, entry is null
								// some EC numbers have been removed from KEGG ("Deleted entry"), entry is null as well
								if (entry != null) {
									// names are separated by ';'
									// but it can happen that information is provided in parentheses with separation by ';'
									// replace all ';' in '(...; ...)' by ','
									if (entry.contains("(")) {
										int fromIndex = 0;
										int length = entry.length();
										while (fromIndex < length) {
											int idx1 = entry.indexOf("(", fromIndex);
											int idx2 = entry.indexOf(";", fromIndex);
											int idx3 = entry.indexOf(")", fromIndex);
											if (idx2 > idx1 && idx2 < idx3)
												entry = entry.substring(0, idx2) + "," + entry.substring(idx2 + 1);
											if (idx1 != -1 && idx3 != -1)
												fromIndex = idx3 + 1;
											else
												fromIndex = length;
										}
									}
									int k = 2;
									while (entry.contains("; ")) {
										AttributeHelper.setLabel(k, n, entry.substring(0, entry.indexOf("; ")), null, AlignmentSetting.HIDDEN.toGMLstring());
										entry = entry.substring(entry.indexOf("; ") + 2);
										k++;
									}
									AttributeHelper.setLabel(k, n, entry, null, AlignmentSetting.HIDDEN.toGMLstring());
								}
							}
						}
					} catch (Exception e) {
						ErrorMsg.addErrorMessage("Could not download KEGG pathway!\n" + e.getMessage());
					}
				}
			},
					new Runnable() {
						public void run() {
							MainFrame.getInstance().showGraph(graph, null);
							if (prettifyLabels.isSelected())
								renameNodes(1);
						}
					}
					);
		}
		
	}
	
	private ArrayList<String> getKeggIDsFromNode(Node node) {
		
		ArrayList<String> ids = new ArrayList<String>();
		String id = KeggGmlHelper.getKeggId(node);
		while (id.trim().contains(" ")) {
			ids.add(id.trim().substring(0, id.indexOf(" ")));
			id = id.trim().substring(id.indexOf(" ") + 1);
		}
		ids.add(id.trim());
		return ids;
		
	}
	
	public void actionPerformed(ActionEvent e) {
		
		if (e.getSource() == getOrganismListFromKegg)
			downloadOrganismList();
		
	}
	
	public static OrganismEntry[] getKEGGorganismFromUser(final List<OrganismEntry> organisms) {
		final MutableList organismSelection = new MutableList(new DefaultListModel());
		
		organismSelection.setPrototypeCellValue("<html>ÄÖyz");
		organismSelection.setFixedCellWidth(580);
		organismSelection.setFixedCellHeight(new JLabel("<html>AyÖÄ").getPreferredSize().height);
		
		Collections.sort(organisms,
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
			}
			
			public void keyReleased(KeyEvent e) {
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
		Object[] result = MyInputHelper.getInput(
				"Please select organisms.<br><small>You may use the Search field to find organisms.", "Select Organisms",
				new Object[] { "Select Organisms", organismSelectionScrollPane, "Search", filter, "", searchResult }
				);
		if (result != null && organismSelection.getSelectedValue() != null) {
			List ooo = organismSelection.getSelectedValuesList();
			ArrayList<OrganismEntry> res = new ArrayList<OrganismEntry>();
			for (Object o : ooo)
				res.add((OrganismEntry) o);
			OrganismEntry[] oe = res.toArray(new OrganismEntry[] {});
			return oe;
		}
		return null;
	}
	
	private void replaceLabelDialog() {
		
		Graph g = null;
		if (MainFrame.getInstance().getActiveEditorSession() != null)
			g = MainFrame.getInstance().getActiveEditorSession().getGraph();
		final String organism = KeggGmlHelper.getKeggOrg(g);
		
		final JDialog frame = new JDialog(MainFrame.getInstance(), "Modify Labels");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setModal(true);
		if (organism != null && organism.toLowerCase().equals("ec"))
			frame.setPreferredSize(new Dimension(344, 283));
		else
			frame.setPreferredSize(new Dimension(279, 229));
		frame.setLocationRelativeTo(MainFrame.getInstance());
		
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setOpaque(true);
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		
		int y;
		y = 0;
		JLabel label = new JLabel("Set compound labels to");
		gridBagConstraints.insets = new Insets(10, 10, 10, 10);
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = y++;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		panel.add(label, gridBagConstraints);
		
		final JRadioButton rbCID = new JRadioButton("IDs");
		gridBagConstraints.insets = new Insets(0, 10, 0, 0);
		gridBagConstraints.gridy = y++;
		panel.add(rbCID, gridBagConstraints);
		
		final JRadioButton rbCName = new JRadioButton("Names");
		gridBagConstraints.gridy = y++;
		panel.add(rbCName, gridBagConstraints);
		
		ButtonGroup bgCompounds = new ButtonGroup();
		bgCompounds.add(rbCID);
		bgCompounds.add(rbCName);
		
		y = 0;
		label = new JLabel("Set reaction labels to");
		gridBagConstraints.insets = new Insets(10, 10, 10, 10);
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = y++;
		panel.add(label, gridBagConstraints);
		
		final JRadioButton rbRID = new JRadioButton("IDs");
		gridBagConstraints.insets = new Insets(0, 10, 0, 10);
		gridBagConstraints.gridy = y++;
		panel.add(rbRID, gridBagConstraints);
		
		final JRadioButton rbRName1 = new JRadioButton("Names (Set 1)");
		gridBagConstraints.gridy = y++;
		panel.add(rbRName1, gridBagConstraints);
		
		final JRadioButton rbRName2 = new JRadioButton("Names (Set 2)");
		gridBagConstraints.gridy = y++;
		panel.add(rbRName2, gridBagConstraints);
		
		final JRadioButton rbRReactionID = new JRadioButton("Reaction IDs");
		gridBagConstraints.gridy = y++;
		panel.add(rbRReactionID, gridBagConstraints);
		
		final JRadioButton rbRECSib = new JRadioButton("IDs (SIB Enzyme Database)");
		final JRadioButton rbRNameSib = new JRadioButton("Names (SIB Enzyme Database)");
		if (organism != null && organism.toLowerCase().equals("ec")) {
			gridBagConstraints.gridy = y++;
			panel.add(rbRECSib, gridBagConstraints);
			gridBagConstraints.gridy = y++;
			panel.add(rbRNameSib, gridBagConstraints);
		}
		
		ButtonGroup bgReactions = new ButtonGroup();
		bgReactions.add(rbRID);
		bgReactions.add(rbRName1);
		bgReactions.add(rbRName2);
		bgReactions.add(rbRReactionID);
		if (organism != null && organism.toLowerCase().equals("ec")) {
			bgReactions.add(rbRECSib);
			bgReactions.add(rbRNameSib);
		}
		
		JPanel buttonPanel = new JPanel(new FlowLayout());
		JButton buttonOK = new JButton("OK");
		buttonOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (rbCID.isSelected())
					renameNodes(0);
				if (rbCName.isSelected())
					renameNodes(1);
				if (rbRID.isSelected())
					renameNodes(2);
				if (rbRName1.isSelected())
					renameNodes(3);
				if (rbRName2.isSelected())
					renameNodes(4);
				if (rbRReactionID.isSelected())
					renameNodes(5);
				if (organism != null && organism.toLowerCase().equals("ec")) {
					if (rbRECSib.isSelected())
						renameNodes(6);
					if (rbRNameSib.isSelected())
						renameNodes(7);
				}
				frame.dispose();
			}
		});
		JButton buttonCanc = new JButton("Cancel");
		buttonCanc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				frame.dispose();
			}
		});
		buttonOK.setPreferredSize(new Dimension(buttonCanc.getPreferredSize().width, buttonOK.getPreferredSize().height));
		buttonPanel.add(buttonOK);
		buttonPanel.add(buttonCanc);
		gridBagConstraints.insets = new Insets(15, 0, 10, 0);
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = y++;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.anchor = GridBagConstraints.CENTER;
		panel.add(buttonPanel, gridBagConstraints);
		
		frame.pack();
		JScrollPane scrollBar = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		frame.add(scrollBar);
		frame.setVisible(true);
		
	}
	
	private void renameNodes(int mode) {
		
		switch (mode) {
			case 0:
				for (Node n : MainFrame.getInstance().getActiveSession().getGraph().getNodes())
					if (KeggGmlHelper.getKeggId(n).startsWith("cpd"))
						AttributeHelper.setLabel(n, AttributeHelper.getLabel(1, n, ""));
				break;
			case 1:
				for (Node n : MainFrame.getInstance().getActiveSession().getGraph().getNodes())
					if (KeggGmlHelper.getKeggId(n).startsWith("cpd"))
						AttributeHelper.setLabel(n, AttributeHelper.getLabel(2, n, ""));
				break;
			case 2:
				for (Node n : MainFrame.getInstance().getActiveSession().getGraph().getNodes())
					if (!KeggGmlHelper.getKeggId(n).startsWith("cpd") && !KeggGmlHelper.getKeggId(n).startsWith("path"))
						AttributeHelper.setLabel(n, AttributeHelper.getLabel(1, n, ""));
				break;
			case 3:
				for (Node n : MainFrame.getInstance().getActiveSession().getGraph().getNodes())
					if (!KeggGmlHelper.getKeggId(n).startsWith("cpd") && !KeggGmlHelper.getKeggId(n).startsWith("path"))
						AttributeHelper.setLabel(n, AttributeHelper.getLabel(2, n, ""));
				break;
			case 4:
				for (Node n : MainFrame.getInstance().getActiveSession().getGraph().getNodes())
					if (!KeggGmlHelper.getKeggId(n).startsWith("cpd") && !KeggGmlHelper.getKeggId(n).startsWith("path")) {
						if (AttributeHelper.getLabel(3, n, "").length() > 0)
							AttributeHelper.setLabel(n, AttributeHelper.getLabel(3, n, ""));
						else
							AttributeHelper.setLabel(n, AttributeHelper.getLabel(2, n, ""));
					}
				break;
			case 5:
				for (Node n : MainFrame.getInstance().getActiveSession().getGraph().getNodes())
					if (!KeggGmlHelper.getKeggId(n).startsWith("cpd") && !KeggGmlHelper.getKeggId(n).startsWith("path")) {
						String reactionID = KeggGmlHelper.getKeggReactions(n).get(0).getValue();
						AttributeHelper.setLabel(n, reactionID.substring(reactionID.indexOf(":") + 1));
					}
				break;
			case 6:
			case 7:
				for (Node n : MainFrame.getInstance().getActiveSession().getGraph().getNodes()) {
					String label = AttributeHelper.getLabel(1, n, "");
					EnzymeEntry ee = EnzymeService.getEnzymeInformation(label, false);
					if (ee != null) {
						if (mode == 7)
							AttributeHelper.setLabel(n, ee.getDE());
						else
							AttributeHelper.setLabel(n, ee.getID());
					}
				}
				break;
			default:
				break;
		}
		
	}
	
	@Override
	public boolean visibleForView(View v) {
		return v == null || v instanceof GraphView;
	}
	
	public int getCurrentStatusValue() {
		return (int) statusValue;
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
		return "Pathways";
	}
	
	@Override
	public int getPreferredTabPosition() {
		return InspectorTab.TAB_LEADING;
	}
	
}
