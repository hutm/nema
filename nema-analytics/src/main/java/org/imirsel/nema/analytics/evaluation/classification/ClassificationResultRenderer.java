package org.imirsel.nema.analytics.evaluation.classification;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.imirsel.nema.analytics.evaluation.FriedmansAnovaTkHsd;
import org.imirsel.nema.analytics.evaluation.ResultRendererImpl;
import org.imirsel.nema.analytics.evaluation.WriteCsvResultFiles;
import org.imirsel.nema.analytics.evaluation.resultpages.FileListItem;
import org.imirsel.nema.analytics.evaluation.resultpages.ImageItem;
import org.imirsel.nema.analytics.evaluation.resultpages.Page;
import org.imirsel.nema.analytics.evaluation.resultpages.PageItem;
import org.imirsel.nema.analytics.evaluation.resultpages.ProtovisConfusionMatrixPlotItem;
import org.imirsel.nema.analytics.evaluation.resultpages.Table;
import org.imirsel.nema.analytics.evaluation.resultpages.TableItem;
import org.imirsel.nema.model.NemaData;
import org.imirsel.nema.model.NemaDataConstants;
import org.imirsel.nema.model.NemaDataset;
import org.imirsel.nema.model.NemaEvaluationResultSet;
import org.imirsel.nema.model.NemaTask;
import org.imirsel.nema.model.NemaTrackList;
import org.imirsel.nema.model.util.IOUtil;

public class ClassificationResultRenderer extends ResultRendererImpl {

    private static final DecimalFormat dec = new DecimalFormat("0.00");
    private static final String BIG_DIVIDER =    "================================================================================\n";
    private static final String SMALL_DIVIDER = "--------------------------------------------------------------------------------\n";
    private static final int COL_WIDTH = 7;
    
    public boolean usingAHierarchy(NemaEvaluationResultSet results){
    	return results.getOverallEvalMetricsKeys().contains(NemaDataConstants.CLASSIFICATION_DISCOUNTED_ACCURACY);
    }
    
