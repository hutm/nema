package org.imirsel.nema.model.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.imirsel.nema.model.util.IOUtil;


/**
 * Provides simple services for reading and writing deliminated text files, such
 * as Comma Separated Variable (CSV) files.
 * @author kris.west@gmail.com
 */
public class DeliminatedTextFileUtilities {
    
    /** Creates a new instance of DeliminatedTextFileUtilities */
    public DeliminatedTextFileUtilities() {

    }

    public static final String USAGE = "args: \n" +
            "-b /path/to/dir/of/input/files extension deliminator(c|t|s) /path/to/output/dir outputDeliminator(c|t|s) [-s]\n" +
            "  OR\n" +
            "/path/input/file deliminator(c|t|s) /path/output/file outputDeliminator(c|t|s) [-s]\n" +
            "\n" +
            "The -s flag forces the use of speech marks in the output file";

    public static void main(String[] args){
        try{
            if (args[0].equalsIgnoreCase("-b")){
                File inDir = new File(args[1]);
                String extension = args[2];
                String delim = args[3];
                File outDir = new File(args[4]);
                String outDelim = args[5];
                boolean useSpeechMarks = false;
                if(args.length > 6){
                    if(args[6].equalsIgnoreCase("-s")){
                        useSpeechMarks = true;
                    }else{
                        System.out.println("unrecognised argument: " + args[6] + "\n");
                        System.out.println(USAGE);
                    }
                }

                List<File> files = IOUtil.getFilteredPathStrings(inDir, extension);
                for (Iterator<File> it = files.iterator(); it.hasNext();){
                    File file = it.next();
                    try{
                        normaliseCSVFileFormat(file, delim, new File(outDir.getAbsolutePath() + File.separator + file.getName()), outDelim, useSpeechMarks);
                    }catch(Exception e){
                        System.out.println("Exception occured while trying to normalise: " + file.getAbsolutePath());
                        System.out.println("Arguments received (minimum 6 required with -b flag): ");
                        for (int i = 0; i < args.length; i++){
                            System.out.println(i + ": " + args[i]);
                        }
                        System.out.println("---");
                        System.out.println("Usage:");
                        System.out.println(USAGE);

                        System.out.println("---");
                        System.out.println("Exception:");
                        throw new RuntimeException(e);
                    }
                }

                System.out.println("done " + files.size() + " files.");

            }else{
                File in = new File(args[0]);
                String delim = args[1];
                File out = new File(args[2]);
                String outDelim = args[3];
                boolean useSpeechMarks = false;
                if(args.length > 4){
                    if(args[4].equalsIgnoreCase("-s")){
                        useSpeechMarks = true;
                    }else{
                        System.out.println("unrecognised argument: " + args[4] + "\n");
                        System.out.println(USAGE);
                    }
                }

                try{
                    normaliseCSVFileFormat(in, delim, out, outDelim, useSpeechMarks);
                    System.out.println("done.");
                }catch(Exception e){
                    System.out.println("Exception occured while trying to normalise: " + in.getAbsolutePath());
                    System.out.println("Arguments received (minimum 4 required): ");
                    for (int i = 0; i < args.length; i++){
                        System.out.println(i + ": " + args[i]);
                    }
                    System.out.println("---");
                    System.out.println("Usage:");
                    System.out.println(USAGE);

                    System.out.println("---");
                    System.out.println("Exception:");
                    throw new RuntimeException(e);
                }
            }

        }catch(Exception e){
            System.out.println("Exception occurred. Arguments received (minimum 4 required): ");
            for (int i = 0; i < args.length; i++){
                System.out.println(i + ": " + args[i]);
            }
            System.out.println("---");
            System.out.println("Usage:");
            System.out.println(USAGE);

            System.out.println("---");
            System.out.println("Exception:");
            throw new RuntimeException(e);
        }
    }

    public static void normaliseCSVFileFormat(File inputFile, String delim, File outputFile, String outputDelim, boolean useSpeechMarks) throws IOException {
        String[][] data = loadDelimTextData(inputFile, delim, -1);
        writeStringDataToDelimTextFile(outputFile, outputDelim, data, useSpeechMarks);
    }

