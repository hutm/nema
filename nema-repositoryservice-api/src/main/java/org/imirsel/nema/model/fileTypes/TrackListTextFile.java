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
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.imirsel.nema.model.*;
import org.imirsel.nema.model.util.DeliminatedTextFileUtilities;
import org.imirsel.nema.model.util.PathAndTagCleaner;

/**
 * Reads and writes list files giving multiple file paths per file.
 * 
 * @author kris.west@gmail.com
 * @since 0.2.0
 */
public class TrackListTextFile extends MultipleTrackEvalFileTypeImpl {

	public static final String READ_DELIMITER = "\\s+";
	public static final String TYPE_NAME = "Track list text file";
	public static final String FILENAME_EXTENSION = ".list.txt";
	
	public TrackListTextFile() {
		super(TYPE_NAME);
		this.setFilenameExtension(FILENAME_EXTENSION);
	}
	
	@Override
	public List<NemaData> readFile(File theFile)
			throws IllegalArgumentException, FileNotFoundException, IOException {
		
		List<File> data = new ArrayList<File>();

        String[][] classificationStringsData = DeliminatedTextFileUtilities.loadDelimTextData(theFile, READ_DELIMITER, -1);
        int nrows = classificationStringsData.length;
        for(int r = 0; r < nrows; r++) {
            File aPath = new File(classificationStringsData[r][0].trim());
            if(aPath.getPath().equals("")){
                throw new IllegalArgumentException("Error: an empty track name was read from file: " + theFile.getAbsolutePath());
            }
            data.add(aPath);
        }

		List<NemaData> examples = new ArrayList<NemaData>(data.size());
        NemaData obj;
        File path;
        String trackID;
        for (Iterator<File> it = data.iterator();it.hasNext();) {
            path = it.next();
            trackID = PathAndTagCleaner.convertFileToMIREX_ID(path);
            obj = new NemaData(trackID);
            obj.setMetadata(NemaDataConstants.PROP_FILE_LOCATION, path);
            examples.add(obj);
        }
        
        getLogger().info(examples.size() + " examples read from list file: " + theFile.getAbsolutePath());
        
        return examples;
	}
	
	@Override
	public void writeFile(File theFile, List<NemaData> data)
			throws IllegalArgumentException, FileNotFoundException, IOException {
		
		BufferedWriter writer = null;
		try{
			writer = new BufferedWriter(new FileWriter(theFile));
			NemaData obj;
			for (Iterator<NemaData> it = data.iterator(); it.hasNext();) {
				obj = it.next();
				writer.write(obj.getStringMetadata(NemaDataConstants.PROP_FILE_LOCATION));
				writer.newLine();
			}
			
			getLogger().info(data.size() + " examples written to list file: " + theFile.getAbsolutePath());
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
