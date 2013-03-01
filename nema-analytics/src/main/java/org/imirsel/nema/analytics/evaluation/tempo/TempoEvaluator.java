package org.imirsel.nema.analytics.evaluation.tempo;

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

public class TempoEvaluator extends EvaluatorImpl {

	/**
	 * Constant definition for tolerance of how much a tempo can be off (ratio).
	 */
	public static final double TOLERANCE = 0.08;

	/**
	 * Constructor
	 */
	public TempoEvaluator(){
		super();
	}

	@Override
	protected void setupEvalMetrics() {
		this.trackEvalMetrics.clear();
		this.trackEvalMetrics.add(NemaDataConstants.TEMPO_EXTRACTION_P_SCORE);
		this.trackEvalMetrics.add(NemaDataConstants.TEMPO_EXTRACTION_ONE_CORRECT);
		this.trackEvalMetrics.add(NemaDataConstants.TEMPO_EXTRACTION_TWO_CORRECT);

		this.overallEvalMetrics.clear();
		this.overallEvalMetrics.add(NemaDataConstants.TEMPO_EXTRACTION_P_SCORE);
		this.overallEvalMetrics.add(NemaDataConstants.TEMPO_EXTRACTION_ONE_CORRECT);
		this.overallEvalMetrics.add(NemaDataConstants.TEMPO_EXTRACTION_TWO_CORRECT);

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

	


	/**
	 * The core evaluation method. Evaluates each file against its ground-truth for a given jobId
	 * @param jobID		the jobId to evaluate
	 * @param theData	the results to evaluate for the jobId. Individual results for each file are added back to this List
	 * @return 			a single NemaData object that contains the average/summary/overall evaluation	
	 */
	@Override
	public NemaData evaluateResultFold(String jobID, NemaTrackList testSet, List<NemaData> theData) {
		//count the number of examples returned and search for any missing tracks in the results returned for the fold
		int numExamples = checkFoldResultsAreComplete(jobID, testSet, theData);


		NemaData outObj = new NemaData(jobID);

		NemaData data;
		NemaData gtData;
		double[] rawData;
		double[] rawGtData;

		/* Initialize the overall/summary counters */
		int overallOneTempoCorrect = 0;
		int overallTwoTempoCorrect = 0;
		double overallP = 0.0;

		/* Begin track by track evaluation */
		int numTracks = theData.size();
		for (int x = 0; x < numTracks; x++) {

			data = theData.get(x);
			gtData = trackIDToGT.get(data.getId());
			rawData = data
			.getDoubleArrayMetadata(NemaDataConstants.TEMPO_EXTRACTION_DATA);
			rawGtData = gtData
			.getDoubleArrayMetadata(NemaDataConstants.TEMPO_EXTRACTION_DATA);
			
			int TT1 = 0;
            int TT2 = 0;
			int oneTempoCorrect = 0;
			int twoTempoCorrect = 0;
			double pScore = 0.0;

			//First tempo correct
            if (Math.abs(rawGtData[0] - rawData[0]) < (rawGtData[0] * TOLERANCE)) {
                TT1++;
            }
            //check for case of switched TT1 and TT2
            else if (Math.abs(rawGtData[0] - rawData[1]) < (rawGtData[0] * TOLERANCE)) {
                TT1++;
            }
            
            // Second tempo correct, if TT1 and TT2 were switched this won't
            // be true anyway
            if (Math.abs(rawGtData[1] - rawData[1]) < (rawGtData[1] * TOLERANCE)) {                   
                TT2++;
            }
            // check for switched TT2 and TT1
            else if(Math.abs(rawGtData[1] - rawData[0]) < (rawGtData[1] * TOLERANCE)) { 
                TT2++;
            }
			
            if ((TT1 + TT2) == 1 || (TT1 + TT2) == 2) {
            	oneTempoCorrect = 1;
            	overallOneTempoCorrect++;
            }
            if ((TT1 + TT2) == 2) {
            	twoTempoCorrect = 1;
            	overallTwoTempoCorrect++;
            }
            
            pScore = rawGtData[2] * TT1 + (1 - rawGtData[2]) * TT2;
            overallP += pScore;
            
            

			/* 
			 * Populate each track's NemaData object with the measures. Most of these are binary {0,1} 
			 * except for p. Store them as doubles though for consistency. */
			data.setMetadata(NemaDataConstants.TEMPO_EXTRACTION_ONE_CORRECT, (double)oneTempoCorrect);
			data.setMetadata(NemaDataConstants.TEMPO_EXTRACTION_TWO_CORRECT, (double)twoTempoCorrect);
			data.setMetadata(NemaDataConstants.TEMPO_EXTRACTION_P_SCORE, pScore);


		}

		/* 
		 * Calculate summary/overall evaluation results. Populate a summary NemaData object with 
		 * the evaluations, and return it */		


		outObj.setMetadata(NemaDataConstants.TEMPO_EXTRACTION_ONE_CORRECT, (double)overallOneTempoCorrect/(double)numExamples);
		outObj.setMetadata(NemaDataConstants.TEMPO_EXTRACTION_TWO_CORRECT, (double)overallTwoTempoCorrect/(double)numExamples);
		outObj.setMetadata(NemaDataConstants.TEMPO_EXTRACTION_P_SCORE, overallP/numExamples);

		return outObj;
	}
}
