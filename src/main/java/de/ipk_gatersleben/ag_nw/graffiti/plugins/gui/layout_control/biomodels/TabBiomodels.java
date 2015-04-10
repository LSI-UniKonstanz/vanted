/**
 * 
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.biomodels;

import java.awt.BorderLayout;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import javax.swing.JOptionPane;
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
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.sbml.SBML_XML_Reader;

/**
 * @author matthiak
 *
 */
public class TabBiomodels extends InspectorTab
implements BiomodelsLoaderCallback{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4603467064417816569L;

	
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
		final SimpleModel finalModel = model;
		try {
			final InputStream bis = new ByteArrayInputStream( modelstring.getBytes() );
			is = MainFrame.getInstance().getIoManager().createInputSerializer(null, ".sbml");

			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					try {
						Graph g = new AdjListGraph();
						g.setName(finalModel.getName());
						boolean saveReaderstate = true;
						if(is instanceof SBML_XML_Reader){
							saveReaderstate = SBML_XML_Reader.isValidatingSBMLOnLoad();
							SBML_XML_Reader.doValidateSBMLOnLoad(false);
						}
						is.read(bis, g);
						if(is instanceof SBML_XML_Reader){
							SBML_XML_Reader.doValidateSBMLOnLoad(saveReaderstate);
						}
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


	}

	
	@Override
	public void resultError(Exception e) {
		JOptionPane.showMessageDialog(MainFrame.getInstance(), "Unable to communicate with Biomodels Webservice", "Communication Error", JOptionPane.ERROR_MESSAGE);
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
