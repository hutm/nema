/**
 * 
 */
package org.imirsel.nema.analytics.evaluation;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import org.imirsel.nema.model.NemaData;
import org.imirsel.nema.model.NemaDataset;
import org.imirsel.nema.model.NemaEvaluationResultSet;
import org.imirsel.nema.model.NemaSubmission;
import org.imirsel.nema.model.NemaTask;
import org.imirsel.nema.model.NemaTrack;
import org.imirsel.nema.model.NemaTrackList;
import org.imirsel.nema.model.logging.AnalyticsLogFormatter;

/**
 * Abstract implementation of Evaluator.
 * 
 * @author kris.west@gmail.com
 * @since 0.1.0
 *
 */
public abstract class EvaluatorImpl implements Evaluator {

	protected Logger _logger;
//	protected File workingDir;
//	protected File outputDir;
	protected Map<String,NemaData> trackIDToGT;
	protected Map<String,Map<NemaTrackList,List<NemaData>>> jobIDToFoldResults;
	protected Map<String,String> jobIDToName;
	protected Map<String,NemaSubmission> jobIDToSubmissionDetails;
	protected NemaTask task;
	protected NemaDataset dataset;
	protected List<NemaTrackList> trainingSets;
	protected List<NemaTrackList> testSets;
	
	protected List<String> overallEvalMetrics;
	protected List<String> foldEvalMetrics;
	protected List<String> trackEvalMetrics;
	
	private boolean appendJobIds = false;
	private boolean addSequenceNumbers = false;
	private static final int JOBID_LENGTH_THRESHOLD = 10;
	
	public EvaluatorImpl(){
		_logger = Logger.getLogger(this.getClass().getName());
		
		trackIDToGT = new HashMap<String,NemaData>();
		jobIDToFoldResults = new HashMap<String,Map<NemaTrackList,List<NemaData>>>();
		jobIDToName = new HashMap<String,String>();
		task = null;
		dataset = null;
		trainingSets = null;
		testSets = null;
		overallEvalMetrics = new ArrayList<String>();
		foldEvalMetrics = new ArrayList<String>();
		trackEvalMetrics = new ArrayList<String>();
		setupEvalMetrics();
		jobIDToSubmissionDetails = null;
	}
	
	protected abstract void setupEvalMetrics();
	
	public void setTrainingSets(List<NemaTrackList> trainingSets){
    	this.trainingSets = trainingSets;
    }
    
    public void setTestSets(List<NemaTrackList> testSets){
    	this.testSets = testSets;
    }
    
    public List<NemaTrackList> getTrainingSets() {
		return trainingSets;
	}

	public List<NemaTrackList> getTestSets() {
		return testSets;
	}

    public NemaEvaluationResultSet getEmptyEvaluationResultSet(){
    	if(trackIDToGT == null){
    		getLogger().warning("Ground-truth was not set before result set was created. Ground-truth will not be available in the results Object!");
    	}
    	NemaEvaluationResultSet results = new NemaEvaluationResultSet(dataset, task, trainingSets, testSets, getOverallEvalMetricsKeys(), getFoldEvalMetricsKeys(), getTrackEvalMetricKeys(), trackIDToGT);
    	if(jobIDToSubmissionDetails != null){
    		results.setJobIdToSubmissionDetails(jobIDToSubmissionDetails);
    	}
    	
    	return results;
    }
	
	public List<String> getOverallEvalMetricsKeys() {
		return overallEvalMetrics;
	}

	public List<String> getFoldEvalMetricsKeys() {
		return foldEvalMetrics;
	}

	public List<String> getTrackEvalMetricKeys() {
		return trackEvalMetrics;
	}

	/**
	 * Performs simple averaging of the per-fold evaluation metrics to produce an
	 * overall evaluation.
	 * @param jobId The jobId related to the results.
	 * @param perFoldEvaluations The collection of fold evaluations to average.
	 * @return A NemaData Object representing the averaged, overall evaluation.
	 */
	protected NemaData averageFoldMetrics(String jobId, Collection<NemaData> perFoldEvaluations){
		NemaData[] foldData = perFoldEvaluations.toArray(new NemaData[perFoldEvaluations.size()]);
		NemaData overall = new NemaData(jobId);
		for (Iterator<String> metricIt = foldEvalMetrics.iterator(); metricIt.hasNext();) {
			String metric = metricIt.next();
			double accum = 0.0;
			for (int i = 0; i < foldData.length; i++) {
				accum += foldData[i].getDoubleMetadata(metric);
			}
			overall.setMetadata(metric, accum / foldData.length);
		}
		return overall;
	}
	
