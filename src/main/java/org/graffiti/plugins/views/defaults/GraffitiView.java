// ==============================================================================
//
// GraffitiView.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau,
// Copyright (c) 2003-2009 IPK-Gatersleben
//
// ==============================================================================
// $Id: GraffitiView.java,v 1.84.6.2 2014/12/08 06:41:18 klapperipk Exp $

package org.graffiti.plugins.views.defaults;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.AttributeHelper;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.ObjectRef;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.color.ColorUtil;
import org.graffiti.attributes.Attributable;
import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.attributes.SortedCollectionAttribute;
import org.graffiti.attributes.StringAttribute;
import org.graffiti.editor.AttributeComponentNotFoundException;
import org.graffiti.editor.GraffitiFrame;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.event.AttributeEvent;
import org.graffiti.event.AttributeListener;
import org.graffiti.event.EdgeEvent;
import org.graffiti.event.EdgeListener;
import org.graffiti.event.GraphEvent;
import org.graffiti.event.GraphListener;
import org.graffiti.event.NodeListener;
import org.graffiti.event.TransactionEvent;
import org.graffiti.event.TransactionListener;
import org.graffiti.graph.AdjListNode;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.graphics.CoordinateAttribute;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.managers.AttributeComponentManager;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.view.AbstractView;
import org.graffiti.plugin.view.AttributeComponent;
import org.graffiti.plugin.view.CoordinateSystem;
import org.graffiti.plugin.view.EdgeComponentInterface;
import org.graffiti.plugin.view.GraphElementComponent;
import org.graffiti.plugin.view.GraphView;
import org.graffiti.plugin.view.MessageListener;
import org.graffiti.plugin.view.ShapeNotFoundException;
import org.graffiti.plugin.view.View2D;
import org.graffiti.plugins.modes.defaults.MegaMoveTool;
import org.graffiti.session.EditorSession;
import org.graffiti.session.Session;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;

/**
 * An implementation of <code>org.graffiti.plugin.view.View2D</code>, that
 * displays a graph. Since it also shows changes in the graph it listens for
 * changes in the graph, attributes, nodes and edges.
 * 
 * @see javax.swing.JPanel
 * @see org.graffiti.plugin.view.View2D
 */
