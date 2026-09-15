/**
 *
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.biomodels;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.biomodels.BiomodelsAccessAdapter.BiomodelsLoaderCallback;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import org.apache.log4j.Logger;
import org.json.JSONException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Biomodels tab panel. Provides a search interface against the BioModels database with one or more
 * combinable filters and loads selected models as SBML networks.
 *
 * @author matthiak
 * @vanted.revision 2.8.8 @author niklas-groene
 *
 */
public class BiomodelsPanel extends JPanel implements BiomodelsLoaderCallback {

	private static final long serialVersionUID = -5772252664151135872L;

	private static final int MAX_FILTERS = 5;

	final Logger logger = Logger.getLogger(BiomodelsPanel.class);

	private final List<FilterRow> filterRows = new ArrayList<>();
	private JPanel filtersPanel;
	private JButton addFilterButton;
	private JButton searchButton;
	private JButton loadSelectedModels;
	private JLabel resultsCountLabel;
	private JLabel labelServiceAvailable;

	private JList<SimpleModel> listResults;

	private final BiomodelsAccessAdapter adapter;

	private CallerThreadForSimpleModel callerThreadForSimpleModel;

	/**
	 * Populates the Biomodels tab panel.
	 */
	public BiomodelsPanel() {
		adapter = new BiomodelsAccessAdapter();
		adapter.addListener(this);
		initGUI();
		checkConnection();
	}

	/**
	 * Get the {@linkplain BiomodelsAccessAdapter}.
	 *
	 * @return the active {@linkplain BiomodelsAccessAdapter}
	 */
	public BiomodelsAccessAdapter getAdapter() {
		return adapter;
	}

	/**
	 * Sets up the GUI.
	 */
	private void initGUI() {
		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(8, 8, 8, 8));

		add(buildHeader(), BorderLayout.NORTH);
		add(buildCenter(), BorderLayout.CENTER);
		add(buildFooter(), BorderLayout.SOUTH);

