package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.connected_components;

import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.vecmath.Vector2d;

import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.algorithm.PreconditionException;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.EdgeHelper;

public class ConnectedComponentLayout extends AbstractAlgorithm {
	
	@Override
	public boolean isLayoutAlgorithm() {
		return true;
	}
	
	@Override
	public void check() throws PreconditionException {
		if (graph == null)
			throw new PreconditionException("Cannot layout graph components, graph is null!");
	}
	
	public String getName() {
		return "Layout unconnected subgraphs on Grid";
	}
	
	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(Category.GRAPH, Category.LAYOUT));
	}
	
	@Override
	public String getDescription() {
		return "<html>Layouts all subgraphs onto a grid, sorted by subgraph size";
	}
	
	public void execute() {
		
		graph.getListenerManager().transactionStarted(this);
		try {
			ConnectedComponentLayout.layoutConnectedComponents(graph);
		} finally {
			graph.getListenerManager().transactionFinished(this);
		}
		// GraphHelper.issueCompleteRedrawForGraph(graph);
		
	}
	
	@SuppressWarnings("unchecked")
	public static void layoutConnectedComponents(Graph graph) {
		
		if (graph.getNumberOfNodes() < 2)
			return;
		
		final double nodeSpacingMinimumX = 100;
		final double nodeSpacingMinimumY = 100;
		final double nodeSpacingFactorX = 0.1;
		final double nodeSpacingFactorY = 0.1;
		
		// #######################################################################################
		// preprocessing
		
		// get node sets
		Set<Set<Node>> nodeSetSet = GraphHelper.getConnectedComponents(graph.getNodes());
		
		if (nodeSetSet.size() <= 1)
			return;
		
		Set<Node>[] nodeSetArray = nodeSetSet.toArray(new Set[nodeSetSet.size()]);
		
		// get node set bounds
		RectangleIn2dSpace[] nodeSetBoundsWithAnchor = new RectangleIn2dSpace[nodeSetArray.length];
		for (int i = 0; i < nodeSetArray.length; i++)
			nodeSetBoundsWithAnchor[i] = ConnectedComponentLayout.getBoundsOfNodes(nodeSetArray[i], nodeSpacingFactorX,
					nodeSpacingFactorY, nodeSpacingMinimumX, nodeSpacingMinimumY);
		
		// bubble sort by node count
		for (int i = 1; i < nodeSetArray.length; i++)
			for (int j = 0; j < nodeSetArray.length - i; j++)
				if (nodeSetArray[j].size() < nodeSetArray[j + 1].size()) {
					Set<Node> s = nodeSetArray[j + 1];
					nodeSetArray[j + 1] = nodeSetArray[j];
					nodeSetArray[j] = s;
					
					RectangleIn2dSpace s1 = nodeSetBoundsWithAnchor[j + 1];
					nodeSetBoundsWithAnchor[j + 1] = nodeSetBoundsWithAnchor[j];
					nodeSetBoundsWithAnchor[j] = s1;
				}
			
		// #######################################################################################
		// arrange node sets
		Vector2d[] nodeSetOffsets = new Vector2d[nodeSetArray.length];
		nodeSetOffsets[0] = new Vector2d(0, 0);
		
		Vector2d[] bestNodeSetOffsets = null;
		double bestScore = 0;
		
		for (int numberOfCCsInFirstLine = 1; numberOfCCsInFirstLine < nodeSetArray.length; numberOfCCsInFirstLine++) {
			// get maximum row Width
			double maxRowWidth = 0;
			double maxRowHeight = nodeSetOffsets[0].y;
			
			for (int i = 0; i < numberOfCCsInFirstLine; i++)
				maxRowWidth += nodeSetBoundsWithAnchor[i].boundX;
			
			double actRowOffsetY = 0;
			double actRowOffsetX = 0;
			
			for (int actCcCount = 0; actCcCount < nodeSetArray.length; actCcCount++) {
				double actEndX = actRowOffsetX + nodeSetBoundsWithAnchor[actCcCount].boundX;
				double actEndY = actRowOffsetY + nodeSetBoundsWithAnchor[actCcCount].boundY;
				
				if (actEndX <= maxRowWidth) {
					// extends current row
					nodeSetOffsets[actCcCount] = new Vector2d(actRowOffsetX, actRowOffsetY);
					
					actRowOffsetX = actEndX;
					
					if (actEndY > maxRowHeight)
						maxRowHeight = actEndY;
				} else {
					// start new row - complete line reset
					
					actRowOffsetX = 0;
					actRowOffsetY = maxRowHeight;
					
					nodeSetOffsets[actCcCount] = new Vector2d(actRowOffsetX, actRowOffsetY);
					
					actRowOffsetX = nodeSetBoundsWithAnchor[actCcCount].boundX;
					maxRowHeight = actEndY;
				}
			}
			
			// assess result and remember the best
			double score = (maxRowWidth > maxRowHeight) ? maxRowHeight / maxRowWidth : maxRowWidth / maxRowHeight;
			
			if (score > bestScore) {
				bestNodeSetOffsets = nodeSetOffsets;
				bestScore = score;
			} else
				// every further result will be worse
				break;
		}
		
		// #######################################################################################
		// assign node position
		for (int i = 0; i < nodeSetArray.length; i++) {
			// safe the bounding rectangle before it's changed by the moveNodes call
			RectangleIn2dSpace rect = ConnectedComponentLayout.getBoundsOfNodes(nodeSetArray[i], nodeSpacingFactorX,
					nodeSpacingFactorY, nodeSpacingMinimumX, nodeSpacingMinimumY);
			
			ConnectedComponentLayout.moveNodes(nodeSetArray[i], bestNodeSetOffsets[i].x, bestNodeSetOffsets[i].y,
					nodeSpacingFactorX, nodeSpacingFactorY, nodeSpacingMinimumX, nodeSpacingMinimumY);
			// move edge bends
			Set<Edge> conComponentEdges = new HashSet<Edge>();
			for (Node n : nodeSetArray[i])
				conComponentEdges.addAll(n.getEdges());
			double shiftX = bestNodeSetOffsets[i].x - rect.offsetX;
			double shiftY = bestNodeSetOffsets[i].y - rect.offsetY;
			for (Edge e : conComponentEdges)
				EdgeHelper.moveBends(e, shiftX, shiftY);
		}
		
	}
	
	/**
	 * @param nodeList
	 * @param xSpacingFactor
	 * @param ySpacingFactor
	 * @param xSpacingMinimum
	 * @param ySpacingMinimum
	 * @return the wrapped bounds of the nodes with spacing
	 */
	private static RectangleIn2dSpace getBoundsOfNodes(Set<Node> nodeList, double xSpacingFactor, double ySpacingFactor,
			double xSpacingMinimum, double ySpacingMinimum) {
		Rectangle2D.Double rect = ConnectedComponentLayout.getBoundsOfNodes(nodeList);
		
		double xSpacing = rect.width * xSpacingFactor;
		double ySpacing = rect.height * ySpacingFactor;
		
		xSpacing = (xSpacing < xSpacingMinimum) ? xSpacingMinimum : xSpacing;
		ySpacing = (ySpacing < ySpacingMinimum) ? ySpacingMinimum : ySpacing;
		
		return new RectangleIn2dSpace(rect.x - xSpacing, rect.y - ySpacing, rect.width + 2 * xSpacing,
				rect.height + 2 * ySpacing);
	}
	
	private static Rectangle2D.Double getBoundsOfNodes(Set<Node> nodeList) {
		double offsetX = Double.MAX_VALUE;
		double offsetY = Double.MAX_VALUE;
		double boundX = Double.NEGATIVE_INFINITY;
		double boundY = Double.NEGATIVE_INFINITY;
		
		for (Node node : nodeList) {
			double x = node.getDouble(GraphicAttributeConstants.COORDX_PATH);
			double y = node.getDouble(GraphicAttributeConstants.COORDY_PATH);
			double width = node.getDouble(GraphicAttributeConstants.DIMW_PATH);
			double height = node.getDouble(GraphicAttributeConstants.DIMH_PATH);
			
			// get top-left point instead of center point
			x = x - width / 2;
			y = y - height / 2;
			double xr = x + width;
			double yb = y + height;
			
			offsetX = (x < offsetX) ? x : offsetX;
			offsetY = (y < offsetY) ? y : offsetY;
			boundX = (xr > boundX) ? xr : boundX;
			boundY = (yb > boundY) ? yb : boundY;
		}
		
		boundX -= offsetX;
		boundY -= offsetY;
		
		return new Rectangle2D.Double(offsetX, offsetY, boundX, boundY);
	}
	
	/**
	 * moves all nodes in the list
	 * 
	 * @param nodeList
	 * @param xShift
	 * @param yShift
	 */
	private static void moveNodes(Set<Node> nodeList, double xShift, double yShift) {
		for (Node node : nodeList) {
			double x = node.getDouble(GraphicAttributeConstants.COORDX_PATH);
			double y = node.getDouble(GraphicAttributeConstants.COORDY_PATH);
			
			double xNew = x + xShift;
			double yNew = y + yShift;
			
			node.setDouble(GraphicAttributeConstants.COORDX_PATH, xNew);
			node.setDouble(GraphicAttributeConstants.COORDY_PATH, yNew);
		}
	}
	
	private static void moveNodes(Set<Node> nodeList, double offsetX, double offsetY, double xSpacingFactor,
			double ySpacingFactor, double xSpacingMinimum, double ySpacingMinimum) {
		RectangleIn2dSpace rect = ConnectedComponentLayout.getBoundsOfNodes(nodeList, xSpacingFactor, ySpacingFactor,
				xSpacingMinimum, ySpacingMinimum);
		double shiftX = offsetX - rect.offsetX;
		double shiftY = offsetY - rect.offsetY;
		
		ConnectedComponentLayout.moveNodes(nodeList, shiftX, shiftY);
	}
	
	private static class RectangleIn2dSpace {
		
		public final double offsetX;
		public final double offsetY;
		public final double boundX;
		public final double boundY;
		
		public RectangleIn2dSpace(double offsetX, double offsetY, double boundX, double boundY) {
			this.offsetX = offsetX;
			this.offsetY = offsetY;
			
			this.boundX = boundX;
			this.boundY = boundY;
		}
		
		@Override
		public String toString() {
			return "offset (" + this.offsetX + ",\t" + this.offsetY + ")\t + (" + this.boundX + ",\t" + this.boundY
					+ ")";
		}
	}
}
