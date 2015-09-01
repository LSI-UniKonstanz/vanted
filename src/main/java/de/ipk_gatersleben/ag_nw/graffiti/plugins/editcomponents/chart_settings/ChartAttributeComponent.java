/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.chart_settings;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JComponent;

import org.AttributeHelper;
import org.apache.log4j.Logger;
import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.graph.Edge;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.graphics.CoordinateAttribute;
import org.graffiti.graphics.DimensionAttribute;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.managers.PreferenceManager;
import org.graffiti.options.PreferencesInterface;
import org.graffiti.plugin.attributecomponent.AbstractAttributeComponent;
import org.graffiti.plugin.parameter.IntegerParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.view.ShapeNotFoundException;
import org.graffiti.plugins.views.defaults.DrawMode;
import org.graffiti.plugins.views.defaults.GraffitiView;
import org.vanted.VantedPreferences;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.viewcomponents.EdgeComponentHelper;

public class ChartAttributeComponent extends AbstractAttributeComponent
		implements GraphicAttributeConstants, PreferencesInterface {
	private static final long serialVersionUID = 1L;
	
	static Logger logger = Logger.getLogger(ChartAttributeComponent.class);
	
	private static int MINSIZE_VISIBILITY;
	public static String PREF_MINSIZE_VISIBILITY="min-size visibility";
	
	
	/**
	 * Flatness value used for the <code>PathIterator</code> used to place
	 * labels.
	 */
	protected final double flatness = 1.0d;
	private Point loc = new Point();
	
	BufferedImage bufferedImage;
	
	
	
	@Override
	public List<Parameter> getDefaultParameters() {
		List<Parameter> params = new ArrayList<Parameter>();
		params.add(new IntegerParameter(10, PREF_MINSIZE_VISIBILITY, "<html>Minimum size of width/height, that this <br/>component will be visible during painting"));
		return params;
	}

	@Override
	public void updatePreferences(Preferences preferences) {
		MINSIZE_VISIBILITY = preferences.getInt(PREF_MINSIZE_VISIBILITY, 10);
	}

	@Override
	public String getPreferencesAlternativeName() {
		// TODO Auto-generated method stub
		return "Charts";
	}

	@Override
	public void attributeChanged(Attribute attr) throws ShapeNotFoundException {
		GraphElement ge = (GraphElement) this.attr.getAttributable();
		if (ge instanceof Node) {
			Node n = (Node) ge;
			if (attr.getPath().startsWith(Attribute.SEPARATOR + GRAPHICS + Attribute.SEPARATOR + COORDINATE)) {
				setLocation(shift.x, shift.y + getCurrentLabelShift());
			} else
				if (attr instanceof CollectionAttribute) {
					if (attr.getPath().equals("")) {
						changeParameters(((CollectionAttribute) attr).getCollection().get(GRAPHICS), n);
					} else
						if (attr.getPath().equals(GRAPHICS)) {
							changeParameters(attr, n);
						} else {
							recreate();
						}
				} else
					if (attr.getId().equals("component")) {
						recreate();
					} else {
						recreate();
					}
		} else {
			// System.out.println("TODO: Process Attribute Change: "+attr.getPath()+attr.getName());
		}
	}
	
	private static Integer defaultSizeForEdges = new Integer(80);
	
	@Override
	public synchronized void recreate() throws ShapeNotFoundException {
		GraphElement ge = (GraphElement) this.attr.getAttributable();
		
		setOpaque(false);
		setBackground(null);
		
		removeAll();
		double border = 0;
		double[][] size = { { border, TableLayoutConstants.FILL, border }, // Columns
				{ border, TableLayoutConstants.FILL, border } }; // Rows
		
		setLayout(new TableLayout(size));
		
		String ct = (String) attr.getValue();
		if(ct.equals("hidden"))
			setVisible(false);
		else
			setVisible(true);
		
		JComponent chart = ChartComponentManager.getInstance().getChartComponent(ct, ge);
		if (chart != null)
			add(chart, "1,1");
		
		if (ge instanceof Node) {
			setSize(getLabelTransformedSize(AttributeHelper.getSizeD((Node) ge)));
			setLocation(shift.x, shift.y + getCurrentLabelShift());
		}
		if (ge instanceof Edge) {
			Edge e = (Edge) ge;
			Integer sx = (Integer) AttributeHelper.getAttributeValue(e, "charting", "chartSizeX", defaultSizeForEdges, defaultSizeForEdges, true);
			Integer sy = (Integer) AttributeHelper.getAttributeValue(e, "charting", "chartSizeY", defaultSizeForEdges, defaultSizeForEdges, true);
			if (sx == null || sx < 0)
				sx = defaultSizeForEdges;
			if (sy == null || sy < 0)
				sy = defaultSizeForEdges;
			setSize(sx, sy);
			EdgeComponentHelper.updatePositionForEdgeMapping(getAttribute(), geShape,
					ChartAttribute.CHARTPOSITION,
					"mapping",
					flatness, getWidth(), getHeight(), loc);
			setLocation((int) (loc.getX() + shift.getX()), (int) (loc.getY() + shift.getY()));
		}
		
		synchronized (getTreeLock()) {
			if (isShowing())
				validate();
			else
				validateTree();
		}
		
		if(getWidth() > 0 && getHeight() > 0) {
//			logger.debug("recreating chartimage with w h" + getWidth()+" "+getHeight());
			bufferedImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
		}
		if(bufferedImage != null) {
			Graphics graphics2 = bufferedImage.getGraphics();
			super.print(graphics2);
		}
		
	}
	
	private boolean isLabelTop() {
		String lp = AttributeHelper.getLabelPosition(attr.getAttributable());
		if (lp != null && lp.equals("t"))
			return true;
		return false;
	}
	
	private boolean isLabelTopOrBottom() {
		String lp = AttributeHelper.getLabelPosition(attr.getAttributable());
		if (lp != null && (lp.equals("t") || lp.equals("b")))
			return true;
		return false;
	}
	
	private Dimension getLabelTransformedSize(Dimension sizeD) {
		Dimension result = new Dimension(sizeD.width,
				sizeD.height - getCurrentLabelHeight(isLabelTopOrBottom()
						));
		return result;
	}
	
	private int getCurrentLabelHeight(boolean isBottomOrTop) {
		if (!isBottomOrTop)
			return 0;
		else
			return AttributeHelper.getLabel(-1, (Node) attr.getAttributable()).getLastComponentHeight();
	}
	
	private int getCurrentLabelShift() {
		if (isLabelTop())
			return getCurrentLabelHeight(true);
		else
			return 0;
	}
	
	private void changeParameters(Object graphicsAttr, GraphElement n)
			throws ShapeNotFoundException {
		if ((graphicsAttr != null)
				&& (graphicsAttr instanceof CollectionAttribute)) {
			CollectionAttribute cAttr = (CollectionAttribute) graphicsAttr;
			Object annotationObject = cAttr.getCollection().get("component");
			
			if ((annotationObject != null)
					&& (annotationObject instanceof ChartAttribute)) {
				recreate();
			}
			
			Object coordinateObject = cAttr.getCollection().get(COORDINATE);
			Object dimensionObject = cAttr.getCollection().get(DIMENSION);
			
			if (((coordinateObject != null) && (coordinateObject instanceof CoordinateAttribute))
					|| ((dimensionObject != null) && (dimensionObject instanceof DimensionAttribute))) {
				setLocation(shift.x, shift.y + getCurrentLabelShift());
			}
		} else {
			System.out.println("TODO: FIX: Changed Parameters not recognized!");
			recreate();
		}
	}
	
	@Override
	public void paint(Graphics g) {
		if(isPaintingForPrint()) {
//			logger.debug("paint for bufferedimage");
			super.paint(g);
		} else if(checkVisibility(MINSIZE_VISIBILITY)) {

			/*
			 * Only for debugging
			 */
//			if(PreferenceManager.getPreferenceForClass(VantedPreferences.class).getBoolean(VantedPreferences.PREFERENCE_DEBUG_SHOWPANELFRAMES, false)) {
//				g.setColor(Color.BLUE);
//				g.drawRect(0, 0, getWidth()-1, getHeight()-1);
//			}
			
			if (getDrawingModeOfView() == DrawMode.REDUCED) {
				g.drawImage(bufferedImage, 0, 0, null);
			}  else
				super.paint(g);
		}
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.view.AttributeComponent#adjustComponentSize()
	 */
	@Override
	public void adjustComponentSize() {
		//
		
	}
	
}
