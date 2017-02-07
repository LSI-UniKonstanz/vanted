package org.vanted.scaling;

import java.awt.Font;

import javax.swing.Icon;
import javax.swing.plaf.FontUIResource;
/**
 * It is sufficient enough to rescale only the default font,
 * this fire a property change and Nimbus does the rest of the 
 * work in that regard. Icons are sometimes shown with marks, so
 * we return them unchanged. 
 * 
 * @author dim8
 *
 */
public class NimbusScaler extends BasicScaler {
	
	public NimbusScaler(float scaleFactor) {
		super(scaleFactor);
	}

	public void initialScaling() {
		
		Font font = uiDefaults.getFont("defaultFont");
		
		if (font != null)
			uiDefaults.put("defaultFont", 
					new FontUIResource(font.getName(), font.getStyle(),
							Math.round(font.getSize() * scaleFactor)));
	}

	//Nimbus scales the rest internally based on the above change
	public Font modifyFont(Object key, Font original) {
		return original;
	}

	// Distortions & marks are visible, no modifications
	public Icon modifyIcon(Object key, Icon original) {
		return original;
	}
}