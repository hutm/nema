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

import org.imirsel.nema.model.NemaData;
import org.imirsel.nema.model.NemaDataConstants;
import org.imirsel.nema.model.util.DeliminatedTextFileUtilities;
import org.imirsel.nema.model.util.PathAndTagCleaner;

/**
 * Onset detection text file type.
 * 
 * @author afe405@gmail.com
 * @author kris.west@gmail.com
 * @since 0.1.0
 */
public class OnsetTextFile extends SingleTrackEvalFileTypeImpl {

	public static final String TYPE_NAME = "Onset text file";
	public static final String READ_DELIMITER = "\\s+";
	public static final String WRITE_DELIMITER = "\t";
	public static final DecimalFormat ONSET_DEC = new DecimalFormat("0.0000");
	
	/**
	 * Constructor
	 */
	public OnsetTextFile() {
		super(TYPE_NAME);
	}
	
	@Override
	public NemaData readFile(File theFile) throws IllegalArgumentException,
			FileNotFoundException, IOException {
		
		if (theFile.exists())
        {
            if(theFile.canRead()){
                BufferedReader textBuffer = null;
                ArrayList<String[]> rowData = new ArrayList<String[]>();
                String className = null;
            	String[] annotators = null;
                int maxRowLength = 0;
                try
                {
	                try
	                {
	                    textBuffer = new BufferedReader( new FileReader(theFile) );
	                }
	                catch(java.io.FileNotFoundException fnfe)
	                {
	                    throw new RuntimeException("The specified file does not exist, this exception should never be thrown and indicates a serious bug.\n\tFile: " + theFile.getPath());
	                }
	                String line = null; 
	                
                    //read data
                    line = textBuffer.readLine();
                    while (line != null)
                    {
                        if (!line.trim().equals("")){
                        	if (line.trim().startsWith("#")) {
                        		String headerRow = line.trim().replaceFirst("#", "");
                        		String[] headerRowArr = headerRow.split("=");
                        		if(headerRowArr.length == 2) {
                        			if(headerRowArr[0].trim().equalsIgnoreCase("class")) {
                        				className = headerRowArr[1].trim();
                        			}
                        			if(headerRowArr[0].equalsIgnoreCase("annotators")) {
                        				annotators = DeliminatedTextFileUtilities.parseDelimTextLine(headerRowArr[1].trim(),READ_DELIMITER);
                        			}
                        			
                        		}
                        		
                        	} else {
                        		String[] row = DeliminatedTextFileUtilities.parseDelimTextLine(line,READ_DELIMITER);
                                if (row != null){
                                	rowData.add(row);
                                	if (row.length > maxRowLength)
                                    {
                                        maxRowLength = row.length;
                                    }
                                }
                        		
                        	}

                        }
                        line = textBuffer.readLine();
                    }
                }
                catch (java.io.IOException ioe)
                {
                    throw new java.io.IOException("An IOException occured while reading file: " + theFile.getPath() + "\n" + ioe);
                }
                catch (java.lang.NullPointerException npe)
                {
                    throw new RuntimeException("NullPointerException caused by: " + theFile.getCanonicalPath(), npe);
                }
                finally{
                	if (textBuffer != null){
                		textBuffer.close();
                	}
                }
                
                double[][] outputData = new double[rowData.size()][maxRowLength];
                for (int i = 0; i < rowData.size(); i++) {
                    String[] row = (String[])rowData.get(i);
                    for (int j = 0; j < row.length; j++) {
                        outputData[i][j] = Double.valueOf(row[j].trim());
                    }
                }
                textBuffer.close();
                NemaData obj = new NemaData(PathAndTagCleaner.convertFileToMIREX_ID(theFile));
                obj.setMetadata(NemaDataConstants.ONSET_DETECTION_DATA, outputData);
                if (className != null) {
                	obj.setMetadata(NemaDataConstants.ONSET_DETECTION_CLASS, className);
                }
                if (annotators != null) {
                	obj.setMetadata(NemaDataConstants.ONSET_DETECTION_ANNOTATORS, annotators);                
                }
                return obj;
                
            }else{
                throw new IOException("The file: " + theFile.getPath() + " is not readable!");
            }
        }else{
            throw new FileNotFoundException("The file: " + theFile.getPath() + " was not found!");
        }
	}

	@Override
	public void writeFile(File theFile, NemaData data)
			throws IllegalArgumentException, FileNotFoundException, IOException {
		BufferedWriter output = null;
		double[][] onsetData = data.get2dDoubleArrayMetadata(NemaDataConstants.ONSET_DETECTION_DATA);
		int nrows = onsetData.length;
		int ncols = onsetData[0].length;
		String[][] theData = new String[nrows][ncols];
		
		String className = null;
		String[] annotators = null;
		if(data.hasMetadata(NemaDataConstants.ONSET_DETECTION_CLASS)) {
			className = data.getStringMetadata(NemaDataConstants.ONSET_DETECTION_CLASS);
		}
		if(data.hasMetadata(NemaDataConstants.ONSET_DETECTION_ANNOTATORS)) {
			annotators = data.getStringArrayMetadata(NemaDataConstants.ONSET_DETECTION_ANNOTATORS);
		}
		
        try {
            //use buffering
            output = new BufferedWriter( new FileWriter(theFile) );
            if(className != null) {
            	String line = "";
            	line = "#CLASS=" + className;
            	output.write( line );
                output.newLine();
            }
            if(annotators != null) {
            	String line = "#ANNOTATORS=";
            	for (int i = 0; i < annotators.length-1; i++) {
            		line += annotators[i] + WRITE_DELIMITER;
            	}
            	line += annotators[annotators.length-1];
            	output.write( line );
                output.newLine();
            }
            
            for (int i = 0; i < theData.length; i++) {
                String line = "";

                for (int j = 0; j < theData[i].length-1; j++) {
                	if(Double.isNaN(onsetData[i][j])) {
                		line += "NaN" + WRITE_DELIMITER;
                	} else {	
                		line += ONSET_DEC.format(onsetData[i][j]) + WRITE_DELIMITER;
                	}
                }
                if(Double.isNaN(onsetData[i][onsetData[i].length-1])) {
            		line += "NaN";
            	} else {	
            		line += ONSET_DEC.format(onsetData[i][onsetData[i].length-1]);
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

}
