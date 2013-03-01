package org.imirsel.nema.analytics.evaluation.onset;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.imirsel.nema.analytics.evaluation.EvaluatorImpl;
import org.imirsel.nema.model.NemaData;
import org.imirsel.nema.model.NemaDataConstants;
import org.imirsel.nema.model.NemaEvaluationResultSet;
import org.imirsel.nema.model.NemaTrackList;

public class OnsetEvaluator extends EvaluatorImpl {

	private static final double TOLERANCE = 0.05;

	private List<String> classList = null;
	
	/**
	 * Constructor (no arg - task, dataset, output and working dirs, training
	 * and test sets must be set manually).
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public OnsetEvaluator() {
		super();
	}

	@Override
	public NemaEvaluationResultSet evaluate() throws IllegalArgumentException,
	IOException {
		String jobId;
		String jobName;
		int numJobs = jobIDToFoldResults.size();

		classList = new ArrayList<String>();
		
		// First determine number of unique classes/instrumentations
		for(NemaData gtData:this.getGroundTruth()){
			if (gtData.hasMetadata(NemaDataConstants.ONSET_DETECTION_CLASS)) {
				String className = gtData.getStringMetadata(NemaDataConstants.ONSET_DETECTION_CLASS);
				if (!classList.contains(className)) {
					classList.add(className);
				}	
			} else {
				String className = "Unclassified";
				if (!classList.contains(className)) {
					classList.add(className);
				}
				
			}
		}
		
		
		/* Check all systems have just one result set */
		Map<NemaTrackList,List<NemaData>> sysResults;

		/* 
		 * Make sure we only have one set of results per jobId (i.e. system), 
		 * as this is not a cross-fold validated experiment */
		checkFolds();

		/* prepare NemaEvaluationResultSet*/
		NemaEvaluationResultSet results = getEmptyEvaluationResultSet();

