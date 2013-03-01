/*
 * Evaluator.java
 *
 * Created on 23 October 2006, 22:21
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.imirsel.nema.analytics.evaluation;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.imirsel.nema.model.NemaData;
import org.imirsel.nema.model.NemaDataset;
import org.imirsel.nema.model.NemaEvaluationResultSet;
import org.imirsel.nema.model.NemaSubmission;
import org.imirsel.nema.model.NemaTask;
import org.imirsel.nema.model.NemaTrackList;

/**
 * Interface defining the methods of a evaluation utility for an MIR task.
 * 
 * @author kris.west@gmail.com
 * @since 0.1.0
 */
public interface Evaluator { 
//    
//	/**
//	 * Set the working directory for the utility, to be used for creation of an temp files
//	 * required.
//	 * 
//	 * @param workingDir File representing the path to the working directory.
//	 * @throws FileNotFoundException Thrown if the specified path can't be found or created.
//	 */
//    public void setWorkingDir(File workingDir) throws FileNotFoundException;
//    
//    /**
//     * Sets the output directory for the utility. Outputs may include encoded data files,
//     * reports, an HTML mini-site providing an interface to the results etc.
//     * 
//     * @param outputDir File representing the output directory.
//     * @throws FileNotFoundException Thrown if the specified path can't be found or created.
//     */
//    public void setOutputDir(File outputDir) throws FileNotFoundException;
//    
    /**
     * Set the task description so that the task name, description and metadata type may 
     * be encoded in the results and used on any generated result pages. The 
     * subject metadata maybe required to perform evaluation (for example the
     * ClassificationEvaluator can only work if it knows the field being classified, e.g. 
     * genre or mood).
     * 
     * @param task the task object to set.
     */
    public void setTask(NemaTask task);
    
    /**
     * Set the dataset description so that the dataset name, description and split criteria
 	 * may be encoded in the results and used on any generated result pages. 
     * 
     * @param dataset the dataset object to set.
     */
    public void setDataset(NemaDataset dataset);
    
    /**
     * Set the list of training datasets relevant to the experiment. 
     * 
     * @param trainingSets the list of training datasets relevant to the experiment.
     */
    public void setTrainingSets(List<NemaTrackList> trainingSets);
    
    /**
     * Set the list of test datasets relevant to the experiment.
     * 
     * @param testSets the list of test datasets relevant to the experiment.
     */
    public void setTestSets(List<NemaTrackList> testSets);
    
    /**
     * returns the list of training datasets relevant to the experiment. 
     * 
     * @return the list of training datasets relevant to the experiment.
     */
    public List<NemaTrackList> getTrainingSets();

    /**
     * Returns the list of test datasets relevant to the experiment.
     * 
     * @return the list of test datasets relevant to the experiment.
     */
    public List<NemaTrackList> getTestSets();

    /**
     * Initializes a NemaEvaluationResultSet Object with the experiment description, ready to be 
     * populated with results.
     * 
     * @return a NemaEvaluationResultSet Object.
     */
    public NemaEvaluationResultSet getEmptyEvaluationResultSet();
    
	/**
	 * @return a List of the String keys for the evaluation metrics expected to be present in 
	 * overall evaluation Objects.
	 */
	public List<String> getOverallEvalMetricsKeys();

	/**
	 * @return a List of the String keys for the evaluation metrics expected to be present in 
	 * per-fold evaluation Objects.
	 */
	public List<String> getFoldEvalMetricsKeys();

	/**
	 * @return a List of the String keys for the evaluation metrics and results that were evauated
	 * expected to be present in per-track evaluation Objects.
	 */
	public List<String> getTrackEvalMetricKeys();

    /**
     * Sets the <code>Collection</code> of groundtruth data to be used to 
     * evaluate results. In a multi-iteration experiment this ground-truth 
     * should provide a superset of ground-truth data to cover all iterations.
     * 
     * Where there are multiple groundtruths for a track these should be 
     * combined into a single <code>NemaData</code> with multiple values for the 
     * specified field. E.g. there might be 4 different annotations of the onset 
     * times from different users.
     *  
     * @param groundtruth A Collection of groundtruth Objects to use in 
     * evaluation.
     */
    public void setGroundTruth(Collection<NemaData> groundtruth);

