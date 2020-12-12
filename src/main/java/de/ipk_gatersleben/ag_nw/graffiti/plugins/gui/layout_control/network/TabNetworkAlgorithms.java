/**
 * 
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.network;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.inspector.InspectorTab;
import org.graffiti.plugin.view.View;
import org.graffiti.plugins.views.defaults.GraffitiView;

import de.ipk_gatersleben.ag_nw.graffiti.services.algorithms.AlgorithmPanelFactory;
import de.ipk_gatersleben.ag_nw.graffiti.services.algorithms.SearchAlgorithms;
import de.ipk_gatersleben.ag_nw.graffiti.services.algorithms.SearchAlgorithms.LogicalOp;
import de.ipk_gatersleben.ag_nw.graffiti.services.algorithms.SearchAlgorithms.OperatorOnCategories;

/**
 * @author matthiak
 *
 */
public class TabNetworkAlgorithms extends InspectorTab {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1375768845115569087L;

	static Logger logger = Logger.getLogger(TabNetworkAlgorithms.class);

	private static final String NAME = "Algorithms";

	public TabNetworkAlgorithms() {
		initComponents();
	}

	private void initComponents() {
		initDialog();
	}

	private void initDialog() {
		double border = 2;
		double[][] size = { { border, TableLayoutConstants.FILL, border }, // Columns
				{ border, TableLayoutConstants.FILL, border } }; // Rows
		this.setLayout(new TableLayout(size));

		JButton searchAlgo = new JButton("search algos");
		searchAlgo.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				OperatorOnCategories[] opsOnCats = new OperatorOnCategories[] {
						new OperatorOnCategories(LogicalOp.OR, new Category[] { Category.ANALYSIS, Category.DATA }),

				};

				logger.debug("===== Found Algorithms =====");
				List<Algorithm> searchAlgorithms = SearchAlgorithms.searchAlgorithms(opsOnCats);
				for (Algorithm algo : searchAlgorithms) {
					logger.debug("Name : " + algo.getName());
					for (Category cat : algo.getSetCategory())
						logger.debug("   category: " + cat);
				}

			}
		});
		// add(searchAlgo, "1,1");

		JPanel algorithmspanel = AlgorithmPanelFactory.createForAlgorithms(true,
				SearchAlgorithms.searchAlgorithms(new Category[] { Category.ANALYSIS, Category.GRAPH }));

		add(algorithmspanel, "1,1");
	}

	@Override
	public String getTitle() {
		return NAME;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public boolean visibleForView(View v) {
		return v != null && v instanceof GraffitiView;
	}

	@Override
	public String getTabParentPath() {
		return "Analysis.Network";
	}

	@Override
	public int getPreferredTabPosition() {
		return InspectorTab.TAB_TRAILING;
	}

}
