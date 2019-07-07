package org.vanted.addons.stressminimization;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Set;
import java.util.function.Supplier;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.JMButton;
import org.Vector2d;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.editor.dialog.ParameterEditPanel;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
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
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.graph_to_origin_mover.CenterLayouterAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;
import info.clearthought.layout.SingleFiledLayout;
import info.clearthought.layout.TableLayout;

/**
 * Executes a BackgroundAlgorithm and manage the GUI interaction.
 *
 */
public class BackgroundExecutionAlgorithm extends ThreadSafeAlgorithm implements PropertyChangeListener, SessionListener {

	private BackgroundAlgorithm algorithm;
	private Graph graph;
	private Selection selection;
	private BackgroundStatus status;
	private JButton startButton;
	private JButton stopButton;
	private JCheckBox autoDrawCheckBox;
	private ParameterEditPanel paramPanel;
	private JLabel statusLabel;


	public BackgroundExecutionAlgorithm(BackgroundAlgorithm algorithm) {
		super();
		this.algorithm=algorithm;
		status = BackgroundStatus.FINISHED;

		MainFrame.getInstance().addSessionListener(this);

		//add BackgroundExecutionAlgorithm to listener list of algorithm
		algorithm.addPropertyChangeListener(this);
	}

	/**
	 * set status of running algorithm
	 * @param statusValue
	 */
	private void setStatus(BackgroundStatus statusValue) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				switch(statusValue) {
				case INIT:
					status = BackgroundStatus.INIT;
					if(startButton!=null) {
						startButton.setEnabled(true);
						startButton.setText("Pause");
						stopButton.setEnabled(true);
						paramPanel.setVisible(false);
						statusLabel.setVisible(true);
					}
					break;
				case RUNNING:
					status = BackgroundStatus.RUNNING;
					if(startButton!=null) {
						startButton.setEnabled(true);
						startButton.setText("Pause");
						stopButton.setEnabled(true);
						paramPanel.setVisible(false);
						statusLabel.setVisible(true);
					}
					break;
				case IDLE:
					status = BackgroundStatus.IDLE;
					if(startButton!=null) {
						startButton.setEnabled(true);
						startButton.setText("Continue");
						stopButton.setEnabled(true);
						paramPanel.setVisible(false);
						statusLabel.setVisible(true);
					}
					break;
				case FINISHED:
					status = BackgroundStatus.FINISHED;
					if(startButton!=null) {
						startButton.setEnabled(true);
						startButton.setText("Layout Network");
						stopButton.setEnabled(false);
						paramPanel.setVisible(true);
						statusLabel.setVisible(false);
					}
					break;
				default:
					status = BackgroundStatus.STATUSERROR;
					break;
				}

			}
		});
	}

	/**
	 * update the GUI graph in the background
	 * @param nodes2newPositions
	 */
	private void newLayout(HashMap<Node, Vector2d> nodes2newPositions) {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					GraphHelper.applyUndoableNodePositionUpdate(nodes2newPositions, getName());
					// keeps intermediate layouts from leaving the screen
					GravistoService.getInstance().runAlgorithm(new CenterLayouterAlgorithm(), graph,
							new Selection(""), null);
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * shows the status description in the bar at the bottom of the main frame
	 * and in the parameter panel
	 * @param statusDescription
	 */
	private void printStatusDescription(String statusDescription){
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				MainFrame.showMessage(statusDescription, MessageType.PERMANENT_INFO);
				if(statusLabel!=null) {
					statusLabel.setText(statusDescription);
				}
			}
		});
	}

	@Override
	public String getName() {
		return algorithm.getName();
	}

	@Override
	public void attach(Graph g, Selection selection) {
		this.graph=g;
		this.selection=selection;
		algorithm.attach(g, selection);
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

	/**
	 * Starts new thread which executes the BackgroundAlgorithm and starts the progress bar
	 */
	public void executeBackgroundAlgorithm() {

		final BackgroundTaskStatusProviderSupportingExternalCall provider = new BackgroundTaskStatusProviderSupportingExternalCallImpl(
				"Computing Layout", "Stress minimization progress:");

		Runnable myTask = new Runnable() {
			@Override
			public void run() {
				provider.setCurrentStatusValue(0);
				int currentStatus = 0;
				//double a = Math.pow(10, -8);
				//double b = Math.pow(10, 10);
				while(algorithm.getStatus() != BackgroundStatus.FINISHED) {
					try {
						Thread.sleep(100);

						//Exponential growth. a and b are chosen so
						//the following points hold: (0/0), (1/100), (0.9/10)
						//currentStatus = (int) (a * Math.pow(b, algorithm.getProgress() - a));

						if(currentStatus < 0) {
							currentStatus = 0;
						}

						// First 10% get the first 10%
						if(algorithm.getProgress() < 1.0) {
							currentStatus = (int) (algorithm.getProgress() * 100);
						}

						//First 90% get 40% in the progress bar
						if(algorithm.getProgress() < 0.9) {
							currentStatus = (int) (algorithm.getProgress()*100 * 0.4 + 10);
						}
						//90-99 get 30% in the progress bar
						else if(algorithm.getProgress() < 0.99) {
							currentStatus = (int) ((algorithm.getProgress()-0.9)*1000 * 0.3 + 50);
						}
						//99-100 get 20% in the progress bar
						else if(algorithm.getProgress() < 0.999) {
							currentStatus = (int) ((algorithm.getProgress()-0.99)*10000 * 0.2 + 80);
						}

						provider.setCurrentStatusValue(currentStatus);

						if (provider.wantsToStop()) {
							algorithm.stop();
						}

					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};

		// the task which will be executed even if the user has stopped the
		// thread
		Runnable myFinishTask = new Runnable() {
			@Override
			public void run() {}
		};

		reset();

		//current graph position and selection
		graph = GravistoService.getInstance().getMainFrame().getActiveSession().getGraph();
		selection = GravistoService.getInstance().getMainFrame().getActiveEditorSession().getSelectionModel().getActiveSelection();

		Runnable algoExecution = new Runnable() {
			@Override
			public void run() {

				attach(graph, selection);
				try {
					check();
				} catch (PreconditionException e) {
					ErrorMsg.addErrorMessage(e.getMessage());
					setStatus(BackgroundStatus.FINISHED);
					return;
				}
				setParameters(paramPanel.getUpdatedParameters());

				try {
					execute();
				} catch (OutOfMemoryError error) {
					ErrorMsg.addErrorMessage("We ran out of memory! Pleace increase heap memory of the JVM");
					algorithm.reset();
				} catch (Exception ex) {
					ErrorMsg.addErrorMessage("Failed to calculate layout: " + ex.getMessage());
					algorithm.reset();
				} finally {
					setStatus(BackgroundStatus.FINISHED);
				}

			}
		};
		Thread backgroundTask = new Thread(algoExecution);
		backgroundTask.setName(algorithm.getName()+" background execution");
		setStatus(BackgroundStatus.INIT);
		backgroundTask.start();

		BackgroundTaskHelper.issueSimpleTask("Stress Minimization Progress",
				"Computing...", myTask, myFinishTask, provider);
	}

	@Override
	public void reset() {
		algorithm.reset();
	}

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
		//set component layout (component is the panel above the layout list 
		//in the layout tab for the chosen BackgroundAlgorithm)
		SingleFiledLayout sfl = new SingleFiledLayout(SingleFiledLayout.COLUMN, SingleFiledLayout.FULL, 1);
		jc.setLayout(sfl);

		//create input component for each algorithm parameter and save all in ParameterEditPanel pep
		EditComponentManager ecm = MainFrame.getInstance().getEditComponentManager();
		ParameterEditPanel pep = null;
		pep = new ParameterEditPanel(algorithm.getParameters(), ecm.getEditComponents(), selection,
				algorithm.getName(), true, algorithm.getName());
		paramPanel = pep;

		//panel with all algorithm parameter components
		JPanel parameterPanel=new JPanel();
		parameterPanel.setLayout(sfl);
		//it is for a gap between start and stop parameter and the specific algorithm parameter
		parameterPanel.add(Box.createVerticalStrut(10));
		parameterPanel.add(pep);

		//initialization of start and pause button
		startButton = new JMButton("Layout Network");
		startButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {

				switch(status) {
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

		//initialization of stop button
		stopButton = new JButton("Stop");
		stopButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				algorithm.stop();
				stopButton.setEnabled(false);
				startButton.setEnabled(false);
			}
		});
		stopButton.setEnabled(false);

		//check box to print the layout after each iteration
		autoDrawCheckBox = new JCheckBox("Auto Redraw", false);

		//add stop button and check box to component
		jc.add(TableLayout.get3Split(stopButton, autoDrawCheckBox, new JLabel(),
				TableLayout.PREFERRED, TableLayout.PREFERRED,TableLayout.FILL ));

		jc.add(new JSeparator(JSeparator.HORIZONTAL));
		
		//add label for status informations to component
		statusLabel = new JLabel();
		statusLabel.setVisible(false);
		jc.add(statusLabel);

		//add parameter panel to component
		if(paramPanel!=null) {
			jc.add(parameterPanel);
		}

		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		//updating layout and status if the background algorithm request

		switch(arg0.getPropertyName()) {

		case "setLayout":
			if(autoDrawCheckBox!=null) {
				if(autoDrawCheckBox.isSelected()) {
					Supplier<HashMap<Node, Vector2d>> layoutSupplier = (Supplier<HashMap<Node, Vector2d>>)arg0.getNewValue();
					newLayout(layoutSupplier.get());
				}
			}
			break;
		case "setStatus":
			setStatus((BackgroundStatus)arg0.getNewValue());
			break;
		case "setStatusDescription":
			printStatusDescription((String)arg0.getNewValue());
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