	/**
	 * Checks that the algorithm results contain results for each of the test 
	 * sets declared in the experiment.
	 * 
	 * @throws IllegalArgumentException Thrown if the results for one of the 
	 * systems does not match the experiment definition.
	 */
	protected int checkFolds() throws IllegalArgumentException{
		String jobID;
		Map<NemaTrackList,List<NemaData>> sysResults;
		for (Iterator<String> it = jobIDToFoldResults.keySet().iterator(); it
				.hasNext();) {
			jobID = it.next();
			sysResults = jobIDToFoldResults.get(jobID);
			if (!sysResults.keySet().containsAll(testSets)) {
				throw new IllegalArgumentException(
						"The folds detected for job Id '" + jobID + "' do not match the dataset!");
			}
		}
		return testSets.size();
	}
	
	/** Checks that results are returned for all tracks in the test set,
	 * sorts the results into trackID order and returns the number of results 
	 * that should be present for use in the evaluation statistics. 
	 * 
	 * @param jobID The job ID being tested.
	 * @param testSet The test set to compare to.
	 * @param theData The list of results returned encoded as NemaData Objects.
	 * @return The number of examples that are in the test set.
	 */
	protected int checkFoldResultsAreComplete(String jobID,
			NemaTrackList testSet, List<NemaData> theData) {
		List<NemaTrack> tracks = testSet.getTracks();
    	int numExamples = -1;
    	if (tracks == null){
    		numExamples = theData.size();
    		getLogger().warning("The list of tracks in the test set was not " +
    				"provided, hence, it cannot be confirmed that job ID " 
    				+ jobID + " returned results for the entire set.");
    	}else{
    		numExamples = tracks.size();
    		if (numExamples != theData.size()){
    			getLogger().warning("job ID " + jobID + " returned results for " +
    					theData.size() + " tracks, when the test contains " + 
    					numExamples + ". Missing tracks will still be counted in" +
    							" the final accuracy score.");
    			
    			//find missing results and report
    			Set<String> returnedIds = new HashSet<String>(numExamples);
    			for (Iterator<NemaData> iterator = theData.iterator(); iterator
						.hasNext();) {
					returnedIds.add(iterator.next().getId());
				}
    			List<String> missing = new ArrayList<String>();
    			for (Iterator<NemaTrack> iterator = tracks.iterator(); iterator
						.hasNext();) {
					String id = iterator.next().getId();
					if (!returnedIds.contains(id)){
						missing.add(id);
					}
				}
    			if (!missing.isEmpty()){
    				String msg = "No predictions were returned for the following track ID(s): ";
    				for (Iterator<String> iterator = missing.iterator(); iterator
							.hasNext();) {
    					String id = iterator.next();
						msg += id;
						if (iterator.hasNext()){
							msg += ", ";
						}
						
					}
    				getLogger().warning(msg);
    			}
    		}
    	}
    	Collections.sort(theData);
		return numExamples;
	}

	public Logger getLogger() {
		if (_logger == null){
			_logger = Logger.getLogger(this.getClass().getName());
		}
		return _logger;
	}

	public void addLogDestination(PrintStream stream) {
		Handler handler = new StreamHandler(stream, new AnalyticsLogFormatter());
		getLogger().addHandler(handler);
	}

//	public void setOutputDir(File outputDir_) throws FileNotFoundException {
//		outputDir = outputDir_;
//		outputDir.mkdirs();
//		if (!outputDir.exists()){
//			throw new FileNotFoundException("Output directory " + outputDir.getAbsolutePath() + " was not found and could not be created!");
//		}
//	}
//
//	public void setWorkingDir(File workingDir_) throws FileNotFoundException {
//		workingDir = workingDir_;
//		workingDir.mkdirs();
//		if (!workingDir.exists()){
//			throw new FileNotFoundException("Working directory " + workingDir.getAbsolutePath() + " was not found and could not be created!");
//		}
//	}
	