    /**
     * Writes the a 2D array of String data to a deliminated text file.
     * @param theData The 2D string array of data to write to file. Indexed [row][column].
     * @param delimTextFile The path and file name to write to.
     * @param delim The delimator to use - comma (,), space ( ) or tab (\t) are recommended.
     * @param useSpeechMarks Determines whether values are wrapped in speech marks.
     * @throws java.io.IOException Thrown if an a problem occurs while writing to the file.
     * @throws org.imirsel.m2k.util.noMetadataException Thrown if any of the fileLocations are not found.
     */
    public static void writeStringDataToDelimTextFile(File delimTextFile, String delim, String[][] theData, boolean useSpeechMarks) throws IOException
    {
        BufferedWriter output = null;
        try {
            //use buffering
            output = new BufferedWriter( new FileWriter(delimTextFile) );
            
            for (int i = 0; i < theData.length; i++) {
                String line = "";
                if (useSpeechMarks)
                {
                    for (int j = 0; j < theData[i].length-1; j++) {
                        line += "\"" + theData[i][j] + "\"" + delim;
                    }
                    line += "\"" + theData[i][theData[i].length-1] + "\"";
                }else{
                    for (int j = 0; j < theData[i].length-1; j++) {
                        line += theData[i][j] + delim;
                    }
                    line += theData[i][theData[i].length-1];
                }
                output.write( line );
                output.newLine();
            }
        } finally {
            //flush and close both "output" and its underlying FileWriter
            if (output != null){
                output.flush();
                output.close();
            }
                
        }
    }
    
    /**
     * Writes the a 2D array of String data to a deliminated text file.
     * @param theData The 2D string array of data to write to file. Indexed [row][column].
     * @param delimTextFile The path and file name to write to.
     * @param delim The delimator to use - comma (,), space ( ) or tab (\t) are recommended.
     * @throws java.io.IOException Thrown if an a problem occurs while writing to the file.
     * @throws org.imirsel.m2k.util.noMetadataException Thrown if any of the fileLocations are not found.
     */
    public static void writeStringDataToDelimTextFile(File delimTextFile, String delim, String[][] theData) throws IOException
    {
        writeStringDataToDelimTextFile(delimTextFile, delim, theData, true);
    }
    
    /**
     * Returns a 2D String array representation of the data block from the csv file.
     * @param csvFile The deliminated text file name and path.
     * @param delimiter The delimiter to use to read the file.
     * @param headerRow The number of the header row or -1 for no header row.
     * @throws java.io.IOException Thrown if there is a problem reading the deliminated text file.
     * @throws java.io.FileNotFoundException Thrown if the deliminated text file is not found.
     * @return a 2D String array representation of the data block from the csv file.
     */
    public static String[][] getDelimTextDataBlock(File csvFile, String delimiter, int headerRow) throws IOException, FileNotFoundException{
        String[][] csvData = loadDelimTextData(csvFile, delimiter, -1);
        if(headerRow == -1)
        {
            return csvData;
        }else{
            //Truncate remaining data
            String[][] truncCSVData = new String[csvData.length - (headerRow + 1)][];
            for (int i = 0; i < (csvData.length - (headerRow + 1)); i++) {
                truncCSVData[i] = csvData[i + (headerRow + 1)];
            }
            return truncCSVData;
        }
    }
    
