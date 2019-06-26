package org.vanted.addons.stressminimization;

import java.util.Dictionary;

import org.graffiti.plugin.parameter.AbstractSingleParameter;

public class SliderParameter extends AbstractSingleParameter{

	public SliderParameter(int def, String name, String description, int min, int max) {
		super(def, name, description);
		SliderOptions pars = new SliderOptions(min, max, def);
		super.setValue(pars);
	}
	
	public SliderParameter(int def, String name, String description, int min, int max, boolean pos, boolean neg) {
		super(def, name, description);
		SliderOptions pars = new SliderOptions(min, max, def, pos, neg);
		super.setValue(pars);
	}
	
	public SliderParameter(int def, String name, String description, int min, int max, boolean pos, boolean neg, Dictionary dict) {
		super(def, name, description);
		SliderOptions pars = new SliderOptions(min, max, def, pos, neg, dict);
		super.setValue(pars);
	}

}