    @SuppressWarnings("unchecked")
	public void renderResults(NemaEvaluationResultSet results) throws IOException {

    	boolean usingAHierarchy = usingAHierarchy(results);
    	
		String jobId;
		int numJobs = results.getJobIds().size();
		
		/* Make per system result directories */
		Map<String, File> jobIDToResultDir = new HashMap<String, File>();
		for (Iterator<String> it = results.getJobIds().iterator(); it.hasNext();) {
			jobId = it.next();
			
			/* Make a sub-directory for the systems results */
			File sysDir = new File(outputDir.getAbsolutePath() + File.separator + jobId);
			sysDir.mkdirs();
			jobIDToResultDir.put(jobId, sysDir);
		}
		
		/* Write out leaderboard CSV file */
		getLogger().info("Writing out leaderboard CSV...");
		File leaderboardCSV = this.writeLeaderBoardCSVFile(NemaDataConstants.CLASSIFICATION_ACCURACY, results, false);
		
//		//plot confusion matrices for each fold
//		getLogger().info("Plotting confusion matrices for each fold for each job");
//		Map<String,File[]> jobIDToFoldConfFileList = new HashMap<String,File[]>(numJobs);
//		for(Iterator<String> it = results.getJobIds().iterator(); it.hasNext();){
//			jobId = it.next();
//			Map<NemaTrackList,NemaData> evalList = results.getPerFoldEvaluation(jobId);
//			File[] foldConfFiles = plotConfusionMatricesForAllFolds(results,jobId, evalList);
//			jobIDToFoldConfFileList.put(jobId,foldConfFiles);
//		}
//		
//		//plot aggregate confusion for each job
//		getLogger().info("Plotting overall confusion matrices for each job");
//		Map<String,File> jobIDToOverallConfFile = new HashMap<String,File>(numJobs);
//		for(Iterator<String> it = results.getJobIds().iterator(); it.hasNext();){
//			jobId = it.next();
//			NemaData aggregateEval = results.getOverallEvaluation(jobId);
//			File overallConfFile = plotAggregatedConfusionForJob(results,jobId, aggregateEval);
//		    jobIDToOverallConfFile.put(jobId, overallConfFile);
//		}
		
		//retrieve class names from eval data
		jobId = results.getJobIds().iterator().next();
		NemaData aggregateEval = results.getOverallEvaluation(jobId);
		List<String> classNames = (List<String>)aggregateEval.getMetadata(NemaDataConstants.CLASSIFICATION_EXPERIMENT_CLASSNAMES);
		
		//write out CSV results files
		getLogger().info("Writing out CSV result files over whole task...");
		File perClassCSV = new File(outputDir.getAbsolutePath()+ File.separator + "PerClassResults.csv");
		WriteCsvResultFiles.writeTableToCsv(WriteCsvResultFiles.prepTableDataOverClassArrays(results.getJobIdToOverallEvaluation(),results.getJobIdToJobName(),classNames,NemaDataConstants.CLASSIFICATION_CONFUSION_MATRIX_PERCENT),perClassCSV);
		
		File perFoldCSV = new File(outputDir.getAbsolutePath() + File.separator + "PerFoldResults.csv");
		WriteCsvResultFiles.writeTableToCsv(WriteCsvResultFiles.prepTableDataOverFoldsAndSystems(results.getTestSetTrackLists(), results.getJobIdToPerFoldEvaluation(), results.getJobIdToJobName(),NemaDataConstants.CLASSIFICATION_ACCURACY),perFoldCSV);
		
		//write out discounted results summary CSVs
		File discountedPerClassCSV = null;
		File discountedPerFoldCSV = null;
		if (results.getOverallEvalMetricsKeys().contains(NemaDataConstants.CLASSIFICATION_DISCOUNTED_ACCURACY)){
		    discountedPerClassCSV = new File(outputDir.getAbsolutePath() + File.separator + "DiscountedPerClassResults.csv");
		    WriteCsvResultFiles.writeTableToCsv(WriteCsvResultFiles.prepTableDataOverClassArrays(results.getJobIdToOverallEvaluation(),results.getJobIdToJobName(),classNames,NemaDataConstants.CLASSIFICATION_DISCOUNT_CONFUSION_VECTOR_PERCENT),discountedPerClassCSV);
		    discountedPerFoldCSV = new File(outputDir.getAbsolutePath() + File.separator + "DiscountedPerFoldResults.csv");
		    WriteCsvResultFiles.writeTableToCsv(WriteCsvResultFiles.prepTableDataOverFoldsAndSystems(results.getTestSetTrackLists(), results.getJobIdToPerFoldEvaluation(),results.getJobIdToJobName(),NemaDataConstants.CLASSIFICATION_DISCOUNTED_ACCURACY),discountedPerFoldCSV);
		}
		
		//write out results summary CSV
		File summaryCSV = new File(outputDir.getAbsolutePath() + File.separator + "summaryResults.csv");
		List<String> metrics = new ArrayList<String>();
		metrics.add(NemaDataConstants.CLASSIFICATION_ACCURACY);
		metrics.add(NemaDataConstants.CLASSIFICATION_NORMALISED_ACCURACY);
		if (results.getOverallEvalMetricsKeys().contains(NemaDataConstants.CLASSIFICATION_DISCOUNTED_ACCURACY)){
			metrics.add(NemaDataConstants.CLASSIFICATION_DISCOUNTED_ACCURACY);
			metrics.add(NemaDataConstants.CLASSIFICATION_NORMALISED_DISCOUNTED_ACCURACY);
		}
		WriteCsvResultFiles.writeTableToCsv(WriteCsvResultFiles.prepSummaryTable(results.getJobIdToOverallEvaluation(),results.getJobIdToJobName(),metrics),summaryCSV);
		
		
		//perform statistical tests
		File friedmanClassTablePNG = null;
		File friedmanClassTable = null;
		File friedmanFoldTablePNG = null;
		File friedmanFoldTable = null;
		File friedmanDiscountClassTablePNG = null;
		File friedmanDiscountClassTable = null;
		File friedmanDiscountFoldTablePNG = null;
		File friedmanDiscountFoldTable = null;
		if (getPerformMatlabStatSigTests() && results.getJobIds().size() > 1){
		    getLogger().info("Performing Friedman's tests...");
		
		    File[] tmp = FriedmansAnovaTkHsd.performFriedman(outputDir, perClassCSV, 0, 1, 1, numJobs, getMatlabPath());
		    friedmanClassTablePNG = tmp[0];
		    friedmanClassTable = tmp[1];
		
		    tmp = FriedmansAnovaTkHsd.performFriedman(outputDir, perFoldCSV, 0, 1, 1, numJobs, getMatlabPath());
		    friedmanFoldTablePNG = tmp[0];
		    friedmanFoldTable = tmp[1];
		
		    if (usingAHierarchy){
		        tmp = FriedmansAnovaTkHsd.performFriedman(outputDir, discountedPerClassCSV, 0, 1, 1, numJobs, getMatlabPath());
		        friedmanDiscountClassTablePNG = tmp[0];
		        friedmanDiscountClassTable = tmp[1];
		        
		        tmp = FriedmansAnovaTkHsd.performFriedman(outputDir, discountedPerFoldCSV, 0, 1, 1, numJobs, getMatlabPath());
		        friedmanDiscountFoldTablePNG = tmp[0];
		        friedmanDiscountFoldTable = tmp[1];
		    }
		}
		
		//write text reports
		getLogger().info("Writing text evaluation reports...");
		Map<String,File> jobIDToReportFile = new HashMap<String,File>(numJobs);
		for (Iterator<String> it = results.getJobIdToJobName().keySet().iterator();it.hasNext();) {
			jobId = it.next();
			File reportFile = new File(jobIDToResultDir.get(jobId).getAbsolutePath() + File.separator + "report.txt");
			writeSystemTextReport(results, jobId, results.getJobIdToJobName().get(jobId), usingAHierarchy, reportFile);
			jobIDToReportFile.put(jobId, reportFile);
		}
		
		//create tarballs of individual result dirs
		getLogger().info("Preparing evaluation data tarballs...");
		Map<String,File> jobIDToTgz = new HashMap<String,File>(results.getJobIdToJobName().size());
		for (Iterator<String> it = results.getJobIdToJobName().keySet().iterator();it.hasNext();) {
			jobId = it.next();
			jobIDToTgz.put(jobId, IOUtil.tarAndGzip(new File(outputDir.getAbsolutePath() + File.separator + jobId)));
		}
		
		
		//write result HTML pages
		getLogger().info("Creating result HTML files...");
		writeHtmlResultPages(usingAHierarchy, results, classNames,
				perClassCSV, perFoldCSV,
				discountedPerClassCSV, discountedPerFoldCSV,
				friedmanClassTablePNG, friedmanClassTable,
				friedmanFoldTablePNG, friedmanFoldTable,
				friedmanDiscountClassTablePNG, friedmanDiscountClassTable,
				friedmanDiscountFoldTablePNG, friedmanDiscountFoldTable,
				jobIDToTgz, outputDir);
    }
    

