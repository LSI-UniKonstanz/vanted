package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.fastshapeview;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.AttributeHelper;
import org.apache.log4j.Logger;
import org.graffiti.event.AttributeEvent;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.managers.AttributeComponentManager;
import org.graffiti.plugin.view.AbstractView;
import org.graffiti.plugin.view.GraphView;

public class FastShapeView extends AbstractView 
implements GraphView{

	private static final long serialVersionUID = -828563505553869720L;
	
	private static final String VIEWNAME = "Fast Shape View";

	static final Logger logger = Logger.getLogger(FastShapeView.class);
	
	
	Map<GraphElement, GraphElementContainer> mapGraphElementToGraphelementContainer;
	
	Map<GraphElementContainer, GraphElement> mapGraphelementContainerToGraphElement;
	
	List<NodeElementContainer> listNodeElementContainer;
	List<EdgeElementContainer> listEdgeElementContainer;
	
	
	
	/** Maps MouseListeners to their corresponding ZoomedMouseListeners */
	private final Map<MouseListener, ZoomedMouseListener> zoomedMouseListeners = new LinkedHashMap<MouseListener, ZoomedMouseListener>();
	
	/**
	 * Maps MouseMotionListeners to their corresponding
	 * ZoomedMouseMotionListeners
	 */
	private final Map<MouseMotionListener, ZoomedMouseMotionListener> zoomedMouseMotionListeners = new LinkedHashMap<MouseMotionListener, ZoomedMouseMotionListener>();


	/**
	 *  Default constructor
	 */
	public FastShapeView() {
		setLayout(null);
		zoom = new AffineTransform();
		
		mapGraphElementToGraphelementContainer = new HashMap<GraphElement, GraphElementContainer>();
		mapGraphelementContainerToGraphElement = new HashMap<GraphElementContainer, GraphElement>();
		listNodeElementContainer = new ArrayList<NodeElementContainer>();
		listEdgeElementContainer = new ArrayList<EdgeElementContainer>();
				
	}


	@Override
	public void completeRedraw() {
		int width = 100;
		int height = 100;
		for(Node curNode : currentGraph.getNodes()){
			NodeElementContainer nec = new NodeElementContainer(this, curNode);
			mapGraphElementToGraphelementContainer.put(nec.graphElement, nec);
			mapGraphelementContainerToGraphElement.put(nec, nec.graphElement);
			listNodeElementContainer.add(nec);
			int tX = (int)(nec.getShape().getBounds().getX() + nec.getShape().getBounds().getWidth());
			if(tX > width)
				width = tX;
			int tY = (int)(nec.getShape().getBounds().getY() + nec.getShape().getBounds().getHeight());
			if(tY > height)
				height = tY;
		}
		for(Edge curEdge : currentGraph.getEdges()){
			EdgeElementContainer eec = new EdgeElementContainer(this, curEdge);
			mapGraphElementToGraphelementContainer.put(eec.graphElement, eec);
			mapGraphelementContainerToGraphElement.put(eec, eec.graphElement);
			listEdgeElementContainer.add(eec);
			
			mapGraphElementToGraphelementContainer.get(curEdge.getSource()).addDependentContainer(eec);
			mapGraphElementToGraphelementContainer.get(curEdge.getTarget()).addDependentContainer(eec);
		}
		setPreferredSize(new Dimension(width + 50, height + 50));
	}

	@Override
	public void repaint(GraphElement ge) {
	}

	@Override
	public void setGraph(Graph g) {
		
		this.currentGraph = g;
		if(g != null)
			completeRedraw();
	}

	@Override
	protected String extractName() {
		return getClass().getName();
	}

	@Override
	public String getViewName() {
		return VIEWNAME;
	}

	
	
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		AffineTransform transform = g2.getTransform();
		transform.concatenate(zoom);
		g2.setTransform(transform);
		
		super.paint(g2);
	}


	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		char[] text = "shape view demo".toCharArray();
		g.drawChars(text, 0, text.length, 10, 10);
		
		
		
		for(NodeElementContainer nec : listNodeElementContainer){
			nec.paint(g);
		}
		for(EdgeElementContainer eec : listEdgeElementContainer){
			eec.paint(g);
		}
	}
	
	
	@Override
	public void zoomChanged(AffineTransform newZoom) {
		this.zoom = newZoom;
		adjustViewPreferredSize();

	}


	/**
	 * 
	 */
	private void adjustViewPreferredSize() {
		double width = (double)getWidth() * zoom.getScaleX();
		double height = (double)getHeight() * zoom.getScaleY();
		setPreferredSize(new Dimension((int)width, (int)height));
	}

	public GraphElementContainer findContainer(int x, int y) {
		GraphElementContainer container = null;
		for(NodeElementContainer nec : listNodeElementContainer) {
			if(nec.getShape().contains(new Point2D.Double(x, y)))
				container = nec;
		}
		return container;
	}

	
	
	
	@Override
	public void postAttributeChanged(AttributeEvent e) {
		GraphElementContainer container = mapGraphElementToGraphelementContainer.get(
				e.getAttributeable());
		container.updateShape();
		
		repaint();
	}


	public boolean putInScrollPane() {
		return true;
	}
	
	public boolean isHidden(GraphElement ge) {
		return AttributeHelper.isHiddenGraphElement(ge);
	}
	
	public AttributeComponentManager getAttributeComponentManager(){
		return acm;
	}
	
	
	@Override
	public void close() {
		// System.err.println("FastView: close");
		zoomedMouseListeners.clear();
		zoomedMouseMotionListeners.clear();
	}


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
		
		/**
		 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
		 */
		public void mouseClicked(MouseEvent e) {
			listener.mouseClicked(getZoomedEvent(e));
		}
		
		/**
		 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
		 */
		public void mouseEntered(MouseEvent e) {
			listener.mouseEntered(getZoomedEvent(e));
		}
		
		/**
		 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
		 */
		public void mouseExited(MouseEvent e) {
			listener.mouseExited(getZoomedEvent(e));
		}
		
		/**
		 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
		 */
		public void mousePressed(MouseEvent e) {
			listener.mousePressed(getZoomedEvent(e));
		}
		
		/**
		 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
		 */
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
		
		/**
		 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
		 */
		public void mouseDragged(MouseEvent e) {
			listener.mouseDragged(getZoomedEvent(e));
		}
		
		/**
		 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
		 */
		public void mouseMoved(MouseEvent e) {
			listener.mouseMoved(getZoomedEvent(e));
		}
	}
	
	MouseEvent getZoomedEvent(MouseEvent e) {
		Point2D invZoomedPoint = null;
		
		try {
			invZoomedPoint = zoom.inverseTransform(e.getPoint(), null);
		} catch (NoninvertibleTransformException nite) {
			// when setting the zoom, it must have been checked that
			// the transform is invertible
		}
		
		MouseEvent newME = new MouseEvent((Component) e.getSource(), e.getID(), e
				.getWhen(), e.getModifiers(), (int) (invZoomedPoint.getX()),
				(int) (invZoomedPoint.getY()), e.getClickCount(), e
						.isPopupTrigger());
		
		return newME;
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
	
	/**
	 * @see java.awt.Component#addMouseMotionListener(java.awt.event.MouseMotionListener)
	 */
	@Override
	public void addMouseMotionListener(MouseMotionListener l) {
		ZoomedMouseMotionListener zoomedListener = new ZoomedMouseMotionListener(
				l);
		zoomedMouseMotionListeners.put(l, zoomedListener);
		super.addMouseMotionListener(zoomedListener);
	}
	
	@Override
	public void removeMouseMotionListener(MouseMotionListener l) {
		ZoomedMouseMotionListener removeListener = zoomedMouseMotionListeners.get(l);
		super.removeMouseMotionListener(removeListener);
		zoomedMouseMotionListeners.remove(l);
	}
	
	
}
