// ==============================================================================
//
// AbstractGraphElementComponent.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: AbstractGraphElementComponent.java,v 1.16 2010/12/22 13:06:19 klukas Exp $

package org.graffiti.plugins.views.defaults;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

import org.AttributeHelper;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.DoubleAttribute;
import org.graffiti.graph.GraphElement;
import org.graffiti.graphics.GraphElementGraphicAttribute;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.plugin.attributecomponent.AbstractAttributeComponent;
import org.graffiti.plugin.view.AttributeComponent;
import org.graffiti.plugin.view.CoordinateSystem;
import org.graffiti.plugin.view.GraffitiViewComponent;
import org.graffiti.plugin.view.GraphElementComponent;
import org.graffiti.plugin.view.GraphElementShape;
import org.graffiti.plugin.view.ShapeNotFoundException;
import org.graffiti.plugin.view.View;
import org.graffiti.plugin.view.Zoomable;
import org.vanted.VantedPreferences;

/**
 * Class that shares common members for all GraphElementComponents.
 * 
 * @version $Revision: 1.16 $
 */
public abstract class AbstractGraphElementComponent
		extends GraphElementComponent
		implements GraffitiViewComponent, GraphicAttributeConstants {
	
	private static Logger logger = Logger.getLogger(AbstractGraphElementComponent.class);
	
	private static boolean isDebuggingLevel = Logger.getRootLogger().getLevel() == Level.DEBUG;
	
	// ~ Instance fields ========================================================
	private static final long serialVersionUID = 1L;
	
	/** The <code>GraphElement</code> that is represented by this component. */
	protected GraphElement graphElement;
	
	/** The <code>shape</code> that is drawn onto that component. */
	protected GraphElementShape shape;
	
	/**
	 * A list of components whose position is dependent on the position of this
	 * shape. This is only meant for edges that depend on the position (and
	 * other graphics attributes) of nodes.
	 */
	protected List<GraphElementComponent> dependentComponents;
	
	/**
	 * A mapping between attribute classnames and attributeComponent classnames
	 * that this <code>GraphElement</code> has. These attributes are therefore
	 * attribute and their position is dependent on the position (and size) of
	 * this GraphElement. (this applies mainly to nodes)
	 */
	protected Map<Attribute, GraffitiViewComponent> attributeComponents;
	
	protected CoordinateSystem coordinateSystem;
	
	/**
	 * To support transparency through alpha value, every graph component will have
	 * a composite field, which defines the transparency
	 */
	protected Composite composite;

	protected float alpha;
	
//	protected BufferedImage opacityRenderImage;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructor for GraphElementComponent.
	 * 
	 * @param ge
	 *           DOCUMENT ME!
	 */
	protected AbstractGraphElementComponent(GraphElement ge) {
		super();
		this.graphElement = ge;
		attributeComponents = new LinkedHashMap<Attribute, GraffitiViewComponent>();
		dependentComponents = new ArrayList<GraphElementComponent>();
		this.setOpaque(false);
		
		/*
		 * new graphelement transparency attribute 
		 */
		double opacity = (double) AttributeHelper.getAttributeValue(ge, GraphicAttributeConstants.GRAPHICS, GraphicAttributeConstants.OPAC, 1.0, new Double(1));
		setupOpacity(opacity);
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Returns GraphElementShape object
	 * 
	 * @return DOCUMENT ME!
	 */
	public GraphElementShape getShape() {
		return this.shape;
	}
	
	/**
	 * Adds an <code>Attribute</code> and its <code>GraffitiViewComponent</code> to the list of registered attributes
	 * that can be displayed. This attribute is then treated as dependent on
	 * the position, size etc. of this <code>GraphElement</code>.
	 * 
	 * @param attr
	 *           the attribute that is registered as being able to be
	 *           displayed.
	 * @param ac
	 *           the component that will be used to display the attribute.
	 */
	public synchronized void addAttributeComponent(Attribute attr, GraffitiViewComponent ac) {
		attributeComponents.put(attr, ac);
	}
	
	/**
	 * Adds a <code>GraphElementComponent</code> to the list of dependent <code>GraphElementComponent</code>s. These will nearly always be
	 * <code>EdgeComponent</code>s that are dependent on their source or
	 * target nodes.
	 * 
	 * @param comp
	 *           the <code>GraphElementComponent</code> that is added to the
	 *           list of dependent components.
	 */
	public void addDependentComponent(GraphElementComponent comp) {
		this.dependentComponents.add(comp);
	}
	
	public List<GraphElementComponent> getDependentGraphElementComponents() {
		return dependentComponents;
	}
	
	/**
	 * Called when an attribute of the GraphElement represented by this
	 * component has changed.
	 * 
	 * @param attr
	 *           the attribute that has triggered the event.
	 * @throws ShapeNotFoundException
	 *            DOCUMENT ME!
	 */
	public void attributeChanged(Attribute attr)
			throws ShapeNotFoundException {


		/* check if attribute is for opacity AND double
		 * because for color transparency it's also called opacity but integer
		 */
		if(attr.getId().equals(GraphicAttributeConstants.OPAC) && attr instanceof DoubleAttribute) {
			double opacity = ((DoubleAttribute) attr).value;
			setupOpacity(opacity);
		}
		
		if (attr.getPath().equals(Attribute.SEPARATOR + GraphicAttributeConstants.GRAPHICS)) {
			Attribute attribute = ((GraphElementGraphicAttribute) attr).getAttribute(OPAC);
			double opacity = ((DoubleAttribute) attribute).value;
			setupOpacity(opacity);
		}
		if (attr.getPath().startsWith(Attribute.SEPARATOR + GraphicAttributeConstants.GRAPHICS)
			
			) {
			if (!attr.getId().equals("cluster")) {
				graphicAttributeChanged(attr);
			}
		} else if(attr.getName().equals("max_charts_in_column")){
			/*
			 * a hack due to old implementation where adding another experiment
			 *	just fires a max_charts_per_row attribute change event
			 *  but we need to re-adjust the graphics for that as well to make it visible 
			 */
			graphicAttributeChanged(attr);
		} else
			nonGraphicAttributeChanged(attr);

	}
	
	/**
	 * Removes a <code>GraphElementComponent</code> from the list of dependent <code>GraphElementComponent</code>s.
	 */
	public void clearDependentComponentList() {
		this.dependentComponents = new ArrayList<GraphElementComponent>();
	}
	
	/**
	 * Called to initialize the shape of the NodeComponent correctly. Also
	 * calls <code>repaint()</code>.
	 * 
	 * @exception ShapeNotFoundException
	 *               thrown when the shape class couldn't be
	 *               resolved.
	 */
	public void createNewShape(CoordinateSystem coordSys)
			throws ShapeNotFoundException {
		this.coordinateSystem = coordSys;
		recreate();
	}
	
	/**
	 * Called to initialize and draw a standard shape, if the specified
	 * shape class could not be found.
	 */
	public abstract void createStandardShape();
	
	/**
	 * Returns the attributeComponents of given attribute.
	 * 
	 * @param attr
	 * @return Map
	 */
	public synchronized AttributeComponent getAttributeComponent(Attribute attr) {
		return (AttributeComponent) attributeComponents.get(attr);
	}
	
	/**
	 * Returns the attributeComponents of given attribute.
	 * 
	 * @return Map
	 */
	public synchronized Iterator<GraffitiViewComponent> getAttributeComponentIterator() {
		return attributeComponents.values().iterator();
	}
	
	public synchronized Collection<GraffitiViewComponent> getAttributeComponents() {
		return attributeComponents.values();
	}
	
	/**
	 * Returns the graphElement.
	 * 
	 * @return GraphElement
	 */
	public GraphElement getGraphElement() {
		return graphElement;
	}
	
	/**
	 * Removes all entries in the attributeComponent list.
	 */
	public synchronized void clearAttributeComponentList() {
		attributeComponents = new HashMap<Attribute, GraffitiViewComponent>();
	}
	
	/**
	 * Returns whether the given coordinates lie within this component and
	 * within its encapsulated shape. The coordinates are assumed to be
	 * relative to the coordinate system of this component.
	 * 
	 * @see java.awt.Component#contains(int, int)
	 */
	@Override
	public boolean contains(int x, int y) {
		return false;
		/*
		 * AffineTransform zoom = getZoom();
		 * Point2D p = null;
		 * try
		 * {
		 * p = zoom.inverseTransform(new Point2D.Double(x + getX(), y +
		 * getY()), null);
		 * }
		 * catch(NoninvertibleTransformException e)
		 * {
		 * }
		 * x = (int) (p.getX() - getX());
		 * y = (int) (p.getY() - getY());
		 * return (super.contains(x, y) && this.shape.contains(x, y));
		 */
	}
	
	/**
	 * Called when a graphic attribute of the GraphElement represented by this
	 * component has changed.
	 * 
	 * @param attr
	 *           the graphic attribute that has triggered the event.
	 * @throws ShapeNotFoundException
	 *            DOCUMENT ME!
	 */
	public synchronized void graphicAttributeChanged(Attribute attr)
			throws ShapeNotFoundException {
		/*
		 * if the type of the shape or the size changed then we have to
		 * rebuild the shape
		 */
		if (attr == null || attr.getId().equals(SHAPE) || (attr.getId().equals(GRAPHICS))) {
			for (Iterator<GraffitiViewComponent> it = attributeComponents.values().iterator(); it.hasNext();) {
				((AttributeComponent) it.next()).recreate();
			}
			
			createNewShape(CoordinateSystem.XY);
		} else { // if another graphic attribute changed only repaint is needed
		
			for (Iterator<GraffitiViewComponent> it = attributeComponents.values().iterator(); it.hasNext();) {
				((JComponent) it.next()).repaint();
			}
			
			repaint();
		}
	}
	
	protected void setupOpacity(double opacity) {
		
		if (opacity > 1.0)
			opacity = 1.0;
		if (opacity < 0)
			opacity = 0.0;
		alpha = ((float) opacity);
		if (opacity < 1.0) {
			setOpaque(false);
			composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
		} else {
			
			composite = null;
			setOpaque(true);
		}
		for (GraffitiViewComponent viewComp : getAttributeComponents()) {
			if (viewComp instanceof AbstractAttributeComponent) {
				((AbstractAttributeComponent) viewComp).setAlpha(alpha);
			}
		}
		
	}
	
	/**
	 * Called when a non-graphic attribute of the GraphElement represented by
	 * this component has changed.
	 * 
	 * @param attr
	 *           the attribute that has triggered the event.
	 * @throws ShapeNotFoundException
	 *            DOCUMENT ME!
	 */
	public synchronized void nonGraphicAttributeChanged(Attribute attr)
			throws ShapeNotFoundException {
		Attribute runAttr = attr;
		
		while (!(attr == null) && !runAttr.getPath().equals("")) {
			if (attributeComponents.containsKey(runAttr)) {
				(attributeComponents.get(runAttr)).attributeChanged(attr);
				break;
			}
			
			// "else":
			runAttr = runAttr.getParent();
		}
	}
	
	/**
	 * Paints the graph element contained in this component.
	 * 
	 * @param g
	 *           the graphics context in which to paint.
	 * @see javax.swing.JComponent#paintComponent(Graphics)
	 */
	@Override
	public void paintComponent(Graphics g) {
		
		if (composite != null && ((GraffitiView)getParent()).getDrawMode() != DrawMode.FAST)
			((Graphics2D) g).setComposite(composite);
		
//		super.paintComponent(g);
		/*
		 * Only for debugging 
		 */
		if(isDebuggingLevel) {
//			boolean drawFrames = PreferenceManager.getPreferenceForClass(VantedPreferences.class).getBoolean(VantedPreferences.PREFERENCE_DEBUG_SHOWPANELFRAMES, false); 
			boolean drawFrames = VantedPreferences.PREFERENCE_DEBUG_SHOWPANELFRAMES_VALUE; 
			// draw frame, indicating the panel-bounds
			if(drawFrames) {
				g.setColor(Color.GRAY);
				g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
			} 
			
			// draw original shape
			drawShape(g);
			
			// overlap shape with position info
			if(drawFrames) {
				g.setFont(new Font(g.getFont().getFontName(), 0, 3));
				g.setColor(Color.WHITE);
				g.fillRect(5, 2, 40, 6);
				g.setColor(Color.BLUE);
				String bounds = "[" + getBounds().getX() + ", " + getBounds().getY() + ", " + (getBounds().getX() + getBounds().getWidth()) + ", " + (getBounds().getY() + getBounds().getHeight()) + "]";
				g.drawString(bounds, 5, 5);
				bounds = "[" + getBounds().getX() + ", " + getBounds().getY() + ", " + getBounds().getWidth() + ", " + getBounds().getHeight() + "]";
				g.drawString(bounds, 5, 8);
			}
		} else
			drawShape(g);
	}
	
	/**
	 * Removes a <code>GraffitiViewComponent</code> of an <code>Attribute</code> from collection of attribute components.
	 * 
	 * @param attr
	 *           the attribute that has to be removed
	 */
	public synchronized void removeAttributeComponent(Attribute attr) {
		attributeComponents.remove(attr);
	}
	
	/**
	 * Removes a <code>GraphElementComponent</code> from the list of dependent <code>GraphElementComponent</code>s.
	 * 
	 * @param comp
	 *           the <code>GraphElementComponent</code> that is removed from
	 *           the list of dependent components.
	 */
	public void removeDependentComponent(GraphElementComponent comp) {
		this.dependentComponents.remove(comp);
	}
	
	/**
	 * Retrieve the zoom value from the view this component is displayed in.
	 * 
	 * @return DOCUMENT ME!
	 */
	protected AffineTransform getZoom() {
		Container parent = getParent();
		
		if (parent instanceof Zoomable) {
			AffineTransform zoom = ((Zoomable) parent).getZoom();
			
			return zoom;
		} else
			return View.NO_ZOOM;
	}
	
	/**
	 * Draws the shape of the graph element contained in this component
	 * according to its graphic attributes.
	 * 
	 * @param g
	 *           the graphics context in which to draw.
	 */
	protected abstract void drawShape(Graphics g);
	
	/**
	 * Used when the shape changed in the datastructure. Makes the painter
	 * create a new shape.
	 */
	protected abstract void recreate()
			throws ShapeNotFoundException;
	
	protected DrawMode getViewDrawMode() {
		if (getParent() instanceof GraffitiView)
			return ((GraffitiView) getParent()).getDrawMode();
		return DrawMode.NORMAL;
	}
	
	/**
	 * Overrides the original method to deal with zoomable containers
	 * It will not call the original repaint, which will call the repaintmanager with
	 * unzoomed coordinates and local coordinates relativ to this graphcomponent
	 * so x and y are always 0
	 * to create the correct repaint region we call the parent frame with new
	 * coordinates, which regard the zoom
	 * the parent will then call its repaintmanager, which will have then the correct
	 * coordinates to paint. This component will then be repainted, because it falls in
	 * the clipping bounds with regard to the current zoom
	 */
	@Override
	public void repaint(long tm, int x, int y, int width, int height) {
		
		Container parent = getParent();
		if (parent != null && parent instanceof Zoomable) {
			double zoomx = getZoom().getScaleX();
			double zoomy = getZoom().getScaleY();
			double newx = (double) (getX()) * zoomx;
			double newy = (double) (getY()) * zoomy;
			double newwidth = (double) (width) * zoomx;
			double newheight = (double) (height) * zoomy;
//			logger.debug("repaint called with new zoomed repaint frame for parent");
			/*
			 * adjusting the width and height with a constant, because somehow the marking border 
			 * on edges doesn't disappear (gets not completely removed).
			 * So the redraw region is widened
			 *
			 */
			int delta = 10; //10 pixels in rescaled (zoomed) space
			parent.repaint(tm, (int) newx, (int) newy, (int) newwidth + delta, (int) newheight + delta);
		}
		else
			super.repaint(tm, x, y, width, height);
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
