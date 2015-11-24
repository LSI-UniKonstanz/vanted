/**
 * 
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.biomodels;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import uk.ac.ebi.biomodels.ws.BioModelsWSException;
import uk.ac.ebi.biomodels.ws.SimpleModel;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.biomodels.BiomodelsAccessAdapter.BiomodelsLoaderCallback;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.biomodels.BiomodelsAccessAdapter.QueryType;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

/**
 * @author matthiak
 *
 */
public class BiomodelsPanel extends JPanel
implements ActionListener, BiomodelsLoaderCallback, KeyListener{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5772252664151135872L;

	Logger logger = Logger.getLogger(BiomodelsPanel.class);
	
	JScrollPane scrollpane;
	
	JTextField queryField;
	
	JButton searchButton;
	
//	JText infoField;
	
	JComboBox<BiomodelsAccessAdapter.QueryType> comboQueryType;
	
	JList<SimpleModel> listResults;
	
//	JProgressBar progressBar;
	
	JButton loadSelectedModels;
	
	JPanel rootpanel;
	
	BiomodelsAccessAdapter adapter;
	
	CallerThreadForSimpleModel callerThreadForSimpleModel;
	
	CallerThreadForSBMLModel callerThreadForSBML;
	/**
	 * 
	 */
	public BiomodelsPanel() {
		adapter = new BiomodelsAccessAdapter();
		adapter.addListener(this);
		initGUI();
//		setPreferredSize(new Dimension(300, 300));
	}

	
	
	public BiomodelsAccessAdapter getAdapter() {
		return adapter;
	}



	public void setAdapter(BiomodelsAccessAdapter adapter) {
		this.adapter = adapter;
	}



	/**
	 * 
	 */
	private void initGUI() {
		TableLayoutConstraints constraint;
		
		setLayout(new BorderLayout());
		
		rootpanel = new JPanel();
		
		scrollpane = new JScrollPane(rootpanel);
		
		rootpanel.setLayout(new TableLayout(new double[][]{
				//columns
				{
					5, TableLayout.PREFERRED, 5, TableLayout.FILL, 5, TableLayout.PREFERRED
				},
				//rows
				{
					5, TableLayout.PREFERRED, 
					5, TableLayout.PREFERRED,
					5, TableLayout.FILL,
					5, TableLayout.PREFERRED
				}
		}));
		
		BiomodelsAccessAdapter.QueryType[] items = BiomodelsAccessAdapter.QueryType.values();
		comboQueryType = new JComboBox<BiomodelsAccessAdapter.QueryType>(items);
		
		rootpanel.add(comboQueryType, "1,1");
		
		queryField = new JTextField(20);
		queryField.addKeyListener(this);
		rootpanel.add(queryField, "3,1");
		
		searchButton = new JButton();
		searchButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("org/images/lupe.png")));
		searchButton.addActionListener(this);
		rootpanel.add(searchButton, "5,1");
		
