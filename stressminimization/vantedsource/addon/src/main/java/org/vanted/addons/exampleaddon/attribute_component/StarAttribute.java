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
package org.vanted.addons.exampleaddon.attribute_component;

import org.graffiti.attributes.BooleanAttribute;
import org.graffiti.attributes.StringAttribute;

/**
 * Please use only String-Attributes, because they are the only ones, which can
 * be serialized and deserialized. Even if there is the {@link BooleanAttribute}, we
 * emulate its functionality by writing out "0" for "false" and "1" for "true".
 * 
 * @author Hendrik Rohn
 */
public class StarAttribute extends StringAttribute {
	
	/**
	 * Static names are useful, if you want to change the path or name of the
	 * attribute later.
	 */
	public static final String path = "stars";
	public static final String name = "value";
	
	public StarAttribute() {
		super();
	}
	
	public StarAttribute(String id, String value) {
		super(id, value);
	}
	
	public StarAttribute(String id) {
		super(id);
	}
}
