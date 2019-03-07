// ==============================================================================
//
// ComponentRegulator.java
//
// Copyright (c) 2017-2019, University of Konstanz
//
// ==============================================================================
package org.vanted.scaling;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.text.JTextComponent;

import org.ErrorMsg;
import org.vanted.scaling.scalers.component.AbstractButtonScaler;
import org.vanted.scaling.scalers.component.ComponentScaler;
import org.vanted.scaling.scalers.component.HTMLScaler;
import org.vanted.scaling.scalers.component.JLabelScaler;
import org.vanted.scaling.scalers.component.JOptionPaneScaler;
import org.vanted.scaling.scalers.component.JSplitPaneScaler;
import org.vanted.scaling.scalers.component.JTabbedPaneScaler;
import org.vanted.scaling.scalers.component.JTextComponentScaler;

/**
 * <i>Notice:</i> it is advisable to not access it directly, but through the
 * provided {@link ScalingCoordinator}, because of factor conversions.
 * 
 * This regulator is responsible for delegating the scaling external to the
 * LAF-defaults aspects of Swing components. It could be used for any user-set
 * modifications that have overwritten the LAF defaults. A typical example, are
 * any specified icons. Those are extracted from the component tree and rescaled
 * accordingly (see {@linkplain ComponentScaler} and its subtypes).
 * 
 * @author D. Garkov
 */
public class ComponentRegulator {

	private float factor;

	/** A set holding the hash-codes of the components with modified aspect. */
	private static HashSet<Integer> modified;

	/** The hash code of the modifed pool/set. */
	private static int modifiedHash = 0;
	/** The hash code of the pool right after an overall scaling. */
	private static int originalMomentHash;

	/* No scaling should be done, before the global scaling has run. */
	private static boolean isInitialized = false;

	/**
	 * This holds all relevant component classes (i.e. their superclasses up to
	 * JComponent) mapped to their respective scaler. Default one is also included -
	 * last.
	 */
	private static LinkedHashMap<Class<?>, ComponentScaler> scalers = new LinkedHashMap<>(); // TODO better inferring

	/**
	 * Constructs a ComponentRegulator instance for upcoming component scaling.
	 * 
	 * @param scaleFactor the working scaling factor, a ratio between current actual
	 *                    DPI and to be emulated DPI.
	 * 
	 * @see {@linkplain ScalingCoordinator}
	 * @see {@linkplain DPIHelper}
	 * 
	 */
	public ComponentRegulator(float scaleFactor) {
		factor = scaleFactor;
	}

	/**
	 * Global system DPI scaling init! Scan and scale the components specifics. This
	 * are taken from the specified parent {@link Container} <code>container</code>.
	 * <p>
	 * 
	 * <b>Important:</b> Register any newly implemented component scalers before
	 * calling this method! (see:
	 * {@linkplain ComponentRegulator#registerNewScaler()}}
	 * 
	 * @param container, application main frame/window/container
	 * 
	 * @throws OutOfMemoryError when the allocated heap memory is not enough to hold
	 *                          all largely scaled components, such as Icons.
	 */
	public void init(Container container) throws OutOfMemoryError {
		if (container == null)
			return;

		// ensure some extra capacity beforehand
		System.gc();

		doExternalScaling(container);

		isInitialized = true;
	}

	/**
	 * Worker scaling method. Consider using the utility method
	 * {@link ComponentRegulator#init(Container)} instead.
	 * 
	 * @param c container, whose components are to be modified
	 * 
	 * @throws OutOfMemoryError when the allocated heap memory is not enough to hold
	 *                          all largely scaled components, such as Icons.
	 */
	private void doExternalScaling(Container c) throws OutOfMemoryError {
		Container container;

		if (c instanceof Frame)
			container = ((JRootPane) ((Frame) c).getComponents()[0]);
		else
			container = c;

		clearModifiedPool();

		registerScalers();

		scaleComponentsOf(container);

		originalMomentHash = modified.hashCode();
	}

	/**
	 * Registers the necessary scalers to avoid instance creation explosion (one pro
	 * Component) and spare memory. Due to the supporting structure the inserting
	 * order matters.
	 */
	private void registerScalers() {
		// specific
		scalers.put(AbstractButton.class, new AbstractButtonScaler(factor));
		scalers.put(JLabel.class, new JLabelScaler(factor));
		scalers.put(JTextComponent.class, new JTextComponentScaler(factor));
		scalers.put(JOptionPane.class, new JOptionPaneScaler(factor));
		scalers.put(JSplitPane.class, new JSplitPaneScaler(factor));
		scalers.put(JTabbedPane.class, new JTabbedPaneScaler(factor));
		// default
		scalers.put(JComponent.class, new ComponentScaler(factor));

		// Prepare modification set
		if (modified == null)
			modified = new HashSet<>();
	}

