package org.vanted.addons.stressminimization;

import org.SystemInfo;
import org.vanted.addons.stressminimization.parameters.SliderParameter;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Dictionary;

public class ParamUIFactory {

    public static JSlider createSlider(int min, int max, int initialValue) {
        JSlider slider = new JSlider();
        slider.setBackground(null);
        slider.setMinimum(min);
        slider.setMaximum(max);
        // todo: setMinorTickSpacing, setMajorTickSpacing
        slider.setPaintLabels(true);
        slider.setPaintTicks(true);
        if (SystemInfo.isMac()) slider.setPaintTrack(false);
        slider.setValue(initialValue);
        return slider;
    }

    public static JSlider createSlider(int min, int max, Dictionary labels, int initialValue) {
        JSlider slider = createSlider(min, max, initialValue);
        slider.setLabelTable(labels);
        return slider;
    }

}
