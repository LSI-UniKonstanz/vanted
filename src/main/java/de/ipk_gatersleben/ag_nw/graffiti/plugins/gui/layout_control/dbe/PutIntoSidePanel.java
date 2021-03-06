package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.graffiti.editor.GravistoService;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.view.View;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;

/**
 * @author klukas
 */
public class PutIntoSidePanel extends AbstractExperimentDataProcessor {
	private ExperimentInterface md;
	private JComponent optGui;
	
	public String getName() {
		return "Put data in 'Experiments' tab";
	}
	
	@Override
	public void processData() {
		String tabtitle;
		try {
			tabtitle = md.getName();
		} catch (Exception e) {
			tabtitle = null;
		}
		if (tabtitle == null || tabtitle.isEmpty())
			tabtitle = ExperimentInterface.UNSPECIFIED_EXPERIMENTNAME;
		
		TabDBE.addOrUpdateExperimentPane(new ProjectEntity(tabtitle, md, optGui));
	}
	
	public boolean activeForView(View v) {
		return true;
	}
	
	@Override
	public void setExperimentData(ExperimentInterface md) {
		this.md = md;
	}
	
	@Override
	public void setComponent(JComponent optSupplementaryPanel) {
		this.optGui = optSupplementaryPanel;
	}
	
	@Override
	public ImageIcon getIcon() {
		return new ImageIcon(GravistoService.getResource(getClass(), "putintosidepanel.png", null));
	}
	
	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(Category.UI, Category.COMPUTATION));
	}
}
