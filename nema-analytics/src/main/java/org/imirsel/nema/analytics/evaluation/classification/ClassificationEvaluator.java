/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.imirsel.nema.analytics.evaluation.classification;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.imirsel.nema.analytics.evaluation.*;
import org.imirsel.nema.model.*;
import org.imirsel.nema.model.util.PathAndTagCleaner;

/**
 * Classification evaluation.
 * 
 * @author kris.west@gmail.com
 * @since 0.1.0
 */
public class ClassificationEvaluator extends EvaluatorImpl{

	/** Command line harness usage statement. */
    public static final String USAGE = "args: taskID(int) taskName taskDescription datasetID(int) datasetName datasetDescription subjectMetadata /path/to/GT/file /path/to/output/dir [-h /path/to/hierarchy/file] /path/to/system1/results/dir system1Name ... /path/to/systemN/results/dir systemNName";
    
    private File hierarchyFile = null;
    private List<String[]> hierarchies = null;
    private List<String> hierachiesKey = null;
    private List<String> classNames = null;
    

    
    /**
	 * Constructor (no arg - task, dataset, output and working dirs, training
	 * and test sets must be set manually).
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public ClassificationEvaluator() {
		super();
	}
  
    protected void setupEvalMetrics() {
		this.trackEvalMetrics.clear();
		this.trackEvalMetrics.add(NemaDataConstants.CLASSIFICATION_ACCURACY);
		if(this.hierarchyFile != null){
			this.trackEvalMetrics.add(NemaDataConstants.CLASSIFICATION_DISCOUNTED_ACCURACY);
		}
		
		this.overallEvalMetrics.clear();
		this.overallEvalMetrics.add(NemaDataConstants.CLASSIFICATION_ACCURACY);
		this.overallEvalMetrics.add(NemaDataConstants.CLASSIFICATION_NORMALISED_ACCURACY);
		this.overallEvalMetrics.add(NemaDataConstants.CLASSIFICATION_CONFUSION_MATRIX_RAW);
		this.overallEvalMetrics.add(NemaDataConstants.CLASSIFICATION_CONFUSION_MATRIX_PERCENT);
		if(this.hierarchyFile != null){
			this.overallEvalMetrics.add(NemaDataConstants.CLASSIFICATION_DISCOUNTED_ACCURACY);
			this.overallEvalMetrics.add(NemaDataConstants.CLASSIFICATION_NORMALISED_DISCOUNTED_ACCURACY);
			this.overallEvalMetrics.add(NemaDataConstants.CLASSIFICATION_DISCOUNT_CONFUSION_VECTOR_RAW);
			this.overallEvalMetrics.add(NemaDataConstants.CLASSIFICATION_DISCOUNT_CONFUSION_VECTOR_PERCENT);
		}
		
		//same as overall metrics - single fold experiment format
		this.foldEvalMetrics = this.overallEvalMetrics;
	}
    
    private void standardizeGtClassnames(){
    	String type = this.getTask().getSubjectTrackMetadataName();
    	
    	//standardise class names
    	for(NemaData d:this.getGroundTruth()){
    		d.setMetadata(type, PathAndTagCleaner.cleanTag(d.getStringMetadata(type)));
    	}
    }
    
    /**
     * Initializes the class names list from the ground-truth.
     */
    private void initClassNames() throws IllegalArgumentException{
    	String type = this.getTask().getSubjectTrackMetadataName();
    	String aClass;
    	NemaData data;
    	Set<String> classes = new HashSet<String>();
    	
    	for (Iterator<NemaData> it = this.getGroundTruth().iterator(); it.hasNext();){
    		data = it.next();
    		aClass = data.getStringMetadata(type);
    		if(aClass == null){
    			throw new IllegalArgumentException("Ground-truth example " + data.getId() + " had no metadata of type '" + type + "'");
    		}else{
    			classes.add(aClass);
    		}
    	}
    	
    	classNames = new ArrayList<String>(classes);
    	Collections.sort(classNames);
    	String classesMsg = "Classes of type '" + type + "' found in ground-truth:";
    	for(Iterator<String> it = classNames.iterator();it.hasNext();){
    		classesMsg += "\n\t" + it.next();
    	}
    	getLogger().info(classesMsg);
    }
    

