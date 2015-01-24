/**
 * 
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.fastshapeview;

import java.awt.Graphics;

import javax.swing.JComponent;

import org.graffiti.attributes.Attribute;
import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.view.CoordinateSystem;
import org.graffiti.plugin.view.ShapeNotFoundException;

/**
 * @author matthiak
 *
 */
public class EdgeElementContainer extends GraphElementContainer{

	
	
	/**
	 * @param ge
	 */
	public EdgeElementContainer(FastShapeView view, GraphElement ge) {
		super(view, ge);
		createShape();
	}

	@Override
	public void attributeChanged(Attribute attr) throws ShapeNotFoundException {
	}

	@Override
	public void createNewShape(CoordinateSystem coordSys)
			throws ShapeNotFoundException {
	}

	
	
	@Override
	public void updateFromDependentContainer(GraphElement ge) {
		shape.update(this.graphElement);
	}

	@Override
	public void createShape() {
		shape = new MyEdgeShape(this.graphElement);
	}

	@Override
	public void updateShape() {
	}

	@Override
	public void paint(Graphics g) {
		shape.paint(g);
		if(listAttributeComponents != null) {
			for(JComponent attrcomp : listAttributeComponents ) {
				attrcomp.paint(g);
			}
		}
	}


}
