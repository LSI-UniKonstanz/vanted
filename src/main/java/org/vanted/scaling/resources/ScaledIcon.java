package org.vanted.scaling.resources;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;

import javax.swing.Icon;

/**
 * Given a regular {@link Icon} this would produce a scaled clone of it.<p>
 * 
 * Note: The idea was that resources are only visible to the relevant Scalers, 
 * which use them. It would have meant package-protected visibility. This, 
 * however, comes firstly in with JAVA 9, due to Project Jigsaw. Until then the
 * modifier is public.
 * 
 * @author dim8
 *
 */
public class ScaledIcon implements Icon {
	
	protected float _scalef;
	protected Icon _icon;

	/**
	 * Calling this would initialize an Icon Object clone of the 
	 * passed <code>oldIcon</code>, but painted in a scaled environment,
	 * determined by the <code>scaleFactor</code>. For more information,
	 * check out the {@link paintIcon()} method.
	 * 
	 * 
	 * @param oldIcon icon to be scaled
	 * @param scaleFactor the respective factor of scaling
	 */
	public ScaledIcon(Icon oldIcon, float scaleFactor) {
		this._icon = oldIcon;
		this._scalef = scaleFactor;			
	}
	
	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		Graphics2D gfx = (Graphics2D) g;
		AffineTransform oldTransf = gfx.getTransform();
		RenderingHints oldHints = gfx.getRenderingHints();

		gfx.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		
		gfx.scale(_scalef, _scalef);
		
		_icon.paintIcon(c, g, (int)(x/_scalef), (int)(y/_scalef));

		gfx.setTransform(oldTransf);
		gfx.setRenderingHints(oldHints);
		
		//do not dispose!
		
	}

	@Override
	public int getIconWidth() {
		return Math.round(_icon.getIconWidth() * _scalef);
	}

	@Override
	public int getIconHeight() {
		return Math.round(_icon.getIconHeight() * _scalef);
	}
}