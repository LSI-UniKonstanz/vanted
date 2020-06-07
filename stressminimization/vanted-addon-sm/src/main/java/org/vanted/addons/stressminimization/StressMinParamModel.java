package org.vanted.addons.stressminimization;

import info.clearthought.layout.SingleFiledLayout;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.text.ParseException;
import java.util.*;

/**
 * This is the "model" (in a stateful sense) representing the current settings of the algorithm.
 */
public class StressMinParamModel {

    public InitialLayoutRadioGroup initialLayoutRadioGroup = new InitialLayoutRadioGroup();
    public MethodRadioGroup methodRadioGroup = new MethodRadioGroup();
    MethodAlphaGroup methodAlphaGroup = new MethodAlphaGroup();
    TerminationStressChangeGroup terminationStressChangeGroup = new TerminationStressChangeGroup();
    TerminationCheckboxGroup terminationCheckboxGroup = new TerminationCheckboxGroup();


    public enum InitialLayoutOption {
        useDisplayed, useRandom;

        @Override
        public String toString() {
            if (this == useDisplayed) {
                return "Use displayed layout";
            } else {
                return "Use random initial layout";
            }
        }
    }


    public enum MethodOption {
        full, landmarksOnly;

        @Override
        public String toString() {
            if (this == full) {
                return "Run optimisation on all nodes";
            } else {
                return "On landmarks only";
            }
        }
    }

    public class RadiosOFEnum<E extends Enum<E>> {
        //List<JRadioButton> radios = new ArrayList<>();
        Map<E, JRadioButton> enumRadioMap = new HashMap<>();
        ButtonGroup group = new ButtonGroup();
        E selected;
        Class<E> theEnum;

        RadiosOFEnum(Class<E> theEnum) {
            this.theEnum = theEnum;
            for (E option : theEnum.getEnumConstants()) {
                JRadioButton radio = new JRadioButton(option.toString());
                radio.setBackground(null);
                radio.addActionListener((e) -> {
                    this.selected = option;
                });
                enumRadioMap.put(option, radio);
                group.add(radio);
            }
            group.getElements().nextElement().setSelected(true);
            selected = theEnum.getEnumConstants()[0];
        }

        public E getSelected() {
            return this.selected;
        }
    }

    public class InitialLayoutRadioGroup extends JPanel {
        RadiosOFEnum<InitialLayoutOption> radios = new RadiosOFEnum<>(InitialLayoutOption.class);

        InitialLayoutRadioGroup() {
            this.setLayout(new SingleFiledLayout(SingleFiledLayout.COLUMN, SingleFiledLayout.FULL, 0));
            this.setBackground(null);
            for (JRadioButton radio : radios.enumRadioMap.values()) {
                this.add(radio);
            }
        }

        public InitialLayoutOption getSelected() {
            return radios.getSelected();
        }
    }

    private class PercentageSlider extends JSlider {
        PercentageSlider() {
            this.setBackground(null);
            this.setMinimum(0);
            this.setMaximum(100);
            this.setMinorTickSpacing(5);
            this.setMajorTickSpacing(25);
            this.setLabelTable(this.createStandardLabels(25));
            this.setPaintLabels(true);
            this.setPaintTicks(true);
            // TODO: put % sign on labels
        }
    }

    public class MethodRadioGroup extends JPanel {
        RadiosOFEnum<MethodOption> radios = new RadiosOFEnum<>(MethodOption.class);
        PercentageSlider percSlider = new PercentageSlider();

        MethodRadioGroup() {
            this.setBackground(null);
            this.setLayout(new SingleFiledLayout(SingleFiledLayout.COLUMN, SingleFiledLayout.FULL, 0));
            JRadioButton fullRadio = radios.enumRadioMap.get(MethodOption.full);
            JRadioButton lmRadio = radios.enumRadioMap.get(MethodOption.landmarksOnly);
            JPanel sliderContainer = new JPanel();
            JLabel percSliderLabel = new JLabel("Percentage of nodes to be used as landmarks:");
            percSliderLabel.setForeground(Color.LIGHT_GRAY);
            fullRadio.setAlignmentX(0);
            lmRadio.setAlignmentX(0);
            this.add(fullRadio);
            fullRadio.addActionListener(e -> {
                percSlider.setEnabled(false);
                percSliderLabel.setForeground(Color.LIGHT_GRAY);
            });
            this.add(lmRadio);
            lmRadio.addActionListener(e -> {
                percSlider.setEnabled(true);
                percSliderLabel.setForeground(Color.BLACK);
            });

            percSlider.setBorder(new EmptyBorder(5, 0, 0, 35));
            sliderContainer.setBackground(null);
            sliderContainer.setLayout(new SingleFiledLayout(SingleFiledLayout.COLUMN, SingleFiledLayout.FULL, 0));
            sliderContainer.setBorder(new EmptyBorder(5, 22, 0, 35));
            sliderContainer.add(percSliderLabel);
            sliderContainer.add(percSlider);
            this.add(sliderContainer);

            percSlider.setEnabled(radios.enumRadioMap.get(MethodOption.landmarksOnly).isSelected());
        }

