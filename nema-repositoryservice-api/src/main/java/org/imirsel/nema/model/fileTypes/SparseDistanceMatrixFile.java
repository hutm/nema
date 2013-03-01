package org.imirsel.nema.model.fileTypes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.imirsel.nema.model.NemaData;
import org.imirsel.nema.model.NemaDataConstants;
import org.imirsel.nema.model.NemaTrackDistance;
import org.imirsel.nema.model.util.PathAndTagCleaner;

public class SparseDistanceMatrixFile extends MultipleTrackEvalFileTypeImpl implements
		NemaFileType {

	public static final String MAJOR_READ_DELIMITER = "\\s+";
	public static final String MAJOR_WRITE_DELIMITER = "\t";	
	public static final String MINOR_READ_DELIMITER = ",";
	public static final String MINOR_WRITE_DELIMITER = ",";	
	
	public static final String TYPE_NAME = "Sparse Distance Matrix File";
	
	public static final String DEFAULT_DIST_MAT_NAME = "Sparse Distance matrix";
	
	public SparseDistanceMatrixFile() {
		super(TYPE_NAME);
	}
	
	@Override
	public List<NemaData> readFile(File theFile)
			throws IllegalArgumentException, FileNotFoundException, IOException {
		
		List<NemaData> queries = null;
		BufferedReader textBuffer = new BufferedReader(new FileReader(theFile));
        int lineNum = 0;
        String matrixName = null;
        String line;
        try{
        	//read matrix name
        	matrixName = textBuffer.readLine();
        	lineNum++;
        	
        	//read query results
        	queries = new ArrayList<NemaData>();
            line = textBuffer.readLine();
            lineNum++;
            String[] comps;
            String[] itemComps;
            while (line != null)
            {
                comps = line.split(MAJOR_READ_DELIMITER);
                
                //get query track ID
                String query = PathAndTagCleaner.convertFileToMIREX_ID(new File(comps[0]));
                ArrayList<NemaTrackDistance> distances = new ArrayList<NemaTrackDistance>();
                
                for (int i = 1; i < comps.length; i++){
                    itemComps = comps[i].split(MINOR_READ_DELIMITER);
                    if (itemComps.length != 2){
                        String msg = "Error: could not interpret line '";
                        if(line.length() > 50){
                            msg += line.substring(0,50) + "...";
                        }else{
                            msg += line;
                        }
                        msg += "'as a 'file,distance' pair, problem component was '" + comps[i] + "'";
                        throw new IllegalArgumentException(msg);
                    }else{
                    	String resultId = PathAndTagCleaner.convertFileToMIREX_ID(new File(itemComps[0]));
                    	try{
	                        distances.add(new NemaTrackDistance(resultId, (float)Double.parseDouble(itemComps[1])));
	                    }catch(NumberFormatException nfe){
	                        if (itemComps[1].equalsIgnoreCase("inf")){
	                        	getLogger().warning("Infinite distance score '" + itemComps[1] + "' on line " + lineNum + " of file: " + theFile.getAbsolutePath());
	                        }else{
	                        	getLogger().warning("Failed to interpret '" + itemComps[1] + "' as a distance score on line " + lineNum + " of file: " + theFile.getAbsolutePath());
	                        }
	                        distances.add(new NemaTrackDistance(resultId, Float.POSITIVE_INFINITY));
	                    }
    	                
    	                Collections.sort(distances);
    	                
    	                //create NemaData Object for query track
    	                NemaData queryResults = new NemaData(query);
    	                distances.trimToSize();
    	                queryResults.setMetadata(NemaDataConstants.SEARCH_TRACK_DISTANCE_LIST, distances);
    	                queryResults.setMetadata(NemaDataConstants.SEARCH_DISTANCE_MATRIX_NAME, matrixName);
    	                queries.add(queryResults);
                    }

                }
                
                line = textBuffer.readLine();
                lineNum++;
            }
            
        }finally{
        	if(textBuffer != null){
        		textBuffer.close();
        	}
        }
		
        getLogger().info(queries.size() + " queries with track distances metadata read from sparse distance matrix file: " + theFile.getAbsolutePath());
        
        return queries;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void writeFile(File theFile, List<NemaData> data)
			throws IllegalArgumentException, FileNotFoundException, IOException {
		
		BufferedWriter output = null;
		try{
			//use buffering
            output = new BufferedWriter( new FileWriter(theFile,false) );
            
            //get dist mat name
            Object nameObj = data.get(0).getMetadata(NemaDataConstants.SEARCH_DISTANCE_MATRIX_NAME);
            String name = DEFAULT_DIST_MAT_NAME;
            if (nameObj == null){
            	getLogger().warning("Distance matrix name not found in first result Object! Using default name.");
            }else{
            	name = (String)nameObj;
            }
            
            //Write name
            output.write( name );
            output.newLine();
           
            //Write sparse data matrix
            NemaTrackDistance dist;
            for (Iterator<NemaData> it = data.iterator();it.hasNext();) {
                NemaData query = it.next();
                String line = "" + query.getId();
                
                //get distances
                Object distsObj = query.getMetadata(NemaDataConstants.SEARCH_TRACK_DISTANCE_LIST);
                if (distsObj == null){
                	throw new IllegalArgumentException("No distances found in " + query);
                }
                List<NemaTrackDistance> dists = (List<NemaTrackDistance>)distsObj;
                Collections.sort(dists);
                
                //write out matrix line
                for (Iterator<NemaTrackDistance> resultIt = dists.iterator(); resultIt.hasNext();)
                {
                	dist = resultIt.next();
                	line += MAJOR_WRITE_DELIMITER + dist.getTrackId() + MINOR_WRITE_DELIMITER + dist.getDistance();
                }
                output.write( line );
                output.newLine();
            }
            
			getLogger().info(data.size() + " queries with track distances metadata written to file: " + theFile.getAbsolutePath());
			
		} finally {
			if (output != null) {
				try {
					output.flush();
					output.close();
				} catch (IOException ex) {
					getLogger().log(Level.SEVERE, null, ex);
				}
			}
		}
	}
}
