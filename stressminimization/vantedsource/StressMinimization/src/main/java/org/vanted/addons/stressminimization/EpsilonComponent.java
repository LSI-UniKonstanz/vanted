package org.vanted.addons.stressminimization;


import java.util.Dictionary;
import java.util.Hashtable;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.AbstractValueEditComponent;

public class EpsilonComponent extends AbstractValueEditComponent{

	private JSlider slider;
	private double value;
	public EpsilonComponent(Displayable disp){
		super(disp);
		
		//Create the slider
		slider = new JSlider(JSlider.HORIZONTAL, -9, -1, -4);
		slider.setMajorTickSpacing(2);
		slider.setMinorTickSpacing(1);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		
		Dictionary dict = new Hashtable();
		for(int i = -9; i<0; i++) {
			if(i==-9) {
				dict.put(i, new JLabel("-\u221e"));
			}
			else 
				dict.put(i, new JLabel(Integer.toString(i)));
			
		}
		
		slider.setLabelTable(dict);
		
		
		
		
		//Change the value of epsilon on slider movement
		slider.addChangeListener(new ChangeListener() {
	         public void stateChanged(ChangeEvent e) {
	            value = ((JSlider)e.getSource()).getValue();
	            //Minimum value of epsilon is 10^(-infinity)
	            if(value == -9) {
	            	value = Double.NEGATIVE_INFINITY;
	            }
	         }
	      });
	}
	
	@Override
	public JComponent getComponent() {
		return slider;
	}

	@Override
	public void setEditFieldValue() {
		
	}

	@Override
	public void setValue() {
		displayable.setValue(value);
	}
	
	

}

