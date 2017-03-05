package org.vanted.scaling;

import java.awt.Container;
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
public class ScaleSlider extends JSlider 
									implements ChangeListener, Serializable {
	
	private static final long serialVersionUID = 939020663044124704L;
	
	private static final int MAJOR_TICK_SPACING = 25;
	private static final int MINOR_TICK_SPACING = 10;
	
	
	/*--------Scaling Preferences-------- */
	private static final String PREFERENCE_SCALING = "Scaling Preferences";
	/** Default value, when not setting any particular value. */
	public static final int VALUE_DEFAULT = -1;
	/** Internal value for checking if any values are stored. */
	public static final int VALUE_UNSET = -2;
	/** Specify when you get Preferences value. */
	public static final boolean PREFERENCES_GET = true;
	/** Specify when you set Preferences value. */
	public static final boolean PREFERENCES_SET = false;
	
	
	private static int STANDARD_DPI;
	private static float MIN_DPI;
	
	/** Used to store temporarily the previous factor for live re-scale
	 *  operations. Ergo non-serializable! */
	private transient float prevFactor = 0f;
	private Container main;

	/* Defaults */
	@SuppressWarnings("unused") //it's used (potentially), see a bit down below
	private int value = 50;
	private static int min = 0;
	private static int max = 100;
	private int extent = 0;
	
	private static int median = (min + max) / 2;
	
	/**
	 * Slider to allow live modifications to the scaling of components. It just
	 * has to be initialized e.g. in Preferences.
	 * 
	 * @param mainContainer the applications main Container/Window/Frame
	 */
	public ScaleSlider(Container mainContainer) {
		this.main = mainContainer;
		
		int prefsValue = managePreferences(VALUE_DEFAULT, PREFERENCES_GET);
		
		scalingSlider(prefsValue, extent, min, max);
	}
	/**
	 * Specify your own Slider model. Factors, however, are dependent on 
	 * values.
	 * 
	 * @param value initial Slider's value
	 * @param extent Slider's extent
	 * @param min minimal Slider's value 
	 * @param max maximal Slider's value
	 * @param mainContainer the applications main Container/Window/Frame
	 */
	public ScaleSlider(int value, int extent, int min, int max, Container mainContainer) {
		this.main = mainContainer;
		
		scalingSlider(value, extent, min, max);
	}

	/**
	 * Internal method for both constructors.
	 */
	private void scalingSlider(int value, int extent, int min, int max) {
		
		addChangeListener(this);
		
		this.value = value; 
		this.extent = extent;
		ScaleSlider.min = min;
		ScaleSlider.max = max;
		
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
	        new ScaleCoordinator(processFactor(dpi), main);
	        
	        managePreferences(dpi, PREFERENCES_SET);      
	        
	        refresh();
	        
	        this.setToolTipText(String.valueOf(source.getValue()));
	    }
	}
	
	private void refresh() {
        //refresh the slider-GUI
        this.invalidate();
        this.repaint();
        this.revalidate();
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
			float prefsPrevFactor = (managePreferences(VALUE_DEFAULT, 
					PREFERENCES_GET) == (float) min) ? (min + 0.5f) / median
							/*1/2 for lowest mark, since 0 neutral */
							: managePreferences(VALUE_DEFAULT, PREFERENCES_GET)
							/ (float) median;
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
	 * To set values use <code>ScalingSlider.PREFERENCES_SET</code> as <code>
	 * get</code> parameter. To get: <code>ScalingSlider.PREFERENCES_GET</code>,
	 * respectively. 
	 * @param val Our new ScalingSlider value.
	 * Used only, when putting into (i.e. <b>get</b> is <b>false</b>). Meaning
	 * when querying values, you could just use <code>
	 * ScalingSlider.VALUE_DEFAULT</code>.
	 * 
	 * @param get When <b>true</b>, it returns the previously stored value.
	 * 
	 * @return Stored value under 'Scaling Preferences' or <code>
	 * ScalingSlider.VALUE_DEFAULT</code> for potential error checking, if 
	 * <b>get</b> is <b>false</b>.
	 */
	public static int managePreferences(int val, boolean get) {
		final Preferences scalingPreferences = PreferenceManager
								.getPreferenceForClass(ScaleSlider.class);
		
		/**
		 * For internal use. Return old value or flag, indicating there are
		 * no stored values, avoid scaling with identity factor.
		 */
		if (get && val == VALUE_UNSET)
			return scalingPreferences.getInt(PREFERENCE_SCALING, VALUE_UNSET);
		
		//do not put new value
		if (get)
			return scalingPreferences.getInt(PREFERENCE_SCALING, median);

		scalingPreferences.putInt(PREFERENCE_SCALING, val);
				
		return VALUE_DEFAULT;
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