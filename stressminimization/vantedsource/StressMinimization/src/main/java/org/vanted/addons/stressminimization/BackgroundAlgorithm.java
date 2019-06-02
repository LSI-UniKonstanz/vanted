package org.vanted.addons.stressminimization;



import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.apache.commons.math3.linear.RealMatrix;
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
	private int status;
	/**
	 * layout changes of the graph
	 */
	private RealMatrix layout;
	
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
     * @param layout
     */
    public void setLayout(RealMatrix layout) {
    	 pcs.firePropertyChange("setLayout",this.layout, layout);
    }
    
    /**
     * notify listener classes end layout of the graph is available
     * @param layout
     */
    public void setEndLayout(RealMatrix layout) {
   	 pcs.firePropertyChange("setEndLayout",this.layout, layout);
   }
    
    /**
     * notify listener classes a new status of the algorithm is available
     * @param status
     */
    public void setStatus(int status) {
    	pcs.firePropertyChange("setStatus", this.status, status);
    }
}
