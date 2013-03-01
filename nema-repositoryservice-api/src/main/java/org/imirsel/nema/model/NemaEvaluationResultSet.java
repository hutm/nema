package org.imirsel.nema.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.List;

/**
 * Class encapsulating results returned by evaluating a system or systems using
 * a NEMA evaluation tool. The object encapsulates the NemaDataset and NemaTask
 * task Objects defining the experiment, the List of NemaTrackList Objects defining
 * the test sets (and training sets if applicable) from the NemaDataset.
 * Results for systems can be added to the Object and are composed of NemaData
 * Objects representing the overall evaluation, evaluation per fold and both
 * the results and evaluation for each NemaTrack in the test set.
 * 
 * @author kris.west@gmail.com
 *
 */
public class NemaEvaluationResultSet {
	
	private NemaDataset dataset;
	private NemaTask task;

	private List<String> overallEvalMetrics;
	private List<String> foldEvalMetrics;
	private List<String> trackEvalMetricsAndResults;
	
	private List<NemaTrackList> trainingSetTrackLists;
	private List<NemaTrackList> testSetTrackLists;

	private Map<String,String> jobIdToJobName;
	private Map<String,NemaSubmission> jobIdToSubmissionDetails;
	private Map<String,NemaData> jobIdToOverallEvaluation;
	private Map<String,Map<NemaTrackList,NemaData>> jobIdToPerFoldEvaluation;
	private Map<String,Map<NemaTrackList,List<NemaData>>> jobIdToPerTrackEvaluationAndResults;
	
	private Map<String,NemaData> trackIDToGT;
	
	/**
	 * Constructor for test-only experiments. Accepts the NemaDataset, NemaTask 
	 * and List of test NemaTrackList Objects defining the dataset.
	 * 
	 * @param dataset NemaDataset describing the dataset.
	 * @param task NemaTask describing the task performed on the dataset.
	 * @param testSetTrackLists List of NemaTrackList Objects defining each test 
	 * set.
	 * @param overallEvalMetrics A list of the evaluation metric keys that apply
	 * to the whole experiment.
	 * @param foldEvalMetrics A list of the evaluation metric keys that apply
	 * to a fold of the experiment.
	 * @param trackEvalMetricsAndResults A list of the evaluation metric and results 
	 * keys that apply to every track in the experiment.
	 * @param trackIDToGT A map of trackID to the ground-truth used to 
	 * evaluate.
	 */
	public NemaEvaluationResultSet(NemaDataset dataset, NemaTask task,
			List<NemaTrackList> testSetTrackLists, List<String> overallEvalMetrics,
			List<String> foldEvalMetrics, List<String> trackEvalMetricsAndResults,
			Map<String,NemaData> trackIDToGT) {
		this.dataset = dataset;
		this.task = task;
		this.testSetTrackLists = testSetTrackLists;
		this.overallEvalMetrics = overallEvalMetrics;
		this.foldEvalMetrics = foldEvalMetrics;
		this.trackEvalMetricsAndResults = trackEvalMetricsAndResults;
		this.trackIDToGT = trackIDToGT;
		
		jobIdToJobName = new HashMap<String,String>();
		jobIdToOverallEvaluation = new HashMap<String,NemaData>();
		jobIdToPerFoldEvaluation = new HashMap<String,Map<NemaTrackList,NemaData>>();
		jobIdToPerTrackEvaluationAndResults = new HashMap<String,Map<NemaTrackList,List<NemaData>>>();
	}
	
