package org.imirsel.nema.model.fileTypes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.imirsel.nema.model.NemaData;
import org.imirsel.nema.model.NemaSalamiSegment;
import org.imirsel.nema.model.NemaSegment;
import org.imirsel.nema.model.NemaDataConstants;
import org.imirsel.nema.model.util.IOUtil;
import org.imirsel.nema.model.util.PathAndTagCleaner;

public class SalamiStructureTextFile extends SingleTrackEvalFileTypeImpl {

	public static final String READ_DELIMITER = "\\s+";
	public static final String READ_DELIMITER2 = ",";
	public static final String WRITE_DELIMITER = "\t";
	public static final String WRITE_DELIMITER2 = ",";
	public static final DecimalFormat STRUCT_DEC = new DecimalFormat("0.000");
	public static final String TYPE_NAME = "SALAMI Structural segmentation text file";

	public static final int MACRO_STRUCT_INDEX = 0; 
	public static final int MICRO_STRUCT_INDEX = 1;
	public static final int LEAD_INSTR_STRUCT_INDEX = 2;
	public static final int NON_MUSIC_SILENCE_INDEX = 3;
	
	public static final String MACRO_REGEX = "[A-Z']+";
	public static final String MICRO_REGEX = "[a-z']+";
	
	
	
	/**
	 * Constructor
	 */
	public SalamiStructureTextFile() {
		super(TYPE_NAME);
	}
	

	@SuppressWarnings("deprecation")
	public static void main(String[] args){
		List<File> files = IOUtil.getFilteredPathStrings(new File(args[0]), ".txt");
		SalamiStructureTextFile reader = new SalamiStructureTextFile();
		for (Iterator<File> iterator = files.iterator(); iterator.hasNext();) {
			File file = iterator.next();
//			System.out.println("Reading file: " + file.getAbsolutePath());
			try {
				reader.readFile(file);
			} catch (Exception e) {
				throw new RuntimeException("Exiting with Exception",e);
			}
		}
		
		System.out.println("\nIssues summary:");
		System.out.println("\tFiles with issues:           " + totalIssues);
		System.out.println("\tNo macro labels:             " + noMacro);
		System.out.println("\tNo micro labels:             " + noMicro);
		System.out.println("\tNo instrument labels:        " + noInstrument);
		System.out.println("\tNo end label:                " + noEnd);
		System.out.println("\tEOF only (no syntax issues): " + EOFonly);
		System.out.println("\tContains equals '=' symbol:  " + equalsSymbol);
		System.out.println("\tFailed to parse:             " + parseFailed);
		
	}
	
	
	private static void endAllNonInstrumentSegments(double time, 
			double currentMacroOnset, String currentMacroLabel, String currentMacroFunctionLabel, 
			double currentMicroOnset, String currentMicroLabel, String currentMicroFunctionLabel, 
			double currentNonMusicOnset, String currentNonMusicLabel, String currentNonMusicFunctionLabel,
			ArrayList<NemaSalamiSegment> macro_segments,
			ArrayList<NemaSalamiSegment> micro_segments,
			ArrayList<NemaSalamiSegment> nonmusic_silence_segments, 
			String issues
		)
	{
		if(currentMacroLabel != null){
			macro_segments.add(new NemaSalamiSegment(currentMacroOnset, time, currentMacroLabel, currentMacroFunctionLabel));
			currentMacroLabel = null;
			currentMacroFunctionLabel = null;
		}
		if(currentMicroLabel != null){
			micro_segments.add(new NemaSalamiSegment(currentMicroOnset, time, currentMicroLabel, currentMicroFunctionLabel));
			currentMicroLabel = null;
			currentMicroFunctionLabel = null;
		}
		if(currentNonMusicLabel != null){
			nonmusic_silence_segments.add(new NemaSalamiSegment(currentNonMusicOnset, time, currentNonMusicLabel, null));
			currentNonMusicLabel = null;
			currentNonMusicFunctionLabel = null;
		}
	}
	