    @Override
	public void renderAnalysis(NemaEvaluationResultSet results) throws IOException {
//		/* Write analysis HTML pages */
//		getLogger().info("Creating result HTML files...");
//		writeHtmlAnalysisPages(results, outputDir);
//		
//		getLogger().info("Done.");
    	
    	throw new UnsupportedOperationException("No analysis result rendering facilities are implemented for classification!");
	}
    
	private void writeHtmlResultPages(boolean usingAHierarchy,
			NemaEvaluationResultSet results, List<String> classNames,
			File perClassCSV,
			File perFoldCSV, File discountedPerClassCSV,
			File discountedPerFoldCSV, File friedmanClassTablePNG,
			File friedmanClassTable, File friedmanFoldTablePNG,
			File friedmanFoldTable, File friedmanDiscountClassTablePNG,
			File friedmanDiscountClassTable, File friedmanDiscountFoldTablePNG,
			File friedmanDiscountFoldTable, Map<String, File> jobIDToTgz,
			File outputDir) {
		
		int numJobs = results.getJobIds().size();
		boolean performStatSigTests = (numJobs > 1) && this.getPerformMatlabStatSigTests();
		
		List<Page> resultPages = new ArrayList<Page>();
        List<PageItem> items;
        Page aPage;

		TableItem legendTable = createLegendTable(results);
		
		//do intro page to describe task
        {
        	resultPages.add(createIntroHtmlPage(results,legendTable));
        }
        
        //do summary page
        {
	        items = new ArrayList<PageItem>();
	        items.add(legendTable);

	        List<String> metrics = new ArrayList<String>();
			metrics.add(NemaDataConstants.CLASSIFICATION_ACCURACY);
			metrics.add(NemaDataConstants.CLASSIFICATION_NORMALISED_ACCURACY);
			if (results.getOverallEvalMetricsKeys().contains(NemaDataConstants.CLASSIFICATION_DISCOUNTED_ACCURACY)){
				metrics.add(NemaDataConstants.CLASSIFICATION_DISCOUNTED_ACCURACY);
				metrics.add(NemaDataConstants.CLASSIFICATION_NORMALISED_DISCOUNTED_ACCURACY);
			}
	        Table summaryTable = WriteCsvResultFiles.prepSummaryTable(results.getJobIdToOverallEvaluation(),results.getJobIdToJobName(),metrics);
	        items.add(new TableItem("summary_results", "Summary Results", summaryTable.getColHeaders(), summaryTable.getRows()));
	        
	        aPage = new Page("summary", "Summary", items, false);
	        resultPages.add(aPage);
        }

        //do per class page
        {
            items = new ArrayList<PageItem>();
            items.add(legendTable);

            Table perClassTable = WriteCsvResultFiles.prepTableDataOverClassArrays(results.getJobIdToOverallEvaluation(),results.getJobIdToJobName(),classNames,NemaDataConstants.CLASSIFICATION_CONFUSION_MATRIX_PERCENT);
            items.add(new TableItem("acc_class", "Accuracy per Class", perClassTable.getColHeaders(), perClassTable.getRows()));
            if (usingAHierarchy){
                Table perDiscClassTable = WriteCsvResultFiles.prepTableDataOverClassArrays(results.getJobIdToOverallEvaluation(),results.getJobIdToJobName(),classNames,NemaDataConstants.CLASSIFICATION_DISCOUNT_CONFUSION_VECTOR_PERCENT);
                items.add(new TableItem("disc_acc_class", "Discounted Accuracy per Class", perDiscClassTable.getColHeaders(), perDiscClassTable.getRows()));
            }
            aPage = new Page("acc_per_class", "Accuracy per Class", items, true);
            resultPages.add(aPage);
        }

        //do per fold page
        {
            items = new ArrayList<PageItem>();
            items.add(legendTable);

            Table perFoldTable = WriteCsvResultFiles.prepTableDataOverFoldsAndSystems(results.getTestSetTrackLists(), results.getJobIdToPerFoldEvaluation(),results.getJobIdToJobName(),NemaDataConstants.CLASSIFICATION_ACCURACY);
            items.add(new TableItem("acc_fold", "Accuracy per Fold", perFoldTable.getColHeaders(), perFoldTable.getRows()));
            if (usingAHierarchy){
                Table perDiscFoldTable = WriteCsvResultFiles.prepTableDataOverFoldsAndSystems(results.getTestSetTrackLists(), results.getJobIdToPerFoldEvaluation(),results.getJobIdToJobName(),NemaDataConstants.CLASSIFICATION_DISCOUNTED_ACCURACY);
                items.add(new TableItem("disc_acc_fold", "Discounted Accuracy per Fold", perDiscFoldTable.getColHeaders(), perDiscFoldTable.getRows()));
            }
            
            aPage = new Page("acc_per_fold", "Accuracy per Fold", items, true);
            resultPages.add(aPage);
        }
        
        //do significance tests
        if (performStatSigTests){
            items = new ArrayList<PageItem>();
            items.add(legendTable);

            items.add(new ImageItem("friedmanClassTablePNG", "Accuracy Per Class: Friedman's ANOVA w/ Tukey Kramer HSD", IOUtil.makeRelative(friedmanClassTablePNG, outputDir)));
            items.add(new ImageItem("friedmanFoldTablePNG", "Accuracy Per Fold: Friedman's ANOVA w/ Tukey Kramer HSD", IOUtil.makeRelative(friedmanFoldTablePNG, outputDir)));
            if(friedmanDiscountClassTable != null){
                items.add(new ImageItem("friedmanDiscountClassTablePNG", "Discounted Accuracy Per Class: Friedman's ANOVA w/ Tukey Kramer HSD", IOUtil.makeRelative(friedmanDiscountClassTablePNG, outputDir)));
            }
            if(friedmanDiscountFoldTable != null){
                items.add(new ImageItem("friedmanDiscountFoldTablePNG", "Accuracy Per Fold: Friedman's ANOVA w/ Tukey Kramer HSD", IOUtil.makeRelative(friedmanDiscountFoldTablePNG, outputDir)));
            }
            aPage = new Page("sig_tests", "Significance Tests", items, true);
            resultPages.add(aPage);
        }

      //do overall confusion matrices
        List<String> sortedJobIDs = new ArrayList<String>(results.getJobIds());
        Collections.sort(sortedJobIDs);
        {
            items = new ArrayList<PageItem>();
            items.add(legendTable);

	        for (int i = 0; i < numJobs; i++){
	        	String jobId = sortedJobIDs.get(i);
                items.add(plotAggregatedConfusionForJob(results, jobId, results.getOverallEvaluation(jobId)));
            }
	        
            aPage = new Page("overall_confusion", "Overall Confusion Matrices", items, true);
            resultPages.add(aPage);
        }
        
      //do per-fold confusion matrices
        {
            items = new ArrayList<PageItem>();
            items.add(legendTable);

	        //add per-fold confusion matrices
	        for (int i = 0; i < numJobs; i++){
	        	String jobId = sortedJobIDs.get(i);
                items.addAll(plotConfusionMatricesForAllFolds(results, jobId, results.getPerFoldEvaluation(jobId)));
            }
	        
            aPage = new Page("per_fold_confusion", "Per-fold Confusion Matrices", items, true);
            resultPages.add(aPage);
        }

        //do files page
        {
            items = new ArrayList<PageItem>();

            //CSVs
            List<String> CSVPaths = new ArrayList<String>(4);
            CSVPaths.add(IOUtil.makeRelative(perClassCSV,outputDir));
            CSVPaths.add(IOUtil.makeRelative(perFoldCSV,outputDir));
            if (usingAHierarchy){
                CSVPaths.add(IOUtil.makeRelative(discountedPerClassCSV,outputDir));
                CSVPaths.add(IOUtil.makeRelative(discountedPerFoldCSV,outputDir));
            }
            items.add(new FileListItem("dataCSVs", "CSV result files", CSVPaths));

            //Friedman's tables and plots
            if (performStatSigTests){
                //Friedmans tables
                List<String> sigCSVPaths = new ArrayList<String>(4);
                sigCSVPaths.add(IOUtil.makeRelative(friedmanClassTable, outputDir));
                sigCSVPaths.add(IOUtil.makeRelative(friedmanFoldTable, outputDir));
                if(friedmanDiscountClassTable != null){
                    sigCSVPaths.add(IOUtil.makeRelative(friedmanDiscountClassTable, outputDir));
                }
                if(friedmanDiscountFoldTable != null){
                    sigCSVPaths.add(IOUtil.makeRelative(friedmanDiscountFoldTable, outputDir));
                }
                items.add(new FileListItem("sigCSVs", "Significance test CSVs", sigCSVPaths));

                //Friedmans plots
                List<String> sigPNGPaths = new ArrayList<String>(4);
                sigPNGPaths.add(IOUtil.makeRelative(friedmanClassTablePNG, outputDir));
                sigPNGPaths.add(IOUtil.makeRelative(friedmanFoldTablePNG, outputDir));
                if(friedmanDiscountClassTable != null){
                    sigPNGPaths.add(IOUtil.makeRelative(friedmanDiscountClassTablePNG, outputDir));
                }
                if(friedmanDiscountFoldTable != null){
                    sigPNGPaths.add(IOUtil.makeRelative(friedmanDiscountFoldTablePNG, outputDir));
                }
                items.add(new FileListItem("sigPNGs", "Significance test plots", sigPNGPaths));
            }

            //System Tarballs
            List<String> tarballPaths = new ArrayList<String>(numJobs);
            for (int i = 0; i < numJobs; i++){
                tarballPaths.add(IOUtil.makeRelative(jobIDToTgz.get(sortedJobIDs.get(i)),outputDir));
            }
            items.add(new FileListItem("tarballs", "Per algorithm evaluation tarball", tarballPaths));
            aPage = new Page("files", "Raw data files", items, true);
            resultPages.add(aPage);
        }

        Page.writeResultPages(results.getTask().getName(), outputDir, resultPages);
	}