	/**
	 * Constructor for train/test experiments. Accepts the NemaDataset, NemaTask 
	 * and Lists of training and test NemaTrackList Objects defining the dataset.
	 * 
	 * @param dataset NemaDataset describing the dataset.
	 * @param task NemaTask describing the task performed on the dataset.
	 * @param trainingSetTrackLists List of NemaTrackList Objects defining each 
	 * training set.
	 * @param testSetTrackLists List of NemaTrackList Objects defining each test 
	 * set.
	 * @param overallEvalMetrics A list of the evaluation metric keys that apply
	 * to the whole experiment.
	 * @param foldEvalMetrics A list of the evaluation metric keys that apply
	 * to a fold of the experiment.
	 * @param trackEvalMetricsAndResults A list of the evaluation metric and results
	 * keys that apply to every track in the experiment.
	 * @param trackIDToGT A map of trackID to the ground-truth used to 
	 * evaluate.
	 */
	public NemaEvaluationResultSet(NemaDataset dataset, NemaTask task,
			List<NemaTrackList> trainingSetTrackLists,
			List<NemaTrackList> testSetTrackLists, List<String> overallEvalMetrics,
			List<String> foldEvalMetrics, List<String> trackEvalMetricsAndResults,
			Map<String,NemaData> trackIDToGT) {
		this.dataset = dataset;
		this.task = task;
		this.trainingSetTrackLists = trainingSetTrackLists;
		this.testSetTrackLists = testSetTrackLists;
		this.overallEvalMetrics = overallEvalMetrics;
		this.foldEvalMetrics = foldEvalMetrics;
		this.trackEvalMetricsAndResults = trackEvalMetricsAndResults;
		this.trackIDToGT = trackIDToGT;
		
		jobIdToJobName = new HashMap<String,String>();
		jobIdToOverallEvaluation = new HashMap<String,NemaData>();
		jobIdToPerFoldEvaluation = new HashMap<String,Map<NemaTrackList,NemaData>>();
		jobIdToPerTrackEvaluationAndResults = new HashMap<String,Map<NemaTrackList,List<NemaData>>>();
	}
	
	/**
	 * Return the set of jobIds that have results.
	 * 
	 * @return List of jobIds.
	 */
	public Set<String> getJobIds(){
		return jobIdToJobName.keySet();
	}
	
	/**
	 * Return the overall evaluation for a specified jobId.
	 * 
	 * @param jobId The ID of the job to retrieve data for.
	 * @return A NemaData Object representing the evaluation of the specified
	 * job averaged over the entire experiment.
	 */
	public NemaData getOverallEvaluation(String jobId){
		return jobIdToOverallEvaluation.get(jobId);
	}
	
	/**
	 * Return a map of the evaluations for a specified job ID over averaged over
	 * each fold of the experiment.
	 * 
	 * @param jobId The ID of the job to retrieve data for.
	 * @return A Map of NemaTrackList to NemaData Objects representing the evaluation
	 * for the specified system averaged over a particular fold of the experiment.
	 */
	public Map<NemaTrackList,NemaData> getPerFoldEvaluation(String jobId){
		return jobIdToPerFoldEvaluation.get(jobId);
	}
	
	/**
	 * Return a map of the evaluations and returned results for a specified job ID 
	 * for every track in each fold of the experiment. 
	 * 
	 * @param jobId The ID of the job to retrieve data for.
	 * @return A Map of NemaTrackList to a List of NemaData Objects, with one
	 * Object per track in each fold.
	 */
	public Map<NemaTrackList,List<NemaData>> getPerTrackEvaluationAndResults(String jobId){
		return jobIdToPerTrackEvaluationAndResults.get(jobId);
	}
	
	/**
	 * Return the job name for a specified job Id.
	 * 
	 * @param jobId The jobId to return a name for.
	 * @return The job name corresponding to the job Id.
	 */
	public String getJobName(String jobId){
		return jobIdToJobName.get(jobId);
	}
	
