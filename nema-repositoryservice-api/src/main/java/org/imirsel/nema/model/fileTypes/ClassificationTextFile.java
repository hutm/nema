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
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.imirsel.nema.model.*;
import org.imirsel.nema.model.util.DeliminatedTextFileUtilities;
import org.imirsel.nema.model.util.PathAndTagCleaner;


/**
 * Reads and writes Classification files giving class for multiple file paths
 * per file.
 * 
 * @author kris.west@gmail.com
 * @since 0.1.0
 */
public class ClassificationTextFile extends MultipleTrackEvalFileTypeImpl {

	public static final String READ_DELIMITER = "\\s+";
	public static final String WRITE_DELIMITER = "\t";	
	public static final String TYPE_NAME = "Classification text file";
	private String metadataType;
	
	public ClassificationTextFile() {
		super(TYPE_NAME);
		this.setMetadataType(NemaDataConstants.CLASSIFICATION_DUMMY);
	}
	
	public ClassificationTextFile(String metadataType) {
		super(TYPE_NAME);
		this.setMetadataType(metadataType);
	}
	
	@Override
	public List<NemaData> readFile(File theFile)
			throws IllegalArgumentException, FileNotFoundException, IOException {
		
		HashMap<String,String> data = readClassificationFile(theFile);
		
		List<NemaData> examples = new ArrayList<NemaData>(data.size());
        NemaData obj;
        File path;
        String trackID;
        for (Iterator<String> it = data.keySet().iterator();it.hasNext();) {
            String pathStr = it.next();
        	path = new File(pathStr);
            trackID = PathAndTagCleaner.convertFileToMIREX_ID(path);
            obj = new NemaData(trackID);
            obj.setMetadata(getMetadataType(), PathAndTagCleaner.cleanTag(data.get(pathStr)));
            obj.setMetadata(NemaDataConstants.PROP_FILE_LOCATION, path.getAbsolutePath());
            examples.add(obj);
        }
        
        getLogger().info(examples.size() + " examples with " + getMetadataType() + " metadata read from file: " + theFile.getAbsolutePath());
        
        return examples;
	}
	
	@Override
	public void writeFile(File theFile, List<NemaData> data)
			throws IllegalArgumentException, FileNotFoundException, IOException {
		
		BufferedWriter writer = null;
		try{
			writer = new BufferedWriter(new FileWriter(theFile));
			NemaData obj;
			String identifier;
			int noFileLocation = 0;
			for (Iterator<NemaData> it = data.iterator(); it.hasNext();) {
				obj = it.next();
				try {
					identifier = obj.getStringMetadata(NemaDataConstants.PROP_FILE_LOCATION);
				}catch(IllegalArgumentException e) {
					identifier = obj.getId();
					noFileLocation++;
				}
				writer.write(identifier + WRITE_DELIMITER + PathAndTagCleaner.cleanTag(obj.getStringMetadata(getMetadataType())));
				writer.newLine();
			}
			
			if(noFileLocation == 0) {
				getLogger().info(data.size() + " examples with " + getMetadataType() + " metadata written to file: " + theFile.getAbsolutePath());
			}else {
				getLogger().warning(noFileLocation + " of " + data.size() + " examples (with " + getMetadataType() + " metadata) did not have file locations, hence they will have be written out with the trackID only.\nFile written: " + theFile.getAbsolutePath());
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

	//-----------
	/**
	 * Reads a classification file and encodes the data in a map.
	 * 
	 * @param toRead The file to read.
	 * @return A map linking file path/track name component to its classification.
	 * @throws IllegalArgumentException Thrown if the file is not in the expected format.
	 * @throws FileNotFoundException Thrown if the specified file is not found.
	 * @throws IOException Thrown if there is a problem reading the file unrelated to the format.
	 */
    public HashMap<String,String> readClassificationFile(File toRead) throws IllegalArgumentException, FileNotFoundException, IOException{
        HashMap<String,String> dataRead = new HashMap<String,String>();

        String[][] classificationStringsData = DeliminatedTextFileUtilities.loadDelimTextData(toRead, READ_DELIMITER, -1);

		int nrows = classificationStringsData.length;
        
        for(int r = 0; r < nrows; r++) {
            File aPath = new File(classificationStringsData[r][0].trim());
            if(aPath.getPath().equals("")){
                throw new IllegalArgumentException("Error: an empty track name was read from file: " + toRead.getAbsolutePath());
            }
            String className = PathAndTagCleaner.cleanTag(classificationStringsData[r][1].trim());
            if(className.equals("")){
                throw new IllegalArgumentException("Error: an empty class name was read from file: " + toRead.getAbsolutePath());
            }
            dataRead.put(aPath.getAbsolutePath(), className);
        }
        getLogger().info("Read " + dataRead.size() + " paths and classifications from " + toRead.getAbsolutePath());
        return dataRead;
    }

//    /**
//     * Reads the file list data from a classification file. The class data is ignored.
//     * 
//     * @param toRead The file to read.
//     * @param MIREXMode A flag that determines whether file paths are converted to just their 
//	 * names, minus the extension.
//	 * @return A list of the file path/track name components
//	 * @throws IllegalArgumentException Thrown if the file is not in the expected format.
//	 * @throws FileNotFoundException Thrown if the specified file is not found.
//	 * @throws IOException Thrown if there is a problem reading the file unrelated to the format.
//     */
//    public List<String> readClassificationFileAsList(File toRead) throws IllegalArgumentException, FileNotFoundException, IOException{
//        List<String> dataRead = new ArrayList<String>();
//
//        String[][] classificationStringsData = DeliminatedTextFileUtilities.loadDelimTextData(toRead, READ_DELIMITER, -1);
//
//		int nrows = classificationStringsData.length;
//        
//        for(int r = 0; r < nrows; r++) {
//            File aPath = new File(classificationStringsData[r][0].trim());
//            String key = PathAndTagCleaner.convertFileToMIREX_ID(aPath);
//            if(key.equals("")){
//                throw new IllegalArgumentException("Error: an empty track name was read from file: " + toRead.getAbsolutePath());
//            }
//            dataRead.add(key);
//        }
//
//        getLogger().info("Read " + dataRead.size() + " paths from " + toRead.getAbsolutePath());
//        return dataRead;
//    }

	public void setMetadataType(String metadataType) {
		this.metadataType = metadataType;
	}

	/** Returns the classification metadata type that this instance is 
	 * configured for.
	 * @return Metadata type key.
	 */
	public String getMetadataType() {
		return metadataType;
	}

}
