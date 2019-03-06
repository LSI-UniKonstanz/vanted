/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;

import net.iharder.dnd.FileDrop;

import org.ErrorMsg;
import org.FeatureSet;
import org.FolderPanel;
import org.JLabelJavaHelpLink;
import org.JMButton;
import org.ReleaseInfo;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.plugin.inspector.InspectorTab;
import org.graffiti.plugin.view.GraphView;
import org.graffiti.plugin.view.View;
import org.w3c.dom.Document;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.ExperimentDataDragAndDropHandler;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.GravistoMainHelper;

public class TabDBE extends InspectorTab implements ExperimentDataPresenter {

	private static final long serialVersionUID = 1L;

	private List<String> projectList = new ArrayList<String>();

	private List<ExperimentDataInfoPane> shownExpPanes = new ArrayList<ExperimentDataInfoPane>();

	private JTabbedPane jTabbedPaneExperimentPanels;

	final String noNode = "no node is selected.";

	final static String NO_EXPERIMENT = "";

	private boolean initPerformed = false;

	private JScrollPane scrollpane;

	private static TabDBE instance = null;

	/**
	 * Initialize GUI
	 */
	private void initComponents() {
		if (initPerformed) {
			ErrorMsg.addErrorMessage("Internal Error: Only one experiment-data-tab may be used!");
			return;
		}
		initPerformed = true;

		instance = this;

		JPanel contentpane = new JPanel();
		scrollpane = new JScrollPane(contentpane);
		scrollpane.setBorder(new EmptyBorder(0, 0, 0, 0));
		scrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollpane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollpane.getVerticalScrollBar().setUnitIncrement(20);

		this.setBorder(new EmptyBorder(0, 0, 0, 0));
		this.setLayout(new BorderLayout());
		this.add(scrollpane, BorderLayout.CENTER);

		jTabbedPaneExperimentPanels = new javax.swing.JTabbedPane();
		jTabbedPaneExperimentPanels.setOpaque(false);
		jTabbedPaneExperimentPanels.setBackground(null);

		double border = 5;
		double[][] size = { { border, TableLayoutConstants.FILL, border }, // Columns
				{ border, TableLayout.PREFERRED, // buttonPanelDBE
						5, TableLayout.PREFERRED, // buttonPanelFile
						3, TableLayoutConstants.FILL, // experimentInfoPane
						border } }; // Rows

		size[1][1] = TableLayout.PREFERRED;

		contentpane.setLayout(new TableLayout(size));
		contentpane.add(jTabbedPaneExperimentPanels, "1,5");
		ClassLoader cl = this.getClass().getClassLoader();
		String path = this.getClass().getPackage().getName().replace('.', '/');

		URL templatefile1 = cl.getResource(path + "/templates/" + "template1.xls");
		TemplateFileManager.getInstance().addTemplate("Experiment Data", templatefile1, null);

		URL templatefile2 = cl.getResource(path + "/templates/" + "template1t.xls");
		TemplateFileManager.getInstance().addTemplate("Experiment Data (transposed)", templatefile2, null);

		// JButton loadProjectData = new JMButton("<html>Load Selected Experiment");

		ActionListener o1 = JLabelJavaHelpLink.getHelpActionListener("inputformats");

		if (!ReleaseInfo.getIsAllowedFeature(FeatureSet.DBE_ACCESS)) {
			o1 = null;
		}

		FolderPanel buttonPanelFile = new FolderPanel("Load Input File", false, true, false, o1);
		buttonPanelFile.setFrameColor(new JTabbedPane().getBackground(), Color.BLACK, 0, 5);
		buttonPanelFile.setBackground(null);

		JButton loadInputForm = new JMButton("Load Dataset");

		loadInputForm.setOpaque(false);
		loadInputForm.addActionListener(getLoadInputFormListener());

		installDragNDrop(loadInputForm);

		JButton loadMAGE = new JMButton("MAGE-ML");

		loadMAGE.setOpaque(false);
		loadMAGE.addActionListener(getLoadMAGElistener());

		buttonPanelFile.addGuiComponentRow(null, loadInputForm, false);
		buttonPanelFile
				.addGuiComponentRow(null,
						new JLabel("<html><font color=\"gray\"><small>Supported formats: "
								+ "templates 1 and 2, <br/> VANTED binary (xml), KEGG Expression, text/csv files"),
						false);

		/*
		 * buttonPanelFile.addGuiComponentRow(null,
		 * TableLayout.getSplitVertical(loadInputForm, loadMAGE, TableLayout.PREFERRED,
		 * TableLayout.PREFERRED), false);
		 */
		// buttonPanelFile.addGuiComponentRow(new JLabel(""), loadGeneExprButton,
		// false);
		buttonPanelFile.layoutRows();

		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.DBE_ACCESS)
				|| ReleaseInfo.getIsAllowedFeature(FeatureSet.FLAREX_ACCESS)
				|| ReleaseInfo.getIsAllowedFeature(FeatureSet.METHOUSE_ACCESS)) {
			contentpane.add(new JLabel(""), "1,1");
		} else
			contentpane.add(TemplateFileManager.getInstance().getTemplateFolderPanel(), "1,1");
		contentpane.add(buttonPanelFile, "1,3");

		// this.validate();
	}

	private ActionListener getLoadMAGElistener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					loadMAGEfile();
				} catch (Exception err) {
					ErrorMsg.addErrorMessage(err);
					MainFrame.showMessageDialog("Error: Could not open MAGE-ML: " + err.getLocalizedMessage(),
							"File could not be processed");
				}
			}
		};
	}

	private ActionListener getLoadInputFormListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					loadExcelOrBinaryFiles();
				} catch (Exception err) {
					ErrorMsg.addErrorMessage(err);
					// MainFrame.showMessageDialog("Error: Could not convert Excel File to XML:
					// "+err.getLocalizedMessage(),
					// "Excel File could not be processed");
				}
			}
		};
	}

	protected void loadMAGEfile() {
		File mageFile = OpenMageFileDialogService.getXMLfile();
		if (mageFile != null) {
			Document doc = MAGEprocessor.getDataset(mageFile);
			Experiment md = Experiment.getExperimentFromDOM(doc);
			processReceivedData(null, mageFile.getName(), md, null);
		}
	}

	@SuppressWarnings("unchecked")
	protected void loadExcelOrBinaryFiles() {
		final Collection<File> fileList = OpenExcelFileDialogService.getExcelOrBinaryFiles();
		if (fileList != null) {
			GravistoMainHelper.processDroppedFiles(fileList.toArray(new File[] {}), false,
					(Class) PutIntoSidePanel.class);
			// ExperimentLoader.loadFile(fileList, this);
		}
	}

	/**
	 * @param projects
	 */
	void loadProjectList(JComboBox<String> projects) {

		projects.removeAllItems();
		projectList.clear();

		projects.addItem(NO_EXPERIMENT);
	}

	public static List<String> getProjectList() {
		return new ArrayList<String>(instance.projectList);
	}

	/**
	 * Constructs a <code>PatternTab</code> and sets the title.
	 */
	public TabDBE() {
		super();
		this.title = "Experiments";
		initComponents();
	}

	public void installDragNDrop(final JButton target) {

		// install global experiment data loader
		GravistoMainHelper.addDragAndDropHandler(new ExperimentDataDragAndDropHandler() {
			private ExperimentDataPresenter receiver;

			@Override
			public String toString() {
				return "VANTED Dataset";
			}

			public boolean process(List<File> files) {
				// GravistoMainHelper.processDroppedFiles(files.toArray(new File[]{}), false,
				// (Class)PutIntoSidePanel.class);
				ExperimentLoader.loadFile(files, receiver != null ? receiver : instance);
				return true;
			}

			public boolean canProcess(File f) {
				return ExperimentLoader.canLoadFile(f);
			}

			public void setExperimentDataReceiver(ExperimentDataPresenter receiver) {
				this.receiver = receiver;
			}

			public boolean hasPriority() {
				return false;
			}
		});

		// add handler for load dataset button
		final String title = target.getText();
		FileDrop.Listener fdl = new FileDrop.Listener() {
			@SuppressWarnings("unchecked")
			public void filesDropped(File[] files) {
				if (files != null && files.length > 0) {
					ArrayList<File> mfiles = new ArrayList<File>();
					for (File f : files)
						mfiles.add(f);
					GravistoMainHelper.processDroppedFiles(files, false, (Class) PutIntoSidePanel.class);
					// ExperimentLoader.loadFile(mfiles, tabDbeInstance);
				}
			}
		};

		Runnable dragdetected = new Runnable() {
			public void run() {
				MainFrame.showMessage(
						"<html><b>Drag &amp; Drop action detected:</b> release mouse button to load experiment data",
						MessageType.PERMANENT_INFO);
				target.setText("<html><br>Drop file to load dataset<br><br>");
				// jTabbedPaneSubstrats.setBorder(BorderFactory.createLineBorder(Color.BLUE,
				// 2));
			}
		};

		Runnable dragenddetected = new Runnable() {
			public void run() {
				// MainFrame.showMessage("Drag & Drop action canceled", MessageType.INFO);
				target.setText(title);
				// jTabbedPaneSubstrats.setBorder(BorderFactory.createLineBorder(null, 0));
			}
		};
		target.setToolTipText("Drag & Drop supported");
		new FileDrop(target, fdl, dragdetected, dragenddetected);
	}

	// /**
	// * @return The Filename of the DBE-Last-Experiment-File
	// */
	// public static String getDataCacheTempFileName() {
	// String tmpFilePath = System.getProperty("java.io.tmpdir");
	// String fn = tmpFilePath + File.separator
	// + "dbe_lastexperiment.xml";
	// return fn;
	// }

	// /**
	// * @param dbe2xmlService
	// * @return
	// * @throws IOException
	// */
	// public static Document getLastCachedExperiment() throws IOException {
	// Document doc;
	// String lastExp = TextFile.read(getDataCacheTempFileName());
	// doc = XMLHelper.getDocumentFromXMLstring(lastExp);
	// return doc;
	// }

	public static String getExperimentNameFromGUIselection(String tempName) {
		if (tempName == null || tempName.equals(NO_EXPERIMENT))
			return null;
		String htmlTag = "<html>";
		int ll = htmlTag.length();
		final String experimentName = tempName.substring(ll);
		return experimentName;
	}

	public synchronized static List<ProjectEntity> getLoadedProjectEntities() {
		List<ProjectEntity> loadedProjects = new ArrayList<ProjectEntity>();
		for (ExperimentDataInfoPane edip : instance.shownExpPanes) {
			loadedProjects.add(new ProjectEntity(edip.getExperimentName(), edip.getDocumentData()));
		}
		return loadedProjects;
	}

	public synchronized static void addOrUpdateExperimentPane(ProjectEntity pe) {
		for (ExperimentDataInfoPane expPane : instance.shownExpPanes) {
			if (
			// expPane.getDocument()==pe.getDocument() ||
			expPane.getExperimentName().equals(pe.getExperimentName())) {
				expPane.updateGUIforUpdatedExperimentData(pe.getExperimentName(), pe.getDocumentData(), pe.getGUI());
				return;
			}
		}
		instance.processReceivedData(null, pe.getExperimentName(), pe.getDocumentData(), pe.getGUI());
	}

	@Override
	public boolean visibleForView(View v) {
		return v == null || v instanceof GraphView;
	}

	public synchronized void processReceivedData(TableData td, String experimentName, ExperimentInterface doc,
			JComponent gui) {
		ExperimentDataInfoPane expPane = new ExperimentDataInfoPane(td, doc, jTabbedPaneExperimentPanels, shownExpPanes,
				gui);
		expPane.setOpaque(false);
		shownExpPanes.add(expPane);

		Component c = jTabbedPaneExperimentPanels.add("<html>&nbsp;&nbsp;" + experimentName, expPane);
		MainFrame.getInstance().getInspectorPlugin().setSelectedTab(this);
		try {
			jTabbedPaneExperimentPanels.setSelectedComponent(c);
		} catch (Exception e) {
			// empty
		}
		MainFrame.showMessage("Load project data: Finished", MessageType.INFO, 3000);
		MainFrame.getInstance().updateActions();
	}

	@Override
	public ImageIcon getIcon() {
		URL url = getClass().getResource("Database-Table-icon.png");
		if (url != null)
			return new ImageIcon(GravistoService.getScaledImage(new ImageIcon(url).getImage(), 16, 16));
		else
			return super.getIcon();
	}

}
