package org.vanted.addons.multilevelframework;

import javax.swing.ImageIcon;

import org.ErrorMsg;
import org.graffiti.editor.GravistoService;
import org.graffiti.plugin.algorithm.Algorithm;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.addons.AddonAdapter;

public class MultilevelFrameworkAddon extends AddonAdapter {
	
	@Override
	protected void initializeAddon() {
		this.algorithms = new Algorithm[] {
				new MultilevelFrameworkLayouter(),
		};
	}
	
	@Override
	public ImageIcon getIcon() {
		try {
		    // TODO maybe edit the logo
			return new ImageIcon(GravistoService.getResource(this.getClass(), "mlf-logo", "png"));
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return super.getIcon();
		}
	}
	
}