	/**
	 * Recursively iterates the component tree and initiate scaling for any
	 * encountered JComponents.
	 * 
	 * <p>
	 * Important: Default behaviour is to first run system scaling and only then any
	 * other proxy scalers, to change this, call
	 * {@linkplain ComponentRegulator#override()}.
	 * </p>
	 * 
	 * @param container the container to crawl
	 */
	public void scaleComponentsOf(Container container) {
		scaleComponentsOf(container.getComponents());
	}

	/**
	 * See {@linkplain ComponentRegulator#scaleComponentsOf(Container)}.
	 * 
	 * @param container the container to crawl
	 */
	public void scaleComponentsOf(Component[] components) {
		if (!isInitialized)
			return;

		for (Component c : components) {
			// delegate further extraction
			if (c instanceof JComponent) {
				conduct((JComponent) c);

				// provide checking information
				addScaledComponent((JComponent) c);
			}

			// go further down recursively
			if (c instanceof Container)
				scaleComponentsOf((Container) c);
		}
	}

	/**
	 * Override of {@link ComponentRegulator#scaleComponentsOf(Container)}, allowing
	 * for checking whether Containers components are scaled and only when those
	 * aren't then scaling, and additionally marking them as such or not.
	 * {@link ComponentRegulator#scaleComponentsOf(Container)} doesn't check and
	 * marks all.
	 * 
	 * <p>
	 * Important: Default behaviour is to first run system scaling and only then any
	 * other proxy scalers, to change this, call
	 * {@linkplain ComponentRegulator#override()}.
	 * </p>
	 * 
	 * @param container
	 * @param checkScaled true to check components for being scaled and avoid double
	 *                    scaling
	 * @param mark        true to mark any scaled components as such
	 */
	public void scaleComponentsOf(Container container, boolean checkScaled, boolean mark) {
		scaleComponentsOf(container.getComponents(), checkScaled, mark);
	}

	/**
	 * See
	 * {@linkplain ComponentRegulator#scaleComponentsOf(Container, boolean, boolean)}
	 * and {@linkplain ComponentRegulator#scaleComponentsOf(Component[])}.
	 * 
	 * @param components
	 * @param check true to check components for being scaled and avoid double
	 *                    scaling
	 * @param mark        true to mark any scaled components as such
	 */
	public void scaleComponentsOf(Component[] components, boolean check, boolean mark) {
		if (!isInitialized) // Cannot run before the application DPI Scaling, initialize with init()
			return;

		if (!check) // marks component in any case
			scaleComponentsOf(components);
		else {
			for (Component c : components) {
				// delegate further extraction

				if (!(c instanceof JComponent))
					continue;

				boolean scale = check ? !isScaled((JComponent) c) : true;
				if (scale) {
					conduct((JComponent) c);

					if (mark)
						addScaledComponent((JComponent) c);
				}

				// go further down recursively
				if (c instanceof Container)
					scaleComponentsOf((Container) c, check, mark);
			}
		}
	}

	/**
	 * Override default behaviour to run proxy scalers before the
	 * application / init DPI scaler.
	 * 
	 * @return the proxy ComponentRegulator that has overridden the
	 * default behaviour.
	 */
	public ComponentRegulator override() {
		ComponentRegulator.isInitialized = true;
		return this;
	}

	/**
	 * Dynamically traffics the component to its compatible
	 * <code>ComponentScaler</code> for scaling.
	 * 
	 * @param component the to be scaled component
	 */
	private static void conduct(JComponent component) {
		ComponentScaler scaler;
		for (Entry<Class<?>, ComponentScaler> entry : scalers.entrySet()) {
			if (matches(component, entry.getKey())) {
				scaler = entry.getValue();
				scaler.scaleComponent(component);

				break;
			}
		}
	}

