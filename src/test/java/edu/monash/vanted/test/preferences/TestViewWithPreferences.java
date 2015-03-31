package edu.monash.vanted.test.preferences;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import org.graffiti.options.PreferencesInterface;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugins.views.defaults.GraffitiView;

public class TestViewWithPreferences extends GraffitiView
implements PreferencesInterface{

	Preferences preferences;
	
	public TestViewWithPreferences() {
	
		System.out.println("constsructor called");
		

	}

	@Override
	public List<Parameter> getDefaultParameters() {
		ArrayList<Parameter> arrayList = new ArrayList<Parameter>();
		arrayList.add(new BooleanParameter(true, "BooleanParamForTestView", "this is some arbitrary description"));
		return arrayList;
	}

	@Override
	public void updatePreferences(Preferences preferences) {
		// TODO Auto-generated method stub
		
	}


//	@Override
//	public Preferences getPreferences() {
//		if(preferences == null)
//			preferences = PreferenceManager.getPreferenceForClass(IPKGraffitiView.class);
//		return preferences;
//	}
	
}
