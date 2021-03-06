// ==============================================================================
//
// StatusBar.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: StatusBar.java,v 1.23.2.1.2.2 2014/12/14 23:20:49 klapperipk Exp $

package org.graffiti.editor;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.AttributeHelper;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.graffiti.core.StringBundle;
import org.graffiti.event.GraphEvent;
import org.graffiti.event.GraphListener;
import org.graffiti.event.ListenerManager;
import org.graffiti.event.ListenerNotFoundException;
import org.graffiti.event.TransactionEvent;
import org.graffiti.graph.Edge;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.view.GraphElementComponent;
import org.graffiti.plugin.view.GraphView;
import org.graffiti.plugin.view.View;
import org.graffiti.plugin.view.ZoomListener;
import org.graffiti.plugin.view.Zoomable;
import org.graffiti.selection.Selection;
import org.graffiti.selection.SelectionEvent;
import org.graffiti.selection.SelectionListener;
import org.graffiti.session.EditorSession;
import org.graffiti.session.Session;
import org.graffiti.session.SessionListener;
import org.vanted.scaling.scalers.component.HTMLScaleSupport;

/**
 * Represents a status line ui component, which can display info and error
 * messages. It also let's the user scroll through the selected nodes and edges,
 * which will be zoomed into the view
 * 
 * @version $Revision: 1.23.2.1.2.2 $
 * @vanted.revision 2.7.0
 */
public class StatusBar extends JPanel implements SessionListener, SelectionListener, GraphListener {
	// ~ Static fields/initializers =============================================
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 219307340702068274L;
	
	static final Logger logger = Logger.getLogger(StatusBar.class);
	
	static {
		logger.setLevel(Level.ERROR);
	}
	
	/** The time, a message is displayed in the status line. */
	private static final int DELAY = 5000;
	
	// ~ Instance fields ========================================================
	
	/** The nodes- and edges label in the status bar. */
	private final JLabel edgesLabel;
	/** The buttons to focus on each selected edge */
	private final JButton bSelEdgePrev;
	private final JButton bSelEdgeNext;
	
	/** The nodes- and edges label in the status bar. */
	private final JLabel nodesLabel;
	
	/** The buttons to focus on each selected node */
	private final JButton bSelNodePrev;
	private final JButton bSelNodeNext;
	
	/** The ui component, which contains the status text. */
	JLabel statusLine;
	
	/** The current session, this status bar is listening to. */
	private Session currentSession;
	
	/** The number of edges. */
	private int edges;
	
	/** The number of nodes. */
	private int nodes;
	
	/**
	 * the current set of Nodes and Edges of the current active session (performance
	 * reasons)
	 */
	Set<Edge> setGraphEdges;
	Set<Node> setGraphNodes;
	
	private int ignoreUpdate = 0;
	
	private Selection activeSelection = null;
	
	/** current scroll index for nodes */
	private List<Node> scrollListNodes;
	/** current scroll index for nodes */
	private List<Edge> scrollListEdges;
	/** list of elements that will be zoomed into when scrolling */
	private Collection<GraphElement> elements = new ArrayList<GraphElement>();
	
