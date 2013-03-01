package org.imirsel.nema.analytics.evaluation.key;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.imirsel.nema.analytics.evaluation.EvaluatorImpl;
import org.imirsel.nema.model.NemaData;
import org.imirsel.nema.model.NemaDataConstants;
import org.imirsel.nema.model.NemaEvaluationResultSet;
import org.imirsel.nema.model.NemaTrackList;

/**
 * Key classification evaluation and rendering.
 * 
 * @author afe405@gmail.com
 * @author kris.west@gmail.com
 * @since 0.1.0
 *
 */
public class KeyEvaluator extends EvaluatorImpl {
	
	/**
     * Constant definition for the plot file type.
     */
	public static final String MELODY_PLOT_EXT = ".png";
	
	/**
     * Constant definition for Major circle-of-fifths.
     */
    public static final String[] MAJOR_CIRCLE = {"c","g","d","a","e","b","f#","db","ab","eb","bb","f"};
    /**
     * Constant definition for Minor circle-of-fifths.
     */
    public static final String[] MINOR_CIRCLE = {"a","e","b","f#","c#","g#","eb","bb","f","c","g","d"};
    /**
     * Constant definition for note equivalences.
     */
    public static final String[][] EQUIVALENCES = {{"eb","d#"},{"f#","gb"},{"g#","ab"},{"bb", "a#"},{"c#","db"}};
    /**
     * Constant definition for score received for a correct detection.
     */
    public static final double CORRECT_SCORE = 1.0;
    /**
     * Constant definition for score received for a fifth error.
     */
    public static final double FIFTH_SCORE = 0.5;
    /**
     * Constant definition for score received for a relative major/minor error.
     */
    public static final double RELATIVE_SCORE = 0.3;
    /**
     * Constant definition for score received for a parallel major/minor error.
     */
    public static final double PARALLEL_SCORE = 0.2;
    /**
     * Constant definition for score received for a total error.
     */
    public static final double MISSED_SCORE = 0.0;

