package org.vanted.addons.stressminimization.parameters;


import java.awt.Color;
import java.awt.FlowLayout;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.AbstractValueEditComponent;
import org.graffiti.session.Session;
import org.graffiti.session.SessionListener;

/**
 * Slider component specially for selecting landmarks. Exponential scale with the opportunity to select all nodes.
 */
public class LandmarkSliderComponent extends AbstractValueEditComponent implements SessionListener, ChangeListener {

	private JSlider slider;
	private JLabel valueLabel;
	private JPanel container;
	private final int minValue;
	private final int maxValue;
	private int value;
	private int numberOfNodes;

	public LandmarkSliderComponent(Displayable disp){
		super(disp);

		MainFrame.getInstance().addSessionListener(this);

		LandmarkParameter landmarkParameter = (LandmarkParameter) disp;

		this.minValue = landmarkParameter.getMinUsefullValue();
		this.maxValue = landmarkParameter.getMaxUsefullValue();
		this.value = (Integer) landmarkParameter.getValue();
		this.numberOfNodes = MainFrame.getInstance().getActiveEditorSession().getGraph().getNumberOfNodes();

		this.valueLabel = new JLabel("" + this.value);
		this.valueLabel.setToolTipText("Selected number of landmarks");

		this.slider = new JSlider(JSlider.HORIZONTAL, minValue, maxValue, value);
		this.slider.addChangeListener(this);

		this.slider.setPaintTicks(true);
		this.slider.setPaintLabels(true);

		this.container = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
		this.container.setBackground(Color.WHITE);
		this.container.add(valueLabel);
		this.container.add(slider);

		updateSlider();

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void updateSlider() {

		int minDecimalDigits = (int) Math.log10(minValue);
		int maxDecimalDigits = (int) Math.log10(maxValue);

		slider.setMinimum(minValue);
		slider.setValue(value);
		slider.setMajorTickSpacing((int) Math.pow(10, maxDecimalDigits - 1));

		Dictionary labelsDict = new Hashtable();
		for (int i = minDecimalDigits; i <= maxDecimalDigits; i += 1) {
			int transformedValue = (int) Math.pow(10, i);
			labelsDict.put(transformedValue, new JLabel(transformedValue + ""));
		}

		if (maxValue >= numberOfNodes) {
			slider.setMaximum(numberOfNodes);
			labelsDict.put(numberOfNodes, new JLabel("all"));
		} else {
			slider.setMaximum(maxValue + 1);
			labelsDict.put(maxValue + 1, new JLabel("all"));
		}


		slider.setLabelTable(labelsDict);

	}

	@Override
	public JComponent getComponent() {
		return container;
	}

	@Override
	public void setEditFieldValue() {

	}

	@Override
	public void setValue() {
		displayable.setValue(value);
	}

	@Override
	public void sessionChanged(Session s) {
		sessionDataChanged(s);
	}

	@Override
	public void sessionDataChanged(Session s) {
		if (s != null && s.getGraph() != null) {
			this.numberOfNodes = s.getGraph().getNumberOfNodes();
			updateSlider();
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {

		int val = slider.getValue();

		if (maxValue < numberOfNodes && val == maxValue + 1) {
			this.value = numberOfNodes;
		} else {
			this.value = val;
		}

		valueLabel.setText("" + this.value);

	}

}