        public MethodOption getSelected() {
            return radios.getSelected();
        }

        public boolean useLandmarks() {
            return (radios.getSelected() == MethodOption.landmarksOnly);
        }

        public double getSliderValue() {
            return (double) percSlider.getValue() / 100;
        }

        public double getSliderValuePos() {
            if (getSliderValue() <= 0.01) {
                return 0.01;
            } else {
                return getSliderValue();
            }
        }
    }

    public class MethodAlphaGroup extends JPanel {
        JSlider slider = new JSlider();

        MethodAlphaGroup() {

            GridBagLayout gridBagLayout = new GridBagLayout();
            gridBagLayout.columnWidths = new int[]{0, 30, 35, 32};
            gridBagLayout.rowHeights = new int[]{20, 0, 0};
            gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0};
            gridBagLayout.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
            setLayout(gridBagLayout);

            Component verticalStrut = Box.createVerticalStrut(5);
            GridBagConstraints gbc_verticalStrut = new GridBagConstraints();
            gbc_verticalStrut.insets = new Insets(0, 0, 5, 5);
            gbc_verticalStrut.gridx = 0;
            gbc_verticalStrut.gridy = 0;
            add(verticalStrut, gbc_verticalStrut);

            JLabel lblNewLabel = new JLabel("Weight exponent:");
            GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
            gbc_lblNewLabel.fill = GridBagConstraints.HORIZONTAL;
            gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
            gbc_lblNewLabel.gridx = 0;
            gbc_lblNewLabel.gridy = 1;
            add(lblNewLabel, gbc_lblNewLabel);

            GridBagConstraints gbc_slider = new GridBagConstraints();
            gbc_slider.anchor = GridBagConstraints.SOUTH;
            gbc_slider.fill = GridBagConstraints.HORIZONTAL;
            gbc_slider.insets = new Insets(0, 0, 0, 5);
            gbc_slider.gridx = 2;
            gbc_slider.gridy = 1;
            add(slider, gbc_slider);

            Component horizontalGlue = Box.createHorizontalGlue();
            GridBagConstraints gbc_horizontalGlue = new GridBagConstraints();
            gbc_horizontalGlue.fill = GridBagConstraints.HORIZONTAL;
            gbc_horizontalGlue.gridx = 3;
            gbc_horizontalGlue.gridy = 1;
            add(horizontalGlue, gbc_horizontalGlue);


            // do not overwrite this on paste
            setBackground(null);
            slider.setBackground(null);
            slider.setMinimum(0);
            slider.setMaximum(2);
            slider.setValue(1);
            Dictionary sliderLabels = new Hashtable();
            sliderLabels.put(0, new JLabel("0"));
            sliderLabels.put(1, new JLabel("-1"));
            sliderLabels.put(2, new JLabel("-2"));
            slider.setLabelTable(sliderLabels);
            slider.setPaintLabels(true);
            slider.setPaintTicks(false);

            /*this.setBorder(new EmptyBorder(15, 0, 0, 0));
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.setBackground(null);
            JLabel label = new JLabel("Weight exponent:");
            label.setAlignmentX(LEFT_ALIGNMENT);
            label.setBackground(null);
            this.add(label);
            slider.setAlignmentX(LEFT_ALIGNMENT);
            slider.setMaximumSize(new Dimension(125, Short.MAX_VALUE));
            slider.setBorder(new EmptyBorder(5, 0, 0, 35));
            slider.setBackground(null);
            slider.setMinimum(0);
            slider.setMaximum(2);
            slider.setLabelTable(slider.createStandardLabels(1));
            slider.setPaintLabels(true);
            slider.setPaintTicks(true);
            this.add(slider);*/
        }

        public int getSliderValue() {
            return slider.getValue();
        }

