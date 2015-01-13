package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.navigation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.AttributeHelper;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.apache.log4j.Logger;
import org.graffiti.event.GraphEvent;
import org.graffiti.event.GraphListener;
import org.graffiti.event.ListenerManager;
import org.graffiti.event.TransactionEvent;
import org.graffiti.graph.Node;
import org.graffiti.plugin.gui.AbstractGraffitiContainer;
import org.graffiti.plugin.view.View;
import org.graffiti.session.Session;
import org.graffiti.session.SessionListener;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.zoomfit.ZoomFitChangeComponent;

/**
 * provides an overview view with navigation window, which allows to browse the network.
 * It is a view, which mimics the same features as e.g. photoshop overview window, where the user has a complete 
 * picture including navigation
 * 
 * This view provides click, move and zoom operations
 * 
 * It preserves the scale of the network, even if the view has other dimensions as the network
 * 
 * This view can be used anywhere, in a panel or a seperate frame
 * 
 * It listens to dedicated events in the current set view, to get information about the position in the network
 * 
 * The component is initialised by using the setView Method
 * 
 * @author klapper
 *
 */
public class NavigationComponentView 
extends AbstractGraffitiContainer
implements
AdjustmentListener, GraphListener, SessionListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5766163120487146555L;

	Logger logger = Logger.getLogger(NavigationComponentView.class);

	//the current graffiti view, we're listening on
	View view;
	JComponent viewcomponent;
	JScrollPane scrollpane;

	AdjustmentEvent curAdjustmentEvent;
	ViewMouseListener viewMouseListener;
	NavigationComponentMouseListener navigationCompMouseListener;
	
	Dimension graphDimesion;

	private double w;

	private double h;
	
	
	public NavigationComponentView() {
		super();
		id = "navigationview";
		/*
		 * the listener for the active Graph view
		 * we need to listen to some events like node movement/add/remove
		 */
		viewMouseListener = new ViewMouseListener();
		/*
		 * this listener class handles all mouse events happening 
		 * on this component
		 */
		navigationCompMouseListener = new NavigationComponentMouseListener();

		/* this component listens to all types of mouse events */
		addMouseListener(navigationCompMouseListener);
		addMouseMotionListener(navigationCompMouseListener);
		addMouseWheelListener(navigationCompMouseListener);
	}

	
	
	@Override
	public void sessionChanged(Session s) {
		if(s == null){
			setView(null);
			return;
		} else
			setView(s.getActiveView());
	}



	@Override
	public void sessionDataChanged(Session s) {
		// TODO Auto-generated method stub
		
	}



	public void setView(View newview){
		init(newview);
		this.view = newview;
		repaint();
	}

	/**
	 * initialises the Navigationview with a new graph view
	 * @param newview
	 */
	void init(View newview){

		/*
		 * if there was a previous graph view, remove all the listeners
		 */
		if(view != null)
		{
			view.getViewComponent().removeMouseListener(viewMouseListener);
			view.getViewComponent().removeMouseMotionListener(viewMouseListener);
		}
		/* catch case, where there is no view in Vanted */
		if(newview == null){
			scrollpane = null;
			return;
		}
		/*
		 * setup the new view with listeners
		 * and get some variables we'll use more often
		 */
		newview.getViewComponent().addMouseListener(viewMouseListener);
		newview.getViewComponent().addMouseMotionListener(viewMouseListener);

		/* remove old scrollbar listeners */
		if(scrollpane != null){
			scrollpane.getHorizontalScrollBar().removeAdjustmentListener(this);
			scrollpane.getVerticalScrollBar().removeAdjustmentListener(this);
		}
		/* add this component to listen to scrollbar changes */
		scrollpane = (JScrollPane) ErrorMsg.findParentComponent(newview.getViewComponent(), JScrollPane.class);
		if(scrollpane == null)
			return;
		scrollpane.getHorizontalScrollBar().addAdjustmentListener(this);
		scrollpane.getVerticalScrollBar().addAdjustmentListener(this);

		/*
		 * listen to graph events, like node added and removed
		 */
		ListenerManager lm = newview.getGraph().getListenerManager();
		lm.addDelayedGraphListener(this);

		graphDimesion = new Dimension();
		
		//		setPreferredSize(new Dimension());
	}

	@Override
	protected void paintComponent(Graphics g) {
		// TODO Auto-generated method stub
		super.paintComponent(g);
		
		/*
		 * draw dark background
		 */
		setBackground(Color.GRAY);

		if(view == null)
			return;
		viewcomponent = view.getViewComponent();
		
		/*
		 * w and h are the variables which will have the same aspect ratio and the graphs viewcomponent
		 * these values have to be calculated first, by using the aspect ratio of the viewcomponent and the aspect ratio
		 * of this navigation view, which can be totally different
		 */
		w = getWidth();
		h = getHeight();
		double ratio = w/h;
		double ratioVC = (double)viewcomponent.getWidth()/(double)viewcomponent.getHeight();
		
		/*
		 * depending of the ratios of the view set the new w and h values
		 * drawing of the overview graph will happen only within width w and height h
		 * This 'magic' formula below calculates the new values based
		 */
		if(ratioVC < ratio){
			w = w*(ratioVC/ratio);
		} else if (ratioVC > ratio){
			h = h/(ratioVC/ratio);
		}
					
		g.setColor(Color.white);
		g.fillRect(0, 0, (int)w, (int)h);
		/*
		 * draw miniture graph
		 */
		if(view.getGraph() != null && view.getGraph().getNodes() != null){
			for(Node n : view.getGraph().getNodes()){
				
				/*
				 * normalize the coordinates within 0..1
				 */
				double normWidth = AttributeHelper.getWidth(n) / viewcomponent.getWidth() * view.getZoom().getScaleX();
				double normHeight = AttributeHelper.getHeight(n) / viewcomponent.getHeight() * view.getZoom().getScaleY();
				double normX = AttributeHelper.getPositionX(n) / viewcomponent.getWidth() * view.getZoom().getScaleX() - normWidth/2;
				double normY = AttributeHelper.getPositionY(n) / viewcomponent.getHeight() * view.getZoom().getScaleY() - normHeight/2;
//				if(AttributeHelper.getLabel(n, "").equals("cytosol")){
//					logger.debug("zoom (scaneXY): "+view.getZoom().getScaleX()+" "+view.getZoom().getScaleY());
//					logger.debug("viewmaxX: "+view.getViewComponent().getWidth()+" viewmaxY: "+view.getViewComponent().getHeight());
////					logger.debug("scrollpane viewportsize w h: "+ scrollpane.getViewport().getWidth()+ " " + scrollpane.getViewport().getHeight());
//					logger.debug("normX normY normW normH: "+normX+" "+normY+" "+normWidth+" "+normHeight);
//					logger.debug("node x y: "+ AttributeHelper.getPositionX(n) + " "+ AttributeHelper.getPositionY(n));
//					logger.debug("view x y w h" +(int)(normX*w)+" "+ (int)(normY * h)+" "+ (int)(normWidth * w)+" "+ (int)(normHeight * h));
//				}

				String shape = AttributeHelper.getShape(n).toLowerCase();
					
				
				//			try {
				//				ColorAttribute colorAtt = null;
				//				colorAtt = (ColorAttribute) n.getAttribute(GraphicAttributeConstants.FILLCOLOR_PATH);
				//				g.setColor(colorAtt.getColor());
				//			} catch (Exception ex) {
				//				g.setColor(Color.black);
				////				logger.debug("using black");
				//			}
				//			g.fillRect((int)(normX*w), (int)(normY * h), (int)(normWidth * w), (int)(normHeight * h));

				//				try {
				//					ColorAttribute colorAtt = null;
				//
				//					if (AttributeHelper.hasAttribute(n, GraphicAttributeConstants.FRAMECOLOR)) {
				//						colorAtt = (ColorAttribute) n.getAttribute(GraphicAttributeConstants.FRAMECOLOR);
				//					} else {
				//						colorAtt = (ColorAttribute) n.getAttribute(GraphicAttributeConstants.OUTLINE_PATH);
				//
				//					}
				//					g.setColor(colorAtt.getColor());
				//				} catch (Exception ex) {
				//					g.setColor(Color.lightGray);
				//
				//				}		
				
				/*
				 * use normalizes coordinates to set element in overview view
				 */
				g.setColor(Color.lightGray);
				if(shape != null && (shape.contains("circle") || shape.contains("oval")))
					g.drawOval((int)(normX*w), (int)(normY * h), (int)(normWidth * w), (int)(normHeight * h));
				else
					g.drawRect((int)(normX*w), (int)(normY * h), (int)(normWidth * w), (int)(normHeight * h));
			}
		}

		/*
		 * draw visible rectangle which represents the region, that the graph viewcomponent shows
		 */

		Rectangle2D visibleRect = view.getViewComponent().getVisibleRect();

		double normX = visibleRect.getX() / viewcomponent.getWidth();
		double normY = visibleRect.getY() / viewcomponent.getHeight();
		double normWidth = (visibleRect.getWidth()) / viewcomponent.getWidth();
		double normHeight = (visibleRect.getHeight()) / viewcomponent.getHeight();
		g.setColor(Color.blue);
		g.drawRect((int)(normX * w), (int)(normY * h), (int)((normWidth)*w), (int)((normHeight)*h));
	}


	/**
	 * listener that listens to value changes of the scrollbar
	 * and simply repaint
	 */
	@Override
	public void adjustmentValueChanged(AdjustmentEvent e) {
		// TODO Auto-generated method stub
//		Adjustable source = e.getAdjustable();
//		curAdjustmentEvent = e;
//		// getValueIsAdjusting() returns true if the user is currently
//		// dragging the scrollbar's knob and has not picked a final value
//		if (e.getValueIsAdjusting()) {
//			//			logger.debug("The user is dragging the knob");
////			return;
//		}
//
//
//
//
//		// Get current value
//		int value = e.getValue();
//		int maxH = scrollpane.getHorizontalScrollBar().getMaximum();
//		int maxV = scrollpane.getVerticalScrollBar().getMaximum();
//		//		logger.debug("value: "+value+" zoom (scaneXY): "+view.getZoom().getScaleX()+" "+view.getZoom().getScaleX());
//		logger.debug("maxH: "+maxH+" maxV: "+maxV);
//		//		logger.debug("viewmaxX: "+view.getViewComponent().getWidth()+" viewmaxY: "+view.getViewComponent().getHeight());
////		Rectangle r = view.getViewComponent().getVisibleRect();
//		//		logger.debug("x y width height: "+r.x+ " "+r.y +" "+ r.width + " "+ r.height);
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				repaint();

			}
		});
	}

	/**
	 * a common mousehandler for several event (click / move) for the navi view
	 * which sets the scrollbarvalues and with that, the position of the viewcomponents visible view
	 * @param e
	 */
	void handleMouseEvent(MouseEvent e){
		if( ! e.getComponent().equals(this))
			return;
		if(view == null || scrollpane == null)
			return;
		logger.debug("mouse event");
		double normX = (double)e.getX() / (double)w;
		double normY = (double)e.getY() / (double)h;

		scrollpane.getHorizontalScrollBar().setValue((int)(normX * viewcomponent.getWidth()) - viewcomponent.getVisibleRect().width/2);
		scrollpane.getVerticalScrollBar().setValue((int)(normY * viewcomponent.getHeight()) - viewcomponent.getVisibleRect().height/2);

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				repaint();

			}
		});
	}

	class NavigationComponentMouseListener implements MouseListener, MouseMotionListener, MouseWheelListener{
		@Override
		public void mouseClicked(MouseEvent e) {

			handleMouseEvent(e);
		}

		@Override
		public void mousePressed(MouseEvent e) {
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			handleMouseEvent(e);
		}

		@Override
		public void mouseMoved(MouseEvent e) {
		}
		public void mouseWheelMoved(MouseWheelEvent e) {
			
			if (e.getWheelRotation() < 0)
				ZoomFitChangeComponent.zoomIn();
			else
				ZoomFitChangeComponent.zoomOut();
			e.consume();
			return;
		}
	}

	class ViewMouseListener implements MouseListener, MouseMotionListener{
		@Override
		public void mouseClicked(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		/**
		 * listen to drag events like node and edge movement
		 * but do not(!) listen to rightclick dragging of the viewcomponent
		 * This would result in a mess
		 */
		@Override
		public void mouseDragged(MouseEvent e) {
//			logger.debug("mouse dragged");
			if(!SwingUtilities.isRightMouseButton(e))
				repaint();

		}

		@Override
		public void mouseMoved(MouseEvent e) {
			// TODO Auto-generated method stub

		}
	}


	@Override
	public void transactionFinished(TransactionEvent e,
			BackgroundTaskStatusProviderSupportingExternalCall status) {
		
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				repaint();

			}
		});
	}

	@Override
	public void transactionStarted(TransactionEvent e) {
	}

	@Override
	public void postEdgeAdded(GraphEvent e) {
	}

	@Override
	public void postEdgeRemoved(GraphEvent e) {
	}

	@Override
	public void postGraphCleared(GraphEvent e) {

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				repaint();

			}
		});
	}

	@Override
	public void postNodeAdded(GraphEvent e) {
//		logger.debug("repaint");

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				repaint();

			}
		});
	}

	@Override
	public void postNodeRemoved(GraphEvent e) {
//		logger.debug("repaint");

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				repaint();

			}
		});
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
}
