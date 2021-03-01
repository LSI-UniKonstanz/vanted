/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.AttributeHelper;
import org.JMButton;
import org.OpenFileDialogService;
import org.graffiti.core.GenericFileFilter;
import org.graffiti.core.StringBundle;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.editor.actions.FileSaveAsAction;
import org.graffiti.event.AttributeEvent;
import org.graffiti.event.TransactionEvent;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.managers.IOManager;
import org.graffiti.managers.MyInputStreamCreator;
import org.graffiti.plugin.inspector.InspectorTab;
import org.graffiti.plugin.io.InputSerializer;
import org.graffiti.plugin.io.ParserException;
import org.graffiti.plugin.view.GraphView;
import org.graffiti.plugin.view.View;
import org.graffiti.selection.Selection;
import org.graffiti.session.EditorSession;
import org.graffiti.session.Session;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.naive_pattern_finder.NaivePatternFinderAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.naive_pattern_finder.PatternAttributeUtils;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.copy_pattern_layout.CopyPatternLayoutAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.graph_to_origin_mover.CenterLayouterAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.LogicConnection;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.NodeOrEdge;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.SearchDialog;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.SearchLogic;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.SearchOperation;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.SearchOption;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.SearchType;

/**
 * Represents the tab, which contains the functionality to edit the attributes
 * of the current graph object.
 * 
 * @version $Revision$
 */
public class TabPatternLayout extends InspectorTab {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6088920628006312093L;
	private javax.swing.JButton jButtonAddPattern;
	private javax.swing.JButton jButtonEdit;
	private javax.swing.JButton jButtonLoad;
	private javax.swing.JButton jButtonRemovePattern;
	private javax.swing.JButton jButtonSave;
	private javax.swing.JPanel jPanelButtons;
	
	private javax.swing.JButton jButtonPatternSearch;
	private javax.swing.JPanel jPanelPattern;
	
	static javax.swing.JTabbedPane jTabbedPane1;
	
	private JButton jButtonSearch;
	private JCheckBox jDirectedSearch;
	private JCheckBox jAllowOverlap;
	private JCheckBox jClearPrevSelection;
	private JButton jButtonCopyLayout;;
	
	private MainFrame mainframe;
	
	/** DOCUMENT ME! */
	private IOManager ioManager;
	
	/** DOCUMENT ME! */
	private StringBundle sBundle;
	
