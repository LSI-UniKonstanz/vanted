package org.vanted.addons.stressminimization;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.JMButton;
import org.Vector2d;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.algorithm.ThreadSafeAlgorithm;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.editcomponent.SpinnerEditComponent;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.selection.Selection;


import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.graph_to_origin_mover.CenterLayouterAlgorithm;
import info.clearthought.layout.SingleFiledLayout;
import info.clearthought.layout.TableLayout;

/**
 * Executes a BackgroundAlgorithm and manage the GUI interaction.
 * 
 */
public class BackgroundExecutionAlgorithm extends ThreadSafeAlgorithm implements PropertyChangeListener {

	private BackgroundAlgorithm algorithm;
	private Graph graph;
	private Selection selection;
	private BackgroundStatus status;
	private Thread backgroundTask;
	private JButton startButton;
	private JButton stopButton;
	private JCheckBox autoDrawCheckBox;
	private boolean autoDraw;
	private SpinnerEditComponent[] parameterAlgo;
	private boolean stop;
	private boolean pause;
	private double diffStress;
	
	public BackgroundExecutionAlgorithm(BackgroundAlgorithm algorithm) {
		super();
		this.algorithm=algorithm;
		status=BackgroundStatus.INIT;
		autoDraw=false;
		pause=false;
		stop=false;
		diffStress=0;
		
		if(algorithm.getParameters()!=null) {
			parameterAlgo = new SpinnerEditComponent[algorithm.getParameters().length];
		}
		else {
			parameterAlgo=null;
		}
		
		algorithm.addPropertyChangeListener(this);
	}
	
	/**
	 * set status of running algorithm
	 * @param statusValue
	 */
	private void setStatus(BackgroundStatus statusValue) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				switch(statusValue) {
				case INIT:
					status=BackgroundStatus.INIT;
					startButton.setText("Layout Network");
					break;
				case RUNNING:
					status= BackgroundStatus.RUNNING;
					autoDrawCheckBox.setEnabled(false);
					stopButton.setEnabled(true);
					break;
				case IDLE:
					status= BackgroundStatus.IDLE;
					stopButton.setEnabled(false);
					break;
				case FINISHED:
					status=BackgroundStatus.FINISHED;
					startButton.setText("Layout Network");
					autoDrawCheckBox.setEnabled(true);
					stopButton.setEnabled(false);
					stop=false;
					break;
				default:
					status= BackgroundStatus.STATUSERROR;
					break;
				}
				
