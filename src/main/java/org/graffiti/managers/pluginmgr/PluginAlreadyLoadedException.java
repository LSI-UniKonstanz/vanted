package org.graffiti.managers.pluginmgr;

public class PluginAlreadyLoadedException extends PluginManagerException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5260911705903582512L;

	public PluginAlreadyLoadedException(String key) {
		super(key);
	}
}
