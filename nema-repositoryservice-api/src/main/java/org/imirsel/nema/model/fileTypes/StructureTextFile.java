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
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.imirsel.nema.model.NemaData;
import org.imirsel.nema.model.NemaSegment;
import org.imirsel.nema.model.NemaDataConstants;
import org.imirsel.nema.model.util.PathAndTagCleaner;

public class StructureTextFile extends SingleTrackEvalFileTypeImpl {

	public static final String READ_DELIMITER = "\\s+";
	public static final String WRITE_DELIMITER = "\t";
	public static final DecimalFormat STRUCT_DEC = new DecimalFormat("0.000");
	public static final String TYPE_NAME = "Structural segmentation text file";

	/**
	 * Constructor
	 */
	public StructureTextFile() {
		super(TYPE_NAME);
	}

	private NemaSegment parseStructureLine(String line) throws IllegalArgumentException{
		double onset, offset;
		String onsetStr, offsetStr, label;
		
		Pattern delimPattern = Pattern.compile(READ_DELIMITER);
        Matcher matcher = delimPattern.matcher(line);
        
        int i = 0;
        int end = line.length();
        //get onset string
        matcher.region(i, end);
        if (matcher.find()){
        	onsetStr = line.substring(i,matcher.start());
        }else{
        	throw new IllegalArgumentException("Failed to parse line '" + line + "', onset string could not be found using regexp " + READ_DELIMITER + " as delimiter");
        }
        i = matcher.end();
        
        //get offset string
        matcher.region(i, end);
        if (matcher.find()){
        	offsetStr = line.substring(i,matcher.start());
        }else{
        	throw new IllegalArgumentException("Failed to parse line '" + line + "', offset string could not be found using regexp " + READ_DELIMITER + " as delimiter");
            }
        
        //get remainder of line as label
        label = line.substring(matcher.end());
        
        onset = Double.parseDouble(onsetStr);
        offset = Double.parseDouble(offsetStr);
        
		return new NemaSegment(onset, offset, label);
	}
	
	@Override
	public NemaData readFile(File theFile) throws IllegalArgumentException,
	FileNotFoundException, IOException {

		BufferedReader textBuffer = new BufferedReader( new FileReader(theFile) );
		try{
			ArrayList<NemaSegment> segments = new ArrayList<NemaSegment>();
	
	        String line = null; 
	    
	        //read data
	        line = textBuffer.readLine();
	        while (line != null)
	        {
	        	line = line.trim();
	        	if (!line.equals("")){
	                segments.add(parseStructureLine(line));
	            }
	            line = textBuffer.readLine();
	        }
	        segments.trimToSize();
	        
			/* Fill the NemaData object with the proper data and return it*/
			NemaData obj = new NemaData(PathAndTagCleaner.convertFileToMIREX_ID(theFile));
			obj.setMetadata(NemaDataConstants.STRUCTURE_SEGMENTATION_DATA, segments);
			return obj;
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

		BufferedWriter writer = null;

		try{
			List<NemaSegment> segments = null;
			try{
				Object obj = data.getMetadata(NemaDataConstants.STRUCTURE_SEGMENTATION_DATA);
				segments = (List<NemaSegment>)obj;
			}catch(Exception e){
				throw new IllegalArgumentException("Failed to retrieve segments from: " + data.getId()); 
			}
			writer = new BufferedWriter(new FileWriter(theFile));

			NemaSegment nemaSegment;
			for (Iterator<NemaSegment> it = segments.iterator(); it.hasNext();) {
				nemaSegment = it.next();
				writer.write(nemaSegment.toString() + "\n");
			}
			getLogger().info(NemaDataConstants.STRUCTURE_SEGMENTATION_DATA + " metadata for " + data.getId() + " written to file: " + theFile.getAbsolutePath());
		} finally {
			if (writer != null) {
				try {
					writer.flush();
					writer.close();
				} catch (IOException ex) {
					getLogger().log(Level.SEVERE, null, ex);
				}
			}
		}

	}

}
