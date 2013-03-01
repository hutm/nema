/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.imirsel.nema.analytics.evaluation.tagsClassification;

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
import java.util.NoSuchElementException;
import java.util.Set;

import org.imirsel.nema.analytics.evaluation.*;
import org.imirsel.nema.model.*;

/**
 * Tag Classification evaluation.
 * 
 * @author kris.west@gmail.com
 * @since 0.4.0
 */
public class TagAffinityEvaluator extends EvaluatorImpl{


	private Set<String> tags = null;
	public static final int[] PRECISION_POINTS = new int[]{3,6,9,12,15};
    
    /**
	 * Constructor (no arg - task, dataset, output and working dirs, training
	 * and test sets must be set manually).
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public TagAffinityEvaluator() {
		super();
	}
  
	
    protected void setupEvalMetrics() {
    	
		this.trackEvalMetrics.clear();
		this.trackEvalMetrics.add(NemaDataConstants.TAG_AFFINITY_PRECISION_AT_N);
		this.trackEvalMetrics.add(NemaDataConstants.TAG_AFFINITY_AUC_ROC);
		this.trackEvalMetrics.add(NemaDataConstants.TAG_AFFINITY_ROC_DATA);
		
		
		this.foldEvalMetrics.clear();
		this.foldEvalMetrics.add(NemaDataConstants.TAG_AFFINITY_PRECISION_AT_N);
		this.foldEvalMetrics.add(NemaDataConstants.TAG_AFFINITY_AUC_ROC);
		this.trackEvalMetrics.add(NemaDataConstants.TAG_AFFINITY_ROC_DATA);
		this.foldEvalMetrics.add(NemaDataConstants.TAG_AFFINITY_AUC_ROC_MAP);
		this.foldEvalMetrics.add(NemaDataConstants.TAG_AFFINITY_ROC_DATA_MAP);
		

		this.overallEvalMetrics.clear();
		this.overallEvalMetrics.add(NemaDataConstants.TAG_AFFINITY_PRECISION_AT_N);
		this.overallEvalMetrics.add(NemaDataConstants.TAG_AFFINITY_AUC_ROC);
		this.overallEvalMetrics.add(NemaDataConstants.TAG_AFFINITY_AUC_ROC_MAP);
	}
    
    @SuppressWarnings("unchecked")
	private Set<String> getAllTags(){
    	HashSet<String> tags = new HashSet<String>();
    	for (NemaData data:this.getGroundTruth()){
    		tags.addAll((Set<String>)data.getMetadata(NemaDataConstants.TAG_CLASSIFICATIONS));
    	}
    	return tags;
    }
    
    /**
     * Perform the evaluation and block until the results are fully written to the output directory.
     * Also return a map encoding the evaluation results for each job in case they are needed for further processing.
     * 
     * @return a map encoding the evaluation results for each job and other data. 
     * @throws IllegalArgumentException Thrown if required metadata is not found, either in the ground-truth
     * data or in one of the system's results.
     */
    public NemaEvaluationResultSet evaluate() throws IllegalArgumentException, IOException{
    	int numJobs = jobIDToFoldResults.size();
        String jobId, jobName;
        
        //check that all systems have the same number of results
        checkFolds();
        
        this.tags = getAllTags();
        
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
					NemaTrackList trackList = testSets.get(testSets.indexOf(trackIt.next()));
					getLogger().fine("Evaluating fold " + trackList.getFoldNumber() + ", set " + trackList.getId() + "...");
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
				// note we are using an overridden version of averageFoldMetrics as the confusion matrices have to be averaged for classification
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
    
    
    
    protected void averageFoldDoubleVals(String metric, Collection<NemaData> foldEvals, NemaData aggregateEval){
    	double out = 0;
    	int numFolds = foldEvals.size();
    	for(NemaData fold:foldEvals){
    		out += fold.getDoubleMetadata(metric);
    	}
    	out /= numFolds;
    	
    	aggregateEval.setMetadata(metric, out);
    }
    
    protected void averageFoldDoubleArrays(String metric, Collection<NemaData> foldEvals, NemaData aggregateEval){
    	double[] out = null;
    	int numFolds = foldEvals.size();
    	Iterator<NemaData> foldIt = foldEvals.iterator();
    	NemaData fold = foldIt.next();
    	double[] array = fold.getDoubleArrayMetadata(metric);
    	out = new double[array.length];
    	while(true){
    		for(int i=0;i<out.length;i++){
    			out[i] += array[i];
    		}
    		try{
    			fold = foldIt.next();
    			array = fold.getDoubleArrayMetadata(metric);
    		}catch(NoSuchElementException e){
    			break;
    		}
    	}
    	
    	for(int i=0;i<out.length;i++){
    		out[i] /= numFolds;
    	}
    	
    	aggregateEval.setMetadata(metric, out);
    }
    
    @SuppressWarnings("unchecked")
	protected void averageFoldMaps(String metric, Collection<NemaData> foldEvals, NemaData aggregateEval){
    	HashMap<String,Double> out = new HashMap<String, Double>();
    	int numFolds = foldEvals.size();
    	for(NemaData fold:foldEvals){
    		Map<String,Double> foldData = (Map<String,Double>)fold.getMetadata(metric);
    		for(String key:foldData.keySet()){
    			double val = foldData.get(key)/numFolds;
    			if(out.containsKey(key)){
    				out.put(key, out.get(key) + val);
    			}else{
    				out.put(key, val);
    			}
    		}
    	}
    	
    	aggregateEval.setMetadata(metric, out);
    }

	@Override
	protected NemaData averageFoldMetrics(String jobId, Collection<NemaData> perFoldEvaluations) {
		int numFolds = this.testSets.size();
		NemaData aggregateEval = new NemaData(jobId);

		if(perFoldEvaluations.size() != numFolds){
			throw new IllegalArgumentException("Job ID " + jobId + 
					" returned " + perFoldEvaluations.size() + " folds, expected " + numFolds);
		}
		
		//precision at N
		averageFoldDoubleArrays(NemaDataConstants.TAG_AFFINITY_PRECISION_AT_N, perFoldEvaluations,aggregateEval);
		
		aggregateEval.setMetadata(NemaDataConstants.TAG_AFFINITY_PRECISION_AT_N_LEVELS, PRECISION_POINTS);
		
		//overall AUC-ROC
		averageFoldDoubleVals(NemaDataConstants.TAG_AFFINITY_AUC_ROC, perFoldEvaluations,aggregateEval);
		
		//per tag AUC-ROC
		averageFoldMaps(NemaDataConstants.TAG_AFFINITY_AUC_ROC_MAP, perFoldEvaluations,aggregateEval);
		
        //Store tag names
		aggregateEval.setMetadata(NemaDataConstants.TAG_EXPERIMENT_CLASSNAMES, tags);
		
		return aggregateEval;
	}

	private void addROCpoint(List<double[]> ROCpointSequence, double falsePosRate, double truePosRate){
        if (Double.isNaN(falsePosRate)){
            falsePosRate = 0.0;
        }
        if (Double.isNaN(truePosRate)){
            truePosRate = 0.0;
        }
        ROCpointSequence.add(new double[]{falsePosRate,truePosRate});
    }
	
	private double computeAreaUnderROCCurve(List<double[]> ROCpointSequence){
        if (ROCpointSequence.size() == 0){
            return 0.0;
        }
        double[] last = ROCpointSequence.get(0);
        double[] curr;
        double area = 0.0;
        for (int i = 1; i < ROCpointSequence.size(); i++) {
            curr = ROCpointSequence.get(i);
            double trap = trapezoidArea(last[0], curr[0], last[1], curr[1]);
            //System.out.println("\ttrap area: " + trap);
            area += trap;
            last = curr;
        }
        //System.out.println("returning " + area);
        if (Double.isNaN(area)){
        	String msg = "WARNING: returning NaN area under ROC curve\n" +
        	"ROC point seq: ";
	        for (int i = 1; i < ROCpointSequence.size(); i++) {
	            curr = ROCpointSequence.get(i);
	            msg += curr[0] + "," + curr[1] + "\t";
	        }
	        getLogger().warning(msg);
        }
        return area;
    }
	
	private double trapezoidArea(double x1, double x2, double y1, double y2){
        //System.out.println("x1: " + x1 + ", x2: " + x2 + ", y1: " + y1 + ", y2: " + y2);
        double area = ((y1 + y2)/2.0) * (x2 - x1);
        //System.out.println("trapezoid area: " + area);
        return area;
    }
    
    @SuppressWarnings("unchecked")
	public NemaData evaluateResultFold(String jobID, NemaTrackList testSet, List<NemaData> theData) {

    	//count the number of examples returned and search for any missing tracks in the results returned for the fold
    	int numExamples = checkFoldResultsAreComplete(jobID, testSet, theData);
    	
        NemaData outObj = new NemaData(jobID);
        
        NemaData data;
        NemaData gtData;
        
//        HashMap<String, Double> clip2AUC_ROC = new HashMap<String, Double>();
        HashMap<String, List<double[]>> clip2ROCpointSequence = new HashMap<String, List<double[]>>();
        
//        HashMap<String, double[]> clip2PrecisionAtN = new HashMap<String, double[]>();
        
        HashMap<String, Double> tag2AUC_ROC = new HashMap<String, Double>();
        HashMap<String, List<double[]>> tag2ROCpointSequence = new HashMap<String, List<double[]>>();
        
        double overallAUC_ROC;
        List<double[]> overallROCpointSequence = null;
        
        HashMap<String, List<AffinityDataPoint>> tag2affinityDataPoints = new HashMap<String, List<AffinityDataPoint>>();
        for (Iterator<String> it = tags.iterator(); it.hasNext();) {
            tag2affinityDataPoints.put(it.next(),new ArrayList<AffinityDataPoint>());
        }
        HashMap<String, Integer> tag2numPositiveExamples = new HashMap<String, Integer>();
        HashMap<String, Integer> tag2numNegativeExamples = new HashMap<String, Integer>();
        
        //util references
        String id, tag;
        Map<String,Double> returnedAffinities;
        Set<String> trueSet, missingAffinities;
        List<AffinityDataPoint> dataPointList;
        int positives;
        int negatives;
        int truePositives;
        int falsePositives;
        double lastAffinity = 0.0;
        AffinityDataPoint dataPoint;
        List<double[]> anROCpointSequence;
//        double[] ROCdomain;
//        double[] ROCrange;
//        double[] ds;
        List<AffinityDataPoint> tmpClipROCPointList;
        List<AffinityDataPoint> overallROCPointList = new ArrayList<AffinityDataPoint>();
        
        double[] avgPrecisionAtN = new double[PRECISION_POINTS.length];
        
        
        getLogger().fine("Computing per track evaluations...");
        for(int x=0; x < theData.size(); x++) {
            //Do simple evaluation
        	data = theData.get(x);
        	id = data.getId();
        	gtData = trackIDToGT.get(id);
            
        	
        	returnedAffinities = (Map<String,Double>)data.getMetadata(NemaDataConstants.TAG_AFFINITY_MAP);
            trueSet = (Set<String>)gtData.getMetadata(NemaDataConstants.TAG_CLASSIFICATIONS);
            tmpClipROCPointList = new ArrayList<AffinityDataPoint>();
            
            for (Iterator<String> it = returnedAffinities.keySet().iterator(); it.hasNext();) {
                tag = it.next();
                //System.out.println("\ttag: " + tag);
                try{
                    AffinityDataPoint tmpDataPoint = new AffinityDataPoint(trueSet.contains(tag), returnedAffinities.get(tag));
                    tag2affinityDataPoints.get(tag).add(tmpDataPoint);
                    tmpClipROCPointList.add(tmpDataPoint);
                    overallROCPointList.add(tmpDataPoint);
                }catch(Exception e){
                    String tagSetStr = "";
                    for (Iterator<String> tagit = tag2affinityDataPoints.keySet().iterator(); tagit.hasNext();){
                        tagSetStr += "\t'" + tagit.next() + "'\n";
                    }
                    throw new IllegalArgumentException("Tag from returned affinities (" + tag + "), for track: " + data.getId() + 
                            ", not found in the tag set. Tag set contains:\n" + tagSetStr);
                }
            }
            
            //fill in missing affinities
            missingAffinities = new HashSet<String>(tags);
            missingAffinities.removeAll(returnedAffinities.keySet());
            for (Iterator<String> it = missingAffinities.iterator(); it.hasNext();) {
                tag = it.next();
                AffinityDataPoint tmpDataPoint = new AffinityDataPoint(trueSet.contains(tag),0.0);
                tag2affinityDataPoints.get(tag).add(tmpDataPoint);
                tmpClipROCPointList.add(tmpDataPoint);
                overallROCPointList.add(tmpDataPoint);
            }
            Collections.sort(tmpClipROCPointList);
            
            //count positives and negatives
            positives = 0;
            negatives = 0;
            for (Iterator<AffinityDataPoint> it2 = tmpClipROCPointList.iterator(); it2.hasNext();) {
                dataPoint = it2.next();
                if(dataPoint.tagApplies){
                    positives++;
                }else{
                    negatives++;
                }
            }
            if (positives == 0){
                System.out.println("no positives!");
            }
            if (negatives == 0){
                System.out.println("no negatives!");
            }
            
            //compute ROC for each clip
            truePositives = 0;
            falsePositives = 0;
            lastAffinity = 0.0;
            anROCpointSequence = new ArrayList<double[]>();
            clip2ROCpointSequence.put(id,anROCpointSequence);
            //addROCpoint(anROCpointSequence, 0.0, 0.0);
            int pointCount = 0;
            int currentN = 0;
            double[] precisions = new double[PRECISION_POINTS.length];
            for (Iterator<AffinityDataPoint> it2 = tmpClipROCPointList.iterator(); it2.hasNext();) {
                dataPoint = it2.next();
                pointCount++;
                if (dataPoint.affinity != lastAffinity){
                    addROCpoint(anROCpointSequence, ((double)falsePositives/(double)negatives), ((double)truePositives/(double)positives));
                }
                if (dataPoint.tagApplies){
                    truePositives++;
                }else{
                    falsePositives++;
                }
                
                //compute precision at N scores
                if (currentN < PRECISION_POINTS.length && pointCount == PRECISION_POINTS[currentN]){
                    precisions[currentN] = truePositives / (double)(truePositives + falsePositives);
                    currentN++;
                }
            }
            
            //store precision at N for each clip
            for (int i = 0; i < PRECISION_POINTS.length; i++){
                avgPrecisionAtN[i] += precisions[i];
            }
            data.setMetadata(NemaDataConstants.TAG_AFFINITY_PRECISION_AT_N, precisions);
            data.setMetadata(NemaDataConstants.TAG_AFFINITY_PRECISION_AT_N_LEVELS, PRECISION_POINTS);
            
            if (anROCpointSequence.size() == 0){
                addROCpoint(anROCpointSequence, 0.0, 0.0);
            }
            addROCpoint(anROCpointSequence, ((double)falsePositives/(double)negatives), ((double)truePositives/(double)positives));
            
            //compute AUC-ROC for each clip 
            double auc = computeAreaUnderROCCurve(anROCpointSequence);
            data.setMetadata(NemaDataConstants.TAG_AFFINITY_AUC_ROC, auc);
            data.setMetadata(NemaDataConstants.TAG_AFFINITY_ROC_DATA, anROCpointSequence);
        }
        
        //compute fold evaluation
        for (int i = 0; i < avgPrecisionAtN.length; i++){
            avgPrecisionAtN[i] /= numExamples;
        }
        
        outObj.setMetadata(NemaDataConstants.TAG_AFFINITY_PRECISION_AT_N, avgPrecisionAtN);
        outObj.setMetadata(NemaDataConstants.TAG_AFFINITY_PRECISION_AT_N_LEVELS, PRECISION_POINTS);
        

        //compute AUC-ROC for each tag
        for (Iterator<String> it = tags.iterator(); it.hasNext();) {
            tag = it.next();
            dataPointList = tag2affinityDataPoints.get(tag);
            //this is unlikely to test false as affinities are patched in for negatives
            if (dataPointList.size() > 0){
                Collections.sort(dataPointList);

                //count positives and negatives
                positives = 0;
                negatives = 0;
                for (Iterator<AffinityDataPoint> it2 = dataPointList.iterator(); it2.hasNext();) {
                    dataPoint = it2.next();
                    if(dataPoint.tagApplies){
                        positives++;
                    }else{
                        negatives++;
                    }
                }

                tag2numPositiveExamples.put(tag, positives);
                tag2numNegativeExamples.put(tag, negatives);

                //compute ROC
                truePositives = 0;
                falsePositives = 0;
                lastAffinity = 0.0;
                anROCpointSequence = new ArrayList<double[]>();
                tag2ROCpointSequence.put(tag,anROCpointSequence);

                for (Iterator<AffinityDataPoint> it2 = dataPointList.iterator(); it2.hasNext();) {
                    dataPoint = it2.next();
                    if (dataPoint.affinity != lastAffinity){
                        addROCpoint(anROCpointSequence, ((double)falsePositives/(double)negatives), ((double)truePositives/(double)positives));
                    }
                    if (dataPoint.tagApplies){
                        truePositives++;
                    }else{
                        falsePositives++;
                    }
                }
                if (anROCpointSequence.size() == 0){
                    addROCpoint(anROCpointSequence, 0.0, 0.0);
                }
                addROCpoint(anROCpointSequence, ((double)falsePositives/negatives), ((double)truePositives/(double)positives));

//                ROCdomain = new double[anROCpointSequence.size()];
//                ROCrange = new double[anROCpointSequence.size()];
//                idx = 0;
//                for (Iterator<double[]> it2 = anROCpointSequence.iterator(); it2.hasNext();) {
//                    ds = it2.next();
//                    ROCdomain[idx] = ds[0];
//                    ROCrange[idx++] = ds[1];
//                }
//                
//                SimpleNumericPlot tagROCPlot = new SimpleNumericPlot(false, false, dataToEvaluate.getFile().getName() + ", " + tag + " ROC curve", 
//                        "tag", null, "True postive rate", "False positive rate", ROCdomain, ROCrange, null);
//
//                File plotFile = new File(plotDir.getAbsolutePath() + File.separator + tag + ".png");
//                tagROCPlot.writeChartToFile(plotFile,600,600);


                //compute AUC-ROC 
                double auc = computeAreaUnderROCCurve(anROCpointSequence);
                tag2AUC_ROC.put(tag, auc);
            }else{
                String message = "WARNING: no examples of tag '" + tag + "' found in the data";
                System.out.println(message);
            }
        }
        
        outObj.setMetadata(NemaDataConstants.TAG_AFFINITY_AUC_ROC_MAP,tag2AUC_ROC);
        outObj.setMetadata(NemaDataConstants.TAG_AFFINITY_ROC_DATA_MAP,tag2ROCpointSequence);
        
        
        {
            //compute overall AUC-ROC
            getLogger().fine("Sorting " + overallROCPointList.size() + " affinity scores in order to compute overall AUC-ROC");
            Collections.sort(overallROCPointList);

            getLogger().fine("Computing overall AUC-ROC");
            //count positives and negatives
            positives = 0;
            negatives = 0;
            for (Iterator<AffinityDataPoint> it2 = overallROCPointList.iterator(); it2.hasNext();) {
                dataPoint = it2.next();
                if(dataPoint.tagApplies){
                    positives++;
                }else{
                    negatives++;
                }
            }

            //compute ROC
            truePositives = 0;
            falsePositives = 0;
            lastAffinity = 0.0;
            anROCpointSequence = new ArrayList<double[]>();
            overallROCpointSequence = anROCpointSequence;

            for (Iterator<AffinityDataPoint> it2 = overallROCPointList.iterator(); it2.hasNext();) {
                dataPoint = it2.next();
                if (dataPoint.affinity != lastAffinity){
                    addROCpoint(overallROCpointSequence, ((double)falsePositives/(double)negatives), ((double)truePositives/(double)positives));
                }
                if (dataPoint.tagApplies){
                    truePositives++;
                }else{
                    falsePositives++;
                }
            }
            if (overallROCpointSequence.size() == 0){
                addROCpoint(overallROCpointSequence, 0.0, 0.0);
            }
            addROCpoint(overallROCpointSequence, ((double)falsePositives/negatives), ((double)truePositives/(double)positives));

//            ROCdomain = new double[overallROCpointSequence.size()];
//            ROCrange = new double[overallROCpointSequence.size()];
//            idx = 0;
//            for (Iterator<double[]> it2 = overallROCpointSequence.iterator(); it2.hasNext();) {
//                ds = it2.next();
//                ROCdomain[idx] = ds[0];
//                ROCrange[idx++] = ds[1];
//            }
//            SimpleNumericPlot overallROCPlot = new SimpleNumericPlot(false, false, dataToEvaluate.getFile().getName() + ", overall ROC curve", 
//                    "tag", null, "True postive rate", "False positive rate", ROCdomain, ROCrange, null);
//
//            File plotFile = new File(plotDir.getAbsolutePath() + File.separator + "overall.png");
//            overallROCPlot.writeChartToFile(plotFile,600,600);

            //compute AUC-ROC 
            double auc = computeAreaUnderROCCurve(anROCpointSequence);
            overallAUC_ROC = auc;

        }
        
        outObj.setMetadata(NemaDataConstants.TAG_AFFINITY_AUC_ROC,overallAUC_ROC);
        outObj.setMetadata(NemaDataConstants.TAG_AFFINITY_ROC_DATA,overallROCpointSequence);
        
        //Store tag names
        outObj.setMetadata(NemaDataConstants.TAG_EXPERIMENT_CLASSNAMES, tags);
        
        outObj.setMetadata(NemaDataConstants.TAG_NUM_POSITIVE_EXAMPLES_MAP, tag2numPositiveExamples);
        outObj.setMetadata(NemaDataConstants.TAG_NUM_NEGATIVE_EXAMPLES_MAP, tag2numNegativeExamples);
        outObj.setMetadata(NemaDataConstants.TAG_NUM_POSITIVE_EXAMPLES, positives);
        outObj.setMetadata(NemaDataConstants.TAG_NUM_NEGATIVE_EXAMPLES, negatives);
        
        return outObj;
    }

}