    /**
	 * Constructor (no arg - task, dataset, output and working dirs, training
	 * and test sets must be set manually).
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public KeyEvaluator() {
		super();
	}
	
	@Override
	protected void setupEvalMetrics() {
		this.trackEvalMetrics.clear();
		this.trackEvalMetrics.add(NemaDataConstants.KEY_DETECTION_WEIGHTED_SCORE);
		this.trackEvalMetrics.add(NemaDataConstants.KEY_DETECTION_CORRECT);
		this.trackEvalMetrics.add(NemaDataConstants.KEY_DETECTION_PERFECT_FIFTH_ERROR);
		this.trackEvalMetrics.add(NemaDataConstants.KEY_DETECTION_RELATIVE_ERROR);
		this.trackEvalMetrics.add(NemaDataConstants.KEY_DETECTION_PARALLEL_ERROR);
		this.trackEvalMetrics.add(NemaDataConstants.KEY_DETECTION_ERROR);
		
		this.overallEvalMetrics.clear();
		this.overallEvalMetrics.add(NemaDataConstants.KEY_DETECTION_WEIGHTED_SCORE);
		this.overallEvalMetrics.add(NemaDataConstants.KEY_DETECTION_CORRECT);
		this.overallEvalMetrics.add(NemaDataConstants.KEY_DETECTION_PERFECT_FIFTH_ERROR);
		this.overallEvalMetrics.add(NemaDataConstants.KEY_DETECTION_RELATIVE_ERROR);
		this.overallEvalMetrics.add(NemaDataConstants.KEY_DETECTION_PARALLEL_ERROR);
		this.overallEvalMetrics.add(NemaDataConstants.KEY_DETECTION_ERROR);
		
		//same as overall metrics - single fold experiment format
		this.foldEvalMetrics = this.overallEvalMetrics;
	}

	/**
	 * {inheritDoc}
	 */
	@Override
	public NemaEvaluationResultSet evaluate() throws IllegalArgumentException, IOException {
		String jobId, jobName;
		int numJobs = jobIDToFoldResults.size();
		
		/* 
		 * Make sure we have same number of sets of results per jobId (i.e. system), 
		 * as defined in the experiment */
		checkFolds();
		
		/* prepare NemaEvaluationResultSet*/
		NemaEvaluationResultSet results = getEmptyEvaluationResultSet();
		
		{
			/* Perform the evaluations on all jobIds (systems) */
			Map<NemaTrackList,List<NemaData>> sysResults;
			Map<String, Map<NemaTrackList,NemaData>> jobIdToFoldEvaluation = new HashMap<String, Map<NemaTrackList,NemaData>>(numJobs);
			for (Iterator<String> it = jobIDToFoldResults.keySet().iterator(); it.hasNext();) {
				jobId = it.next();
				getLogger().info("Evaluating experiment for jobID: " + jobId);
				sysResults = jobIDToFoldResults.get(jobId);
				Map<NemaTrackList,NemaData> foldEvals = new HashMap<NemaTrackList,NemaData>(testSets.size());
				for (Iterator<NemaTrackList> trackIt = sysResults.keySet().iterator(); trackIt.hasNext();) {
					//make sure we use the evaluators copy of the track list
					NemaTrackList trackList = testSets.get(testSets.indexOf(trackIt.next()));
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


	@Override
	public NemaData evaluateResultFold(String jobID, NemaTrackList testSet, List<NemaData> theData) {
		//count the number of examples returned and search for any missing tracks in the results returned for the fold
    	int numExamples = checkFoldResultsAreComplete(jobID, testSet, theData);
    	
		
		NemaData outObj = new NemaData(jobID);

		NemaData data;
		NemaData gtData;
		String[] rawData;
		String[] rawGtData;
		
		/* Initialize the overall/summary counters */
		int overallCorrect = 0;
        int overallPerfectFifths = 0;
        int overallRelative = 0;
        int overallParallel = 0;
        int overallErrors = 0;
        double overallPerf = 0.0;

        /* Begin track by track evaluation */
		for (int x = 0; x < theData.size(); x++) {
			
			data = theData.get(x);
			gtData = trackIDToGT.get(data.getId());
			rawData = data
					.getStringArrayMetadata(NemaDataConstants.KEY_DETECTION_DATA);
			rawGtData = gtData
					.getStringArrayMetadata(NemaDataConstants.KEY_DETECTION_DATA);
			
			/* Initialize the individual track scores */
			int correct = 0;
			int perfectFifth = 0;
			int relative = 0;
			int parallel = 0;
			int error = 0;
			double perf = 0.0;
			
			/* Check if the tonics match between result and ground-truth. Map equivalent tonics to the 'same' thing */
			if (mapToPrimaryEquivalents(rawData[0].toLowerCase()).compareTo(mapToPrimaryEquivalents(rawGtData[0].toLowerCase())) == 0) {
 
				/* Check if modes also match. If yes, correct key. If not, parallel major/minor error */ 
				if (rawData[1].toLowerCase().compareTo(rawGtData[1].toLowerCase()) == 0) {
                    correct++;
                    overallCorrect++;
                    perf = CORRECT_SCORE;
                } else {
                    parallel++;
                    overallParallel++;
                    perf = PARALLEL_SCORE;
                }
            } 
			
			/* If tonics do not match, check for perfect-fifth or relative major/minor errors */
			else {
				
				/* Check if both result and ground-truth are same mode */
                if ((rawData[1].toLowerCase()).compareTo(rawGtData[1].toLowerCase()) == 0) {
                    
                	/* Check for perfect-fifth errors using the circle-of-fifths */ 
                    int detIdx = -1;
                    int gtIdx = -1;
                    if ((rawData[1] == "minor")) {
                        
                    	/* find indices on the minor circle-of-fifths */
                        for (int m=0;m<MINOR_CIRCLE.length;m++) {
                            if(mapToPrimaryEquivalents(rawData[0].toLowerCase()).compareTo(mapToPrimaryEquivalents(MINOR_CIRCLE[m]).toLowerCase()) == 0) {
                                detIdx = m;
                            }
                            if(mapToPrimaryEquivalents(rawGtData[0].toLowerCase()).compareTo(this.mapToPrimaryEquivalents(MINOR_CIRCLE[m]).toLowerCase()) == 0) {
                                gtIdx = m;
                            }
                        }
                    } 
                    else
                    {
                    	/* find indices on the major circle-of-fifths */
                        for (int m=0;m<MAJOR_CIRCLE.length;m++) {
                            if(mapToPrimaryEquivalents(rawData[0].toLowerCase()).compareTo(mapToPrimaryEquivalents(MAJOR_CIRCLE[m]).toLowerCase()) == 0) {
                                detIdx = m;
                            }
                            if(mapToPrimaryEquivalents(rawGtData[0].toLowerCase()).compareTo(mapToPrimaryEquivalents(MAJOR_CIRCLE[m]).toLowerCase()) == 0) {
                                gtIdx = m;
                            }
                        }
                    }
                    
                    /* 
                     * For a fifth error, the result must be at an index 1 higher than the ground-truth. 
                     * Since the circle-of-fifths is circular, we must also check the wrap around from index 
                     * 0 to index 11 in the circle */
                    if( (detIdx - gtIdx == 1) || (detIdx - gtIdx) == -11) {
                        perfectFifth++;
                        overallPerfectFifths++;
                        perf = FIFTH_SCORE;
                    } else {
                        error++;
                        overallErrors++;
                    }  
                } 
                
                /* If mode AND tonic are different, we check for relative major/minor error */
                else {
                	
                    /* Search for relative major/minor errors */
                    int detIdx = -1;
                    int gtIdx = -1;
                    
                    /* If result is minor, find indices of result in the minor circle, 
                     * and ground-truth in the major circle
                     */
                    if (((rawData[1].toLowerCase()).compareTo("minor")) == 0) {

                    	/*
                    	 * find index into the minor circle-of-fifths for the result tonic, and the index
                    	 * into the major circle-of-fifths for the ground-truth tonic 
                    	 */
                        for (int m=0;m<MINOR_CIRCLE.length;m++) {
                            if(mapToPrimaryEquivalents(rawData[0].toLowerCase()).compareTo(mapToPrimaryEquivalents(MINOR_CIRCLE[m]).toLowerCase()) == 0) {
                                detIdx = m;
                            }
                            if(mapToPrimaryEquivalents(rawGtData[0].toLowerCase()).compareTo(mapToPrimaryEquivalents(MAJOR_CIRCLE[m]).toLowerCase()) == 0) {
                                gtIdx = m;
                            }
                        }
                    } 
                    else{
                    	
                    	/*
                    	 * find index into the major circle-of-fifths for the result tonic, and the index
                    	 * into the minor circle-of-fifths for the ground-truth tonic 
                    	 */
                        for (int m=0;m<MAJOR_CIRCLE.length;m++) {
                            if(mapToPrimaryEquivalents(rawData[0].toLowerCase()).compareTo(mapToPrimaryEquivalents(MAJOR_CIRCLE[m]).toLowerCase()) == 0) {
                                detIdx = m;
                            }
                            if(mapToPrimaryEquivalents(rawGtData[0].toLowerCase()).compareTo(mapToPrimaryEquivalents(MINOR_CIRCLE[m]).toLowerCase()) == 0) {
                                gtIdx = m;
                            }
                        }
                    }
                    
                    /* If the indices in the corresponding circles are the same, we have a relative error */
                    if ((gtIdx == detIdx)&&(gtIdx != -1)) {
                        relative++;
                        overallRelative++;
                        perf = RELATIVE_SCORE;
                    } else {
                        error++;
                        overallErrors++;
                    }
                }
                
			} 	
			
			/* 
			 * Populate each track's NemaData object with the measures. Most of these are binary {0,1} 
			 * except for perf. Store them as doubles though for consistency. */
			data.setMetadata(NemaDataConstants.KEY_DETECTION_WEIGHTED_SCORE, perf);
			data.setMetadata(NemaDataConstants.KEY_DETECTION_CORRECT, (double)correct);
			data.setMetadata(NemaDataConstants.KEY_DETECTION_PERFECT_FIFTH_ERROR, (double)perfectFifth);
			data.setMetadata(NemaDataConstants.KEY_DETECTION_RELATIVE_ERROR, (double)relative);
			data.setMetadata(NemaDataConstants.KEY_DETECTION_PARALLEL_ERROR, (double)parallel);
			data.setMetadata(NemaDataConstants.KEY_DETECTION_ERROR, (double)error);		

		}
		
		/* 
		 * Calculate summary/overall evaluation results. Populate a summary NemaData object with 
		 * the evaluations, and return it */		
		overallPerf = CORRECT_SCORE*(double)overallCorrect 
					+ FIFTH_SCORE*(double)overallPerfectFifths 
					+ RELATIVE_SCORE*(double)overallRelative 
					+ PARALLEL_SCORE*(double)overallParallel 
					+ MISSED_SCORE*(double)overallErrors;

		outObj.setMetadata(NemaDataConstants.KEY_DETECTION_WEIGHTED_SCORE, overallPerf/(double)numExamples);
		outObj.setMetadata(NemaDataConstants.KEY_DETECTION_CORRECT, (double)overallCorrect/(double)numExamples);
		outObj.setMetadata(NemaDataConstants.KEY_DETECTION_PERFECT_FIFTH_ERROR, (double)overallPerfectFifths/(double)numExamples);
		outObj.setMetadata(NemaDataConstants.KEY_DETECTION_RELATIVE_ERROR, (double)overallRelative/(double)numExamples);
		outObj.setMetadata(NemaDataConstants.KEY_DETECTION_PARALLEL_ERROR, (double)overallParallel/(double)numExamples);
		outObj.setMetadata(NemaDataConstants.KEY_DETECTION_ERROR, (double)overallErrors/(double)numExamples);

		return outObj;
	}
	
	/**
     * Some tonics in music are 'equivalent'. This method looks for tonics that are
     * equivalent to others and maps them onto the primary version of the tonic.
     * 
     * @param key 	The key (a tonic) to map to its primary equivalent
     * @return 		The re-mapped key (a tonic)
     */
    public String mapToPrimaryEquivalents(String key) {
        String output;
        output = key;
        for (int i=0;i<EQUIVALENCES.length;i++) {
            if(output.compareTo(EQUIVALENCES[i][1]) == 0) {
                output = EQUIVALENCES[i][0];
            }
        }
        return output;
    }
}