public class GraffitiView extends AbstractView implements View2D, GraphView, GraphListener, AttributeListener,
		NodeListener, EdgeListener, TransactionListener {
	
	private static final Logger logger = Logger.getLogger(GraffitiView.class);
	
	static {
		logger.setLevel(Level.INFO);
	}
	
	// ~ Instance fields ========================================================
	
	private static final long serialVersionUID = 3257849887318882097L;
	
	protected int maxNodeCnt = Integer.MAX_VALUE; // 5000
	
	/** Maps MouseListeners to their corresponding ZoomedMouseListeners */
	private final Map<MouseListener, ZoomedMouseListener> zoomedMouseListeners = new LinkedHashMap<MouseListener, ZoomedMouseListener>();
	
	/**
	 * Maps MouseMotionListeners to their corresponding ZoomedMouseMotionListeners
	 */
	private final Map<MouseMotionListener, ZoomedMouseMotionListener> zoomedMouseMotionListeners = new LinkedHashMap<MouseMotionListener, ZoomedMouseMotionListener>();
	
	private boolean blockAdjust = false;
	
	private boolean blockEdges = false;
	
	public boolean threadedRedraw = true;
	
	protected CoordinateSystem coordinateSystem = CoordinateSystem.XY;
	
	/**
	 * components can use this variable to check, if this view is currently
	 * finishing a transaction and thus can behave differently as ususal Currently
	 * it is used to selectively NOT update dependent components on each node
	 */
	public boolean isFinishingTransacation;
	
	Object redrawLock = new Object();
	
	public GraffitiView() {
		setLayout(null);
		isFinishingTransacation = false;
	}
	
	// ~ Methods ================================================================
	
	@Override
	public Component getComponentAt(int x, int y) {
		// return super.getComponentAt(x, y);
		// return super.getComponentAt((int) (x * ((Point2D) zoom).getX()),
		// (int) (y * ((Point2D) zoom).getY()));
		Point2D pt2d = zoom.transform(new Point(x, y), null);
		Point zoomedPoint = new Point((int) pt2d.getX(), (int) pt2d.getY());
		Component c = super.getComponentAt(zoomedPoint.x, zoomedPoint.y);
		while (!(c instanceof NodeComponent || c instanceof GraffitiView)) {
			c = c.getParent();
		}
		return c;
	}
	
	public Component getComponentOfAnyTypeAt(int x, int y) {
		// return super.getComponentAt(x, y);
		// return super.getComponentAt((int) (x * ((Point2D) zoom).getX()),
		// (int) (y * ((Point2D) zoom).getY()));
		Point2D pt2d = zoom.transform(new Point(x, y), null);
		Point zoomedPoint = new Point((int) pt2d.getX(), (int) pt2d.getY());
		Component c = super.getComponentAt(zoomedPoint.x, zoomedPoint.y);
		return c;
	}
	
	/**
	 * Sets the graph this view displays.
	 * 
	 * @param g
	 *           graph this view should display.
	 */
	@Override
	public void setGraph(Graph g) {
		currentGraph = g;
		if (g != null) {
			completeRedraw();
		} else {
			removeAll();
			validate();
			repaint();
		}
	}
	
	/**
	 * Disable edge component creation.
	 * 
	 * @param block
	 *           If true, no edges will be created upon next redraw.
	 */
	public void setBlockEdges(boolean block) {
		this.blockEdges = block;
	}
	
	public boolean getBlockEdges() {
		return this.blockEdges;
	}
	
	@Override
	public Graphics getGraphics() {
		Graphics2D sg = (Graphics2D) super.getGraphics();
		
		if (sg == null) {
			return null;
		} else {
			sg.transform(zoom);
			
			return sg;
		}
	}
	
	/**
	 * Adds a message listener to the view.
	 * 
	 * @param ml
	 *           a message listener
	 * @throws IllegalArgumentException
	 *            DOCUMENT ME!
	 */
	@Override
	public void addMessageListener(MessageListener ml) {
		if (ml == null)
			throw new IllegalArgumentException("The argument may not be null");
		
		this.messageListeners.add(ml);
	}
	
	@Override
	public void addMouseListener(MouseListener l) {
		ZoomedMouseListener zoomedListener = new ZoomedMouseListener(l);
		zoomedMouseListeners.put(l, zoomedListener);
		super.addMouseListener(zoomedListener);
	}
	
	@Override
	public void removeMouseListener(MouseListener listener) {
		ZoomedMouseListener removeListener = zoomedMouseListeners.get(listener);
		super.removeMouseListener(removeListener);
		zoomedMouseListeners.remove(listener);
	}
	
	@Override
	public void addMouseMotionListener(MouseMotionListener l) {
		ZoomedMouseMotionListener zoomedListener = new ZoomedMouseMotionListener(l);
		zoomedMouseMotionListeners.put(l, zoomedListener);
		super.addMouseMotionListener(zoomedListener);
	}
	
	@Override
	public void removeMouseMotionListener(MouseMotionListener l) {
		ZoomedMouseMotionListener removeListener = zoomedMouseMotionListeners.get(l);
		super.removeMouseMotionListener(removeListener);
		zoomedMouseMotionListeners.remove(l);
	}
	
	/**
	 * Closes the current view.
	 */
	@Override
	public void close() {
		super.close();
		removeAll();
		validate();
		zoomedMouseListeners.clear();
		zoomedMouseMotionListeners.clear();
		setVisible(false);
	}
	
	protected int getActiveTransactions() {
		return getGraph().getListenerManager().getNumTransactionsActive();
	}
	
	boolean redrawInProgress = false;
	
	protected DrawMode drawMode = DrawMode.NORMAL;
	
	protected boolean repaintFast = true;
	
	@Override
	public boolean redrawActive() {
		return redrawInProgress;
	}
	
	public void completeRedraw() {
		logger.debug("complete redraw issued for " + getGraph().getName());
		if (redrawInProgress)
			return;
		redrawInProgress = true;
		// setVisible(false);
		JComponent sp = (JComponent) ErrorMsg.findParentComponent(this, JInternalFrame.class);
		if (sp != null)
			sp.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		if (repaintFast && getParent() != null)
			getParent().setVisible(false);
		try {
			if (getActiveTransactions() > 0) {
				ErrorMsg.addErrorMessage("Recreation of view requested during run of transaction.");
				getGraph().getListenerManager().finishOpenTransactions();
			}
			
			long aaaa = System.currentTimeMillis();
			
			long nna = System.currentTimeMillis();
			clearGraphElementComponentMap();
			long nne = System.currentTimeMillis();
			if (nne == nna)
				nne += 1;
			
			optimizedGraphElementCreation(true, aaaa);
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		} catch (Error er) {
			ErrorMsg.addErrorMessage(er.getLocalizedMessage());
		}
		final Color c = AttributeHelper.getColorFromAttribute(currentGraph, "", "graphbackgroundcolor", Color.white);
		if (c != null)
			if (getParent() == null) {
				final ObjectRef or = new ObjectRef();
				final AncestorListener lll = new AncestorListener() {
					public void ancestorRemoved(AncestorEvent event) {
					}
					
					public void ancestorMoved(AncestorEvent event) {
					}
					
					public void ancestorAdded(AncestorEvent arg0) {
						getParent().setBackground(c);
						GraffitiView.this.removeAncestorListener((AncestorListener) or.getObject());
					}
				};
				or.setObject(lll);
				addAncestorListener(lll);
			} else
				getParent().setBackground(c);
			
	}
	
	private void optimizedGraphElementCreation(final boolean addShapesAndAttributeComponentsTogether,
			final long startTime) {
		logger.debug("optimizedGraphElementCreation for graph : " + getGraph().getName());
		long nna;
		long nne;
		nna = System.currentTimeMillis();
		for (Node n : currentGraph.getNodes()) {
			NodeComponent nc = createNodeComponent(getGraphElementComponentMap(), n);
			if (nc == null)
				continue;
			try {
				nc.createNewShape(coordinateSystem);
			} catch (ShapeNotFoundException e) {
				nc.createStandardShape();
				informMessageListener("statusbar.error.graphelement.ShapeNotFoundException", MessageType.ERROR);
			}
		}
		
		nne = System.currentTimeMillis();
		if (nne == nna)
			nne += 1;
		long eea = System.currentTimeMillis();
		for (Edge e : currentGraph.getEdges()) {
			EdgeComponent ec = createEdgeComponent(getGraphElementComponentMap(), e);
			if (ec == null)
				continue;
			try {
				ec.createNewShape(CoordinateSystem.XY);
			} catch (ShapeNotFoundException snf) {
				ec.createStandardShape();
				informMessageListener("statusbar.error.graphelement.ShapeNotFoundException", MessageType.ERROR);
			}
		}
		
		long eee = System.currentTimeMillis();
		if (eee == eea)
			eee += 1;
		// currentGraph.numberGraphElements();
		final GraphElement[] ges = getGraphElementComponentMap().keySet().toArray(new GraphElement[] {});
		long tttA = System.currentTimeMillis();
		sortGraphElements(ges);
		long tttB = System.currentTimeMillis();
		if (tttB == tttA)
			tttB += 1;
		// System.out.println("Z-Sort of graph element GUI components ("+ges.length+"):
		// T="+(tttB-tttA)+" ms. "+(int)speedAB+" elements per second.");
		if (getGraph() == null)
			return;
		
		/**
		 * If enabled, each opened graph would get "fresh" IDs...
		 */
		boolean numberNodes = false;
		
		if (numberNodes)
			getGraph().numberGraphElements();
		
		final BlockingQueue<JComponent> result = new LinkedBlockingQueue<JComponent>();
		// final Queue<JComponent> result = new LinkedList<JComponent>();
		
		final boolean isThreadedF = threadedRedraw; // getGraph().getNumberOfNodes()>100;
		
		final JComponent finishElement = new JLabel("Ende...");
		
		final Thread t = new Thread(new Runnable() {
			public void run() {
				if (getGraph() == null)
					return;
				
				// ListenerManager lm = getGraph().getListenerManager();
				try {
					// getGraph().setListenerManager(null);
					final long ttt = System.currentTimeMillis();
					final Map<GraphElement, GraphElementComponent> gecm = getComponentElementMap();
					if (!addShapesAndAttributeComponentsTogether) {
						for (GraphElement ge : ges) {
							GraphElementComponent gc = gecm.get(ge);
							result.add(gc);
						}
					}
					final int maxx = ges.length;
					// if more than one thread is used, the resulting components are currently not
					// created properly sorted
					// thus, until the ordering is preserved, only one thread can be used
					ExecutorService run = Executors.newFixedThreadPool(1, // Runtime.getRuntime().availableProcessors(),
							new ThreadFactory() {
								public Thread newThread(Runnable r) {
									Thread t = new Thread(r);
									t.setName("GEC creation thread for graph " + getGraph().getName(false));
									return t;
								}
							});
					ArrayList<GraphElement> work = new ArrayList<GraphElement>();
					GraphElement lastGe = null;
					if (ges.length > 0)
						lastGe = ges[ges.length - 1];
					final ThreadSafeOptions curr = new ThreadSafeOptions();
					curr.setInt(0);
					
					JComponent sp = (JComponent) ErrorMsg.findParentComponent(getViewComponent(), JInternalFrame.class);
					if (sp != null)
						sp.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					
					for (GraphElement ge : ges) {
						work.add(ge);
						
						if (work.size() > 100 || ge == lastGe || !threadedRedraw) {
							final Collection<GraphElement> workF = work;
							Runnable r = new Runnable() {
								public void run() {
									if (getGraph() == null)
										return;
									createGUIcomponentElements(addShapesAndAttributeComponentsTogether, workF, result,
											ttt, gecm);
									curr.addInt(workF.size());
									long ttt2 = System.currentTimeMillis();
									if (ttt2 == ttt)
										ttt2 += 1;
									MainFrame.showMessage(
											"Create view components... " + (int) (100d * curr.getInt() / maxx) + "%",
											MessageType.PERMANENT_INFO);// -
									// "+curr+"/"+maxx+"
									// ("+(int)speed+"
									// elements
									// per
									// second).",
									// MessageType.PERMANENT_INFO);
								}
							};
							if (threadedRedraw)
								run.execute(r);
							else
								r.run();
							work = new ArrayList<GraphElement>();
							
						}
					}
					try {
						run.shutdown();
						run.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
						JComponent sp2 = (JComponent) ErrorMsg.findParentComponent(getViewComponent(),
								JInternalFrame.class);
						if (sp2 != null)
							sp2.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					long ttt2 = System.currentTimeMillis();
					if (ttt2 == ttt)
						ttt2 += 1;
					final double speed = (double) ges.length / (double) (ttt2 - ttt) * 1000d;
					final long ttt2f = ttt2;
					final long tttf = ttt;
					try {
						if (isThreadedF)
							SwingUtilities.invokeAndWait(new Runnable() {
								public void run() {
									MainFrame.showMessage(
											"Creation of graph element attribute GUI components finished (" + ges.length
													+ "): T=" + (ttt2f - tttf) + " ms. " + (int) speed
													+ " elements per second. Add elements to view. Please wait.",
											MessageType.INFO);
									// System.out.println("Creation of graph element attribute GUI components
									// finished ("+ges.length+"): T="+(ttt2f-tttf)+" ms. "+(int)speed+" elements per
									// second. Add elements to view. Please wait.");
								}
							});
						else {
							MainFrame.showMessage(
									"Creation of graph element attribute GUI components finished (" + ges.length
											+ "): T=" + (ttt2f - tttf) + " ms. " + (int) speed
											+ " elements per second. Add elements to view. Please wait.",
									MessageType.INFO);
							// System.out.println("Creation of graph element attribute GUI components
							// finished ("+ges.length+"): T="+(ttt2f-tttf)+" ms. "+(int)speed+" elements per
							// second. Add elements to view. Please wait.");
						}
						
					} catch (Exception e) {
						// empty
					}
				} finally {
					result.add(finishElement);
					if (isThreadedF)
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								addElements(result, finishElement, startTime, isThreadedF);
							}
						});
				}
				MainFrame.showMessage("", MessageType.INFO);
			}
		});
		t.setName("Attribute GUI Component Creation");
		t.setPriority(Thread.NORM_PRIORITY);
		if (threadedRedraw)
			t.start();
		else
			t.run();
		if (!isThreadedF) {
			addElements(result, finishElement, startTime, isThreadedF);
		}
	}
	
	public ArrayList<GraphElement> getSortedGraphElements(boolean inverse) {
		final GraphElement[] ges = getGraphElementComponentMap().keySet().toArray(new GraphElement[] {});
		sortGraphElements(ges);
		ArrayList<GraphElement> res = new ArrayList<GraphElement>();
		for (GraphElement ge : ges)
			res.add(ge);
		if (inverse) {
			Collections.reverse(res);
		}
		return res;
	}
	
	public void sortGraphElements(final GraphElement[] ges) {
		Arrays.sort(ges, new Comparator<GraphElement>() {
			public int compare(GraphElement a, GraphElement b) {
				int res = a.compareTo(b);
				if (res != 0)
					return res;
				if ((a instanceof Node) && (b instanceof Node)) {
					GraphElementComponent gecA = getGraphElementComponentMap().get(a);
					GraphElementComponent gecB = getGraphElementComponentMap().get(b);
					if (gecA != null && gecB != null) {
						double areaA = gecA.getWidth() * gecA.getHeight();
						double areaB = gecB.getWidth() * gecB.getHeight();
						if (areaA < areaB)
							return 1;
						if (areaA > areaB)
							return -1;
					}
				}
				if ((a instanceof Node) && (b instanceof Edge)) {
					return -1;
				}
				if ((a instanceof Edge) && (b instanceof Node)) {
					return +1;
				}
				return 0;
			}
			
		});
	}
	
	private void addElements(final BlockingQueue<JComponent> result, final JComponent finishElement,
			final long startTime, boolean nonBlock) {
		// logger.debug("addElements for graph : " + getGraph().getName());
		JComponent sp = (JComponent) ErrorMsg.findParentComponent(this, JInternalFrame.class);
		if (sp != null)
			sp.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		setVisible(false);
		removeAll();
		while (!nonBlock || (nonBlock && result.size() > 0)) {
			JComponent jc;
			try {
				if (nonBlock)
					jc = result.poll();
				else
					jc = result.poll(Integer.MAX_VALUE, TimeUnit.SECONDS);
			} catch (InterruptedException e1) {
				break;
			}
			if (jc != null && jc != finishElement) {
				add(jc, 0);
				listCopyComponents.add(0, jc);
				
			}
			if (jc == finishElement)
				break;
		}
		
		synchronized (getTreeLock()) {
			if (isShowing())
				validate();
			else
				validateTree();
		}
		
		adjustPreferredSize(true);
		
		JComponent sp2 = (JComponent) ErrorMsg.findParentComponent(this, JInternalFrame.class);
		if (sp2 != null)
			sp2.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		setVisible(true);
		if (repaintFast && getParent() != null)
			getParent().setVisible(true);
		
		for (Session s : MainFrame.getSessions()) {
			if (!(s instanceof EditorSession))
				continue;
			EditorSession es = (EditorSession) s;
			if (s != null && es.getSelectionModel() != null && es.getGraph() == currentGraph)
				es.getSelectionModel().selectionChanged();
		}
		
		long bbbb = System.currentTimeMillis();
		if (bbbb == startTime)
			bbbb += 1;
		// System.out.println("Overall recreation time : T="+(bbbb-startTime)+" ms.");
		redrawInProgress = false;
		
	}
	
	/**
	 * @see java.awt.Container#findComponentAt(int, int)
	 */
	@Override
	public Component findComponentAt(int x, int y) {
		Component c = myFindComponent(x, y);
		while (c != null && (c.getParent() != null) && (!(c instanceof AttributeComponent || c instanceof NodeComponent
				|| c instanceof GraffitiView || c instanceof EdgeComponent))) {
			c = c.getParent();
		}
		return c;
	}
	
	private Component myFindComponent(int x, int y) {
		Component result = this;
		
		for (int i = 0; i < getComponentCount(); i++) {
			Component c = getComponent(i);
			if (c.isVisible()) {
				if (c instanceof NodeComponent)
					if ((c.getWidth() < 50 && c.getHeight() < 50 && x >= c.getX()) && (y >= c.getY())
							&& (x <= (c.getX() + c.getWidth())) && (y <= (c.getY() + c.getHeight()))) {
						result = c;
						break;
					}
			}
		}
		if (result == this)
			for (int i = 0; i < getComponentCount(); i++) {
				Component c = getComponent(i);
				if (c.isVisible()) {
					if (c instanceof EdgeComponentInterface) {
						EdgeComponent ec = (EdgeComponent) c;
						if ((x >= c.getX()) && (y >= c.getY()) && (x <= (c.getX() + c.getWidth()))
								&& (y <= (c.getY() + c.getHeight()))) {
							if (ec.getShape().contains(x - c.getX(), y - c.getY())) {
								result = c;
								break;
							}
							boolean bendHit = false;
							Point2D coord;
							CoordinateAttribute coordAttr;
							Edge edge = (Edge) ec.getGraphElement();
							SortedCollectionAttribute bendsColl = (SortedCollectionAttribute) edge
									.getAttribute(GraphicAttributeConstants.BENDS_PATH);
							Collection<?> bends = bendsColl.getCollection().values();
							for (Iterator<?> it = bends.iterator(); it.hasNext();) {
								coordAttr = (CoordinateAttribute) it.next();
								coord = coordAttr.getCoordinate();
								
								if (MegaMoveTool.hit(new Point(x, y), coord)) {
									bendHit = true;
									break;
								}
							}
							if (bendHit) {
								result = c;
								break;
							}
						}
					} else {
						if ((x >= c.getX()) && (y >= c.getY()) && (x <= (c.getX() + c.getWidth()))
								&& (y <= (c.getY() + c.getHeight()))) {
							result = c;
							break;
						}
					}
				}
			}
		return result;
	}
	
	/**
	 * Called after an attribute has been added.
	 * 
	 * @param e
	 *           the AttributeEvent detailing the changes.
	 */
	@Override
	public void postAttributeAdded(AttributeEvent e) {
		Attribute attr = e.getAttribute();
		
		GraphElement ge = null;
		
		try {
			ge = (GraphElement) attr.getAttributable();
		} catch (ClassCastException cce) {
			// added an attribute to the graph
			// since view does not display any attribute of graphs, nothing
			// needs to be done
			// -> new development: shows the backgroundcolor of the graph
			Attributable a = attr.getAttributable();
			if (getParent() != null && a instanceof Graph && attr.getName().equals("graphbackgroundcolor")
					&& attr instanceof StringAttribute) {
				Color c = ColorUtil.getColorFromHex((String) attr.getValue());
				getParent().setBackground(c);
			}
			return;
		}
		
		GraphElementComponent gec = getGraphElementComponent(ge);
		if (gec != null) {
			recurseAttributes(attr, gec);
			try {
				gec.attributeChanged(attr);
				// gec.invalidate();
				// gec.validate();
			} catch (ShapeNotFoundException snfe) {
				informMessageListener("statusbar.error.attribute.ShapeNotFoundException", MessageType.ERROR);
			}
		}
		repaint();
		getGraph().setModified(true);
	}
	
	/**
	 * Called after an attribute has been changed.
	 * 
	 * @param e
	 *           the AttributeEvent detailing the changes.
	 */
	@Override
	public void postAttributeChanged(AttributeEvent e) {
		if (getActiveTransactions() > 0 && !isFinishingTransacation)
			return;
		
		Attribute attr = e.getAttribute();
		if (attr.getAttributable() != null && attr.getAttributable() instanceof Graph)
			attributeChanged(attr);
		try {
			GraphElementComponent gec = null;
			if (attr.getAttributable() instanceof GraphElement)
				gec = getGraphElementComponent((GraphElement) attr.getAttributable());
			if (gec != null) {
				gec.attributeChanged(attr);
				gec.repaint();
			}
		} catch (ShapeNotFoundException snfe) {
			informMessageListener("statusbar.error.attribute.ShapeNotFoundException", MessageType.ERROR);
		}
		repaint();
		if (getGraph() != null)
			getGraph().setModified(true);
	}
	
	/**
	 * Called after an attribute has been removed.
	 * 
	 * @param e
	 *           the AttributeEvent detailing the changes.
	 */
	@Override
	public void postAttributeRemoved(AttributeEvent e) {
		Attribute attr = e.getAttribute();
		Attributable attributable = attr.getAttributable();
		AttributeComponent ac = null;
		
		if (!(attributable instanceof GraphElement))
			return;
		GraphElementComponent gec = getGraphElementComponent((GraphElement) attributable);
		if (gec == null)
			return;
		ac = gec.getAttributeComponent(attr);
		
		if (attr.getAttributable() instanceof Graph && attr.getName().equals("graphbackgroundcolor")
				&& attr instanceof StringAttribute) {
			attr.getParent().remove(attr);
			setBackground(Color.white);
		}
		
		gec.removeAttributeComponent(attr);
		
		try {
			gec.attributeChanged(attr.getParent());
			gec.invalidate();
		} catch (ShapeNotFoundException snfe) {
			informMessageListener("statusbar.error.attribute.ShapeNotFoundException", MessageType.ERROR);
		}
		
		if (ac != null) {
			this.remove(ac);
			listCopyComponents.remove(ac);
			this.revalidate();
		}
		
		repaint();
		if (getGraph() != null)
			getGraph().setModified(true);
	}
	
	/**
	 * Called after the edge was set directed or undirected.
	 * 
	 * @param e
	 *           the EdgeEvent detailing the changes.
	 */
	@Override
	public void postDirectedChanged(EdgeEvent e) {
		if (getActiveTransactions() > 0 && !isFinishingTransacation)
			return;
		
		Edge edge = e.getEdge();
		EdgeComponent ec = (EdgeComponent) getGraphElementComponent(edge);
		ec.updateShape();
		
		repaint();
		if (getGraph() != null)
			getGraph().setModified(true);
	}
	
	/**
	 * Called after an edge has been added to the graph.
	 * 
	 * @param e
	 *           the GraphEvent detailing the changes.
	 */
	@Override
	public void postEdgeAdded(GraphEvent e) {
		if (getActiveTransactions() > 0 && !isFinishingTransacation)
			return;
		
		Edge edge = e.getEdge();
		
		if (isHidden(edge))
			return;
		
		EdgeComponent component = createEdgeComponent(getGraphElementComponentMap(), edge);
		if (component == null)
			return;
		// graphElementComponents.put(edge, component);
		try {
			component.createNewShape(CoordinateSystem.XY);
		} catch (ShapeNotFoundException snfe) {
			component.createStandardShape();
			informMessageListener("statusbar.error.graphelement.ShapeNotFoundException", MessageType.ERROR);
		}
		
		// Node node1 = edge.getSource();
		// NodeComponent nc = (NodeComponent) graphElementComponents.get(node1);
		// nc.addDependentComponent(component);
		// Node node2 = edge.getTarget();
		// nc = (NodeComponent) graphElementComponents.get(node2);
		// nc.addDependentComponent(component);
		// addLabelComponent(edge, component, 0);
		add(component, 0);
		listCopyComponents.add(0, component);
		addAttributeComponents(edge, component);
		
		// validate();
		repaint(edge);
		if (getGraph() != null)
			getGraph().setModified(true);
	}
	
	/**
	 * Called after an edge has been removed from the graph.
	 * 
	 * @param e
	 *           the GraphEvent detailing the changes.
	 */
	@Override
	public void postEdgeRemoved(GraphEvent e) {
		if (getActiveTransactions() > 0 && !isFinishingTransacation)
			return;
		
		Edge edge = e.getEdge();
		processEdgeRemoval(edge);
		repaint(edge);
		if (getGraph() != null)
			getGraph().setModified(true);
	}
	
	protected void processEdgeRemoval(Edge edge) {
		EdgeComponent ec = (EdgeComponent) getGraphElementComponent(edge);
		
		NodeComponent dependentNode;
		dependentNode = (NodeComponent) getGraphElementComponent(edge.getSource());
		if (dependentNode != null)
			dependentNode.removeDependentComponent(ec);
		dependentNode = (NodeComponent) getGraphElementComponent(edge.getTarget());
		if (dependentNode != null)
			dependentNode.removeDependentComponent(ec);
		removeGraphElementComponent(edge);
		if (ec != null) {
			for (Iterator<?> it = ec.getAttributeComponentIterator(); it.hasNext();) {
				Object next = it.next();
				remove((Component) next);
				listCopyComponents.remove((Component) next);
			}
			ec.clearAttributeComponentList();
			
			remove(ec);
			listCopyComponents.remove(ec);
			// validate();
			
			// adjustPreferredSize();
			repaintGraphElementComponent(ec);
		}
		if (getGraph() != null)
			getGraph().setModified(true);
	}
	
	/**
	 * Called after the edge has been reversed.
	 * 
	 * @param e
	 *           the EdgeEvent detailing the changes.
	 */
	@Override
	public void postEdgeReversed(EdgeEvent e) {
		if (getActiveTransactions() > 0 && !isFinishingTransacation)
			return;
		
		Edge edge = e.getEdge();
		EdgeComponent ec = (EdgeComponent) getGraphElementComponent(edge);
		
		ec.updateShape();
		if (getGraph() != null)
			getGraph().setModified(true);
	}
	
	/**
	 * Called after method <code>clear()</code> has been called on a graph. No other
	 * events (like remove events) are generated.
	 * 
	 * @param e
	 *           the GraphEvent detailing the changes.
	 */
	@Override
	public void postGraphCleared(GraphEvent e) {
		if (getActiveTransactions() > 0 && !isFinishingTransacation)
			return;
		
		clearGraphElementComponentMap();
		removeAll();
		validate();
		if (getGraph() != null)
			getGraph().setModified(true);
	}
	
	/**
	 * Called after an edge has been added to the graph.
	 * 
	 * @param e
	 *           the GraphEvent detailing the changes.
	 */
	@Override
	public void postNodeAdded(GraphEvent e) {
		if (getActiveTransactions() > 0 && !isFinishingTransacation)
			return;
		
		Node node = e.getNode();
		
		if (isHidden(node))
			return;
		
		NodeComponent component = createNodeComponent(getGraphElementComponentMap(), node);
		
		try {
			component.createNewShape(coordinateSystem);
		} catch (ShapeNotFoundException snfe) {
			component.createStandardShape();
			informMessageListener("statusbar.error.graphelement.ShapeNotFoundException", MessageType.ERROR);
		}
		
		add(component, 0);
		listCopyComponents.add(0, component);
		addAttributeComponents(node, component);
		
		// validate();
		repaint(node);
		if (getGraph() != null)
			getGraph().setModified(true);
	}
	
	/**
	 * Called after a node has been removed from the graph. All edges incident to
	 * this node have already been removed (preEdgeRemoved and postEdgeRemoved have
	 * been called).
	 * 
	 * @param e
	 *           the GraphEvent detailing the changes.
	 */
	@Override
	public void postNodeRemoved(GraphEvent e) {
		if (getActiveTransactions() > 0 && !isFinishingTransacation)
			return;
		
		Node node = e.getNode();
		
		processNodeRemoval(node);
		// if (getActiveTransactions() <= 0) {
		// adjustPreferredSize();
		// repaint();
		// }
		repaint(node);
		if (getGraph() != null)
			getGraph().setModified(true);
	}
	
	protected void processNodeRemoval(Node node) {
		
		NodeComponent nc = (NodeComponent) getGraphElementComponent(node);
		
		// remove attributeComponents (like label)
		if (nc != null) {
			for (Iterator<?> it = nc.getAttributeComponentIterator(); it.hasNext();) {
				Object next = it.next();
				remove((Component) next);
				
				listCopyComponents.remove((Component) next);
			}
			
			nc.clearAttributeComponentList();
			remove(nc);
			listCopyComponents.remove(nc);
			
		}
		removeGraphElementComponent(node);
		
		repaintGraphElementComponent(nc);
	}
	
	@Override
	public void remove(Component comp) {
		super.remove(comp);
		if (comp instanceof MouseListener) {
			removeMouseListener((MouseListener) comp);
		}
	}
	
	/**
	 * Called after the source node of an edge has changed.
	 * 
	 * @param e
	 *           the EdgeEvent detailing the changes.
	 */
	@Override
	public void postSourceNodeChanged(EdgeEvent e) {
		if (getActiveTransactions() > 0 && !isFinishingTransacation)
			return;
		
		Edge edge = e.getEdge();
		Node newSource = edge.getSource();
		EdgeComponent ec = (EdgeComponent) getGraphElementComponent(edge);
		NodeComponent nc = (NodeComponent) getGraphElementComponent(newSource);
		ec.setSourceComponent(nc);
		nc.addDependentComponent(ec);
		
		ec.updateShape();
		if (getGraph() != null)
			getGraph().setModified(true);
	}
	
	/**
	 * Called after the target node of an edge has changed.
	 * 
	 * @param e
	 *           the EdgeEvent detailing the changes.
	 */
	@Override
	public void postTargetNodeChanged(EdgeEvent e) {
		if (getActiveTransactions() > 0 && !isFinishingTransacation)
			return;
		
		Edge edge = e.getEdge();
		Node newTarget = edge.getTarget();
		EdgeComponent ec = (EdgeComponent) getGraphElementComponent(edge);
		NodeComponent nc = (NodeComponent) getGraphElementComponent(newTarget);
		ec.setTargetComponent(nc);
		nc.addDependentComponent(ec);
		
		ec.updateShape();
		if (getGraph() != null)
			getGraph().setModified(true);
	}
	
	/**
	 * Called before a change of the source node of an edge takes place.
	 * 
	 * @param e
	 *           the EdgeEvent detailing the changes.
	 */
	@Override
	public void preSourceNodeChanged(EdgeEvent e) {
		if (getActiveTransactions() > 0 && !isFinishingTransacation)
			return;
		
		Edge edge = e.getEdge();
		EdgeComponent ec = (EdgeComponent) getGraphElementComponent(edge);
		Node node = edge.getSource();
		NodeComponent nc = (NodeComponent) getGraphElementComponent(node);
		nc.removeDependentComponent(ec);
	}
	
	/**
	 * Called before a change of the target node of an edge takes place.
	 * 
	 * @param e
	 *           the EdgeEvent detailing the changes.
	 */
	@Override
	public void preTargetNodeChanged(EdgeEvent e) {
		if (getActiveTransactions() > 0 && !isFinishingTransacation)
			return;
		
		Edge edge = e.getEdge();
		EdgeComponent ec = (EdgeComponent) getGraphElementComponent(edge);
		Node node = edge.getTarget();
		NodeComponent nc = (NodeComponent) getGraphElementComponent(node);
		nc.removeDependentComponent(ec);
	}
	
	/**
	 * Removes a message listener from the view.
	 * 
	 * @param ml
	 *           a message listener
	 * @throws IllegalArgumentException
	 *            DOCUMENT ME!
	 */
	@Override
	public void removeMessageListener(MessageListener ml) {
		if (ml == null)
			throw new IllegalArgumentException("The argument may not be null");
		
		this.messageListeners.remove(ml);
	}
	
	/**
	 * Called when a transaction has stopped.
	 * 
	 * @param event
	 *           the EdgeEvent detailing the changes.
	 */
	@Override
	public void transactionFinished(TransactionEvent event, BackgroundTaskStatusProviderSupportingExternalCall status) {
		final TransactionEvent fevent = event;
		final BackgroundTaskStatusProviderSupportingExternalCall fstatus = status;
		if (!SwingUtilities.isEventDispatchThread()) {
			try {
				synchronized (redrawLock) {
					SwingUtilities.invokeAndWait(new Runnable() {
						
						@Override
						public void run() {
							logger.debug("calling transactionFinishedOnSwingThread() from NON-Event dispatch thread");
							transactionFinishedOnSwingThread(fevent, fstatus);
						}
					});
				}
			} catch (InvocationTargetException | InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			transactionFinishedOnSwingThread(fevent, fstatus);
		}
	}
	
	private void transactionFinishedOnSwingThread(TransactionEvent event,
			BackgroundTaskStatusProviderSupportingExternalCall status) {
		// logger.setLevel(Level.DEBUG);
		long startTimeTransFinished = System.currentTimeMillis();
		isFinishingTransacation = true;
		
		logger.debug("transactionFinishedOnSwingThread() --------------- EVENT DISPATCH THREAD? "
				+ SwingUtilities.isEventDispatchThread());
		
		// logger.debug("view contains " + getComponentCount() + " components");
		
		blockAdjust = true;
		
		Set<GraphElementComponent> setDependendComponents = new HashSet<>();
		
		Set<GraphElement> setPostAddedNodes = new HashSet<>();
		// must add edges AFTER nodes ...
		Set<Edge> edgesToAdd = new HashSet<Edge>();
		
		Collection<Object> changed = event != null ? new ArrayList<Object>(event.getChangedObjects().values())
				: new ArrayList<Object>();
		
		String s1 = null, s2 = null;
		if (status != null)
			s1 = status.getCurrentStatusMessage1();
		if (status != null)
			s2 = status.getCurrentStatusMessage2();
		double s3 = Double.NaN;
		if (status != null)
			s3 = status.getCurrentStatusValueFine();
		if (status != null)
			status.setCurrentStatusText1("View-Update");
		int idx = 0;
		int maxIdx = changed.size();
		boolean requestCompleteRedraw = false;
		logger.debug("changing " + changed.size() + " objects");
		long time1;
		
		time1 = System.currentTimeMillis();
		Iterator<Object> iterator = changed.iterator();
		
		while (iterator.hasNext()) {
			Object obj = iterator.next();
			if (status != null)
				if (status.wantsToStop())
					break;
			idx++;
			if (status != null)
				status.setCurrentStatusText2("Processing change " + idx + "/" + maxIdx + "...");
			if (status != null)
				status.setCurrentStatusValueFine(100d * idx / maxIdx);
			
			Attributable atbl = null;
			// System.out.println("Changed: "+obj);
			if (obj instanceof Attributable) {
				// if the object is an Attributable, use it ...
				atbl = (Attributable) obj;
			} else {
				if (obj instanceof AttributeEvent) {
					atbl = ((AttributeEvent) obj).getAttribute().getAttributable();
					obj = ((AttributeEvent) obj).getAttribute();
				} else {
					if (obj instanceof Attribute)
						atbl = ((Attribute) obj).getAttributable();
					else
						continue;
				}
			}
			
			if (atbl instanceof Graph) {
				if (obj instanceof Attribute) {
					if (((Attribute) obj).getName().equals("background_coloring")
							|| ((Attribute) obj).getName().equals("graphbackgroundcolor")) {
						attributeChanged((Attribute) obj);
						// we don't do anything if we match one of the attributes
					}
				} else if (obj instanceof Graph) {
					// information not helpful
					blockAdjust = false;
					completeRedraw();
					return; // completeRedraw should be complete?!
				}
				
			} else {
				
				try {
					if (atbl instanceof GraphElement) {
						if (((GraphElement) atbl).getGraph() == null) {
							// graph element has been DELETED
							if (atbl instanceof Node) {
								postNodeRemoved(new GraphEvent((Node) atbl));
							} else if (atbl instanceof Edge) {
								postEdgeRemoved(new GraphEvent((Edge) atbl));
							}
						} else {
							// graph element has been CHANGED
							GraphElementComponent gec = getGraphElementComponent((GraphElement) atbl);
							if (gec != null) {
								// (this will change dependent components, too
								// e.g. edges if a node has changed)
								boolean process = true;
								String id = null;
								if (obj instanceof Attribute) {
									if (((Attribute) obj).isDeleted()) {
										postAttributeRemoved(new AttributeEvent((Attribute) obj));
									} else {
										id = ((Attribute) obj).getId();
										if (id != null && id.equals("tooltip"))
											process = false;
									}
								} else if (obj instanceof AdjListNode) {
									// if a mapping occured then the whole node is part of the transaction
									// and calling this method is just a shortcut and will take care
									// of creating all necessary attribute components for this node
									recurseAttributes(((GraphElement) atbl).getAttributes(), gec);
									gec.attributeChanged(atbl.getAttribute(""));
									
								}
								if (process) {
									if (obj instanceof Attribute) {
										if (id != null && (id.equals("width") || id.equals("frameThickness"))) {
											checkHiddenStatus((GraphElement) atbl);
											gec = getGraphElementComponent((GraphElement) atbl);
										} /*
											 * else System.out.println("Attribute Change: "+id);
											 */
									}
									if (gec != null) {
										// if (obj instanceof Attribute && ae.getEventType() == EVENTTYPE.DELETED )
										// postAttributeRemoved(ae);
										if (obj instanceof Attribute)
											gec.attributeChanged((Attribute) obj);
										else {
											gec.attributeChanged(atbl.getAttribute("graphics"));
											// gec.attributeChanged(atbl.getAttribute(""));
										}
										
										if (gec instanceof AbstractGraphElementComponent) {
											setDependendComponents.addAll(((AbstractGraphElementComponent) gec)
													.getDependentGraphElementComponents());
										}
									}
								}
							} else {
								// graph element has been ADDED
								if (atbl instanceof Node) {
									setPostAddedNodes.add((Node) atbl);
									// postNodeAdded(new GraphEvent((Node) atbl));
								} else {
									if (atbl instanceof Edge) {
										edgesToAdd.add((Edge) atbl);
									}
								}
							}
						}
					} else {
						blockAdjust = false;
						requestCompleteRedraw = true;
						break;
					}
				} catch (ShapeNotFoundException snfe) {
					informMessageListener("statusbar.error.attribute.ShapeNotFoundException", MessageType.ERROR);
				}
			}
		}
		logger.debug("time for changing " + changed.size() + "transaction elements "
				+ (System.currentTimeMillis() - time1) + "ms");
		logger.debug("in transaction: updating " + setDependendComponents.size() + " dependend components");
		// long size = setDependendComponents.size();
		long time = System.currentTimeMillis();
		final int numDepComp = setDependendComponents.size();
		if (numDepComp > 4000) {
			int threads = SystemAnalysis.getNumberOfCPUs();
			if (threads > 2 && numDepComp < 10000)
				threads = 2;
			final int numThreads = threads;
			final int numElemPerThread = numDepComp / numThreads;
			ExecutorService executor = Executors.newFixedThreadPool(numThreads);
			final GraphElementComponent arrayGEC[] = setDependendComponents
					.toArray(new GraphElementComponent[numDepComp]);
			for (int i = 0; i < numThreads; i++) {
				final int startIdx = i;
				executor.submit(new Runnable() {
					
					@Override
					public void run() {
						int start = startIdx * numElemPerThread;
						int end;
						if (startIdx < numThreads - 1)
							end = startIdx * numElemPerThread + numElemPerThread;
						else
							end = numDepComp;
						logger.debug("executing thread " + startIdx + " with elements" + start + " to " + (end - 1));
						for (int k = start; k < end; k++) {
							try {
								if (arrayGEC[k] instanceof EdgeComponent)
									((EdgeComponent) arrayGEC[k]).updateShape();
								else
									((AbstractGraphElementComponent) arrayGEC[k]).createNewShape(CoordinateSystem.XY);
							} catch (ShapeNotFoundException e) {
								e.printStackTrace();
							}
						}
					}
				});
			}
			executor.shutdown();
			try {
				executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			logger.debug("time to update dep comp(threads): " + (System.currentTimeMillis() - time) + "ms");
		} else {
			
			for (GraphElementComponent gec : setDependendComponents) {
				
				try {
					if (gec instanceof EdgeComponent)
						((EdgeComponent) gec).updateShape();
					else
						((AbstractGraphElementComponent) gec).createNewShape(CoordinateSystem.XY);
				} catch (ShapeNotFoundException e) {
					e.printStackTrace();
				}
			}
			logger.debug("time to update dep comp: " + (System.currentTimeMillis() - time) + "ms");
			
		}
		/*
		 * add nodes and edges from transaction in one go
		 */
		if (!setPostAddedNodes.isEmpty() || !edgesToAdd.isEmpty()) {
			setVisible(false); // disable layout-trigger for each added component
			logger.debug("creating " + setPostAddedNodes.size() + " new nodes");
			for (GraphElement atbl : setPostAddedNodes) {
				postNodeAdded(new GraphEvent((Node) atbl));
			}
			// add pending edges
			logger.debug("creating " + edgesToAdd.size() + " new edges");
			for (Iterator<Edge> iter = edgesToAdd.iterator(); iter.hasNext();) {
				idx++;
				if (status != null)
					status.setCurrentStatusValueFine(100d * idx / maxIdx);
				postEdgeAdded(new GraphEvent(iter.next()));
			}
			setVisible(true); // layout components trigger
		}
		
		if (requestCompleteRedraw) {
			if (status != null)
				status.setCurrentStatusValue(-1);
			if (status != null)
				status.setCurrentStatusText2("Complete recreation needed");
			completeRedraw();
			if (status != null)
				status.setCurrentStatusText2("Complete recreation finished");
		}
		blockAdjust = false;
		
		adjustPreferredSize(false);
		
		// invalidate();
		logger.debug("calling repaint");
		repaint();
		
		if (status != null) {
			status.setCurrentStatusText1(s1);
			status.setCurrentStatusText2(s2);
			status.setCurrentStatusValueFine(s3);
		}
		
		getGraph().setModified(true);
		// ToolButton.requestToolButtonFocus();
		isFinishingTransacation = false;
		logger.debug("time for transactionfinished: " + (System.currentTimeMillis() - startTimeTransFinished) + "ms");
	}
	
	private void checkHiddenStatus(GraphElement ge) {
		if (ge == null)
			return;
		if (isHidden(ge)) {
			if (ge instanceof Edge)
				processEdgeRemoval((Edge) ge);
			if (ge instanceof Node)
				processNodeRemoval((Node) ge);
		}
	}
	
	/**
	 * @see org.graffiti.event.TransactionListener#transactionStarted(org.graffiti.event.TransactionEvent)
	 */
	@Override
	public void transactionStarted(TransactionEvent e) {
		super.transactionStarted(e);
		// getActiveTransactions()++;
		if (SwingUtilities.isEventDispatchThread())
			repaint();
	}
	
	/**
	 * @see org.graffiti.plugin.view.ZoomListener#zoomChanged(AffineTransform)
	 */
	@Override
	public void zoomChanged(AffineTransform newZoom) {
		super.zoomChanged(newZoom);
		adjustPreferredSize(true);
	}
	
	@Override
	public void attributeChanged(Attribute attr) {
		
		if (attr.getAttributable() instanceof Graph && attr.getName().equals("graphbackgroundcolor")
				&& attr instanceof StringAttribute) {
			Color c = ColorUtil.getColorFromHex((String) attr.getValue());
			if (getParent() != null)
				getParent().setBackground(c);
			
			getGraph().setModified(true);
		}
		
	}
	
	/**
	 * Creates a new <code>NodeComponent</code>. It first checks if the
	 * <code>graphElementComponents</code> map already (/ still) contains a
	 * component for that very edge. If yes, this component is used. Otherwise, a
	 * new component is created and entered into the given map. The original map is
	 * not altered. Therefore, the caller is responsible to use the gecMap
	 * correctly: Either provide the <code>graphElementComponents</code>, then
	 * everything is as expected. Or provide a new map; then only the new map is
	 * updated. This is used for a complete redraw. for example. There, it is not
	 * necessary to create new components if a component already exists (in fact it
	 * would be dangerous since the component might have changed, like a border
	 * added).
	 * 
	 * @param gecMap
	 * @param node
	 *           the node for which the component is built.
	 * @return DOCUMENT ME!
	 * @see #completeRedraw() for an example.
	 */
	protected NodeComponent createNodeComponent(Map<GraphElement, GraphElementComponent> gecMap, Node node) {
		
		NodeComponent nodeComponent = (NodeComponent) gecMap.get(node);
		
		if (isHidden(node)) {
			if (nodeComponent != null)
				processNodeRemoval(node);
			return null;
		}
		
		if (nodeComponent == null) {
			nodeComponent = (NodeComponent) gecMap.get(node);
			
			if (nodeComponent == null) {
				nodeComponent = new NodeComponent(node);
			}
		}
		
		if (nodeComponent != null) {
			nodeComponent.clearDependentComponentList();
			gecMap.put(node, nodeComponent);
		}
		return nodeComponent;
	}
	
	/**
	 * Extracts the name of this view class. It has to be overridden by all extended
	 * subclasses of this class.
	 * 
	 * @return DOCUMENT ME!
	 */
	@Override
	protected String extractName() {
		return this.getClass().getName();
	}
	
	MouseEvent getZoomedEvent(MouseEvent e) {
		Point2D invZoomedPoint = null;
		try {
			invZoomedPoint = zoom.inverseTransform(e.getPoint(), null);
			MouseEvent newME = new MouseEvent((Component) e.getSource(), e.getID(), e.getWhen(), e.getModifiers(),
					(int) (invZoomedPoint.getX()), (int) (invZoomedPoint.getY()), e.getClickCount(),
					e.isPopupTrigger());
			
			return newME;
		} catch (NoninvertibleTransformException nite) {
			// when setting the zoom, it must have been checked that
			// the transform is invertible
			return e;
		}
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param e
	 *           DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	MouseEvent getZoomedEventDB(MouseEvent e) {
		Point2D invZoomedPoint = null;
		try {
			invZoomedPoint = zoom.inverseTransform(e.getPoint(), null);
			MouseEvent newME = new MouseEvent((Component) e.getSource(), e.getID(), e.getWhen(), e.getModifiers(),
					(int) (invZoomedPoint.getX()), (int) (invZoomedPoint.getY()), e.getClickCount(),
					e.isPopupTrigger());
			
			return newME;
		} catch (NoninvertibleTransformException nite) {
			// when setting the zoom, it must have been checked that
			// the transform is invertible
			return e;
		}
		
	}
	
	/**
	 * Just calls <code>recurseAttributes</code>.
	 * 
	 * @param ge
	 * @param gec
	 */
	private void addAttributeComponents(GraphElement ge, GraphElementComponent gec) {
		recurseAttributes(ge.getAttributes(), gec);
	}
	
	// /**
	// * Adjusts the preferred size of the view, so that it covers all components
	// * with the new preferred size - good for automatic scrolling capability.
	// */
	// private void adjustPreferredSize() {
	// adjustPreferredSize(false);
	// }
	
	/**
	 * Adjusts the preferred size of the view, so that it covers all components with
	 * the new preferred size - good for automatic scrolling capability.
	 * 
	 * @param shrink
	 *           DOCUMENT ME!
	 */
	private void adjustPreferredSize(boolean shrink) {
		if (blockAdjust)
			return;
		Component[] components = getComponents();
		Point maxPos = new Point(50, 50);
		int compDownRightX;
		int compDownRightY;
		
		// calculates the size of the area has to be scrolled.
		for (int i = components.length - 1; i >= 0; i--) {
			compDownRightX = (components[i].getX() + components[i].getWidth());
			
			compDownRightY = (components[i].getY() + components[i].getHeight());
			maxPos.setLocation(Math.max(compDownRightX, maxPos.x), Math.max(compDownRightY, maxPos.y));
			
			// zoom.transform(maxPos, maxPos);
		}
		
		if (shrink) {
			// shrink if necessary
			Point2D zoomedMax = zoom.transform(maxPos, null);
			Dimension minSize = new Dimension((int) zoomedMax.getX(), (int) zoomedMax.getY());
			this.setSize(minSize);
			this.setPreferredSize(minSize);
		}
		
		autoresize(maxPos);
		// repaint();
	}
	
	/**
	 * Creates a new <code>EdgeComponent</code> and sets the NodeComponents
	 * associated with this edge.
	 * 
	 * @param gecMap
	 *           see createNodeComponent for its use
	 * @param edge
	 *           an edge for which this component will be built.
	 * @return an edge component with associated node components, which represent
	 *         components of source and target of the contained edge.
	 * @see #completeRedraw() for an example.
	 */
	protected EdgeComponent createEdgeComponent(Map<GraphElement, GraphElementComponent> gecMap, Edge edge) {
		
		if (blockEdges || gecMap == null)
			return null;
		
		assert edge != null;
		assert edge.getSource() != null;
		assert edge.getTarget() != null;
		EdgeComponent edgeComponent = (EdgeComponent) gecMap.get(edge);
		
		if (isHidden(edge)) {
			if (edgeComponent != null)
				processEdgeRemoval(edge);
			return null;
		}
		
		Node s = edge.getSource();
		Node t = edge.getTarget();
		NodeComponent source = ((NodeComponent) gecMap.get(s));
		NodeComponent target = ((NodeComponent) gecMap.get(t));
		
		if (edgeComponent == null) {
			edgeComponent = new EdgeComponent(edge, source, target);
		}
		
		if (edgeComponent != null) {
			gecMap.put(edge, edgeComponent);
			if (source != null)
				source.addDependentComponent(edgeComponent);
			if (target != null)
				target.addDependentComponent(edgeComponent);
		}
		return edgeComponent;
	}
	
	/**
	 * If there is a registered <code>AttributeComponent</code> for the given
	 * attribute, add it to the view. If not, do nothing. Returns <code>true</code>
	 * if a component was added, false otherwise.
	 * 
	 * @param attribute
	 * @param gec
	 * @return boolean
	 */
	private boolean maybeAddAttrComponent(Attribute attribute, GraphElementComponent gec) {
		if (acm == null || !acm.hasAttributeComponent(attribute.getClass()))
			return false;
		try {
			AttributeComponent attrComp = acm.getAttributeComponent(attribute.getClass());
			
			if (gec.getAttributeComponent(attribute) != null || attrComp == null) {
				return false;
			}
			
			gec.addAttributeComponent(attribute, attrComp);
			
			attrComp.setShift(gec.getLocation());
			
			attrComp.setAttribute(attribute);
			attrComp.setGraphElementShape(gec.getShape());
			
			try {
				attrComp.createNewShape(coordinateSystem);
				attrComp.setShift(gec.getLocation());
			} catch (ShapeNotFoundException snfe) {
				throw new RuntimeException("Should not happen since no shape is used here" + snfe);
			}
			if (gec.getGraphElement() != null && attrComp != null) {
				if (!graphElementAttributeComponents.containsKey(gec.getGraphElement()))
					graphElementAttributeComponents.put(gec.getGraphElement(), new HashSet<AttributeComponent>());
				graphElementAttributeComponents.get(gec.getGraphElement()).add(attrComp);
			}
			if (attrComp != null) {
				int indexOf = listCopyComponents.indexOf(gec);
				if (indexOf < 0) {
					this.add(attrComp, 0);
					listCopyComponents.add(0, attrComp);
				} else {
					this.add(attrComp, indexOf); // , 0);
					listCopyComponents.add(indexOf, attrComp);
				}
				
			}
			return true;
		} catch (AttributeComponentNotFoundException acnfe) {
			return false;
		}
	}
	
	private boolean maybeCreateAttributeComponent(Queue<JComponent> result, Attribute attribute,
			GraphElementComponent gec) {
		AttributeComponentManager acm = this.acm;
		if (acm == null) {
			return false;
		}
		if (!acm.hasAttributeComponent(attribute.getClass()))
			return false;
		try {
			AttributeComponent attrComp = acm.getAttributeComponent(attribute.getClass());
			
			if (gec.getAttributeComponent(attribute) != null) {
				return false;
			}
			
			attrComp.setShift(gec.getLocation());
			attrComp.setAttribute(attribute);
			attrComp.setGraphElementShape(gec.getShape());
			
			try {
				synchronized (result) {
					attrComp.createNewShape(coordinateSystem);
				}
				attrComp.setShift(gec.getLocation());
			} catch (ShapeNotFoundException snfe) {
				throw new RuntimeException("Should not happen since no shape is used here" + snfe);
			}
			if (attrComp != null) {
				GraphElement ge = gec.getGraphElement();
				if (graphElementAttributeComponents != null) {
					synchronized (graphElementAttributeComponents) {
						if (ge != null && !graphElementAttributeComponents.containsKey(ge))
							graphElementAttributeComponents.put(ge, new HashSet<AttributeComponent>());
						graphElementAttributeComponents.get(ge).add(attrComp);
					}
				} else {
					System.err.println("Internal Error: graphElementAttributeComponents is null (GraffitiView)");
				}
			}
			
			gec.addAttributeComponent(attribute, attrComp);
			
			result.add(attrComp);
			return true;
		} catch (AttributeComponentNotFoundException acnfe) {
			return false;
		}
	}
	
	/**
	 * Recursively checks all attributes in the attribute tree with the given
	 * attribute as root (using <code>maybeAddAttrComponent</code>).
	 * 
	 * @param attribute
	 * @param gec
	 */
	private void recurseAttributes(Attribute attribute, GraphElementComponent gec) {
		if (!maybeAddAttrComponent(attribute, gec) && attribute instanceof CollectionAttribute) {
			CollectionAttribute ca = (CollectionAttribute) attribute;
			ArrayList<Attribute> al = new ArrayList<Attribute>(ca.getCollection().values());
			for (Attribute subAttribute : al) {
				recurseAttributes(subAttribute, gec);
			}
		}
	}
	
	private void createAttributeComponents(Queue<JComponent> result, Attribute attribute, GraphElementComponent gec) {
		if (!maybeCreateAttributeComponent(result, attribute, gec) && attribute instanceof CollectionAttribute) {
			CollectionAttribute ca = (CollectionAttribute) attribute;
			ArrayList<Attribute> al = new ArrayList<Attribute>(ca.getCollection().values());
			for (Attribute subAttribute : al) {
				createAttributeComponents(result, subAttribute, gec);
			}
		}
	}
	
	// ~ Inner Classes ==========================================================
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @author $Author: klapperipk $
	 * @version $Revision: 1.84.6.2 $ $Date: 2014/12/08 06:41:18 $
	 */
	class ZoomedMouseListener implements MouseListener {
		/** DOCUMENT ME! */
		private final MouseListener listener;
		
		/**
		 * Creates a new ZoomedMouseListener object.
		 * 
		 * @param l
		 *           DOCUMENT ME!
		 */
		public ZoomedMouseListener(MouseListener l) {
			this.listener = l;
		}
		
		public void mouseClicked(MouseEvent e) {
			listener.mouseClicked(getZoomedEvent(e));
		}
		
		public void mouseEntered(MouseEvent e) {
			listener.mouseEntered(getZoomedEvent(e));
		}
		
		public void mouseExited(MouseEvent e) {
			listener.mouseExited(getZoomedEvent(e));
		}
		
		public void mousePressed(MouseEvent e) {
			listener.mousePressed(getZoomedEvent(e));
		}
		
		public void mouseReleased(MouseEvent e) {
			listener.mouseReleased(getZoomedEvent(e));
		}
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @author $Author: klapperipk $
	 * @version $Revision: 1.84.6.2 $ $Date: 2014/12/08 06:41:18 $
	 */
	class ZoomedMouseMotionListener implements MouseMotionListener {
		/** DOCUMENT ME! */
		private final MouseMotionListener listener;
		
		/**
		 * Creates a new ZoomedMouseMotionListener object.
		 * 
		 * @param l
		 *           DOCUMENT ME!
		 */
		public ZoomedMouseMotionListener(MouseMotionListener l) {
			this.listener = l;
		}
		
		public void mouseDragged(MouseEvent e) {
			listener.mouseDragged(getZoomedEvent(e));
		}
		
		public void mouseMoved(MouseEvent e) {
			listener.mouseMoved(getZoomedEventDB(e));
		}
	}
	
	@Override
	public String getViewName() {
		return "Gravisto Default View";
	}
	
	public void repaint(GraphElement ge) {
		repaint();
	}
	
	public void repaintGraphElementComponent(GraphElementComponent gec) {
		if (gec == null)
			return;
		logger.debug("repainting Graph Element Compoment");
		double zoomx = getZoom() == null ? 1 : getZoom().getScaleX();
		double zoomy = getZoom() == null ? 1 : getZoom().getScaleY();
		double newx = (double) (gec.getX()) * zoomx;
		double newy = (double) (gec.getY()) * zoomy;
		double newwidth = (double) (gec.getWidth()) * zoomx;
		double newheight = (double) (gec.getHeight()) * zoomy;
		int delta = 10; // 10 pixels in rescaled (zoomed) space
		repaint(0, (int) newx, (int) newy, (int) newwidth + delta, (int) newheight + delta);
		
	}
	
	public GraffitiFrame[] getDetachedFrames() {
		ArrayList<JFrame> result = new ArrayList<JFrame>();
		for (GraffitiFrame gf : MainFrame.getInstance().getDetachedFrames()) {
			if (gf.getSession().getGraph() == getGraph())
				result.add(gf);
		}
		return result.toArray(new GraffitiFrame[] {});
	}
	
	public boolean putInScrollPane() {
		return true;
	}
	
	public boolean isHidden(GraphElement ge) {
		try {
			ge.getAttribute(GraphicAttributeConstants.GRAPHICS);
		} catch (AttributeNotFoundException e) {
			return false;
		}
		return AttributeHelper.isHiddenGraphElement(ge);
	}
	
	public CoordinateSystem getCoordinateSystem() {
		return coordinateSystem;
	}
	
	private int createGUIcomponentElements(final boolean addShapesAndAttributeComponentsTogether,
			final Collection<GraphElement> ges, final BlockingQueue<JComponent> result, long ttt,
			Map<GraphElement, GraphElementComponent> gecm) {
		
		int curr = 0;
		
		for (GraphElement ge : ges) {
			if (ge.getGraph() == null)
				continue;
			if (addShapesAndAttributeComponentsTogether) {
				GraphElementComponent gc = gecm.get(ge);
				if (gc != null)
					result.add(gc);
			}
			GraphElementComponent gc = gecm.get(ge);
			createAttributeComponents(result, ge.getAttributes(), gc);
		}
		return curr;
	}
	
	/**
	 * @param reduced
	 */
	public void setDrawMode(DrawMode dm) {
		this.drawMode = dm;
		repaint();
	}
	
	public DrawMode getDrawMode() {
		return drawMode;
	}
	
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
