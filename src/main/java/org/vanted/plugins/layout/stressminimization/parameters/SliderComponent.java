package org.vanted.plugins.layout.stressminimization.parameters;


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
	private double def;
	private boolean pos;
	private boolean neg;
	private Dictionary<Integer, JLabel> dict;
	
	//===========================
	// Constructor
	//===========================
	
	public SliderComponent(Displayable disp){
		super(disp);
		SliderOptions pars = ((SliderParameter)disp).getSliderOptions();
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
		slider = new JSlider(JSlider.HORIZONTAL, min, max, (int) def);
		slider.setMajorTickSpacing(2);
		slider.setMinorTickSpacing(1);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);

		//set value to default value
		this.value = def;
		
		//Create the Labels for the Slider
		if(dict == null) {
			dict = new Hashtable<>();
			for(int i = min; i<=max; i++) {
				//Label with infinity symbol
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
		
		
		
		
		//Change the value on slider movement
		slider.addChangeListener(new ChangeListener() {
	         public void stateChanged(ChangeEvent e) {
	            //Set value to slider value
	        	 value = ((JSlider)e.getSource()).getValue();
	            
	        	 //If slider is set to a value representing infinity, change value accordingly
	            if(value == min && neg) {
	            	value = Double.NEGATIVE_INFINITY;
	            }
	            else if(value == max && pos) {
	            	value = Double.POSITIVE_INFINITY;
	            }
	            
	         }
	      });
	}	

	//=================================================
	// needed functions for AbstractValueEditComponent
	//=================================================
	
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

