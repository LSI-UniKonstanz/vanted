/*************************************************************************************
 * The Exemplary Add-on is (c) 2008-2011 Plant Bioinformatics Group, IPK Gatersleben,
 * http://bioinformatics.ipk-gatersleben.de
 * The source code for this project, which is developed by our group, is
 * available under the GPL license v2.0 available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html. By using this
 * Add-on and VANTED you need to accept the terms and conditions of this
 * license, the below stated disclaimer of warranties and the licenses of
 * the used libraries. For further details see license.txt in the root
 * folder of this project.
 ************************************************************************************/
package org.vanted.addons.exampleaddon.attribute_component;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import org.AttributeHelper;
import org.Vector2d;
import org.graffiti.attributes.Attributable;
import org.graffiti.attributes.Attribute;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.plugin.attributecomponent.AbstractAttributeComponent;
import org.graffiti.plugin.view.ShapeNotFoundException;
import org.jfree.chart.renderer.StatisticalBarRenderer;

/**
 * The component visualises your {@link Attribute} in any matter. In this
 * example we visualise a star, but it can be any other component or drawing.
 * 
 * @author Hendrik Rohn
 */
public class StarAttributeComponent extends AbstractAttributeComponent {
	private static final long serialVersionUID = 1L;
	private Vector2d size;
	private Color color = Color.black;
	
	@Override
	public void attributeChanged(Attribute attr) throws ShapeNotFoundException {
		// here we could check for some attribute changes, which are not relevant
		// for this component, in order to supress the recreation of the component
		// for uninteresting attribute changes
		if (attr.getParent().getName().equals(attr.getParent().getName()))
			recreate();
	}
	
	/**
	 * This method is nice for brushing functionality etc:
	 * An event will be thrown, if the user moves with
	 * the mouse over or from this component.
	 **/
	@Override
	public void highlight(boolean value, MouseEvent e) {
		super.highlight(value, e);
		if (value)
			color = Color.yellow;
		else
			color = Color.black;
	}
	
	/**
	 * Is called every time the attribute value is changed and if it is not a
	 * change in size, shift or position.
	 */
	@Override
	public void recreate() throws ShapeNotFoundException {
		GraphElement ge = (GraphElement) this.attr.getAttributable();
		if (ge instanceof Node) {
			updatePosition(ge);
			adjustComponentSize();
			repaint();
		}
	}
	
	protected void updatePosition(Attributable attributable) {
		if (attributable instanceof Node) {
			Node n = (Node) attributable;
			Point2D pos = AttributeHelper.getPosition(n);
			Vector2d size = AttributeHelper.getSize((Node) attr.getAttributable());
			setLocation((int) (pos.getX() - size.x / 2 - 1), (int) (pos.getY() - size.y / 2 - 1));
		}
	}
	
	@Override
	public void setShift(Point shift) {
		if (attr != null && attr.getAttributable() != null)
			updatePosition(attr.getAttributable());
	}
	
	@Override
	public void adjustComponentSize() {
		if (attr != null) {
			size = AttributeHelper.getSize((Node) attr.getAttributable());
			setSize((int) size.x, (int) size.y);
		}
	}
	
	/**
	 * Here the actual drawing is performed.
	 */
	@Override
	public void paint(Graphics g) {
		super.paintComponent(g);
		((Graphics2D) g).draw(StatisticalBarRenderer.getTTestShape(2f, 2f, (float) (size.x - 4), (float) (size.y - 4)));
		g.setColor(color);
		((Graphics2D) g).setFont(new Font("SansSerif", Font.PLAIN, (int) size.y));
		g.drawString(attr.getValue() + "", 2, (int) size.y - 2);
	}
	
}
