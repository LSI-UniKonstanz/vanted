package org.vanted.scaling;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.graffiti.managers.PreferenceManager;

/**
 * An utility Manager that takes care of processing the DPI from a 
 * provided value (Slider value) among some other such functions.
 * 
 * @author dim8
 */
public class DPIManager {

	private static DPIManager instance = null;
	
	private static final String RESOURCE_PKG = "org.vanted.scaling.resources";
	/**
	 * A ratio between display height and emulated DPI. All values
	 * below this threshold are safe in all cases, in sense of 
	 * user-friendly (not too big/small UIDefaults size). */
	private static final int USABILITY_THRESHOLD = 22;
	
	/*--------Scaling Preferences-------- */
	static final String PREFERENCE_SCALING = "Scaling Preferences";
	/** Standard value, when no scaling is to be performed. Useful for resetting.*/
	public static final int STANDARD_VALUE = 50;
	/** Default value, when not setting any particular value. */
	public static final int VALUE_DEFAULT = -1;
	/** Internal value for checking if any values are stored. */
	public static final int VALUE_UNSET = -2;
	/** Specify when you get Preferences value. */
	public static final boolean PREFERENCES_GET = true;
	/** Specify when you set Preferences value. */
	public static final boolean PREFERENCES_SET = false;
	
	static final Preferences scalingPreferences = PreferenceManager
			.getPreferenceForClass(DPIManager.class);
	
	private static final String RESET_DIALOG_PREFS = "ResetDialogPreferences";
	
	public DPIManager() {
		instance = this;
	}
	
	public static DPIManager getInstance() {
		if (instance == null)
			instance = new DPIManager();
		
		return instance;
	}
	
	/**
	 * Converts a slider value into DPI Factor to be passed on to the 
	 * ScalingCoordinator. We firstly determine standard constants and 
	 * we use in the process the default slider values. For different
	 * than default, one should initialize a ScalingSlider with such
	 * new values.
	 * 
	 * @param sValue sliderValue (get it from Preferences)
	 * @return DPI Factor
	 */
	public static float processDPI(int sValue) {
		int standard = ScalingSlider.getStandard(); //also sets MIN_DPI for public use!
		
		//handling the artificial 0
		return (sValue == ScalingSlider.min)
				? ScalingSlider.MIN_DPI 
				: standard * (sValue / (float) ScalingSlider.median);
	}
	
	/**
	 * Test whether scaling is currently necessary.
	 * 
	 * @return true if Scaling could be skipped.
	 */
	public static boolean isAvoidable() {
		int value = DPIManager.managePreferences(VALUE_UNSET, PREFERENCES_GET);
		float factor = Toolkit.getDefaultToolkit().getScreenResolution() / 
				DPIManager.processDPI(value);
		
		/**
		 * ScalingSlider.managePreferences() called with the above
		 * combination of parameters returns a flag value for checking,
		 * if actually a value has ever been stored under the specified
		 * preferences. If there is some writing error at the time, this 
		 * would also affect scaling. Additionally, if the factor is the
		 * identity element, just avoid defaults & components iteration.
		 */
		if (value == VALUE_UNSET || factor == 1.0)
			return true;
		
		return false;
	}
	
	public void displayResetter() {
		int value = DPIManager.managePreferences(VALUE_DEFAULT, true);
		
		if (isSafe(value))
			return;
		
		if (hide())
			return;
		
		ImageIcon lifesaver = new ImageIcon(
				DPIManager.loadResource(ScalerLoader.class, "lifesaver.png"));
		String title = "Reset DPI";
		JFrame parent = new JFrame(title);
		parent.setIconImage(lifesaver.getImage());
	    parent.setUndecorated(true);
	    parent.pack();
		parent.setVisible(true);
	    parent.setLocationRelativeTo(null);
	    
		int selection = JOptionPane.showConfirmDialog(parent, getContents(value),
				title, JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, lifesaver);
		
		if (selection == JOptionPane.YES_OPTION) {
			//write
			DPIManager.managePreferences(50, false);
			//& flush
			try {
				DPIManager.flushPreferences();
			} catch (BackingStoreException e) {
				e.printStackTrace();
			}
		}
		
		parent.dispose();
	}
	
	/**
	 * Get the contents of the reset dialog ready.
	 * 
	 * @param value the ScalingSlider value, saved in Preferences
	 *  
	 * @return a JPanel, filled with all needed contents
	 */
	private JPanel getContents(int value) {
		int dpi = Math.round(DPIManager.processDPI(value));
		JPanel contents = new JPanel();
		contents.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		
		contents.add(new JLabel("<html>Emulated DPI is " + dpi + 
				". This could render interaction impossible.<br><br>"
				+ "Would you like to reset it?</html>"), c);
		
		JButton toDisable = new JButton("Disable");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipady = 0;
		c.weighty = 1.0;
		c.anchor = GridBagConstraints.PAGE_END;
		c.insets = new Insets(0, 0, 0, 10);
		c.gridx = 1; c.gridy = 1;
		toDisable.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				scalingPreferences.putBoolean(RESET_DIALOG_PREFS, true);
			}
		});
		contents.add(toDisable, c);
		
		return contents;
	}
	
	/**
	 * If unsafe, the Resetter is initiated, for the user to choose an option.
	 * 
	 * @param value value to check "safety" against
	 * 
	 * @return true if set Slider value is in the defined usability boundaries 
	 */
	private static boolean isSafe(int value) {
		int dpi = Math.round(DPIManager.processDPI(value));
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		float height = (float) gd.getDisplayMode().getHeight();
		int usabilityRatio = Math.round(height/dpi);
		
		if (usabilityRatio < USABILITY_THRESHOLD && value < ScalingSlider.max)
			return true;
		
		return false;
	}
	
	/**
	 * Check, if the user has permanently disabled the resetter. If so - 
	 * hide it.
	 * 
	 * @return true if Resetter has been disabled
	 */
	private static boolean hide() {
		boolean hide = scalingPreferences.getBoolean(RESET_DIALOG_PREFS, false);
		
		return hide;
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
		/**
		 * For internal use. Return old value or flag, indicating there are
		 * no stored values, avoid scaling with identity factor.
		 */
		if (get && val == VALUE_UNSET)
			return scalingPreferences.getInt(PREFERENCE_SCALING, VALUE_UNSET);
		
		//do not put new value
		if (get)
			return scalingPreferences.getInt(PREFERENCE_SCALING, ScalingSlider.median);
	
		scalingPreferences.putInt(PREFERENCE_SCALING, val);
				
		return VALUE_DEFAULT;
	}
	
	/**
	 * Write preferences to disk.
	 * 
	 * @throws BackingStoreException
	 */
	public static void flushPreferences() throws BackingStoreException {
		scalingPreferences.flush();
		PreferenceManager.storePreferences();
	}
	
	
	/**
	 * A resource loading utility method for resources placed in the resource package.
	 * 
	 * @param clazz delegates to the respective class loader
	 * @param filename the resource name
	 * 
	 * @return the loaded resource
	 * 
	 * @see {@link ScalerLoader#RESOURCE_PKG} 
	 * 
	 */
	public static URL loadResource(Class<?> clazz, String filename) {
		ClassLoader cl = clazz.getClassLoader();
		String path = RESOURCE_PKG.replace('.', '/');
		
		return	cl.getResource(path + "/" + filename);
	}
}