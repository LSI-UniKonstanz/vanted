/*************************************************************************************
 * The Exemplary Add-on is (c) 2008-2011 Plant Bioinformatics Group, IPK Gatersleben,
 * http://bioinformatics.ipk-gatersleben.de
 * The source code for this project, which is developed by our group, is
 * available under the GPL license v2.0 available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html. By using this
 * Add-on and VANTED you need to accept the terms and conditions of this
 * license, the below stated disclaimer of warranties and the licenses of
 * the used libraries. For further details see license.txt in the root
 * folder of this project.
 ************************************************************************************/
package org.vanted.addons.exampleaddon;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.AttributeHelper;
import org.ErrorMsg;
import org.StringManipulationTools;
import org.graffiti.graph.Graph;
import org.graffiti.plugin.io.OutputSerializer;

public class TestWriter implements OutputSerializer {
	
	@Override
	public void write(OutputStream stream, Graph g) throws IOException {
		PrintStream p;
		try {
			p = new PrintStream(stream, true, StringManipulationTools.Unicode);
		} catch (UnsupportedEncodingException e) {
			ErrorMsg.addErrorMessage(e);
			p = new PrintStream(stream, false);
		}
		
		p.println("# This is a test for serializing graphs");
		p.println("Graph contains " + g.getNodes().size() + " nodes and " + g.getEdges().size() + " edges.");
		p.println("written at " + AttributeHelper.getDateString(new Date()));
	}
	
	/**
	 * Specify all extensions you are able to write
	 */
	@Override
	public String[] getExtensions() {
		return new String[] { ".test" };
	}
	
	@Override
	public boolean validFor(Graph g) {
		return true;
	}
	
	/**
	 * Description of the file format, which will be shown in the file dialog as
	 * <p>
	 * "Example writer (*.test)"
	 */
	@Override
	public String[] getFileTypeDescriptions() {
		return new String[] { "Example Writer" };
	}
	
}
