/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
// ==============================================================================
//
// GraffitiView.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id$

package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.RepaintManager;

import org.AttributeHelper;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.Release;
import org.ReleaseInfo;
import org.Vector2df;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.event.AttributeEvent;
import org.graffiti.event.GraphEvent;
import org.graffiti.event.TransactionEvent;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.options.OptionPane;
import org.graffiti.options.PreferencesInterface;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.IntegerParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.view.GraphElementComponent;
import org.graffiti.plugins.views.defaults.DrawMode;
import org.graffiti.plugins.views.defaults.GraffitiView;
import org.graffiti.plugins.views.defaults.NodeComponent;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.cluster_colors.ClusterColorAttribute;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands.PajekClusterColor;

/**
 * An implementation of <code>org.graffiti.plugin.view.View2D</code>, that
 * displays a graph. Since it also shows changes in the graph it listens for
 * changes in the graph, attributes, nodes and edges.
 * 
 * @see javax.swing.JPanel
 * @see org.graffiti.plugin.view.View2D
 */
public class IPKGraffitiView extends GraffitiView
		implements OptionPane, Printable, PaintStatusSupport, PreferencesInterface {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(IPKGraffitiView.class);

	static {
		logger.setLevel(Level.INFO);
	}

	ClusterBackgroundDraw gcbd = new ClusterBackgroundDraw();
	boolean dirty = true;

	private BackgroundTaskStatusProviderSupportingExternalCall optStatus;

	private static boolean useAntialiasing = true;

	/*
	 * Static variables and they key-names that store values of the Class Preference
	 * Parameters Will be set during bootup and update by the preferencemanager
	 */
	public static String PARAM_MAX_NODES = "Maximum Nodes";
	private static int PARAMDEFVAL_MAX_NODES = 300;
	private static int MAX_NODES;

	public static String PARAM_MAX_EDGES = "Maximum Edges";
	private static int PARAMDEFVAL_MAX_EDGES = 300;
	private static int MAX_EDGES;

	public static String PARAM_DRAW_GRID = "Draw Grid";
	private static boolean PARAMDEFVAL_DRAW_GRID = false;
	private static boolean DRAW_GRID;

	public IPKGraffitiView() {
		super();
		// GravistoService.getInstance().addKnownOptionPane(IPKGraffitiView.class,
		// this);
		setBorder(null);

	}

	@Override
	public List<Parameter> getDefaultParameters() {
		ArrayList<Parameter> arrayList = new ArrayList<Parameter>();
		arrayList.add(new IntegerParameter(PARAMDEFVAL_MAX_NODES, PARAM_MAX_NODES,
				"Maximum Nodes until antialiasing is turned off"));
		arrayList.add(new IntegerParameter(PARAMDEFVAL_MAX_EDGES, PARAM_MAX_EDGES,
				"Maximum Edges until antialiasing is turned off"));
		arrayList.add(new BooleanParameter(PARAMDEFVAL_DRAW_GRID, PARAM_DRAW_GRID, "Enable / Disable grid drawing"));
		return arrayList;

	}

	@Override
	public void updatePreferences(Preferences preferences) {
		logger.debug("updating preferences");
		MAX_NODES = preferences.getInt(PARAM_MAX_NODES, PARAMDEFVAL_MAX_NODES);
		MAX_EDGES = preferences.getInt(PARAM_MAX_EDGES, PARAMDEFVAL_MAX_EDGES);
		DRAW_GRID = preferences.getBoolean(PARAM_DRAW_GRID, PARAMDEFVAL_DRAW_GRID);

		/*
		 * Show changed of preferences immediately Since the method usually is called by
		 * the PreferenceManager, it will have another own instance created to update
		 * the preferences. Any calls made to method of that instance are not received
		 * by the actual session-active IPKGraffitiViews This actual session-active View
		 * must be retrieved and separately used.
		 */
		if (MainFrame.getInstance().getActiveSession() != null)
			((IPKGraffitiView) MainFrame.getInstance().getActiveSession().getActiveView()).repaint();

	}

	/**
	 * @vanted.revision 2.7.0 How it shows in Preferences
	 */
	@Override
	public String getPreferencesAlternativeName() {
		return "Network View";
	}

	/**
	 * This methods sets the graph and adds the background color attribute, since
	 * this View supports background coloring
	 */
	@Override
	public void setGraph(Graph g) {
		super.setGraph(g);
		if (g != null) {
			Color c = AttributeHelper.getColorFromAttribute(currentGraph, "", "graphbackgroundcolor", Color.white);
			AttributeHelper.setColorFromAttribute(g, "", "graphbackgroundcolor", c);
			/*
			 * in any case (background was (not) present before set the graph to not
			 * modified only, if the attribute changes during the session
			 */
			g.setModified(false);
		}
		dirty = true;
	}

	@Override
	protected NodeComponent createNodeComponent(Map<GraphElement, GraphElementComponent> gecMap, Node node) {

		NodeComponent nodeComponent = (NodeComponent) getGraphElementComponent(node);

		if (isHidden(node)) {
			if (nodeComponent != null)
				processNodeRemoval(node);
			return null;
		}

		if (nodeComponent == null) {
			nodeComponent = (NodeComponent) gecMap.get(node);
			if (nodeComponent == null)
				nodeComponent = new IPKnodeComponent(node);
			// nodeComponent = IPKnodeComponent.getNewAndMatchingNodeComponent(node,
			// currentGraph);
		}

		nodeComponent.clearDependentComponentList();
		gecMap.put(node, nodeComponent);

		return nodeComponent;
	}

	@Override
	protected void paintComponent(Graphics g) {
		// logger.debug("paintcomponent");
		// synchronized (this) {

		RepaintManager repaintManager = RepaintManager.currentManager(this);
		repaintManager.setDoubleBufferingEnabled(true);
		// useAntialiasing = true;
		// if (useAntialiasing) {

		// } else {
		// ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		// RenderingHints.VALUE_ANTIALIAS_OFF);
		// ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
		// RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		// }
		super.paintComponent(g);
		// }
	}

	@Override
	public void paint(Graphics g) {
		// Rectangle clipBounds = g.getClipBounds();
		// logger.debug("called paint");
		// long startTime=System.currentTimeMillis();
		// logger.debug("start paint cliprect("+clipBounds.x+" : "+clipBounds.y+" |
		// "+clipBounds.width+" : "+clipBounds.height+")");

		if (!printInProgress)
			((Graphics2D) g).transform(zoom);

		//
		setRenderingHints(g);

		if (!printInProgress && drawMode == DrawMode.NORMAL)
			drawBackground(g);

		if (DRAW_GRID)
			drawGrid(g);

		super.paint(g);
		// long lastPaintTime=System.currentTimeMillis()-startTime;
		// if (lastPaintTime>maxDrawTime) {
		// logger.debug(" end paint ..Redraw time: "+lastPaintTime+"ms");
		// }
	}

	/**
	 * @param g
	 */
	private void drawGrid(Graphics g) {
		logger.debug("ipkgraffitiview: w:" + getWidth() + ", h: " + getHeight());

		Rectangle visibleRect = getVisibleRect();

		logger.debug("physical pixel area visible x: " + visibleRect.getX() + " y: " + visibleRect.getY() + " w: "
				+ visibleRect.getWidth() + " h: " + visibleRect.getHeight());
		/*
		 * create grid every 50 pixles
		 */
		int STEP = 50;
		int startx = 0;// (int) (visibleRect.getX() % STEP);
		int starty = 0;// (int) (visibleRect.getY() % STEP);
		int curX = startx;
		int curY = starty;

		g.setColor(Color.LIGHT_GRAY);

		int zoomedwidth = (int) ((visibleRect.getX() + visibleRect.getWidth()) * 1 / getZoom().getScaleX());
		int zoomedheight = (int) ((visibleRect.getY() + visibleRect.getHeight()) * 1 / getZoom().getScaleX());
		logger.debug("zoomedwidth " + zoomedwidth + " zoomedheight " + zoomedheight);
		while (curX < zoomedwidth) {

			g.drawLine(curX, 0, curX, zoomedheight);

			if (curX % 100 == 0) {
				g.drawString("" + curX, curX + 5, (int) ((visibleRect.getY() + 20) / zoom.getScaleX()));
			}

			curX += STEP;
		}
		while (curY < zoomedheight) {

			g.drawLine(0, curY, zoomedwidth, curY);

			if (curY % 100 == 0) {
				g.drawString("" + curY, (int) ((visibleRect.getX() + 5) / zoom.getScaleX()), curY + 10);
			}

			curY += STEP;
		}

	}

	@Override
	public void repaint() {
		// logger.debug("issuing repaint");
		super.repaint();
	}

	/**
	 * Depending on the given DRAWMODE, Antialiasing is enabled or disabled
	 * 
	 * @param g
	 */
	private void setRenderingHints(Graphics g) {
		if (printInProgress || (drawMode == DrawMode.NORMAL && getGraph().getNumberOfNodes() < MAX_NODES
				&& getGraph().getNumberOfEdges() < MAX_EDGES)) {
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			// ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
			// RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			// ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
			// RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BICUBIC);

			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
					RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
		}
	}

	private void drawBackground(Graphics g) {
		if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR && AttributeHelper.hasAttribute(getGraph(),
				ClusterColorAttribute.attributeFolder, ClusterColorAttribute.attributeName)) {
			Boolean enablebackground = (Boolean) AttributeHelper.getAttributeValue(getGraph(), "",
					"background_coloring", Boolean.valueOf(false), Boolean.valueOf(false), true);
			Boolean clearOuter = (Boolean) AttributeHelper.getAttributeValue(getGraph(), "",
					"clusterbackground_fill_outer_region", Boolean.valueOf(false), Boolean.valueOf(false), true);
			Boolean spaceFill = (Boolean) AttributeHelper.getAttributeValue(getGraph(), "",
					"clusterbackground_space_fill", Boolean.valueOf(true), Boolean.valueOf(true), true);
			if (enablebackground) {
				Double radius = (Double) AttributeHelper.getAttributeValue(getGraph(), "", "clusterbackground_radius",
						Double.valueOf(200), Double.valueOf(200), true);
				Double alpha = (Double) AttributeHelper.getAttributeValue(getGraph(), "", "clusterbackground_low_alpha",
						Double.valueOf(0.2), Double.valueOf(0.2), true);
				Double grid = (Double) AttributeHelper.getAttributeValue(getGraph(), "", "clusterbackground_grid",
						Double.valueOf(50), Double.valueOf(50), true);
				drawClusterBackground(g, g.getClipBounds(), grid.intValue(), !clearOuter, radius.intValue(),
						alpha.floatValue(), spaceFill);
			}
		}
	}

	private void drawClusterBackground(Graphics g, Rectangle clipBounds, float mesh, boolean outClear, int out,
			float lowAlpha, boolean spaceFill) {
		if (lowAlpha < 0f)
			lowAlpha = 0f;
		if (lowAlpha > 1f)
			lowAlpha = 1f;
		if (out < 0)
			out = 0;
		// int xs = 50;
		// g.setColor(Color.BLUE);
		// for (int i=0; i<10; i++) {
		// g.drawLine(i*xs, 0, i*xs, 500);
		// g.drawLine(0, i*xs, 500, i*xs);
		// }
		// g.drawString("paint ("+g.getClipBounds().x+" : "+g.getClipBounds().y+" |
		// "+g.getClipBounds().width+
		// " : "+g.getClipBounds().height+")",
		// 50, 50);

		if (dirty) {
			dirty = false;
			gcbd = new ClusterBackgroundDraw();
			gcbd.init(getGraph());
		}

		ClusterBackgroundDraw cbd = gcbd;

		boolean image = false;
		if (optStatus != null && image)
			optStatus.setCurrentStatusText2("Scan node positions...");
		if (clipBounds == null || printInProgress) {
			if (!printInProgress)
				clipBounds = new Rectangle(0, 0, (int) cbd.maxX + 500, (int) cbd.maxY + 500);
			if (mesh > 5)
				mesh = 5;
			image = true;
		}
		boolean meshResizeAllowed = true;
		if (mesh < 0) {
			mesh = -mesh;
			meshResizeAllowed = false;
		}
		if (mesh < 1)
			mesh = 1;
		float xstart = clipBounds.x - clipBounds.x % mesh;
		float ystart = clipBounds.y - clipBounds.y % mesh;
		float xend = clipBounds.x + clipBounds.width + mesh;
		float yend = clipBounds.y + clipBounds.height + mesh;
		long startTime = System.currentTimeMillis();

		if (optStatus != null && image)
			optStatus.setCurrentStatusText2("Draw colorized background...");

		for (float x = xstart; x <= xend + mesh; x = x + mesh) {
			if (optStatus != null && image)
				optStatus.setCurrentStatusValueFine((x - xstart) / (xend - xstart) * 100d);
			for (float y = ystart; y <= yend + mesh; y = y + mesh) {
				double minDistance = Double.MAX_VALUE;
				Node minNode = null;
				for (Map.Entry<Node, Vector2df> e : cbd.node2position.entrySet()) {
					Vector2df p = e.getValue();
					float dist = (float) Math.sqrt((p.x - x) * (p.x - x) + (p.y - y) * (p.y - y));
					if (dist < minDistance) { // && (cbd.node2cluster.get(e.getKey()).length()>0)) {
						minNode = e.getKey();
						minDistance = dist;
					}
				}
				if (minDistance > out && outClear) {
					double dL = x - cbd.minX;
					double dT = y - cbd.minY;
					double dR = cbd.maxX - x;
					double dB = cbd.maxY - y;
					if (minDistance > getPositiveMin(dL, dT, dR, dB))
						continue;
				}
				if (!spaceFill && minDistance > out)
					continue;

				Color targetColor = null;
				if (cbd.node2cluster.containsKey(minNode)) {
					String cluster = cbd.node2cluster.get(minNode);
					targetColor = cbd.cluster2color.get(cluster);
					if (targetColor == null)
						targetColor = getBackground();
				}

				if (targetColor != null) {
					if (lowAlpha < 1f)
						targetColor = getTargetColor(targetColor, minDistance, out, 1f, lowAlpha);
					g.setColor(targetColor);

					g.fillRect((int) (x - mesh / 2f), (int) (y - mesh / 2f), (int) mesh, (int) mesh);
				}
			}
			long currTime = System.currentTimeMillis();
			int maxTime = (mesh < 50 ? 20 : 100);
			if (currTime - startTime > maxTime && !image && meshResizeAllowed) {
				x -= mesh / 2 + 1;
				mesh = mesh * 2;
				startTime = System.currentTimeMillis();
				MainFrame.showMessage(
						"Mesh size has been temporarily increased to " + mesh + " to improve performance.",
						MessageType.INFO);
			}
		}
		if (optStatus != null && image) {
			optStatus.setCurrentStatusValueFine(100);
			optStatus.setCurrentStatusText2("Painting network...");
		}

		// g.setColor(Color.BLUE);
		// g.drawRect((int)cbd.minX, (int)cbd.minY, (int)(cbd.maxX-cbd.minX),
		// (int)(cbd.maxY-cbd.minY));

	}

	private static Color getTargetColor(Color c, double dist, double maxd, float src, float tgt) {
		float alpha;
		if (dist < maxd)
			alpha = (float) ((maxd - dist) / maxd * (src - tgt) + tgt);
		else
			alpha = tgt;
		return new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (alpha * 255));
	}

	@Override
	public void postAttributeChanged(AttributeEvent e) {
		super.postAttributeChanged(e);
		// dirty = true;
		PajekClusterColor.executeClusterColoringOnGraph(getGraph());
	}

	@Override
	public void postNodeAdded(GraphEvent e) {
		dirty = true;
		super.postNodeAdded(e);
	}

	@Override
	public void postNodeRemoved(GraphEvent e) {
		dirty = true;
		super.postNodeRemoved(e);
	}

	@Override
	public void transactionFinished(TransactionEvent event, BackgroundTaskStatusProviderSupportingExternalCall status) {
		dirty = true;
		super.transactionFinished(event, status);
	}

	@Override
	public void close() {
		super.close();
		optStatus = null;
		gcbd = null;
	}

	private static double getPositiveMin(double dl, double dt, double dr, double db) {
		double min = Double.MAX_VALUE;
		dl = Math.abs(dl);
		dt = Math.abs(dt);
		dr = Math.abs(dr);
		db = Math.abs(db);
		if (dl < min)
			min = dl;
		if (dt < min)
			min = dt;
		if (dr < min)
			min = dr;
		if (db < min)
			min = db;
		return min;
	}

	public static boolean getUseAntialiasingSetting() {
		return useAntialiasing;
	}

	public JComponent getOptionDialogComponent() {
		JPanel options = new JPanel();

		double border = 5;
		double[][] size = { { border, TableLayoutConstants.FILL, border }, // Columns
				{ border, TableLayoutConstants.PREFERRED, TableLayoutConstants.FILL, border } }; // Rows

		options.setLayout(new TableLayout(size));

		JCheckBox checkBoxUseAntiAliasing = new JCheckBox("Use Anti-Aliasing", useAntialiasing);

		options.putClientProperty("checkBoxUseAntiAliasing", checkBoxUseAntiAliasing);

		options.add(checkBoxUseAntiAliasing, "1,1");
		options.revalidate();

		return options;
	}

	public void init(JComponent options) {
		JCheckBox checkBoxUseAntiAliasing = (JCheckBox) options.getClientProperty("checkBoxUseAntiAliasing");
		checkBoxUseAntiAliasing.setSelected(useAntialiasing);
	}

	@Override
	public String getViewName() {
		return getOptionName();
	}

	public void save(JComponent options) {
		JCheckBox checkBoxUseAntiAliasing = (JCheckBox) options.getClientProperty("checkBoxUseAntiAliasing");
		useAntialiasing = checkBoxUseAntiAliasing.isSelected();
		GravistoService.getInstance().getMainFrame().repaint();
	}

	public String getCategory() {
		return "View";
	}

	public String getOptionName() {
		return "Network View (default)";
	}

	public boolean printInProgress = false;

	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
		synchronized (this) {
			System.out.println("Printing Page " + pageIndex + ", Page Orientation: " + pageFormat.getOrientation());
			Graphics2D g2 = (Graphics2D) graphics;
			Dimension d = getViewComponent().getPreferredSize();
			d.width = (int) (d.width / getZoom().getScaleX());
			d.height = (int) (d.height / getZoom().getScaleY());
			double panelWidth = d.width; // width in pixels
			double panelHeight = d.height; // height in pixels
			double pageHeight = pageFormat.getImageableHeight(); // height of printer page
			double pageWidth = pageFormat.getImageableWidth(); // width of printer page
			double scale1 = pageWidth / panelWidth;
			double scale2 = pageHeight / panelHeight;
			double scale = min2(scale1, scale2);
			int numPages = 1; // (int) Math.ceil(scale * panelHeight / pageHeight);
			int response;
			if (pageIndex >= numPages) {
				response = NO_SUCH_PAGE;
			} else {
				g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
				g2.translate(0f, -pageIndex * pageHeight);
				g2.scale(scale, scale);
				printInProgress = true;
				paint(g2);
				printInProgress = false;
				response = Printable.PAGE_EXISTS;
			}
			return response;
		}
	}

	/**
	 * @param smallestX Value 1
	 * @param cx        Value 2
	 * @return The smaller one of the parameters
	 */
	private static double min2(double smallestX, double cx) {
		return smallestX < cx ? smallestX : cx;
	}

	public void setStatusProvider(BackgroundTaskStatusProviderSupportingExternalCall optStatus) {
		this.optStatus = optStatus;
	}

	public boolean statusDrawInProgress() {
		return optStatus != null;
	}

}
