package org.vanted.garuda;

import java.io.File;
import java.net.URL;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import jp.sbi.garuda.backend.GarudaBackend;
import jp.sbi.garuda.backend.POJOs.CompatibleGadgetDetails;
import jp.sbi.garuda.backend.incomingHandler.garudaActionListeners.requests.adapters.LoadDataRequestAdapter;
import jp.sbi.garuda.backend.incomingHandler.garudaActionListeners.responses.adapters.ActivateGadgetResponseAdapter;
import jp.sbi.garuda.backend.incomingHandler.garudaActionListeners.responses.adapters.GetCompatibleGadgetListResponseAdapter;
import jp.sbi.garuda.backend.incomingHandler.responseCodes.GarudaResponseCode;
import jp.sbi.garuda.backend.net.exception.GarudaConnectionNotInitializedException;
import jp.sbi.garuda.backend.net.exception.NetworkConnectionException;

import org.AttributeHelper;
import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;
import org.graffiti.plugin.view.View;
import org.graffiti.session.EditorSession;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.helper.DBEgravistoHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBML_Constants;

public class GarudaBackendConnector extends AbstractEditorAlgorithm {
	
	GarudaBackend garudaBackend;
	
//	private static final String GRAUDA_UUID = "a80b506b-bd15-4c0a-9bc5-061f55e3a360";
//	private static final String GRAUDA_UUID = "4d62b271-d81d-43fb-849f-65063f2e449c";
	private static final String GRAUDA_UUID = "6daf2625-44e7-4dcc-945a-492d36163272"; //official VANTED ID in Garuda
	
	private static GarudaBackendConnector instance;
	
	public GarudaBackendConnector() {
		try {
			garudaBackend = new GarudaBackend(GRAUDA_UUID, DBEgravistoHelper.DBE_GRAVISTO_NAME_SHORT, MainFrame.getInstance());
			garudaBackend.activateGadget();
			
			garudaBackend.addGarudaGlassPanel(MainFrame.getInstance(), null);
			
			MainFrame.getInstance().setGlassPane(garudaBackend.getGarudaGlassPanel());
			
			garudaBackend.getIncomingResponseHandler().addActivateGadgetResponseActionListener(new ActivateGadgetResponseAdapter() {
				
				@Override
				public void gadgetActivationFailed(GarudaResponseCode arg0) {
					// TODO Auto-generated method stub
					JOptionPane.showMessageDialog(MainFrame.getInstance(), "Could not connect to Garuda", "Garuda Connenction error", JOptionPane.ERROR_MESSAGE);
				}
			});
			
			garudaBackend.getIncomingResponseHandler().addGetCompatibleGadgetListResponseActionListener(new GetCompatibleGadgetListResponseAdapter() {
				
				@Override
				public void gotCompatibleGadgetList(List<CompatibleGadgetDetails> arg0) {
					garudaBackend.getGarudaGlassPanel().showPanel(arg0);
				}
				
				@Override
				public void fileNotInOutBoundList(GarudaResponseCode arg0) {
					// TODO Auto-generated method stub
					JOptionPane.showMessageDialog(MainFrame.getInstance(), "Requested file not in outbound list", "Garuda error", JOptionPane.ERROR_MESSAGE);
				}
			});
			
			garudaBackend.getIncomingRequestHandler().addLoadDataRequestActionListener(new LoadDataRequestAdapter() {
				
				@Override
				public void loadDataRequestReceivedAsFile(File file, String senderId,
						String senderName) {
					MainFrame.getInstance().loadGraph(file);
					
				}
			});
			
		} catch (GarudaConnectionNotInitializedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NetworkConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static GarudaBackendConnector getInstance() {
		return instance;
	}
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Discover";
	}
	
	@Override
	public void execute() {
		if (garudaBackend == null)
			return;
		
		EditorSession activeEditorSession = MainFrame.getInstance().getActiveEditorSession();
		Graph graph = activeEditorSession.getGraph();
		if (graph.isModified())
			MainFrame.getInstance().saveActiveFileAs();
		
		String fileNameFull = activeEditorSession.getFileNameFull();
		String fileExtension = graph.getName().substring(graph.getName().lastIndexOf(".") + 1);
		if (fileExtension.equals("xml")) {
			if (AttributeHelper.hasAttribute(graph, SBML_Constants.SBML,
					SBML_Constants.MODEL_ID))
				garudaBackend.getCompatibleGadgetList(new File(fileNameFull), "SBML");
			else {
				Attribute attribute;
				try {
					attribute = graph.getAttribute("BioPax");
					garudaBackend.getCompatibleGadgetList(new File(fileNameFull), "biopax");
				} catch (AttributeNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else
		{
			garudaBackend.getCompatibleGadgetList(new File(fileNameFull), fileExtension);
		}
	}
	
	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getCategory() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getMenuCategory() {
		// TODO Auto-generated method stub
		return "Garuda";
	}
	
	@Override
	public boolean activeForView(View v) {
		if (v != null)
			return true;
		else
			return false;
	}
	
	@Override
	public boolean showMenuIcon() {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	public ImageIcon getIcon() {
		URL url = getClass().getResource("garuda-icon.png");
		if (url != null)
			return new ImageIcon(GravistoService.getScaledImage(new ImageIcon(url).getImage(), 16, 16));
		else
			return super.getIcon();
	}
	
}