	public void addResults(String systemName, String jobID, NemaTrackList fold, List<NemaData> results) throws IllegalArgumentException{
		NemaData data;
		String metaKey = getTask().getSubjectTrackMetadataName();
		
//		//check name is unique, if not either append job IDs to job name 
//			//or if they are too long use sequence numbers instead
//		if (addSequenceNumbers){
//			
//		}else{
//			if(appendJobIds){
//				if(jobID.length() > JOBID_LENGTH_THRESHOLD){
//					addSequenceNumbers = true;
//					//adjust those already received
//				}
//			}else if(jobIDToName.values().contains(systemName)){
//				if(jobID.length() > JOBID_LENGTH_THRESHOLD){
//					addSequenceNumbers = true;
//					//adjust those already received
//				}else{
//					appendJobIds = true;
//					//adjust those already received
//				}
//				
//			}else{
//				jobIDToName.put(jobID, systemName);
//			}
//		}
		
		jobIDToName.put(jobID, systemName);
		
		for(Iterator<NemaData> it = results.iterator(); it.hasNext();){
			data = it.next();
			if (!data.hasMetadata(metaKey)) {
				String[] avail = data.metadataKeys();
				String keyString = "";
	            for (int i=0;i<avail.length;i++) {
	                keyString += avail[i] + "\t";
	            }
				_logger.warning("Results object received for job ID '" + jobID + "', track ID '" + data.getId() + "' does not contain '" + metaKey + "' metadata!\n" +
						"Metadata keys available:\n"
						+ keyString);
			}
		}
		
		Map<NemaTrackList,List<NemaData>> resultList = jobIDToFoldResults.get(jobID);
		if (resultList == null){
			resultList = new HashMap<NemaTrackList,List<NemaData>>(testSets.size());
			jobIDToFoldResults.put(jobID, resultList);
		}
		int testSetIdx = testSets.indexOf(fold);
		if(testSetIdx == -1){
			throw new IllegalArgumentException("Fold number " + fold.getFoldNumber() + ", id " + fold.getId() + " was not found in the experiment definition (which includes " + testSets.size() + " sets)");
		}
		NemaTrackList testSet = testSets.get(testSetIdx);
		resultList.put(testSet,results);
	}
	
	public void addResults(NemaSubmission submissionDetails, String jobID, NemaTrackList fold, List<NemaData> results) throws IllegalArgumentException{
		addResults(submissionDetails.getSubmissionCode(),jobID,fold,results);
		if(jobIDToSubmissionDetails == null){
			jobIDToSubmissionDetails = new HashMap<String, NemaSubmission>();
		}
		jobIDToSubmissionDetails.put(jobID, submissionDetails);
	}
	

	public void setGroundTruth(Collection<NemaData> groundtruth) {
		NemaData data;
		String metaKey = getTask().getSubjectTrackMetadataName();
		for(Iterator<NemaData> it = groundtruth.iterator(); it.hasNext();){
			data = it.next();
			if (!data.hasMetadata(metaKey)) {
				String[] avail = data.metadataKeys();
				String keyString = "";
	            for (int i=0;i<avail.length;i++) {
	                keyString += avail[i] + "\t";
	            }
				_logger.warning("Ground-truth object received for track ID '" + data.getId() + "' does not contain '" + metaKey + "' metadata!\n" +
						"Metadata keys available:\n"
						+ keyString);
			}
			trackIDToGT.put(data.getId(), data);
		}		
		_logger.info("Received groundtruth for " + groundtruth.size() + " track IDs");
	}
	
	public Collection<NemaData> getGroundTruth() {
		return trackIDToGT.values();
	}
	
	public NemaData getGroundTruth(String trackID){
		return trackIDToGT.get(trackID);
	}

	public void setTask(NemaTask task) {
		this.task = task;
	}
	
	public void setDataset(NemaDataset dataset) {
		this.dataset = dataset;
	}
	
	public NemaTask getTask() {
		return task;
	}
	
	public NemaDataset getDataset() {
		return dataset;
	}

//	public void setPerformMatlabStatSigTests(boolean performMatlabStatSigTests) {
//		this.performMatlabStatSigTests = performMatlabStatSigTests;
//	}
//
//	public boolean getPerformMatlabStatSigTests() {
//		return performMatlabStatSigTests;
//	}
//
//	public void setMatlabPath(File matlabPath) {
//		this.matlabPath = matlabPath;
//	}
//
//	public File getMatlabPath() {
//		return matlabPath;
//	}

	public abstract NemaEvaluationResultSet evaluate() throws IllegalArgumentException, IOException;
	
	public abstract NemaData evaluateResultFold(String jobID, NemaTrackList testSet, List<NemaData> theData);

//    public abstract void renderResults(NemaEvaluationResultSet results, File outputDir) throws IOException;
}
