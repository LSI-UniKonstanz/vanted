/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
package vanted_feature;

import java.util.prefs.Preferences;

import org.FeatureSet;
import org.Release;
import org.ReleaseInfo;
import org.SettingsHelperDefaultIsTrue;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;

/**
 * @author Christian Klukas
 */
public class PluginFeatureDataMapping extends IPK_PluginAdapter {

	public PluginFeatureDataMapping() {
		if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR)
			if (new SettingsHelperDefaultIsTrue().isEnabled("Experiment-data processing"))
				ReleaseInfo.enableFeature(FeatureSet.DATAMAPPING);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graffiti.plugin.GenericPlugin#configure(java.util.prefs.Preferences)
	 */
	@Override
	public void configure(Preferences p) {
		super.configure(p);
	}
}
