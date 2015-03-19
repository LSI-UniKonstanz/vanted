package edu.monash.vanted.test.preferences;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.KeyStroke;

import org.graffiti.graph.Graph;
import org.graffiti.managers.PreferenceManager;
import org.graffiti.options.PreferencesInterface;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.IntegerParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.selection.Selection;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.IPKGraffitiView;

public class TestAlgorithm implements Algorithm, PreferencesInterface{

	
	Preferences preferences;
	
	public TestAlgorithm() {
		
	}
	
	
	@Override
	public List<Parameter> getDefaultParameters() {
		ArrayList<Parameter> arrayList = new ArrayList<Parameter>();
		arrayList.add(new IntegerParameter(100, "TestAlgo-speed", "testalgo integer speed parameter"));
		return arrayList;
	}

	@Override
	public Preferences getPreferences() {
		if(preferences == null)
			preferences = PreferenceManager.getPreferenceForClass(IPKGraffitiView.class);
		return preferences;
	}
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "TestAlgo";
	}

	@Override
	public void setParameters(Parameter[] params) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Parameter[] getParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void attach(Graph g, Selection selection) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void check() throws PreconditionException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void execute() {
		// TODO Auto-generated method stub
		System.out.println("algo exec: prefs int: "+getPreferences().getInt("Test2Algo-speed", -1));
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getCategory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Category> getSetCategory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMenuCategory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isLayoutAlgorithm() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean showMenuIcon() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public KeyStroke getAcceleratorKeyStroke() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setActionEvent(ActionEvent a) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ActionEvent getActionEvent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean mayWorkOnMultipleGraphs() {
		// TODO Auto-generated method stub
		return false;
	}

	
}