	/**
	 * Adds a complete set of results for a system to the set of results.
	 *  
	 * @param jobId The job Id the results relate to.
	 * @param jobName The name of the job that the results relate to.
	 * @param overallEval NemaData Object containing the evaluation results averaged over the whole
	 * experiment.
	 * @param perFoldEval Map from NemaTrackList to a NemaData Object containing the evaluation
	 * results averaged over each fold of the experiment. In a single fold experiment this will be
	 * the same as the overall evaluation.
	 * @param perTrackEvalAndResults Map from NemaTrackList to a List of NemaData Objects containing
	 * the evaluation result and prediction returned for each NemaTrack in each fold of the experiment.
	 * @throws IllegalArgumentException Thrown if it can be determined that the result data is inconsistent
	 * with the NemaDataset and associated task lists (e.g. doesn't contain the right number of folds).
	 */
	public void addCompleteResultSet(String jobId, String jobName, NemaData overallEval, Map<NemaTrackList,NemaData> perFoldEval, Map<NemaTrackList,List<NemaData>> perTrackEvalAndResults)
			throws IllegalArgumentException{
		//check job has data for every test set
		if(perFoldEval.size() != testSetTrackLists.size()){
			throw new IllegalArgumentException("Expected per-fold results for " + testSetTrackLists.size() + " folds, received results for " + perFoldEval.size() + " folds for jobId: " + jobId);
		}
		if(perTrackEvalAndResults.size() != testSetTrackLists.size()){
			throw new IllegalArgumentException("Expected per-track results for " + testSetTrackLists.size() + " folds, received results for " + perTrackEvalAndResults.size() + " folds for jobId: " + jobId);
		}
		//check all sets are known
		if(!testSetTrackLists.containsAll(perFoldEval.keySet())){
			throw new IllegalArgumentException("Unknown test-set in per-fold results for jobId: " + jobId);
		}
		if(!testSetTrackLists.containsAll(perTrackEvalAndResults.keySet())){
			throw new IllegalArgumentException("Unknown test-set in per-track results for jobId: " + jobId);
		}
		
		//check results from each system for each fold have data for all the tracks
		//KW: disabled as evaluators now account for missing tracks in their scores and multiple systems appear to skip tracks they don't like
//		for (Iterator<NemaTrackList> testSetIt = testSetTrackLists.iterator(); testSetIt.hasNext();) {
//			NemaTrackList list = testSetIt.next();
//			if(list.getTracks() != null){ //check if we know the actual track list contents
//				List<NemaTrack> trackList = list.getTracks();
//				if(perTrackEvalAndResults.get(list).size() != trackList.size()){
//					throw new IllegalArgumentException("Expected " + trackList.size() + " per-track results relating to NemaTrackList " + list.getId() + ", received results for " + perTrackEvalAndResults.get(list).size() + " tracks on that fold for jobId: " + jobId);
//				}
//			}
//		}
		
		//check the expected metrics are all there
		//overall
		for (Iterator<String> iterator = overallEvalMetrics.iterator(); iterator.hasNext();) {
			String key = iterator.next();
			if(!overallEval.hasMetadata(key)){
				throw new IllegalArgumentException("Expected overall evaluation to contain metric '" + key + "', but it was not found for jobId: " + jobId);
			}
		}
		
		//per fold
		for (Iterator<NemaData> foldIter = perFoldEval.values().iterator(); foldIter.hasNext();) {
			NemaData eval = foldIter.next();
			for (Iterator<String> iterator = foldEvalMetrics.iterator(); iterator.hasNext();) {
				String key = iterator.next();
				if(!eval.hasMetadata(key)){
					throw new IllegalArgumentException("Expected per-fold evaluation to contain metric '" + key + "', but it was not found for jobId: " + jobId);
				}
			}
		}
		
		
		//per track
		for (Iterator<List<NemaData>> foldIter = perTrackEvalAndResults.values().iterator(); foldIter.hasNext();) {
			List<NemaData> trackList = foldIter.next();
			for (Iterator<NemaData> trackIter = trackList.iterator(); trackIter.hasNext();){
				NemaData trackData = trackIter.next();
				for (Iterator<String> metricIterator = trackEvalMetricsAndResults.iterator(); metricIterator.hasNext();) {
					String key = metricIterator.next();
					if(!trackData.hasMetadata(key)){
						throw new IllegalArgumentException("Expected per-track evaluation for '" + trackData.getId() + "' to contain metric '" + key + "' but it was not found for jobId: " + jobId);
					}
				}
			}
		}
		
		jobIdToJobName.put(jobId, jobName);
		jobIdToOverallEvaluation.put(jobId,overallEval);
		jobIdToPerFoldEvaluation.put(jobId, perFoldEval);
		jobIdToPerTrackEvaluationAndResults.put(jobId, perTrackEvalAndResults);
	}
	