	private PageItem plotAggregatedConfusionForJob(NemaEvaluationResultSet results, String jobID, NemaData aggregateEval) {
		return plotConfusionMatrix(results, jobID, aggregateEval, " - overall");
	}

	private List<PageItem> plotConfusionMatricesForAllFolds(NemaEvaluationResultSet results, String jobID, Map<NemaTrackList, NemaData> evals) {
		List<PageItem> foldPlots = new ArrayList<PageItem>();
		for(Iterator<NemaTrackList> foldIt = results.getTestSetTrackLists().iterator();foldIt.hasNext();){
			NemaTrackList testSet = foldIt.next();
			NemaData eval = evals.get(testSet);
			foldPlots.add(plotConfusionMatrix(results, jobID, eval, " - fold " + testSet.getFoldNumber()));
		}
		return foldPlots;
	}

	@SuppressWarnings("unchecked")
	private PageItem plotConfusionMatrix(NemaEvaluationResultSet results, String jobId, NemaData eval, String titleComp) {
		double[][] confusion = eval.get2dDoubleArrayMetadata(NemaDataConstants.CLASSIFICATION_CONFUSION_MATRIX_PERCENT);
		List<String> classNames = (List<String>)eval.getMetadata(NemaDataConstants.CLASSIFICATION_EXPERIMENT_CLASSNAMES);
		
		String name = results.getJobName(jobId) + "_" + titleComp + "_conf_matrix";
		String caption = results.getJobName(jobId) + " " + titleComp + " confusion matrix";
		
		return new ProtovisConfusionMatrixPlotItem(name, caption, classNames, confusion);
	}
	
