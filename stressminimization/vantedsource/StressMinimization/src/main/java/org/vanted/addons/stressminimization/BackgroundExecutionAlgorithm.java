package org.vanted.addons.stressminimization;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Set;

import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.Vector2d;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.view.View;
import org.graffiti.selection.Selection;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;

/**
 * Executes a BackgroundAlgorithm and manage the GUI interaction.
 * 
 */
public class BackgroundExecutionAlgorithm extends AbstractEditorAlgorithm{
	private BackgroundAlgorithm algorithm;
	private Graph graph;
	private Selection selection;
	private String status="init, not started";
	private Thread backgroundTask;
	
	public BackgroundExecutionAlgorithm(BackgroundAlgorithm algorithm) {
		super();
		this.algorithm=algorithm;
	}
	
	/**
	 * set status of running algorithm
	 * @param statusValue
	 */
	public void setStatus(int statusValue) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (statusValue == 0) {
					status="init, not started";
				}
				if (statusValue == 1) {
					status= "running";
				}
				if (statusValue == 2) {
					status= "idle";
				}
				if (statusValue == 3) {
					status="finished";
				}
				status= "status error";
			}
		});
	}
	
	/**
	 * update the GUI graph in the background
	 * @param nodes2newPositions
	 */
	public void newLayout(HashMap<Node, Vector2d> nodes2newPositions) {
		//run new thread
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				GraphHelper.applyUndoableNodePositionUpdate(nodes2newPositions, getName());	
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
	public void attach(Graph g, Selection selection) {
		this.graph=g;		
		this.selection=selection;
		algorithm.attach(g, selection);
	}

	@Override
	public void check() throws PreconditionException {
		algorithm.check();
	}

	@Override
	public void execute() {
		//reference to BackgroundExecutionAlgorithm to apply newLayout and setState
		algorithm.setBackgroundExecutionAlgorithm(this);
		
		Runnable algoExecution = new Runnable() {
			public void run() {
				Selection selectAll = new Selection();
				selectAll.addAll(graph.getNodes());
				selectAll.addAll(graph.getEdges());
				
				algorithm.attach(graph, selectAll);
				try {
					algorithm.check();
				} catch (PreconditionException e) {
					e.printStackTrace();
				}
				algorithm.execute();
			}
		};
		backgroundTask=new Thread(algoExecution);
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
	public boolean activeForView(View v) {
		return false;
	}
}
