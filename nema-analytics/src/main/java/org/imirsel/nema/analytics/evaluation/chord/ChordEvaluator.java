/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.imirsel.nema.analytics.evaluation.chord;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.imirsel.nema.analytics.evaluation.*;
import org.imirsel.nema.model.*;
import org.imirsel.nema.model.util.ChordConversionUtil;

/**
 * Chord estimation evaluation.
 * 
 * @author mert.bay@gmail.com
 * @author kris.west@gmail.com
 * @since 0.1.0
 */
public class ChordEvaluator extends EvaluatorImpl{
    
    private static final int GRID_RESOLUTION = 1000; //The grid resolution. 
    
    /**
	 * Constructor (no arg - task, dataset, output and working dirs, training
	 * and test sets must be set manually).
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public ChordEvaluator() {
		super();
	}

    @Override
	protected void setupEvalMetrics() {
		this.trackEvalMetrics.clear();
		this.trackEvalMetrics.add(NemaDataConstants.CHORD_OVERLAP_RATIO);
		
		this.overallEvalMetrics.clear();
		this.overallEvalMetrics.add(NemaDataConstants.CHORD_OVERLAP_RATIO);
		this.overallEvalMetrics.add(NemaDataConstants.CHORD_WEIGHTED_AVERAGE_OVERLAP_RATIO);
		
		//same as overall metrics
		this.foldEvalMetrics = this.overallEvalMetrics;
	}
    
    @Override
	public NemaEvaluationResultSet evaluate() throws IllegalArgumentException, IOException{
    	
		/* prepare NemaEvaluationResultSet*/
		NemaEvaluationResultSet results = getEmptyEvaluationResultSet();
		{
			/* Make sure we have same number of sets of results per jobId (i.e. system), 
			 * as defined in the experiment */
			checkFolds();
			
			int numJobs = jobIDToFoldResults.size();
	        
	        String jobId, jobName;
	        Map<NemaTrackList,List<NemaData>> sysResults;
			
	        //evaluate each fold for each system
			Map<String, Map<NemaTrackList,NemaData>> jobIdToFoldEvaluation = new HashMap<String, Map<NemaTrackList,NemaData>>(numJobs);
			for(Iterator<String> it = jobIDToFoldResults.keySet().iterator(); it.hasNext();){
	        	jobId = it.next();
	        	getLogger().info("Evaluating experiment folds for jobID: " + jobId);
	        	sysResults = jobIDToFoldResults.get(jobId);
	        	Map<NemaTrackList,NemaData> foldEvals = new HashMap<NemaTrackList,NemaData>(testSets.size());
				for (Iterator<NemaTrackList> trackListIt = sysResults.keySet().iterator(); trackListIt.hasNext();) {
					//make sure we use the evaluators copy of the track list
					NemaTrackList trackList = testSets.get(testSets.indexOf(trackListIt.next()));
					NemaData result = evaluateResultFold(jobId, trackList, sysResults.get(trackList));
					foldEvals.put(trackList, result);
				}
	
	        	jobIdToFoldEvaluation.put(jobId, foldEvals);
	        }
			
			/* Aggregated evaluation to produce overall results */
			Map<String, NemaData> jobIdToOverallEvaluation = new HashMap<String, NemaData>(numJobs);
			for (Iterator<String> it = jobIDToFoldResults.keySet().iterator(); it.hasNext();) {
				jobId = it.next();
				getLogger().info("Aggregating results for jobID: " + jobId);
				Map<NemaTrackList,NemaData> foldEvals = jobIdToFoldEvaluation.get(jobId);
				NemaData overall = averageFoldMetrics(jobId, foldEvals.values());
				jobIdToOverallEvaluation.put(jobId, overall);
			}
			
			/* Populate NemaEvaluationResultSet */
			for (Iterator<String> it = jobIDToName.keySet().iterator(); it.hasNext();) {
				jobId = it.next();
				jobName = jobIDToName.get(jobId);
				results.addCompleteResultSet(jobId, jobName, jobIdToOverallEvaluation.get(jobId), jobIdToFoldEvaluation.get(jobId), jobIDToFoldResults.get(jobId));
			}
	    }
        
