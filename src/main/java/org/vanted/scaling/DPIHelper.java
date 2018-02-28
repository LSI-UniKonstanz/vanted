package org.vanted.scaling;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.net.URL;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.graffiti.managers.PreferenceManager;
import org.graffiti.options.AbstractOptionPane;
import org.vanted.scaling.resources.ImmutableCheckBox;
import org.vanted.scaling.scalers.component.WindowScaler;
import org.vanted.scaling.vanted.HighDPISupport;

//TODO remove PreferenceManager dependencies

/**
 * An utility helper that takes care of processing the DPI from a provided value
 * (Slider value) and is responsible for managing the scaling preferences among
 * some other useful functions in hand.
 * 
 * @author dim8
 */
public class DPIHelper {

	private static DPIHelper instance = null;

	private static final String RESOURCE_PKG = "org.vanted.scaling.resources";

	/*--------Scaling Preferences-------- */
	static final String PREFERENCE_SCALING = "Scaling Preferences";
	/** Standard value, when no scaling is to be performed. Useful for resetting. */
	public static final int STANDARD_VALUE = ScalingSlider.median;
	/** Default value, when not setting any particular value. */
	public static final int VALUE_DEFAULT = -1;
	/** Internal value for checking if any values are stored. */
	public static final int VALUE_UNSET_INTERNAL = -2;
	/** Specify when you get Preferences value. */
	public static final boolean PREFERENCES_GET = true;
	/** Specify when you set Preferences value. */
	public static final boolean PREFERENCES_SET = false;

	private static final String LIFESAVER_PARAM = "enable lifesaver";

	static final Preferences scalingPreferences = DPIHelper.loadPreferences(DPIHelper.class);

	private static boolean lifesaver_enabled = scalingPreferences.getBoolean(LIFESAVER_PARAM, true);

	public DPIHelper() {
		instance = this;
	}

	public static DPIHelper getInstance() {
		if (instance == null)
			instance = new DPIHelper();

		return instance;
	}

	/**
	 * Converts a slider value into an Emulated DPI value. We firstly determine
	 * standard constants and we use in the process the default slider values. For
	 * different than default, one should initialize a ScalingSlider with such new
	 * values.
	 * 
	 * @param sValue
	 *            sliderValue (get it from Preferences)
	 * @return float DPI value
	 */
	public static float processEmulatedDPIValue(int sValue) {
		int standard = ScalingSlider.getStandard(); // also sets MIN_DPI for public use!

		// handling the artificial 0
		return (sValue == ScalingSlider.min) ? ScalingSlider.MIN_DPI
				: standard * (sValue / (float) ScalingSlider.median);
	}

	/**
	 * Test whether scaling is currently necessary.
	 * 
	 * @return true if Scaling could be skipped.
	 */
	public static boolean isAvoidable() {
		int value = DPIHelper.managePreferences(VALUE_UNSET_INTERNAL, PREFERENCES_GET);
		int standard = (int) DPIHelper.processEmulatedDPIValue(value);

		/**
		 * DPIHelper.managePreferences() called with the above combination of parameters
		 * returns a flag value for checking, if value has ever been stored under the
		 * specified preferences. If there is some writing error at preferences flushing
		 * time, that would also affect scaling.
		 * 
		 * If the slider set value is the standard, then do not apply ... scaling.
		 */
		// TODO is unset value standard slider value?
		if (value == VALUE_UNSET_INTERNAL || standard == ScalingSlider.getStandard())
			return true;

		return false;
	}

	/**
	 * Insert the preferencing Components into the specified pane.
	 * 
	 * @param pane
	 *            to add into
	 */
	public static void addScalingComponents(AbstractOptionPane pane, Container main) {
		String name = "Emulated DPI";
		// first we add the label
		pane.addComponent(new JLabel(name));
		// afterwards - the actual component
		pane.addComponent("\t\t", new ScalingSlider(main));
		final JCheckBox lifesaver = UIManager.getLookAndFeel().getName().toLowerCase().contains("mac") ? new JCheckBox()
				: new ImmutableCheckBox();
		lifesaver.setSelected(DPIHelper.getLifesaverBoolean());
		lifesaver.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.DESELECTED)
					DPIHelper.putLifesaverValue(false);

