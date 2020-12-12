package org.vanted.plugins.layout.stressminimization.parameters;


import java.awt.Color;
import java.awt.Dimension;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.AbstractValueEditComponent;
import org.graffiti.selection.Selection;
import org.graffiti.selection.SelectionModel;
import org.graffiti.session.EditorSession;
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
	private int numberOfNodesSliderValue;

	public LandmarkSliderComponent(Displayable disp){
		super(disp);

		MainFrame.getInstance().addSessionListener(this);

		LandmarkParameter landmarkParameter = (LandmarkParameter) disp;

		this.minValue = landmarkParameter.getMinUsefullValue();
		this.maxValue = landmarkParameter.getMaxUsefullValue();
		this.value = (Integer) landmarkParameter.getValue();

		this.valueLabel = new JLabel("" + this.value);
		this.valueLabel.setToolTipText("Selected number of landmarks");
		this.valueLabel.setMaximumSize(new Dimension(100, 30));
		this.valueLabel.setMinimumSize(new Dimension(100, 30));

		this.slider = new JSlider(JSlider.HORIZONTAL, 0, value, value);
		this.slider.addChangeListener(this);

		this.slider.setPaintTicks(true);
		this.slider.setPaintLabels(true);

		this.container = new JPanel();
		this.container.setLayout(new BoxLayout(this.container, BoxLayout.LINE_AXIS));
		this.container.setBackground(Color.WHITE);
		this.container.add(valueLabel);
		this.container.add(slider);

		updateSlider();

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void updateSlider() {

		updateNumberOfNodes();

		slider.setMinimum(0);
		slider.setValue(value);

		int majorTickSpanning = 100;
		slider.setMajorTickSpacing(majorTickSpanning);

		Dictionary labelsDict = new Hashtable();
		labelsDict.put(minValue, new JLabel("" + minValue));

		if (maxValue >= numberOfNodes) {
			numberOfNodesSliderValue = numberOfNodes;
			slider.setMaximum(numberOfNodes);
			labelsDict.put(numberOfNodes, new JLabel("off"));
		} else {
			numberOfNodesSliderValue = maxValue + majorTickSpanning;
			slider.setMaximum(numberOfNodesSliderValue);
			labelsDict.put(numberOfNodesSliderValue, new JLabel("off"));
		}


		slider.setLabelTable(labelsDict);

	}

	private void updateNumberOfNodes() {

		EditorSession activeSession = MainFrame.getInstance().getActiveEditorSession();

		if (activeSession == null || activeSession.getGraph() == null) {
			return;
		}

		SelectionModel selectionModel = activeSession.getSelectionModel();
		Selection sel = selectionModel == null ? null : selectionModel.getActiveSelection();

		if (sel == null || sel.isEmpty()) {
			numberOfNodes = activeSession.getGraph().getNumberOfNodes();
		} else {
			numberOfNodes = sel.getNumberOfNodes();
		}

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
			updateSlider();
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {

		int val = slider.getValue();

		if (maxValue < numberOfNodes && val > maxValue) {

			// divide the space between maxValue and numberOfNodes in two halves
			// in the first half, the value will be maxValue, in the other numberOfNodes
			int maxValueToNumberOfNodesSliderValueDifference = numberOfNodesSliderValue - maxValue;
			if (val < maxValue + maxValueToNumberOfNodesSliderValueDifference/2) {
				this.value = maxValue;
			} else {
				this.value = numberOfNodes;
			}

		} else if (val < minValue) {
			this.value = minValue;
		} else {
			this.value = val;
		}

		valueLabel.setText("" + this.value);

	}

}