        return results;
    }
    
    @SuppressWarnings("unchecked")
	@Override
	public NemaData evaluateResultFold(String jobID, NemaTrackList testSet, List<NemaData> theData) {
    	//count the number of examples returned and search for any missing tracks in the results returned for the fold
    	int numExamples = checkFoldResultsAreComplete(jobID, testSet, theData);
    	
        NemaData outObj = new NemaData(jobID);
        NemaData data;
        NemaData gtData;
        
        List<NemaChord> systemChords;
        List<NemaChord> gtChords;
        
        double overlapAccum = 0.0;
        double weightedAverageOverlapAccum = 0.0;
        double lengthAccum = 0.0;
        
        
        //iterate through GT tracks and compute true length of GT
        HashMap<String,Integer> trackIdToLnGT = new HashMap<String, Integer>();
        List<NemaTrack> tracks = testSet.getTracks();
    	if (tracks == null){
    		getLogger().warning("The list of tracks in the test set was not " +
    				"provided, hence, it cannot be confirmed that job ID " 
    				+ jobID + " returned results for the entire set.");
    		for (Iterator<NemaData> iterator = theData.iterator(); iterator.hasNext();) {
    			String id = iterator.next().getId();
	    		gtData = trackIDToGT.get(id);
	    		gtChords = (List<NemaChord>)gtData.getMetadata(NemaDataConstants.CHORD_LABEL_SEQUENCE);
	    		int lnGT = (int)(GRID_RESOLUTION*gtChords.get(gtChords.size()-1).getOffset()) ;
	    		lengthAccum += lnGT; 
	    		trackIdToLnGT.put(id, lnGT);
			}
    	}else{
    		for (Iterator<NemaTrack> iterator = tracks.iterator(); iterator.hasNext();) {
    			String id = iterator.next().getId();
	    		gtData = trackIDToGT.get(id);
	    		gtChords = (List<NemaChord>)gtData.getMetadata(NemaDataConstants.CHORD_LABEL_SEQUENCE);
	    		int lnGT = (int)(GRID_RESOLUTION*gtChords.get(gtChords.size()-1).getOffset()) ;
	    		lengthAccum += lnGT; 
	    		trackIdToLnGT.put(id, lnGT);
			}
    	}
        
        //iterate through tracks
        for(int x=0; x < theData.size(); x++) {
            //Do simple evaluation
        	data = theData.get(x);
        	getLogger().info("Evaluating " + data.getId());
        	gtData = trackIDToGT.get(data.getId());

        	systemChords = (List<NemaChord>)data.getMetadata(NemaDataConstants.CHORD_LABEL_SEQUENCE);
        	gtChords = (List<NemaChord>)gtData.getMetadata(NemaDataConstants.CHORD_LABEL_SEQUENCE);
        	
        	//evaluate here
        	
        	//Create grid for the ground-truth
        	int lnGT = trackIdToLnGT.get(data.getId()) ;
        	if (lnGT == 0 ){
        		throw new IllegalArgumentException("Length of GT is 0!");
        	}        		
        	int[][] gridGT = new int[lnGT][];
        	for (int i = 0; i < gtChords.size(); i++) {
        		NemaChord currentChord = gtChords.get(i);
        		int onset_index = (int)(currentChord.getOnset()*GRID_RESOLUTION);
        		int offset_index = (int)(currentChord.getOffset()*GRID_RESOLUTION);
        		for (int j = onset_index; j<offset_index; j++){
        			gridGT[j]=currentChord.getNotes();
        		}
			}
        
        	// Create grid for the system
        	int lnSys = (int)(Math.ceil(GRID_RESOLUTION*systemChords.get(systemChords.size()-1).getOffset()));
        	// for debugging
        /*	if (lnSys<lnGT){
        		double last_offGT = gtChords.get(gtChords.size()-1).getOffset();
        		systemChords.get(systemChords.size()-1).setOffset(last_offGT);
            	lnSys = (int)(Math.ceil(GRID_RESOLUTION*systemChords.get(systemChords.size()-1).getOffset()));
        	}*/
        	double overlap_score;
        	if (lnSys == 0 ){
        		//they get nothing for this file!
        		getLogger().warning("Length of system results is " +
        				"0 for track: " + data.getId() + ", number of system " +
        						"chords: " + systemChords.size() + ", last chord: "
        						+ systemChords.get(systemChords.size()-1));
        		overlap_score = 0;
        	}else{
        	

        		int[][] gridSys = new int[lnSys][]; // comment in later
        		//int[][] gridSys = new int[lnGT][];         		//for debugging
	        	for (int i = 0; i < systemChords.size(); i++) {
	        		NemaChord currentChord = systemChords.get(i);
	        		int onset_index = (int)(currentChord.getOnset()*GRID_RESOLUTION);
	        		int offset_index = (int)(currentChord.getOffset()*GRID_RESOLUTION);
	        	//	System.out.println("Chord no " + i + " onset="+onset_index + "offset=" + offset_index);
	        		for (int j = onset_index; j < offset_index; j++){
	        			gridSys[j]=currentChord.getNotes();
	        			if (gridSys[j] == null){
	        				getLogger().warning("Returned null notes for track: " + data.getId() + ", chord " + i + ", onset index: " + onset_index + ", offset index: " + offset_index);
	        			}
	        		}
				}
	        		
	        		
	        	
	        	int lnOverlap = Math.min(lnGT, lnSys);
	        	//int[] overlaps = new int[lnOverlap]; 
	        	int  overlap_total = 0;
	        	//Calculate the overlap score 
	        	for (int i = 0; i < lnOverlap; i++ ){
	        		int[] gtFrame = gridGT[i]; 
	//        		if(gtFrame == null){
	//        			getLogger().warning("GT chord Null at " +i + "-ith frame");
	//        		}
	        		
	        		int[] sysFrame = gridSys[i];
	        		//disabled check as some systems do not mark data until first chord 
	//        		if(sysFrame == null){
	//        			getLogger().warning("System chord Null at " +i + "-ith frame");
	//        		}
	        	//	overlap_total +=  calcOverlap(gtFrame,sysFrame);    
	        		//debugging purposes
	        		//end debugging
	        		overlap_total +=  calcOverlap(gtFrame,sysFrame);

	        	}
	        	
	        	//set eval metrics on input obj for track
	        	overlap_score = (double)overlap_total / (double)lnGT;
	        	
	        	
        	}
        	weightedAverageOverlapAccum += overlap_score*lnGT;	
        	overlapAccum += overlap_score;
        	getLogger().info("jobID: " + jobID + ", track: " + data.getId() + ", overlap score: " + overlap_score);
        	data.setMetadata(NemaDataConstants.CHORD_OVERLAP_RATIO, overlap_score);
        }
        
        //produce avg chord ratio
        double avg = overlapAccum / numExamples;
        //produce weighted average chord ratio
        double weightedAverageOverlap = weightedAverageOverlapAccum / lengthAccum;
        
        getLogger().info("jobID: " + jobID + ", average overlap score: " + avg);
        getLogger().info("jobID: " + jobID + ", weighted average overlap score: " + weightedAverageOverlap);
        
        //set eval metrics on eval object for fold
        outObj.setMetadata(NemaDataConstants.CHORD_OVERLAP_RATIO, avg);
        outObj.setMetadata(NemaDataConstants.CHORD_WEIGHTED_AVERAGE_OVERLAP_RATIO, weightedAverageOverlap);
        
        return outObj;
    }
    

    protected int calcOverlap(int[] gt, int[] sys) {
    	
    	
    	if (gt == null || sys == null ){
    		return 0;
    	}
    	else if (gt.length==1 && sys.length==1) {
    		if (gt[0]==24 && sys[0]==24){
    			return 1;
    		}
    		else{
    			return 0;
    		}
    			
    	}   		
    	else {
    		int match_ctr=0;
    		for (int i = 0; i < sys.length; i++) {
    			if(involves(sys[i],gt)){
    				match_ctr++;
    			}
			}
    		int threshold = 3;
    		String chord = ChordConversionUtil.getInstance().convertNoteNumbersToShorthand(gt);
    		if(chord.contains("dim") || chord.contains("aug")){
    			threshold = 2;
    		}
    		if (match_ctr >=threshold){
        		return 1;
    		}
    		else {
    			return 0;
    		}
    	}
    }
    
    private boolean involves(int key, int[] set){
    	
    	for (int i=0; i< set.length; i++){
    		if (set[i]==key){
    			return true;
    		}
    	}
    	return false;
    }
    
    

    
}
