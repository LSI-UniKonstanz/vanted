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
/*
 * Created on Mar 17, 2010 by Christian Klukas
 */
package org.vanted.addons.exampleaddon;

/**
 * @author klukas
 */
public enum WindowOrder {

	HORIZONTAL("Horizontal"), QUADRATIC("Grid"), VERTICAL("Vertical");

	String text;

	WindowOrder(String text) {
		this.text = text;
	}

	public String toString() {
		return text;
	}
}
