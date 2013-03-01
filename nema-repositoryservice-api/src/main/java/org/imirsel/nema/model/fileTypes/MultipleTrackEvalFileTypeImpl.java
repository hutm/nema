package org.imirsel.nema.model.fileTypes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.imirsel.nema.model.NemaData;

/**
 * 
 * @author kris.west@gmail.com
 * @since 0.1.0
 */
public abstract class MultipleTrackEvalFileTypeImpl extends NemaFileTypeImpl implements MultipleTrackEvalFileType {

	public MultipleTrackEvalFileTypeImpl(String typeName) {
		super(typeName);
	}
	

    public List<List<NemaData>> readDirectory(File theDir, String extension)
			throws IllegalArgumentException, FileNotFoundException, IOException{
		
    	List<List<NemaData>> out = new ArrayList<List<NemaData>>();
		
		File[] files = theDir.listFiles();
		ArrayList<File> filesToUse = new ArrayList<File>();
		
		getLogger().info("got " + files.length + " files for " + theDir.getAbsolutePath());
		
		if(extension == null){
			for (int i = 0; i < files.length; i++){
		        File file = files[i];
		        if (!file.isDirectory()){
		            filesToUse.add(file);
		        }
		    }
		}else{
			for (int i = 0; i < files.length; i++){
		        File file = files[i];
		        if (!file.isDirectory() && file.getName().endsWith(extension)){
		            filesToUse.add(file);
		        }
		    }
		}
		//this should sort results consistently across all submissions,
		//   if they use the same names for their results files 
		//   (otherwise there is no way to know if they are about the same test across different submissions)
		Collections.sort(filesToUse);
		
		for(Iterator<File> it = filesToUse.iterator();it.hasNext();){
			File toRead = it.next();
			getLogger().info("Reading file: " + toRead.getAbsolutePath());
			out.add(readFile(toRead));
		}
		
		getLogger().info("Retrieved " + out.size() + " of " + files.length + " files from " + theDir.getAbsolutePath());
		
		
		return out;
	}
	
	public abstract List<NemaData> readFile(File theFile)
		throws IllegalArgumentException, FileNotFoundException, IOException;
	
	public abstract void writeFile(File theFile, List<NemaData> data)
		throws IllegalArgumentException, FileNotFoundException, IOException;

}