		{
			/* keep track of the classes */
			Set<String> classNames = null;
			
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
					
					//check classes here - must be same across all folds/jobs
					List<String> classes = (List<String>)result.getMetadata(NemaDataConstants.ONSET_DETECTION_CLASSES);
					if(classNames == null){
						classNames = new HashSet<String>(classes);
					}else{
						if (!classNames.containsAll(classes)){
							throw new IllegalArgumentException("");
						}
					}
					
					
					
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
				List<String> metricsSingle = new ArrayList<String>();
				metricsSingle.add(NemaDataConstants.ONSET_DETECTION_AVG_FMEASURE);
				metricsSingle.add(NemaDataConstants.ONSET_DETECTION_AVG_PRECISION);
				metricsSingle.add(NemaDataConstants.ONSET_DETECTION_AVG_RECALL);
				List<String> metricsArray = new ArrayList<String>();
				metricsArray.add(NemaDataConstants.ONSET_DETECTION_AVG_FMEASURE_BY_CLASS);
				metricsArray.add(NemaDataConstants.ONSET_DETECTION_AVG_PRECISION_BY_CLASS);
				metricsArray.add(NemaDataConstants.ONSET_DETECTION_AVG_RECALL_BY_CLASS);
				NemaData overall = averageFoldMetrics(jobId, foldEvals.values(), metricsSingle, metricsArray, classList, NemaDataConstants.ONSET_DETECTION_CLASSES);
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
	public NemaData evaluateResultFold(String jobID, NemaTrackList testSet,
			List<NemaData> dataList) {

		int numExamples = checkFoldResultsAreComplete(jobID, testSet, dataList);

		NemaData gtData;
		
		Collections.sort(classList);
		
		// Compute number of classes. We will create also a class "Total" hence the +1 if there are no distinct classes
		int numClasses = 0;
		if (!classList.isEmpty()) {
			numClasses = classList.size();
		}

		double totalFMeasure = 0.0;
		double totalRecall = 0.0;
		double totalPrecision = 0.0;
		int totalCorrect = 0;
		int totalFalsePositives = 0;
		int totalFalseNegatives = 0;
		int totalDoubled = 0;
		int totalMerged = 0;
		double FPsum = 0.0;
		double CDsum = 0.0;
		double qsum = 0.0;
		
		int[] classTotalCorrect = null;
		int[] classFalsePositives = null;
		int[] classFalseNegatives = null;
		int[] classDoubled = null;
		int[] classMerged = null;
		int[] classCounts = null;
		double[] classAvgCorrect = null;
		double[] classAvgFalsePositives = null;
		double[] classAvgFalseNegatives = null;
		double[] classAvgDoubled = null;
		double[] classAvgMerged = null;
		double[] classFMeasures = null;
		double[] classRecalls = null;
		double[] classPrecisions = null;

		if (!classList.isEmpty()) {
			classTotalCorrect = new int[numClasses];
			classFalsePositives = new int[numClasses];
			classFalseNegatives = new int[numClasses];
			classDoubled = new int[numClasses];
			classMerged = new int[numClasses];
			classCounts = new int[numClasses];
			classAvgCorrect = new double[numClasses];
			classAvgFalsePositives = new double[numClasses];
			classAvgFalseNegatives = new double[numClasses];
			classAvgDoubled = new double[numClasses];
			classAvgMerged = new double[numClasses];
			classFMeasures = new double[numClasses];
			classRecalls = new double[numClasses];
			classPrecisions = new double[numClasses];
		}

		int numInDetFiles = 0;
		int numInGTFiles = 0;

		double meanAbsDistance = 0.0;
		double meanDistance = 0.0;


		for(NemaData data:dataList){
			gtData = trackIDToGT.get(data.getId());
			double[][] rawGtData2D = gtData.get2dDoubleArrayMetadata(NemaDataConstants.ONSET_DETECTION_DATA);
			double[][] rawData2D = data.get2dDoubleArrayMetadata(NemaDataConstants.ONSET_DETECTION_DATA);
			double[] rawData = new double[rawData2D.length];
			for(int i = 0; i < rawData.length; i++) {
				rawData[i] = rawData2D[i][0];
			}
			int numAnnotators = rawGtData2D[0].length;
			
			//Check which class it is if they exist. Find it in the classList and get the index. 
			// We are reserving the 0th element for the total, overall, hence the + 1
			int classNum = 0;
			if (gtData.hasMetadata(NemaDataConstants.ONSET_DETECTION_CLASS)) {
				classNum = classList.indexOf(gtData.getStringMetadata(NemaDataConstants.ONSET_DETECTION_CLASS));
			} else {
				classNum = classList.indexOf("Unclassified");
			}
			double avgFMeasureForFile = 0.0;
            double avgCorrectForFile = 0.0;
            double avgFPForFile = 0.0;
            double avgFNForFile = 0.0;
            double avgRecForFile = 0.0;
            double avgPrecForFile = 0.0;
            double avgMergedForFile = 0.0;
            double avgDoubledForFile = 0.0;
            int totCorrectForFile = 0;
            int totFPForFile = 0;
            int totFNForFile = 0;
            int totMergedForFile = 0;
            int totDoubledForFile = 0;
            
			for (int curGT = 0; curGT < numAnnotators; curGT++) {
				ArrayList<Double> gtDataArr = new ArrayList<Double>();
				for (int t=0; t<rawGtData2D.length; t++) {
					double onTime = rawGtData2D[t][curGT];
					if (!Double.isNaN(onTime)) {
						gtDataArr.add(new Double(rawGtData2D[t][curGT]));
					}
				}
				double[] rawGtData = new double[gtDataArr.size()];
				for(int i = 0; i < rawGtData.length; i++) {
					rawGtData[i] = gtDataArr.get(i).doubleValue();
				}

				int correct = 0;
				int doubled = 0;
				int merged = 0;
				int falsePositives = 0;
				int falseNegatives = 0;

				int count = 0;
				for (int t=0; t<rawGtData.length; t++) {
					double onTime = rawGtData[t];
					{
						// if we've allocated scores for everything in the detection, but there's more to go in the ground truth
						// they will be falseNegatives
						if(count > rawData.length - 1) {
							falseNegatives++;
						}
						// main loop
						for (int c=count;c<rawData.length;c++) {
							if (Math.abs(rawData[c] - onTime) < TOLERANCE) {
								correct++;
								meanAbsDistance += Math.abs(rawData[c] - onTime);
								meanDistance += onTime - rawData[c];
								count = c+1;
								for (int c1=count;c1<rawData.length;c1++) {
									if (t < rawGtData.length - 1) {
										double onTime2 = rawGtData[t+1];
										// we're checking for doubles in the next predicted value in regards to the current ground truth.
										// first though, we check that the next prediction doesn't lie in the tolerance of the next truth.
										if (Math.abs(rawData[c1] - onTime2) < TOLERANCE) {
											break;
										}
										if (Math.abs(rawData[c1] - onTime) < TOLERANCE) {
											doubled++;
										}
									}
								}
								break;
							} else if (rawData[c] > (onTime + TOLERANCE)) {
								//System.out.println("false neg c: " + c + " t: " + t + " truth time: " + onTime + " test time: " + testSig.getData()[testOnsetCol][c]);
								falseNegatives++;
								count = c;
								break;
							}
							// if we've allocated scores for everything in the detection, but there's more to go in the ground truth
							// they will be falseNegatives
							if(c == rawData.length - 1){
								falseNegatives++;
							}

						}
					}
				}
				int count2 = 0;
				for (int c=0;c<rawData.length;c++) {
					double onTime = rawData[c];
					for (int t=count2; t<rawGtData.length; t++) {					
						if (Math.abs(rawGtData[t] - onTime) < TOLERANCE) {
							count2 = t+1;
							for (int c1=count2;c1<rawGtData.length;c1++) {
								if(c < rawData.length - 1) {
									double onTime2 = rawData[c+1];

									if (Math.abs(rawGtData[c1] - onTime2) < TOLERANCE) {
										break;
									}
								}
								if (Math.abs(rawGtData[c1] - onTime) < TOLERANCE) {
									merged++;
								}
							}
							break;
						}
					}
				} 

				falsePositives = rawData.length - correct;
				totalCorrect += correct;
				totalFalseNegatives += falseNegatives;
				totalFalsePositives += falsePositives;
				totalDoubled += doubled;
				totalMerged += merged;

				numInDetFiles += rawData.length;
				numInGTFiles += rawGtData.length;

				double precision = 0.0;
				double recall = 0.0;
				if (rawData.length > 0) {
					precision = (((double)correct/(double)rawData.length));
				}
				if (rawGtData.length > 0) {
					recall = (((double)correct/(double)rawGtData.length));
				}
				double fmeasure = 0.0;
				if (recall != 0.0 && precision != 0.0) {
					fmeasure = (2 * recall * precision)/(recall + precision);
				}

				double FPRate = (double)falsePositives/(double)correct * 100.0;
				double CDRate = (double)(correct - falseNegatives)/(double)correct * 100.0;
				double q = (double)(correct + falseNegatives - (falseNegatives + falsePositives))/(double)(correct + falseNegatives + falsePositives);
				avgFMeasureForFile += fmeasure;
				avgRecForFile += recall;
				avgPrecForFile += precision;
				FPsum += FPRate;
				CDsum += CDRate;
				qsum += q;
				totCorrectForFile += correct;
				totFPForFile += falsePositives;
				totFNForFile += falseNegatives;
				totMergedForFile += merged;
				totDoubledForFile += doubled;

			}
			avgFMeasureForFile = avgFMeasureForFile/(double)numAnnotators;
			avgRecForFile = avgRecForFile/(double)numAnnotators;
			avgPrecForFile = avgPrecForFile/(double)numAnnotators;
			avgCorrectForFile = (double)totCorrectForFile/(double)numAnnotators;
			avgFPForFile = (double)totFPForFile/(double)numAnnotators;
			avgFNForFile = (double)totFNForFile/(double)numAnnotators;
			avgMergedForFile = (double)totMergedForFile/(double)numAnnotators;
			avgDoubledForFile = (double)totDoubledForFile/(double)numAnnotators;

			if (!classList.isEmpty()) {
				classTotalCorrect[classNum] += totCorrectForFile;
				classFalsePositives[classNum] += totFPForFile;
				classFalseNegatives[classNum] += totFNForFile;
				classDoubled[classNum] += totDoubledForFile;
				classMerged[classNum] += totMergedForFile;
				classCounts[classNum]++;
				classFMeasures[classNum] = classFMeasures[classNum] + avgFMeasureForFile;
				classRecalls[classNum] = classRecalls[classNum] + avgRecForFile;
				classPrecisions[classNum] = classPrecisions[classNum] + avgPrecForFile;

				classAvgCorrect[classNum] += avgCorrectForFile;
				classAvgFalsePositives[classNum] += avgFPForFile;
				classAvgFalseNegatives[classNum] += avgFNForFile;
				classAvgDoubled[classNum] += avgDoubledForFile;
				classAvgMerged[classNum]  += avgMergedForFile;
			}

			totalFMeasure += avgFMeasureForFile;
			totalRecall += avgRecForFile;
			totalPrecision += avgPrecForFile;

			data.setMetadata(NemaDataConstants.ONSET_DETECTION_AVG_FMEASURE, avgFMeasureForFile);
			data.setMetadata(NemaDataConstants.ONSET_DETECTION_AVG_RECALL, avgRecForFile);
			data.setMetadata(NemaDataConstants.ONSET_DETECTION_AVG_PRECISION, avgPrecForFile);
		}
		
		for (int i = 0; i < classCounts.length; i ++) {
			classFMeasures[i] = classFMeasures[i]/classCounts[i];
			classRecalls[i] = classRecalls[i]/classCounts[i];
			classPrecisions[i] = classPrecisions[i]/classCounts[i];
		}
		totalFMeasure /= numExamples;
		totalRecall /= numExamples;
		totalPrecision /= numExamples;
		
		String[] classNames = new String[numClasses];
		for (int i = 0; i < numClasses; i++) {
			classNames[i] = classList.get(i);
		}

		NemaData outObj = new NemaData(jobID);
		outObj.setMetadata(NemaDataConstants.ONSET_DETECTION_CLASSES, classList);
		outObj.setMetadata(NemaDataConstants.ONSET_DETECTION_AVG_FMEASURE, totalFMeasure);
		outObj.setMetadata(NemaDataConstants.ONSET_DETECTION_AVG_RECALL, totalRecall);
		outObj.setMetadata(NemaDataConstants.ONSET_DETECTION_AVG_PRECISION, totalPrecision);
		outObj.setMetadata(NemaDataConstants.ONSET_DETECTION_AVG_FMEASURE_BY_CLASS, classFMeasures);
		outObj.setMetadata(NemaDataConstants.ONSET_DETECTION_AVG_RECALL_BY_CLASS, classRecalls);
		outObj.setMetadata(NemaDataConstants.ONSET_DETECTION_AVG_PRECISION_BY_CLASS, classPrecisions);
		return outObj;
	}


	@Override
	protected void setupEvalMetrics() {
		this.trackEvalMetrics.clear();
		this.trackEvalMetrics.add(NemaDataConstants.ONSET_DETECTION_AVG_FMEASURE);
		this.trackEvalMetrics.add(NemaDataConstants.ONSET_DETECTION_AVG_PRECISION);
		this.trackEvalMetrics.add(NemaDataConstants.ONSET_DETECTION_AVG_RECALL);

		this.overallEvalMetrics.clear();
		this.overallEvalMetrics.add(NemaDataConstants.ONSET_DETECTION_AVG_FMEASURE);
		this.overallEvalMetrics.add(NemaDataConstants.ONSET_DETECTION_AVG_PRECISION);
		this.overallEvalMetrics.add(NemaDataConstants.ONSET_DETECTION_AVG_RECALL);
		this.overallEvalMetrics.add(NemaDataConstants.ONSET_DETECTION_AVG_FMEASURE_BY_CLASS);
		this.overallEvalMetrics.add(NemaDataConstants.ONSET_DETECTION_AVG_PRECISION_BY_CLASS);
		this.overallEvalMetrics.add(NemaDataConstants.ONSET_DETECTION_AVG_RECALL_BY_CLASS);

		//same as overall metrics - single fold experiment format
		this.foldEvalMetrics = this.overallEvalMetrics;

	}
	
	public NemaData averageFoldMetrics(String jobId, Collection<NemaData> perFoldEvaluations, List<String> metricsSingle, List<String> metricsArray, List<String> classes, String classesKey){
		NemaData[] foldData = perFoldEvaluations.toArray(new NemaData[perFoldEvaluations.size()]);
		NemaData overall = new NemaData(jobId);
		//ArrayList<String> classes = new ArrayList<String>();
		for (Iterator<String> metricIt = metricsSingle.iterator(); metricIt.hasNext();) {
			String metric = metricIt.next();
			double accum = 0.0;
			for (int i = 0; i < foldData.length; i++) {
				accum += foldData[i].getDoubleMetadata(metric);
			}
			overall.setMetadata(metric, accum / foldData.length);
		}
		
		for (Iterator<String> metricIt = metricsArray.iterator(); metricIt.hasNext();) {
			String metric = metricIt.next();
			double[] accum = null;
			for (int i = 0; i < foldData.length; i++) {
				double[] metricArr = foldData[i].getDoubleArrayMetadata(metric);
				//classes = (ArrayList<String>)foldData[i].getMetadata(classesKey);
				if (accum == null) {
					accum = metricArr;
				} else {
					for(int k = 0; k < accum.length; k++) {
						accum[k] += metricArr[k];
					}
				}
			}
			for(int k = 0; k < accum.length; k++) {
				accum[k] /= foldData.length;
			}
			overall.setMetadata(metric, accum);
		}
		overall.setMetadata(classesKey, classes);
		return overall;
	}

}
