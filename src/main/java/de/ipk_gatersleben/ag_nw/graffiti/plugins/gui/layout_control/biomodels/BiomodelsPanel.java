/**
 * 
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.biomodels;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.biomodels.BiomodelsAccessAdapter.BiomodelsLoaderCallback;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;
import org.apache.log4j.Logger;
import org.json.JSONException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This is the Biomodels tab panel.
 * 
 * @author matthiak
 * @vanted.revision 2.8.3
 */
public class BiomodelsPanel extends JPanel implements ActionListener, BiomodelsLoaderCallback, KeyListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5772252664151135872L;
	
	final Logger logger = Logger.getLogger(BiomodelsPanel.class);
	
	JScrollPane scrollpane;
	
	JTextField queryField;
	
	JButton searchButton;
	
	JComboBox<BiomodelsAccessAdapter.QueryType> comboQueryType;
	
	JList<SimpleModel> listResults;
	
	JButton loadSelectedModels;
	
	JPanel rootpanel;
	
	BiomodelsAccessAdapter adapter;
	
	CallerThreadForSimpleModel callerThreadForSimpleModel;

	
	private JLabel labelServiceAvailable;
	private boolean isServiceAvailable;
	private String connectivityString;
	
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
	 * Checks connection and updates availability status.
	 */
	private void checkConnection() {
		new Thread(() -> {

			try {
				isServiceAvailable = adapter.isAvailable();
			} catch (Exception e) {
				e.printStackTrace();
			}

			SwingUtilities.invokeLater(() -> {

				if (isServiceAvailable) {
					connectivityString = "OK";
					labelServiceAvailable.setForeground(Color.GREEN.darker());
					labelServiceAvailable.setText(connectivityString);
				} else {
					connectivityString = "<html>Offline?<br/><br/>"
							+ "No, then change the webservice endpoint from Edit &rarr; Preferences.";
					labelServiceAvailable.setForeground(Color.RED.darker());
					labelServiceAvailable.setText(connectivityString);

				}

			});
		}).start();
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
	 * Set the {@linkplain BiomodelsAccessAdapter}.
	 * 
	 * @param adapter
	 *           the new {@linkplain BiomodelsAccessAdapter}
	 */
	public void setAdapter(BiomodelsAccessAdapter adapter) {
		this.adapter = adapter;
	}
	
	/**
	 * Sets up the GUI.
	 */
	private void initGUI() {
		TableLayoutConstraints constraint;
		
		setLayout(new BorderLayout());
		
		rootpanel = new JPanel();
		
		scrollpane = new JScrollPane(rootpanel);
		
		rootpanel.setLayout(new TableLayout(new double[][] {
				// columns
				{ 5, TableLayout.PREFERRED, 5, TableLayout.FILL, 5, TableLayout.PREFERRED },
				// rows
				{ 5, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5, TableLayout.FILL,4} }));
		
		rootpanel.add(new JLabel("Availability: "), "1,1");
		labelServiceAvailable = new JLabel("availability");
		
		rootpanel.add(labelServiceAvailable, "3,1");
		
		BiomodelsAccessAdapter.QueryType[] items = BiomodelsAccessAdapter.QueryType.values();
		comboQueryType = new JComboBox<>(items);
		
		rootpanel.add(comboQueryType, "1,3");
		
		queryField = new JTextField(20);
		queryField.addKeyListener(this);
		rootpanel.add(queryField, "3,3");
		
		searchButton = new JButton();
		searchButton.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("org/images/lupe.png"))));
		searchButton.addActionListener(this);
		rootpanel.add(searchButton, "5,3");
		
		listResults = new JList<>();
		listResults.addMouseListener(new ListMouseAdapapter());
		listResults.setCellRenderer(new ListBiomodelsCellRenderer());
		
		JScrollPane resultscrollpane = new JScrollPane(listResults);
		constraint = new TableLayoutConstraints(1, 5, 5, 5, TableLayoutConstraints.CENTER,
				TableLayoutConstraints.CENTER);
		rootpanel.add(resultscrollpane, constraint);
		
		loadSelectedModels = new JButton("Load Models");
		loadSelectedModels.addActionListener(this);
		rootpanel.add(loadSelectedModels, "6,3");
		
		add(rootpanel, BorderLayout.CENTER);
		
		listResults.setEnabled(false);
		loadSelectedModels.setEnabled(false);
	}
	
	private void triggerQuery(String query) {
		BiomodelsAccessAdapter.QueryType selItem = (BiomodelsAccessAdapter.QueryType) comboQueryType.getSelectedItem();
		
		if (query == null || query.isEmpty())
			return;
		
		if (callerThreadForSimpleModel != null && callerThreadForSimpleModel.isAlive())
			callerThreadForSimpleModel.cancelRequest();
		
		BackgroundTaskHelper.issueSimpleTask("BioModels Query", "Processing results...",
				new CallerThreadForSimpleModel(selItem, query), null);
	}
	
	private void triggerLoadSBML(SimpleModel model) {
		if (model == null)
			return;
		
		BackgroundTaskHelper.issueSimpleTask("BioModels Query", "Processing results...",
				new CallerThreadForSBMLModel(model), null);
	}
	
	@Override
	public void resultForSimpleModelQuery(List<SimpleModel> simpleModel) {
		
		if (simpleModel == null) {
			logger.debug("no results");
			simpleModel = new ArrayList<>();
		}
		if (SwingUtilities.isEventDispatchThread())
			listResults.setListData(simpleModel.toArray(new SimpleModel[0]));
		else {
			final List<SimpleModel> simpleModelSwingThread = simpleModel;
			SwingUtilities.invokeLater(() -> listResults.setListData(
					simpleModelSwingThread.toArray(new SimpleModel[0])));
		}
		listResults.setEnabled(true);
		loadSelectedModels.setEnabled(true);
	}
	
	@Override
	public void resultForSBML() {
		logger.debug("having result for SBML");
		
		listResults.setEnabled(true);
		loadSelectedModels.setEnabled(true);
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(loadSelectedModels)) {
			
			List<SimpleModel> selectedValuesList = listResults.getSelectedValuesList();
			if (selectedValuesList == null || selectedValuesList.isEmpty())
				return;
			
			listResults.setEnabled(false);
			loadSelectedModels.setEnabled(false);
			for (SimpleModel model : selectedValuesList)
				triggerLoadSBML(model);
		}
		if (e.getSource().equals(searchButton)) {
			listResults.setEnabled(false);
			loadSelectedModels.setEnabled(false);
			triggerQuery(queryField.getText().trim());
		}
		
	}
	
	/*
	 * for input field
	 */
	@Override
	public void keyTyped(KeyEvent e) {
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		// logger.debug("keyPressed");
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		// logger.debug("keyReleased");
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			listResults.setEnabled(false);
			loadSelectedModels.setEnabled(false);
			triggerQuery(queryField.getText().trim());
		}
	}
	
	class ListMouseAdapapter extends MouseAdapter {
		
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				listResults.setEnabled(false);
				loadSelectedModels.setEnabled(false);
				triggerLoadSBML(listResults.getSelectedValue());
			}
			
		}
		
	}
	
	class CallerThreadForSimpleModel extends Thread {
		final BiomodelsAccessAdapter.QueryType selItem;
		final String query;
		
		/**
		 * 
		 */
		public CallerThreadForSimpleModel(BiomodelsAccessAdapter.QueryType selItem, String query) {
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
		
		/**
		 * 
		 */
		public CallerThreadForSBMLModel(SimpleModel model) {
			this.model = model;
		}
		
		@Override
		public void run() {
			logger.debug("calling adapter for sbml model");

			try{
				TabBiomodels.resultForSBML(model,RestApiBiomodels.getModelSBMLById(model.getId()));
				adapter.notifySBML();
			} catch (JSONException ignored){
			}
		}
		public void cancelRequest() {
			adapter.setAbort(true);
		}
	}
}