	/** current selection index for scrolling nodes */
	private int idxScrollNodes;
	/** current selection index for scrolling edges */
	private int idxScrollEdges;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new status bar.
	 * 
	 * @param sBundle
	 *           DOCUMENT ME!
	 */
	public StatusBar(StringBundle sBundle) {
		super();
		
		nodes = 0;
		edges = 0;
		
		setLayout(new GridBagLayout());
		
		statusLine = new MyJLabel("");
		statusLine.setBorder(BorderFactory.createEtchedBorder());
		// statusLine.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		statusLine.setToolTipText(HTMLScaleSupport.scaleText("<html><small>Click or <b>use F2</b> to view full status text"));
		/*
		 * statusLine.setBorder(BorderFactory.createCompoundBorder(
		 * BorderFactory.createLoweredBevelBorder(), statusLine.getBorder()));
		 */
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.insets = new Insets(1, 1, 1, 1);
		
		add(statusLine, c);
		
		nodesLabel = new JLabel(" ");
		nodesLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		nodesLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				processRightClick(e, true);
			}
		});
		nodesLabel.setToolTipText(HTMLScaleSupport.scaleText(sBundle.getString("statusBar.nodes.tooltip")));
		nodesLabel.setBorder(BorderFactory.createEtchedBorder());
		/*
		 * nodesLabel.setBorder(BorderFactory.createCompoundBorder(
		 * BorderFactory.createLoweredBevelBorder(), nodesLabel.getBorder()));
		 */
		nodesLabel.setVerticalAlignment(SwingConstants.BOTTOM);
		
		/*
		 * add the scroll buttons left and right to the node label
		 */
		bSelNodePrev = new JButton("<");
		bSelNodePrev.setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 2));
		bSelNodePrev.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				processScrollButtonAction(arg0);
				
			}
		});
		bSelNodeNext = new JButton(">");
		bSelNodeNext.setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 2));
		bSelNodeNext.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				processScrollButtonAction(arg0);
				
			}
		});
		
		edgesLabel = new JLabel(" ");
		edgesLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		edgesLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				processRightClick(e, false);
			}
		});
		edgesLabel.setToolTipText(HTMLScaleSupport.scaleText(sBundle.getString("statusBar.edges.tooltip")));
		edgesLabel.setBorder(BorderFactory.createEtchedBorder());
		/*
		 * edgesLabel.setBorder(BorderFactory.createCompoundBorder(
		 * BorderFactory.createLoweredBevelBorder(), edgesLabel.getBorder()));
		 */
		edgesLabel.setVerticalAlignment(SwingConstants.BOTTOM);
		
		/*
		 * add the scroll buttons left and right to the edge label
		 */
		bSelEdgePrev = new JButton("<");
		bSelEdgePrev.setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 2));
		bSelEdgePrev.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				processScrollButtonAction(arg0);
				
			}
		});
		bSelEdgeNext = new JButton(">");
		bSelEdgeNext.setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 2));
		bSelEdgeNext.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				processScrollButtonAction(arg0);
				
			}
		});
		
		c.weightx = 0.0;
		
		c.gridx = 1;
		add(bSelNodePrev);
		c.gridx = 2;
		add(nodesLabel, c);
		c.gridx = 3;
		add(bSelNodeNext);
		bSelNodePrev.setVisible(false);
		nodesLabel.setVisible(false);
		bSelNodeNext.setVisible(false);
		
		c.gridx = 4;
		add(bSelEdgePrev);
		c.gridx = 5;
		add(edgesLabel, c);
		c.gridx = 6;
		add(bSelEdgeNext);
		bSelEdgePrev.setVisible(false);
		edgesLabel.setVisible(false);
		bSelEdgeNext.setVisible(false);
		
		c.gridx = 7;
		
		JLabel memLabel = GravistoService.getMemoryInfoLabel(true);
		memLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		// memLabel.setBorder(BorderFactory.createEtchedBorder());
		memLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		memLabel.setVerticalAlignment(SwingConstants.BOTTOM);
		add(memLabel, c);
		
		c.gridx = 8;
		JLabel space = new JLabel();
		space.setPreferredSize(new Dimension(15, 5));
		space.setMinimumSize(new Dimension(15, 5));
		if (AttributeHelper.macOSrunning())
			add(space, c);
		
		updateGraphInfo();
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Clears the current text of the status bar.
	 */
	public synchronized void clear() {
		statusLine.setText(" ");
		// setToolTipText(null);
	}
	
	@Override
	public void postEdgeAdded(GraphEvent e) {
		edges++;
		updateGraphInfo();
	}
	
	@Override
	public void postEdgeRemoved(GraphEvent e) {
		edges--;
		updateGraphInfo();
	}
	
	@Override
	public void postGraphCleared(GraphEvent e) {
		edges = 0;
		nodes = 0;
		activeSelection = null;
		updateGraphInfo();
	}
	
	@Override
	public void postNodeAdded(GraphEvent e) {
		nodes++;
		updateGraphInfo();
	}
	
	@Override
	public void postNodeRemoved(GraphEvent e) {
		nodes--;
		updateGraphInfo();
	}
	
	@Override
	public void preEdgeAdded(GraphEvent e) {
	}
	
	@Override
	public void preEdgeRemoved(GraphEvent e) {
	}
	
	@Override
	public void preGraphCleared(GraphEvent e) {
	}
	
	@Override
	public void preNodeAdded(GraphEvent e) {
	}
	
	@Override
	public void preNodeRemoved(GraphEvent e) {
	}
	
	@Override
	public void selectionChanged(SelectionEvent e) {
		logger.debug("selectionChanged");
		// activeSelection = e.getSelection();
		if (currentSession != null) {
			activeSelection = ((EditorSession) currentSession).getSelectionModel().getActiveSelection();
			updateGraphInfo();
		}
	}
	
	@Override
	public void selectionListChanged(SelectionEvent e) {
		logger.debug("selectionListChanged");
		// activeSelection = e.getSelection();
		if (currentSession != null) {
			activeSelection = ((EditorSession) currentSession).getSelectionModel().getActiveSelection();
			updateGraphInfo();
		}
	}
	
	@Override
	public void sessionChanged(Session session) {
		logger.debug("sessionChanged");
		ListenerManager lm = null;
		
		if (currentSession != null) {
			// remove the status bar from the graph listener list of the
			// old session ...
			if (currentSession.getGraph() != null) {
				lm = currentSession.getGraph().getListenerManager();
				
				try {
					if (lm != null)
						lm.removeGraphListener(this);
				} catch (ListenerNotFoundException lnfe) {
					ErrorMsg.addErrorMessage(lnfe);
				}
			}
		}
		
		// remember the new session
		currentSession = session;
		
		if (session != null) {
			lm = session.getGraph().getListenerManager();
			
			// and add the status bar to the listener list of the new session.
			if (lm != null)
				lm.addDelayedGraphListener(this);
			if (session instanceof EditorSession) {
				activeSelection = ((EditorSession) session).getSelectionModel().getActiveSelection();
				initScrolling(activeSelection);
			} else {
				activeSelection = null;
				initScrolling(activeSelection);
			}
			nodes = currentSession.getGraph().getNumberOfNodes();
			edges = currentSession.getGraph().getNumberOfEdges();
			
			// bSelNodePrev.setVisible(true);
			nodesLabel.setVisible(true);
			// bSelNodeNext.setVisible(true);
			edgesLabel.setVisible(true);
		} else {
			// bSelNodePrev.setVisible(false);
			nodesLabel.setVisible(false);
			// bSelNodeNext.setVisible(false);
			edgesLabel.setVisible(false);
			activeSelection = null;
		}
		
		updateGraphInfo();
	}
	
	@Override
	public void sessionDataChanged(Session s) {
		updateGraphInfo();
	}
	
	/**
	 * Shows the given error message in the status bar for <tt>DELAY</tt> seconds.
	 * 
	 * @param status
	 *           the message to display in the status bar.
	 */
	public synchronized void showError(String status) {
		showError(status, DELAY);
	}
	
	/**
	 * Shows the given error message in the status bar for the given interval.
	 * 
	 * @param status
	 *           the message to display in the status bar.
	 * @param timeMillis
	 *           DOCUMENT ME!
	 */
	public synchronized void showError(final String val, int timeMillis) {
		final String status;
		
		if (val == null)
			status = "";
		else
			status = val;
		Timer timer = new Timer(0, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (isShowing()) {
					// if (statusLine.getText() == null)
					// statusLine.setText("");
					// FIXED, CK: This avoids flickering
					if (status == null || statusLine == null || statusLine.getText().equals(status))
						clear();
				}
			}
		});
		
		statusLine.setForeground(Color.red);
		statusLine.setText(HTMLScaleSupport.scaleText(status));
		timer.setInitialDelay(timeMillis);
		timer.setRepeats(false);
		timer.start();
	}
	
	/**
	 * Shows the given message in the status bar for <tt>DELAY</tt> seconds.
	 * 
	 * @param message
	 *           the message to display in the status bar.
	 */
	public synchronized void showInfo(String message) {
		showInfo(message, DELAY);
	}
	
	/**
	 * Shows the given message in the status bar for the given interval.
	 * 
	 * @param message
	 *           the message to display in the status bar.
	 * @param timeMillis
	 *           DOCUMENT ME!
	 */
	public synchronized void showInfo(final String val, int timeMillis) {
		final String message;
		if (val == null)
			message = "";
		else
			message = val;
		Timer timer = new Timer(timeMillis, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (isShowing()) {
					// FIXED, CK: This avoids flickering
					if (statusLine != null && statusLine.getText() == null)
						statusLine.setText("");
					if (statusLine != null && statusLine.getText() != null && message != null
							&& statusLine.getText().equals(message))
						clear();
				}
			}
		});
		
		statusLine.setForeground(Color.black);
		statusLine.setText(HTMLScaleSupport.scaleText(message));
		timer.setInitialDelay(timeMillis);
		timer.setRepeats(false);
		timer.start();
	}
	
	@Override
	public void transactionFinished(TransactionEvent e, BackgroundTaskStatusProviderSupportingExternalCall status) {
		// ignoreUpdate--;
		ignoreUpdate = 0;
		
		if (currentSession != null) {
			nodes = currentSession.getGraph().getNumberOfNodes();
			edges = currentSession.getGraph().getNumberOfEdges();
			updateGraphInfo();
		}
	}
	
	@Override
	public void transactionStarted(TransactionEvent e) {
		ignoreUpdate++;
	}
	
	ThreadSafeOptions tso = new ThreadSafeOptions();
	
	/**
	 * Updates the graph information ui components.
	 */
	private void updateGraphInfo() {
		
		if (!SwingUtilities.isEventDispatchThread()) {
			if (!tso.getBval(0, false)) {
				tso.setBval(0, true);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						tso.setBval(0, false);
						updateGraphInfo();
					}
				});
			}
			return;
		}
		
		if (ignoreUpdate > 0) {
			// System.out.println("some transaction not yet finished");
			return;
		}
		
		if (activeSelection != null)
			logger.debug("Session: " + currentSession.getGraph().getName() + "SelectionHash: "
					+ activeSelection.hashCode() + " Number sel Nodes=" + activeSelection.getNodes().size()
					+ " , sel Edges=" + activeSelection.getEdges().size());
		
		/*
		 * in case someone deleted nodes in the graph without updating the selection
		 * object this will take care that the selection object is updated and the
		 * status display displays the correct numbers
		 */
		
		if (activeSelection != null && currentSession != null && currentSession instanceof EditorSession) {
			setGraphNodes = new HashSet<Node>(currentSession.getGraph().getNodes());
			setGraphEdges = new HashSet<Edge>(currentSession.getGraph().getEdges());
			ArrayList<GraphElement> listGEToRemoveFromActiveSelection = new ArrayList<>();
			for (GraphElement element : activeSelection.getNodes()) {
				if (element.getGraph() == null || !setGraphNodes.contains(element)) {
					listGEToRemoveFromActiveSelection.add(element);
				}
			}
			for (GraphElement element : activeSelection.getEdges()) {
				if (element.getGraph() == null || !setGraphEdges.contains(element)) {
					listGEToRemoveFromActiveSelection.add(element);
				}
			}
			
			for (GraphElement ge : listGEToRemoveFromActiveSelection) {
				activeSelection.remove(ge);
			}
			
		}
		
		initScrolling(activeSelection);
		
		String selInfo1 = "";
		String selInfo2 = "";
		String selInfoE1 = "";
		String selInfoE2 = "";
		String br = "<br>";
		if (activeSelection != null) {
			if (activeSelection.getNodes().size() > 0) {
				selInfo1 = activeSelection.getNodes().size() + "/";
				selInfo2 = "<br>selected";
				br = " ";
				if (activeSelection.getNodes().size() == nodes)
					selInfo1 = "all ";
			}
			if (activeSelection.getEdges().size() > 0) {
				selInfoE1 = activeSelection.getEdges().size() + "/";
				selInfoE2 = "<br>selected";
				br = " ";
				if (activeSelection.getEdges().size() == edges)
					selInfoE1 = "all ";
			}
		}
		String nodeText = "";
		String edgeText = "";
		if (nodes == 1)
			nodeText = "<html>" + selInfo1 + nodes + "<br><small>node" + selInfo2;
		else if (nodes == 0)
			nodeText = "<html><small><br>no nodes";
		else
			nodeText = "<html>" + selInfo1 + nodes + "<small>" + br + "nodes" + selInfo2;
		
		if (edges == 1)
			edgeText = "<html>" + selInfoE1 + edges + "<small>" + br + "edge" + selInfoE2;
		else if (edges == 0)
			edgeText = "<html><small><br>no edges";
		else
			edgeText = "<html>" + selInfoE1 + edges + "<small>" + br + "edges" + selInfoE2;
		nodeText = nodeText.replaceAll("all 1<br>", "1 ");
		nodeText = nodeText.replaceAll("all 1<small>", "1");
		nodeText = nodeText.replaceAll(" ", "&nbsp;");
		edgeText = edgeText.replaceAll("all 1<", "1<");
		edgeText = edgeText.replaceAll("&nbsp;", "&nbsp;");
		nodesLabel.setText(HTMLScaleSupport.scaleText(nodeText));
		edgesLabel.setText(HTMLScaleSupport.scaleText(edgeText));
	}
	
	private void processRightClick(MouseEvent e, final boolean processNodesTrue_otherwiseEdges) {
		if (true) { // SwingUtilities.isRightMouseButton(e) || SwingUtilities.isLeftMouseButton(e))
			// {
			JPopupMenu popup = new JPopupMenu();
			JMenuItem selAll = new JMenuItem("Select All");
			selAll.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (currentSession instanceof EditorSession) {
						Selection sel = new Selection("id");
						sel.addAll(((EditorSession) currentSession).getSelectionModel().getActiveSelection()
								.getElements());
						if (processNodesTrue_otherwiseEdges)
							sel.addAll(currentSession.getGraph().getNodes());
						else
							sel.addAll(currentSession.getGraph().getEdges());
						((EditorSession) currentSession).getSelectionModel().setActiveSelection(sel);
						// ((EditorSession)currentSession).getSelectionModel().selectionChanged();
					}
				}
			});
			popup.add(selAll);
			
			JMenuItem invert = new JMenuItem("Invert Selection");
			invert.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (currentSession instanceof EditorSession) {
						Selection sel = new Selection("id");
						List<GraphElement> activeSel = ((EditorSession) currentSession).getSelectionModel()
								.getActiveSelection().getElements();
						if (processNodesTrue_otherwiseEdges) {
							List<Node> nodes2 = currentSession.getGraph().getNodes();
							for (GraphElement ge : nodes2)
								if (!activeSel.contains(ge))
									sel.add(ge);
							sel.addAll(((EditorSession) currentSession).getSelectionModel().getActiveSelection()
									.getEdges());
						} else {
							Collection<Edge> edges2 = currentSession.getGraph().getEdges();
							for (GraphElement ge : edges2)
								if (!activeSel.contains(ge))
									sel.add(ge);
							sel.addAll(((EditorSession) currentSession).getSelectionModel().getActiveSelection()
									.getNodes());
						}
						((EditorSession) currentSession).getSelectionModel().setActiveSelection(sel);
						// ((EditorSession)currentSession).getSelectionModel().selectionChanged();
					}
				}
			});
			popup.add(invert);
			
			JMenuItem selClear = new JMenuItem("Clear Selection");
			selClear.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Selection sel = new Selection("id");
					if (currentSession instanceof EditorSession) {
						if (processNodesTrue_otherwiseEdges)
							sel.addAll(((EditorSession) currentSession).getSelectionModel().getActiveSelection()
									.getEdges());
						else
							sel.addAll(((EditorSession) currentSession).getSelectionModel().getActiveSelection()
									.getNodes());
						((EditorSession) currentSession).getSelectionModel().setActiveSelection(sel);
					}
					
				}
			});
			popup.add(selClear);
			popup.show(e.getComponent(), e.getX(), e.getY());
		}
	}
	
	public String getCurrentText() {
		String res = statusLine.getText();
		if (res != null)
			return res;
		else
			return "";
	}
	
	private void initScrolling(Selection activeSelection) {
		if (activeSelection != null) {
			idxScrollNodes = activeSelection.getNodes().size();
			idxScrollEdges = 0;
			scrollListNodes = new ArrayList<Node>(activeSelection.getNodes());
			scrollListEdges = new ArrayList<Edge>(activeSelection.getEdges());
		}
		if (activeSelection != null && activeSelection.getNodes().size() > 0) {
			bSelNodePrev.setVisible(true);
			bSelNodeNext.setVisible(true);
		} else {
			bSelNodePrev.setVisible(false);
			bSelNodeNext.setVisible(false);
		}
		if (activeSelection != null && activeSelection.getEdges().size() > 0) {
			bSelEdgePrev.setVisible(true);
			bSelEdgeNext.setVisible(true);
		} else {
			bSelEdgePrev.setVisible(false);
			bSelEdgeNext.setVisible(false);
		}
	}
	
	private void processScrollButtonAction(ActionEvent e) {
		elements.clear();
		
		if (e.getSource().equals(bSelNodeNext)) {
			idxScrollNodes++;
			if (idxScrollNodes >= activeSelection.getNodes().size())
				idxScrollNodes = 0;
			elements.add(scrollListNodes.get(idxScrollNodes));
		}
		if (e.getSource().equals(bSelNodePrev)) {
			idxScrollNodes--;
			if (idxScrollNodes < 0)
				idxScrollNodes = activeSelection.getNodes().size() - 1;
			elements.add(scrollListNodes.get(idxScrollNodes));
		}
		if (e.getSource().equals(bSelEdgeNext)) {
			idxScrollEdges++;
			if (idxScrollEdges == activeSelection.getEdges().size())
				idxScrollEdges = 0;
			elements.add(scrollListEdges.get(idxScrollEdges));
		}
		if (e.getSource().equals(bSelEdgePrev)) {
			idxScrollEdges--;
			if (idxScrollEdges < 0)
				idxScrollEdges = activeSelection.getEdges().size() - 1;
			elements.add(scrollListEdges.get(idxScrollEdges));
		}
		
		zoomView(e, MainFrame.getInstance().getActiveEditorSession().getActiveView(), elements, 5);
	}
	
	/*
	 * zoom algorithm to zoom into scroll list elements copied code from
	 * ZoomFitChangeComonent
	 */
	private void zoomView(final ActionEvent e, Zoomable myView, Collection<GraphElement> elements, int zoomIntoValue) {
		
		AffineTransform currentZoom = myView.getZoom();
		final ZoomListener zoomView = (ZoomListener) myView;
		View view = (View) myView;
		final JScrollPane scrollPane;
		Dimension sps;
		try {
			scrollPane = (JScrollPane) ((JComponent) zoomView).getParent().getParent();
			sps = scrollPane.getViewport().getSize();
		} catch (ClassCastException cce) {
			sps = ((JComponent) myView).getPreferredSize();
			return;
		}
		final Dimension scrollPaneSize = sps;
		
		// if (e.getSource().equals(jbZoomRegion) || e.getSource().equals(jbZoomOut) ||
		// e.getSource().equals(jbZoomIn)) {
		
		Rectangle currentViewRect = scrollPane.getViewport().getViewRect();
		Point a = currentViewRect.getLocation();
		Point b = new Point(a.x + currentViewRect.width, a.y + currentViewRect.height);
		try {
			currentZoom.inverseTransform(a, a);
			currentZoom.inverseTransform(b, b);
			currentViewRect = new Rectangle(a.x, a.y, b.x - a.x, b.y - a.y);
		} catch (NoninvertibleTransformException e1) {
			System.err.println(e1);
		}
		
		Rectangle targetViewRect;
		
		Rectangle selectionViewRect = getViewRectFromSelection(view, elements);
		if (selectionViewRect == null)
			return;
		targetViewRect = selectionViewRect;
		
		double selWidth = selectionViewRect.width;
		double selHeight = selectionViewRect.height;
		double selX = selectionViewRect.x;
		double selY = selectionViewRect.y;
		double viewWidth = view.getViewComponent().getWidth();
		double viewHeight = view.getViewComponent().getHeight();
		
		Point viewWidthHeight = new Point((int) viewWidth, (int) viewHeight);
		try {
			/*
			 * the size of the jcomponent view depends on the zoom value for the canvas so
			 * we have to inverse transform it to get the real size values
			 */
			currentZoom.inverseTransform(viewWidthHeight, viewWidthHeight);
		} catch (NoninvertibleTransformException e1) {
			e1.printStackTrace();
		}
		
		/*
		 * zoom to factor 1 (100%) if selection is smaller than "unzoomed" view
		 */
		if (selWidth < scrollPaneSize.width && selHeight < scrollPaneSize.height) {
			targetViewRect.x = (int) ((selX + selWidth / 2) - scrollPaneSize.width / 2);
			targetViewRect.y = (int) ((selY + selHeight / 2) - scrollPaneSize.height / 2);
			targetViewRect.width = (int) scrollPaneSize.width;
			targetViewRect.height = (int) scrollPaneSize.height;
		}
		targetViewRect = selectionViewRect;
		
		final double srcSmallestX = currentViewRect.getX();
		final double srcSmallestY = currentViewRect.getY();
		final double srcGreatestX = currentViewRect.getX() + currentViewRect.getWidth();
		final double srcGreatestY = currentViewRect.getY() + currentViewRect.getHeight();
		
		final double smallestX = targetViewRect.getX();
		final double smallestY = targetViewRect.getY();
		final double greatestX = targetViewRect.getX() + targetViewRect.getWidth();
		final double greatestY = targetViewRect.getY() + targetViewRect.getHeight();
		
		boolean smooth = true;
		if (!smooth) {
			setZoom(zoomView, scrollPane, scrollPaneSize, smallestX, smallestY, greatestX, greatestY);
		} else {
			int duration = 300;
			duration = duration / 2;
			long startTime = System.currentTimeMillis();
			double f = 0;
			final ThreadSafeOptions tso = new ThreadSafeOptions();
			tso.setBval(0, false);
			while (f < 1) {
				long currTime = System.currentTimeMillis();
				f = (currTime - startTime) / (double) duration;
				if (f > 1)
					f = 1;
				if (f < 0)
					f = 0;
				double tx1 = getScale(f, srcSmallestX, smallestX);
				double ty1 = getScale(f, srcSmallestY, smallestY);
				double tx2 = getScale(f, srcGreatestX, greatestX);
				double ty2 = getScale(f, srcGreatestY, greatestY);
				setZoom(zoomView, scrollPane, scrollPaneSize, tx1, ty1, tx2, ty2);
				scrollPane.paintImmediately(scrollPane.getVisibleRect());
				try {
					Thread.sleep(10);
				} catch (InterruptedException err) {
					ErrorMsg.addErrorMessage(err);
				}
			}
			setZoom(zoomView, scrollPane, scrollPaneSize, smallestX, smallestY, greatestX, greatestY);
		}
		// }
	}
	
	private double getScale(double x, double a, double b) {
		double f2 = 1.2d / (1 + Math.exp(-(x - 0.5) * 5d)) - 0.1;
		if (f2 < 0)
			f2 = 0;
		if (f2 > 1)
			f2 = 1;
		return a + (b - a) * f2;
	}
	
	private void setZoom(final ZoomListener zoomView, JScrollPane scrollPane, Dimension scrollPaneSize,
			double smallestX, double smallestY, double greatestX, double greatestY) {
		double zomedSizeX, zomedSizeY;
		zomedSizeX = scrollPaneSize.getWidth() / (greatestX - smallestX);
		zomedSizeY = scrollPaneSize.getHeight() / (greatestY - smallestY);
		final boolean xIsLimit = zomedSizeX < zomedSizeY;
		
		double borderPercent = 0; // 0.1;
		
		double zoomFaktorWanted = min2(zomedSizeX, zomedSizeY) * (1 - borderPercent);
		final double zoomFaktor = zoomFaktorWanted; // min2(zoomFaktorWanted, 5); // maximum 500% zoom!
		
		final AffineTransform at = new AffineTransform();
		at.setToScale(zoomFaktor, zoomFaktor);
		
		MainFrame.showMessage("Active Zoom: " + (int) (100d * at.getScaleX()) + "%", MessageType.INFO);
		
		final double middleX = (greatestX + smallestX) / 2;
		final double middleY = (greatestY + smallestY) / 2;
		final double gtX = greatestX;
		final double gtY = greatestY;
		final double smX = smallestX;
		final double smY = smallestY;
		final double bdP = borderPercent;
		
		final JScrollPane spf = scrollPane;
		final Dimension spsf = scrollPaneSize;
		
		zoomView.zoomChanged(at);
		if (xIsLimit) {
			double offX = (gtX - smX) * bdP / 2;
			spf.getHorizontalScrollBar().setValue((int) ((smX - offX) * at.getScaleX()));
			double targetY = middleY * zoomFaktor - spsf.getHeight() / 2;
			spf.getVerticalScrollBar().setValue((int) targetY);
		} else {
			double offY = (gtY - smY) * bdP / 2;
			spf.getVerticalScrollBar().setValue((int) ((smY - offY) * at.getScaleY()));
			double targetX = middleX * zoomFaktor - spsf.getWidth() / 2;
			spf.getHorizontalScrollBar().setValue((int) targetX);
		}
		
		/*
		 * SwingUtilities.invokeLater(new Runnable() { public void run() {
		 * zoomView.zoomChanged(at); if (xIsLimit) { double offX = (gtX - smX) * bdP /
		 * 2; spf.getHorizontalScrollBar().setValue( (int) ((smX - offX) *
		 * at.getScaleX())); double targetY = middleY * zoomFaktor - spsf.getHeight() /
		 * 2; spf.getVerticalScrollBar().setValue((int) targetY); } else { double offY =
		 * (gtY - smY) * bdP / 2; spf.getVerticalScrollBar().setValue( (int) ((smY -
		 * offY) * at.getScaleY())); double targetX = middleX * zoomFaktor -
		 * spsf.getWidth() / 2; spf.getHorizontalScrollBar().setValue((int) targetX); }
		 * } });
		 */
	}
	
	/**
	 * @param smallestX
	 *           Value 1
	 * @param cx
	 *           Value 2
	 * @return The smaller one of the parameters
	 */
	private double min2(double smallestX, double cx) {
		return smallestX < cx ? smallestX : cx;
	}
	
	private Rectangle getViewRectFromSelection(View view, Collection<GraphElement> elements) {
		Rectangle viewRect = null;
		for (GraphElement ge : elements) {
			if (view instanceof GraphView && ((GraphView) view).isHidden(ge))
				continue;
			
			GraphElementComponent gvc = view.getComponentForElement(ge);
			Rectangle r = null;
			boolean ra = view.redrawActive();
			if ((gvc == null || ra) && (ge instanceof Node)) {
				if (!AttributeHelper.isHiddenGraphElement(ge))
					r = AttributeHelper.getNodeRectangleAWT((Node) ge);
			} else if (!ra && gvc != null)
				r = gvc.getBounds();
			if (r == null)
				continue;
			if (viewRect == null)
				viewRect = r;
			else
				viewRect.add(r);
			// if (gvc != null)
			// try {
			// for (Object o : gvc.getAttributeComponents()) {
			// if (o instanceof JComponent) {
			// JComponent jc = (JComponent) o;
			// if (viewRect == null)
			// viewRect = jc.getBounds();
			// else
			// viewRect.add(jc.getBounds());
			// }
			// }
			// } catch (ConcurrentModificationException cc) {
			//
			// }
		}
		return viewRect;
	}
	
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