	/**
	 * Similar to {@linkplain ComponentRegulator#conduct(JComponent)}, but for HTML
	 * scaling. It crawls the list of scalers and matches the parameter to its
	 * appropriate scaler. It is responsibility of the matched scaler to have
	 * implemented such method.
	 * <p>
	 * 
	 * <b>Note:</b> No need to explicitly call this method, since it is part of the
	 * {@linkplain ComponentRegulator#init(Container)} workflow. Useful when
	 * implementing HTML support for a new scaler.
	 * 
	 * @param component
	 * 
	 * @see HTMLSupport
	 */
	public static void scaleHTML(JComponent component) {
		for (Entry<Class<?>, ComponentScaler> entry : scalers.entrySet()) {
			if (matches(component, entry.getKey())) {
				// through reflection
				ComponentScaler scaler = entry.getValue();
				try {
					if (!isInterfaceImplemented(scaler.getClass(), HTMLScaler.class))
						return;

					Method htmlModifier = scaler.getClass().getDeclaredMethod("coscaleHTML", JComponent.class);
					htmlModifier.invoke(scaler, component);
				} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					ErrorMsg.addErrorMessage(e);
				}

				break;
			}
		}
	}

	/**
	 * Returns true given there is a match between component and therefore
	 * responsible scaler. Dynamic equivalent of a terraced <code>instanceof</code>
	 * check.
	 * <p>
	 * 
	 * @param component the component being tested
	 * @param clazz     the class that may be a superclass of component
	 */
	private static boolean matches(JComponent component, Class<?> clazz) {
		return (clazz.isInstance(component));
	}

	/**
	 * Checks if interface contract between interface and supposed implementation is
	 * respected.
	 * 
	 * @param implementation
	 * @param interfaceClass
	 * 
	 * @return true if the interface has been implemented
	 */
	private static boolean isInterfaceImplemented(Class<?> implementation, Class<?> interfaceClass) {
		List<Class<?>> interfaces = Arrays.asList(implementation.getInterfaces());

		return interfaces.contains(interfaceClass);
	}

	/**
	 * Add the component to the set of already scaled components.
	 * 
	 * @param component to be stored component
	 */
	public static void addScaledComponent(JComponent component) {
		// Autoboxing comes into play
		modified.add(component.hashCode());
	}

	/**
	 * Registers new ComponentScaler to use. Does not overwrite previous mappings
	 * and just returns. Please, use before
	 * {@linkplain ComponentRegulator#init(Container)}.
	 * <p>
	 * 
	 * When adding new scalers be aware of the supertype-subtype relationship
	 * between the new and the already provided ones. In other words, the more
	 * generic the type, further back in the list should be placed.
	 * 
	 * @param superclass the super class, or possibly even direct class for
	 *                   overriding purposes.
	 * @param scaler     the newly implemented ComponentScaler
	 */
	public static void registerNewScaler(Class<?> superclass, ComponentScaler scaler) {
		if (scalers.containsKey(superclass))
			return;

		scalers.put(superclass, scaler);
	}

	public void setFactor(float factor) {
		this.factor = factor;
	}

	public float getFactor() {
		return factor;
	}

	/**
	 * A rough measure to determine whether <code>component</code> had one of his
	 * scaling specifics - Font, Icon, Insets; scaled.
	 * 
	 * @param component to be checked at runtime
	 * 
	 * @return true if the component has been scaled
	 */
	public static boolean isScaled(JComponent component) {
		if (modified == null)
			return false;

		return modified.contains(component.hashCode());
	}

	/**
	 * Reset the supporting data structure for reuse.
	 */
	public void clearModifiedPool() {
		if (modified != null) {
			modifiedHash = modified.hashCode();
			modified = null;
		}
	}

	/**
	 * Reset the supporting data structure for reuse.
	 */
	public void clearScalersMap() {
		if (scalers != null)
			scalers.clear();
	}

	/**
	 * True, when the modified pool has been recently cleared and re-created. Also
	 * tells us whether a new scaling has been performed at runtime.
	 * <p>
	 * 
	 * Useful for reseting a DPI value that is being kept in the caller class.
	 * 
	 * @return true when the pool with scaled components has been refilled. Once a
	 *         pool has been refilled (i.e <b>true</b>), it changes its state
	 *         effectively to filled (i.e <b>false</b>). Not filled also returns
	 *         <b>false</b>.
	 * 
	 * @see Toolbox#wasScalingPerformed()
	 */
	public static boolean isModifiedPoolRefilled() {
		if (modifiedHash != 0 && modifiedHash != originalMomentHash) {
			modifiedHash = originalMomentHash;
			return true;
		} else
			return false;
	}
}