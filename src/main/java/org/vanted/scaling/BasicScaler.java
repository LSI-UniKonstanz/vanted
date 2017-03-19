package org.vanted.scaling;

import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.function.Predicate;
import java.lang.IllegalArgumentException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.IconUIResource;
import javax.swing.plaf.InsetsUIResource;

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
	public void initialScaling() {}//Nimbus exclusive

	@Override
	public Font modifyFont(Object key, Font original) {
		if (original instanceof FontUIResource && 
				lower(key.toString()).endsWith("font"))
			return newScaledFontUIResource(original, scaleFactor);

		return original;
	}

	protected static FontUIResource newScaledFontUIResource(Font original, float scale) {
		int newSize = Math.round(original.getSize() * scale);
		
		return new FontUIResource(original.getName(), original.getStyle(), newSize);
	}

	@Override
	public Integer modifyInteger(Object key, Integer original) {
		if (!endsWithOneOf(lower(key), LOWER_SUFFIXES_INTEGERS))
			return original;

		return (int) (original * scaleFactor);
	}
	/**
	 * Interface method for {@link modifyInsets}, encapsulating the delegation
	 * and performing the differentiating.
	 * 
	 * @param original an Insets instance to be scaled
	 * @return a newly scaled instance or null
	 */
	@Override
	public Insets getModifiedInsets(Insets original) {
		if (original instanceof InsetsUIResource)
			return modifyInsets(null, (InsetsUIResource) original);
		
		if (original instanceof Insets)
			return modifyInsets((Insets) original, null);
		
		//sentinel for non-scalable value
		return null;		
	}
	
	/**
	 * Internal worker for the actual scaling operations.
	 * 
	 * @param original an Insets instance
	 * @param original2 an InsetsUIResource instance
	 * 
	 * @return the respective newly scaled Instance 
	 * 
	 * @throws IllegalArgumentException if arguments are wrongly set
	 */
	protected Insets modifyInsets(Insets original, InsetsUIResource original2)
				throws IllegalArgumentException {
		if ((original != null && original2 != null) || 
				(original == null && original2 == null))
			throw new IllegalArgumentException("Original must be either Insets"
					+ " OR InsetsUIResource, but not both!");
		
		//Default Insets
		if (original != null && original2 == null)
			return new Insets(
					Math.round(original.top * scaleFactor),
					Math.round(original.left * scaleFactor),
					Math.round(original.bottom * scaleFactor),
					Math.round(original.right * scaleFactor));
		
		//Then it should be the following
		return new InsetsUIResource(
				Math.round(original2.top * scaleFactor),
				Math.round(original2.left * scaleFactor),
				Math.round(original2.bottom * scaleFactor),
				Math.round(original2.right * scaleFactor));
	}
	
	/**
	 * Interface method for {@link modifyImageIcon(Icon icon)} and 
	 * {@link modifyIcon(Object key, Icon original)}.
	 * 
	 * @param icon to be scaled
	 * @return newly scaled instance of either {@link ImageIcon} or 
	 * {@link IconUIResource}.
	 */
	@Override
	public Icon getModifiedIcon(Object key, Icon icon) {
		if (icon == null)
			return null;
		
		ImageIcon imageIcon = null;
		IconUIResource iconResource = null;
		
		if (icon instanceof IconUIResource)
			//we are save to cast (see modifyIcon)
			iconResource = (IconUIResource) modifyIcon(key, icon);
		
		if (icon instanceof ImageIcon)
			imageIcon = modifyImageIcon(icon);
		
		return (imageIcon != null) ? imageIcon : iconResource;
	}
	

	/** 
	 * This modifies not all Icons, only those from LAF Defaults!
	 */
	protected Icon modifyIcon(Object key, Icon original) {
		return new IconUIResource(new ScaledIcon(original, scaleFactor));
	}
	
	/**
	 * Similar to {@link modifyIcon(Object key,  Icon original)}. However, here
	 * we work most of the time with ImageIcons only, although the cast is 
	 * inside the method. There is a difference between the LAF-version 
	 * in regards the filter not being changed and the return instance, i.e.
	 *  {@link IconUIResource}.
	 * 
	 * @param icon to be modified with the <code>scaleFactor</code> of 
	 * {@link BasicScaler}.
	 * @return newly scaled {@link ImageIcon} instance.
	 */
	protected ImageIcon modifyImageIcon(Icon icon) {
		ImageIcon newIcon = (ImageIcon) icon;
		BufferedImage newImage = new BufferedImage(
				(int) (newIcon.getIconWidth() * scaleFactor),
				(int) (newIcon.getIconHeight() * scaleFactor), 
				BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D gfx = newImage.createGraphics();
		gfx.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
				RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		
		gfx.setRenderingHint(RenderingHints.KEY_RENDERING, 
				RenderingHints.VALUE_RENDER_SPEED);
		
		
		gfx.drawImage(newIcon.getImage(), 0, 0,
				(int) (newIcon.getIconWidth() * scaleFactor),
				(int) (newIcon.getIconHeight() * scaleFactor), null);
		
		gfx.dispose();
		
		return new ImageIcon(newImage);
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
}