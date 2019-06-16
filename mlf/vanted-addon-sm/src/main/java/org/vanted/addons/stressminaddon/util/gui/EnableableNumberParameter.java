package org.vanted.addons.stressminaddon.util.gui;

import org.graffiti.plugin.parameter.JComponentParameter;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

/**
 * The static methods in this class create special {@link JComponentParameter}s containing
 * a value similar to {@link org.graffiti.plugin.parameter.DoubleParameter} that has a more
 * precise internal {@link java.text.DecimalFormat} in case of an floating point argument so that the user can select
 * and write * smaller values.
 * The {@link JSpinner} can be enabled or disabled by using a provided checkbox.
 * To access the saved values {@link JComponentParameter#getValue()} can be used followed by
 * {@link #isEnabled()} or {@link #getValue()}.
 *
 * @author Jannik
 */
public class EnableableNumberParameter<T extends Number & Comparable<T>> extends JPanel {

    /** The decimal used by {@link java.text.DecimalFormat} to format the users input. */
    private static final String NUMBER_PATTERN = "#,##0.#############";

    /** The box to save whether the {@link JSpinner} is enabled. */
    final JCheckBox box;
    /** Holds a value that can be selected by the user. */
    final JSpinner value;

    /**
     * Creates a new parameter (which is a {@link JPanel}) that holds a {@link JSpinner} with a more granular
     * scale/format that can be enabled or disabled by the user.
     *
     * @param value the value that shall be used as start value for the {@link JSpinner}.
     * @param min the minimum value of the {@link JSpinner}.
     * @param max the maximum allowed value for the {@link JSpinner}.
     * @param steps the step with with each click on an button of the {@link JSpinner}.
     * @param alwaysEnabled whether this field is always enabled. The {@link JCheckBox} will
     *                      not be added to the component in this case.
     * @param enabled whether this field is enabled.
     * @param textEnabled
     *      the text to display on the enable/disable {@link JCheckBox} indicating that it is currently selected.
     * @param textDisabled
     *      the text to display on the enabled/disable {@link JCheckBox} indicating that it is currently not selected.
     *
     * @author Jannik
     */
    private EnableableNumberParameter(final T value, final T min, final T max, final T steps,
                                      final boolean alwaysEnabled, final boolean enabled, final String textEnabled, final String textDisabled) {
        this.setLayout(new BorderLayout());
        // set up box and JSpinner
        box = new JCheckBox();
        // dirty hack to prevent a floating fount box from oversizing
        this.value = new JSpinner(new SpinnerNumberModel(value, min, max, steps));
        JSpinner small = new JSpinner(new SpinnerNumberModel(value.intValue(), min.intValue(), max.intValue(), steps.intValue()));
        Dimension minDim = small.getMinimumSize();
        if (value instanceof Double || value instanceof Float || value instanceof BigDecimal) {
           this.value.setEditor(new JSpinner.NumberEditor(this.value, NUMBER_PATTERN));
        }
        box.setSelected(enabled || alwaysEnabled);
        this.value.setEnabled(enabled || alwaysEnabled);

        // Add special listener
        if (!alwaysEnabled) {
            box.setToolTipText(enabled ? textEnabled : textDisabled);
            box.addActionListener(x -> {
                this.value.setEnabled(box.isSelected());
                box.setToolTipText(box.isSelected() ?
                        textEnabled : textDisabled);
            });
        }

        if (!alwaysEnabled) {
            this.add(box, BorderLayout.WEST);
        }
        this.value.setMinimumSize(minDim);
        this.value.setPreferredSize(minDim);
        this.add(this.value, BorderLayout.CENTER);

        this.setVisible(true);
    }

    /**
     * Creates a new parameter (which is a {@link JPanel}) that holds a {@link JSpinner} with a more granular
     * scale/format that can be enabled or disabled by the user.
     *
     * @param value the value that shall be used as start value for the {@link JSpinner}.
     * @param min the minimum value of the {@link JSpinner}.
     * @param max the maximum allowed value for the {@link JSpinner}.
     * @param steps the step with with each click on an button of the {@link JSpinner}.
     * @param enabled whether this field is enabled.
     * @param textEnabled
     *      the text to display on the enable/disable {@link JCheckBox} indicating that it is currently selected.
     * @param textDisabled
     *      the text to display on the enabled/disable {@link JCheckBox} indicating that it is currently not selected.
     * @param name
     *      the name of the underlying {@link org.graffiti.plugin.parameter.Parameter}.
     * @param description
     *      the description of the underlying {@link org.graffiti.plugin.parameter.Parameter}.
     *
     * @return
     *      a {@link JComponentParameter} containing a {@link EnableableNumberParameter} as value.
     *
     * @author Jannik
     */
    public static <T extends Number & Comparable<T>>
        JComponentParameter canBeEnabledDisabled(T value, T min, T max, T steps,
                                                 boolean enabled, String textEnabled, String textDisabled,
                                                 String name, String description) {
       return new JComponentParameter(new EnableableNumberParameter<>(
               value, min, max, steps, false, enabled, textEnabled, textDisabled),
               name, description);
    }

    /**
     * Creates a new double parameter (which is a {@link JPanel}) that holds a {@link JSpinner} with a more granular
     * scale/format. It is always enabled.
     *
     * @param value the value that shall be used as start value for the {@link JSpinner}.
     * @param min the minimum value of the {@link JSpinner}.
     * @param max the maximum allowed value for the {@link JSpinner}.
     * @param steps the step with with each click on an button of the {@link JSpinner}.
     * @param name
     *      the name of the underlying Doub{@link org.graffiti.plugin.parameter.Parameter}.
     * @param description
     *      the description of the underlying {@link org.graffiti.plugin.parameter.Parameter}.
     *
     * @return
     *      a {@link JComponentParameter} containing a {@link EnableableNumberParameter} as argument.
     *
     * @author Jannik
     */
    public static <T extends Number & Comparable<T>> JComponentParameter alwaysEnabled(T value, T min, T max, T steps,
                                                          String name, String description) {
        return new JComponentParameter(new EnableableNumberParameter<>(
                value, min, max, steps, true, true, null, null),
                name, description);
    }

    /**
     * @return whether the user has enabled the text input.
     *
     * @author Jannik
     */
    @Override
    public boolean isEnabled() {
        return box.isSelected();
    }

    /**
     * @return the value selected by the {@link JSpinner}.
     *
     * @author Jannik
     */
    public T getValue() {
        return (T) value.getValue();
    }
}
