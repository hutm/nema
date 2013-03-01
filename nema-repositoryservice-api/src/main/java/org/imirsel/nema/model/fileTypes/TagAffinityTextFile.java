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
 * Reads and writes Tag Affinity files which encode affinities for multiple 
 * tracks to multiple tags per file.
 * 
 * @author kris.west@gmail.com
 * @since 0.4.0
 */
public class TagAffinityTextFile extends MultipleTrackEvalFileTypeImpl {

    public static final String READ_DELIMITER = "\\s+";
	public static final String WRITE_DELIMITER = "\t";	
	public static final String TYPE_NAME = "Tag affinity text file";
	
	public TagAffinityTextFile() {
		super(TYPE_NAME);
	}
	
	@Override
	public List<NemaData> readFile(File theFile)
			throws IllegalArgumentException, FileNotFoundException, IOException {
		
		HashMap<String,HashMap<String,Double>> pathsToAffinityMap = new HashMap<String, HashMap<String,Double>>();
        HashSet<String> allTags = new HashSet<String>();
        double minAffinity = Double.POSITIVE_INFINITY;
        double maxAffinity = Double.NEGATIVE_INFINITY;
        
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
            Double value = Double.valueOf(tagData[r][2].trim());
            if((value != Double.NaN)&&(value != Double.NEGATIVE_INFINITY)&&(value != Double.POSITIVE_INFINITY)){
                if (value > maxAffinity){
                    maxAffinity = value;
                }else if(value < minAffinity){
                    minAffinity = value;
                }
            }
            
            HashMap<String,Double> tagMap = pathsToAffinityMap.get(aPath);
            if (tagMap == null){
                tagMap = new HashMap<String,Double>();
                pathsToAffinityMap.put(aPath, tagMap);
            }
            tagMap.put(tagName,value);
            
        }
        getLogger().fine("Read tag affinity sets for " + pathsToAffinityMap.size() + " paths from " + theFile.getAbsolutePath());
        
        //normalise affinity values
        double affRange = maxAffinity - minAffinity;
        getLogger().fine("Normalising affinity scores:\n" +
        		"\tMin affinity: " + minAffinity + "\n" +
        		"\tMax affinity: " + maxAffinity + "\n" +
        		"\tRange:        " + affRange);
        String aTag;
        double normval;

        for (Iterator<HashMap<String,Double>> it = pathsToAffinityMap.values().iterator(); it.hasNext();){
            HashMap<String,Double> aMap = it.next();
            for (Iterator<String> it1 = aMap.keySet().iterator(); it1.hasNext();){
                aTag = it1.next();
                normval = (aMap.get(aTag) - minAffinity) / affRange;
                aMap.put(aTag, normval);
            }
        }
        
		List<NemaData> examples = new ArrayList<NemaData>(pathsToAffinityMap.size());
        NemaData obj;
        File path;
        String trackID;
        for (Iterator<String> it = pathsToAffinityMap.keySet().iterator();it.hasNext();) {
            String pathStr = it.next();
        	path = new File(pathStr);
            trackID = PathAndTagCleaner.convertFileToMIREX_ID(path);
            obj = new NemaData(trackID);
            obj.setMetadata(NemaDataConstants.TAG_AFFINITY_MAP, pathsToAffinityMap.get(pathStr));
            obj.setMetadata(NemaDataConstants.TAG_EXPERIMENT_CLASSNAMES, allTags);
            obj.setMetadata(NemaDataConstants.PROP_FILE_LOCATION, path.getAbsolutePath());
            examples.add(obj);
        }
        
        getLogger().info(examples.size() + " examples with " + NemaDataConstants.TAG_AFFINITY_MAP + " metadata read from file: " + theFile.getAbsolutePath());
        
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
			String tag;
			HashMap<String,Double> tagAffinities = null;
			int noFileLocation = 0;
			for (Iterator<NemaData> it = data.iterator(); it.hasNext();) {
				obj = it.next();
				try {
					identifier = obj.getStringMetadata(NemaDataConstants.PROP_FILE_LOCATION);
				}catch(IllegalArgumentException e) {
					identifier = obj.getId();
					noFileLocation++;
				}
				tagAffinities = (HashMap<String,Double>)obj.getMetadata(NemaDataConstants.TAG_AFFINITY_MAP);
				//write a line per tag
				for (Iterator<String> tagIt = tagAffinities.keySet().iterator(); tagIt.hasNext();) {
					tag = tagIt.next();
					writer.write(identifier + WRITE_DELIMITER + tag + WRITE_DELIMITER + tagAffinities.get(tag));
					writer.newLine();
				}
			}
			
			if(noFileLocation == 0) {
				getLogger().info(data.size() + " examples with " + NemaDataConstants.TAG_AFFINITY_MAP + " metadata written to file: " + theFile.getAbsolutePath());
			}else {
				getLogger().warning(noFileLocation + " of " + data.size() + " examples (with " + NemaDataConstants.TAG_AFFINITY_MAP + " metadata) did not have file locations, hence they will have be written out with the trackID only.\nFile written: " + theFile.getAbsolutePath());
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
