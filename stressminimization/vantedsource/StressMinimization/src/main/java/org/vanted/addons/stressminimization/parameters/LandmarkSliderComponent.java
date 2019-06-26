package org.vanted.addons.stressminimization.parameters;


import java.util.Dictionary;
import java.util.Hashtable;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.AbstractValueEditComponent;
import org.graffiti.session.Session;
import org.graffiti.session.SessionListener;

public class LandmarkSliderComponent extends AbstractValueEditComponent implements SessionListener, ChangeListener {

	private JSlider slider;
	private int value;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public LandmarkSliderComponent(Displayable disp){
		super(disp);

		MainFrame.getInstance().addSessionListener(this);

		value = (Integer) disp.getValue();


		//Create the slider
		slider = new JSlider(JSlider.HORIZONTAL, 5, 100, value);
		slider.addChangeListener(this);

		slider.setMinimum(5);
		slider.setValue(value);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);

		int numberOfNodes = MainFrame.getInstance().getActiveEditorSession().getGraph().getNumberOfNodes();
		updateSlider(numberOfNodes);

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void updateSlider(int numberOfNodes) {

		slider.setMaximum(numberOfNodes);

		slider.setMajorTickSpacing(numberOfNodes / 5);

		Dictionary labelsDict = new Hashtable();
		for (int i = 0; i < numberOfNodes; i += numberOfNodes / 5) {
			labelsDict.put(i, new JLabel(i + ""));
		}

		int min = slider.getMinimum();
		int max = slider.getMaximum();
		labelsDict.put(min, new JLabel(min + ""));
		labelsDict.put(max, new JLabel(max + ""));

		slider.setLabelTable(labelsDict);

	}

	@Override
	public JComponent getComponent() {
		return slider;
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
			int numberOfNodes = s.getGraph().getNumberOfNodes();
			updateSlider(numberOfNodes);
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		value = slider.getValue();
	}

}

