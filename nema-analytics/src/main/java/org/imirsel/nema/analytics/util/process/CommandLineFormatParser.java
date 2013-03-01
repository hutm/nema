package org.imirsel.nema.analytics.util.process;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.imirsel.nema.model.fileTypes.NemaFileType;

/**
 * Parses and produces command line formatting strings, an examle of which is:
 * {@code -v -x 1234 $i1\{org.imirsel.nema.analytics.util.io.TrackListTextFile(bitrate=96k,sample-rate=22050)\} -laln -o=$o1\{org.imirsel.nema.analytics.evaluation.classification.ClassificationTextFile\} asdioajds}
 * 
 * May also be used to format a command, which might take the form:
 * {@code -v -x 1234 /path/to/input/1.txt -laln -o=/path/to/output/1.txt asdioajds}
 * 
 * @author kris.west@gmail.com
 * @since 0.2.0
 */
public class CommandLineFormatParser {

	private List<CommandArgument> arguments = null;
	
	private Map<Integer,FileCommandArgument> inputs = null;
	private Map<Integer,FileCommandArgument> outputs = null;
	private ScratchDirCommandArgument scratchDir = null;
	
	/**
	 * Manual constructor accepting a list of {@code CommandArgument} Objects 
	 * defining the command line format. This is used to setup the parser model 
	 * which can then be used to generate the config string and sample formatted
	 * strings. 
	 * @param arguments a list of {@code CommandArgument} Objects.
	 */
	public CommandLineFormatParser(List<CommandArgument> arguments) {
		setArguments(arguments);
	}

	/**
	 * Parsing constructor that takes a config string of the form:
	 * {@code -v -x 1234 $i1\{org.imirsel.nema.analytics.util.io.TrackListTextFile(bitrate=96k,sample-rate=22050)\} -laln -o=$o1\{org.imirsel.nema.analytics.evaluation.classification.ClassificationTextFile\} asdioajds}
	 * parses it and constructs the parser model that can be used to format a
	 * command or regenerate the config string.
	 * @param commandFormatString The config string to parse.
	 * @throws IllegalArgumentException Thrown if the format can't be parsed.
	 */
	public CommandLineFormatParser(String commandFormatString) throws
			IllegalArgumentException{
		this.arguments = new ArrayList<CommandArgument>();
		
		this.inputs = new HashMap<Integer,FileCommandArgument>();
		this.outputs = new HashMap<Integer,FileCommandArgument>();
		scratchDir = null;
		
		int idx = 0;
		int lastIdx = 0;
		
		while(idx<commandFormatString.length()) {
			if (commandFormatString.charAt(idx) == ' ') {
				//ending a string component
				arguments.add(new StringCommandArgument(commandFormatString.substring(lastIdx, idx),true));
				idx++;
				lastIdx = idx;
			}else if(commandFormatString.charAt(idx) == '$') {
				//inside a file component
				//clear up any trailing strings
				if(idx != lastIdx) {
					arguments.add(new StringCommandArgument(commandFormatString.substring(lastIdx, idx),false));
				}
				idx++;
				
				//input or output?
				boolean isOutput = false;
				boolean isScratch = false;
//				if(commandFormatString.charAt(idx) == 'i') {
//					isOutput = false;
//				}else 
				if(commandFormatString.charAt(idx) == 'o') {
					isOutput = true;
				}else if(commandFormatString.charAt(idx) == 'i') {
					isOutput = false;
				}else if(commandFormatString.charAt(idx) == 's') {
					isScratch = true;
				}else {
					throw new IllegalArgumentException("Unable to determine whether File argument (" 
							+ commandFormatString.charAt(idx) + ") is input or output at position " 
							+ idx + " in: " + commandFormatString);
				}
				idx++;
				lastIdx = idx;
				
				if(isScratch){
					//check for trailing space
					boolean followedBySpace = false;
					if (idx < commandFormatString.length() && commandFormatString.charAt(idx) == ' ') {
						followedBySpace = true;
						idx++;
					}
					
					ScratchDirCommandArgument scratchComp = new ScratchDirCommandArgument(followedBySpace);
					arguments.add(scratchComp);
					scratchDir = scratchComp;
					
					lastIdx = idx;
				}else{
					//get index (search forward for { then parse)
					try {
						while(commandFormatString.charAt(idx) != '{'){
							idx++;
						}
					}catch(IndexOutOfBoundsException e) {
						throw new IllegalArgumentException("End of string reached while seeking type argument opening { in: " + commandFormatString);
					}
					
					int ioIndex;
					try {
						ioIndex = Integer.parseInt(commandFormatString.substring(lastIdx,idx));
					}catch (NumberFormatException e) {
						throw new IllegalArgumentException("Failed to parse IO index (" + commandFormatString.substring(lastIdx,idx) + ") in: " + commandFormatString,e);
					}
					idx++;
					lastIdx = idx;
					
					//get file format
					//search forward for close } or arguments (
					try {
						while(commandFormatString.charAt(idx) != '}' && commandFormatString.charAt(idx) != '('){
							idx++;
						}
					}catch(IndexOutOfBoundsException e) {
						throw new IllegalArgumentException("End of string reached while seeking type argument close } or properties opening ( in: " + commandFormatString);
					}
					String typeStr = commandFormatString.substring(lastIdx,idx);
					lastIdx = idx;
					Class<? extends NemaFileType> typeClass;
					try {
						typeClass = (Class<? extends NemaFileType>)Class.forName(typeStr);
					}catch(Exception e) {
						throw new IllegalArgumentException("Failed to interpret valid file type from '" + typeStr + "' in: " + commandFormatString,e);
					}
					
					//grab any properties
					Map<String,String> props = null;
					if (commandFormatString.charAt(idx) == '(') {
						idx++;
						lastIdx = idx;
						
						try {
							while(commandFormatString.charAt(idx) != ')'){
								idx++;
							}
						}catch(IndexOutOfBoundsException e) {
							throw new IllegalArgumentException("End of string reached while seeking properties closing ) in: " + commandFormatString);
						}
						String propertiesStr = commandFormatString.substring(lastIdx,idx);
						props = parsePropertiesString(propertiesStr);	
						idx++;
					}
					
					//last char should be }
					if (commandFormatString.charAt(idx) != '}') {
						throw new IllegalArgumentException("Expected closing } at position " + idx + " in: " + commandFormatString);
					}
					idx++;
					
					//check for trailing space
					boolean followedBySpace = false;
					if (idx < commandFormatString.length() && commandFormatString.charAt(idx) == ' ') {
						followedBySpace = true;
						idx++;
					}
					
					FileCommandArgument fileComp = new FileCommandArgument(isOutput, typeClass, props, followedBySpace, ioIndex);
					arguments.add(fileComp);
					if (isOutput) {
						outputs.put(ioIndex,fileComp);
					}else {
						inputs.put(ioIndex,fileComp);
					}
					
					lastIdx = idx;
				}
				
				
			}else {
				idx++;
			}
		}
		
		//ending a string component
		if (lastIdx != idx){
			arguments.add(new StringCommandArgument(commandFormatString.substring(lastIdx, idx),true));
			idx++;
			lastIdx = idx;
		}
	}
	