//		constraint = new TableLayoutConstraints(1, 3, 3, 3, TableLayoutConstraints.CENTER, TableLayoutConstraints.CENTER);
//		progressBar = new JProgressBar();
//		progressBar.setPreferredSize(new Dimension(30, 10));
//		rootpanel.add(progressBar, constraint);
		
		listResults = new JList<SimpleModel>();
		listResults.addMouseListener(new ListMouseAdapapter());
		listResults.setCellRenderer(new ListBiomodelsCellRenderer());
		
		JScrollPane resultscrollpane = new JScrollPane(listResults);
		constraint = new TableLayoutConstraints(1, 5, 5, 5, TableLayoutConstraints.CENTER, TableLayoutConstraints.CENTER);
		rootpanel.add(resultscrollpane, constraint);
		
		loadSelectedModels = new JButton("Load Models");
		loadSelectedModels.addActionListener(this);
		rootpanel.add(loadSelectedModels, "1,7");
		
		add(rootpanel, BorderLayout.CENTER);
		
		
		listResults.setEnabled(false);
		loadSelectedModels.setEnabled(false);
	}

	private void triggerQuery(String query){
		BiomodelsAccessAdapter.QueryType selItem = 
				(BiomodelsAccessAdapter.QueryType)comboQueryType.getSelectedItem();
		
		if(query == null || query.isEmpty())
			return;

//		progressBar.setIndeterminate(true);
		
		if(callerThreadForSimpleModel != null && callerThreadForSimpleModel.isAlive())
			callerThreadForSimpleModel.cancelRequest();
			

		BackgroundTaskHelper.issueSimpleTask(
				"Biomodels query task", 
				"query result list", 
				new CallerThreadForSimpleModel(selItem, query), 
				null);
//		callerThreadForSimpleModel = new CallerThreadForSimpleModel(selItem, query);
//		callerThreadForSimpleModel.start();
	}

	private void triggerLoadSBML(SimpleModel model){
		if(model == null)
			return;

//		progressBar.setIndeterminate(true);
		
//		if(callerThreadForSBML != null && callerThreadForSBML.isAlive())
//			callerThreadForSBML.cancelRequest();
//			
//		callerThreadForSBML = new CallerThreadForSBMLModel(model);
//		callerThreadForSBML.start();
		BackgroundTaskHelper.issueSimpleTask(
				"Biomodels query task", 
				"query result list", 
				new CallerThreadForSBMLModel(model),
				null);

	}
	
	@Override
	public void resultForSimpleModelQuery(QueryType type,
			List<SimpleModel> simpleModel) {
		
//		progressBar.setIndeterminate(false);
		
		if(simpleModel == null){
			logger.debug("no results");
			simpleModel = new ArrayList<SimpleModel>();
		}
		if(SwingUtilities.isEventDispatchThread())
			listResults.setListData(simpleModel.toArray(new SimpleModel[simpleModel.size()]));
		else {
			final List<SimpleModel> simpleModelSwingThread = simpleModel;
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					listResults.setListData(simpleModelSwingThread.toArray(new SimpleModel[simpleModelSwingThread.size()]));
				}
			});
		}
		listResults.setEnabled(true);
		loadSelectedModels.setEnabled(true);
	}

	@Override
	public void resultForSBML(SimpleModel model, String modelstring) {
		logger.debug("having result for SBML");
//		progressBar.setIndeterminate(false);
		
		listResults.setEnabled(true);
		loadSelectedModels.setEnabled(true);
	}

	@Override
	public void resultError(Exception e) {
//		progressBar.setIndeterminate(false);
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(loadSelectedModels)){
			
			List<SimpleModel> selectedValuesList = listResults.getSelectedValuesList();
			if(selectedValuesList == null || selectedValuesList.isEmpty())
				return;
			
			listResults.setEnabled(false);
			loadSelectedModels.setEnabled(false);
			for(SimpleModel model : selectedValuesList)
				triggerLoadSBML(model);
			
		}
		if(e.getSource().equals(searchButton)) {
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
//		logger.debug("keyPressed");
	}

	@Override
	public void keyReleased(KeyEvent e) {
//		logger.debug("keyReleased");
		if(e.getKeyCode() == KeyEvent.VK_ENTER){
			listResults.setEnabled(false);
			loadSelectedModels.setEnabled(false);
			triggerQuery(queryField.getText().trim());
		}
	}

	class ListMouseAdapapter extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent e) {
			if(e.getClickCount() == 2) {
				listResults.setEnabled(false);
				loadSelectedModels.setEnabled(false);
				triggerLoadSBML(listResults.getSelectedValue());
			}
				
		}
		
	}

	class CallerThreadForSimpleModel extends Thread{
		BiomodelsAccessAdapter.QueryType selItem;
		String query;
		/**
		 * 
		 */
		public CallerThreadForSimpleModel(
				BiomodelsAccessAdapter.QueryType selItem,
				String query) {
			this.selItem = selItem;
			this.query = query;
		}
		@Override
		public void run() {
			try {
				logger.debug("calling adapter for query");
				adapter.queryForSimpleModel(selItem, query);
			} catch (BioModelsWSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
		public void cancelRequest() {
			adapter.setAbort(true);
		}
	}
	class CallerThreadForSBMLModel extends Thread{
		SimpleModel model;
		/**
		 * 
		 */
		public CallerThreadForSBMLModel(
				SimpleModel model) {
			this.model = model;
		}
		@Override
		public void run() {
			try {
				logger.debug("calling adapter for sbml model");
				adapter.getSBMLModel(model);
			} catch (BioModelsWSException e) {
				e.printStackTrace();
			}
		}
		
		public void cancelRequest() {
			adapter.setAbort(true);
		}
	}
}