	/**
	 * Adds results for a specified system for one fold of the experiment.
	 * 
	 * @param jobId The job Id the results relate to.
	 * @param jobName The name of the job that the results relate to.
	 * @param testSet The test-set for the fold of the experiment that the results relate to.
	 * @param perFoldEval a NemaData Object containing the evaluation results for the specified 
	 * fold of the experiment.
	 * @param perTrackEvalAndResults a List of NemaData Objects containing the evaluation result 
	 * and predictions returned for each NemaTrack in the specified fold of the experiment.
	 * @throws IllegalArgumentException Thrown if it can be determined that the result data is inconsistent
	 * with the NemaDataset and associated task lists (e.g. doesn't contain the specified test-set).
	 */
	public void addSingleFoldResultSet(String jobId, String jobName, NemaTrackList testSet, NemaData perFoldEval, List<NemaData> perTrackEvalAndResults)
			throws IllegalArgumentException{
		
		//check test-set is known
		int idx = this.testSetTrackLists.indexOf(testSet);
		if (idx == -1){
			throw new IllegalArgumentException("Test-set " + testSet.getId() + " is not known to be part of this experiment!");
		}
		
		//check if we know the actual track list contents
		NemaTrackList list = this.testSetTrackLists.get(idx);
		if(list.getTracks() != null){ 
			List<NemaTrack> trackList = list.getTracks();
			if(perTrackEvalAndResults.size() != trackList.size()){
				throw new IllegalArgumentException("Expected " + trackList.size() + " per-track results relating to NemaTrackList " + list.getId() + ", received results for " + perTrackEvalAndResults.size() + " tracks on that fold for jobId: " + jobId);
			}
		}
		
		//check the expected metrics are all there
		//per fold
		for (Iterator<String> iterator = foldEvalMetrics.iterator(); iterator.hasNext();) {
			String key = iterator.next();
			if(!perFoldEval.hasMetadata(key)){
				throw new IllegalArgumentException("Expected per-fold evaluation to contain metric '" + key + "', but it was not found!");
			}
		}
		//per track
		for (Iterator<NemaData> trackIter = perTrackEvalAndResults.iterator(); trackIter.hasNext();){
			NemaData trackData = trackIter.next();
			
			for (Iterator<String> metricIterator = trackEvalMetricsAndResults.iterator(); metricIterator.hasNext();) {
				String key = metricIterator.next();
				if(!trackData.hasMetadata(key)){
					throw new IllegalArgumentException("Expected per-track evaluation for '" + trackData.getId() + "' to contain metric '" + key + "' but it was not found!");
				}
			}
		}
		
		
		jobIdToJobName.put(jobId, jobName);
		

		Map<NemaTrackList,NemaData> perFoldMap = jobIdToPerFoldEvaluation.get(jobId);
		if (perFoldMap == null){
			perFoldMap = new HashMap<NemaTrackList, NemaData>(testSetTrackLists.size());
			jobIdToPerFoldEvaluation.put(jobId, perFoldMap);
		}
		perFoldMap.put(testSet, perFoldEval);
			
		Map<NemaTrackList,List<NemaData>> perTrackMap = jobIdToPerTrackEvaluationAndResults.get(jobId);
		if (perTrackMap == null){
			perTrackMap = new HashMap<NemaTrackList, List<NemaData>>(testSetTrackLists.size());
			jobIdToPerTrackEvaluationAndResults.put(jobId, perTrackMap);
		}
		perTrackMap.put(testSet, perTrackEvalAndResults);
	}
	
