package org.imirsel.nema.analytics.evaluation.multif0;

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
 * Melody (single F0) evaluation and result rendering.
 * 
 * @author mertbay@gmail.com
 * @author kris.west@gmail.com
 * @since 0.1.0
 *
 */
public class MultiF0EstEvaluator extends EvaluatorImpl {

	private static final int LOWER_BOUND = 220;
	private static final int UPPER_BOUND = 440;
	private static final double TOLERANCE = 0.5;

	/**
	 * Constructor (no arg - task, dataset, output and working dirs, training
	 * and test sets must be set manually).
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public MultiF0EstEvaluator() {
		super();
	}
	
	@Override
	protected void setupEvalMetrics() {
		this.trackEvalMetrics.clear();
		this.trackEvalMetrics.add(NemaDataConstants.MULTI_F0_EST_DATA);
		this.trackEvalMetrics.add(NemaDataConstants.MULTI_F0_EST_ACCURACY);
		this.trackEvalMetrics.add(NemaDataConstants.MULTI_F0_EST_CHROMA_ACCURACY);
		this.trackEvalMetrics.add(NemaDataConstants.MULTI_F0_EST_PRECISION);
		this.trackEvalMetrics.add(NemaDataConstants.MULTI_F0_EST_E_TOT);
		this.trackEvalMetrics.add(NemaDataConstants.MULTI_F0_EST_E_MISS);
		this.trackEvalMetrics.add(NemaDataConstants.MULTI_F0_EST_E_FA);
		
		this.overallEvalMetrics.clear();
		this.overallEvalMetrics.add(NemaDataConstants.MULTI_F0_EST_DATA);
		this.overallEvalMetrics.add(NemaDataConstants.MULTI_F0_EST_ACCURACY);
		this.overallEvalMetrics.add(NemaDataConstants.MULTI_F0_EST_CHROMA_ACCURACY);
		this.overallEvalMetrics.add(NemaDataConstants.MULTI_F0_EST_PRECISION);
		this.overallEvalMetrics.add(NemaDataConstants.MULTI_F0_EST_E_TOT);
		this.overallEvalMetrics.add(NemaDataConstants.MULTI_F0_EST_E_MISS);
		this.overallEvalMetrics.add(NemaDataConstants.MULTI_F0_EST_E_FA);
		
		
		//same as overall metrics - single fold experiment format
		this.foldEvalMetrics = this.overallEvalMetrics;
	}

	/**
	 * {inheritDoc}
	 */
	@Override
	public NemaEvaluationResultSet evaluate() throws IllegalArgumentException,
			IOException {
		String jobId;
		String jobName;
		int numJobs = jobIDToFoldResults.size();
		
		/* Check all systems have just one result set */
		Map<NemaTrackList,List<NemaData>> sysResults;
		
		/* 
		 * Make sure we only have one set of results per jobId (i.e. system), 
		 * as this is not a cross-fold validated experiment */
		checkFolds();
		
		/* prepare NemaEvaluationResultSet*/
		NemaEvaluationResultSet results = getEmptyEvaluationResultSet();
		
		{
			/* Perform the evaluations on all jobIds (systems) */
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
		double[][] rawData;
		double[][] rawGtData;
		
		double vxRecallOvarall = 0.0;
		double vxFalseAlarmOverall = 0.0;
		double rawPitchOverall = 0.0;
		double rawChromaOverall = 0.0;
		double accuracyOverall = 0.0;

		for (int x = 0; x < theData.size(); x++) {
			System.out.println("ID is "+ theData.get(x).getId());
			/* Pull the algorithm and ground-truth raw data */
			data = theData.get(x);
			gtData = trackIDToGT.get(data.getId());
			rawData = data
					.get2dDoubleArrayMetadata(NemaDataConstants.MULTI_F0_EST_DATA);
			rawGtData = gtData
					.get2dDoubleArrayMetadata(NemaDataConstants.MULTI_F0_EST_DATA);

			/* Initialize frame-by-frame counters for the evaluation measures */
			int correct = 0;
			int nomelcorrect = 0;
			int incorrect = 0;
			int falsePositives = 0;
			int falseNegatives = 0;
			int falseNegCorF0 = 0;
			int octaveCorrect = 0;
			int octaveNoMelCorrect = 0;
			int octaveIncorrect = 0;
			int octaveFalsePositives = 0;
			int octaveFalseNegatives = 0;
			int octaveFalseNegCorF0 = 0;
			
			/* Perform evaluation */
			int tot = rawGtData.length;
			for (int t = 0; t < tot; t++) {
				double gtF0 = rawGtData[t][1];
				double detF0;
				if (t >= rawData.length) {
					detF0 = 0.0;
				} else {
					detF0 = rawData[t][1];
				}

				if ((gtF0 == 0) && (detF0 > 0)) {
					falsePositives++;
				} else if ((detF0 <= 0) && (gtF0 != 0)) {
					falseNegatives++;
					if ((-detF0 > (gtF0 / Math.pow(Math.pow(2.0, TOLERANCE),
							(1.0 / 12.0))))
							&& (-detF0 < (gtF0 * Math.pow(Math.pow(2.0,
									TOLERANCE), (1.0 / 12.0))))) {
						falseNegCorF0++;
					}
				} else if ((detF0 <= 0) && (gtF0 == 0)) {
					nomelcorrect++;
				} else if ((detF0 > (gtF0 / Math.pow(Math.pow(2.0, TOLERANCE),
						(1.0 / 12.0))))
						&& (detF0 < (gtF0 * Math.pow(Math.pow(2.0, TOLERANCE),
								(1.0 / 12.0))))) {
					correct++;
				} else {
					incorrect++;
				}
			}

			/* Do one octave evaluation */
			for (int t = 0; t < tot; t++) {
				double gtF0 = rawGtData[t][1];
				double detF0;
				if (t >= rawData.length) {
					detF0 = 0.0;
				} else {
					detF0 = rawData[t][1];
				}

				/* Map to one octave */
				if (gtF0 != 0) {
					while (!((gtF0 >= LOWER_BOUND) && (gtF0 < UPPER_BOUND))) {
						gtF0 = (gtF0 >= UPPER_BOUND) ? (gtF0 / 2) : (gtF0 * 2);
					}
				}
				if (detF0 != 0) {
					if (detF0 > 0) {
						while (!((detF0 >= LOWER_BOUND) && (detF0 < UPPER_BOUND))) {
							detF0 = (detF0 >= UPPER_BOUND) ? (detF0 / 2)
									: (detF0 * 2);
						}
					} else {
						while (!((-detF0 >= LOWER_BOUND) && (-detF0 < UPPER_BOUND))) {
							detF0 = (-detF0 >= UPPER_BOUND) ? (detF0 / 2)
									: (detF0 * 2);
						}
					}
				}

				if ((gtF0 == 0) && (detF0 > 0)) {
					octaveFalsePositives++;
				} else if ((detF0 <= 0) && (gtF0 == 0)) {
					octaveNoMelCorrect++;
				} else if ((detF0 <= 0) && (gtF0 != 0)) {
					octaveFalseNegatives++;
					if ((-detF0 > (gtF0 / Math.pow(Math.pow(2.0, TOLERANCE),
							(1.0 / 12.0))))
							&& (-detF0 < (gtF0 * Math.pow(Math.pow(2.0,
									TOLERANCE), (1.0 / 12.0))))) {
						octaveFalseNegCorF0++;
					}
					/*
					 * The following two else if's test a certain pathological
					 * case in octave mapping. For example in mapping to the
					 * range of [220, 440), a ground truth of 438 will stay 438
					 * while a prediction of 442 will be mapped to 221.
					 * Therefore we have to check if either double the
					 * prediction or double the ground truth is within
					 * TOLERANCE. This case will only arise in cases around the
					 * pitch 'A' for these bounds.
					 */
					else if ((-2.0 * detF0 > (gtF0 / Math.pow(Math.pow(2.0,
							TOLERANCE), (1.0 / 12.0))))
							&& (-2.0 * detF0 < (gtF0 * Math.pow(Math.pow(2.0,
									TOLERANCE), (1.0 / 12.0))))) {
						octaveFalseNegCorF0++;
					} else if ((-detF0 > (2.0 * gtF0 / Math.pow(Math.pow(2.0,
							TOLERANCE), (1.0 / 12.0))))
							&& (-detF0 < (2.0 * gtF0 * Math.pow(Math.pow(2.0,
									TOLERANCE), (1.0 / 12.0))))) {
						octaveFalseNegCorF0++;
					}
				} else if ((detF0 > (gtF0 / Math.pow(Math.pow(2.0, TOLERANCE),
						(1.0 / 12.0))))
						&& (detF0 < (gtF0 * Math.pow(Math.pow(2.0, TOLERANCE),
								(1.0 / 12.0))))) {
					octaveCorrect++;
				}
				/*
				 * The following two else if's test a certain pathological case
				 * in octave mapping. For example in mapping to the range of
				 * [220, 440), a ground truth of 438 will stay 438 while a
				 * prediction of 442 will be mapped to 221. Therefore we have to
				 * check if either double the prediction or double the ground
				 * truth is within TOLERANCE. This case will only arise in cases
				 * around the pitch 'A' for these bounds.
				 */
				else if ((2.0 * detF0 > (gtF0 / Math.pow(Math.pow(2.0,
						TOLERANCE), (1.0 / 12.0))))
						&& (2.0 * detF0 < (gtF0 * Math.pow(Math.pow(2.0,
								TOLERANCE), (1.0 / 12.0))))) {
					octaveCorrect++;
				} else if ((detF0 > (2.0 * gtF0 / Math.pow(Math.pow(2.0,
						TOLERANCE), (1.0 / 12.0))))
						&& (detF0 < (2.0 * gtF0 * Math.pow(Math.pow(2.0,
								TOLERANCE), (1.0 / 12.0))))) {
					octaveCorrect++;
				} else {
					octaveIncorrect++;
				}
			} 
			
			/* Calculate the evaluation measures for this track*/
			int gv = correct + incorrect + falseNegatives;
			int gu = nomelcorrect + falsePositives;
			double vxRecall = ((double) correct + (double) incorrect)
					/ (gv);
			double vxFalseAlarm = (Math.max(0.001, falsePositives))
					/ (Math.max(0.001, gu));
			double rawPitch = ((double) correct + (double) falseNegCorF0) 
					/ (gv); 
			double rawChroma = ((double) octaveCorrect + (double) octaveFalseNegCorF0)
					/ (gv);
			double accuracy = ((double) correct + (double) nomelcorrect)
					/ (tot);
			
			vxRecallOvarall += vxRecall;
			vxFalseAlarmOverall += vxFalseAlarm;
			rawPitchOverall += rawPitch;
			rawChromaOverall += rawChroma;
			accuracyOverall += accuracy;	
			
			data.setMetadata(NemaDataConstants.MELODY_OVERALL_ACCURACY, accuracy);
			data.setMetadata(NemaDataConstants.MELODY_RAW_PITCH_ACCURACY, rawPitch);
			data.setMetadata(NemaDataConstants.MELODY_RAW_CHROMA_ACCURACY, rawChroma);
			data.setMetadata(NemaDataConstants.MELODY_VOICING_RECALL, vxRecall);
			data.setMetadata(NemaDataConstants.MELODY_VOICING_FALSE_ALARM, vxFalseAlarm);
					

		}
		
		/* 
		 * Calculate summary/overall evaluation results. Populate a summary NemaData object with 
		 * the evaluations, and return it */
		
		vxRecallOvarall = vxRecallOvarall / (numExamples);
		vxFalseAlarmOverall = vxFalseAlarmOverall / (numExamples);
		rawPitchOverall = rawPitchOverall / (numExamples);
		rawChromaOverall = rawChromaOverall / (numExamples);
		accuracyOverall = accuracyOverall / (numExamples);
		
		outObj.setMetadata(NemaDataConstants.MELODY_OVERALL_ACCURACY, accuracyOverall);
		outObj.setMetadata(NemaDataConstants.MELODY_RAW_PITCH_ACCURACY, rawPitchOverall);
		outObj.setMetadata(NemaDataConstants.MELODY_RAW_CHROMA_ACCURACY, rawChromaOverall);
		outObj.setMetadata(NemaDataConstants.MELODY_VOICING_RECALL, vxRecallOvarall);
		outObj.setMetadata(NemaDataConstants.MELODY_VOICING_FALSE_ALARM, vxFalseAlarmOverall);

		return outObj;
	}
}