    /**
     * Returns only the headers from the deliminated text file.
     * @param delimTextFile The deliminated text file name and path.
     * @param delimiter The delimiter to be used to read the text file.
     * @param headerRow The row number to be used as the header row.
     * @return the headers row from the deliminated text file.
     * @throws java.io.IOException Thrown if there was a problem reading the deliminated text file.
     * @throws java.io.FileNotFoundException Thrown if the deliminated text file was not found.
     */
    public static String[] loadDelimTextHeaders(File delimTextFile, String delimiter, int headerRow) throws IOException, FileNotFoundException, IllegalArgumentException{
        if (delimTextFile.exists())
        {
            if(delimTextFile.canRead()){
                BufferedReader textBuffer;
                try
                {
                    textBuffer = new BufferedReader( new FileReader(delimTextFile) );
                }
                catch(java.io.FileNotFoundException fnfe)
                {
                    throw new RuntimeException("The specified file does not exist, this exception should never be thrown and indicates a serious bug.\n\tFile: " + delimTextFile.getPath());
                }
                String line = null; 
                try
                {
                    //read data
                    int count = -1;
                    while(count < headerRow){
                        //skip to header row
                        line = textBuffer.readLine();
                        count++;
                    }
                    String[] headers = parseDelimTextLine(line,delimiter);
                    if(headers == null){
                    	throw new IllegalArgumentException("No content found on line " + headerRow + " (N.B. row indices are zero based)");
                    }
                    for (int i = 0; i < headers.length; i++) {
                        headers[i] = headers[i].trim();
                    }
                    textBuffer.close();
                    return headers;
                }
                catch (java.io.IOException ioe)
                {
                    textBuffer.close();
                    throw new java.io.IOException("An IOException occured while reading file: " + delimTextFile.getPath() + "\n" + ioe);
                }
                catch (java.lang.NullPointerException npe)
                {
                    textBuffer.close();
                    npe.printStackTrace();
                    throw new RuntimeException("NullPointerException caused by: " + delimTextFile.getCanonicalPath());
                }
            }else{
                throw new IOException("The file: " + delimTextFile.getPath() + " is not readable!");
            }
        }else{
            throw new FileNotFoundException("The file: " + delimTextFile.getPath() + " was not found!");
        }
    }
    
    /**
     * Loads the entire deliminated text file into a 2d String array.
     * @param csvFile The deliminated text file name and path.
     * @param delimiter The delimiter to be used to read the text file.
     * @param lines Number of lines to read or -1 for all of them.
     * @return the entire deliminated text file represented as a 2d String array.
     * @throws java.io.IOException Thrown if there was a problem reading the deliminated text file.
     * @throws java.io.FileNotFoundException Thrown if the deliminated text file was not found.
     */
    public static String[][] loadDelimTextData(File csvFile, String delimiter, int lines) throws IOException, FileNotFoundException{
        if (csvFile.exists())
        {
            if(csvFile.canRead()){
                BufferedReader textBuffer = null;
                ArrayList<String[]> rowData = new ArrayList<String[]>();
                int maxRowLength = 0;
                try
                {
	                try
	                {
	                    textBuffer = new BufferedReader( new FileReader(csvFile) );
	                }
	                catch(java.io.FileNotFoundException fnfe)
	                {
	                    throw new RuntimeException("The specified file does not exist, this exception should never be thrown and indicates a serious bug.\n\tFile: " + csvFile.getPath());
	                }
	                String line = null; 
	                
                    //read data
                    int count = 0;
                    line = textBuffer.readLine();
                    while ((line != null)&&((count < lines)|(lines == -1)))
                    {
                        if (!line.trim().equals("")){
                            String[] row = parseDelimTextLine(line,delimiter);
                            if (row != null){
                            	rowData.add(row);
	                            if (row.length > maxRowLength)
	                            {
	                                maxRowLength = row.length;
	                            }
                            }
                        }
                        line = textBuffer.readLine();
                        count++;
                    }
                }
                catch (java.io.IOException ioe)
                {
                    throw new java.io.IOException("An IOException occured while reading file: " + csvFile.getPath() + "\n" + ioe);
                }
                catch (java.lang.NullPointerException npe)
                {
                    throw new RuntimeException("NullPointerException caused by: " + csvFile.getCanonicalPath(), npe);
                }
                finally{
                	if (textBuffer != null){
                		textBuffer.close();
                	}
                }
                
                String[][] outputData = new String[rowData.size()][maxRowLength];
                for (int i = 0; i < rowData.size(); i++) {
                    String[] row = (String[])rowData.get(i);
                    for (int j = 0; j < row.length; j++) {
                        outputData[i][j] = row[j].trim();
                        //detect and remove speechmarks
                    }
                }
                textBuffer.close();
                return outputData;
            }else{
                throw new IOException("The file: " + csvFile.getPath() + " is not readable!");
            }
        }else{
            throw new FileNotFoundException("The file: " + csvFile.getPath() + " was not found!");
        }
    }
    
    
    /**
     * Returns a 2D String array representation of the data block from the csv file.
     * @param classPath The class path of the deliminated text file to be read.
     * @param delimiter The delimiter to use to read the file.
     * @param headerRow The number of the header row or -1 for no header row.
     * @throws java.io.IOException Thrown if there is a problem reading the deliminated text file.
     * @throws java.io.FileNotFoundException Thrown if the deliminated text file is not found.
     * @return a 2D String array representation of the data block from the csv file.
     */
    public static String[][] getDelimTextDataBlock(String classPath, String delimiter, int headerRow) throws IOException, FileNotFoundException{
        String[][] csvData = loadDelimTextData(classPath, delimiter, -1);
        //String[] headers;
        if(headerRow == -1)
        {
            //set headers
            //headers = new String[csvData.length]; 
            return csvData;
        }else{
            //headers = csvData[headerRow];
            //Truncate remaining data
            String[][] truncCSVData = new String[csvData.length - (headerRow + 1)][];
            for (int i = 0; i < (csvData.length - (headerRow + 1)); i++) {
                truncCSVData[i] = csvData[i + (headerRow + 1)];
            }
            return truncCSVData;
        }
    }
    
