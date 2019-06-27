package org.vanted.addons.stressminimization.parameters;

import java.util.Dictionary;

import org.graffiti.plugin.parameter.AbstractSingleParameter;

public class SliderParameter extends AbstractSingleParameter{

	private SliderOptions pars;
	
	public SliderParameter(double def, String name, String description, int min, int max) {
		super(def, name, description);
		pars = new SliderOptions(min, max, def);
		setValue(def);
	}
	
	public SliderParameter(int def, String name, String description, int min, int max, boolean pos, boolean neg) {
		super(def, name, description);
		pars = new SliderOptions(min, max, def, pos, neg);
		setValue(def);
	}
	
	public SliderParameter(int def, String name, String description, int min, int max, boolean pos, boolean neg, Dictionary dict) {
		super(def, name, description);
		pars = new SliderOptions(min, max, def, pos, neg, dict);
		setValue(def);
	}
	
	public SliderOptions getSliderOptions() {
		return pars;
	}

	@Override
	public void setValue(Object val) {
		Double doubleValue;
		if (val instanceof Integer) {
			doubleValue = (double) (int) (Integer) val;
		} else if (val instanceof Double) {
			doubleValue = (Double) val;
		} else {
			throw new IllegalArgumentException();
		}
		super.setValue(doubleValue);
	}
	
}
