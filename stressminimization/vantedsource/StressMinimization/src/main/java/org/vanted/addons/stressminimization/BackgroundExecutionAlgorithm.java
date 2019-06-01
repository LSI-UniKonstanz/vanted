package org.vanted.addons.stressminimization;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import info.clearthought.layout.SingleFiledLayout;

/**
 * Executes a BackgroundAlgorithm and manage the GUI interaction.
 * 
 */
public class BackgroundExecutionAlgorithm extends ThreadSafeAlgorithm {

	private BackgroundAlgorithm algorithm;
	private Graph graph;
	private Selection selection;
	private String status;
	private Thread backgroundTask;
	private JButton startButton;
	private JCheckBox autoDrawCheckBox;
	private boolean autoDraw;
	private SpinnerEditComponent[] parameterAlgo;
	private boolean stop;
	
	
	public BackgroundExecutionAlgorithm(BackgroundAlgorithm algorithm) {
		super();
		this.algorithm=algorithm;
		status="init, not started";
		autoDraw=false;
		stop=false;
		if(algorithm.getParameters()!=null) {
			parameterAlgo = new SpinnerEditComponent[algorithm.getParameters().length];
		}
		else {
			parameterAlgo=null;
		}
		//reference to BackgroundExecutionAlgorithm to apply newLayout and setState
		algorithm.setBackgroundExecutionAlgorithm(this);
	}
	
	/**
	 * set status of running algorithm
	 * @param statusValue
	 */
	public synchronized void setStatus(int statusValue) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (statusValue == 0) {
					status="init, not started";
					startButton.setText("Layout Network");
				}
				else if (statusValue == 1) {
					status= "running";
					autoDrawCheckBox.setEnabled(false);
				}
				else if (statusValue == 2) {
					status= "idle";
				}
				else if (statusValue == 3) {
					status="finished";
					startButton.setText("Layout Network");
					autoDrawCheckBox.setEnabled(true);
				}
				else {
					status= "status error";
				}
				System.out.println("Status background task: "+status);
			}
		});
	}
	
	/**
	 * update the GUI graph in the background
	 * @param nodes2newPositions
	 */
	public synchronized void newLayout(int n,RealMatrix layout,List<Node> nodes) {
		//run new thread
		new Thread(new Runnable() {
			public void run() {
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
	 * return boolean if auto draw check box is checked
	 * @return
	 */
	public synchronized boolean getAutoDraw(){
		return autoDraw;
	}
	
	/**
	 * Check if stop button clicked. If stop button
	 * clicked the Thread sleep until the button is
	 * pressed again. 
	 */
	public synchronized void isStopButtonClick() {
		if(stop) {
			while(stop) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * set parameter to stop or continue the background task 
	 * @param stop
	 */
	private synchronized void setStop(boolean stop) {
		this.stop=stop;
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
		selection =GravistoService.getInstance().getMainFrame().getActiveEditorSession().getSelectionModel().getActiveSelection();
		
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
				setStatus(0);
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
				if(status.compareTo("init, not started")==0||status.compareTo("finished")==0) {
					startButton.setText("Stop Algorithm");
					execute();
				}
				if(status.compareTo("running")==0) {
					setStop(true);	//stop background thread
					setStatus(2);
					startButton.setText("Continue");
				}
				if(status.compareTo("idle")==0) {
					setStop(false);	//continue background thread
					setStatus(3);
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
				
				//name of the parameter
				JLabel paramLabel = new JLabel(algorithm.getParameters()[i].getName());
				jc.add(paramLabel);
				
				//spinner component
				SpinnerEditComponent spinnerComponent = new SpinnerEditComponent(algorithm.getParameters()[i]);
				jc.add(spinnerComponent.getComponent());
				parameterAlgo[i]=spinnerComponent;
				
				JSpinner spinner =(JSpinner)spinnerComponent.getComponent();
				spinner.setToolTipText(algorithm.getDescription());
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
		
	}

	@Override
	public void resetDataCache(ThreadSafeOptions options) {
		
	}

	@Override
	public void attach(Graph g, Selection selection) {
		this.graph=g;
		this.selection=selection;
		algorithm.attach(g, selection);
	}
}