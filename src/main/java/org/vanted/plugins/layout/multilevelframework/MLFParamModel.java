package org.vanted.plugins.layout.multilevelframework;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.PatternSpringembedder;
import info.clearthought.layout.SingleFiledLayout;
import org.JMButton;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.dialog.ParameterEditPanel;
import org.graffiti.managers.EditComponentManager;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.selection.Selection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;

public class MLFParamModel {
	public LayoutAlgGroup layoutAlgGroup = new LayoutAlgGroup();
	public MergerGroup mergerGroup = new MergerGroup();
	public PlacerGroup placerGroup = new PlacerGroup();
	
	public class LayoutAlgGroup extends JComponent {
		
		/**
		* 
		*/
		private static final long serialVersionUID = -6846113290305737922L;
		private final String defaultAlg = (new PatternSpringembedder()).getName();
		private final Map<String, LayoutAlgorithmWrapper> layoutAlgorithms = LayoutAlgorithmWrapper.getLayoutAlgorithms();
		private final JComboBox<String> algorithmList = new JComboBox<>(
				this.layoutAlgorithms.keySet().stream().sorted().toArray(String[]::new));
		JPanel algOptionsContainer = new JPanel();
		JLabel optionsLabel;
		JCheckBox randomTop;
		
		LayoutAlgGroup() {
			this.setBackground(null);
			this.setLayout(new SingleFiledLayout(SingleFiledLayout.COLUMN, SingleFiledLayout.LEFT, 1));
			
			// set "force directed" to be the default alg.
			// care has to be taken that this is done before algOptionsContainer is initialised
			algorithmList.setSelectedItem(defaultAlg);
			algorithmList.addActionListener((e) -> updateAlgUI());
			this.updateAlgUI();
			
			algOptionsContainer.setBorder(new EmptyBorder(2, 2, 2, 2));
			algOptionsContainer.setBackground(null);
			JComponent optionsGUI = this.getSelected().getGUI();
			// optionsGUI.setBackground(null);
			algOptionsContainer.add(optionsGUI);
			
			JLabel listLabel = new JLabel("Layout algorithm applied to each level:");
			listLabel.setBackground(null);
			listLabel.setBorder(new EmptyBorder(5, 0, 3, 0));
			
			optionsLabel = new JLabel("Configure the selected algorithm:");
			optionsLabel.setBackground(null);
			optionsLabel.setBorder(new EmptyBorder(5, 0, 3, 0));
			
			randomTop = new JCheckBox("Random layout for topmost layer");
			randomTop.setBackground(null);
			randomTop.setBorder(new EmptyBorder(5, 0, 7, 0));
			
			this.add(randomTop);
			this.add(listLabel);
			this.add(algorithmList);
			this.add(optionsLabel);
			this.add(algOptionsContainer);
		}
		
		private void updateAlgUI() {
			algOptionsContainer.removeAll();
			JComponent algGui = this.getSelected().getGUI();
			if (algGui == null) {
				optionsLabel.setEnabled(false);
				algOptionsContainer.setEnabled(false);
				algOptionsContainer.add(new JLabel("No options available"));
			} else {
				algOptionsContainer.add(this.getSelected().getGUI());
				removeControlButtons(algOptionsContainer);
			}
		}
		
		/**
		 * Removes the "Layout Network" button, the "auto redraw" / "auto refresh" checkboxes and the "stop"
		 * button from the given JPanel. the fact that the "layout network", "auto redraw" etc buttons are
		 * part of the panel created by `ThreadSafeAlgorithm.setControlInterface` is a consequence of the fact
		 * that in case of ThreadSafeAlgorithms there is no standard button supplied by the core, so anything
		 * done to circumvent this without changes to core will necessarily be hacky.
		 */
		private void removeControlButtons(JPanel algOptionsContainer) {
			Container algOptions = ((Container) (((Container) ((Container) algOptionsContainer.getComponent(0)).getComponent(0)).getComponent(0)));
			for (Component comp : algOptions.getComponents()) {
				Container cont = (Container) comp;
				if (cont instanceof JMButton) {
					if (((JMButton) cont).getText().equalsIgnoreCase("layout network")) {
						algOptions.remove(cont);
					}
				}
				if (cont instanceof JPanel && cont.getComponents().length > 0) {
					Component firstChild = cont.getComponents()[0];
					if (firstChild instanceof JButton) {
						if (((JButton) firstChild).getText().equalsIgnoreCase("refresh view")) {
							algOptions.remove(cont);
						}
						if (((JButton) firstChild).getText().equalsIgnoreCase("auto redraw")) {
							algOptions.remove(cont);
						}
						if (((JButton) firstChild).getText().equalsIgnoreCase("stop")) {
							algOptions.remove(cont);
						}
					}
				}
			}
			algOptions.validate();
		}
		
		public LayoutAlgorithmWrapper getSelected() {
			return this.layoutAlgorithms.get(Objects.toString(this.algorithmList.getSelectedItem()));
		}
		
		public boolean isRandomTop() {
			return this.randomTop.isSelected();
		}
		
	}
	
