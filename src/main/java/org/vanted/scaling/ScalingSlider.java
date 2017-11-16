package org.vanted.scaling;

import java.awt.Container;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.Serializable;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.vanted.scaling.AutomatonBean.State;
import org.vanted.scaling.resources.ImmutableSlider;

/**
 * The front-end of the provided scalers. Because of the
 * arithmetics and our goal (tackling issues on High DPI displays),
 * the user could decrease UIdefaults with the factor of 2 at most.
 * On the contrary, one could increase with a factor of 100. This
 * could be overwritten though.
 * 
 * @author dim8
 */
public class ScalingSlider extends ImmutableSlider 
									implements ChangeListener, FocusListener, Serializable {
	
	private static final long serialVersionUID = 939020663044124704L;
	
	private static final int MAJOR_TICK_SPACING = 25;
	private static final int MINOR_TICK_SPACING = 10;	
	
	private static int STANDARD_DPI;
	static float MIN_DPI;
	
	/** The runtime internal ScalingSlider factor, used for computing the
	 *  emulated DPI. 	 */
	private transient float prevFactor = 0f;
	/** Used to store temporarily the previous factor for live re-scale
	 *  operations. Ergo non-serializable! */
	private transient int oldValue;  //used when reverting
	private Container main;
	private static ScalingSlider instance;
	private static List<ChangeListener> listeners;

	/* Defaults */
	private int value = 50;
	static int min = 0;
	static int max = 100;
	private int extent = 0;
	
	static int median = (min + max) / 2;
	/**
	 * Slider to allow live modifications to the scaling of components. It just
	 * has to be initialized e.g. in Preferences window.
	 * 
	 * @param mainContainer the applications main Container/Window/Frame
	 */
	public ScalingSlider(Container mainContainer) {
		this.main = mainContainer;
		
		int prefsValue = DPIHelper.managePreferences(DPIHelper.VALUE_DEFAULT, 
				DPIHelper.PREFERENCES_GET);
		
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
	public ScalingSlider(int value, int extent, int min, int max, Container mainContainer) {
		this.main = mainContainer;
		
		scalingSlider(value, extent, min, max);
	}

	/**
	 * Internal method for both constructors.
	 */
	private void scalingSlider(int value, int extent, int min, int max) {
		
		addChangeListener(this);
		addFocusListener(this);
		
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
		
		oldValue = this.value;
		
		instance = this;
	}
	
	/**
	 * This determines what our standard DPI is. <p>
	 * 
	 * Basically, Macintosh uses 72 DPI - pretty neat, since dots and pixels
	 * to inch are then literally the same (WYSIWYG principle).
	 * However, Windows is another story, to fix some of the 72-DPI-problems,
	 * it introduced a 33% bigger size - 96 DPI. As for Linux, we could assume
	 * the Gnome default 96 DPI, because otherwise it's just a mess. And then
	 * come all the custom resolution size displays, e.g. through HDMI, so we
	 * take as a standard the default main-monitor DPI resolution.  
	 */
	public static int getStandard() {
		STANDARD_DPI = Toolkit.getDefaultToolkit().getScreenResolution();
		
		MIN_DPI = STANDARD_DPI / (2f * median);
		
		return STANDARD_DPI;
	}
	
	public static int getSliderValue() {
		return instance.getValue();
	}
	
	/**
	 * This method could be used to add any number of ChangeListeners, 
	 * so that you could synchronize any actions with the movement of
	 * the slider, i.e. the live scaling itself.
	 * @param changeListeners
	 */
	public static void registerChangeListeners(ChangeListener[] changeListeners) {
		if (listeners != null)
			listeners.addAll(Arrays.asList(changeListeners));
		else
			listeners = new LinkedList<ChangeListener>(Arrays.asList(changeListeners));
	}
	
	public static List<ChangeListener> getRegisteredChangeListeners() {
		return listeners;
	}
	
	private void addChangeListeners() {
		if (listeners != null) {
			for (int i = 0; i < listeners.size(); i++)
				this.addChangeListener(listeners.get(i));
			
			listeners.clear();
			listeners = null;
		}
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
		this.setOrientation(JSlider.HORIZONTAL);
		/* set up labels*/
		insertLabels();
		/*set initial TooltipText*/
		int v = this.getValue();
		this.setToolTipText(String.valueOf(v) + " (DPI: " + 
									Math.round(DPIHelper.processEmulatedDPIValue(v)) + ")");
		
		//When no startUp scaling has been done, state automaton is null
		if (AutomatonBean.getInstance() == null)
			new AutomatonBean();
	}
	
	/**
	 * Insert the labels at the specified major ticks.
	 */
	private void insertLabels() {
		Hashtable<Integer, JComponent> labels = new Hashtable<Integer, JComponent>();
		/* truncate float */
		DecimalFormat df = new DecimalFormat("##.##");
		df.setRoundingMode(RoundingMode.UP);
		/* add elements */
		float factor = 1f / max;
		for (int i = min; i <= max; i += MAJOR_TICK_SPACING) {
			float label = STANDARD_DPI * factor;
			
			if (!Float.toString(label).endsWith(".0")) //show as float
				labels.put(i + extent, new JLabel(df.format((label))
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
		//Add any externally registered ChangeListeners
		addChangeListeners();
		
		// Standard implementation
	    JSlider source = (JSlider) e.getSource();
	    
	    if (!source.getValueIsAdjusting()) {    	
	        int value = source.getValue();
	        
	        // Warn against too low DPI values out of memory considerations
	        if (handleMemoryWarning(value))
	        	return;  // Revert
	        
	        // Call the Coordinator to update LAF!
	        new ScalingCoordinator(computeCoordinatorFactor(value), main);
	        
	        DPIHelper.managePreferences(value, DPIHelper.PREFERENCES_SET);      
	        
	        this.setToolTipText(String.valueOf(value) + " (DPI: " 
	        		+ Math.round(DPIHelper.processEmulatedDPIValue(value)) + ")");
	        
	        oldValue = value;
	        
	    	//set new scaling property
	    	setScalingState();
	    }
	}
	
	/**
	 * Here we process the DPI according to the Slider's
	 * selected value. Furthermore, we have to re-adjust the 
	 * starting value by taking 'new/old' to reset the old one,
	 * which is needed at runtime.<p>
	 * 
	 * Calling it twice consecutively, is as going back and forth
	 * with the slider itself and thus giving a factor of 1.0 and
	 * no change at all.
	 * 
	 * @param sliderValue the Slider's selected DPI value
	 * @return the adjusted DPI factor ready to be pass onto the ScalingCoordinator
	 */
	private float computeCoordinatorFactor(int sliderValue) {
		float dpif = DPIHelper.processEmulatedDPIValue(sliderValue);

		if (prevFactor != 0f) //0.0 is here not the min. value, but the unset!
			dpif /= prevFactor;
		else {
			int prefsSliderValue = DPIHelper.managePreferences(DPIHelper.VALUE_DEFAULT, 
					DPIHelper.PREFERENCES_GET);
			float prefsPrevFactor = (prefsSliderValue == (float) min) ? (min + 0.5f) / median
							/*1/2 for lowest mark, since 0 neutral */
							: prefsSliderValue / (float) median;
			dpif /= prefsPrevFactor;
		}
		
		//update next previous DPI factor
		prevFactor = (sliderValue == (float) min) ? (min + 0.5f) / median
				: (float) sliderValue / median;
		
		return dpif;
	}
	
	/**
	 * Because scaling with very low DPI uses a lot of memory, especially for
	 * the components, different from the default LookAndFeel ones, we issue
	 * a warning to give the user an opportunity to decide whether he truly
	 * needs and could support such low DPI scaling.
	 * 
	 * @param value
	 */
	private boolean handleMemoryWarning(int value) {
		if (DPIHelper.processEmulatedDPIValue(value) >= 8f)
			return false;
		
		String message = "You are performing really low DPI emulation. "
				+ "This might lead to excessive memory usage.\n\nDo you want to revert it?";
		
		//TODO on mac put dialog on top
		int selection = JOptionPane.showConfirmDialog(main, message, "Memory Notice",
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		
		if (selection == JOptionPane.YES_OPTION) {
			this.setValue(oldValue);
			this.repaint();
			return true;
		} else
			return false;		
	}
	
	private void setScalingState() {
		if (AutomatonBean.getState()
				.equals(State.ON_SLIDER.toString()))
			AutomatonBean.setState(State.RESCALED);
		else
			AutomatonBean.setState(State.ON_SLIDER);
	}
	
	@Override
	public void focusGained(FocusEvent e) {/* do nothing */}
	
	@Override
	public void focusLost(FocusEvent e) {
		String v = AutomatonBean.getState();
		if (v.equals(State.ON_SLIDER.toString())
				|| v.equals(State.RESCALED.toString()))
			AutomatonBean.setState(State.IDLE);
	}
}