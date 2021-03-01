// ==============================================================================
//
// Toolbox.java
//
// Copyright (c) 2017-2019, University of Konstanz
//
// ==============================================================================
package org.vanted.scaling;

import java.awt.Toolkit;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import org.vanted.scaling.AutomatonBean.State;
import org.vanted.scaling.scalers.component.*;

/**
 * An utility class holding some handy methods for tweaking the scaling of some
 * components, where necessary.
 * 
 * @author D. Garkov
 */
public class Toolbox {
	
	public static final String STATE_UNSCALED = "UNSCALED";
	public static final String STATE_ON_START = "ON_START";
	public static final String STATE_ON_SLIDER = "ON_SLIDER";
	public static final String STATE_RESCALED = "RESCALED";
	public static final String STATE_IDLE = "IDLE";
	
	public static final int UL_TYPE_DISC = 100;
	public static final int UL_TYPE_CIRCLE = 101;
	public static final int UL_TYPE_SQUARE = 102;
	public static final int UL_TYPE_EXTRA = 103;
	
	private static final int prefsSliderValue = DPIHelper.managePreferences(DPIHelper.VALUE_DEFAULT,
			DPIHelper.PREFERENCES_GET);
	
	public Toolbox() {
	}
	
	/**
	 * Adds a new component scaler to the automatic scaling routine.
	 * 
	 * @param jComponentSuperclass
	 *           the super JComponent for which the scaler is
	 * @param scaler
	 *           newly implemented ComponentScaler sub-type
	 * @see ComponentRegulator#registerNewScaler(Class, ComponentScaler)
	 */
	public static void registerScaler(Class<?> jComponentSuperclass, ComponentScaler scaler) {
		ComponentRegulator.registerNewScaler(jComponentSuperclass, scaler);
	}
	
	/**
	 * Add a PropertyChangeListener to be informed about the workflow of the scaling
	 * procedures. If the listener has already been added, it does nothing. This
	 * could be useful when coordinating the scaling of any extra components that
	 * haven't been scaled globally. For instance, components that are initialized
	 * after the scaling is done or any that change their layout continuously. Then
	 * their respective scaler could be dispatched. One should listen for property
	 * change on the one of the three active states: <i>onStart</i>, <i>onSlider</i>
	 * and <i>rescaled</i>.
	 * <p>
	 * For more information see {@linkplain AutomatonBean} documentation.
	 * 
	 * @param listener
	 */
	public static void addScalingListener(PropertyChangeListener listener) {
		if (AutomatonBean.getInstance() == null) {
			AutomatonBean ab = new AutomatonBean();
			ab.addPropertyChangeListener(listener);
		} else
			AutomatonBean.getInstance().addPropertyChangeListener(listener);
	}
	
	/**
	 * Remove a previously add PropertyChangeListener. Nothing happens, if the
	 * removal fails.
	 * 
	 * @param listener
	 */
	public static void removeScalingListener(PropertyChangeListener listener) {
		if (!(AutomatonBean.getInstance() == null))
			AutomatonBean.getInstance().removePropertyChangeListener(listener);
	}
	
	/**
	 * This method could be used to add any number of ChangeListeners, so that you
	 * could synchronize any actions with the movement of the slider, i.e. the live
	 * scaling itself.
	 * 
	 * @param changeListeners
	 */
	public static void registerSliderChangeListeners(ChangeListener[] changeListeners) {
		ScalingSlider.registerChangeListeners(changeListeners);
	}
	
	/**
	 * Get the current state of the scaling workflow.
	 */
	public static String getCurrentState() {
		return AutomatonBean.getState();
	}
	
	/**
	 * Access other than the current states by value. The case doesn't matter.
	 * Alternatively, one could use the String State fields.
	 * 
	 * @param value
	 *           Possible values are:
	 *           <p>
	 *           <b>unscaled</b>, <b>rescaled</b>,<br>
	 *           <b>onStart</b>, <b>onSlider</b>,<br>
	 *           <b>idle</b>.
	 * @return the state or an exception
	 * @throws IllegalArgumentException
	 *            if the value is different from the allowed
	 *            names.
	 * @throws NullPointerException
	 *            if {@code value} is null.
	 */
	public static State getState(String value) {
		return Enum.valueOf(AutomatonBean.State.class, value.toUpperCase());
	}
	
	/**
	 * A shortcut method for {@linkplain ComponentRegulator#isScaled(JComponent)}
	 * that checks at runtime whether a component has been scaled with the most
	 * recent scaling procedure.
	 * 
	 * @param component
	 *           to be tested
	 * @return true if the component has been scaled
	 */
	public static boolean isComponentScaled(JComponent component) {
		return ComponentRegulator.isScaled(component);
	}
	
	/**
	 * Another shortcut utility method, which determines whether a new scaling
	 * procedure has been performed at runtime. It is regarded as <i>new</i> only on
	 * the first call, after the DPI was changed. Any subsequent calls will be
	 * regarded as not new. This conform to the underlying method.
	 * 
	 * @return true if scaled anew at runtime.
	 * @see ComponentRegulator#isModifiedPoolRefilled()
	 */
	public static boolean wasScalingPerformed() {
		return ComponentRegulator.isModifiedPoolRefilled();
	}
	