    /**
     * Returns the <code>Collection</code> of groundtruth data to be used to 
     * evaluate results. In a multi-iteration experiment this ground-truth 
     * should provide a superset of ground-truth data to cover all iterations.
     * 
     * Where there are multiple groundtruths for a track these should be 
     * combined into a single <code>NemaData</code> with multiple values for the 
     * specified field. E.g. there might be 4 different annotations of the onset 
     * times from different users.
     *  
     * @return A Collection of groundtruth Objects that will be used in 
     * evaluation.
     */
	public Collection<NemaData> getGroundTruth();

	/**
     * Adds a set of results to the evaluation. For a single iteration experiment add a single
     * result per jobID. For a multi-iteration experiment add results per iteration (in order) for 
     * each system (it doesn't matter if results from different systems are shuffled together but 
     * the results for each system must be added in order).
     * 
     * @param systemName The name of the system (for printing on result output).
     * @param jobID Primary key for systems, can be a unique system name or URI for flow/config of
     * job that produced results. Used in encoded results.
     * @param results A <code>List</code> of <code>EvaluationDataObjects</code>.
     * @param fold The NemaTrackList Object defining the test set that the results relate to.
     */
    public void addResults(String systemName, String jobID, NemaTrackList fold, List<NemaData> results);
    
    /**
     * Adds a set of results to the evaluation. For a single iteration experiment add a single
     * result per jobID. For a multi-iteration experiment add results per iteration (in order) for 
     * each system (it doesn't matter if results from different systems are shuffled together but 
     * the results for each system must be added in order).
     * 
     * @param submissionDetails Details of the submission that apply to the 
     * system (for printing on result output).
     * @param jobID Primary key for systems, can be a unique system name or URI for flow/config of
     * job that produced results. Used in encoded results.
     * @param results A <code>List</code> of <code>EvaluationDataObjects</code>.
     * @param fold The NemaTrackList Object defining the test set that the results relate to.
     */
    public void addResults(NemaSubmission submissionDetails, String jobID, NemaTrackList fold, List<NemaData> results) throws IllegalArgumentException;
    
    /**
     * Returns the logger in use. Can be used to change the logging verbosity 
     * level with:
     * getLogger.setLevel(Level.WARNING).
     * @return the Logger that will be used for console output.
     */
    public Logger getLogger();
    
    /**
	 * Ensures that the log output is also sent to the specified PrintStream.
	 * @param stream The PrintStream to send the log output to.
	 */
	public void addLogDestination(PrintStream stream);
	
    /**
     * Perform the evaluation and block until the results are fully written to the output directory.
     * Also return a map encoding the evaluation results for each job in case they are needed for further processing.
     * 
     * @return a NemaEvaluationResultSet Object encoding the evaluation results for each system
     * overall, per-fold and per-track. The predictions returned for each track are also encoded.
     * @throws IllegalArgumentException Thrown if required metadata is not found, either in the ground-truth
     * data or in one of the system's results.
     */
    public NemaEvaluationResultSet evaluate() throws IllegalArgumentException, IOException;
    
    /**
     * Evaluates a single iteration/fold of the experiment and returns an Object 
     * representing the evaluation results.
     * 
     * @param jobID The jobID by which the results will be referred to.
     * @param testSet the testSet being evaluated.
     * @param theData The list of data Objects each representing a prediction 
     * about a track to be evaluated.
     * @return an Object representing the evaluation results.
     */
    public NemaData evaluateResultFold(String jobID, NemaTrackList testSet, List<NemaData> theData);
    
//    /**
//     * Render results from the Evaluator to a folder on disk.
//     * 
//     * @param results The results Object to render.
//     * @param outputDir The directory to render the results into.
//     * @throws IOException Thrown if an IOException occurs while rendering the 
//     * results.
//     */
//    public void renderResults(NemaEvaluationResultSet results, File outputDir) throws IOException;
//    
//    //TODO: remove these temporary methods when we get a java implementation of stats tests
//    
//    /**
//     * Sets a flag determining whether significance tests are performed in 
//     * matlab.
//     * @param performMatlabStatSigTests The flag to set.
//     */
//	public void setPerformMatlabStatSigTests(boolean performMatlabStatSigTests);
//
//	/**
//	 * Returns a flag determining whether significance tests are performed in 
//	 * matlab.
//	 * @return The flag value.
//	 */
//	public boolean getPerformMatlabStatSigTests();
//
//	/**
//	 * Sets the path to the matlab executable.
//	 * @param matlabPath The executable path.
//	 */
//	public void setMatlabPath(File matlabPath);
//
//	/**
//	 * Returns  the path to the matlab executable.
//	 * @return matlabPath The executable path.
//	 */
//	public File getMatlabPath();
}
