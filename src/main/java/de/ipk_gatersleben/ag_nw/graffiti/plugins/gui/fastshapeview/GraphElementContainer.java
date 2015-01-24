/**
 * 
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.fastshapeview;

import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.swing.JComponent;

import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.editor.AttributeComponentNotFoundException;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.GraphElement;
import org.graffiti.managers.AttributeComponentManager;
import org.graffiti.plugin.view.AttributeComponent;
import org.graffiti.plugin.view.CoordinateSystem;
import org.graffiti.plugin.view.GraffitiViewComponent;
import org.graffiti.plugin.view.ShapeNotFoundException;
import org.graffiti.plugin.view.View;

/**
 * @author matthiak
 *
 */
public abstract class GraphElementContainer implements GraffitiViewComponent{
	
	FastShapeView view;
	
	MyShape shape;
	
	List<JComponent> listAttributeComponents;
	
	CoordinateSystem coordSys;
	
	List<GraphElementContainer> dependentContainer;

	GraphElement graphElement;
	
	/**
	 * 
	 */
	public GraphElementContainer(FastShapeView view, GraphElement ge) {
		this.view = view;
		this.graphElement = ge;
		dependentContainer = new ArrayList<GraphElementContainer>();
		
	}
	public abstract void createShape();

	public abstract void updateShape();

	public abstract void updateFromDependentContainer( GraphElement ge);
	
	public abstract void paint(Graphics g);
	
	public MyShape getShape() {
		return shape;
	}

	public void setShape(MyShape shape) {
		this.shape = shape;
	}

	public List<JComponent> getAttributes() {
		return listAttributeComponents;
	}

	public void setAttributes(List<JComponent> attributes) {
		this.listAttributeComponents = attributes;
	}
	
	public void addDependentContainer(GraphElementContainer depCont){
		dependentContainer.add(depCont);
	}
	public void delDependentContainer(GraphElementContainer depCont) {
		dependentContainer.remove(depCont);
	}
	
	protected void createAttributeComponents(Attribute attributes) {
		if (!maybeCreateAttributeComponent(attributes) && attributes instanceof CollectionAttribute) {
			CollectionAttribute ca = (CollectionAttribute) attributes;
			ArrayList<Attribute> al = new ArrayList<Attribute>(ca.getCollection().values());
			for (Attribute subAttribute : al) {
				createAttributeComponents(subAttribute);
			}
		}
	}
	
	private boolean maybeCreateAttributeComponent(Attribute attribute
			) {
		if(listAttributeComponents == null){
			listAttributeComponents = new ArrayList<JComponent>();
		}
		AttributeComponentManager acm = view.getAttributeComponentManager();
		if (acm == null) {
			return false;
		}
		if (!acm.hasAttributeComponent(attribute.getClass()))
			return false;
		try {
			AttributeComponent attrComp = acm.getAttributeComponent(attribute.getClass());
			Point p = new Point();
			p.x = (int)shape.getBounds().getX();
			p.y = (int)shape.getBounds().getY();
					
			attrComp.setShift(p);
			attrComp.setGraphElementShape(shape);
			attrComp.setAttribute(attribute);
			try {
				attrComp.createNewShape(coordSys);
			} catch (ShapeNotFoundException e) {
				e.printStackTrace();
			}
//			attrComp.setShift(p);
			if(attrComp.getSize().getWidth() == 0 || attrComp.getSize().getHeight() == 0)
				attrComp.setSize(attrComp.getPreferredSize());
			listAttributeComponents.add(attrComp);

			
			return true;
		} catch (AttributeComponentNotFoundException acnfe) {
			return false;
		}
	}
}
