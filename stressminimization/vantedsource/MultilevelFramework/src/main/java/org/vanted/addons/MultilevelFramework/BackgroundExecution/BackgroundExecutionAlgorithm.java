package org.vanted.addons.MultilevelFramework.BackgroundExecution;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.ErrorMsg;
import org.JMButton;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.dialog.ParameterEditPanel;
import org.graffiti.graph.Graph;
import org.graffiti.managers.EditComponentManager;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.algorithm.ThreadSafeAlgorithm;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.selection.Selection;
import org.graffiti.session.Session;
import org.graffiti.session.SessionListener;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskPanelEntry;
import info.clearthought.layout.SingleFiledLayout;
import info.clearthought.layout.TableLayout;

/**
 * Executes a BackgroundAlgorithm and manage the GUI interaction.
 * 
 */
public class BackgroundExecutionAlgorithm extends ThreadSafeAlgorithm
		implements PropertyChangeListener, SessionListener {

	private BackgroundAlgorithm algorithm;
	private Graph graph;
	private Selection selection;
	private BackgroundStatus status;
	private JButton startButton;
	private JButton stopButton;
	private ParameterEditPanel paramPanel;
	private BackgroundProgress progress;
	private BackgroundTaskPanelEntry progressBar;

	public BackgroundExecutionAlgorithm(BackgroundAlgorithm algorithm) {
		super();
		this.algorithm = algorithm;
		status = BackgroundStatus.FINISHED;
		progress = new BackgroundProgress(algorithm);

		MainFrame.getInstance().addSessionListener(this);

		// add BackgroundExecutionAlgorithm to listener list of algorithm
		algorithm.addPropertyChangeListener(this);
	}

	/**
	 * set status of running algorithm
	 * 
	 * @param statusValue
	 */
	private void setStatus(BackgroundStatus statusValue) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				switch (statusValue) {
				case INIT:
					status = BackgroundStatus.INIT;
					if (startButton != null) {
						startButton.setText("Pause");
						stopButton.setEnabled(true);
						progressBar.setVisible(true);
						progressBar.setPreferredSize(new Dimension(100, 100));
					}
					break;
				case RUNNING:
					status = BackgroundStatus.RUNNING;
					if (startButton != null) {
						startButton.setText("Pause");
						stopButton.setEnabled(true);
						progressBar.setVisible(true);
						progressBar.setPreferredSize(new Dimension(100, 100));
					}
					break;
				case IDLE:
					status = BackgroundStatus.IDLE;
					if (startButton != null) {
						startButton.setText("Continue");
						stopButton.setEnabled(true);
						progressBar.setVisible(true);
						progressBar.setPreferredSize(new Dimension(100, 100));
					}
					break;
				case FINISHED:
					status = BackgroundStatus.FINISHED;
					if (startButton != null) {
						startButton.setText("Layout Network");
						startButton.setEnabled(true);
						stopButton.setEnabled(false);
						progressBar.setVisible(false);
						progressBar.setPreferredSize(new Dimension(0, 0));
					}
					break;
				default:
					status = BackgroundStatus.STATUSERROR;
					break;
				}

				System.out.println("Status background task: " + status);
			}
		});
	}

	/**
	 * Shows level/graph in new window (mostly useful for testing and debugging)
	 * 
	 * @param g Graph to show
	 */
	private void showLevel(Graph g) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				GraphHelper.diplayGraph(g);
			}
		});

	}

	@Override
	public String getName() {
		return algorithm.getName();
	}

	@Override
	public void setParameters(Parameter[] params) {
		algorithm.setParameters(params);
	}

	@Override
	public Parameter[] getParameters() {
		return algorithm.getParameters();
	}

	@Override
	public void check() throws PreconditionException {
		algorithm.check();
	}

	@Override
	public void execute() {
		algorithm.execute();
	}

	@Override
	public void reset() {
		algorithm.reset();
	}

	@SuppressWarnings("deprecation")
	@Override
	public String getCategory() {
		return algorithm.getCategory();
	}

	@Override
	public Set<Category> getSetCategory() {
		return algorithm.getSetCategory();
	}

	@Override
	public String getMenuCategory() {
		return algorithm.getMenuCategory();
	}

	@Override
	public boolean isLayoutAlgorithm() {
		return algorithm.isLayoutAlgorithm();
	}

	@Override
	public boolean showMenuIcon() {
		return algorithm.showMenuIcon();
	}

	@Override
	public KeyStroke getAcceleratorKeyStroke() {
		return algorithm.getAcceleratorKeyStroke();
	}

	@Override
	public String getDescription() {
		return algorithm.getDescription();
	}

	@Override
	public void setActionEvent(ActionEvent a) {
		algorithm.setActionEvent(a);
	}

	@Override
	public ActionEvent getActionEvent() {
		return algorithm.getActionEvent();
	}

	@Override
	public boolean mayWorkOnMultipleGraphs() {
		return algorithm.mayWorkOnMultipleGraphs();
	}

	@Override
	public boolean setControlInterface(ThreadSafeOptions options, JComponent jc) {
		// set component layout (component is the panel above the layout list
		// in the layout tab for the chosen BackgroundAlgorithm)
		SingleFiledLayout sfl = new SingleFiledLayout(SingleFiledLayout.COLUMN, SingleFiledLayout.FULL, 1);
		jc.setLayout(sfl);

		// create input component for each algorithm parameter and save all in
		// ParameterEditPanel pep
		EditComponentManager ecm = MainFrame.getInstance().getEditComponentManager();
		ParameterEditPanel pep = null;
		pep = new ParameterEditPanel(algorithm.getParameters(), ecm.getEditComponents(), selection, algorithm.getName(),
				true, algorithm.getName());
		paramPanel = pep;

		// panel with all algorithm parameter components
		JPanel parameterPanel = new JPanel();
		parameterPanel.setLayout(sfl);
		parameterPanel.add(Box.createVerticalStrut(10));
		parameterPanel.add(pep);

		// initialization of start and pause button
		startButton = new JMButton("Layout Network");
		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				switch (status) {
				case FINISHED:
					startButton.setText("Layout Network");
					executeBackgroundAlgorithm();
					break;
				case INIT:
				case RUNNING:
					algorithm.pause();
					break;
				case IDLE:
					algorithm.resume();
					break;
				default:
					setStatus(BackgroundStatus.STATUSERROR);
					break;
				}

			}
		});
		jc.add(startButton);

		// initialization of stop button
		stopButton = new JButton("Stop");
		stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				algorithm.stop();
				stopButton.setEnabled(false);
				startButton.setEnabled(false);
			}
		});
		stopButton.setEnabled(false);

		// add stop button to component
		jc.add(TableLayout.get3Split(stopButton, new JLabel(), new JLabel(), TableLayout.PREFERRED,
				TableLayout.PREFERRED, TableLayout.FILL));

		// initialization of progress bar with description
		progressBar = new BackgroundTaskPanelEntry(false);
		progressBar.setStatusProvider(progress, getName(), getName() + " Progress");
		progressBar.setVisible(false);
		progressBar.setPreferredSize(new Dimension(0, 5));
		jc.add(progressBar);

		// add parameter panel to component
		if (paramPanel != null) {
			jc.add(parameterPanel);
		}

		return true;
	}

	/**
	 * Starts new thread which executes the BackgroundAlgorithm
	 */
	private void executeBackgroundAlgorithm() {
		Runnable algoExecution = new Runnable() {
			public void run() {
				// current graph position and selection
				graph = GravistoService.getInstance().getMainFrame().getActiveSession().getGraph();
				selection = GravistoService.getInstance().getMainFrame().getActiveEditorSession().getSelectionModel()
						.getActiveSelection();
				attach(graph, selection);
				try {
					check();
				} catch (PreconditionException e1) {
					e1.printStackTrace();
				}
				setParameters(paramPanel.getUpdatedParameters());
				try {
					execute();
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e); // exception with more detailed message
					ErrorMsg.addErrorMessage(e.toString()); // general exception message
					reset();
					setStatus(BackgroundStatus.FINISHED);
				}
			}
		};
		reset();
		setStatus(BackgroundStatus.INIT);

		// starts execution of the background algorithm
		Thread backgroundTask = new Thread(algoExecution, getName());
		backgroundTask.start();
	}

	@Override
	public void attach(Graph g, Selection selection) {
		algorithm.attach(g, selection);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		// updating status and show new level of a graph if the background algorithm
		// request
		switch (arg0.getPropertyName()) {

		case "showLevel":
			showLevel((Graph) arg0.getNewValue());
			break;
		case "setStatus":
			setStatus((BackgroundStatus) arg0.getNewValue());
			break;
		default:
			break;
		}
	}

	@Override
	public void sessionChanged(Session s) {
		sessionDataChanged(s);
	}

	@Override
	public void sessionDataChanged(Session s) {
		if (s == null || s.getGraph() == null) {
			return;
		}

		if (status != BackgroundStatus.FINISHED) {
			algorithm.stop();
		}
	}

	// both never used but required for thread safe algorithm
	// ThreadSafeAlgorithm is adopted since it alone provides
	// GUI customization

	@Override
	public void executeThreadSafe(ThreadSafeOptions options) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void resetDataCache(ThreadSafeOptions options) {
		throw new UnsupportedOperationException();
	}

}