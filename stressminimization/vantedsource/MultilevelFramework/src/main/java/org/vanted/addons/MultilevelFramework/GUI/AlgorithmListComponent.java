package org.vanted.addons.MultilevelFramework.GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.graffiti.editor.MainFrame;
import org.graffiti.editor.dialog.ParameterEditPanel;
import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.editcomponent.AbstractValueEditComponent;
import org.graffiti.plugin.parameter.Parameter;
import org.vanted.addons.MultilevelFramework.AlgorithmWithParameters;

/**
 * Component includes a drop down menu allowing to select an algorithm. The
 * component also includes a panel where the parameters of the selected
 * algorithm can be specified. The parameter displayed by this is
 * AlgorithmListParameter.
 */
public class AlgorithmListComponent extends AbstractValueEditComponent {
	private JPanel mainPanel;
	private JComponent algorithmParameterComponent;
	private JComboBox<String> comboBox;
	private List<Algorithm> algorithms;
	private List<String> algorithmNames;

	public AlgorithmListComponent(Displayable disp) {
		super(disp);
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		comboBox = new JComboBox<String>();
		algorithms = ((AlgorithmListParameter) displayable).possibleAlgorithms;
		algorithmNames = new LinkedList<String>();
		// find pretty names for all algorithms
		for (Algorithm a : algorithms) {
			String name = a.getName();
			// some algorithms don't have a name specified
			// in that case we use the class name
			if (name == null || name.equals("")) {
				name = a.getClass().getSimpleName();
			}
			algorithmNames.add(name);
			comboBox.addItem(name);
		}
		algorithmParameterComponent = new JPanel();
		mainPanel.add(comboBox);
		mainPanel.add(algorithmParameterComponent);
		rebuildAlgorithmParameterComponent();

		// whenever a different algorithm is selected the parameter panel needs to be
		// rebuild
		ActionListener comboBoxListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				rebuildAlgorithmParameterComponent();
			}
		};
		comboBox.addActionListener(comboBoxListener);
	}

	/**
	 * Builds the algorithmParameterComponent for the currently selected algorithm
	 */
	private void rebuildAlgorithmParameterComponent() {
		mainPanel.remove(algorithmParameterComponent);
		Algorithm selectedAlgorithm = getSelectedAlgorithm();
		algorithmParameterComponent = new ParameterEditPanel(selectedAlgorithm.getParameters(),
				MainFrame.getInstance().getEditComponentManager().getEditComponents(), null, "", true, "");
		mainPanel.add(algorithmParameterComponent);
	}

	@Override
	public JComponent getComponent() {
		return mainPanel;
	}

	@Override
	public void setEditFieldValue() {

	}

	/**
	 * Sets the parameter value to the currently selected algorithm with the
	 * specified parameters. This value if of Type AlgorithmWithParameters.
	 */
	@Override
	public void setValue() {
		Algorithm selectedAlgorithm = getSelectedAlgorithm();
		Parameter[] selectedParameters = ((ParameterEditPanel) algorithmParameterComponent).getUpdatedParameters();
		AlgorithmWithParameters selectedAlgorithmWithParameters = new AlgorithmWithParameters(selectedAlgorithm,
				selectedParameters);
		if (this.displayable.getValue() == null
				|| !this.displayable.getValue().equals(selectedAlgorithmWithParameters)) {
			displayable.setValue(selectedAlgorithmWithParameters);
		}
	}

	/**
	 * @return returns the currently selected algorithm
	 */
	private Algorithm getSelectedAlgorithm() {
		return algorithms.get(comboBox.getSelectedIndex());
	}

}
