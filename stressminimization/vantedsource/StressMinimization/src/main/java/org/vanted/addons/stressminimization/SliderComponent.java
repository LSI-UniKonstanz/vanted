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

public class SliderComponent extends AbstractValueEditComponent{

	private JSlider slider;
	private double value;
	private int min;
	private int max;
	private int def;
	private boolean pos;
	private boolean neg;
	private Dictionary dict;
	
	public SliderComponent(Displayable disp){
		super(disp);
		SliderOptions pars = (SliderOptions)disp.getValue();
		this.min = pars.getMin();
		this.max = pars.getMax();
		this.def = pars.getDef();
		this.value = def;
		this.pos = pars.isPos();
		this.neg = pars.isNeg();
		this.dict = pars.getDict();
		
		//If we want infinity, create a value on the slider for that
		if(pos) {
			max++;
		}
		if(neg) {
			min--;
		}
		
		
		//Create the slider
		slider = new JSlider(JSlider.HORIZONTAL, min, max, def);
		slider.setMajorTickSpacing(2);
		slider.setMinorTickSpacing(1);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		
		//Create the Labels for the Slider
		if(dict == null) {
			dict = new Hashtable();
			for(int i = min; i<=max; i++) {
				if(pos && i==max) {
					dict.put(i, new JLabel("\u221e"));
				}
				else if(neg && i==min) {
					dict.put(i, new JLabel("-\u221e"));
				}
				else 
					dict.put(i, new JLabel(Integer.toString(i)));
				
			}

		}
		slider.setLabelTable(dict);
		
		
		
		
		//Change the value of epsilon on slider movement
		slider.addChangeListener(new ChangeListener() {
	         public void stateChanged(ChangeEvent e) {
	            value = ((JSlider)e.getSource()).getValue();
	            //Minimum value of epsilon is 10^(-infinity)
	            if(value == min && neg) {
	            	value = Double.NEGATIVE_INFINITY;
	            }
	            else if(value == max && pos) {
	            	value = Double.POSITIVE_INFINITY;
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

