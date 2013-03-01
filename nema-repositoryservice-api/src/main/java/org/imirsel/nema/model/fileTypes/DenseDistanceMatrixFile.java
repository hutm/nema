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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.imirsel.nema.model.NemaData;
import org.imirsel.nema.model.NemaDataConstants;
import org.imirsel.nema.model.NemaTrack;
import org.imirsel.nema.model.NemaTrackDistance;
import org.imirsel.nema.model.util.PathAndTagCleaner;

public class DenseDistanceMatrixFile extends MultipleTrackEvalFileTypeImpl implements
		NemaFileType {

	public static final String READ_DELIMITER = "\\s+";
	public static final String WRITE_DELIMITER = "\t";	
	public static final String TYPE_NAME = "Dense Distance Matrix File";
	
	public static final String DEFAULT_DIST_MAT_NAME = "Dense Distance matrix";
	
	public DenseDistanceMatrixFile() {
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
        ArrayList<NemaTrack> tracks = new ArrayList<NemaTrack>();
        try{
        	//read matrix name
        	matrixName = textBuffer.readLine();
        	lineNum++;
        	
        	//read files
            line = textBuffer.readLine();
            lineNum++;
            while ((line != null) && !( (line.toLowerCase().startsWith("q/r")) || (line.toLowerCase().startsWith("q\\r")) ) )
            {
                String[] comps = line.split(READ_DELIMITER);
                
                //get track ID of each candidate
                tracks.add(new NemaTrack(PathAndTagCleaner.convertFileToMIREX_ID(new File(comps[1]))));
                
                line = textBuffer.readLine();
                lineNum++;
            }
            if (line == null)
            {
                //something went wrong
                throw new IllegalArgumentException("Unexpected end of file at line " + lineNum + ".\nDistance matrix file: " + theFile.getPath());
            }else if( !( (line.toLowerCase().startsWith("Q/R".toLowerCase())) || (line.toLowerCase().startsWith("Q\\R".toLowerCase())) ) )
            {
                //Again, something went wrong
                throw new IllegalArgumentException("The file is not in the expected format as the 'Q/R' token that denotes the start of the distances portion of the matrix file was not found at line " + lineNum + ".\nDistance matrix file: " + theFile.getPath());
            }
            
            //skip that line as its just a header row
            line = textBuffer.readLine();
            lineNum++;
            
            int numTracks = tracks.size();
            getLogger().info(numTracks + " track IDs listed in distance matrix file: " + theFile.getAbsolutePath());
            queries = new ArrayList<NemaData>(numTracks);
            
            //read distances for each query line
            try{
	            while (line != null) {
	                String[] comps = line.split(READ_DELIMITER);
	                if (comps.length < (numTracks + 1)){
	                    throw new IllegalArgumentException("Insufficient distances found on line " + lineNum + ", " + (comps.length-1) + " distances found, " + numTracks + " required.\nDistance matrix file: " + theFile.getPath());
	                }if (comps.length > (numTracks + 1))
	                {
	                    getLogger().warning((comps.length - (numTracks + 1)) + " additional tokens were found on line " + lineNum + " of file: " + theFile.getPath());
	                }
	                
	                int queryIdx = Integer.parseInt(comps[0]);
	                
	                List<NemaTrackDistance> distances = new ArrayList<NemaTrackDistance>(numTracks);
	                
	                for (int j = 0; j < numTracks; j++) {
	                    try{
	                        distances.add(new NemaTrackDistance(tracks.get(j), (float)Double.parseDouble(comps[j+1])));
	                    }catch(NumberFormatException nfe){
	                        if (comps[j+1].equalsIgnoreCase("inf")){
	                        	getLogger().warning("Infinite distance score '" + comps[j+1] + "' on line " + lineNum + " of file: " + theFile.getAbsolutePath());
	                        }else{
	                        	getLogger().warning("Failed to interpret '" + comps[j+1] + "' as a distance score on line " + lineNum + " of file: " + theFile.getAbsolutePath());
	                        }
	                        distances.add(new NemaTrackDistance(tracks.get(j), Float.POSITIVE_INFINITY));
	                    }
	                }
	                Collections.sort(distances);
	                
	                //create NemaData Object for query track
	                if(queryIdx > numTracks){
	                	throw new IllegalArgumentException("Query index on line " + lineNum + " (" + queryIdx + ") is greater than the number of tracks read (" + numTracks + ") from the listing in file: " + theFile.getAbsolutePath());
	                }
	                NemaTrack query = tracks.get(queryIdx-1); // indexes in dist mat files start at 1
	                NemaData queryResults = new NemaData(query.getId());
	                queryResults.setMetadata(NemaDataConstants.SEARCH_TRACK_DISTANCE_LIST, distances);
	                queryResults.setMetadata(NemaDataConstants.SEARCH_DISTANCE_MATRIX_NAME, matrixName);
	                queries.add(queryResults);
	                
	                //setup for next line of matrix
	                line = textBuffer.readLine();
	                lineNum++;
	            }
            }catch(NumberFormatException nfe){
            	throw new IllegalArgumentException("Failed to interpret query index on line " + lineNum,nfe);
            }
            
        }finally{
        	if(textBuffer != null){
        		textBuffer.close();
        	}
        }
		
        getLogger().info(queries.size() + " queries with track distances metadata read from dense distance matrix file: " + theFile.getAbsolutePath());
        
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
            	getLogger().warning("Distance matrix name not found in first result Object! Using default");
            }else{
            	name = (String)nameObj;
            }
            
            //Write name
            output.write( name );
            output.newLine();
            
            
            //Write file names and index trackIDs
            List<String> trackIDs = new ArrayList<String>();
            Map<String,Integer> trackIDToIndex = new HashMap<String,Integer>();
            String id;
            int idx = 1;
            for (Iterator<NemaData> it = data.iterator();it.hasNext();) {
            	id = it.next().getId();
            	trackIDToIndex.put(id, idx);
            	trackIDs.add(id);
                output.write(idx + WRITE_DELIMITER + id);
                idx++;
                output.newLine();
            }
            
            int numTracks = trackIDs.size();
            
            //write header line
            String header = "Q/R";
            for (int i = 0; i < numTracks; i++) {
                header += WRITE_DELIMITER + (i+1);
            }
            output.write(header);
            output.newLine();
            
            //Write data matrix
            
            for (Iterator<NemaData> it = data.iterator();it.hasNext();) {
                NemaData query = it.next();
                String queryId = query.getId();
                int queryIdx = trackIDToIndex.get(queryId);
                String line = "" + queryIdx;
                
                //get distances and map to trackIDs
                Object distsObj = query.getMetadata(NemaDataConstants.SEARCH_TRACK_DISTANCE_LIST);
                if (distsObj == null){
                	throw new IllegalArgumentException("No distances found in " + query);
                }
                List<NemaTrackDistance> dists = (List<NemaTrackDistance>)distsObj;
                Map<String,Float> distsMap = createDistsMap(dists);
                
                //write out matrix line
                Float dist = null;
                for (Iterator<String> trackIt = trackIDs.iterator();trackIt.hasNext();)
                {
                	id = trackIt.next();
                	dist = distsMap.get(id);
                	if(dist == null){
                		line += WRITE_DELIMITER + Float.POSITIVE_INFINITY;
                	}else{
                		line += WRITE_DELIMITER + distsMap.get(id);
                	}
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
	
	private Map<String,Float> createDistsMap(List<NemaTrackDistance> dists){
		Map<String,Float> out = new HashMap<String,Float>(dists.size());
		for (Iterator<NemaTrackDistance> iterator = dists.iterator(); iterator.hasNext();) {
			NemaTrackDistance dist = iterator.next();
			out.put(dist.getTrackId(),dist.getDistance());
		}
		
		return out;
	}
	
}