	private void initComponents() {
		
		jPanelButtons = new javax.swing.JPanel();
		
		jButtonAddPattern = new JMButton();
		jButtonEdit = new JMButton();
		jButtonLoad = new JMButton();
		jButtonRemovePattern = new JMButton();
		jButtonSave = new JMButton();
		jButtonPatternSearch = new JMButton();
		
		jButtonAddPattern.setOpaque(false);
		jButtonEdit.setOpaque(false);
		jButtonLoad.setOpaque(false);
		jButtonRemovePattern.setOpaque(false);
		jButtonSave.setOpaque(false);
		jButtonPatternSearch.setOpaque(false);
		
		jButtonSearch = new JMButton("<html><b>Perform Search");
		
		jDirectedSearch = new JCheckBox("directed");
		jDirectedSearch.setOpaque(false);
		jDirectedSearch.setSelected(true);
		
		jAllowOverlap = new JCheckBox("allow overlap");
		jAllowOverlap.setOpaque(false);
		jAllowOverlap.setSelected(false);
		
		jClearPrevSelection = new JCheckBox("clear previous selection");
		jClearPrevSelection.setOpaque(false);
		jClearPrevSelection.setSelected(false);
		
		jButtonCopyLayout = new JMButton("Apply Layout");
		
		jPanelPattern = new javax.swing.JPanel();
		jPanelPattern.setBackground(null);
		jPanelPattern.setOpaque(false);
		
		jTabbedPane1 = new javax.swing.JTabbedPane();
		
		jTabbedPane1.addChangeListener(new ChangeListener() {
			
			public void stateChanged(ChangeEvent arg0) {
				System.out.println("Tab changed state");
				
				List<?> sessions = GravistoService.getInstance().getPatternSessionList();
				
				GravistoService.getInstance().framesDeselect();
				
				MainFrame mf = GravistoService.getInstance().getMainFrame();
				if (jTabbedPane1.getSelectedIndex() >= 0) {
					mf.fireSessionChanged((Session) sessions.get(jTabbedPane1.getSelectedIndex()));
				} else {
					// mf.fireSessionChanged(null);
				}
			}
		});
		
		jPanelButtons.setLayout(new java.awt.GridLayout(6, 2));
		
		jButtonAddPattern.setText("Add Network");
		jButtonAddPattern.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonAddPatternActionPerformed(evt);
			}
		});
		
		jPanelButtons.add(jButtonAddPattern);
		
		// DefaultPluginEntry te =
		// GraffitiSingleton.getInstance().getPluginInstanceFromPluginDescription("Default
		// Editing Tools");
		// boolean ipkEditToolsLoaded =
		// te.getDescription().getAuthor().toUpperCase().indexOf("IPK")>=0;
		boolean ipkEditToolsLoaded = false;
		if (!ipkEditToolsLoaded) {
			jButtonEdit.setText("Edit Network");
			jButtonEdit.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					if (GravistoService.getInstance().isEditorFrameSelected()) {
						MainFrame.showMessage("You may now edit the Network", MessageType.INFO);
						List<?> sessions = GravistoService.getInstance().getPatternSessionList();
						
						GravistoService.getInstance().framesDeselect();
						
						MainFrame mf = GravistoService.getInstance().getMainFrame();
						if (jTabbedPane1.getSelectedIndex() >= 0) {
							mf.fireSessionChanged((Session) sessions.get(jTabbedPane1.getSelectedIndex()));
						} else {
							// mf.fireSessionChanged(null);
						}
					}
				}
			});
			
			jPanelButtons.add(jButtonEdit);
		}
		jPanelButtons.add(jButtonRemovePattern);
		
		jButtonSave.setText("Save Network");
		jButtonSave.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonSaveActionPerformed(evt);
			}
		});
		
		jPanelButtons.add(jButtonSave);
		
		jButtonLoad.setText("Load Network");
		jButtonLoad.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonLoadActionPerformed(evt);
			}
		});
		jPanelButtons.add(jButtonLoad);
		
		jButtonRemovePattern.setText("Remove Network");
		jButtonRemovePattern.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonRemovePatternActionPerformed(evt);
			}
		});
		
		jButtonCopyLayout = new JMButton("Apply Layout (right to left)");
		jButtonCopyLayout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Graph graph = null;
				try {
					graph = MainFrame.getInstance().getActiveEditorSession().getGraph();
				} catch (NullPointerException npe) {
					MainFrame.showMessageDialog("No active graph editor window found!", "Error");
				}
				if (graph != null)
					GravistoService.getInstance().runPlugin(new CopyPatternLayoutAlgorithm().getName(), graph, null);
			}
		});
		jPanelButtons.add(jButtonCopyLayout);
		
		/*
		 * apply layout should only be possible with non-overlapping subnetworks,
		 * because else there would be conflicts and the algorithm wouldn't work
		 * properly. So we disable this button, if overlap is enabled
		 */
		jAllowOverlap.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (jAllowOverlap.isSelected())
					jButtonCopyLayout.setEnabled(false);
				else
					jButtonCopyLayout.setEnabled(true);
			}
		});
		
		jButtonSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Graph graph = null;
				try {
					graph = MainFrame.getInstance().getActiveEditorSession().getGraph();
				} catch (NullPointerException npe) {
					MainFrame.showMessageDialog("No active graph editor window found!", "Error");
					return;
				}
				
				if (jClearPrevSelection.isSelected()) {
					MainFrame.getInstance().getActiveEditorSession().getSelectionModel().getActiveSelection().clear();
				}
				
				if (graph != null && graph.getGraphElements().size() > 0) {
					for (GraphElement ge : graph.getGraphElements()) {
						AttributeHelper.deleteAttribute(ge, PatternAttributeUtils.PATTERN_PATH,
								PatternAttributeUtils.PATTERN_RECORD_PREFIX + "*");
					}
				}
				NaivePatternFinderAlgorithm alg = new NaivePatternFinderAlgorithm();
				alg.setIgnoreEdgeDirection(!jDirectedSearch.isSelected());
				alg.setAllowOverlap(jAllowOverlap.isSelected());
				GravistoService.getInstance().runAlgorithm(alg, graph, new Selection("empty"), null);
				SearchOption[] so = new SearchOption[] { new SearchOption(LogicConnection.OR, NodeOrEdge.Nodes,
						".AGNW.PATTERN.PATTERN_1", "PATTERN_NAME", "", 0, 0.0, false, SearchType.searchString,
						SearchLogic.searchMatched, SearchOperation.include) };
				SearchDialog.doSearch(so);
			}
		});
		jPanelButtons.add(jButtonSearch);
		// jPanelButtons.add(TableLayout.getSplit(jButtonSearch, jDirectedSearch,
		// TableLayoutConstants.PREFERRED, TableLayoutConstants.FILL));
		
		jButtonPatternSearch.setText("Select Search-Result Nodes");
		jButtonPatternSearch.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				SearchOption[] so = new SearchOption[] { new SearchOption(LogicConnection.OR, NodeOrEdge.Nodes,
						".AGNW.PATTERN.PATTERN_1", "PATTERN_NAME", "", 0, 0.0, false, SearchType.searchString,
						SearchLogic.searchMatched, SearchOperation.include) };
				SearchDialog.doSearch(so);
			}
		});
		jPanelButtons.add(jButtonPatternSearch);
		
		jPanelButtons.add(jDirectedSearch);
		jPanelButtons.add(jAllowOverlap);
		jPanelButtons.add(jClearPrevSelection);
		
		jPanelPattern.setLayout(new java.awt.GridLayout(1, 1));
		jPanelPattern.add(jTabbedPane1);
		
		double border = 5;
		double size[][] = { { border, TableLayoutConstants.FILL, border }, // Columns
				{ border, TableLayoutConstants.PREFERRED, TableLayoutConstants.FILL, border } }; // Rows
		
		this.setBounds(10, 10, 100, 100);
		
		this.setLayout(new TableLayout(size));
		
		this.add(jPanelButtons, "1,1");
		this.add(jPanelPattern, "1,2");
		
		this.setBackground(null);
		this.setOpaque(false);
		
		validate();
	}
	
	void jButtonLoadActionPerformed(java.awt.event.ActionEvent evt) {
		// GravistoService gs = GravistoService.getInstance();
		// FileOpenAction oa =
		// new FileOpenAction(gs.getMainFrame());
		//
		// JScrollPane sp = oa.loadFile(evt); // null
		//
		// if (sp == null)
		// return;
		JFileChooser fc = ioManager.createOpenFileChooser();
		
		OpenFileDialogService.setActiveDirectoryFor(fc);
		
		int returnVal = fc.showDialog(MainFrame.getInstance(), sBundle.getString("menu.file.open"));
		
		OpenFileDialogService.setActiveDirectoryFrom(fc.getCurrentDirectory());
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			String fileName = file.getName();
			
			if (fileName.indexOf(".") == -1) {
				fileName = file.getName() + ((GenericFileFilter) fc.getFileFilter()).getExtension();
			}
			
			String ext = fileName.substring(fileName.lastIndexOf("."));
			
			try {
				
				try {
					MyInputStreamCreator ic = new MyInputStreamCreator(file);
					InputSerializer is = ioManager.createInputSerializer(ic.getNewInputStream(), ext);
					
					Graph g = null;
					g = is.read(ic.getNewInputStream());
					
					EditorSession es = new EditorSession(g);
					es.setFileName(file.getAbsolutePath());
					String[] fileTypeDescriptions = is.getFileTypeDescriptions();
					if (fileTypeDescriptions != null && fileTypeDescriptions.length > 0)
						es.setFileTypeDescription(fileTypeDescriptions[0]);
					JScrollPane sp = mainframe.showViewChooserDialog(es, true, evt);
					
					GravistoService.getInstance().addPatternSession(es);
					
					jTabbedPane1.add("Network " + Integer.valueOf(jTabbedPane1.getComponentCount() + 1).toString(), sp);
					
				} catch (ParserException pe) {
					showError(pe.getLocalizedMessage());
				} catch (IOException ioe) {
					showError(ioe.getLocalizedMessage());
				}
			} catch (IllegalAccessException iae) {
				iae.printStackTrace(System.err);
				showError(iae.getLocalizedMessage());
			} catch (InstantiationException ie) {
				ie.printStackTrace(System.err);
				showError(ie.getLocalizedMessage());
			}
		}
		
		// GraffitiView gf =
		// (GraffitiView) ((JViewport) sp.getComponent(0)).getComponent(0);
		//
		
		// // Search session which belongs to the view gf
		// Iterator<?> searchSessions =
		// gs.getMainFrame()
		// .getSessionsIterator();
		// while (searchSessions.hasNext()) {
		// Session sessionTemp = (Session) searchSessions.next();
		// List<?> tempViews = sessionTemp.getViews();
		//
		// for (int i = 0; i < tempViews.size(); i++) {
		// if (tempViews.get(i) == gf) {
		// GravistoService.getInstance().addPatternSession(
		// sessionTemp);
		// }
		// }
		// }
		/*
		 * ((JViewport) sp.getComponent(0)).getComponent(0).setBackground( new
		 * Color(230, 240, 230));
		 */
		
	}
	
	void jButtonSaveActionPerformed(java.awt.event.ActionEvent evt) {
		
		List<Session> sessionList = GravistoService.getInstance().getPatternSessionList();
		Session s = MainFrame.getInstance().getActiveSession();
		if (s == null) {
			MainFrame.showMessageDialog("No active editor session found!", "Error");
			return;
		}
		if (sessionList == null || !sessionList.contains(s)) {
			MainFrame.showMessageDialog(
					"<html>" + "Please activate at first the desired search graph to be saved!<br>"
							+ "Use the command <b>Edit Search-Graph</b> to activate the search-graph editor session.",
					"Error");
			return;
		}
		GravistoService gs = GravistoService.getInstance();
		FileSaveAsAction sa = new FileSaveAsAction(GravistoService.getInstance().getMainFrame(),
				gs.getMainFrame().getIoManager(), gs.getMainFrame().getSessionManager(), StringBundle.getInstance());
		sa.actionPerformed(evt);
	}
	
	void jButtonRemovePatternActionPerformed(java.awt.event.ActionEvent evt) {
		// removePattern
		
		List<?> sessions = GravistoService.getInstance().getPatternSessionList();
		
		if (sessions == null || sessions.isEmpty())
			return;
		if (jTabbedPane1.getComponentCount() > 0) {
			if (jTabbedPane1.getSelectedIndex() >= 0) {
				sessions.remove(jTabbedPane1.getSelectedIndex());
				jTabbedPane1.remove(jTabbedPane1.getSelectedIndex());
			} else {
				GravistoService.getInstance().getPatternSessionList().remove(sessions.get(0));
				sessions.remove(0);
				jTabbedPane1.remove(0);
			}
		}
	}
	
	/**
	 * Removes all pattern graph - sessions from the global pattern graph session
	 * list. Warning: All user loaded visible pattern sessions are removed as well.
	 */
	public static void removeHiddenPatterns() {
		// removePattern
		List<?> sessions = GravistoService.getInstance().getPatternSessionList();
		
		while (sessions.size() > 0) {
			sessions.remove(0);
		}
		
	}
	
	void jButtonAddPatternActionPerformed(java.awt.event.ActionEvent evt) {
		// Add your handling code here:
		// add Pattern
		
		final MainFrame mf = GravistoService.getInstance().getMainFrame();
		
		EditorSession newSession = new EditorSession();
		GravistoService.getInstance().addPatternSession(newSession);
		
		JScrollPane sp = mf.showViewChooserDialog(newSession, true, null);
		
		jTabbedPane1.add("Network " + Integer.valueOf(jTabbedPane1.getComponentCount() + 1).toString(), sp);
		jTabbedPane1.setSelectedIndex(jTabbedPane1.getComponentCount() - 1);
		GravistoService.getInstance().framesDeselect();
	}
	
	// public static void addActionListenerForPatternSearch(ActionListener l) {
	// if (jButtonPatternSearch!=null)
	// jButtonPatternSearch.addActionListener(l);
	// else
	// System.out.println("Info: Running non-GUI mode, pattern search command not
	// available.");
	// }
	
	public static void showPattern(Graph g) {
		final MainFrame mf = GravistoService.getInstance().getMainFrame();
		
		// ToDo: Chris: check plugin name
		GravistoService.getInstance().runPlugin(new CenterLayouterAlgorithm().getName(), g, null);
		
		EditorSession newSession = new EditorSession(g);
		GravistoService.getInstance().addPatternSession(newSession);
		
		JScrollPane sp = mf.showViewChooserDialog(newSession, true, null);
		
		jTabbedPane1.add("Network " + Integer.valueOf(jTabbedPane1.getComponentCount() + 1).toString(), sp);
	}
	
	/**
	 * * Constructs a <code>PatternTab</code> and sets the title.
	 */
	public TabPatternLayout() {
		super();
		this.title = "Search Subnetworks";
		this.mainframe = MainFrame.getInstance();
		this.ioManager = mainframe.getIoManager();
		this.sBundle = StringBundle.getInstance();
		
		initComponents();
	}
	
	public void postAttributeAdded(AttributeEvent e) {
	}
	
	public void postAttributeChanged(AttributeEvent e) {
	}
	
	public void postAttributeRemoved(AttributeEvent e) {
	}
	
	public void preAttributeAdded(AttributeEvent e) {
	}
	
	public void preAttributeChanged(AttributeEvent e) {
	}
	
	public void preAttributeRemoved(AttributeEvent e) {
	}
	
	public void transactionFinished(TransactionEvent e) {
	}
	
	public void transactionStarted(TransactionEvent e) {
	}
	
	/**
	 * For getting the current Pattern Graph.
	 * 
	 * @return Null, if no Pattern Graph is available. Current (user-selected)
	 *         Graph-Instance, if at least one pattern is loaded.
	 */
	public static Graph getCurrentPatternGraph() {
		if (GravistoService.getInstance().getPatternGraphs().isEmpty()) {
			return null;
		} else {
			return GravistoService.getInstance().getPatternGraphs().get(jTabbedPane1.getSelectedIndex());
		}
	}
	
	public void sessionChanged(Session s) {
	}
	
	public void sessionDataChanged(Session s) {
	}
	
	@Override
	public boolean visibleForView(View v) {
		List<Session> sessionList = GravistoService.getInstance().getPatternSessionList();
		if (sessionList != null && sessionList.size() > 0)
			return v == null || (v instanceof GraphView);
		else
			return v != null && (v instanceof GraphView);
	}
	
	@Override
	public String getTabParentPath() {
		return "Analysis.Network";
	}
	
	@Override
	public int getPreferredTabPosition() {
		return InspectorTab.TAB_TRAILING;
	}
	
	protected void showError(String msg) {
		JOptionPane.showMessageDialog(mainframe, msg, StringBundle.getInstance().getString("message.dialog.title"),
				JOptionPane.ERROR_MESSAGE);
	}
}