	private static void endAllInstrumentSegments(double time, 
			LinkedList<Double> openInstOnsets, LinkedList<String> openInstLabels, 
			ArrayList<NemaSalamiSegment> instrument_segments,
			String issues){
		for (int i = 0; i < openInstOnsets.size(); i++) {
			instrument_segments.add(new NemaSalamiSegment(openInstOnsets.get(i),time,openInstLabels.get(i),null));
			if (!issues.equals("")){
				issues += ", ";
			}
			issues += "Instrument '" + openInstLabels.get(i) + "' was not closed before end of track";
		}
		openInstLabels.clear();
		openInstOnsets.clear();
	}
	
	private static void endSpecificInstrumentSegment(double time, String instrument, 
			LinkedList<Double> openInstOnsets, LinkedList<String> openInstLabels, 
			ArrayList<NemaSalamiSegment> instrument_segments,
			String issues){
		boolean found = false;
		for (int i = 0; i < openInstLabels.size(); i++) {
			if(instrument.equalsIgnoreCase(openInstLabels.get(i))){
				instrument_segments.add(new NemaSalamiSegment(openInstOnsets.remove(i),time,openInstLabels.remove(i),null));
				found = true;
				break;
			}
		}
		
		if (!issues.equals("")){
			issues += ", ";
		}
		issues += "Instrument '" + instrument + "' not found, but was closed";
	}
	
	static int totalIssues = 0;
	static int noInstrument = 0;
	static int noMacro = 0;
	static int noMicro = 0;
	static int noEnd = 0;
	static int EOFonly = 0;
	static int parseFailed = 0;
	static int equalsSymbol = 0;
	
