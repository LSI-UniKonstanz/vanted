/**
 * 
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.fastshapeview;

import java.awt.Graphics;
import java.awt.Shape;

import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.view.GraphElementShape;


/**
 * @author matthiak
 *
 */
public interface MyShape extends GraphElementShape{

	
	public abstract void update(GraphElement ge);
	
	public abstract void paint(Graphics g);
	
	
}
