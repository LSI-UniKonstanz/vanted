package org.vanted.addons.stressminimization;



import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;

import org.Vector2d;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;


/**
 * Algorithm which runs in the background and 
 * and able to notify other classes about state
 * of the algorithm or the layout change of the graph.
 */
public abstract class BackgroundAlgorithm extends AbstractEditorAlgorithm{
	/**
	 * change support to notify other classes  
	 */
	protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	/**
	 * status of the algorithm
	 */
	private BackgroundStatus status;
	/**
	 * layout changes of the graph
	 */
	private HashMap<Node, Vector2d> nodes2newPositions;
	
	/**
	 * return list of PropertyChangeListener
	 * @return PropertyChangeListener
	 */
	public PropertyChangeListener[] getPropertyChangeListener() {
		return pcs.getPropertyChangeListeners();
	}
	
	/**
	 * add new PropertyChangeListener
	 * @param pcl
	 */
	public void addPropertyChangeListener(PropertyChangeListener pcl) {
        pcs.addPropertyChangeListener(pcl);
    }
    /**
     * remove PropertyChangeListener
     * @param pcl
     */
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        pcs.removePropertyChangeListener(pcl);
    }
    
    /**
     * add elements of an array to the property change list
     * @param pcl
     */
    public void addPropertyChangeListeners(PropertyChangeListener[] pcl) {
        for(PropertyChangeListener listener:pcl) {
        	pcs.addPropertyChangeListener(listener);
        }
    }
    
    /**
     * notify listener classes a new graph layout is available and
     * send the old and the new layout
     * @param nodes2newPositions
     */
    public void setLayout(HashMap<Node, Vector2d> nodes2newPositions) {
    	 pcs.firePropertyChange("setLayout",this.nodes2newPositions, nodes2newPositions);
    }
    
    /**
     * notify listener classes end layout of the graph is available
     * @param nodes2newPositions
     */
    public void setEndLayout(HashMap<Node, Vector2d> nodes2newPositions) {
   	 pcs.firePropertyChange("setEndLayout",this.nodes2newPositions, nodes2newPositions);
   }
    
    /**
     * notify listener classes a new status of the algorithm is available
     * @param status
     */
    public void setStatus(BackgroundStatus status) {
    	pcs.firePropertyChange("setStatus", this.status, status);
    }
    
    /**
     * check if first listener in the list is form class BackgroundExecutionAlgorithm 
     * and if true than check if stop Button pressed. And if true then thread stop
     * until status of stop button has changed
     */
    public void isStopButtonPressed() {
    	if(((Object) getPropertyChangeListener()[0]).getClass().equals(BackgroundExecutionAlgorithm.class)) {
    		BackgroundExecutionAlgorithm b =(BackgroundExecutionAlgorithm)((Object) getPropertyChangeListener()[0]);
    		if(b.getStop()) {
    			while(b.getStop()) {
    				try {
    					Thread.sleep(100);
    				} catch (InterruptedException e) {
    					e.printStackTrace();
    				}
    			}
    		}
    	}
    }
}