	@SuppressWarnings("unchecked")
	@Override
	public NemaData readFile(File theFile) throws IllegalArgumentException,
	FileNotFoundException, IOException {

		double onset = -1;
		double currentMacroOnset = -1, currentMicroOnset = -1, currentNonMusicOnset = -1;
		LinkedList<Double> openInstOnsets = new LinkedList<Double>();
		LinkedList<String> openInstLabels = new LinkedList<String>();
		String currentMacroLabel = null, currentMicroLabel = null, currentNonMusicLabel = null;
		String currentMacroFunctionLabel = null, currentMicroFunctionLabel = null, currentNonMusicFunctionLabel = null;
		String closingInstLabel = null;
		double closingInstOnset = -1;
		int end;
		String onsetStr, label;

		Pattern delimPattern = Pattern.compile(READ_DELIMITER);
		Matcher matcher;
        
		boolean ended = false;
		
		BufferedReader textBuffer = new BufferedReader( new FileReader(theFile) );
		try{
			ArrayList<NemaSalamiSegment> macro_segments = new ArrayList<NemaSalamiSegment>(10);
			ArrayList<NemaSalamiSegment> micro_segments = new ArrayList<NemaSalamiSegment>(40);
			ArrayList<NemaSalamiSegment> instrument_segments = new ArrayList<NemaSalamiSegment>(10);
			ArrayList<NemaSalamiSegment> nonmusic_silence_segments = new ArrayList<NemaSalamiSegment>(5);
			
			ArrayList<NemaSalamiSegment>[] lists = new ArrayList[]{
					macro_segments, micro_segments, instrument_segments, nonmusic_silence_segments};
			
	        String line = null; 
	    
	        //read data
	        int lineNum = 1;
	        line = textBuffer.readLine();
	        String allIssues = "";
	        boolean macroLevelFunctions = false;
	        boolean microLevelFunctions = false;
	        boolean containsEquals = false;
	        while (line != null && !ended)
	        {
//	        	System.out.println("line " + lineNum);
//	        	System.out.flush();
	        	//clip comments
	        	int idx = line.indexOf('#');
	        	if(idx != -1){
	        		line = line.substring(0,idx);
	        	}
	        	line = line.trim();
	        	
	        	if (!line.equals("")){
	        		matcher = delimPattern.matcher(line);
	        		end = line.length();
	        		
	        		String issues = "";
	        		
	        		//get onset string
	                matcher.region(0, end);
	                if (matcher.find()){
	                	onsetStr = line.substring(0,matcher.start());
	                	onset = Double.parseDouble(onsetStr);
	                }else{
	                	throw new IllegalArgumentException("Failed to parse line '" + line + "', onset string could not be found using regexp " + READ_DELIMITER + " as delimiter");
	                }
	                
	                //closing instruments
	                if (closingInstLabel != null){
	                	instrument_segments.add(new NemaSalamiSegment(closingInstOnset, onset, closingInstLabel, null));
	                	closingInstLabel = null;
	                }
	                
	                //get remainder of line as label
	                label = line.substring(matcher.end());
	                label = label.trim();
	                
	                String[] comps = label.split(READ_DELIMITER2);

	                boolean setNonmusic = false;
            		boolean setMacro = false;
            		boolean setMicro = false;
            		
	                for (int j = 0; j < comps.length; j++) {
	                	comps[j] = comps[j].trim();
	                	if(comps[j].startsWith("(")){
	                		//starting an instrument
	                		
	                		if(comps[j].endsWith(")")){
	                			//start and end
	                			String inst = comps[j].substring(1, comps[j].length()-1);
	                			closingInstLabel = inst;
	                			closingInstOnset = onset;
	                			
	                		}else{
	                			//just start
	                			String inst = comps[j].substring(1, comps[j].length());
	                			openInstLabels.add(inst);
	                			openInstOnsets.add(onset);
	                		}
	                	}else if(comps[j].endsWith(")")){
	                		//ending an instrument we didn't just start
	                		String inst = comps[j].substring(0, comps[j].length()-1);
	                		endSpecificInstrumentSegment(onset, inst, openInstOnsets, openInstLabels, instrument_segments, issues);
	                		
	                	}else if(comps[j].equalsIgnoreCase("end")){
	                		//end of track, close all segments
	                		endAllNonInstrumentSegments(onset, 
	                				currentMacroOnset, currentMacroLabel, currentMacroFunctionLabel, currentMicroOnset, currentMicroLabel, 
	                				currentMicroFunctionLabel, currentNonMusicOnset, currentNonMusicLabel, 
	                				currentNonMusicFunctionLabel, macro_segments, micro_segments,
	                				nonmusic_silence_segments, issues);
	                		endAllInstrumentSegments(onset, openInstOnsets, openInstLabels, instrument_segments,
	                				issues);
	                		ended = true;
	                	}else if(comps[j].equalsIgnoreCase("silence")){
	                		//silence
	                		endAllNonInstrumentSegments(onset, 
	                				currentMacroOnset, currentMacroLabel, currentMacroFunctionLabel, currentMicroOnset, currentMicroLabel, 
	                				currentMicroFunctionLabel, currentNonMusicOnset, currentNonMusicLabel, 
	                				currentNonMusicFunctionLabel, macro_segments, micro_segments,
	                				nonmusic_silence_segments, issues);
	                		currentNonMusicLabel = "silence";
	                		currentNonMusicOnset = onset;
	                		setNonmusic = true;
	                		
	                	}else if(comps[j].equalsIgnoreCase("Z")){
	                		//non music
	                		endAllNonInstrumentSegments(onset, 
	                				currentMacroOnset, currentMacroLabel, currentMacroFunctionLabel, currentMicroOnset, currentMicroLabel, 
	                				currentMicroFunctionLabel, currentNonMusicOnset, currentNonMusicLabel, 
	                				currentNonMusicFunctionLabel, macro_segments, micro_segments,
	                				nonmusic_silence_segments, issues);
	                		endAllInstrumentSegments(onset, openInstOnsets, openInstLabels, instrument_segments,
	                				issues);
	                		currentNonMusicLabel = comps[j];
	                		currentNonMusicOnset = onset;
	                		setNonmusic = true;
	                		
	                	}else if(comps[j].equalsIgnoreCase("=")){
	                		//shouldn't be here... 
	                		if (!issues.equals("")){
	                			issues += ", ";
	                		}
	                		if(!issues.contains("Found an '=' symbol")){
	                			issues += "Found an '=' symbol";
		                		containsEquals = true;
	                		}
	                	}else{
	                		//handle macros, micros and functions
	                		
	                		if(comps[j].matches(MACRO_REGEX)){
	                			if(currentMacroLabel != null){
	                				macro_segments.add(new NemaSalamiSegment(currentMacroOnset, onset, currentMacroLabel, currentMacroFunctionLabel));
	                				currentMacroLabel = null;
	                				currentMacroFunctionLabel = null;
	                			}
	                			if(currentNonMusicFunctionLabel != null){
	                				nonmusic_silence_segments.add(new NemaSalamiSegment(currentNonMusicOnset, onset, currentNonMusicLabel, currentNonMusicFunctionLabel));
	                				currentNonMusicLabel = null;
	                				currentNonMusicFunctionLabel = null;
	                			}
	                			currentMacroLabel = comps[j];
	                			currentMacroOnset = onset;
	                			setMacro = true;
	                		}else if(comps[j].matches(MICRO_REGEX)){
	                			if(currentMicroLabel != null){
	                				micro_segments.add(new NemaSalamiSegment(currentMicroOnset, onset, currentMicroLabel, currentMicroFunctionLabel));
	                				currentMicroLabel = null;
	                				currentMicroFunctionLabel = null;
	                			}
	                			if(currentNonMusicFunctionLabel != null){
	                				nonmusic_silence_segments.add(new NemaSalamiSegment(currentNonMusicOnset, onset, currentNonMusicLabel, currentNonMusicFunctionLabel));
	                				currentNonMusicLabel = null;
	                				currentNonMusicFunctionLabel = null;
	                			}
	                			currentMicroLabel = comps[j];
	                			currentMicroOnset = onset;
                				setMicro = true;
	                		}else{//is function
	                			if (setMacro){
	                				currentMacroFunctionLabel = comps[j];
	                				macroLevelFunctions = true;
	                				if(microLevelFunctions){
	                					if (!issues.equals("")){
	        	                			issues += ", ";
	        	                		}
	                					issues += "Mixed macro and micro levels for function labels";
	                				}
	                			}else if(setMicro){
	                				currentMicroFunctionLabel = comps[j];
	                				microLevelFunctions = true;
	                				if(macroLevelFunctions){
	                					if (!issues.equals("")){
	        	                			issues += ", ";
	        	                		}
	                					issues += "Mixed macro and micro levels for function labels";
	                				}
	                				
	                			}else if(setNonmusic){
	                				currentNonMusicFunctionLabel = comps[j];
	                			}else{
	                				if (!issues.equals("")){
	    	                			issues += ", ";
	    	                		}
	    	                		issues += "Didn't find a macro, micro or non-music label on line for function '" + comps[j] + "'";
	                			}
	                		}
	                	}
	                	
					}
	                
	                //do something with issues
	                if(!issues.equals("")){
	                	allIssues += lineNum + "\t" + issues + "\n";
	    	        }
	            }
	            line = textBuffer.readLine();
	            lineNum++;
	        }
	        
	        String issues = "";
	        //end of track, close all segments
    		endAllNonInstrumentSegments(onset, 
    				currentMacroOnset, currentMacroLabel, currentMacroFunctionLabel, currentMicroOnset, currentMicroLabel, 
    				currentMicroFunctionLabel, currentNonMusicOnset, currentNonMusicLabel, 
    				null, macro_segments, micro_segments,
    				nonmusic_silence_segments, issues);
    		endAllInstrumentSegments(onset, openInstOnsets, openInstLabels, instrument_segments,
    				issues);
    		
    		if (closingInstLabel != null){
    			if (!issues.equals("")){
        			issues += ", ";
        		}
        		issues = "Self closing instrument didn't get offset time as file ended without end label";
    		}
	        
	        if (!ended){
	        	if (!issues.equals("")){
        			issues += ", ";
        		}
        		issues += "No end label";
        		noEnd++;
    		}
	        
	        if(macro_segments.size() == 0){
	        	if (!issues.equals("")){
        			issues += ", ";
        		}
        		issues += "No macro annotations";
        		noMacro++;
	        }
	        
	        if(micro_segments.size() == 0){
	        	if (!issues.equals("")){
        			issues += ", ";
        		}
        		issues += "No micro annotations";
        		noMicro++;
	        }
	        
	        if(instrument_segments.size() == 0){
	        	if (!issues.equals("")){
        			issues += ", ";
        		}
        		issues += "No instrument annotations";
        		noInstrument++;
	        }
	        
	        if(containsEquals){
	        	equalsSymbol++;
	        }
	        
	        //do something with issues
	        if(!issues.equals("")){
	        	if(allIssues.equals("")){
	        		EOFonly++;
	        	}
	        	allIssues = "EOF\t" + issues + "\n";	
	        }
	        
	        
    		
	        if(!allIssues.equals("")){
	        	totalIssues++;
	        	File issueFile = new File(theFile.getAbsolutePath() + ".issues");
	        	IOUtil.writeBytesToFile(issueFile, issues.getBytes("UTF-8"));
	        	System.out.println("\n\nFound issues for file: " + theFile.getAbsolutePath() + "\n" + allIssues);
	        }
    		
	        for (int j = 0; j < lists.length; j++) {
				lists[j].trimToSize();
			}
	        
			/* Fill the NemaData object with the proper data and return it*/
			NemaData obj = new NemaData(PathAndTagCleaner.convertFileToMIREX_ID(theFile));
			obj.setMetadata(NemaDataConstants.SALAMI_STRUCTURE_SEGMENTATION_DATA, 
					lists
					);
			return obj;
		}catch(Exception e){
			String msg = "Parsing of " + theFile.getAbsolutePath() + " failed with exception:\n";
			msg += e.toString();
			System.out.println(msg);
			e.printStackTrace(System.out);
			parseFailed++;
			File issueFile = new File(theFile.getAbsolutePath() + ".issues");
        	IOUtil.writeBytesToFile(issueFile, msg.getBytes("UTF-8"));
        	return null;
		}finally{
			if(textBuffer != null){
				try {
					textBuffer.close();
				} catch (IOException ex) {
					getLogger().log(Level.SEVERE, null, ex);
				}
			}
		}
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public void writeFile(File theFile, NemaData data)
	throws IllegalArgumentException, FileNotFoundException, IOException {
		throw new UnsupportedOperationException("Not implemented yet");
//		BufferedWriter writer = null;
//
//		try{
//			List<NemaSegment> segments = null;
//			try{
//				Object obj = data.getMetadata(NemaDataConstants.STRUCTURE_SEGMENTATION_DATA);
//				segments = (List<NemaSegment>)obj;
//			}catch(Exception e){
//				throw new IllegalArgumentException("Failed to retrieve segments from: " + data.getId()); 
//			}
//			writer = new BufferedWriter(new FileWriter(theFile));
//
//			NemaSegment nemaSegment;
//			for (Iterator<NemaSegment> it = segments.iterator(); it.hasNext();) {
//				nemaSegment = it.next();
//				writer.write(nemaSegment.toString() + "\n");
//			}
//			getLogger().info(NemaDataConstants.STRUCTURE_SEGMENTATION_DATA + " metadata for " + data.getId() + " written to file: " + theFile.getAbsolutePath());
//		} finally {
//			if (writer != null) {
//				try {
//					writer.flush();
//					writer.close();
//				} catch (IOException ex) {
//					getLogger().log(Level.SEVERE, null, ex);
//				}
//			}
//		}
//
	}

}