				if (e.getStateChange() == ItemEvent.SELECTED)
					DPIHelper.putLifesaverValue(true);
			}
		});

		pane.addComponent("<html>Enable Lifesaver&emsp;&emsp;", lifesaver);

		addEmptyLine(pane);
		addEmptyLine(pane);
		addEmptyLine(pane);
		addEmptyLine(pane);
		addEmptyLine(pane);

		addResetter(pane);
	}

	private static void addEmptyLine(AbstractOptionPane pane) {
		JLabel emptyLine = new JLabel("<html>");
		pane.addComponent(emptyLine);
	}

	/**
	 * Builds and displays a reset button for user convenience.
	 * 
	 * @param pane
	 *            onto which to display
	 */
	private static void addResetter(AbstractOptionPane pane) {
		final JButton resetter = new JButton("Reset");

		resetter.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (ScalingSlider.getSliderValue() != STANDARD_VALUE)
					ScalingSlider.setSliderValue(STANDARD_VALUE);
			}
		});

		pane.addComponent(resetter, GridBagConstraints.LINE_END);

	}

	/**
	 * The CSS pixel <i>px</i> tries to match the reference pixel, which depends on
	 * screen DPI and viewer distance from screen. Here we obtain DPPX - the device
	 * pixel ratio. Thus we map pixels to provided number of pixels.
	 * 
	 * @param pixels
	 * @return
	 */
	public static int scaleCssPixels(int pixels) {
		return Math.round((pixels * Toolbox.getDPIScalingRatio()));
	}

	/**
	 * Attaches a WindowResizer listener that will resize any newly opened windows.
	 * This does not resize any already visible windows.
	 */
	public static void initWindowResizer() {
		WindowScaler.attachSystemWindowResizer();
	}

	/**
	 * Displays a reset dialog that prompts the user to reset the scaling or not,
	 * when the previous setting has gone out of reasonable scaling bounds and
	 * proper viewing has become impossible. There is also a 'Disable' option.
	 */
	public void displayLifesaver() {
		int value = DPIHelper.managePreferences(VALUE_DEFAULT, true);

		if (isSafe(value))
			return;

		if (hide())
			return;

		ImageIcon lifesaver = new ImageIcon(DPIHelper.loadResource(ScalerLoader.class, "lifesaver.png"));
		String title = "Reset DPI";
		JFrame parent = new JFrame(title);
		parent.setIconImage(lifesaver.getImage());
		parent.setUndecorated(true);
		parent.pack();
		parent.setVisible(true);
		parent.setLocationRelativeTo(null);

		int selection = JOptionPane.showConfirmDialog(parent, getContents(value), title, JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, lifesaver);

		if (selection == JOptionPane.YES_OPTION) {
			// write
			DPIHelper.managePreferences(STANDARD_VALUE, false);
			// & flush
			try {
				DPIHelper.flushPreferences();
			} catch (BackingStoreException e) {
				e.printStackTrace();
			}
		}

		parent.dispose();
	}

	/**
	 * Get the contents of the reset dialog ready.
	 * 
	 * @param value
	 *            the ScalingSlider value, saved in Preferences
	 * @return a JPanel, filled with all needed contents
	 */
	private JPanel getContents(int value) {
		int dpi = Math.round(DPIHelper.processEmulatedDPIValue(value));
		JPanel contents = new JPanel();
		contents.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;

		contents.add(new JLabel("<html>Emulated DPI is " + dpi + ". This could render interaction impossible.<br><br>"
				+ "Would you like to reset it?</html>"), c);

		final JButton toDisable = new JButton("Disable Lifesaver");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipady = 0;
		c.weighty = 1.0;
		c.anchor = GridBagConstraints.PAGE_END;
		c.insets = new Insets(0, 0, 0, 10);
		c.gridx = 1;
		c.gridy = 1;
		toDisable.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				lifesaver_enabled = false;
				scalingPreferences.putBoolean(LIFESAVER_PARAM, lifesaver_enabled);
				toDisable.setEnabled(false);
			}
		});
		contents.add(toDisable, c);

		return contents;
	}

	/**
	 * If unsafe, the Lifesaver is initiated, for the user to choose an option. For
	 * simplicity, as unsafe values are regarded those having less than one third of
	 * the standard DPI.
	 * 
	 * @param value
	 *            value to check "safety" against
	 * 
	 * @return true if set Slider value is in the defined usability boundaries
	 */
	private static boolean isSafe(int value) {
		int dpi = Math.round(DPIHelper.processEmulatedDPIValue(value));
		int limit = Math.round(ScalingSlider.getStandard() / (float) ScalingSlider.max * 30);

		if (dpi > limit)
			return true;

		return false;
	}

	public static boolean getLifesaverBoolean() {
		return lifesaver_enabled;
	}

	public static void putLifesaverValue(boolean value) {
		scalingPreferences.putBoolean(LIFESAVER_PARAM, value);
	}

	/**
	 * Check, if the user has permanently disabled the Lifesaver (resetter). If so -
	 * hide it.
	 * 
	 * @return true if Lifesaver has been disabled
	 */
	private static boolean hide() {
		return !scalingPreferences.getBoolean(LIFESAVER_PARAM, true);
	}

	/**
	 * This is a two-way internal method, used for all scaling-only
	 * preferences-related procedures. It handles both setting/putting and getting
	 * of values. To set values use <code>DPIHelper.PREFERENCES_SET</code> as <code>
	 * get</code> parameter. To get: <code>DPIHelper.PREFERENCES_GET</code>,
	 * respectively.
	 * 
	 * @param val
	 *            Our new ScalingSlider value. Used only, when putting into (i.e.
	 *            <b>get</b> is <b>false</b>). Meaning when querying values, you
	 *            could just use <code>
	 * DPIHelper.VALUE_DEFAULT</code>.
	 * 
	 * @param get
	 *            When <b>true</b>, it returns the previously stored value.
	 * 
	 * @return Stored value under 'Scaling Preferences' or <code>
	 * DPIHelper.VALUE_DEFAULT</code> for potential error checking, if <b>get</b> is
	 *         <b>false</b>.
	 */
	public static int managePreferences(int val, boolean get) {
		/**
		 * For internal use. Return old value or flag, indicating there are no stored
		 * values, avoid scaling with identity factor.
		 */
		if (get && val == VALUE_UNSET_INTERNAL)
			return scalingPreferences.getInt(PREFERENCE_SCALING, VALUE_UNSET_INTERNAL);

		// do not put new value
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
	 * Load the Preferences Pane.
	 */
	public static void loadPane() {
		PreferenceManager.getInstance().addPreferencingClass(HighDPISupport.class);
	}

	/**
	 * By transferring all Window decorations from OS to LookAndFeel, those could be
	 * now scaled too. Should be called before initialization of any JFrames or
	 * JDialogs, since it affects only subsequently created ones.
	 */
	public static void adjustWindowDecoratations() {
		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);
	}

	/**
	 * A resource loading utility method for resources placed in the resource
	 * package.
	 * 
	 * @param clazz
	 *            delegates to the respective class loader
	 * @param filename
	 *            the resource name
	 * 
	 * @return the loaded resource
	 * 
	 * @see {@link DPIHelper#RESOURCE_PKG}
	 * 
	 */
	public static URL loadResource(Class<?> clazz, String filename) {
		ClassLoader cl = clazz.getClassLoader();
		String path = RESOURCE_PKG.replace('.', '/');

		return cl.getResource(path + "/" + filename);
	}

	/**
	 * Fetches the respective Preferences for <code>clazz</code>.
	 * 
	 * @param clazz
	 * @return preferences instance
	 */
	public static Preferences loadPreferences(Class<?> clazz) {
		// Modified code from PreferenceManager.getPreferenceForClass()
		String pathName = clazz.getName().replace(".", File.separator);

		return Preferences.userRoot().node(pathName);
	}
}