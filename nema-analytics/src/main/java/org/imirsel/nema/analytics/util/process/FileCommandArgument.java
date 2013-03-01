/**
 * 
 */
package org.imirsel.nema.analytics.util.process;

import java.util.Iterator;
import java.util.Map;

import org.imirsel.nema.model.fileTypes.NemaFileType;

/**
 * Class representing an input or output file component of a command line calling
 * format for a binary executable. The Type, input/output number and any 
 * properties are encoded.
 * 
 * @author kris.west@gmail.com
 * @since 0.2.0
 */
class FileCommandArgument implements CommandArgument{
	private boolean isOutput;
	private String preparedPath;
	private Class<? extends NemaFileType> fileType;
	private Map<String,String> properties;
	boolean followedBySpace;
	int ioIndex;
	
	/**
	 * Constructor.
	 * 
	 * @param isOutput Flag indicating whether this is an input or output 
	 * argument.
	 * @param fileType Class representing the file type.
	 * @param properties Map containing key/value pairs representing the 
	 * properties.
	 * @param followedBySpace Flag indicating whether this argument should be 
	 * followed by a space.
	 * @param ioIndex The input/output index of the argument.
	 */
	public FileCommandArgument(boolean isOutput, 
			Class<? extends NemaFileType> fileType,
			Map<String,String> properties,
			boolean followedBySpace,
			int ioIndex) {
		this.isOutput = isOutput;
		this.fileType = fileType;
		this.properties = properties;
		this.followedBySpace = followedBySpace;
		this.ioIndex = ioIndex;
	}

	/**
	 * @return the map of property key/value pairs.
	 */
	public Map<String, String> getProperties() {
		return properties;
	}

	/**
	 * Set the map of property key/value pairs.
	 * @param properties
	 */
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	/**
	 * @return The input/output index of the argument.
	 */
	public int getIoIndex() {
		return ioIndex;
	}

	/**
	 * Set the input/output index of the argument.
	 * @param ioIndex
	 */
	public void setIoIndex(int ioIndex) {
		this.ioIndex = ioIndex;
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
	 * @return Class representing the file type.
	 */
	public Class<? extends NemaFileType> getFileType() {
		return fileType;
	}

	/**
	 * Set Class representing the file type.
	 * @param fileType
	 */
	public void setFileType(Class<? extends NemaFileType> fileType) {
		this.fileType = fileType;
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
	 * @return Flag indicating whether this is an input or output argument.
	 */
	public boolean isOutput() {
		return isOutput;
	}

	/**
	 * Set Flag indicating whether this is an input or output argument.
	 * @param isOutput
	 */
	public void setOutput(boolean isOutput) {
		this.isOutput = isOutput;
	}

	/**
	 * Formats the properties string part of the config string.
	 * @param map Properties map of key/value pairs.
	 * @return Formatted string.
	 */
	private static String producePropertiesString(Map<String,String> map) {
		String out = "";
		for (Iterator<String> iterator = map.keySet().iterator(); iterator
				.hasNext();) {
			String key = iterator.next();
			String val = map.get(key);
			out += key + "=" + val;
			if(iterator.hasNext()) {
				out += ",";
			}
		}
		return out;
	}
	
	/**
	 * Generates the config string representation of the argument.
	 * @return Config string representaiton of this argument.
	 */
	public String toConfigString() {
		String out = "$";
		if (isOutput) {
			out += "o";
		}else {
			out += "i";
		}
		out += ioIndex + "{";
		out += this.fileType.getName();
		if (this.properties != null) {
			out += "(" + producePropertiesString(this.properties) + ")";
		}
		out += "}";
		
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