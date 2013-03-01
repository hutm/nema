/**
 * 
 */
package org.imirsel.nema.analytics.util.process;

/**
 * Class representing a String fragment forming part of a command line calling
 * format for a binary executable.
 * 
 * @author kris.west@gmail.com
 * @since 0.2.0
 */
public class StringCommandArgument implements CommandArgument{
	private String string;
	boolean followedBySpace;
	
	/**
	 * Constructor, accepting a string fragment and a boolean determining
	 * whether the fragment should be followed by a space.
	 * 
	 * @param string String fragment.
	 * @param followedBySpace boolean determining whether the fragment should be 
	 * followed by a space.
	 */
	public StringCommandArgument(String string, boolean followedBySpace) {
		this.string = string;
		this.followedBySpace = followedBySpace;
	}
	
	/**
	 * @return The string framgent.
	 */
	public String getString() {
		return string;
	}

	/**
	 * Set the String fragment.
	 * @param string
	 */
	public void setString(String string) {
		this.string = string;
	}

	/**
	 * Return the String fragment.
	 */
	public String toConfigString() {
		return string;
	}
	
	/**
	 * Return the String fragment.
	 */
	public String toFormattedString() {
		return string;
	}
	
	/**
	 * @return boolean indicating whether the fragment should be followed by a 
	 * space.
	 */
	public boolean followedBySpace() {
		return followedBySpace;
	}
}