		setSearchEnabled(false);
	}

	private JComponent buildHeader() {
		JPanel header = new JPanel(new BorderLayout());
		header.setBorder(new EmptyBorder(0, 0, 8, 0));

		JLabel title = new JLabel("BioModels Database");
		title.setFont(title.getFont().deriveFont(Font.BOLD, title.getFont().getSize() + 3f));
		header.add(title, BorderLayout.WEST);

		labelServiceAvailable = new JLabel("checking…");
		labelServiceAvailable.setForeground(Color.GRAY);
		header.add(labelServiceAvailable, BorderLayout.EAST);

		return header;
	}

	private JComponent buildCenter() {
		JPanel center = new JPanel(new BorderLayout(0, 6));

		// --- filter section ---
		JPanel searchSection = new JPanel(new BorderLayout(0, 6));

		filtersPanel = new JPanel();
		filtersPanel.setLayout(new BoxLayout(filtersPanel, BoxLayout.Y_AXIS));
		addFilterRow();
		searchSection.add(filtersPanel, BorderLayout.NORTH);

		JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
		addFilterButton = new JButton("Add filter");
		setIcon(addFilterButton, "org/images/icons8-plus-15.png");
		addFilterButton.setToolTipText("Add another filter combined with AND");
		addFilterButton.addActionListener(e -> addFilterRow());
		actions.add(addFilterButton);

		searchButton = new JButton("Search");
		searchButton.addActionListener(e -> performSearch());
		actions.add(searchButton);
		searchSection.add(actions, BorderLayout.CENTER);

		center.add(searchSection, BorderLayout.NORTH);

		// --- results section ---
		JPanel resultsSection = new JPanel(new BorderLayout(0, 4));
		resultsCountLabel = new JLabel(" ");
		resultsCountLabel.setForeground(Color.GRAY);
		resultsSection.add(resultsCountLabel, BorderLayout.NORTH);

		listResults = new JList<>();
		listResults.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		listResults.setCellRenderer(new ListBiomodelsCellRenderer());
		listResults.addMouseListener(new ListMouseAdapter());

		JScrollPane resultScrollPane = new JScrollPane(listResults);
		resultScrollPane.setPreferredSize(new Dimension(280, 300));
		resultsSection.add(resultScrollPane, BorderLayout.CENTER);

		center.add(resultsSection, BorderLayout.CENTER);

		return center;
	}

	private JComponent buildFooter() {
		JPanel footer = new JPanel(new BorderLayout());
		footer.setBorder(new EmptyBorder(8, 0, 0, 0));

		loadSelectedModels = new JButton("Load selected model(s)");
		loadSelectedModels.setToolTipText("Load the selected models as networks (double-click a model to load it directly)");
		loadSelectedModels.addActionListener(e -> loadSelected());
		loadSelectedModels.setEnabled(false);
		footer.add(loadSelectedModels, BorderLayout.CENTER);

		return footer;
	}

	/**
	 * Adds a new filter row (up to {@link #MAX_FILTERS}).
	 */
	private void addFilterRow() {
		if (filterRows.size() >= MAX_FILTERS)
			return;

		FilterRow row = new FilterRow();
		filterRows.add(row);
		filtersPanel.add(row);
		refreshFilterRows();
	}

	private void removeFilterRow(FilterRow row) {
		if (filterRows.size() <= 1)
			return;
		filterRows.remove(row);
		filtersPanel.remove(row);
		refreshFilterRows();
	}

	/**
	 * Keeps remove-buttons and the add-button consistent with the current number of rows.
	 */
	private void refreshFilterRows() {
		boolean canRemove = filterRows.size() > 1;
		for (FilterRow row : filterRows)
			row.removeButton.setEnabled(canRemove);
		if (addFilterButton != null)
			addFilterButton.setEnabled(filterRows.size() < MAX_FILTERS);
		filtersPanel.revalidate();
		filtersPanel.repaint();
	}

	/**
	 * Collects the filters and triggers a background query.
	 */
	private void performSearch() {
		List<BiomodelsAccessAdapter.QueryType> types = new ArrayList<>();
		List<String> queries = new ArrayList<>();
		for (FilterRow row : filterRows) {
			String text = row.queryField.getText().trim();
			if (!text.isEmpty()) {
				types.add((BiomodelsAccessAdapter.QueryType) row.comboQueryType.getSelectedItem());
				queries.add(text);
			}
		}

		if (queries.isEmpty()) {
			resultsCountLabel.setText("Enter at least one search term.");
			return;
		}

		if (callerThreadForSimpleModel != null && callerThreadForSimpleModel.isAlive())
			callerThreadForSimpleModel.cancelRequest();

		searchButton.setText("Searching…");
		searchButton.setEnabled(false);
		listResults.setEnabled(false);
		loadSelectedModels.setEnabled(false);
		resultsCountLabel.setText("Searching…");

		callerThreadForSimpleModel = new CallerThreadForSimpleModel(
				types.toArray(new BiomodelsAccessAdapter.QueryType[0]),
				queries.toArray(new String[0]));
		BackgroundTaskHelper.issueSimpleTask("BioModels Query", "Processing results…",
				callerThreadForSimpleModel, null);
	}

	private void loadSelected() {
		List<SimpleModel> selected = listResults.getSelectedValuesList();
		if (selected == null || selected.isEmpty())
			return;

		listResults.setEnabled(false);
		loadSelectedModels.setEnabled(false);
		for (SimpleModel model : selected)
			triggerLoadSBML(model);
	}

	private void triggerLoadSBML(SimpleModel model) {
		if (model == null)
			return;

		BackgroundTaskHelper.issueSimpleTask("BioModels: loading " + model.getId(),
				"Processing results…", new CallerThreadForSBMLModel(model), null);
	}

	@Override
	public void resultForSimpleModelQuery(List<SimpleModel> simpleModel) {
		final List<SimpleModel> result = (simpleModel == null) ? new ArrayList<>() : simpleModel;
		Runnable update = () -> {
			listResults.setListData(result.toArray(new SimpleModel[0]));
			listResults.setEnabled(true);
			searchButton.setText("Search");
			searchButton.setEnabled(true);
			loadSelectedModels.setEnabled(!result.isEmpty());
			if (result.isEmpty())
				resultsCountLabel.setText("No models found.");
			else
				resultsCountLabel.setText(result.size() + " model"
						+ (result.size() == 1 ? "" : "s") + " found.");
		};
		if (SwingUtilities.isEventDispatchThread())
			update.run();
		else
			SwingUtilities.invokeLater(update);
	}

	@Override
	public void resultForSBML() {
		logger.debug("having result for SBML");
		SwingUtilities.invokeLater(() -> {
			listResults.setEnabled(true);
			loadSelectedModels.setEnabled(true);
		});
	}

	/**
	 * Checks connection and updates the availability indicator.
	 */
	private void checkConnection() {
		new Thread(() -> {
			boolean available = false;
			try {
				available = adapter.isAvailable();
			} catch (Exception e) {
				e.printStackTrace();
			}

			final boolean isAvailable = available;
			SwingUtilities.invokeLater(() -> {
				if (isAvailable) {
					labelServiceAvailable.setText("● Online");
					labelServiceAvailable.setForeground(new Color(0, 153, 0));
					labelServiceAvailable.setToolTipText("Connected to www.biomodels.org");
				} else {
					labelServiceAvailable.setText("● Offline");
					labelServiceAvailable.setForeground(Color.RED.darker());
					labelServiceAvailable.setToolTipText(
							"Could not reach www.biomodels.org. Check your internet connection.");
				}
				setSearchEnabled(isAvailable);
			});
		}, "BioModels-connectivity-check").start();
	}

	private void setSearchEnabled(boolean enabled) {
		if (searchButton != null)
			searchButton.setEnabled(enabled);
		for (FilterRow row : filterRows) {
			row.comboQueryType.setEnabled(enabled);
			row.queryField.setEnabled(enabled);
		}
	}

	private void setIcon(AbstractButton button, String resource) {
		URL url = getClass().getClassLoader().getResource(resource);
		if (url != null)
			button.setIcon(new ImageIcon(url));
	}

	/**
	 * A single search filter: a query-type selector plus an input field and a remove button.
	 */
	private class FilterRow extends JPanel {
		private static final long serialVersionUID = 1L;

		final JComboBox<BiomodelsAccessAdapter.QueryType> comboQueryType;
		final JTextField queryField;
		final JButton removeButton;

		FilterRow() {
			setLayout(new BorderLayout(6, 0));
			setBorder(new EmptyBorder(2, 0, 2, 0));
			setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

			comboQueryType = new JComboBox<>(BiomodelsAccessAdapter.QueryType.values());
			comboQueryType.setPreferredSize(new Dimension(150, 26));
			add(comboQueryType, BorderLayout.WEST);

			queryField = new JTextField();
			queryField.setToolTipText("Press Enter to search");
			queryField.addActionListener(e -> performSearch());
			add(queryField, BorderLayout.CENTER);

			removeButton = new JButton();
			setIcon(removeButton, "org/images/icons8-minus-16.png");
			if (removeButton.getIcon() == null)
				removeButton.setText("−");
			removeButton.setToolTipText("Remove this filter");
			removeButton.addActionListener(e -> removeFilterRow(this));
			add(removeButton, BorderLayout.EAST);
		}
	}

	private class ListMouseAdapter extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				SimpleModel model = listResults.getSelectedValue();
				if (model != null) {
					listResults.setEnabled(false);
					loadSelectedModels.setEnabled(false);
					triggerLoadSBML(model);
				}
			}
		}
	}

	class CallerThreadForSimpleModel extends Thread {
		final BiomodelsAccessAdapter.QueryType[] selItem;
		final String[] query;

		public CallerThreadForSimpleModel(BiomodelsAccessAdapter.QueryType[] selItem, String[] query) {
			this.selItem = selItem;
			this.query = query;
		}

		@Override
		public void run() {
			logger.debug("calling adapter for query");
			adapter.queryForSimpleModel(selItem, query);
		}

		public void cancelRequest() {
			adapter.setAbort(true);
		}
	}

	class CallerThreadForSBMLModel extends Thread {
		final SimpleModel model;

		public CallerThreadForSBMLModel(SimpleModel model) {
			this.model = model;
		}

		@Override
		public void run() {
			logger.debug("calling adapter for sbml model");
			try {
				String sbml = RestApiBiomodels.getModelSBMLById(model.getId());
				if (sbml != null) {
					TabBiomodels.resultForSBML(model, sbml);
					adapter.notifySBML();
				} else {
					SwingUtilities.invokeLater(() -> {
						JOptionPane.showMessageDialog(BiomodelsPanel.this,
								"Could not download model " + model.getId() + ".",
								"BioModels", JOptionPane.WARNING_MESSAGE);
						listResults.setEnabled(true);
						loadSelectedModels.setEnabled(true);
					});
				}
			} catch (JSONException ex) {
				logger.warn("failed to parse model " + model.getId(), ex);
				SwingUtilities.invokeLater(() -> {
					listResults.setEnabled(true);
					loadSelectedModels.setEnabled(true);
				});
			}
		}

		public void cancelRequest() {
			adapter.setAbort(true);
		}
	}
}
