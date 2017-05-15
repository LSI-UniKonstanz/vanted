package org.vanted.scaling.resources;

import java.awt.Font;

import javax.swing.BoundedRangeModel;
import javax.swing.JSlider;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.SliderUI;

/**
 * A JSlider that remains unchanged regardless of the scaling. To change, 
 * you have to re-instantiate it.<p>
 * 
 * Used for the HighDPISupport pane, where it is vital to preserve state, so
 * that the user can continue to work with its components.
 * 
 * TODO TODO TODO Mac still changes it!
 * 
 * @author dim8
 *
 */
public class ImmutableSlider extends JSlider {

	private static final long serialVersionUID = -5190816962285639529L;
	
	private FontUIResource font;
	private SliderUI ui;

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

	private void saveDefaults() {
		font = (FontUIResource) UIManager.get("Slider.font");
		ui = (SliderUI) this.getUI();
	}

	@Override
	public Font getFont() {
		return font;
	}
	
	@Override
	public SliderUI getUI() {
		return ui;
	}
}