				System.out.println("Status background task: "+status);
				//show status of the running algorithm in the bar at the bottom of the main frame
				if(diffStress==0) {
					MainFrame.showMessage("Stress Minimization: "+status+"calc distances", MessageType.PERMANENT_INFO);
				}
				MainFrame.showMessage("Stress Minimization: "+status+", difference: "+diffStress, MessageType.PERMANENT_INFO);
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
				public void run() {
					GraphHelper.applyUndoableNodePositionUpdate(nodes2newPositions, getName());	
					GravistoService.getInstance().runAlgorithm(new CenterLayouterAlgorithm(), graph,
							new Selection(""), null);
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * shows the difference between old stress value and new stress value
	 * in the bar at the bottom of the main frame 
	 * @param oldStressValue
	 * @param newStressValue
	 */
	private void printStressValueDiff(double oldStressValue, double newStressValue){
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
//				MainFrame.showMessage("Stress Minimization: "+status+", old stress value: "+oldStressValue+", new stress value: "+
//						newStressValue+", difference: "+((oldStressValue - newStressValue) / oldStressValue), MessageType.PERMANENT_INFO);
				diffStress=((oldStressValue - newStressValue) / oldStressValue);
				MainFrame.showMessage("Stress Minimization: "+status+", difference: "+diffStress, MessageType.PERMANENT_INFO);
			}
		});
	}
	
	/**
	 * return whether pause button was pressed
	 * @return pause
	 */
	public boolean pauseButtonPressed() {
		return pause;
	}
	
	/**
	 * return whether stop button was pressed
	 * @return stop
	 */
	public boolean stopButtonPressed() {
		return stop;
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
		//current graph position and selection
		graph =GravistoService.getInstance().getMainFrame().getActiveSession().getGraph();
		selection = GravistoService.getInstance().getMainFrame().getActiveEditorSession().getSelectionModel().getActiveSelection();;
		
		Runnable algoExecution = new Runnable() {
			public void run() {
				
				Selection selectAll = new Selection();
				selectAll.addAll(graph.getNodes());
				
				algorithm.attach(graph, selection);
				algorithm.setParameters(updateParameters());
				try {
					algorithm.check();
				} catch (PreconditionException e) {
					e.printStackTrace();
				}
				algorithm.execute();
				setStatus(BackgroundStatus.INIT);
			}
		};
		backgroundTask = new Thread(algoExecution);
		backgroundTask.setName(algorithm.getName()+" background execution");
		backgroundTask.start();
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
		return true;
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
		//set component layout
		SingleFiledLayout sfl = new SingleFiledLayout(SingleFiledLayout.COLUMN, SingleFiledLayout.FULL, 1);
		jc.setLayout(sfl);
		
		//initialization of start and pause button
		startButton = new JMButton("Layout Network");
		startButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				switch(status) {
				
				case INIT:
					startButton.setText("Pause");
					execute();
					break;
				case FINISHED:
					startButton.setText("Pause");
					execute();
					break;
				case RUNNING:
					pause=true;	//stop background thread
					setStatus(BackgroundStatus.IDLE);
					startButton.setText("Continue");
					break;
				case IDLE:
					pause=false;	//continue background thread
					setStatus(BackgroundStatus.RUNNING);
					startButton.setText("Pause");
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
				stop=true;
				stopButton.setEnabled(false);
			}
		});
		stopButton.setEnabled(false);
		
		//check box to print the layout after each iteration
		autoDrawCheckBox = new JCheckBox("Auto Redraw",this.autoDraw);
		autoDrawCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				autoDraw=autoDrawCheckBox.isSelected();
			}
		});
		
		//add stop button and check box to component
		jc.add(TableLayout.get3Split(stopButton, autoDrawCheckBox, new JLabel(),
			TableLayout.PREFERRED, TableLayout.PREFERRED,TableLayout.FILL ));
				
		
		//for each parameter of the algorithm a JSpinner component is created
		if(algorithm.getParameters()!=null) {
			for(int i=0;i<algorithm.getParameters().length;i++) {
				
				//name of the algorithm parameter
				JLabel paramLabel = new JLabel(algorithm.getParameters()[i].getName());
				jc.add(paramLabel);
				
				SpinnerEditComponent spinnerComponent = new SpinnerEditComponent(algorithm.getParameters()[i]);
				jc.add(spinnerComponent.getComponent());
				parameterAlgo[i]=spinnerComponent;
				
				JSpinner spinner =(JSpinner)spinnerComponent.getComponent();
				spinner.addChangeListener(new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent arg0) {
						System.out.println("Parameter "+ spinner.getName() +":  "+spinner.getValue());
						//check JSpinner value and update value of the Parameter Object 
						spinnerComponent.getDisplayable().setValue(spinner.getValue());
					}
					
				});
			}
		}
		return true;
	}
	
	/**
	 * pick out all algorithm parameters from the GUI components  
	 * @return Parameter[]
	 */
	private Parameter[] updateParameters() {
		if(parameterAlgo!=null) {
			Parameter[] param = new Parameter[algorithm.getParameters().length];
			if(algorithm.getParameters()!=null) {
				for(int i=0;i<algorithm.getParameters().length;i++) {
					param[i]=(Parameter)parameterAlgo[i].getDisplayable();	
				}
			}
			return param;
		}
		return null;
	}

	@Override
	public void executeThreadSafe(ThreadSafeOptions options) {
		execute();
	}

	@Override
	public void resetDataCache(ThreadSafeOptions options) {
		reset();
	}

	@Override
	public void attach(Graph g, Selection selection) {
		this.graph=g;
		this.selection=selection;
		algorithm.attach(g, selection);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		//updating layout and status if the background algorithm request
		
		switch(arg0.getPropertyName()) {
			
		case "setLayout":
			if(autoDraw) {
				newLayout((HashMap<Node, Vector2d>)arg0.getNewValue());
			}
			break;
		case "setStatus":
			setStatus((BackgroundStatus)arg0.getNewValue());
			break;
		case "setEndLayout":
			newLayout((HashMap<Node, Vector2d> )arg0.getNewValue());
			break;
		case "setStressValue":
			printStressValueDiff((double)arg0.getOldValue(), (double)arg0.getNewValue());
			break;
		default:
			break;
		}
	}
}