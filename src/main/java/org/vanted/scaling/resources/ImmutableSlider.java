package org.vanted.scaling.resources;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;

import javax.swing.BoundedRangeModel;
import javax.swing.JSlider;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.basic.BasicSliderUI;

import org.vanted.scaling.Toolbox;

/**
 * A JSlider that remains unchanged regardless of the scaling. To change, 
 * you have to re-instantiate it.<p>
 * 
 * Used for the HighDPISupport pane, where it is vital to preserve state, so
 * that the user can continue to work with its components.<p>
 * 
 * The LookAndFeel immutable JSlider has two skins, one plain and one
 * color-coded. 
 * 
 * @author dim8
 *
 */
public class ImmutableSlider extends JSlider {

	private static final long serialVersionUID = -5190816962285639529L;
	
	private FontUIResource font;


	/* ================================================================ *
	 *                          Constructors                            *
	 * ================================================================ */
	
	public ImmutableSlider() {
		saveDefaults();
	}

	public ImmutableSlider(int orientation) {
		super(orientation);
		saveDefaults();
	}

	public ImmutableSlider(BoundedRangeModel brm) {
		super(brm);
		saveDefaults();
	}

	public ImmutableSlider(int min, int max) {
		super(min, max);
		saveDefaults();
	}

	public ImmutableSlider(int min, int max, int value) {
		super(min, max, value);
		saveDefaults();
	}

	public ImmutableSlider(int orientation, int min, int max, int value) {
		super(orientation, min, max, value);
		saveDefaults();
	}
	
	/* ================================================================ *
	 *                           Overridden                             *
	 * ================================================================ */

	@Override
	public Font getFont() {
		return font;
	}
	
	/* ================================================================ *
	 *                           Methods                                *
	 * ================================================================ */

	private void saveDefaults() {
		font = (FontUIResource) UIManager.get("Slider.font");
		correctUI();
	}
	
	private void correctUI() {
		ColoredImmutableSliderUI iUI = new ColoredImmutableSliderUI(this);
		this.setUI(iUI);
	}
	
	/* ================================================================ *
	 *                        Helper Classes                            *
	 * ================================================================ */
	
	/**
	 * By saving and loading certain LAF Defaults, we deactivate any
	 * LAF influences from then on, that might be brought through a
	 * LAF change. Thus, we turn it LAF-immutable.
	 * 
	 * @author dim8
	 *
	 */
	protected static class PlainImmutableSliderUI extends BasicSliderUI {

		protected final float factor = Toolbox.getDPIScalingRatio();
		
		private Dimension hsize;
		private Dimension vsize;
		private Dimension minHor;
		private Dimension minVert;
		private int tickLength = 8;
		
		public PlainImmutableSliderUI(JSlider b) {
			super(b);
			hsize = (Dimension) UIManager.get("Slider.horizontalSize");
			vsize = (Dimension) UIManager.get("Slider.verticalSize");
			minHor = (Dimension) UIManager.get("Slider.minimumHorizontalSize");
			minVert = (Dimension) UIManager.get("Slider.minimumVerticalSize");
			//No focusInsets, those are (0, 0, 0, 0)
			tickLength *= factor;
		}

		@Override
		public Dimension getPreferredHorizontalSize() {
			hsize.height *= factor;
			return hsize;
		}

		@Override
		public Dimension getPreferredVerticalSize() {
			return vsize;
		}

		@Override
		public Dimension getMinimumHorizontalSize() {
			return minHor;
		}

		@Override
		public Dimension getMinimumVerticalSize() {
			return minVert;
		}
		
		@Override
		protected int getTickLength() {
			return tickLength;
		}
	}
	
	/**
	 * This gives the ScalingSlider a rainbow-look. This serves a couple of
	 * purposes, first, it color-maps the scaling regions to stimulate better
	 * mental representation. E.g. the high-memory region with very small DPI
	 * is color-coded in red. This corresponds well to the subsequently shown
	 * Warning message. This is not the only case. Second, it has its own
	 * unique look and thus leaves a strong impression. Also by overridding 
	 * the painting procedures, we turn it immutable. For completion (e.g. to
	 * avoid size-changing), it extends the {@linkplain PlainImmutableSliderUI}.
	 * 
	 * @author dim8
	 *
	 */
	protected static class ColoredImmutableSliderUI extends PlainImmutableSliderUI {

	    private static float[] fracs = {0f, .1f, .2f, .6f, .8f, 1f};
	    private LinearGradientPaint p;
		
	    public ColoredImmutableSliderUI(JSlider slider) {
	        super(slider);	        
	    }
	    
		@Override
		protected void calculateTrackRect() {
			super.calculateTrackRect();

	        if (slider.getOrientation() == JSlider.HORIZONTAL)
	        	trackRect.height *= factor;
	        else
	        	trackRect.width *= factor;

	        //Here, before calculateThumbLocation(), to not distort the
	        //additional to-value placing that is calculated there
	        thumbRect.width *= factor;
	        thumbRect.height *= factor;
		}
		
		@Override
		protected void calculateLabelRect() {
			super.calculateLabelRect();
			if (slider.getOrientation() == JSlider.HORIZONTAL)
				labelRect.height *= factor;
			else
				labelRect.width *= factor;
		}

		@Override
	    public void paintTrack(Graphics g) {
	        Graphics2D g2d = (Graphics2D) g;
	        Rectangle t = trackRect;
	        Point2D start = new Point2D.Float(t.x, t.y);
	        Point2D end = new Point2D.Float(t.x + t.width, t.y + t.height);
	        Color[] colors = {Color.RED, Color.ORANGE, Color.YELLOW,
	        		Color.GREEN, Color.CYAN, Color.BLUE};
	        p = new LinearGradientPaint(start, end, fracs, colors);	        
	        g2d.setPaint(p);
	        if (slider.getOrientation() == JSlider.HORIZONTAL)
	        	g2d.fillRoundRect(t.x, t.y, t.width, t.height, t.height / 2, t.height / 2);
	        else
	        	g2d.fillRoundRect(t.x, t.y, t.width, t.height, t.width / 2, t.width / 2);
	    }

	    @Override
	    public void paintThumb(Graphics g) {	    	
	        Graphics2D g2d = (Graphics2D) g;
	        g2d.setRenderingHint(
	            RenderingHints.KEY_ANTIALIASING,
	            RenderingHints.VALUE_ANTIALIAS_ON);
	        Rectangle t = thumbRect;
	        g2d.setColor(Color.BLACK);
	        int tw2 = t.width / 2;
	        int th2 = t.height / 2;
	        if (slider.getOrientation() == JSlider.HORIZONTAL) {
	        	g2d.drawLine(t.x + 1, t.y + th2/2 + th2/4, t.x + tw2, t.y + t.height - 1);
	        	g2d.drawLine(t.x + t.width - 2, t.y + th2/2 + th2/4, t.x + tw2, t.y + t.height - 1);
	        	g2d.fillRect(t.x, t.y, t.width, th2/2 + th2/4);
	        	g2d.fillOval(t.x, t.y + th2/4 + 1, t.width, t.width);
	        } else {
	        	g2d.drawLine(t.x + tw2/2 + tw2/4, t.y + 1, t.x + t.width - 1, t.y + th2);
	        	g2d.drawLine(t.x + tw2/2 + tw2/4, t.y + t.height - 2, t.x + t.width - 1, t.y + th2);
	        	g2d.fillRect(t.x, t.y, tw2/2 + tw2/4, t.height);
	        	g2d.fillOval(t.x + 1, t.y, t.height, t.height);
	        }
	    }
	}
}
