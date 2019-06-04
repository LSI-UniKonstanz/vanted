package org.vanted.addons.stressminimization;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.List;
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

import org.BackgroundTaskStatusProvider;
import org.JMButton;
import org.Vector2d;
import org.apache.commons.math3.linear.RealMatrix;
import org.graffiti.editor.GravistoService;
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
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskWindow;
import info.clearthought.layout.SingleFiledLayout;

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
	private JCheckBox autoDrawCheckBox;
	private boolean autoDraw;
	private SpinnerEditComponent[] parameterAlgo;
	private boolean stop;
	
	public BackgroundExecutionAlgorithm(BackgroundAlgorithm algorithm) {
		super();
		this.algorithm=algorithm;
		status=BackgroundStatus.INIT;
		autoDraw=false;
		stop=false;
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
	private synchronized void setStatus(BackgroundStatus statusValue) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (statusValue == BackgroundStatus.INIT) {
					status=BackgroundStatus.INIT;
					startButton.setText("Layout Network");
				}
				else if (statusValue == BackgroundStatus.RUNNING) {
					status= BackgroundStatus.RUNNING;
					autoDrawCheckBox.setEnabled(false);
				}
				else if (statusValue == BackgroundStatus.IDLE) {
					status= BackgroundStatus.IDLE;
				}
				else if (statusValue == BackgroundStatus.FINISHED) {
					status=BackgroundStatus.FINISHED;
					startButton.setText("Layout Network");
					autoDrawCheckBox.setEnabled(true);
				}
				else {
					status= BackgroundStatus.STATUSERROR;
				}
				System.out.println("Status background task: "+status);
			}
		});
	}
	
	/**
	 * update the GUI graph in the background
	 * @param nodes2newPositions
	 */
	private synchronized void newLayout(RealMatrix layout) {
		//run new thread
		new Thread(new Runnable() {
			public void run() {
				List<Node> nodes = graph.getNodes();
				int n = nodes.size();
				double scaleFactor = 100;
				HashMap<Node, Vector2d> nodes2newPositions = new HashMap<Node, Vector2d>();
				for (int i = 0; i < n; i += 1) {
					double[] pos = layout.getRow(i);
					Vector2d position = new Vector2d(pos[0] * scaleFactor, 
													 pos[1] * scaleFactor);
					nodes2newPositions.put(nodes.get(i), position);
				}
				GraphHelper.applyUndoableNodePositionUpdate(nodes2newPositions, getName());	
				GravistoService.getInstance().runAlgorithm(new CenterLayouterAlgorithm(), graph,
						new Selection(""), null);
			}
		}).start();
	}
	
	/**
	 * return if stop button is clicked
	 * @return stop
	 */
	public boolean getStop() {
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
		
		//initialization of start and stop button
		startButton = new JMButton("Layout Network");
		startButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if(status==BackgroundStatus.INIT||status==BackgroundStatus.FINISHED) {
					startButton.setText("Stop Algorithm");
					execute();
				}
				if(status==BackgroundStatus.RUNNING) {
					stop=true;	//stop background thread
					setStatus(BackgroundStatus.IDLE);
					startButton.setText("Continue");
				}
				if(status==BackgroundStatus.IDLE) {
					stop=false;	//continue background thread
					setStatus(BackgroundStatus.RUNNING);
					startButton.setText("Stop Algorithm");
				}
				
			}
		});
		jc.add(startButton);
		
		//check box to print the layout after each iteration
		autoDrawCheckBox = new JCheckBox("Auto Redraw",this.autoDraw);
		autoDrawCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				autoDraw=autoDrawCheckBox.isSelected();
			}
		});
		jc.add(autoDrawCheckBox);
		
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
	 * @return
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

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		//updating layout and status if the background algorithm request
		if(arg0.getPropertyName().compareTo("setLayout")==0) {
			if(autoDraw) {
				newLayout((RealMatrix)arg0.getNewValue());
			}
		}
		if(arg0.getPropertyName().compareTo("setStatus")==0) {
			setStatus((BackgroundStatus)arg0.getNewValue());
		}
		if(arg0.getPropertyName().compareTo("setEndLayout")==0) {
			newLayout((RealMatrix)arg0.getNewValue());
		}
	}
}