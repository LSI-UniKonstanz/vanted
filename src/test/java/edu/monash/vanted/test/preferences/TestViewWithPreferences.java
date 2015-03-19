package edu.monash.vanted.test.preferences;

import java.util.ArrayList;
import java.util.List;

import org.graffiti.options.PreferencesInterface;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.IntegerParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugins.views.defaults.GraffitiView;

public class TestViewWithPreferences extends GraffitiView
implements PreferencesInterface{

	static {
		
//		defaultPreferences.add(new BooleanParameter(true, "BooleanParamForTestView", "this is some arbitrary description"));
//		System.out.println("TestViewWithPreferences: added a bool preference");
	}
	
	public TestViewWithPreferences() {
	
		System.out.println("constsructor called");
		

	}

	@Override
	public List<Parameter> getDefaultParameters() {
		ArrayList<Parameter> arrayList = new ArrayList<Parameter>();
		arrayList.add(new BooleanParameter(true, "BooleanParamForTestView", "this is some arbitrary description"));
		return arrayList;
	}
	
}