    /**
     * Initialises the class hierarchy data-structures if a hierarchy file is in use.
     */
    private void initHierachy() throws FileNotFoundException, IOException{
        //Initialise Hierarchy scoring stuff
    	String msg = "reading hierarchy file: " + hierarchyFile.getAbsolutePath() + "\n";
        this.hierarchies = new ArrayList<String[]>();
        this.hierachiesKey = new ArrayList<String>();
        BufferedReader textBuffer = null;
        String[] dataLine = {"init1", "init2"};
        msg += "Hierarchy data:\n";
        try {
            //use buffering
            //this implementation reads one line at a time
            textBuffer = new BufferedReader( new FileReader(hierarchyFile) );
            String line = null; //not declared within while loop
            while (( line = textBuffer.readLine()) != null) {
                line = line.trim();
                if(!line.equals("")){
                    dataLine = line.split("[\t]+");
//                    for (int i = 0; i < dataLine.length; i++){
//                        dataLine[i] = TagClassificationGroundTruthFileReader.cleanTag(dataLine[i]);
//                    }
                    this.hierarchies.add(dataLine);
                    this.hierachiesKey.add(dataLine[0]);

                    msg += "\t" + dataLine[0];
                    for (int i = 1; i < dataLine.length; i++){
                        msg += " -> " + dataLine[i];
                    }
                    msg += "\n";
                }
            }
            getLogger().info(msg);
        } finally {
            try {
                if (textBuffer!= null) {
                    //flush and close both "input" and its underlying FileReader
                    textBuffer.close();
                }
            } catch (IOException ex) {
            }
        }
    }
    
//    /** Parse command line arguments for the main method harness.
//     * 
//     * @param args Full command line arguments received by the JVM.
//     * @return An instantiated ClassificationEvaluator, based on the arguments, that is ready to run.
//     * @throws IllegalArgumentException Thrown if a results or ground-truth file is not in the expected format.
//     * @throws FileNotFoundException Thrown if a non-null hierarchy file is passed, but cannot be 
//     * found.
//     * @throws IOException Thrown if there is a problem reading a results or ground-truth file, unrelated to 
//     * format.
//     */
//    public static ClassificationEvaluator parseCommandLineArgs(String[] args) throws IllegalArgumentException, FileNotFoundException, IOException{
//        if (args.length < 10 ){
//            System.err.println("ERROR: Insufficient arguments!\n" + USAGE);
//            System.exit(1);
//        }
//        
//        //args: taskID(int) taskName taskDescription datasetID(int) datasetName datasetDescription subjectMetadataID subjectMetadataName /path/to/GT/file /path/to/output/dir [-h /path/to/hierarchy/file] /path/to/system1/results/dir system1Name ... /path/to/systemN/results/dir systemNName
//        NemaTask task = new NemaTask();
//        task.setId(Integer.parseInt(args[0]));
//        task.setName(args[1]);
//        task.setDescription(args[2]);
//        task.setDatasetId(Integer.parseInt(args[3]));
//        task.setSubjectTrackMetadataId(Integer.parseInt(args[6]));
//        task.setSubjectTrackMetadataName(args[7]);
//        
//        NemaDataset dataset = new NemaDataset();
//        dataset.setId(task.getDatasetId());
//        dataset.setName(args[4]);
//        dataset.setDescription(args[5]);
//        
//        File gtFile = new File(args[8]);
//        File workingDir = new File(args[9]);
//        File hierarchyFile = null;
//        String msg = "\n" + 
//        		"Task description:  \n" + task.toString() + 
//    			"Ground-truth file: " + gtFile.getAbsolutePath() + "\n" + 
//    			"Working directory: " + workingDir.getAbsolutePath() + "\n" + 
//    			"OutputDirectory:   " + workingDir.getAbsolutePath() + "\n";
//
//        if (args.length % 2 != 1){
//            System.err.println("WARNING: an even number of arguments was specified, one may have been ignored!\n" + USAGE);
//        }
//        
//        int startIdx = -1;
//        if (args[9].equalsIgnoreCase("-h")){
//            hierarchyFile = new File(args[10]);
//            msg += "Hierarchy file:    " + hierarchyFile.getAbsolutePath() + "\n";
//            startIdx = 11;
//        }else{startIdx = 9;
//            msg += "Hierarchy file:    null\n";
//        }
//        
//        ClassificationEvaluator eval = new ClassificationEvaluator(task, dataset, workingDir, workingDir, true, new File("matlab"), hierarchyFile);
//        eval.getLogger().info(msg);
//        
//        //reading ground-truth data
//        ClassificationTextFile reader = new ClassificationTextFile(task.getSubjectTrackMetadataName());
//        List<NemaData> gt = reader.readFile(gtFile);
//        eval.setGroundTruth(gt);
//        
//        msg = "Results to evaluate:\n";
//        for (int i = startIdx; i < args.length; i+=2) {
//            String systemName = args[i+1];
//            File resultsPath = new File(args[i]);
//            
//            List<List<NemaData>> results = reader.readDirectory(resultsPath,null);
//            for(Iterator<List<NemaData>> it = results.iterator();it.hasNext();){
//            	eval.addResults(systemName, systemName, it.next());
//            }
//            msg += systemName + ": " + resultsPath.getAbsolutePath() + ", read " + results.size() + " result files\n";
//        }
//        eval.getLogger().info(msg);
//        
//        return eval;
//    }
//    
//    /**
//     * Main method harness.
//     * 
//     * @param args Command line arguments that will be parsed by {@link #parseCommandLineArgs(String[] args)}.
//     */
//    public static void main(String[] args) {
//        
//        System.err.println("MIREX Classification evaluator\n" +
//                "\t\tby Kris West (kris.west@gmail.com");
//        System.err.println("");
//        
//        ClassificationEvaluator eval = null;
//		try {
//			eval = parseCommandLineArgs(args);
//			try{
//				eval.evaluate();
//			}catch(Exception e){
//				eval.getLogger().log(Level.SEVERE, "Exception occured while executing evaluate!",e);
//			}
//		} catch (Exception e) {
//			if (eval != null){
//				eval.getLogger().log(Level.SEVERE, "Exception occured while parsing command line arguments!",e);
//			}else{
//				Logger.getLogger(ClassificationEvaluator.class.getName()).log(Level.SEVERE, "Exception occured while parsing command line arguments!",e);
//			}
//		}
//        
//        System.err.println("---exit---");
//    }
    
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
        
