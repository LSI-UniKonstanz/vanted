package org.vanted.scaling;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.prefs.Preferences;

import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.SystemInfo;
import org.graffiti.managers.PreferenceManager;
/**
 * The front-end of the provided scalers. Because of the
 * arithmetics and our goal, the user could decrease UIdefaults
 * with the factor of 2 at most. On the contrary, one could 
 * increase with a factor of 100. This is overwritable.
 * 
 * @author dim8
 *
 */
public class ScalingSlider extends JSlider 
									implements ChangeListener, Serializable {
	
	public static boolean START_UP = true;
	
	private static final long serialVersionUID = 939020663044124704L;
	
	private static final int MAJOR_TICK_SPACING = 25;
	private static final int MINOR_TICK_SPACING = 10;
	private static final String PREFERENCE_SCALING = "Scaling Preferences";
	private static int STANDARD_DPI;
	private static float MIN_DPI;
	
	private float prevFactor = 0f;

	/* Defaults */
	@SuppressWarnings("unused") //it's used (potentially), see a bit down below
	private int value = 50;
	private static int min = 0;
	private static int max = 100;
	private int extent = 0;
	
	private static int median = (min + max) / 2;
	
	public ScalingSlider() {
		int prefsValue = managePreferences(-1, true);
		
		scalingSlider(prefsValue, extent, min, max);
	}
	
	public ScalingSlider(int value, int extent, int min, int max) {
		scalingSlider(value, extent, min, max);
	}

	/**
	 * Internal method for both constructors.
	 */
	private void scalingSlider(int value, int extent, int min, int max) {
		
		addChangeListener(this);
		
		this.value = value; 
		this.extent = extent;
		ScalingSlider.min = min;
		ScalingSlider.max = max;
		
		median = (min + max) / 2;
		
		DefaultBoundedRangeModel rModel = 
				new DefaultBoundedRangeModel(value, extent, min, max);
		this.setModel(rModel);
		
		getStandard();
		
		setSpecifics();
	}
	
	/**
	 * This determines what our standard DPI is. 
	 * Basically, Macintosh uses 72 dpi, which is pretty neat, since then
	 * dots and pixels to inch are literally the same (WYSIWYG principle). 
	 * However, Windows is another story, to fix some of the 72 dpi problems,
	 * it introduced a 33% bigger size - 96 dpi. As for Linux, we just assume 
	 * the Gnome default 96 dpi, because otherwise it's just a mess.
	 */
	public static int getStandard() {
		STANDARD_DPI = SystemInfo.isMac() ? 72 : 96;
		
		MIN_DPI = STANDARD_DPI / (2f * median);
		
		return STANDARD_DPI;
	}
	
	/**
	 * Set the Scaling slider specifics.
	 */
	private void setSpecifics() {
		/* ticks */
		this.setMajorTickSpacing(MAJOR_TICK_SPACING);
		this.setMinorTickSpacing(MINOR_TICK_SPACING);
		this.setPaintTicks(true);
		/* orientation */
		this.setOrientation(SwingConstants.HORIZONTAL);
		/* set up labels*/
		insertLabels();
	}

	
	
	/**
	 * Insert the labels at the specified major ticks.
	 */
	private void insertLabels() {
		Hashtable<Integer, JComponent> labels = new Hashtable<Integer, JComponent>();
		/* add elements */
		float factor = 1f / max;
		for (int i = min; i <= max; i += MAJOR_TICK_SPACING) {
			float label = STANDARD_DPI * factor;

			if (!Float.toString(label).endsWith(".0")) //show as float
				labels.put(i + extent, new JLabel(Float.toString((STANDARD_DPI * factor))
						+ " DPI"));
			else //otherwise as int
				labels.put(i + extent, new JLabel(Integer.toString((int)(label))
						+ " DPI"));
			
			//update factor with next value
			factor = ((float) (i + MAJOR_TICK_SPACING)) / median;
		}
		
		setLabelTable(labels);
		setPaintLabels(true);
		updateLabelUIs();
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		// Standard implementation
	    JSlider source = (JSlider) e.getSource();
	    
	    if (!source.getValueIsAdjusting()) {
	        int dpi = source.getValue();
	        //call the Coordinator to update LAF!
	        new ScaleCoordinator(processFactor(dpi));
	        
	        managePreferences(dpi, false);
	        
	        this.setToolTipText(String.valueOf(source.getValue()));
	    }
	}
	/**
	 * Here we process the DPI-Factor according to the Slider's
	 * selected value. Furthermore, we have to re-adjust the 
	 * starting value by taking 'new/old' to reset the old one,
	 * which is needed only at runtime.  
	 * @param sliderValue the Slider's selected dpi value
	 * @return the adjusted factor ready to be pass onto the ScaleCoordinator
	 */
	private float processFactor(int sliderValue) {
		float dpif = processSliderValue(sliderValue);
		
		if (prevFactor != 0.0)//0.0 is here not the min. value, but unset!
			dpif /= prevFactor;
		else {
			float prefsPrevFactor = (managePreferences(-1, true) == (float) min)
					? (min + 0.5f) / median
							/*1/2 for lowest mark, since 0 neutral */
							: managePreferences(-1, true) / (float) median;
			dpif /=  prefsPrevFactor;
		}
		
		//update next previous factor
		prevFactor = (sliderValue == (float) min) ? (min + 0.5f) / median
				: sliderValue / median;
		
		return dpif;		
	}
	
	/**
	 * This is a two-way intern method, used for all preferences-related
	 * procedures. It handles both setting/putting and getting of values.
	 * For latter you need to specify the <b>get</b> parameter. 
	 * @param val Our new ScalingSlider value.
	 * Used only, when putting into (i.e. <b>get</b> is <b>false</b>).   
	 * @param get When <b>true</b>, it returns the previously stored value.
	 * @return Stored value under 'Scaling Preferences' or -1 for potential 
	 * error checking, if <b>get</b> <b>false</b>.
	 */
	public static int managePreferences(int val, boolean get) {
		final Preferences scalingPreferences = PreferenceManager
								.getPreferenceForClass(ScalingSlider.class);
		
		//do not put new value
		if(get)
			return scalingPreferences.getInt(PREFERENCE_SCALING, median);

		scalingPreferences.putInt(PREFERENCE_SCALING, val);
				
		//default value, when used for setting not getting
		return -1;
	}
	
	/**
	 * Converts a slider value into DPI Factor to be passed on to the 
	 * ScaleCoordinator. We firstly determine standard constants and 
	 * we use in the process the default slider values. For different
	 * than default, one should initialize a ScalingSlider with such
	 * new values.
	 * @param sValue sliderValue (get it from Preferences)
	 * @return DPI Factor
	 */
	public static float processSliderValue(int sValue) {
		int standard = getStandard(); //also sets MIN_DPI for public use!
		
		//handling the artificial 0
		return (sValue == min)
				? MIN_DPI 
				: standard * (sValue / (float) median);
	}
}