	public class MergerGroup extends JComponent {
		
		/**
		* 
		*/
		private static final long serialVersionUID = 6239086739503695709L;
		private final Merger defaultMerger = new RandomMerger();
		private final Map<String, Merger> mergers = new HashMap<>();
		
		JComboBox<String> mergerSelection;
		ParameterEditPanel paramPanel;
		
		MergerGroup() {
			
			for (Merger merger : MultilevelFrameworkLayout.getMergers()) {
				mergers.put(merger.getName(), merger);
			}
			
			this.setBackground(null);
			this.setLayout(new SingleFiledLayout(SingleFiledLayout.COLUMN, SingleFiledLayout.LEFT, 1));
			
			JPanel mergerOptions = new JPanel();
			
			mergerSelection = new JComboBox<>(
					this.mergers.keySet().stream().sorted().toArray(String[]::new));
			mergerSelection.addActionListener((e) -> {
				updateMergerOptions(mergerOptions);
			});
			mergerSelection.setSelectedItem(mergers.get(defaultMerger.getName()));
			this.updateMergerOptions(mergerOptions);
			
			JLabel selectionLabel = new JLabel("Merging procedure to construct next level:");
			JLabel optionsLabel = new JLabel("Configure the selected merger:");
			selectionLabel.setBorder(new EmptyBorder(5, 0, 3, 0));
			optionsLabel.setBorder(new EmptyBorder(5, 0, 3, 0));
			
			this.add(selectionLabel);
			this.add(mergerSelection);
			this.add(optionsLabel);
			this.add(mergerOptions);
		}
		
		private void updateMergerOptions(JPanel optionsPanel) {
			optionsPanel.removeAll();
			Merger selectedMerger = mergers.get(mergerSelection.getSelectedItem());
			Parameter[] mergerParams = selectedMerger.getParameters();
			EditComponentManager ecm = MainFrame.getInstance().getEditComponentManager();
			paramPanel = new ParameterEditPanel(
					mergerParams,
					ecm.getEditComponents(),
					new Selection(),
					"",
					true,
					"");
			paramPanel.setBackground(null);
			optionsPanel.add(paramPanel);
		}
		
		public Merger getSelected() {
			return mergers.get(mergerSelection.getSelectedItem());
		}
		
		public Parameter[] getUpdatedParameters() {
			return paramPanel.getUpdatedParameters();
		}
		
	}
	
	public class PlacerGroup extends JComponent {
		/**
		* 
		*/
		private static final long serialVersionUID = 2830370243627722134L;
		
		private final Placer defaultPlacer = new RandomPlacer();
		
		private final Map<String, Placer> placers = new HashMap<>();
		
		JComboBox<String> placerSelection;
		JLabel optionsLabel = new JLabel("Configure the selected placer:");
		ParameterEditPanel paramPanel;
		
		PlacerGroup() {
			
			for (Placer placer : MultilevelFrameworkLayout.getPlacers()) {
				placers.put(placer.getName(), placer);
			}
			
			this.setBackground(null);
			this.setLayout(new SingleFiledLayout(SingleFiledLayout.COLUMN, SingleFiledLayout.LEFT, 1));
			
			JPanel placerOptions = new JPanel();
			
			placerSelection = new JComboBox<>(
					this.placers.keySet().stream().sorted().toArray(String[]::new));
			placerSelection.addActionListener((e) -> {
				updatePlacerOptions(placerOptions);
			});
			placerSelection.setSelectedItem(placers.get(defaultPlacer.getName()));
			this.updatePlacerOptions(placerOptions);
			
			JLabel selectionLabel = new JLabel("Placement procedure to create initial layout from previous " +
					"level:");
			selectionLabel.setBorder(new EmptyBorder(5, 0, 3, 0));
			optionsLabel.setBorder(new EmptyBorder(5, 0, 3, 0));
			
			this.add(selectionLabel);
			this.add(placerSelection);
			this.add(optionsLabel);
			this.add(placerOptions);
		}
		
		private void updatePlacerOptions(JPanel optionsPanel) {
			optionsPanel.removeAll();
			Placer selectedPlacer = placers.get(placerSelection.getSelectedItem());
			Parameter[] placerParams = selectedPlacer.getParameters();
			if (placerParams.length > 0) {
				EditComponentManager ecm = MainFrame.getInstance().getEditComponentManager();
				paramPanel = new ParameterEditPanel(
						placerParams,
						ecm.getEditComponents(),
						new Selection(),
						"",
						true,
						"");
				paramPanel.setBackground(null);
				optionsPanel.add(paramPanel);
				optionsLabel.setEnabled(true);
				optionsPanel.setBackground(UIManager.getColor("Panel.background"));
			} else {
				optionsLabel.setEnabled(false);
				optionsPanel.add(new JLabel("No options available"));
				optionsPanel.setBackground(null);
			}
		}
		
		public Placer getSelected() {
			return placers.get(placerSelection.getSelectedItem());
		}
		
		public Parameter[] getUpdatedParameters() {
			return paramPanel.getUpdatedParameters();
		}
	}
	
}
