package org.vanted.scaling;

import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.util.Arrays;
import java.util.function.Predicate;

import javax.swing.Icon;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.IconUIResource;

/**
 * This scales the following: all fonts, all integers specified in
 * {@code LOWER_SUFFIXES_INTEGERS}, all icons and all insets. For
 * specific LAF implementations consider {@link NimbusScaler} and
 * {@link WindowsScaler}.
 * 
 * @author dim8
 *
 */
public class BasicScaler implements Scaler {
	protected final float scaleFactor;
	protected final UIDefaults uiDefaults = UIManager.getLookAndFeelDefaults();
	
	private static final String[] LOWER_SUFFIXES_INTEGERS = 
			new String[] { "width", "height", "indent", "size", "gap", "padding" };

	public BasicScaler(float scaleFactor) {
		this.scaleFactor = scaleFactor;
	}

	@Override
	public void initialScaling() {}

	@Override
	public Font modifyFont(Object key, Font original) {
		if (original instanceof FontUIResource && key.toString().endsWith(".font"))
			return newScaledFontUIResource(original, scaleFactor);

		return original;
	}

	protected static FontUIResource newScaledFontUIResource(Font original, float scale) {
		int newSize = Math.round(original.getSize() * scale);
		
		return new FontUIResource(original.getName(), original.getStyle(), newSize);
	}

	@Override
	public Icon modifyIcon(Object key, Icon original) {
		return new IconUIResource(new ScaledIcon(original, scaleFactor));
	}

	@Override
	public Integer modifyInteger(Object key, Integer original) {
		if (!endsWithOneOf(lower(key), LOWER_SUFFIXES_INTEGERS))
			return original;

		return (int) (original * scaleFactor);
	}
	
	@Override
	public Insets modifyInsets(Object key, Insets original) {
		return new Insets(
				Math.round(original.top * scaleFactor),
				Math.round(original.left * scaleFactor),
				Math.round(original.bottom * scaleFactor),
				Math.round(original.right * scaleFactor));
	}

	private boolean endsWithOneOf(String text, String[] suffixes) {
		final String t = text;
		
		return Arrays.stream(suffixes).anyMatch(new Predicate<String>() {
			@Override
			public boolean test(String suffix) {
				return t.endsWith(suffix);
			}
		});
	}

	private String lower(Object key) {
		return (key instanceof String) ? ((String) key).toLowerCase() : "";
	}
	
	private class ScaledIcon implements Icon {
		
		protected float _scalef;
		protected Icon _icon;

		public ScaledIcon(Icon sIcon, float scaleFactor) {
			this._icon = sIcon;
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
			
			_icon.paintIcon(c, g, x, y);

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
}