package de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;

import org.Vector2d;
import org.graffiti.graphics.DimensionAttribute;
import org.graffiti.graphics.NodeGraphicAttribute;
import org.graffiti.plugins.views.defaults.PolygonalNodeShape;

public abstract class RelativePolyShape extends PolygonalNodeShape {
	
	protected int offX = 0;
	protected int offY = 0;
	protected int addSx = 0;
	protected int addSy = 0;
	
	@Override
	public void buildShape(NodeGraphicAttribute graphics) {
		this.nodeAttr = graphics;
		
		DimensionAttribute dim = graphics.getDimension();
		double w = dim.getWidth();
		double h = dim.getHeight();
		
		double rr = graphics.getRoundedEdges();
		if (rr * 2 > w)
			rr = w / 2;
		if (rr * 2 > h)
			rr = h / 2;
		this.roundingRadius = rr / w;
		
		// w+=addSx;
		// h+=addSx;
		
		double ft = graphics.getFrameThickness();
		double offset = ft / 2d;
		int os = (int) Math.round(offset);
		
		Collection<Vector2d> relativePositions = getRelativePointPositions();
		
		int nr = relativePositions.size();
		int[] xcoord = new int[nr];
		int[] ycoord = new int[nr];
		
		int i = 0;
		for (Vector2d rp : relativePositions) {
			if (rp.x > 0)
				xcoord[i] = (int) Math.round((rp.x * w));
			else
				xcoord[i] = (int) Math.round((-rp.x * w));
			if (rp.y > 0)
				ycoord[i] = (int) Math.round((rp.y * h));
			else {
				if (rp.y < -0.5)
					ycoord[i] = (int) Math.round((h - (1 + rp.y) * w));
				else
					ycoord[i] = (int) Math.round((-rp.y * w));
			}
			
//			if(xcoord[i] == 0)
//				xcoord[i]++;
//			if(xcoord[i] == w)
//				xcoord[i]--;
//			if(ycoord[i] == 0)
//				ycoord[i]++;
//			if(ycoord[i] == h)
//				ycoord[i]--;
			
			i++;
		}
		
		polygon = new Polygon(xcoord, ycoord, nr);
		polygon.translate(os + offX, os + offY);
		
		Rectangle bounds = polygon.getBounds();
		
		if (Double.compare(Math.floor(offset), offset) == 0) {
			w = w + ft + 1;
			h = h + ft + 1;
		} else {
			w += ft;
			h += ft;
		}
		
		/*
		 * if we don't increment the size if the framethickness
		 * is smaller 2, somehow it's not completely drawn
		 */
		if (ft < 2.0) {
			w++;
			h++;
		}
		
//		setThickShape(bounds.getWidth() + os + addSx + graphics.getFrameThickness() / 2, bounds.getHeight() + os + addSy + graphics.getFrameThickness() / 2);
//		setThickShape(bounds.getWidth() + os + addSx + Math.ceil(graphics.getFrameThickness() / 2), bounds.getHeight() + os + addSy + Math.ceil(graphics.getFrameThickness() / 2));
		setThickShape(w + addSx, h + addSy);
	}
	
	protected abstract Collection<Vector2d> getRelativePointPositions();
	
	protected int roundingSimulationSteps = 10;
	protected double roundingRadius = 0.1;
	
	protected Collection<Vector2d> getRoundingRightBottom() {
		ArrayList<Vector2d> points = new ArrayList<Vector2d>();
		points.add(new Vector2d(1, -1 + roundingRadius));
		double xc = 1 - roundingRadius;
		double yc = 1 - roundingRadius;
		double singleStep = Math.PI / 2 / roundingSimulationSteps;
		for (int i = 1; i < roundingSimulationSteps; i++) {
			int step = roundingSimulationSteps - i;
			double x = Math.sin(singleStep * step) * roundingRadius + xc;
			double y = Math.cos(singleStep * step) * roundingRadius + yc;
			points.add(new Vector2d(x, -y));
		}
		points.add(new Vector2d(1 - roundingRadius, 1));
		return points;
	}
	
	protected Collection<Vector2d> getRoundingLeftBottom() {
		ArrayList<Vector2d> points = new ArrayList<Vector2d>();
		points.add(new Vector2d(roundingRadius, 1));
		double xc = roundingRadius;
		double yc = 1 - roundingRadius;
		double singleStep = Math.PI / 2 / roundingSimulationSteps;
		for (int i = 1; i < roundingSimulationSteps; i++) {
			int step = -i;
			double x = Math.sin(singleStep * step) * roundingRadius + xc;
			double y = Math.cos(singleStep * step) * roundingRadius + yc;
			points.add(new Vector2d(x, -y));
		}
		points.add(new Vector2d(0, -1 + roundingRadius));
		return points;
	}
	
	protected Collection<Vector2d> getRoundingTopLeft() {
		ArrayList<Vector2d> points = new ArrayList<Vector2d>();
		double xc = roundingRadius;
		double yc = roundingRadius;
		double singleStep = Math.PI / 2 / roundingSimulationSteps;
		for (int i = 1; i < roundingSimulationSteps; i++) {
			int step = -i - roundingSimulationSteps + 1;
			double x = Math.sin(singleStep * step) * roundingRadius + xc;
			double y = Math.cos(singleStep * step) * roundingRadius + yc;
			points.add(new Vector2d(x, -y));
		}
		points.add(new Vector2d(roundingRadius, 0));
		return points;
	}
}