    /**
     * Returns only the headers from the deliminated text file.
     * @param classPath The class path of the deliminated text file to be read.
     * @param delimiter The delimiter to be used to read the text file.
     * @param headerRow The row number to be used as the header row.
     * @throws java.io.IOException Thrown if there is a problem reading the deliminated text file.
     * @return the headers row from the deliminated text file.
     */
    public static String[] loadDelimTextHeaders(String classPath, String delimiter, int headerRow) throws IOException {

        BufferedReader textBuffer; 
        
        InputStream iStream = ClassLoader.getSystemResourceAsStream(classPath);
        textBuffer= new BufferedReader(new InputStreamReader(iStream));
        
        String line = null; 
        try
        {
            //read data
            int count = -1;
            while(count < headerRow){
                //skip to header row
                line = textBuffer.readLine();
                count++;
            }
            String[] headers = parseDelimTextLine(line,delimiter);
            for (int i = 0; i < headers.length; i++) {
                headers[i] = headers[i].trim();
            }
            textBuffer.close();
            return headers;
        }
        catch (java.io.IOException ioe)
        {
            textBuffer.close();
            throw new java.io.IOException("An IOException occured while reading file: " + classPath + "\n" + ioe);
        }
    }
    
    /**
     * Loads the entire deliminated text file into a 2d String array.
     * @param classPath The class path of the deliminated text file to be read.
     * @param delimiter The delimiter to be used to read the text file.
     * @param lines Number of lines to read or -1 for all of them.
     * @throws java.io.IOException Thrown if there is a problem reading the deliminated text file.
     * @return the entire deliminated text file represented as a 2d String array.
     */
    public static String[][] loadDelimTextData(String classPath, String delimiter, int lines)  throws IOException{

        BufferedReader textBuffer = null;
        ArrayList<String[]> rowData = new ArrayList<String[]>();
        int maxRowLength = 0;
        try{
            InputStream iStream = DeliminatedTextFileUtilities.class.getResourceAsStream(classPath);
            textBuffer = new BufferedReader(new InputStreamReader(iStream));

            String line = null; 

            //read data
            int count = 0;
            line = textBuffer.readLine();
            while ((line != null)&&((count < lines)|(lines == -1)))
            {
                if (!line.trim().equals("")){
                    String[] row = parseDelimTextLine(line,delimiter);
                    rowData.add(row);
                    if (row.length > maxRowLength)
                    {
                        maxRowLength = row.length;
                    }
                }
                line = textBuffer.readLine();
                count++;
            }
        }
        catch (java.io.IOException ioe)
        {
        	if (textBuffer != null){
        		textBuffer.close();
        	}
            throw new java.io.IOException("An IOException occured while reading file: " + classPath + "\n" + ioe);
        }
        catch (java.lang.NullPointerException npe)
        {
        	if (textBuffer != null){
        		textBuffer.close();
        	}
            throw new IllegalArgumentException("NullPointerException caused by classpath: " + classPath, npe);
        }

        String[][] outputData = new String[rowData.size()][maxRowLength];
        for (int i = 0; i < rowData.size(); i++) {
            String[] row = (String[])rowData.get(i);
            for (int j = 0; j < row.length; j++) {
                outputData[i][j] = row[j].trim();
                //detect and remove speechmarks
            }
        }
        textBuffer.close();
        return outputData;
    }
    