	/**
	 * Return the list of argument components.
	 * @return the list of argument components.
	 */
	public List<CommandArgument> getArguments() {
		return arguments;
	}

	/**
	 * Set the list of argument components.
	 * @param arguments the list of argument components to set.
	 */
	public void setArguments(List<CommandArgument> arguments) {
		this.arguments = arguments;
		for (Iterator<CommandArgument> iterator = arguments.iterator(); iterator.hasNext();) {
			CommandArgument commandArgument = iterator.next();
			if(commandArgument.getClass().equals(FileCommandArgument.class)){
				FileCommandArgument fileArg = (FileCommandArgument)commandArgument;
				if(fileArg.isOutput()){
					this.outputs.put(fileArg.ioIndex,fileArg);
				}else{
					this.inputs.put(fileArg.ioIndex,fileArg);
				}
			}else if(commandArgument.getClass().equals((ScratchDirCommandArgument.class))){
				this.scratchDir = (ScratchDirCommandArgument)commandArgument;
			}
		}
	}

	/**
	 * Parse a properties string component of a file type definition.
	 * @param propsString The string to parse
	 * @return A list of key value pairs representing the properties.
	 * @throws IllegalArgumentException Thrown if the string can't be parsed.
	 */
	public static Map<String,String> parsePropertiesString(String propsString) throws IllegalArgumentException{
		Map<String,String> map = new HashMap<String,String>();
		if (propsString.trim().equals("")) {
			return map;
		}
		
		String[] comps = propsString.split(",");
		for (int i = 0; i < comps.length; i++) {
			String[] keyValPair = comps[i].split("=");
			if(keyValPair.length != 2) {
				throw new IllegalArgumentException("Wrong number of arguments for properties component '" + comps[i] + "' of properties String: " + propsString);
			}
			map.put(keyValPair[0], keyValPair[1]);
		}
		return map;
	}
	
