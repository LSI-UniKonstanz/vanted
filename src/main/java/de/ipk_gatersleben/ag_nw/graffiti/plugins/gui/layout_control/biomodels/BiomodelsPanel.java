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
import org.junit.Ignore;

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

	JTextField queryField2;

	JTextField queryField3;

	JTextField queryField4;

	JTextField queryField5;
	
	JButton extend;

	JButton extend2;

	JButton extend3;

	JButton extend4;

	JButton minus;

	JComboBox<BiomodelsAccessAdapter.QueryType> comboQueryType;

	JComboBox<BiomodelsAccessAdapter.QueryType> comboQueryType2;

	JComboBox<BiomodelsAccessAdapter.QueryType> comboQueryType3;

	JComboBox<BiomodelsAccessAdapter.QueryType> comboQueryType4;

	JComboBox<BiomodelsAccessAdapter.QueryType> comboQueryType5;
	
	JList<SimpleModel> listResults;
	
	JButton loadSelectedModels;
	
	JPanel rootpanel;
	
	BiomodelsAccessAdapter adapter;
	
	CallerThreadForSimpleModel callerThreadForSimpleModel;

	JScrollPane resultscrollpane;

	
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
				{ 5, TableLayout.PREFERRED, 5, TableLayout.PREFERRED,TableLayout.PREFERRED
						,TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, 5, TableLayout.FILL} }));

		rootpanel.add(new JLabel("Availability: "), "1,1");

		labelServiceAvailable = new JLabel("availability");

		rootpanel.add(labelServiceAvailable, "3,1");

		BiomodelsAccessAdapter.QueryType[] items = BiomodelsAccessAdapter.QueryType.values();
		comboQueryType = new JComboBox<>(items);

		rootpanel.add(comboQueryType, "1,3");

		queryField = new JTextField(20);
		queryField.addKeyListener(this);
		rootpanel.add(queryField, "3,3");

		queryField2 = new JTextField(20);
		queryField2.addKeyListener(this);

		queryField3 = new JTextField(20);
		queryField3.addKeyListener(this);

		queryField4 = new JTextField(20);
		queryField4.addKeyListener(this);

		queryField5 = new JTextField(20);
		queryField5.addKeyListener(this);

		extend = new JButton();
		extend.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("org/images/icons8-plus-15.png"))));
		extend.addActionListener(this);
		rootpanel.add(extend, "5,3");

		extend2 = new JButton();
		extend2.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("org/images/icons8-plus-15.png"))));
		extend2.addActionListener(this);

		extend3 = new JButton();
		extend3.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("org/images/icons8-plus-15.png"))));
		extend3.addActionListener(this);

		extend4 = new JButton();
		extend4.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("org/images/icons8-plus-15.png"))));
		extend4.addActionListener(this);

		minus = new JButton();
		minus.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("org/images/icons8-minus-16.png"))));
		minus.addActionListener(this);

		listResults = new JList<>();
		listResults.addMouseListener(new ListMouseAdapapter());
		listResults.setCellRenderer(new ListBiomodelsCellRenderer());

		resultscrollpane = new JScrollPane(listResults);
		constraint = new TableLayoutConstraints(1, 9, 5, 9, TableLayoutConstraints.CENTER,
				TableLayoutConstraints.CENTER);
		rootpanel.add(resultscrollpane, constraint);


		loadSelectedModels = new JButton("Load Models");
		loadSelectedModels.addActionListener(this);
		rootpanel.add(loadSelectedModels, "6,3");

		add(rootpanel, BorderLayout.CENTER);

		listResults.setEnabled(false);
		loadSelectedModels.setEnabled(false);
	}

	private void triggerQuery(String[] query) {
		BiomodelsAccessAdapter.QueryType[] selItem = new BiomodelsAccessAdapter.QueryType[5];
		selItem[0] = (BiomodelsAccessAdapter.QueryType) comboQueryType.getSelectedItem();
		try{
			selItem[1] = (BiomodelsAccessAdapter.QueryType) comboQueryType2.getSelectedItem();
			selItem[2] = (BiomodelsAccessAdapter.QueryType) comboQueryType3.getSelectedItem();
			selItem[3] = (BiomodelsAccessAdapter.QueryType) comboQueryType4.getSelectedItem();
			selItem[4] = (BiomodelsAccessAdapter.QueryType) comboQueryType5.getSelectedItem();

		}catch (Exception ignored) { }

		if (query == null)
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
		if (SwingUtilities.isEventDispatchThread()) {
			listResults.setListData(simpleModel.toArray(new SimpleModel[0]));
		} else {
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
		BiomodelsAccessAdapter.QueryType[] items = BiomodelsAccessAdapter.QueryType.values();

		if (e.getSource().equals(loadSelectedModels)) {

			List<SimpleModel> selectedValuesList = listResults.getSelectedValuesList();
			if (selectedValuesList == null || selectedValuesList.isEmpty())
				return;

			listResults.setEnabled(false);
			loadSelectedModels.setEnabled(false);
			for (SimpleModel model : selectedValuesList)
				triggerLoadSBML(model);
		}


		if (e.getSource().equals(extend)) {
			comboQueryType2 = new JComboBox<>(items);
			rootpanel.add(comboQueryType2, "1,4");

			rootpanel.add(queryField2, "3,4");

			rootpanel.add(extend2, "5,4");

			extend.removeActionListener(this);
			rootpanel.remove(extend);

			rootpanel.repaint();
		}

		if (e.getSource().equals(extend2)){
			comboQueryType3 = new JComboBox<>(items);
			rootpanel.add(comboQueryType3, "1,5");

			rootpanel.add(queryField3, "3,5");

			extend2.removeActionListener(this);
			rootpanel.remove(extend2);

			rootpanel.add(extend3, "5,5");

			rootpanel.repaint();

		}

		if (e.getSource().equals(extend3)){
			comboQueryType4 = new JComboBox<>(items);
			rootpanel.add(comboQueryType4, "1,6");

			rootpanel.add(queryField4, "3,6");

			extend3.removeActionListener(this);
			rootpanel.remove(extend3);

			rootpanel.add(extend4, "5,6");

			rootpanel.repaint();


		}

		if (e.getSource().equals(extend4)){
			comboQueryType5 = new JComboBox<>(items);
			rootpanel.add(comboQueryType5, "1,7");

			rootpanel.add(queryField5, "3,7");

			extend4.removeActionListener(this);
			rootpanel.remove(extend4);

			//TODO: Minus Button implementieren
			//rootpanel.add(minus,"5,7");

			rootpanel.repaint();
		}
		if (e.getSource().equals(minus)){
			rootpanel.remove(comboQueryType5);
			rootpanel.remove(queryField5);
			rootpanel.remove(minus);
			rootpanel.revalidate();
			rootpanel.repaint();
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
			//TODO: important
			String[] querys = new String[5];
			querys[0] = queryField.getText().trim();
			try{
				querys[1] = queryField2.getText().trim();
			} catch (Exception ignored){ }
			try {
				querys[2] = queryField3.getText().trim();
			} catch (Exception ignored) { }
			try {
				querys[3] = queryField4.getText().trim();
			} catch (Exception ignored) { }
			try {
				querys[4] = queryField5.getText().trim();
			} catch (Exception ignored) { }
			triggerQuery(querys);
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
		final BiomodelsAccessAdapter.QueryType[] selItem;
		final String[] query;
		/**
		 * 
		 */
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