	/**
     * Writes a textual evaluation report on the results of one system to an UTF-8 text file. Includes 
     * the confusion matrices, accuracy, discounted accuracy and normalised versions of each for each 
     * iteration of the experiment and overall.
     * 
     * @param aggregateEval An Object representing the combined evaluation of all iterations.
     * @param testSets A list of the NemaTrackList Objects representing the test sets.
     * @param foldEvals A map of objects representing the evaluation of each fold/iteration of the 
     * experiment.
     * @param classNames An ordered list of the class names used in the experiment.
     * @param jobID The jobID of the system being evaluated.
     * @param jobName The name of the job being evaluated.
     * @param usingAHierarchy Flag indicating whether the evaluation used a hierarchy to discount confusions
     * (meaning we need to retrieve and report on the extra discounted results).
     * @param outputFile The File to write the report to.
     * @throws IOException Thrown if there is a problem writing to the report file.
     * @throws FileNotFoundException Thrown if the report file cannot be created.
     */
    @SuppressWarnings("unchecked")
	public void writeSystemTextReport(NemaEvaluationResultSet results, String jobId, String jobName, boolean usingAHierarchy, File outputFile) throws IOException, FileNotFoundException{
        
        NemaTask task = results.getTask();
        NemaDataset dataset = results.getDataset();
        List<NemaTrackList> testSets = results.getTestSetTrackLists();
        NemaData aggregateEval = results.getOverallEvaluation(jobId);
        Map<NemaTrackList,NemaData> foldEvals = results.getPerFoldEvaluation(jobId);
        List<String> classNames = (List<String>)aggregateEval.getMetadata(NemaDataConstants.CLASSIFICATION_EXPERIMENT_CLASSNAMES);
        
    	//Write output for each fold
    	String bufferString = BIG_DIVIDER + "Classification Evaluation Report\n";
    	bufferString += "Job ID:                  " + jobId + "\n";
    	bufferString += "Job Name:                " + jobName + "\n";
    	bufferString += "Number of iterations:    " + foldEvals.size() + "\n";
    	bufferString += "Task ID:                 " + task.getId() + "\n";
    	bufferString += "Task Name:               " + task.getName() + "\n";
    	bufferString += "Task Description:        " + task.getDescription() + "\n";
    	bufferString += "Metadata predicted id:   " + task.getSubjectTrackMetadataId() + "\n";
    	bufferString += "Metadata predicted name: " + task.getSubjectTrackMetadataName() + "\n";
    	bufferString += "Dataset ID:              " + dataset.getId() + "\n";
    	bufferString += "Dataset Name:            " + dataset.getName() + "\n";
    	bufferString += "Dataset Description:     " + dataset.getDescription() + "\n\n";
    	bufferString += SMALL_DIVIDER;
    	
	    for(Iterator<NemaTrackList> foldIt = testSets.iterator();foldIt.hasNext();){
	    	NemaTrackList fold = foldIt.next();
	    	NemaData foldData = foldEvals.get(fold);
	    	
	    	bufferString += "Fold " + fold.getFoldNumber() + " (" + fold.getId() + ")\n";
		    bufferString += "Accuracy: " + dec.format(foldData.getDoubleMetadata(NemaDataConstants.CLASSIFICATION_ACCURACY) * 100) + "%\n";
		    bufferString += "Accuracy (normalised for class sizes): " + dec.format(foldData.getDoubleMetadata(NemaDataConstants.CLASSIFICATION_NORMALISED_ACCURACY) * 100) + "%\n";
	    	
		    if(usingAHierarchy) {
		        bufferString += "Hierachically Discounted Accuracy: " + dec.format(foldData.getDoubleMetadata(NemaDataConstants.CLASSIFICATION_DISCOUNTED_ACCURACY) * 100) + "%\n";
		        bufferString += "Hierachically Discounted Accuracy (normalised for class sizes): " + dec.format(foldData.getDoubleMetadata(NemaDataConstants.CLASSIFICATION_NORMALISED_DISCOUNTED_ACCURACY) * 100) + "%\n";
		    }
		    
		    bufferString += "Raw Confusion Matrix:\n";
		    bufferString += writeIntConfusionMatrix(foldData.get2dIntArrayMetadata(NemaDataConstants.CLASSIFICATION_CONFUSION_MATRIX_RAW), classNames);
		    bufferString += "\nConfusion Matrix percentage:\n";
		    bufferString += writePercentageConfusionMatrix(foldData.get2dDoubleArrayMetadata(NemaDataConstants.CLASSIFICATION_CONFUSION_MATRIX_PERCENT), classNames);
		    bufferString += writeMatrixKey(classNames);
		    
		    if (usingAHierarchy)
		    {
		        bufferString += "\nHierachically Discounted Confusion Vector:\n";
		        bufferString += writeDoubleConfusionVector(foldData.getDoubleArrayMetadata(NemaDataConstants.CLASSIFICATION_DISCOUNT_CONFUSION_VECTOR_RAW), classNames);
		        bufferString += "\nHierachically Discounted Confusion Matrix percentage:\n";
		        bufferString += writePercentageConfusionVector(foldData.getDoubleArrayMetadata(NemaDataConstants.CLASSIFICATION_DISCOUNT_CONFUSION_VECTOR_PERCENT), classNames);
		    }
		    if(foldIt.hasNext()){
		    	bufferString += SMALL_DIVIDER;
		    }
	    }
	    
	    bufferString += "\n" + BIG_DIVIDER;
	    bufferString += "Overall Evaluation\n";
	    bufferString += "Accuracy: " + dec.format(aggregateEval.getDoubleMetadata(NemaDataConstants.CLASSIFICATION_ACCURACY) * 100) + "%\n";
	    bufferString += "Accuracy (normalised for class sizes): " + dec.format(aggregateEval.getDoubleMetadata(NemaDataConstants.CLASSIFICATION_NORMALISED_ACCURACY) * 100) + "%\n";
    	
	    if(usingAHierarchy) {
	        bufferString += "Hierachically Discounted Accuracy: " + dec.format(aggregateEval.getDoubleMetadata(NemaDataConstants.CLASSIFICATION_DISCOUNTED_ACCURACY) * 100) + "%\n";
	        bufferString += "Hierachically Discounted Accuracy (normalised for class sizes): " + dec.format(aggregateEval.getDoubleMetadata(NemaDataConstants.CLASSIFICATION_NORMALISED_DISCOUNTED_ACCURACY) * 100) + "%\n";
	    }
	    
	    bufferString += "Raw Confusion Matrix:\n";
	    bufferString += writeIntConfusionMatrix(aggregateEval.get2dIntArrayMetadata(NemaDataConstants.CLASSIFICATION_CONFUSION_MATRIX_RAW), classNames);
	    bufferString += "\nConfusion Matrix percentage:\n";
	    bufferString += writePercentageConfusionMatrix(aggregateEval.get2dDoubleArrayMetadata(NemaDataConstants.CLASSIFICATION_CONFUSION_MATRIX_PERCENT), classNames);
	    bufferString += writeMatrixKey(classNames);
	        
	    if (usingAHierarchy)
	    {
	        bufferString += "\nHierachically Discounted Confusion Vector:\n";
	        bufferString += writeDoubleConfusionVector(aggregateEval.getDoubleArrayMetadata(NemaDataConstants.CLASSIFICATION_DISCOUNT_CONFUSION_VECTOR_RAW), classNames);
	        bufferString += "\nHierachically Discounted Confusion Matrix percentage:\n";
	        bufferString += writePercentageConfusionVector(aggregateEval.getDoubleArrayMetadata(NemaDataConstants.CLASSIFICATION_DISCOUNT_CONFUSION_VECTOR_PERCENT), classNames);
	    }
	    
	    bufferString += BIG_DIVIDER;
	    
	    FileUtils.writeStringToFile(outputFile, bufferString, "UTF-8");
    }

