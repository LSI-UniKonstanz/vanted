package org.vanted.plugins.layout.stressminimization;

import info.clearthought.layout.SingleFiledLayout;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.text.ParseException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * This class represents the GUI and at the same time the "model" of the algorithm
 * settings/options/parameters. Note that we entirely omit the ThreadSafeOptions object and use this here
 * object to manage options.
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
				return "Use currently displayed layout";
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
		/**
		* 
		*/
		private static final long serialVersionUID = 3451205769834506374L;
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
		/**
		* 
		*/
		private static final long serialVersionUID = 5762476401467563324L;
		
		PercentageSlider() {
			this.setBackground(null);
			this.setMinimum(0);
			this.setMaximum(100);
			this.setMinorTickSpacing(5);
			this.setMajorTickSpacing(25);
			Dictionary<Integer, JLabel> sliderLabels = new Hashtable<>();
			sliderLabels.put(0, new JLabel("0%"));
			sliderLabels.put(25, new JLabel("25%"));
			sliderLabels.put(50, new JLabel("50%"));
			sliderLabels.put(75, new JLabel("75%"));
			sliderLabels.put(100, new JLabel("100%"));
			this.setLabelTable(sliderLabels);
			this.setPaintLabels(true);
			this.setPaintTicks(true);
		}
	}
	
	public class MethodRadioGroup extends JPanel {
		/**
		* 
		*/
		private static final long serialVersionUID = -17947224799165607L;
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
		/**
		* 
		*/
		private static final long serialVersionUID = 1222253448210707114L;
		
		JSlider slider = new JSlider();
		
		JSpinner spinner;
		
		MethodAlphaGroup() {
			
			this.setBackground(null);
			
			GridBagLayout gridBagLayout = new GridBagLayout();
			gridBagLayout.columnWidths = new int[] { 0, 5, 58, 32 };
			gridBagLayout.rowHeights = new int[] { 20, 0, 0 };
			gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 0.0, 1.0 };
			gridBagLayout.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
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
			
			Component horizontalGlue_1 = Box.createHorizontalGlue();
			GridBagConstraints gbc_horizontalGlue_1 = new GridBagConstraints();
			gbc_horizontalGlue_1.insets = new Insets(0, 0, 0, 5);
			gbc_horizontalGlue_1.gridx = 1;
			gbc_horizontalGlue_1.gridy = 1;
			add(horizontalGlue_1, gbc_horizontalGlue_1);
			
			spinner = new JSpinner(new SpinnerNumberModel(-2, -2, 0, 1));
			spinner.setValue(-2);
			GridBagConstraints gbc_spinner = new GridBagConstraints();
			gbc_spinner.fill = GridBagConstraints.HORIZONTAL;
			gbc_spinner.insets = new Insets(0, 0, 0, 5);
			gbc_spinner.gridx = 2;
			gbc_spinner.gridy = 1;
			add(spinner, gbc_spinner);
			
			Component horizontalGlue = Box.createHorizontalGlue();
			GridBagConstraints gbc_horizontalGlue = new GridBagConstraints();
			gbc_horizontalGlue.fill = GridBagConstraints.HORIZONTAL;
			gbc_horizontalGlue.gridx = 3;
			gbc_horizontalGlue.gridy = 1;
			add(horizontalGlue, gbc_horizontalGlue);
			
			setBackground(null);
			slider.setBackground(null);
			slider.setMinimum(0);
			slider.setMaximum(2);
			slider.setValue(1);
			Dictionary<Integer, JLabel> sliderLabels = new Hashtable<>();
			sliderLabels.put(0, new JLabel("0"));
			sliderLabels.put(1, new JLabel("-1"));
			sliderLabels.put(2, new JLabel("-2"));
			slider.setLabelTable(sliderLabels);
			slider.setPaintLabels(true);
			slider.setPaintTicks(false);
		}
		
		public int getSliderValue() {
			return slider.getValue();
		}
		
		public int getAlpha() {
			try {
				spinner.commitEdit();
			} catch (ParseException e) {
				MainFrame.showMessage("Error parsing alpha value", MessageType.ERROR);
			}
			return (int) spinner.getValue() * (-1);
		}
	}
	
	private class LabeledSlider extends JPanel {
		/**
		* 
		*/
		private static final long serialVersionUID = 259553874108220388L;
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
	}
	
	public class TerminationStressChangeGroup extends JPanel {
		/**
		* 
		*/
		private static final long serialVersionUID = -1723911364466399931L;
		JSlider slider = new JSlider();
		
		TerminationStressChangeGroup() {
			Dictionary<Integer, JLabel> labels = new Hashtable<>();
			// see https://en.wikipedia.org/wiki/Unicode_subscripts_and_superscripts#Superscripts_and_subscripts_block
			labels.put(-9, new JLabel("10\u207b\u2079"));
			labels.put(-7, new JLabel("10\u207b\u2077"));
			labels.put(-5, new JLabel("10\u207b\u2075"));
			labels.put(-3, new JLabel("10\u207b\u00B3"));
			labels.put(-1, new JLabel("10\u207b\u00B9"));
			slider.setMinimum(-9);
			slider.setMaximum(-1);
			slider.setMajorTickSpacing(1);
			slider.setLabelTable(labels);
			slider.setPaintLabels(true);
			slider.setPaintTicks(true);
			slider.setBackground(null);
			slider.setBorder(new EmptyBorder(0, 5, 0, 0));
			
			slider.setValue(-7);
			this.setBackground(null);
			this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			LabeledSlider withLabel = new LabeledSlider("Stress change threshold (always active):", slider);
			withLabel.setAlignmentX(LEFT_ALIGNMENT);
			this.add(withLabel);
		}
		
		public double getValue() {
			// note that values are negative
			return Math.pow(10, slider.getValue());
		}
	}
	
	public class TerminationCheckboxGroup extends JPanel {
		
		/**
		* 
		*/
		private static final long serialVersionUID = 2549980630453924124L;
		JSpinner nodeMovementSpinner = new JSpinner();
		JSpinner iterationsSpinner = new JSpinner();
		JCheckBox movementCheckbox = new JCheckBox("Node movement threshold:");
		JCheckBox iterCheckbox = new JCheckBox("Max. number of iterations:");
		
		TerminationCheckboxGroup() {
			
			GridBagLayout gridBagLayout = new GridBagLayout();
			gridBagLayout.columnWidths = new int[] { 0, 30, 95, 0, 10 };
			gridBagLayout.rowHeights = new int[] { 20, 0, 0, 0 };
			gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 0.0, 1.0 };
			gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
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
			
			nodeMovementSpinner.setEnabled(false);
			nodeMovementSpinner.setModel(new SpinnerNumberModel(1, 1, null, 10));
			GridBagConstraints gbc_nodeMovementSpinner = new GridBagConstraints();
			gbc_nodeMovementSpinner.fill = GridBagConstraints.HORIZONTAL;
			gbc_nodeMovementSpinner.insets = new Insets(0, 0, 5, 5);
			gbc_nodeMovementSpinner.gridx = 2;
			gbc_nodeMovementSpinner.gridy = 1;
			add(nodeMovementSpinner, gbc_nodeMovementSpinner);
			
			GridBagConstraints gbc_chckbxNewCheckBox_1 = new GridBagConstraints();
			gbc_chckbxNewCheckBox_1.anchor = GridBagConstraints.WEST;
			gbc_chckbxNewCheckBox_1.insets = new Insets(0, 0, 0, 5);
			gbc_chckbxNewCheckBox_1.gridx = 0;
			gbc_chckbxNewCheckBox_1.gridy = 2;
			add(iterCheckbox, gbc_chckbxNewCheckBox_1);
			
			iterationsSpinner.setEnabled(false);
			iterationsSpinner.setModel(new SpinnerNumberModel(0, 0, null, 10));
			GridBagConstraints gbc_iterationsSpinner = new GridBagConstraints();
			gbc_iterationsSpinner.fill = GridBagConstraints.HORIZONTAL;
			gbc_iterationsSpinner.insets = new Insets(0, 0, 0, 5);
			gbc_iterationsSpinner.gridx = 2;
			gbc_iterationsSpinner.gridy = 2;
			add(iterationsSpinner, gbc_iterationsSpinner);
			
			iterationsSpinner.setValue(100);
			
			movementCheckbox.addItemListener(e -> nodeMovementSpinner.setEnabled(e.getStateChange() == ItemEvent.SELECTED));
			iterCheckbox.addItemListener(e -> iterationsSpinner.setEnabled(e.getStateChange() == ItemEvent.SELECTED));
			
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
				nodeMovementSpinner.commitEdit();
			} catch (ParseException e) {
				MainFrame.showMessage("Error parsing node movement threshold", MessageType.ERROR);
			}
			return (int) nodeMovementSpinner.getValue();
		}
		
		public int getMaxIterations() {
			try {
				iterationsSpinner.commitEdit();
			} catch (ParseException e) {
				MainFrame.showMessage("Error parsing max iterations", MessageType.ERROR);
			}
			return (int) iterationsSpinner.getValue();
		}
		
	}
}