        //standardize GT class names
        standardizeGtClassnames();
        
        //check that all systems have the same number of results
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
				// note we are using an overridden version of averageFoldMetrics as the confusion matrices have to be averaged for classification
				NemaData overall = averageFoldMetrics(jobId, foldEvals.values());
				jobIdToOverallEvaluation.put(jobId, overall);
			}
			
			/* Populate NemaEvaluationResultSet */
			for (Iterator<String> it = jobIDToName.keySet().iterator(); it.hasNext();) {
				jobId = it.next();
				getLogger().info("Populating results output for jobID: " + jobId);
				jobName = jobIDToName.get(jobId);
				results.addCompleteResultSet(jobId, jobName, jobIdToOverallEvaluation.get(jobId), jobIdToFoldEvaluation.get(jobId), jobIDToFoldResults.get(jobId));
			}
		}	
        
        return results;
    }

	@Override
	protected NemaData averageFoldMetrics(String jobId, Collection<NemaData> perFoldEvaluations) {
		int numClasses = this.classNames.size();
		int numFolds = this.testSets.size();
		boolean usingAHierarchy = this.hierarchyFile != null;
		NemaData aggregateEval = new NemaData(jobId);
		int[][][] confFolds = new int[numFolds][][];
		int f = 0;
		
        //Store class names
		aggregateEval.setMetadata(NemaDataConstants.CLASSIFICATION_EXPERIMENT_CLASSNAMES, classNames);
		
		if(perFoldEvaluations.size() != numFolds){
			throw new IllegalArgumentException("Job ID " + jobId + 
					" returned " + perFoldEvaluations.size() + " folds, expected " + numFolds);
		}
		
		for(Iterator<NemaData> foldIt = perFoldEvaluations.iterator(); foldIt.hasNext();){
			confFolds[f] = foldIt.next().get2dIntArrayMetadata(NemaDataConstants.CLASSIFICATION_CONFUSION_MATRIX_RAW);
			if(confFolds[f].length != numClasses){
				throw new IllegalArgumentException("Fold " + f + " for job ID " + jobId + 
						" returned a confusion matrix of dimension " + confFolds[f].length + ", expected " + numClasses);
			}
			for (int i = 0; i < confFolds[f].length; i++) {
				if(confFolds[f][i].length != numClasses){
					throw new IllegalArgumentException("Fold " + f + " for job ID " + jobId + 
							" returned a (non-square) confusion matrix of dimension " + confFolds[f][i].length + ", expected " + numClasses);
				}
			}
			f++;
		}
		int[][] confusionRaw = new int[numClasses][numClasses];
		double[][] confusionPercent = new double[numClasses][numClasses];
		
		int[] resultsPerClass = new int[numClasses];
		for(int i=0;i<numClasses;i++){
			resultsPerClass[i] = 0;
			for(int j=0;j<numClasses;j++){
				for(int f2=0;f2<numFolds;f2++){
					confusionRaw[j][i] += confFolds[f2][j][i];
				}
				resultsPerClass[i] += confusionRaw[j][i];
			}
			if(resultsPerClass[i] > 0){
				for(int j=0;j<numClasses;j++){
					confusionPercent[j][i] = (double)confusionRaw[j][i] / resultsPerClass[i];
				}
			}
		}
		
		aggregateEval.setMetadata(NemaDataConstants.CLASSIFICATION_CONFUSION_MATRIX_RAW, confusionRaw);
		aggregateEval.setMetadata(NemaDataConstants.CLASSIFICATION_CONFUSION_MATRIX_PERCENT, confusionPercent);
		
		//Calculate final accuracy as diagonal sum of confusion matrix divided by total number of examples
      
		double finalAccuracy = 0.0;
		double finalDiscountedAccuracy = 0.0;
		int finalSum = 0;
		for (int i=0;i<numClasses; i++) {
		    finalSum += resultsPerClass[i];
			finalAccuracy += confusionRaw[i][i];
		}
		finalAccuracy /= (double)finalSum;
		
		aggregateEval.setMetadata(NemaDataConstants.CLASSIFICATION_ACCURACY, finalAccuracy);
		
		//Calculate Normalized accuracy as mean of percentage confusion matrix diagonal
		double finalNormalisedAccuracy = 0.0;
		for (int i=0;i<numClasses; i++) {
		    finalNormalisedAccuracy += confusionPercent[i][i];
		}
		finalNormalisedAccuracy /= (double)numClasses;
		aggregateEval.setMetadata(NemaDataConstants.CLASSIFICATION_NORMALISED_ACCURACY, finalNormalisedAccuracy);
		
		//repeat for discounted stuff
		if(usingAHierarchy){
			double[][] discountFoldAccs = new double[numFolds][];
			f = 0;
			for(Iterator<NemaData> foldIt = perFoldEvaluations.iterator(); foldIt.hasNext();){
				discountFoldAccs[f++] = foldIt.next().getDoubleArrayMetadata(NemaDataConstants.CLASSIFICATION_DISCOUNT_CONFUSION_VECTOR_RAW);
			}
			double[] discountConfusionRaw = new double[numClasses];
			double[] discountConfusionPercent = new double[numClasses];
			for(int i=0;i<numClasses;i++){
				for(int f2=0;f2<numFolds;f2++){
					discountConfusionRaw[i] += discountFoldAccs[f2][i];
				}
				if(resultsPerClass[i] > 0){
					discountConfusionPercent[i] = discountConfusionRaw[i] / resultsPerClass[i];
				}
			}
			
			aggregateEval.setMetadata(NemaDataConstants.CLASSIFICATION_DISCOUNT_CONFUSION_VECTOR_RAW, discountConfusionRaw);
			aggregateEval.setMetadata(NemaDataConstants.CLASSIFICATION_DISCOUNT_CONFUSION_VECTOR_PERCENT, discountConfusionPercent);
			
			for (int i=0;i<numClasses; i++) {
		        finalDiscountedAccuracy += discountConfusionRaw[i];
		    }
		    finalDiscountedAccuracy /= finalSum;
		    aggregateEval.setMetadata(NemaDataConstants.CLASSIFICATION_DISCOUNTED_ACCURACY, finalDiscountedAccuracy);
		    
		    //Calculate Normalized accuracy as mean of percentage discounted confusion matrix diagonal
		    double finalNormalisedDiscountedAccuracy = 0.0;
		    for (int i=0;i<numClasses; i++) {
		        finalNormalisedDiscountedAccuracy += discountConfusionPercent[i];
		    }
		    finalNormalisedDiscountedAccuracy /= (double)numClasses;
		    aggregateEval.setMetadata(NemaDataConstants.CLASSIFICATION_NORMALISED_DISCOUNTED_ACCURACY, finalNormalisedDiscountedAccuracy);    
		}
		return aggregateEval;
	}

    
    public NemaData evaluateResultFold(String jobID, NemaTrackList testSet, List<NemaData> theData) {
    	String type = getTask().getSubjectTrackMetadataName();
        
    	//standardise class names
    	for(NemaData d:theData){
    		d.setMetadata(type, PathAndTagCleaner.cleanTag(d.getStringMetadata(type)));
    	}
    	
    	if(classNames == null){
    		initClassNames();
    	}        
    	
    	//count the number of examples returned and search for any missing tracks in the results returned for the fold
    	int numExamples = checkFoldResultsAreComplete(jobID, testSet, theData);
    	
    	int[] numExamplesPerClass = new int[classNames.size()];
    	List<NemaTrack> tracks = testSet.getTracks();
    	if (tracks == null){
    		for (Iterator<NemaData> iterator = theData.iterator(); iterator.hasNext();) {
    			numExamplesPerClass[classNames.indexOf(trackIDToGT.get(iterator.next().getId()).getStringMetadata(this.getTask().getSubjectTrackMetadataName()))]++;
			}
    	}else{
    		for (Iterator<NemaTrack> iterator = tracks.iterator(); iterator.hasNext();) {
				numExamplesPerClass[classNames.indexOf(trackIDToGT.get(iterator.next().getId()).getStringMetadata(this.getTask().getSubjectTrackMetadataName()))]++;
			}
    	}
    	
    	
    	boolean usingAHierarchy = hierarchyFile != null;
        
        int errors = 0;
        int[][] confusion = new int[classNames.size()][classNames.size()];
        double[] discountedConfusion = null;
        if(usingAHierarchy) {
            discountedConfusion = new double[classNames.size()];
        }
        
        NemaData outObj = new NemaData(jobID);
        
        NemaData data;
        NemaData gtData;
        String classString;
        int classification;
        String truthString;
        int truth;
        
        for(int x=0; x < theData.size(); x++) {
            //Do simple evaluation
        	data = theData.get(x);
        	classString = data.getStringMetadata(type);
            classification = classNames.indexOf(classString);
            gtData = trackIDToGT.get(data.getId());
            truthString = gtData.getStringMetadata(type);
            truth = classNames.indexOf(truthString);
            
            confusion[classification][truth]++;
            if(usingAHierarchy&&(truthString.equalsIgnoreCase(classString)))
            {
                discountedConfusion[truth] += 1.0;
            }
            if (!truthString.equals(classString)) {
                errors++;
                //set individual accuracy (1 or 0 - no hierarchy)
                data.setMetadata(NemaDataConstants.CLASSIFICATION_ACCURACY, 0.0);
                
                // do hierarchical discounting of confusions if necessary
                if(usingAHierarchy) {
                    ArrayList<String[]> trueHierachies = new ArrayList<String[]>(this.hierarchies);
                    ArrayList<String> trueKeys = new ArrayList<String>(this.hierachiesKey);
                    
                    double highestDiscountScore = 0.0;
                    
                    int trueIndex = trueKeys.indexOf(truthString);
                    while(trueIndex != -1)
                    {
                        double discountScore = 0.0;
                        ArrayList<String[]> classifiedHierachies = new ArrayList<String[]>(this.hierarchies);
                        ArrayList<String> classifiedKeys = new ArrayList<String>(this.hierachiesKey);
                        int classifiedIndex = classifiedKeys.indexOf(classString);
                    
                        trueKeys.remove(trueIndex);
                        String[] tempTrue = (String[])trueHierachies.remove(trueIndex);
                        ArrayList<String> truePath = new ArrayList<String>();
                        for(int i=0;i<tempTrue.length;i++) {
                            truePath.add(tempTrue[i]);
                        }
                        while(classifiedIndex != -1)
                        {
                            classifiedKeys.remove(classifiedIndex);
                            String[] tempClassification = (String[])classifiedHierachies.remove(classifiedIndex);
                            ArrayList<String> classifiedPath = new ArrayList<String>();
                            for(int i=0;i<tempClassification.length;i++) {
                                classifiedPath.add(tempClassification[i]);
                            }
                            for (int i=0;i<classifiedPath.size();i++) {
                                if (truePath.indexOf(classifiedPath.get(i)) != - 1) {
                                    discountScore += 1.0 / ((double)truePath.size());
                                }
                            }
                            
                            if (discountScore > highestDiscountScore){
                                highestDiscountScore = discountScore;
                            }
                            classifiedIndex = classifiedKeys.indexOf(classString);                    
                        }
                        trueIndex = trueKeys.indexOf(truthString);
                    }
                
                    discountedConfusion[truth] += highestDiscountScore;
                    //set individual accuracy (1 or 0)
                    data.setMetadata(NemaDataConstants.CLASSIFICATION_DISCOUNTED_ACCURACY, highestDiscountScore);
                }
            }else{//correct classification
            	//set individual accuracy (1 or 0)
            	data.setMetadata(NemaDataConstants.CLASSIFICATION_ACCURACY, 1.0);
            	if(usingAHierarchy){
            		data.setMetadata(NemaDataConstants.CLASSIFICATION_DISCOUNTED_ACCURACY, 1.0);
            	}
            }
        }
        
        //Store class names
        outObj.setMetadata(NemaDataConstants.CLASSIFICATION_EXPERIMENT_CLASSNAMES, classNames);
        
        //store raw confusion matrices
        outObj.setMetadata(NemaDataConstants.CLASSIFICATION_CONFUSION_MATRIX_RAW, confusion);
        //If necessary, store discounted confusion matrices
        if(usingAHierarchy) {
        	outObj.setMetadata(NemaDataConstants.CLASSIFICATION_DISCOUNT_CONFUSION_VECTOR_RAW, discountedConfusion);
        }
        
        //calculate percentage confusion matrix and, if necessary, discounted confusion matrix for this iteration
        double[][] percentConfusion = new double[classNames.size()][classNames.size()];
        double[] percentDiscountedConfusion = null;
        if (usingAHierarchy) {
            percentDiscountedConfusion = new double[classNames.size()];
        }
        for(int y=0; y<classNames.size(); y++) {
            if(numExamplesPerClass[y] > 0){
	            if(usingAHierarchy) {
	                percentDiscountedConfusion[y] = discountedConfusion[y] / (double)numExamplesPerClass[y];
	            }
	            for(int x=0; x<classNames.size(); x++) {
	                percentConfusion[x][y] = (double)confusion[x][y] / (double)numExamplesPerClass[y];
	            }
            }
        }
        
        //store percentage confusion matrices
        outObj.setMetadata(NemaDataConstants.CLASSIFICATION_CONFUSION_MATRIX_PERCENT, percentConfusion);
        //If necessary, store discounted confusion matrices
        if(usingAHierarchy) {
        	outObj.setMetadata(NemaDataConstants.CLASSIFICATION_DISCOUNT_CONFUSION_VECTOR_PERCENT, percentDiscountedConfusion);
        }
        
        //Calculate accuracy as diagonal sum of confusion matrix divided by total number of examples
        double Accuracy = 0.0;//(double)(theSignalsLength - errors) / (double)(theSignalsLength);
        double DiscountedAccuracy = 0.0;
        for (int i=0;i<classNames.size(); i++) {
            Accuracy += confusion[i][i];
        }
        Accuracy /= (double)numExamples;
        outObj.setMetadata(NemaDataConstants.CLASSIFICATION_ACCURACY, Accuracy);
        if (usingAHierarchy) {
            //Calculate accuracy as diagonal sum of discounted confusion matrix divided by total number of examples
            for (int i=0;i<classNames.size(); i++) {
                DiscountedAccuracy += discountedConfusion[i];
            }
            DiscountedAccuracy /= (double)numExamples;
            outObj.setMetadata(NemaDataConstants.CLASSIFICATION_DISCOUNTED_ACCURACY, DiscountedAccuracy);
        }
        
        //Calculate Normalized accuracy as mean of percentage confusion matrix diagonal
        double NormalisedAccuracy = 0.0;
        double NormalisedDiscountedAccuracy = 0.0;
        for (int i=0;i<classNames.size(); i++) {
            NormalisedAccuracy += percentConfusion[i][i];
        }
        NormalisedAccuracy /= classNames.size();
        outObj.setMetadata(NemaDataConstants.CLASSIFICATION_NORMALISED_ACCURACY, NormalisedAccuracy);
        
        if (usingAHierarchy) {
            //Calculate Normalized accuracy as mean of percentage discounted confusion matrix diagonal
            for (int i=0;i<classNames.size(); i++) {
                NormalisedDiscountedAccuracy += percentDiscountedConfusion[i];
            }
            NormalisedDiscountedAccuracy /= (double)classNames.size();
            outObj.setMetadata(NemaDataConstants.CLASSIFICATION_NORMALISED_DISCOUNTED_ACCURACY, NormalisedDiscountedAccuracy);
        }

        return outObj;
    }

	public void setHierarchyFile(File hierarchyFile) {
		this.hierarchyFile = hierarchyFile;
	}

	public File getHierarchyFile() {
		return hierarchyFile;
	}
	
}