        public int getAlpha() {
            return slider.getValue();
        }
    }

    private class LabeledSlider extends JPanel {
        private final JSlider slider;
        private final JLabel label;

        LabeledSlider(String labelText, JSlider slider) {
            this.setBackground(null);
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.slider = slider;
            this.slider.setBorder(new EmptyBorder(5, 15, 5, 15));
            this.slider.setAlignmentX(LEFT_ALIGNMENT);
            this.slider.setBackground(null);
            this.slider.setPaintTicks(true);
            this.label = new JLabel(labelText);
            this.label.setBackground(null);
            this.label.setAlignmentX(LEFT_ALIGNMENT);
            this.add(label);
            this.add(slider);
        }

        public JSlider getSlider() {
            return this.slider;
        }
    }

    public class TerminationStressChangeGroup extends JPanel {
        JSlider slider = new JSlider();

        TerminationStressChangeGroup() {
            Dictionary labels = new Hashtable();
            labels.put(-9, new JLabel("0"));
            labels.put(-8, new JLabel("10\u207b\u2078"));
            labels.put(-6, new JLabel("10\u207b\u2076"));
            labels.put(-4, new JLabel("10\u207b\u2074"));
            labels.put(-2, new JLabel("10\u207b\u00B2"));
            slider.setMinimum(-9);
            slider.setMaximum(-1);
            slider.setMajorTickSpacing(1);
            slider.setLabelTable(labels);
            slider.setPaintLabels(true);
            this.setBackground(null);
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            LabeledSlider withLabel = new LabeledSlider("Stress change threshold (always active):", slider);
            withLabel.setAlignmentX(LEFT_ALIGNMENT);
            // todo: construct this specific slider here
            this.add(withLabel);
        }

        public double getValue() {
            int val = slider.getValue();
            if (val == -9) return 0;
                // note that values are negative
            else return Math.pow(10, slider.getValue());
        }
    }

    public class TerminationCheckboxGroup extends JPanel {

        JSpinner spinner = new JSpinner();
        JSpinner spinner_1 = new JSpinner();
        JCheckBox movementCheckbox = new JCheckBox("Node movement threshold:");
        JCheckBox iterCheckbox = new JCheckBox("Max. number of iterations:");

        TerminationCheckboxGroup() {

            GridBagLayout gridBagLayout = new GridBagLayout();
            gridBagLayout.columnWidths = new int[]{0, 30, 95, 0, 10};
            gridBagLayout.rowHeights = new int[]{20, 0, 0, 0};
            gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0};
            gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
            setLayout(gridBagLayout);

            Component verticalStrut = Box.createVerticalStrut(5);
            GridBagConstraints gbc_verticalStrut = new GridBagConstraints();
            gbc_verticalStrut.insets = new Insets(0, 0, 5, 5);
            gbc_verticalStrut.gridx = 0;
            gbc_verticalStrut.gridy = 0;
            add(verticalStrut, gbc_verticalStrut);

            movementCheckbox.setHorizontalAlignment(SwingConstants.LEFT);
            GridBagConstraints gbc_chckbxNewCheckBox = new GridBagConstraints();
            gbc_chckbxNewCheckBox.anchor = GridBagConstraints.WEST;
            gbc_chckbxNewCheckBox.insets = new Insets(0, 0, 5, 5);
            gbc_chckbxNewCheckBox.gridx = 0;
            gbc_chckbxNewCheckBox.gridy = 1;
            add(movementCheckbox, gbc_chckbxNewCheckBox);

            spinner.setEnabled(false);
            spinner.setModel(new SpinnerNumberModel(new Integer(0), new Integer(0), null, new Integer(10)));
            GridBagConstraints gbc_spinner = new GridBagConstraints();
            gbc_spinner.fill = GridBagConstraints.HORIZONTAL;
            gbc_spinner.insets = new Insets(0, 0, 5, 5);
            gbc_spinner.gridx = 2;
            gbc_spinner.gridy = 1;
            add(spinner, gbc_spinner);

            GridBagConstraints gbc_chckbxNewCheckBox_1 = new GridBagConstraints();
            gbc_chckbxNewCheckBox_1.anchor = GridBagConstraints.WEST;
            gbc_chckbxNewCheckBox_1.insets = new Insets(0, 0, 0, 5);
            gbc_chckbxNewCheckBox_1.gridx = 0;
            gbc_chckbxNewCheckBox_1.gridy = 2;
            add(iterCheckbox, gbc_chckbxNewCheckBox_1);

            spinner_1.setEnabled(false);
            spinner_1.setModel(new SpinnerNumberModel(new Integer(0), new Integer(0), null, new Integer(10)));
            GridBagConstraints gbc_spinner_1 = new GridBagConstraints();
            gbc_spinner_1.fill = GridBagConstraints.HORIZONTAL;
            gbc_spinner_1.insets = new Insets(0, 0, 0, 5);
            gbc_spinner_1.gridx = 2;
            gbc_spinner_1.gridy = 2;
            add(spinner_1, gbc_spinner_1);

            // do not overwrite this when pasting

            spinner_1.setValue(100);

            movementCheckbox.addItemListener(e -> spinner.setEnabled(e.getStateChange() == ItemEvent.SELECTED));
            iterCheckbox.addItemListener(e -> spinner_1.setEnabled(e.getStateChange() == ItemEvent.SELECTED));

            setBackground(null);
            movementCheckbox.setBackground(null);
            iterCheckbox.setBackground(null);
        }

        public boolean nodeThresholdActive() {
            return movementCheckbox.isSelected();
        }

        public boolean iterThresholdActive() {
            return iterCheckbox.isSelected();
        }

        public int getNodeMovementThreshold() {
            try {
                spinner.commitEdit();
            } catch (ParseException e) {
                MainFrame.showMessage("Error parsing node movement threshold", MessageType.ERROR);
            }
            return (int) spinner.getValue();
        }

        public int getMaxIterations() {
            try {
                spinner_1.commitEdit();
            } catch (ParseException e) {
                MainFrame.showMessage("Error parsing max iterations", MessageType.ERROR);
            }
            return (int) spinner_1.getValue();
        }

    }

