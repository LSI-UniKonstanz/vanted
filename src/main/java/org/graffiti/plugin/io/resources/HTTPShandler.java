package org.graffiti.plugin.io.resources;

import java.io.InputStream;
import java.net.URL;

public class HTTPShandler extends AbstractResourceIOHandler {
	
	public static final String PREFIX = "https";
	
	public String getPrefix() {
		return PREFIX;
	}
	
	@Override
	public InputStream getInputStream(IOurl url) throws Exception {
		if (url.isEqualPrefix(getPrefix()))
			return new URL(url.toString()).openStream();
		else
			return null;
	}
	
	@Override
	public IOurl copyDataAndReplaceURLPrefix(InputStream is, String targetFilename, ResourceIOConfigObject config)
						throws Exception {
		throw new UnsupportedOperationException("HTTPS save not supported");
	}
	
	public static IOurl getURL(String httpurl) {
		return new IOurl(httpurl);
	}
}
