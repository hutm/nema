/**
 * 
 */
package org.imirsel.nema.analytics.util.process;

/**
 * Class representing a scratch directory file component of a command line calling
 * format for a binary executable. 
 * 
 * @author kris.west@gmail.com
 * @since 0.2.0
 */
class ScratchDirCommandArgument implements CommandArgument{
	private String preparedPath;
	boolean followedBySpace;
	
	/**
	 * Constructor.
	 * 
	 * @param followedBySpace Flag indicating whether this argument should be 
	 * followed by a space.
	 */
	public ScratchDirCommandArgument(boolean followedBySpace) {
		this.followedBySpace = followedBySpace;
	}

	/**
	 * Set flag indicating whether this argument should be followed by a space.
	 * @param followedBySpace
	 */
	public void setFollowedBySpace(boolean followedBySpace) {
		this.followedBySpace = followedBySpace;
	}

	/**
	 * @return Flag indicating whether this argument should be followed by a space.
	 */
	public boolean followedBySpace() {
		return followedBySpace;
	}
	
	/**
	 * @return the path that has been set for the file (if any).
	 */
	public String getPreparedPath() {
		return preparedPath;
	}

	/**
	 * Set a path to a prepared file in the specified format. Must be called
	 * before the formatted string can be generated.
	 * @param path
	 */
	public void setPreparedPath(String path) {
		preparedPath = path;
	}
	
	/**
	 * Clear the prepared file path.
	 */
	public void clearPreparedPath() {
		preparedPath = null;
	}
	
	/**
	 * Generates the config string representation of the argument.
	 * @return Config string representation of this argument.
	 */
	public String toConfigString() {
		String out = "$s";
		return out;
	}
	
	/**
	 * @return the formatted string representation of the argument, which is
	 * just the file path.
	 */
	public String toFormattedString() {
		if(preparedPath == null) {
			throw new IllegalArgumentException("No path prepared for InputFileComandComponent. A path must be set before the formatted String can be returned.");
		}
		return preparedPath;
	}
	
}