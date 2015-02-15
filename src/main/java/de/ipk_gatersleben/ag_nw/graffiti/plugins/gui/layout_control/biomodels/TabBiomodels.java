/**
 * 
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.biomodels;

import java.awt.BorderLayout;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.List;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Graph;
import org.graffiti.plugin.inspector.InspectorTab;
import org.graffiti.plugin.io.InputSerializer;
import org.graffiti.plugin.view.View;

import uk.ac.ebi.biomodels.ws.SimpleModel;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.biomodels.BiomodelsAccessAdapter.BiomodelsLoaderCallback;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.biomodels.BiomodelsAccessAdapter.QueryType;

/**
 * @author matthiak
 *
 */
public class TabBiomodels extends InspectorTab
implements BiomodelsLoaderCallback{

	static final Logger logger = Logger.getLogger(TabBiomodels.class);
	
	static final String NAME = "Biomodels";
	
	BiomodelsPanel panel;
	/**
	 * 
	 */
	public TabBiomodels() {
		panel = new BiomodelsPanel();
		panel.getAdapter().addListener(this);
		setLayout(new BorderLayout());
		add(panel, BorderLayout.CENTER);
	}

	
	
	@Override
	public void resultForSimpleModelQuery(QueryType type,
			List<SimpleModel> simpleModel) {
	}



	@Override
	public void resultForSBML(SimpleModel model, String modelstring) {
		logger.debug("creating graph from sbml model");
			final InputSerializer is;
			final StringReader reader;
			final SimpleModel finalModel = model;
			try {
				final InputStream bis = new ByteArrayInputStream( modelstring.getBytes() );
				is = MainFrame.getInstance().getIoManager().createInputSerializer(null, ".sbml");
//				reader = new StringReader(modelstring);

				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						try {
							Graph g = new AdjListGraph();
							g.setName(finalModel.getName());
							is.read(bis, g);
							
							MainFrame.getInstance().showGraph(g, null);
						} catch (Exception e) {
							e.printStackTrace();
						}
					
					}
				});
			
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
			} catch (InstantiationException e1) {
				e1.printStackTrace();
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			

		
		/*
		try {
			FileWriter writer = new FileWriter("/tmp/sbml-testwrite-"+model.getId()+".sbml");
			writer.write(modelstring);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
	}



	@Override
	public String getTitle() {
		return NAME;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public boolean visibleForView(View v) {
		return true;
	}

	@Override
	public String getTabParentPath() {
		return "Pathways";
	}

	@Override
	public int getPreferredTabPosition() {
		return InspectorTab.TAB_TRAILING;
	}

	
}