    /**
     * Parse a single line of the deliminated text file, removing speechmarks if 
     * necessary.
     * 
     * @param line The line to parse.
     * @param delimiter The delimiter to be used to segment the line.
     * @return A String[] representing the data from the delimited line.
     * @throws IllegalArgumentException Thrown if an exception is casued by bad a
     * bad delimiter or line.
     */
    public static String[] parseDelimTextLine(String line, String delimiter) throws IllegalArgumentException{
    	if (line == null || line.trim().equals("")){
    		return null;
    	}
        ArrayList<String> output = new ArrayList<String>();
        String tmp = line.trim();
//        char delimChar = delimiter.charAt(0);
        
        //add boundary requirement to delimiter
        Pattern delimPattern = Pattern.compile("^" + delimiter);
        Matcher matcher = delimPattern.matcher(tmp);
        //set use Anchoring bounds 
        matcher.useAnchoringBounds(true);
        
        
        
        int end = tmp.length();
        int lastIdx = 0;
        int i = 0;
        try{
//        	System.out.println("'" + line + "'");
        	if (tmp.contains("\"")){
        		boolean encounteredSpeechMarks = false;
                boolean insideSpeechMarks = false;
        	
	            for (; i < end; i++){
//	            	System.out.println("char: " + line.charAt(i));
	            	if(tmp.charAt(i) == '\"'){
	            		encounteredSpeechMarks = true;
	                    insideSpeechMarks = !insideSpeechMarks;
//	                    System.out.println("encountered speechmark, insideSpeechMarks=" + insideSpeechMarks);
	            	}
	            	if(!insideSpeechMarks){
	            		matcher.region(i, end);
		            	
		                if (matcher.find()){
//		                	System.out.println("did find");
		                    if (encounteredSpeechMarks){
		                    	String comp = tmp.substring(lastIdx+1, i-1);
		                        output.add(comp);
//		                        System.out.println("added " + comp + " in speech mark block");
		                        encounteredSpeechMarks = false;
		                        lastIdx = matcher.end();
		                        i=lastIdx-1;
//		                        System.out.println("lastIdx set to " + lastIdx);
		                    }else{
		                    	String comp = tmp.substring(lastIdx, i);
		                        output.add(comp);
//		                        System.out.println("added " + comp + " in NO speech mark block");
		                        
		                        lastIdx = matcher.end();
		                        i=lastIdx-1;
//		                        System.out.println("lastIdx set to " + lastIdx);
		                    }
		                }else{
//		                	System.out.println("didn't find");
		                }
	            	}
	            }
	            if (lastIdx != i){
	                if (encounteredSpeechMarks){
	                    if (insideSpeechMarks){
	                        System.out.println("WARNING: DeliminatedTextFileUtilities.parseDelimTextLine(): unclosed quotes (\") encountered on line '" + line + "'\n" +
	                        		"lastIdx=" + lastIdx + "\ti=" + i );
	                        output.add(tmp.substring(lastIdx+1, i));
	                    }else{
	                        output.add(tmp.substring(lastIdx+1, i-1));
	                    }
	                }else{
	                    output.add(tmp.substring(lastIdx, i));
	                }
	            }
            
        	}else{
	        	
	            for (; i < end; i++){
	            	matcher.region(i, end);
	            	
	                if (matcher.find()){
	                    output.add(tmp.substring(lastIdx, i));
                        lastIdx = matcher.end();
                        i=lastIdx;
	                }
	            }
	            if (lastIdx != i){
                    output.add(tmp.substring(lastIdx, i));
	            }
        	}
            
        }catch(Exception e){
        	throw new IllegalArgumentException("Error parsing line:\n\t[" + tmp + "]\n" + 
        			"Was at index: " + i + "\n" +
					"lastIdx: " + lastIdx + "\n",e);
        }
                
        String[] outLine = output.toArray(new String[output.size()]);
        
        return outLine;
    }
    
}
