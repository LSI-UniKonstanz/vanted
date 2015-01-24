/**
 * 
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.fastshapeview;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;

import javax.swing.JComponent;

import org.graffiti.attributes.Attribute;
import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.view.AttributeComponent;
import org.graffiti.plugin.view.CoordinateSystem;
import org.graffiti.plugin.view.ShapeNotFoundException;

/**
 * @author matthiak
 *
 */
public class NodeElementContainer extends GraphElementContainer{


	
	/**
	 * @param ge
	 */
	public NodeElementContainer(FastShapeView view, GraphElement ge) {
		super(view, ge);
		createShape();
		createAttributeComponents(this.graphElement.getAttributes());
	}

	@Override
	public void attributeChanged(Attribute attr) throws ShapeNotFoundException {
	}

	@Override
	public void createNewShape(CoordinateSystem coordSys)
			throws ShapeNotFoundException {
		this.coordSys = coordSys;
	}

	@Override
	public void createShape() {
		
		shape = new MyNodeShape(graphElement);
		
	}


	@Override
	public void updateFromDependentContainer(GraphElement ge) {
	}

	@Override
	public void updateShape() {
		shape.update(graphElement);
		updateAttributeComponents();
		for(GraphElementContainer eContainer : dependentContainer)
			eContainer.updateFromDependentContainer(graphElement);
			
	}
	

	/**
	 * 
	 */
	private void updateAttributeComponents() {
		for(JComponent attrcomp : listAttributeComponents ){
			try {
				Point p = new Point();
				p.x = (int)shape.getBounds().getX();
				p.y = (int)shape.getBounds().getY();
				((AttributeComponent)attrcomp).setShift(p);
				((AttributeComponent)attrcomp).setGraphElementShape(shape);
				((AttributeComponent)attrcomp).attributeChanged(graphElement.getAttributes());
			} catch (ShapeNotFoundException e) {
				e.printStackTrace();
			}
		}
			
	}

	@Override
	public void paint(Graphics g) {
		g.setColor(Color.BLACK);		
		
		shape.paint(g);
		Graphics2D g2 = (Graphics2D)g;
		Graphics localGraphics;
//		AffineTransform saveTransform = g2.getTransform();
//		g2.translate(shape.getBounds().getX(), shape.getBounds().getY());
		if(listAttributeComponents != null) {
			for(JComponent attrcomp : listAttributeComponents ) {
//				g.setColor(Color.BLACK);	
//				view.remove(attrcomp);
//				view.add(attrcomp);
//				if(attrcomp instanceof LabelComponent)
//					g2.setColor(Color.RED);
//				else
//					g2.setColor(Color.BLACK);
//				g2.fillRect(
//						attrcomp.getX(), 
//						attrcomp.getY(), 
//						attrcomp.getWidth(),
//						attrcomp.getHeight());
//				if(attrcomp instanceof ChartAttributeComponent)
//					continue;
				localGraphics = g.create(
						attrcomp.getX(), 
						attrcomp.getY(), 
						attrcomp.getWidth(),
						attrcomp.getHeight());
				attrcomp.printAll(localGraphics);
			}
		}
//		g2.setTransform(saveTransform);
		
	}

	
	

}