    /**
     * Writes an integer confusion matrix to a file.
     * @param matrix The matrix to be written.
     * @param classNames The class names.
     * @return
     */
    public String writeIntConfusionMatrix(int[][] matrix, List<String> classNames) {
        String bufferString = "Truth\t\t";
        for(int x=0; x<classNames.size(); x++) {
            bufferString += getKey(x) + "\t";
        }
        bufferString += "\nClassification\n";
        for(int x=0; x<classNames.size(); x++) {
            bufferString += getKey(x) + "\t\t";
            for(int y=0; y<classNames.size(); y++) {
                bufferString += fmtInt(matrix[x][y]) + "\t";
            }
            bufferString += "\n";
        }
        return bufferString;
    }
    
    /**
     * Writes a double confusion matrix to a file.
     * @param vector The matrix to be written.
     * @param classNames The class names.
     * @return
     */
    public String writeDoubleConfusionVector(double[] vector, List<String> classNames) {
        String bufferString = "Truth\t\t";
        for(int x=0; x<classNames.size(); x++) {
            bufferString += getKey(x) + "\t";
        }
        
        for(int x=0; x<classNames.size(); x++) {
            bufferString += fmtDec(vector[x]) + "\t";
        }
        bufferString += "\n";
        return bufferString;
    }
    