	/**
	 * (Re)generate the config string.
	 * @return the config string.
	 */
	public String toConfigString() {
		String out = "";
		for (Iterator<CommandArgument> iterator = arguments.iterator(); iterator
				.hasNext();) {
			CommandArgument comp = iterator.next();
			out += comp.toConfigString();
			if(comp.followedBySpace()) {
				out += " ";
			}
		}
		return out;
	}
	
	public void setPreparedPathForInput(int ioIndex, String path) {
		inputs.get(ioIndex).setPreparedPath(path);
	}
	
	public void setPreparedPathForOutput(int ioIndex, String path) {
		outputs.get(ioIndex).setPreparedPath(path);
	}
	
	public void setPreparedPathForScratchDir(String path){
		if (scratchDir != null){
			scratchDir.setPreparedPath(path);
		}
	}
	
	public void clearPreparedPaths() {
		for (Iterator<FileCommandArgument> iterator = inputs.values().iterator(); iterator.hasNext();) {
			iterator.next().clearPreparedPath();
		}
		for (Iterator<FileCommandArgument> iterator = outputs.values().iterator(); iterator.hasNext();) {
			iterator.next().clearPreparedPath();
		}
		if (scratchDir != null){
			scratchDir.clearPreparedPath();
		}
	}
	
	/**
	 * Generate a formatted string. Note that a file name for each input and 
	 * output must have already been set with:
	 * {@code setPreparedPathForInput(int, String)} or
	 * {@code setPreparedPathForOutput(int,String)}.
	 * 
	 * @return The formatted string.
	 * @throws IllegalArgumentException Thrown if a path hasn't been set fo one 
	 * of the input or output files.
	 */
	public String toFormattedString() throws IllegalArgumentException{
		String out = "";
		for (Iterator<CommandArgument> iterator = arguments.iterator(); iterator
				.hasNext();) {
			CommandArgument comp = iterator.next();
			out += comp.toFormattedString();
			if(comp.followedBySpace()) {
				out += " ";
			}
		}
		return out;
	}
		
	/**
	 * Return a map of the input index to the {@code FileCommandArgument} Object
	 * representing it.
	 * @return map of the input index to the {@code FileCommandArgument} Object
	 * representing it.
	 */
	public Map<Integer, FileCommandArgument> getInputs() {
		return inputs;
	}

	/**
	 * Return a map of the output index to the {@code FileCommandArgument} Object
	 * representing it.
	 * @return map of the output index to the {@code FileCommandArgument} Object
	 * representing it.
	 */
	public Map<Integer, FileCommandArgument> getOutputs() {
		return outputs;
	}

	/**
	 * Returns the type of the specified input.
	 * @param inputIdx The input index.
	 * @return Class Object representing the {@code NemaFileType} that should
	 * be used to write the input file.
	 */
	public Class<? extends NemaFileType> getInputType(int inputIdx){
		FileCommandArgument arg = inputs.get(inputIdx);
		if(arg == null) {
			return null;
		}
		return arg.getFileType();
	}

	/**
	 * Returns the type of the specified output.
	 * @param outputIdx The output index.
	 * @return Class Object representing the {@code NemaFileType} that should
	 * be used to read the output file.
	 */
	public Class<? extends NemaFileType> getOutputType(int outputIdx){
		FileCommandArgument arg = outputs.get(outputIdx);
		if(arg == null) {
			return null;
		}
		return arg.getFileType();
	}
	
	/**
	 * Returns any audio encoding properties of the input file. These are used 
	 * to specify encoding constraints on teh input files (e.g. only use mp3 
	 * files at sample rate 22050 Hz).
	 * 
	 * @param inputIdx The input index.
	 * @return Map with key value pairs defining the properties.
	 */
	public Map<String,String> getInputProperties(int inputIdx){
		FileCommandArgument arg = inputs.get(inputIdx);
		if(arg == null) {
			return null;
		}
		return arg.getProperties();
	}

	/**
	 * Returns any file type properties of the output file. These are may be 
	 * used to specify arguments to the file type, such notifying the 
	 * classification file type that it is reading a particular metadata type.
	 * 
	 * @param outputIdx The output index.
	 * @return Map with key value pairs defining the properties.
	 */
	public Map<String,String> getOutputProperties(int outputIdx){
		FileCommandArgument arg = outputs.get(outputIdx);
		if(arg == null) {
			return null;
		}
		return arg.getProperties();
	}
	
	public ScratchDirCommandArgument getScratchDirArgument(){
		return scratchDir;
	}
}