	/**
	 * Adds a complete set of analysis results for a system to the set of results.
	 *  
	 * @param jobId The job Id the results relate to.
	 * @param jobName The name of the job that the results relate to.
	 * @param perTrackResults Map from NemaTrackList to a List of NemaData Objects containing
	 * the evaluation result and prediction returned for each NemaTrack in each fold of the experiment.
	 * @throws IllegalArgumentException Thrown if it can be determined that the result data is inconsistent
	 * with the NemaDataset and associated task lists (e.g. doesn't contain the right number of folds).
	 */
	public void addCompleteAnalysisSet(String jobId, String jobName, Map<NemaTrackList,List<NemaData>> perTrackResults)
			throws IllegalArgumentException{
		if(perTrackResults.size() != testSetTrackLists.size()){
			throw new IllegalArgumentException("Expected per-track results for " + testSetTrackLists.size() + " folds, received results for " + perTrackResults.size() + " folds for jobId: " + jobId);
		}
		//check all sets are known
		if(!testSetTrackLists.containsAll(perTrackResults.keySet())){
			throw new IllegalArgumentException("Unknown test-set in per-track results for jobId: " + jobId);
		}
		
		//check all the expected data is in each track
		for (Iterator<List<NemaData>> foldIter = perTrackResults.values().iterator(); foldIter.hasNext();) {
			List<NemaData> trackList = foldIter.next();
			for (Iterator<NemaData> trackIter = trackList.iterator(); trackIter.hasNext();){
				NemaData trackData = trackIter.next();
				for (Iterator<String> metricIterator = trackEvalMetricsAndResults.iterator(); metricIterator.hasNext();) {
					String key = metricIterator.next();
					if(!trackData.hasMetadata(key)){
						throw new IllegalArgumentException("Expected per-track data for '" + trackData.getId() + "' to contain metric '" + key + "' but it was not found!");
					}
				}
			}
		}
		
		jobIdToJobName.put(jobId, jobName);
		jobIdToPerTrackEvaluationAndResults.put(jobId, perTrackResults);
	}
	
	/**
	 * Adds results for a specified system for one fold of the experiment.
	 * 
	 * @param jobId The job Id the results relate to.
	 * @param jobName The name of the job that the results relate to.
	 * @param testSet The test-set for the fold of the experiment that the results relate to.
	 * @param perTrackEvalAndResults a List of NemaData Objects containing the evaluation result 
	 * and predictions returned for each NemaTrack in the specified fold of the experiment.
	 * @throws IllegalArgumentException Thrown if it can be determined that the result data is inconsistent
	 * with the NemaDataset and associated task lists (e.g. doesn't contain the specified test-set).
	 */
	public void addSingleFoldAnalysisSet(String jobId, String jobName, NemaTrackList testSet, List<NemaData> perTrackEvalAndResults)
			throws IllegalArgumentException{
		
		//check test-set is known
		int idx = this.testSetTrackLists.indexOf(testSet);
		if (idx == -1){
			throw new IllegalArgumentException("Test-set " + testSet.getId() + " is not known to be part of this experiment!");
		}
		
		//check if we know the actual track list contents
		NemaTrackList list = this.testSetTrackLists.get(idx);
		if(list.getTracks() != null){ 
			List<NemaTrack> trackList = list.getTracks();
			if(perTrackEvalAndResults.size() != trackList.size()){
				throw new IllegalArgumentException("Expected " + trackList.size() + " per-track results relating to NemaTrackList " + list.getId() + ", received results for " + perTrackEvalAndResults.size() + " tracks on that fold for jobId: " + jobId);
			}
		}
		
		//check the expected metrics are all there
		//per track
		for (Iterator<NemaData> trackIter = perTrackEvalAndResults.iterator(); trackIter.hasNext();){
			NemaData trackData = trackIter.next();
			
			for (Iterator<String> metricIterator = trackEvalMetricsAndResults.iterator(); metricIterator.hasNext();) {
				String key = metricIterator.next();
				if(!trackData.hasMetadata(key)){
					throw new IllegalArgumentException("Expected per-track evaluation for '" + trackData.getId() + "' to contain metric '" + key + "' but it was not found!");
				}
			}
		}
		
		
		jobIdToJobName.put(jobId, jobName);
			
		Map<NemaTrackList,List<NemaData>> perTrackMap = jobIdToPerTrackEvaluationAndResults.get(jobId);
		if (perTrackMap == null){
			perTrackMap = new HashMap<NemaTrackList, List<NemaData>>(testSetTrackLists.size());
			jobIdToPerTrackEvaluationAndResults.put(jobId, perTrackMap);
		}
		perTrackMap.put(testSet, perTrackEvalAndResults);
	}
	
