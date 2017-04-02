package org.vanted.scaling;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Toolkit;
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

import org.vanted.scaling.resources.ScaledFontUIResource;
import org.vanted.scaling.resources.ScaledIcon;

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
	
	/**Constants for allowed minimal width and height. */
	private static final int MIN_ICON_SIZE = 1;
	
	public BasicScaler(float scaleFactor) {
		this.scaleFactor = scaleFactor;
	}

	@Override
	public void initialScaling() {}//Nimbus exclusive

	@Override
	public Font modifyFont(Object key, Font original) {
		
		//We have a non-LAF related call
		if (key == null && original != null)
			return newScaledFont(original, scaleFactor);
		
		if (original instanceof FontUIResource && key != null &&
				lower(key.toString()).endsWith("font"))
			return newScaledFontUIResource(original, scaleFactor);

		return original;
	}

	/**
	 * Creates a new {@link ScaledFontUIResource} by modifying the size.
	 * 
	 * @param original 
	 * @param factor the scale factor
	 * 
	 * @return a {@link ScaledFontUIResource} instance
	 */
	protected static ScaledFontUIResource newScaledFontUIResource(Font original, float factor) {
		int newSize = Math.round(original.getSize() * factor);
		ScaledFontUIResource newFont = 
				new ScaledFontUIResource(original.getName(), original.getStyle(), newSize);
		newFont.setDPI((int) (Toolkit.getDefaultToolkit().getScreenResolution() / factor));
		
		return newFont;
	}
	
	/**
	 * We scale a given {@link Font}, while preserving the type in the scope of Swing.
	 * 
	 * @param original
	 * @param factor the scale factor
	 * 
	 * @return a {@link Font} instance
	 */
	protected static Font newScaledFont(Font original, float factor) {
		int newSize = Math.round(original.getSize() * factor);
		
		//test for the sub-type
		if (original instanceof FontUIResource)
			return new FontUIResource(original.getName(), original.getStyle(), newSize);
			
		return new Font(original.getName(), original.getStyle(), newSize);
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
		int width = 
			((width = Math.round((newIcon.getIconWidth() * scaleFactor))) < MIN_ICON_SIZE)
				? MIN_ICON_SIZE : width;
		int height = 
				((height = Math.round((newIcon.getIconHeight() * scaleFactor))) < MIN_ICON_SIZE)
					? MIN_ICON_SIZE : height;
		
		BufferedImage newImage = new BufferedImage(width, height,
											BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D gfx = newImage.createGraphics();
		gfx.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
				RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		
		gfx.setRenderingHint(RenderingHints.KEY_RENDERING, 
				RenderingHints.VALUE_RENDER_SPEED);
		
		
		gfx.drawImage(newIcon.getImage(), 0, 0,
				Math.round(newIcon.getIconWidth() * scaleFactor),
				Math.round(newIcon.getIconHeight() * scaleFactor), null);
		
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
}