    /**
     * Writes a double confusion matrix to a file.
     * @param vector The matrix to be written.
     * @param classNames The class names.
     * @return
     */
    public String writePercentageConfusionVector(double[] vector, List<String> classNames) {
        String bufferString = "Truth\t\t";
        for(int x=0; x<classNames.size(); x++) {
            bufferString += getKey(x) + "\t";
        }
        
        for(int x=0; x<classNames.size(); x++) {
            bufferString += fmtPercent(vector[x] * 100.0) + "\t";
        }
        bufferString += "\n";
        return bufferString;
    }
    
    /**
     * Writes a double confusion matrix to a file.
     * @param matrix The matrix to be written.
     * @param classNames The class names.
     * @return 
     */
    public String writePercentageConfusionMatrix(double[][] matrix, List<String> classNames) {
        String bufferString = "Truth\t\t";
        for(int x=0; x<classNames.size(); x++) {
            bufferString += getKey(x) + "\t";
        }
        bufferString += "\nClassification\n";
        for(int x=0; x<classNames.size(); x++) {
            //bufferString += (String)classNames.get(x) + "\t\t";
            bufferString += getKey(x) + "\t\t";
            for(int y=0; y<classNames.size(); y++) {
                bufferString += fmtPercent(matrix[x][y] * 100.0) + "\t";
            }
            bufferString += "\n";
        }
        return bufferString;
    }