	/**
	 * Returns a boolean flag indicating whether results have been received overall, for all folds and 
	 * for all tracks in the experiment - for every system already appearing in the results set.
	 * 
	 * @return True if results have been received overall, for all folds and 
	 * for all tracks in the experiment - for every system already appearing in the results set. 
	 */
	public boolean resultsAreComplete(){
		int numJobs = jobIdToJobName.size();
		//check all jobs have data
		if (jobIdToOverallEvaluation.size() != numJobs){
			return false;
		}
		if (jobIdToPerFoldEvaluation.size() != numJobs){
			return false;
		}
		if (jobIdToPerTrackEvaluationAndResults.size() != numJobs){
			return false;
		}
			
		//check all jobs have data for every test-set
		for (Iterator<String> iterator = jobIdToJobName.keySet().iterator(); iterator.hasNext();) {
			String jobId = iterator.next();
			Map<NemaTrackList,NemaData> perFoldMap = jobIdToPerFoldEvaluation.get(jobId);
			if(perFoldMap.size() != testSetTrackLists.size()){
				return false;
			}
			Map<NemaTrackList,List<NemaData>> perTrackMap = jobIdToPerTrackEvaluationAndResults.get(jobId);
			if(perTrackMap.size() != testSetTrackLists.size()){
				return false;
			}
		}
		
		//check results from each system for each fold have data for all the tracks
		for (Iterator<NemaTrackList> testSetIt = testSetTrackLists.iterator(); testSetIt.hasNext();) {
			NemaTrackList list = testSetIt.next();
			if(list.getTracks() != null){ //check if we know the actual track list contents
				List<NemaTrack> trackList = list.getTracks();
				for (Iterator<String> jobIt = jobIdToJobName.keySet().iterator(); testSetIt.hasNext();) {
					String jobId = jobIt.next();
					Map<NemaTrackList,List<NemaData>> perTrackMap = jobIdToPerTrackEvaluationAndResults.get(jobId);
					if(perTrackMap.get(list).size() != trackList.size()){
						return false;
					}
				}
			}
		}
		
		//check the expected metrics are all there
		//overall
		for (Iterator<String> jobIt = jobIdToJobName.keySet().iterator(); jobIt.hasNext();) {
			String jobId = jobIt.next();
			NemaData overallEval = jobIdToOverallEvaluation.get(jobId);
			for (Iterator<String> iterator = overallEvalMetrics.iterator(); iterator.hasNext();) {
				String key = iterator.next();
				if(!overallEval.hasMetadata(key)){
					throw new IllegalArgumentException("Expected overall evaluation to contain metric '" + key + "', but it was not found!");
				}
			}
		}
		
		//per fold
		for (Iterator<String> jobIt = jobIdToJobName.keySet().iterator(); jobIt.hasNext();) {
			String jobId = jobIt.next();
			Map<NemaTrackList,NemaData> perFoldEval = jobIdToPerFoldEvaluation.get(jobId);
			for (Iterator<NemaData> foldIter = perFoldEval.values().iterator(); foldIter.hasNext();) {
				NemaData eval = foldIter.next();
				for (Iterator<String> iterator = foldEvalMetrics.iterator(); iterator.hasNext();) {
					String key = iterator.next();
					if(!eval.hasMetadata(key)){
						throw new IllegalArgumentException("Expected per-fold evaluation to contain metric '" + key + "', but it was not found!");
					}
				}
			}
		}		
		
		//per track
		for (Iterator<String> jobIt = jobIdToJobName.keySet().iterator(); jobIt.hasNext();) {
			String jobId = jobIt.next();
			Map<NemaTrackList,List<NemaData>> perTrackEvalAndResults = jobIdToPerTrackEvaluationAndResults.get(jobId);
			for (Iterator<List<NemaData>> foldIter = perTrackEvalAndResults.values().iterator(); foldIter.hasNext();) {
				List<NemaData> trackList = foldIter.next();
				for (Iterator<NemaData> trackIter = trackList.iterator(); trackIter.hasNext();){
					NemaData trackData = trackIter.next();
					for (Iterator<String> metricIterator = trackEvalMetricsAndResults.iterator(); metricIterator.hasNext();) {
						String key = metricIterator.next();
						if(!trackData.hasMetadata(key)){
							throw new IllegalArgumentException("Expected per-track evaluation for '" + trackData.getId() + "' to contain metric '" + key + "' but it was not found!");
						}
					}
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Returns a boolean flag indicating whether analyses have been received overall, for all folds and 
	 * for all tracks in the experiment - for every system already appearing in the results set.
	 * 
	 * @return True if results have been received overall, for all folds and 
	 * for all tracks in the experiment - for every system already appearing in the results set. 
	 */
	public boolean analysesAreComplete(){
		int numJobs = jobIdToJobName.size();
		//check all jobs have data
		if (jobIdToPerTrackEvaluationAndResults.size() != numJobs){
			return false;
		}
			
		//check all jobs have data for every test-set
		for (Iterator<String> iterator = jobIdToJobName.keySet().iterator(); iterator.hasNext();) {
			String jobId = iterator.next();
			Map<NemaTrackList,List<NemaData>> perTrackMap = jobIdToPerTrackEvaluationAndResults.get(jobId);
			if(perTrackMap.size() != testSetTrackLists.size()){
				return false;
			}
		}
		
		//check results from each system for each fold have data for all the tracks
		for (Iterator<NemaTrackList> testSetIt = testSetTrackLists.iterator(); testSetIt.hasNext();) {
			NemaTrackList list = testSetIt.next();
			if(list.getTracks() != null){ //check if we know the actual track list contents
				List<NemaTrack> trackList = list.getTracks();
				for (Iterator<String> jobIt = jobIdToJobName.keySet().iterator(); testSetIt.hasNext();) {
					String jobId = jobIt.next();
					Map<NemaTrackList,List<NemaData>> perTrackMap = jobIdToPerTrackEvaluationAndResults.get(jobId);
					if(perTrackMap.get(list).size() != trackList.size()){
						return false;
					}
				}
			}
		}
		
		//check the expected metrics are all there
		//per track
		for (Iterator<String> jobIt = jobIdToJobName.keySet().iterator(); jobIt.hasNext();) {
			String jobId = jobIt.next();
			Map<NemaTrackList,List<NemaData>> perTrackEvalAndResults = jobIdToPerTrackEvaluationAndResults.get(jobId);
			for (Iterator<List<NemaData>> foldIter = perTrackEvalAndResults.values().iterator(); foldIter.hasNext();) {
				List<NemaData> trackList = foldIter.next();
				for (Iterator<NemaData> trackIter = trackList.iterator(); trackIter.hasNext();){
					NemaData trackData = trackIter.next();
					for (Iterator<String> metricIterator = trackEvalMetricsAndResults.iterator(); metricIterator.hasNext();) {
						String key = metricIterator.next();
						if(!trackData.hasMetadata(key)){
							throw new IllegalArgumentException("Expected per-track data for '" + trackData.getId() + "' to contain metric '" + key + "' but it was not found!");
						}
					}
				}
			}
		}
		
		return true;
	}
	
	/**
	 * @return The NemaDataset representing the experiment dataset.
	 */
	public NemaDataset getDataset() {
		return dataset;
	}
	
//	public void setDataset(NemaDataset dataset) {
//		this.dataset = dataset;
//	}
	
	/**
	 * @return The NemaTask object representing the task performed on the dataset
	 * (e.g. genre classification).
	 */
	public NemaTask getTask() {
		return task;
	}
	
//	public void setTask(NemaTask task) {
//		this.task = task;
//	}
	
	/**
	 * @return A List of NemaTrackList Objects representing the training set for each
	 * experiment iteration.
	 */
	public List<NemaTrackList> getTrainingSetTrackLists() {
		return trainingSetTrackLists;
	}
	
//	public void setTrainingSetTrackLists(List<NemaTrackList> trainingSetTrackLists) {
//		this.trainingSetTrackLists = trainingSetTrackLists;
//	}
	
	/**
	 * @return A List of NemaTrackList Objects representing the test set for each
	 * experiment iteration.
	 */
	public List<NemaTrackList> getTestSetTrackLists() {
		return testSetTrackLists;
	}
	
//	public void setTestSetTrackLists(List<NemaTrackList> testSetTrackLists) {
//		this.testSetTrackLists = testSetTrackLists;
//	}
	
	/**
	 * @return a Map linking job Id to job name for each system evaluated.
	 */
	public Map<String, String> getJobIdToJobName() {
		return jobIdToJobName;
	}
	
	/**
	 * Overwrites the map of job Id to Job name.
	 * @param jobIdToJobName A map from job Id to job name. No checks are performed to 
	 * see if job IDs exist in the new map for existing data.
	 */
	public void setJobIdToJobName(Map<String, String> jobIdToJobName) {
		this.jobIdToJobName = jobIdToJobName;
	}
	
	/**
	 * @return A Map of job Id to the overall evaluation result represented as a NemaData Object.
	 */
	public Map<String, NemaData> getJobIdToOverallEvaluation() {
		return jobIdToOverallEvaluation;
	}
	
//	public void setJobIdToOverallEvaluation(
//			Map<String, NemaData> jobIdToOverallEvaluation) {
//		this.jobIdToOverallEvaluation = jobIdToOverallEvaluation;
//	}
	
	/**
	 * @return A map of job Id to the map of per fold evaluation results represented as NemaData 
	 * Objects.
	 */
	public Map<String, Map<NemaTrackList,NemaData>> getJobIdToPerFoldEvaluation() {
		return jobIdToPerFoldEvaluation;
	}
	
//	public void setJobIdToPerFoldEvaluation(
//			Map<String,Map<NemaTrackList,NemaData>> jobIdToPerFoldEvaluation) {
//		this.jobIdToPerFoldEvaluation = jobIdToPerFoldEvaluation;
//	}
	
	/**
	 * @return A map of job Id to the map of per track evaluation results and predicted results,
	 * represented as a List of NemaData Objects (one per track).
	 */
	public Map<String,Map<NemaTrackList,List<NemaData>>> getJobIdToPerTrackEvaluationAndResults() {
		return jobIdToPerTrackEvaluationAndResults;
	}
	
//	public void setJobIdToPerTrackEvaluationAndResults(
//			Map<String,Map<NemaTrackList,List<NemaData>>> jobIdToPerTrackEvaluationAndResults) {
//		this.jobIdToPerTrackEvaluationAndResults = jobIdToPerTrackEvaluationAndResults;
//	}
	
	/**
	 * @return a List of the String keys for the evaluation metrics expected to be present in 
	 * overall evaluation Objects.
	 */
	public List<String> getOverallEvalMetricsKeys() {
		return overallEvalMetrics;
	}

	/**
	 * @return a List of the String keys for the evaluation metrics expected to be present in 
	 * per-fold evaluation Objects.
	 */
	public List<String> getFoldEvalMetricsKeys() {
		return foldEvalMetrics;
	}

	/**
	 * @return a List of the String keys for the evaluation metrics and results that were evauated
	 * expected to be present in per-track evaluation Objects.
	 */
	public List<String> getTrackEvalMetricsAndResultsKeys() {
		return trackEvalMetricsAndResults;
	}

	/**
	 * Sets a String trackID to ground-truth NemaData Object map that was used to 
	 * evaluate.
	 * @param trackIDToGT String trackID to ground-truth NemaData map.
	 */
	public void setTrackIDToGT(Map<String,NemaData> trackIDToGT) {
		this.trackIDToGT = trackIDToGT;
	}

	/**
	 * Returns the String trackID to ground-truth NemaData Object map that was 
	 * used to evaluate.
	 * @return String trackID to ground-truth NemaData map.
	 */
	public Map<String,NemaData> getTrackIDToGT() {
		return trackIDToGT;
	}

	public void setJobIdToSubmissionDetails(Map<String,NemaSubmission> jobIdToSubmissionDetails) {
		this.jobIdToSubmissionDetails = jobIdToSubmissionDetails;
	}

	public Map<String,NemaSubmission> getJobIdToSubmissionDetails() {
		return jobIdToSubmissionDetails;
	}

	
}
