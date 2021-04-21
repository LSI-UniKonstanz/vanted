package org.vanted.plugins.layout.multilevelframework;

/**
 * Implementing classes have a name and description.
 *
 * @author Jannik
 */
public interface Describable {
	
	/**
	 * @return
	 *         the name of the implementing class.
	 *         This may be be used to represent this class to the user.
	 *         It should not be {@code null} and be unique between all classes
	 *         that implement this interface.
	 * @author Jannik
	 */
	public String getName();
	
	/**
	 * @return
	 *         the description of the implementing class.
	 *         This may be be used to explain the behaviour of this class to the user.
	 *         It should not be {@code null}.
	 * @author Jannik
	 */
	public String getDescription();
}
