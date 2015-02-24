package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.r;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;







import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.FolderPanel;
import org.apache.log4j.Logger;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.plugin.inspector.InspectorTab;
import org.graffiti.plugin.view.View;
import org.graffiti.plugins.views.defaults.GraffitiView;
import org.graffiti.session.Session;
import org.graffiti.session.SessionListenerExt;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;


public class TabRControl  extends InspectorTab implements ActionListener, SessionListenerExt{

	private static final long serialVersionUID = 1L;
	
	static Logger logger = Logger.getLogger(TabRControl.class);
	
	RScriptHandler scriptHandler;
	
	JButton loadGraphData;
	boolean graphDataLoaded;
	Graph graph;

	public TabRControl() {
		graphDataLoaded = false;
		graph = null;
		MainFrame.getInstance().addSessionListener(this);
		scriptHandler = new RScriptHandler(this);
		refreshPanel(scriptHandler.isScriptLoaded());
	}
	
	public void refreshPanel(boolean scriptLoaded)
	{
		removeAll();
		setBackground(null);
		setOpaque(false);
		double border = 5;
		
		double[][] size = { { border, TableLayoutConstants.FILL, border }, // Columns
				{ border,
												TableLayoutConstants.PREFERRED,
												2 * border,
												TableLayoutConstants.PREFERRED,
												border } }; // Rows
		JPanel cp = new JPanel(new TableLayout(size));
		setLayout(new BorderLayout());
				
		ArrayList<FolderPanel> panelList = new ArrayList<FolderPanel>();
		FolderPanel editScriptPanel = new FolderPanel("Edit Scripts", true, true, false, null);
		editScriptPanel.setColumnStyle(TableLayoutConstants.PREFERRED, TableLayoutConstants.FILL);
		editScriptPanel.layoutRows();
		editScriptPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, (int) border, 0));

		JButton newScript = new JButton("new R-Script");
		newScript.setActionCommand("newButton");
		newScript.addActionListener(this);

		JButton openScript = new JButton("open R-Script");
		openScript.setActionCommand("openButton");
		openScript.addActionListener(this);

		editScriptPanel.addGuiComponentRow(null, newScript, false, 5);
		editScriptPanel.addGuiComponentRow(null, openScript, false, 5);
		panelList.add(editScriptPanel);
		

		
		FolderPanel loadScriptPanel =
			new FolderPanel("Load Input File", !scriptLoaded, true, false, null);
		loadScriptPanel.setColumnStyle(TableLayoutConstants.PREFERRED, TableLayoutConstants.FILL);
		loadScriptPanel.layoutRows();
		loadScriptPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, (int) border, 0));

		JButton loadScript = new JButton("load R-script");
		loadScript.setActionCommand("loadButton");
		loadScript.addActionListener(this);
		
		loadGraphData = new JButton();
		setGraphDataLoadButton();
		loadGraphData.setActionCommand("loadGraphDataButton");
		loadGraphData.addActionListener(this);
		
		loadScriptPanel.addGuiComponentRow(null, loadScript, false, 5);
		loadScriptPanel.addGuiComponentRow(null, loadGraphData, false, 5);
		
		if(scriptLoaded)
		{
			loadScriptPanel.addGuiComponentRow(null,scriptHandler.getInputVariablePanel(), false, 5);
//            TODO: Output-Variablen
//			loadScriptPanel.addGuiComponentRow(null,scriptHandler.getOutputVariablePanel(), true, 5);
			JButton runScript = new JButton("run R-Script");
			runScript.setActionCommand("runButton");
			runScript.addActionListener(this);
			loadScriptPanel.addGuiComponentRow(null, runScript, false, 5);

			loadScriptPanel.setCondensedState(false);
			loadScriptPanel.revalidate();
			loadScriptPanel.repaint();
		}
		
		panelList.add(loadScriptPanel);
		cp.add(TableLayout.getMultiSplitVertical(panelList), "1,3");
		
		JScrollPane sp = new JScrollPane(cp);
		sp.getVerticalScrollBar().setUnitIncrement(15);
		
		add(sp, BorderLayout.CENTER);

		
		revalidate();
		
	}
	
	protected boolean checkRAvailability () {
		try {
			RConnection c = new RConnection();
			return true;
		} catch (RserveException e) {
			logger.error(e.getLocalizedMessage());
			return false;
		}
	}
	
	protected void setGraph(Graph g)
	{
		graph = g;
	}
	
	protected void setGraphDataLoadButton()
	{
		if(scriptHandler.isGraphDataLoaded())
			loadGraphData.setText("<html>load graph data &#10003;");
		else
			loadGraphData.setText("load graph data");
		loadGraphData.validate();
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("loadButton"))
		{
			loadScript();
		}
		else if(e.getActionCommand().equals("newButton"))
		{
			newScript();
		}
		else if(e.getActionCommand().equals("openButton"))
		{
			openScript();
		}
		else if(e.getActionCommand().equals("loadGraphDataButton"))
		{
			if(MainFrame.getInstance().getActiveEditorSession() != null && MainFrame.getInstance().getActiveEditorSession().getGraph() != null)
			{
				setGraph(MainFrame.getInstance().getActiveEditorSession().getGraph());
				loadGraphData(graph);
			}
			else
				JOptionPane.showMessageDialog(MainFrame.getInstance(), "Please open a graph first!", "Error", JOptionPane.ERROR_MESSAGE);
		}
		else if(e.getActionCommand().equals("runButton"))
		{
			runScript();
		}
	}
	
	private void runScript()
	{
		scriptHandler.runScript();
	}
	
	private void loadGraphData(Graph g)
	{
		scriptHandler.loadGraphData(g);
		setGraphDataLoadButton();
	}

	private void openScript() {
		JFileChooser fc = new JFileChooser();
 
        fc.setFileFilter(new RFileFilter());
        fc.setDialogTitle("R script filechooser");

        int ret = fc.showOpenDialog(this);

        if (ret != JFileChooser.APPROVE_OPTION) {
        	return;
        }

        File f = fc.getSelectedFile();
        if (f.isFile() && f.canRead()) {
        	new ScriptEditor(f);
        } else {
            JOptionPane.showMessageDialog(MainFrame.getInstance(),
                    "Could not open file: " + f,
                    "Error opening file",
                    JOptionPane.ERROR_MESSAGE);
        }
	}

	private void newScript() {
		new ScriptEditor(this);
	}

	private void loadScript() {
		JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new RFileFilter());
        int ret = fc.showOpenDialog(this);

        if (ret != JFileChooser.APPROVE_OPTION) {
        	return;
        }

        File f = fc.getSelectedFile();
        loadScript(f);
	}
	
	public void loadScript(File f)
	{
        if (f.isFile() && f.canRead()) {
        	scriptHandler.loadScript(f);
        	refreshPanel(scriptHandler.isScriptLoaded());
        } else {
            JOptionPane.showMessageDialog(MainFrame.getInstance(),
                    "Could not open file: " + f,
                    "Error opening file",
                    JOptionPane.ERROR_MESSAGE);
        }		
	}

	@Override
	public String getName() {
		return getTitle();
	}
	
	/**
	 * Don't forget to set the title, otherwise the tab won't be shown.
	 */
	@Override
	public String getTitle() {
		return "R script";
	}
	
	
	
	@Override
	public String getTabParentPath() {
		return "Analysis.Data";
	}

	@Override
	public int getPreferredTabPosition() {
		return InspectorTab.TAB_TRAILING;
	}

	public void sessionChanged(Session s) {}
	public void sessionDataChanged(Session s) {}
	public void sessionClosed(Session s) {
		if(s.getGraph() == graph)
		{
			scriptHandler.graphClosed();
			setGraphDataLoadButton();
			graph = null;
			refreshPanel(scriptHandler.isScriptLoaded());
		}
	}

	/**
	 * Vanted will ask every time all tabs, when a view changes, if this tab
	 * wants to be hidden or not.
	 */
	@Override
	public boolean visibleForView(View v) {
		return v != null && v instanceof GraffitiView;
	}
}