	/**
	 * This is the scaling factor that could be passed onto a ComponentScaler to
	 * directly scale an exception component, for example, without going through the
	 * whole component tree, when using the ScalingCoordinator. If the user hasn't
	 * performed any scaling operations so far, nothing will happen.
	 * 
	 * @return a ready to use scaling factor to use directly with scalers
	 */
	public static float getDPIScalingRatio() {
		try {
			if (ScalingSlider.getSliderValue() > 0)
				return Toolkit.getDefaultToolkit().getScreenResolution()
						/ DPIHelper.processEmulatedDPIValue(ScalingSlider.getSliderValue());
		} catch (NullPointerException e) {
			// ok, go to next return
		}
		
		return Toolkit.getDefaultToolkit().getScreenResolution() / DPIHelper.processEmulatedDPIValue(prefsSliderValue);
		
	}
	
	/**
	 * Prepares an &lt;ul> by removing any style-type elements, such as bullets,
	 * squares, circles or discs with none. Then the removed character ought be
	 * added as part of the contents to simulate styling. E.g.:
	 * <p>
	 * &lt;li>&#x25CF; Fist item &lt;/li><br>
	 * &lt;li>&#x25CF; Second item &lt;/li>
	 * <p>
	 * Additionally, the padding is adjusted as well.
	 * <p>
	 * <b><i>Warning: </b></i> The JEditorPane should have as mime type "html".
	 * 
	 * @param ep
	 * @param type
	 *           one of the constant types
	 * @param extra
	 *           if some other specific type is necessary
	 *           ({@linkplain Toolbox#UL_TYPE_EXTRA})
	 */
	public static void scaleJEditorPaneUnorderedLists(JEditorPane ep, int type, String extra) {
		HTMLEditorKit ekit = (HTMLEditorKit) ep.getEditorKit();
		StyleSheet stylesheet = ekit.getStyleSheet();
		String pad = "padding:" + String.valueOf(DPIHelper.scaleCssPixels(12)) + "px;";
		stylesheet.addRule("ul {list-style-type: none;" + pad + "}");
		
		String echar;
		switch (type) {
			case UL_TYPE_DISC:
				echar = "&#x25CF; ";
				break;
			case UL_TYPE_CIRCLE:
				echar = "&#x25CB; ";
				break;
			case UL_TYPE_SQUARE:
				echar = "&#x25A0; ";
				break;
			case UL_TYPE_EXTRA:
				echar = extra + " ";
				break;
			default:
				echar = "";
		}
		
		String text = "";
		for (String t : ep.getText().split("<li>"))
			text += t + "<li>" + echar;
		ep.setText(text);
	}
	
	/**
	 * Reset the scaling of {@code component} and its children. This does count as
	 * actual scaling and also does not check whether components have been scaled.
	 * 
	 * @param component
	 *           whose scaling shall be reset (and of its children)
	 * @param previousDPIRatio
	 *           the previous ratio
	 */
	public static void resetScalingOf(JComponent component, float previousDPIratio) {
		float resetRatio = previousDPIratio / Toolkit.getDefaultToolkit().getScreenResolution();
		// TODO check and reset only scaled
		// TODO save previousDPIRatio internally and use here (in overload)
		assignScaler(component, resetRatio, false, false);
		
	}
	
	/**
	 * Version of {@linkplain Toolbox#scaleComponent(JComponent, float, boolean)}
	 * using the default DPI scaling ratio.
	 * 
	 * @param component
	 *           container to start scaling from
	 * @param check
	 *           if true it will make sure to avoid doubly scaling
	 */
	public static void scaleComponent(JComponent component, boolean check) {
		scaleComponent(component, getDPIScalingRatio(), check);
	}
	
	/**
	 * This automatically marks the component and its children as scaled, while
	 * doing so.
	 * 
	 * @param component
	 *           container to start scaling from
	 * @param DPIratio
	 *           see {@linkplain Toolbox#getDPIScalingRatio()}
	 * @param check,
	 *           if true it will make sure to avoid doubly scaling
	 */
	public static void scaleComponent(JComponent component, float DPIratio, boolean check) {
		if (DPIratio == 1f)
			return;
		assignScaler(component, DPIratio, check, true);
	}
	
	public static int getSliderValue() {
		return ScalingSlider.getSliderValue();
	}
	
	private static void assignScaler(JComponent component, float factor, boolean check, boolean mark) {
		// scale all
		if (component.getComponentCount() > 0) {
			if (check)
				new ComponentRegulator(factor).scaleComponentsOf(component, check, mark);
			else
				new ComponentRegulator(factor).scaleComponentsOf(component);
		} else {
			// scale top-most only
			if ((check && Toolbox.isComponentScaled(component)) || !ComponentRegulator.isInitialized)
				return;
			
			if (component instanceof JLabel)
				new JLabelScaler(factor).scaleComponent(component);
			else if (component instanceof AbstractButton)
				new AbstractButtonScaler(factor).scaleComponent(component);
			else if (component instanceof JTextComponent)
				new JTextComponentScaler(factor).scaleComponent(component);
			else if (component instanceof JOptionPane)
				new JOptionPaneScaler(factor).scaleComponent(component);
			else if (component instanceof JTabbedPane)
				new JTabbedPaneScaler(factor).scaleComponent(component);
			else
				new ComponentScaler(factor).scaleComponent(component);
			
			if (mark)
				ComponentRegulator.addScaledComponent(component);
		}
	}
	
	/**
	 * By overriding the order for all, you can allow proxy scalers to run before
	 * the application DPI scaling init. This may lead to Exceptions. To scale a
	 * specific single component, you can just use one of the component scalers
	 * directly.
	 */
	public static void overrideExecutionOrder() {
		ComponentRegulator.overrideForAll();
	}
}