/*
    // todo: update names and descriptions to be more informative

    public IntegerParameter numberOfLandmarks = new LandmarkParameter(
            100,
            10, // values below this give no more significant speed up
            1000, // values above this consume to much time in the current implementation
            "Landmarks Count",
            "The number of nodes that will be mainly layouted. All remaining nodes will be positioned relatively to these nodes. You may turn off this option by selecting all nodes as landmarks."
    );

    public BooleanParameter landMarkPreprocessing = new BooleanParameter(
            false,
            "Disable landmark preprocessing",
            "Turn of landmark preprocessing. In general this preprocessing speeds up execution, but will slow it down if you have an optimized initial layout (since that may be destroyed) or if you are using this layouter in a multilevel framework."
    );

    private SliderParameter createEpsilonParam() {
        Dictionary dict = new Hashtable();
        dict.put(-9, new JLabel("0"));
        dict.put(-8, new JLabel("10\u207b\u2078"));
        dict.put(-6, new JLabel("10\u207b\u2076"));
        dict.put(-4, new JLabel("10\u207b\u2074"));
        dict.put(-2, new JLabel("10\u207b\u00B2"));
        return (new SliderParameter(
                -4,
                "Stress change threshold",
                "Change in stress of succeeding layouts threshold. If the change is below this threshold the layouter will terminate. Low values will give better layouts, but computation will consume more time.",
                -8, -1, false, true, dict
        ));
    }
    public SliderParameter stressChangeThreshold = createEpsilonParam();

    public DoubleParameter minimumNodeMovementThreshold = new DoubleParameter(
            0.0,
            0.0,
            1.0,
            0.05,
            "Node movement threshold",
            "Minimum required movement of any node for continuation of computation. If all nodes move less than this value in an iteration, execution is terminated."
    );

    public SliderParameter initialStressPercentage = createStressPercentageParam();

    private SliderParameter createStressPercentageParam() {
        Dictionary stressDict = new Hashtable();
        stressDict.put(0, new JLabel("0%"));
        stressDict.put(50, new JLabel("50%"));
        stressDict.put(100, new JLabel("100%"));
        return (new SliderParameter(
                0,
                "Initial stress percentage threshold",
                "If the stress of a layout falls below this percentage of the stress on the beginning of the core process (after any preprocessing), execution is terminated.",
                0,100, false, false, stressDict
        ));
    }

    public SliderParameter iterationsThreshold = createIterationsThresholdParam();

    private SliderParameter createIterationsThresholdParam() {
        Dictionary iterDict = new Hashtable();
        iterDict.put(25, new JLabel("25"));
        iterDict.put(50, new JLabel("50"));
        iterDict.put(75, new JLabel("75"));
        iterDict.put(100, new JLabel("100"));
        iterDict.put(125, new JLabel("\u221e"));
        return (new SliderParameter(
                75,
                "Maximum number of iterations",
                "Maximum number of iterations after which algorithm excution will be terminated.",
                25, 124, true, false, iterDict
        ));
    }

    public SliderParameter alpha = new SliderParameter(
            2,
            "Weight Factor",
            "Sets the importance of correct distance placement of more distanced nodes. A high value indicates that nodes with a high distance to one individual node will have less impact of the placement of this node, while nodes with a low distance will have a bigger impact.",
            0,2
    );

    @Deprecated
    public BooleanParameter randomInitial() {
        return new BooleanParameter(
                false,
                "Random initial layout",
                "Use a random layout as initial layout rather than the present layout."
        );
    }*/


}
