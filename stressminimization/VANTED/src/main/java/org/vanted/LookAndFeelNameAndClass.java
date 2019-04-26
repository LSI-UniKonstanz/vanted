package org.vanted;

import javax.swing.UIManager.LookAndFeelInfo;

import com.sun.istack.NotNull;

/**
 * Initial prototype by mathiak in VantedPreferences. This is an extracted class
 * from there. This version provides a bit more convenient way of initialization
 * and most of all it is <b>hashable</b>. It also proves to be a somewhat
 * lighter version of LookAndFeelInfo.
 * 
 * @version 1.1
 * 
 * @author mathiak
 * @author dim8
 *
 */
public class LookAndFeelNameAndClass {
	String name;
	String className;

	public LookAndFeelNameAndClass(LookAndFeelInfo lafi) {
		this.name = lafi.getName();
		this.className = lafi.getClassName();
	}

	public LookAndFeelNameAndClass(@NotNull String name, @NotNull Class<?> className) {
		this.name = name;
		this.className = className.getCanonicalName();
	}

	public LookAndFeelNameAndClass(@NotNull String name, @NotNull String className) {
		this.name = name;
		this.className = className;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return className;
	}

	@Override
	public int hashCode() {
		int hash = 11;

		hash = 31 * hash + ((name == null) ? 0 : name.hashCode());
		hash = 31 * hash + ((className == null) ? 0 : className.hashCode());

		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof LookAndFeelNameAndClass))
			return false;

		LookAndFeelNameAndClass lafnac = (LookAndFeelNameAndClass) obj;

		return this.name.equals(lafnac.getName()) && this.className.equals(lafnac.toString());
	}

}