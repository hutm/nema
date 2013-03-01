/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.imirsel.nema.model.fileTypes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.imirsel.nema.model.*;
import org.imirsel.nema.model.util.DeliminatedTextFileUtilities;
import org.imirsel.nema.model.util.PathAndTagCleaner;


/**
 * Reads and writes Tag Classification files which encode multiple tags for 
 * multiple track paths per file.
 * 
 * @author kris.west@gmail.com
 * @since 0.4.0
 */
public class TagClassificationTextFile extends MultipleTrackEvalFileTypeImpl {

	public static final String READ_DELIMITER = "\\s+";
	public static final String WRITE_DELIMITER = "\t";	
	public static final String TYPE_NAME = "Tag classification text file";
	
	public TagClassificationTextFile() {
		super(TYPE_NAME);
	}
	
	@Override
	public List<NemaData> readFile(File theFile)
			throws IllegalArgumentException, FileNotFoundException, IOException {
		
		HashMap<String,HashSet<String>> pathsToRelevantTags = new HashMap<String,HashSet<String>>();
        HashSet<String> allTags = new HashSet<String>();
        
        String[][] tagData = DeliminatedTextFileUtilities.loadDelimTextData(theFile, READ_DELIMITER, -1);

		int nrows = tagData.length;
        
        for(int r = 0; r < nrows; r++) {
        	String aPath = tagData[r][0].trim();
            if(aPath.equals("")){
                throw new IllegalArgumentException("Error: an empty track name was read from file: " + theFile.getAbsolutePath());
            }
            String tagName = PathAndTagCleaner.cleanTag(tagData[r][1].trim());
            if(tagName.equals("")){
                throw new IllegalArgumentException("Error: an empty class name was read from file: " + theFile.getAbsolutePath());
            }
            allTags.add(tagName);
            
            //if three col format, ignore anything marked with a 0
            if (tagData[r].length > 2 && tagData[r][2] != null){
            	if (tagData[r][2].trim().equals("0")){
            		continue;
            	}
            }
            HashSet<String> tagSet = pathsToRelevantTags.get(aPath);
            if (tagSet == null){
                tagSet = new HashSet<String>();
                pathsToRelevantTags.put(aPath, tagSet);
            }
            tagSet.add(tagName);
            
        }
        getLogger().info("Read tag classification sets for " + pathsToRelevantTags.size() + " paths from " + theFile.getAbsolutePath());
        
		List<NemaData> examples = new ArrayList<NemaData>(pathsToRelevantTags.size());
        NemaData obj;
        File path;
        String trackID;
        for (Iterator<String> it = pathsToRelevantTags.keySet().iterator();it.hasNext();) {
            String pathStr = it.next();
        	path = new File(pathStr);
            trackID = PathAndTagCleaner.convertFileToMIREX_ID(path);
            obj = new NemaData(trackID);
            obj.setMetadata(NemaDataConstants.TAG_CLASSIFICATIONS, pathsToRelevantTags.get(pathStr));
            obj.setMetadata(NemaDataConstants.TAG_EXPERIMENT_CLASSNAMES, allTags);
            obj.setMetadata(NemaDataConstants.PROP_FILE_LOCATION, path.getAbsolutePath());
            examples.add(obj);
        }
        
        getLogger().info(examples.size() + " examples with " + NemaDataConstants.TAG_CLASSIFICATIONS + " metadata read from file: " + theFile.getAbsolutePath());
        
        return examples;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void writeFile(File theFile, List<NemaData> data)
			throws IllegalArgumentException, FileNotFoundException, IOException {
		
		BufferedWriter writer = null;
		try{
			writer = new BufferedWriter(new FileWriter(theFile));
			NemaData obj;
			String identifier;
			HashSet<String> tags = null;
			int noFileLocation = 0;
			for (Iterator<NemaData> it = data.iterator(); it.hasNext();) {
				obj = it.next();
				try {
					identifier = obj.getStringMetadata(NemaDataConstants.PROP_FILE_LOCATION);
				}catch(IllegalArgumentException e) {
					identifier = obj.getId();
					noFileLocation++;
				}
				tags = (HashSet<String>)obj.getMetadata(NemaDataConstants.TAG_CLASSIFICATIONS);
				//write a line per tag
				for (Iterator<String> tagIt = tags.iterator(); tagIt.hasNext();) {
					writer.write(identifier + WRITE_DELIMITER + tagIt.next());
					writer.newLine();
				}
			}
			
			if(noFileLocation == 0) {
				getLogger().info(data.size() + " examples with " + NemaDataConstants.TAG_CLASSIFICATIONS + " metadata written to file: " + theFile.getAbsolutePath());
			}else {
				getLogger().warning(noFileLocation + " of " + data.size() + " examples (with " + NemaDataConstants.TAG_CLASSIFICATIONS + " metadata) did not have file locations, hence they will have be written out with the trackID only.\nFile written: " + theFile.getAbsolutePath());
			}
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