    /** 
     * Outputs the confusion matrix key
     * @param classNames
     * @return the key
     */
    public String writeMatrixKey(List<String> classNames) {
        StringBuffer sb = new StringBuffer();
        sb.append("Matrix Key:\n");
        for(int x=0; x<classNames.size(); x++) {
            sb.append("   ");
            sb.append(getKey(x));
            sb.append(": "); 
            sb.append(classNames.get(x));
            sb.append("\n");
        }
        return sb.toString();
    }
    
        /** 
     *  Returns a two character key for a classname based upon
     * its index
     * @param keyIndex  the class name index
     * @return  a two character key
     */
    private String getKey(int keyIndex) {
        StringBuffer label = new StringBuffer(); 
        if (keyIndex >= 26) {
            label.append((char) ('A' + (keyIndex / 26) - 1));
            keyIndex = keyIndex % 26;
        } else {
            label.append(' ');
        }
        label.append((char) ('A' + keyIndex));
        return pad(label.toString() + "  ", COL_WIDTH);
    }

    /** 
     * Format a decimal number for column output
     * @param val the value to format
     * @return the formatted value
     */
    private String fmtDec(double val) {
        return pad(dec.format(val), COL_WIDTH);
    }

    /** 
     * Format an int for column output
     * @param val the value to format
     * @return the formatted output
     */
    private String fmtInt(int val) {
        return pad(Integer.toString(val), COL_WIDTH);
    }

    /** 
     * Format a percentage value for output
     * @param val the value to format
     * @return the formatted value
     */
    private String fmtPercent(double val) {
        return pad(dec.format(val) + "%", COL_WIDTH);
    }

    /** 
     *  Pad the given string to the given length
     * @param v  the string to pad
     * @param padLength  the length to pad
     * @return the padded string
     */
    private String pad(String v, int padLength) {
        String paddedString = "                                                    " + v;
        return paddedString.substring(
            paddedString.length() - padLength);
    }
	
}
