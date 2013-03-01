package org.imirsel.nema.analytics.evaluation.tagsClassification;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import org.imirsel.nema.analytics.evaluation.FriedmansAnovaTkHsd;
import org.imirsel.nema.analytics.evaluation.ResultRendererImpl;
import org.imirsel.nema.analytics.evaluation.WriteCsvResultFiles;
import org.imirsel.nema.analytics.evaluation.resultpages.FileListItem;
import org.imirsel.nema.analytics.evaluation.resultpages.ImageItem;
import org.imirsel.nema.analytics.evaluation.resultpages.Page;
import org.imirsel.nema.analytics.evaluation.resultpages.PageItem;
import org.imirsel.nema.analytics.evaluation.resultpages.Table;
import org.imirsel.nema.analytics.evaluation.resultpages.TableItem;
import org.imirsel.nema.model.NemaData;
import org.imirsel.nema.model.NemaDataConstants;
import org.imirsel.nema.model.NemaEvaluationResultSet;
import org.imirsel.nema.model.NemaTrackList;
import org.imirsel.nema.model.util.IOUtil;

/**
 * Tag classification results rendering.
 * 
 * @author kris.west@gmail.com
 * @since 0.4.0
 */
public class TagAffinityResultRenderer extends ResultRendererImpl {

	public TagAffinityResultRenderer() {
		super();
	}

	public TagAffinityResultRenderer(File workingDir, File outputDir) {
		super(workingDir, outputDir);
	}
	
	@Override
	public void renderAnalysis(NemaEvaluationResultSet results) throws IOException {
		throw new UnsupportedOperationException("No rendering provided for tag classificaiton without evaluation");
	}
	
	/**
	 * Writes per track csv files for each system containing AUC-ROC and
	 * precision-at-n scores.
	 *
	 * @param numJobs The number of jobs.
	 * @param jobIDToResultDir Map of job ID to result directory to write to.
	 * @return A map of job ID to the CSV file created for it.
	 * @throws IOException
	 */
	protected Map<String, File> writePerTrackSystemResultCSVs(
			NemaEvaluationResultSet results,
			Map<String, File> jobIDToResultDir) throws IOException {
		String jobId;
		Map<String, File> jobIDToPerFoldCSV = new HashMap<String, File>(jobIDToResultDir.size());
		for (Iterator<String> it = results.getJobIds().iterator(); it
				.hasNext();) {
			jobId = it.next();
			
			File sysDir = jobIDToResultDir.get(jobId);
			File foldCSV = new File(sysDir.getAbsolutePath() + File.separator + "per_track_results.csv");
			WriteCsvResultFiles.writeTableToCsv(prepTableDataOverTracks(results, jobId),
					foldCSV);
			jobIDToPerFoldCSV.put(jobId, foldCSV);
		}
		return jobIDToPerFoldCSV;
	}
	

	/**
     * Prepares a Table Object representing the AUC-ROC and Precision-at-N 
     * scores per tracks, where the metrics are the columns of the table and the 
     * rows are the different tracks in the evaluation.
     * 
     * @param testSets An ordered list of the test sets.
     * @param foldEval Map of test set to the evaluation results for that 
     * particular fold of the experiment, encoded as a NemaData Object.
	 * @return The prepared Table.
     */
    private Table prepTableDataOverTracks(NemaEvaluationResultSet results, String jobId) {
    	DecimalFormat DEC = new DecimalFormat("0.0000");
		int[] precisionAtNLevels = results.getOverallEvaluation(jobId).getIntArrayMetadata(NemaDataConstants.TAG_AFFINITY_PRECISION_AT_N_LEVELS);
		
		//set column names
    	int numMetrics = 1 + precisionAtNLevels.length;
    	int numCols = numMetrics + 2;
        String[] colNames = new String[numCols];
        colNames[0] = "Fold";
        colNames[1] = "Track";
        colNames[2] = NemaDataConstants.TAG_AFFINITY_AUC_ROC;
        for (int i = 0; i < precisionAtNLevels.length; i++) {
        	colNames[i+3] = NemaDataConstants.TAG_AFFINITY_PRECISION_AT_N.replaceAll("N", ""+precisionAtNLevels[i]);
		}

      //count number of rows to produce
        int numTracks = 0;
        Map<NemaTrackList,List<NemaData>> perTrackResults = results.getPerTrackEvaluationAndResults(jobId);
        for (Iterator<List<NemaData>> iterator = perTrackResults.values().iterator(); iterator.hasNext();) {
			List<NemaData> list = iterator.next();
			numTracks += list.size();
		}
        
        List<NemaTrackList> sets = results.getTestSetTrackLists();
//        int numFolds = sets.size();
        Collections.sort(sets,new Comparator<NemaTrackList>(){
			public int compare(NemaTrackList o1, NemaTrackList o2) {
				return o1.getFoldNumber() - o2.getFoldNumber();
			}
        });
        
        
        //produce rows (assume but check that results are ordered the same for each system)
        List<String[]> rows = new ArrayList<String[]>();
        int foldNum = 0;
        NemaTrackList foldList = sets.get(foldNum);
        int foldTrackCount = 0;
        int actualRowCount = 0;
        String[] row;
        NemaData data;
        while(actualRowCount < numTracks){
        	if (foldTrackCount == perTrackResults.get(foldList).size()){
        		foldNum++;
        		foldList = sets.get(foldNum);
        		foldTrackCount = 0;
        	}
        	row = new String[numCols];
        	row[0] = "" + foldList.getFoldNumber();
        	data = perTrackResults.get(foldList).get(foldTrackCount);
        	row[1] = data.getId();
        	row[2] = DEC.format(data.getDoubleMetadata(NemaDataConstants.TAG_AFFINITY_AUC_ROC));
        	double[] scores = data.getDoubleArrayMetadata(NemaDataConstants.TAG_AFFINITY_PRECISION_AT_N);
        	for (int i = 0; i < precisionAtNLevels.length; i++) {
        		row[i+3] = DEC.format(scores[i]);
        	}
        	rows.add(row);

        	actualRowCount++;
        	foldTrackCount++;
        }
        
        return new Table(colNames, rows);
    }
    
    /**
	 * Writes per fold csv files for each system containing AUC-ROC and
	 * precision-at-n scores.
	 * 
	 * @param numJobs The number of jobs.
	 * @param jobIDToResultDir Map of job ID to result directory to write to.
	 * @return A map of job ID to the CSV file created for it.
	 * @throws IOException
	 */
	protected Map<String, File> writePerFoldSystemResultCSVs(
			NemaEvaluationResultSet results,
			Map<String, File> jobIDToResultDir) throws IOException {
		String jobId;
		Map<String, File> jobIDToPerFoldCSV = new HashMap<String, File>(jobIDToResultDir.size());
		for (Iterator<String> it = results.getJobIds().iterator(); it
				.hasNext();) {
			jobId = it.next();
			
			File sysDir = jobIDToResultDir.get(jobId);
			File foldCSV = new File(sysDir.getAbsolutePath() + File.separator + "per_fold_results.csv");
			WriteCsvResultFiles.writeTableToCsv(prepTableDataOverFolds(results, jobId),
					foldCSV);
			jobIDToPerFoldCSV.put(jobId, foldCSV);
		}
		return jobIDToPerFoldCSV;
	}
	

	/**
     * Prepares a Table Object representing the AUC-ROC and Precision-at-N 
     * scores per fold, where the metrics are the columns of the table and the 
     * rows are the different folds in the evaluation.
     * 
     * @param testSets An ordered list of the test sets.
     * @param foldEval Map of test set to the evaluation results for that 
     * particular fold of the experiment, encoded as a NemaData Object.
	 * @return The prepared Table.
     */
    private Table prepTableDataOverFolds(NemaEvaluationResultSet results, String jobId) {
    	DecimalFormat DEC = new DecimalFormat("0.0000");
		int[] precisionAtNLevels = results.getOverallEvaluation(jobId).getIntArrayMetadata(NemaDataConstants.TAG_AFFINITY_PRECISION_AT_N_LEVELS);
		
		//set column names
    	int numMetrics = 1 + precisionAtNLevels.length;
    	int numCols = numMetrics + 1;
        String[] colNames = new String[numCols];
        colNames[0] = "Fold";
        colNames[1] = NemaDataConstants.TAG_AFFINITY_AUC_ROC;
        for (int i = 0; i < precisionAtNLevels.length; i++) {
        	colNames[i+2] = NemaDataConstants.TAG_AFFINITY_PRECISION_AT_N.replaceAll("N", ""+precisionAtNLevels[i]);
		}

        //count number of rows to produce
        List<NemaTrackList> sets = results.getTestSetTrackLists();
        int numFolds = sets.size();
        Collections.sort(sets,new Comparator<NemaTrackList>(){
			public int compare(NemaTrackList o1, NemaTrackList o2) {
				return o1.getFoldNumber() - o2.getFoldNumber();
			}
        });
        
        
        //produce rows (assume but check that results are ordered the same for each system)
        List<String[]> rows = new ArrayList<String[]>(numFolds);
        String[] row;
        NemaData eval;
        NemaTrackList set;
        for(int f=0;f<numFolds;f++){
        	set = sets.get(f);
        	eval = results.getPerFoldEvaluation(jobId).get(set);
        	row = new String[numCols];
        	row[0] = "" + set.getFoldNumber();
        	row[1] = DEC.format(eval.getDoubleMetadata(NemaDataConstants.TAG_AFFINITY_AUC_ROC));
        	double[] scores = eval.getDoubleArrayMetadata(NemaDataConstants.TAG_AFFINITY_PRECISION_AT_N);
        	for (int i = 0; i < precisionAtNLevels.length; i++) {
        		row[i+2] = DEC.format(scores[i]);
        	}
        	rows.add(row);
        	f++;
        }
        
        return new Table(colNames, rows);
    }
	
	/**
	 * Default method of writing overall result summary CSV file. Uses the 
	 * declared overall metric keys to produce a summary result table.
	 * 
	 * @param results Result set to get per-track, per-system result data from.
	 * @return File Object representing the CSV created.
	 * @throws IOException
	 */
	protected File writeOverallResultsCSV(NemaEvaluationResultSet results)
			throws IOException {
		File summaryCsv = new File(outputDir.getAbsolutePath() + File.separator + "summaryResults.csv");
		WriteCsvResultFiles.writeTableToCsv(
				prepOverallResultsTable(results),
				summaryCsv
			);
		return summaryCsv;
	}
	
	/**
	 * Creates an overall summary Table with the AUC-ROC and Precision-at-N data. 
	 * 
	 * @param results Result set to result data from.
	 * @return Table Object representing the data.
	 * @throws IOException
	 */
	private Table prepOverallResultsTable(NemaEvaluationResultSet results){
		DecimalFormat DEC = new DecimalFormat("0.0000");
		int[] precisionAtNLevels = results.getOverallEvaluation(results.getJobIds().iterator().next()).getIntArrayMetadata(NemaDataConstants.TAG_AFFINITY_PRECISION_AT_N_LEVELS);
		int numCols = 2 + precisionAtNLevels.length;
        String[] colNames = new String[numCols];
        colNames[0] = "Algorithm";
        colNames[1] = NemaDataConstants.TAG_AFFINITY_AUC_ROC;
        for (int i = 0; i < precisionAtNLevels.length; i++) {
        	colNames[i+2] = NemaDataConstants.TAG_AFFINITY_PRECISION_AT_N.replaceAll("N", ""+precisionAtNLevels[i]);
		}
        
        List<String[]> rows = new ArrayList<String[]>();
        
        NemaData eval;
		String jobId;
		String jobName;
        for (Iterator<String> it = results.getJobIdToOverallEvaluation().keySet().iterator();it.hasNext();) {
        	jobId = it.next();
        	jobName = results.getJobName(jobId);
        	eval = results.getOverallEvaluation(jobId);
        	String[] row = new String[numCols];
        	row[0] = jobName;
        	row[1] = DEC.format(eval.getDoubleMetadata(NemaDataConstants.TAG_AFFINITY_AUC_ROC));
        	double[] scores = eval.getDoubleArrayMetadata(NemaDataConstants.TAG_AFFINITY_PRECISION_AT_N);
        	for (int i = 0; i < precisionAtNLevels.length; i++) {
        		row[i+2] = DEC.format(scores[i]);
        	}
            rows.add(row);
        }

        return new Table(colNames, rows);
	}
	
	@Override
	public void renderResults(NemaEvaluationResultSet results)
			throws IOException {
		
		int numJobs = results.getJobIds().size();
		
		
		getLogger().info("Creating system result directories...");
		Map<String, File> jobIDToResultDir = makeSystemResultDirs(results);

//		List<File> overallCSVs = new ArrayList<File>();

		/* Write out leaderboard CSV file */
		getLogger().info("Writing out leaderboard CSV...");
		File leaderboardCSV = this.writeLeaderBoardCSVFile(NemaDataConstants.TAG_AFFINITY_AUC_ROC, results, false);
//		overallCSVs.add(leaderboardCSV);
		
		getLogger().info("Writing out CSV result files...");
		/* Write out summary CSV */
		//write out results summary CSV
		File summaryCSV = writeOverallResultsCSV(results);
//		overallCSVs.add(summaryCSV);
		
		
		
		List<File> foldCSVs = new ArrayList<File>();
		//write out summaries for each metric over folds
		//AUC-ROC
		File aucRocCsv = new File(outputDir.getAbsolutePath() + File.separator + "AUCROCByFold.csv");
		WriteCsvResultFiles.writeTableToCsv(WriteCsvResultFiles.prepTableDataOverFoldsAndSystems(results.getTestSetTrackLists(),results.getJobIdToPerFoldEvaluation(),results.getJobIdToJobName(),NemaDataConstants.TAG_AFFINITY_AUC_ROC),aucRocCsv);
		foldCSVs.add(aucRocCsv);
		
		//precision-at-N
			//very ugly way of getting precision at N levels
		int[] precisionAtNLevels = results.getOverallEvaluation(results.getJobIds().iterator().next()).getIntArrayMetadata(NemaDataConstants.TAG_AFFINITY_PRECISION_AT_N_LEVELS);
		List<File> precisionAtNFiles = new ArrayList<File>(precisionAtNLevels.length);
		for (int i = 0; i < precisionAtNLevels.length; i++) {
			File precCSV = new File(outputDir.getAbsolutePath() + File.separator + "precision-at-" + precisionAtNLevels[i] + ".csv");
			WriteCsvResultFiles.writeTableToCsv(WriteCsvResultFiles.prepTableDataOverFoldsAndSystems(results.getTestSetTrackLists(),results.getJobIdToPerFoldEvaluation(),results.getJobIdToJobName(),NemaDataConstants.TAG_AFFINITY_PRECISION_AT_N,i),precCSV);
			foldCSVs.add(precCSV);
			precisionAtNFiles.add(precCSV);
		}
		
		
		//write out summaries for each metric over tags
		//get tag names
		List<String> tags = new ArrayList<String>((Collection<String>)results.getJobIdToOverallEvaluation().values().iterator().next().getMetadata(NemaDataConstants.TAG_EXPERIMENT_CLASSNAMES));

		//AUC-ROC
		File aucRocTagCSV = new File(outputDir.getAbsolutePath() + File.separator + "AUCROCByTag.csv");
		WriteCsvResultFiles.writeTableToCsv(WriteCsvResultFiles.prepTableDataOverClassMaps(results.getJobIdToOverallEvaluation(),results.getJobIdToJobName(),tags,NemaDataConstants.TAG_AFFINITY_AUC_ROC_MAP),aucRocTagCSV);
		
		
		List<File> tagCSVs = new ArrayList<File>();
		tagCSVs.add(aucRocTagCSV);
		
		//write out per system: folds and metrics
		Map<String, File> jobIDToPerFoldCSV = writePerFoldSystemResultCSVs(
				results, jobIDToResultDir);
		
		//write out per system: tracks and metrics
		Map<String, File> jobIDToPerTrackCSV = writePerTrackSystemResultCSVs(
				results, jobIDToResultDir);
		

		// perform statistical tests
		/* Do we need to stats tests? */
		boolean performStatSigTests = true;
		if (numJobs < 2) {
			performStatSigTests = false;
		}

		File friedmanAUCROCperTagPNG = null;
		File friedmanAUCROCperTagTable = null;
		
		File friedmanAUCROCperFoldPNG = null;
		File friedmanAUCROCperFoldTable = null;

		List<File> friedmanPrecisionAtNPNGs = null;
		List<File> friedmanPrecisionAtNTables = null;
		
		if (getPerformMatlabStatSigTests() && performStatSigTests) {
			getLogger().info("Performing Friedman's tests in Matlab...");

			File[] tmp =  null;
			
			tmp = FriedmansAnovaTkHsd.performFriedman(outputDir,
					aucRocTagCSV, 0, 1, 1, numJobs, getMatlabPath());
			friedmanAUCROCperTagPNG = tmp[0];
			friedmanAUCROCperTagTable = tmp[1];

			tmp = FriedmansAnovaTkHsd.performFriedman(outputDir, aucRocCsv, 0,
					1, 1, numJobs, getMatlabPath());
			friedmanAUCROCperFoldPNG = tmp[0];
			friedmanAUCROCperFoldTable = tmp[1];
			
			
			
			friedmanPrecisionAtNPNGs = new ArrayList<File>(precisionAtNLevels.length);
			friedmanPrecisionAtNTables = new ArrayList<File>(precisionAtNLevels.length);
			
			for (int i = 0; i < precisionAtNLevels.length; i++) {
				File csv = precisionAtNFiles.get(i);
				tmp = FriedmansAnovaTkHsd.performFriedman(outputDir, csv, 0,
						1, 1, numJobs, getMatlabPath());
				friedmanPrecisionAtNPNGs.add(tmp[0]);
				friedmanPrecisionAtNTables.add(tmp[1]);
			}
		}

		/* Create tar-balls of individual result directories */
		getLogger().info("Preparing evaluation data tarballs...");
		Map<String, File> jobIDToTgz = compressResultDirectories(jobIDToResultDir);

		
		// write result HTML pages
		writeHtmlResultPages(performStatSigTests, results, tags, summaryCSV, foldCSVs,
				tagCSVs, jobIDToPerTrackCSV, jobIDToPerFoldCSV, 
				friedmanAUCROCperFoldPNG, friedmanAUCROCperFoldTable,
				friedmanAUCROCperTagPNG, friedmanAUCROCperTagTable,
				friedmanPrecisionAtNPNGs, friedmanPrecisionAtNTables, 
				jobIDToTgz);
	}

	
	private void writeHtmlResultPages(boolean performStatSigTests,
			NemaEvaluationResultSet results, List<String> tags,
			File summaryCsv,
			List<File> foldCSVs,
			List<File> tagCSVs,
			Map<String, File> jobIDToPerTrackCSV, 
			Map<String, File> jobIDToPerFoldCSV, 
			File friedmanAUCROCFoldTablePNG,
			File friedmanAUCROCFoldTable, 
			File friedmanAUCROCTagTablePNG,
			File friedmanAUCROCTagTable, 
			List<File> friedmanPrecisionAtNPNGs,
			List<File> friedmanPrecisionAtNTables,
			Map<String, File> jobIDToTgz) {

		int numJobs = results.getJobIds().size();

		String jobId;
		Map<NemaTrackList, List<NemaData>> sysResults;
		Map<NemaTrackList, NemaData> systemFoldResults;
		getLogger().info("Creating result HTML files...");

		List<Page> resultPages = new ArrayList<Page>();
		List<PageItem> items;
		Page aPage;

		TableItem legendTable = createLegendTable(results);
		
		//do intro page to describe task
        {
        	resultPages.add(createIntroHtmlPage(results,legendTable));
        }

		// do summary page
		{
			getLogger().info("Creating summary page...");
			items = new ArrayList<PageItem>();
			items.add(legendTable);
			Table summaryTable;
			summaryTable = prepOverallResultsTable(results);
			items.add(new TableItem("summary_results", "Summary Results",
					summaryTable.getColHeaders(), summaryTable.getRows()));
		
			aPage = new Page("summary", "Summary", items, true);
			resultPages.add(aPage);
		}

		
		
		
		
		
		getLogger().info("Creating per-metric pages...");
		//AUC-ROC page
		{	
			items = new ArrayList<PageItem>();
			items.add(legendTable);
			items.add(plotSummaryOverMetric(results,NemaDataConstants.TAG_AFFINITY_AUC_ROC));
			
			Table theFoldTable = WriteCsvResultFiles.prepTableDataOverFoldsAndSystems(results.getTestSetTrackLists(),results.getJobIdToPerFoldEvaluation(),results.getJobIdToJobName(),NemaDataConstants.TAG_AFFINITY_AUC_ROC);
			items.add(new TableItem("aucroc_by_fold",
					"AUCROC Per Fold",
					theFoldTable.getColHeaders(),
					theFoldTable.getRows()));

			Table theTagTable = WriteCsvResultFiles.prepTableDataOverClassMaps(results.getJobIdToOverallEvaluation(),results.getJobIdToJobName(),tags,NemaDataConstants.TAG_AFFINITY_AUC_ROC_MAP);
			items.add(new TableItem("aucroc_by_tag",
					"AUCROC Per Tag", 
					theTagTable.getColHeaders(), 
					theTagTable.getRows()));
			
			aPage = new Page("aucroc",
					"AUCROC", items, true);
			resultPages.add(aPage);
		}
		
		//Precision-at-N page
		{	
			items = new ArrayList<PageItem>();
			items.add(legendTable);
			int[] precisionAtNLevels = results.getOverallEvaluation(results.getJobIds().iterator().next()).getIntArrayMetadata(NemaDataConstants.TAG_AFFINITY_PRECISION_AT_N_LEVELS);
			List<File> precisionAtNFiles = new ArrayList<File>(precisionAtNLevels.length);
			for (int i = 0; i < precisionAtNLevels.length; i++) {
				
				Table thePrecTable = WriteCsvResultFiles.prepTableDataOverFoldsAndSystems(
						results.getTestSetTrackLists(),results.getJobIdToPerFoldEvaluation(),results.getJobIdToJobName(),NemaDataConstants.TAG_AFFINITY_PRECISION_AT_N,i);
				items.add(new TableItem("prec-at-" + precisionAtNLevels[i],
						"Overall precision-at-" + precisionAtNLevels[i],
						thePrecTable.getColHeaders(),
						thePrecTable.getRows()));
			}
			
			aPage = new Page("precisionatn",
					"Precision-at-N", items, true);
			resultPages.add(aPage);
		}

		// do per system pages
		{
			getLogger().info("Creating per-system pages...");
			for (Iterator<String> it = results.getJobIds().iterator(); it
					.hasNext();) {
				jobId = it.next();
				items = new ArrayList<PageItem>();
				TableItem filtLegend = filterLegendTable(legendTable, jobId);
				if(filtLegend != null){
					items.add(filtLegend);
				}
				sysResults = results.getPerTrackEvaluationAndResults(jobId);
				systemFoldResults = results.getPerFoldEvaluation(jobId);
				{
					Table systemFoldTable = prepTableDataOverFolds(results,	jobId);
					items.add(new TableItem(results.getJobIdToJobName().get(jobId) + "_per_fold", results
							.getJobIdToJobName().get(jobId)
							+ " per fold results", systemFoldTable.getColHeaders(),
							systemFoldTable.getRows()));
				}
				{
					Table systemTrackTable = prepTableDataOverTracks(results,jobId);
					items.add(new TableItem(results.getJobIdToJobName().get(jobId) + "_per_track", results
							.getJobIdToJobName().get(jobId)
							+ " per track results", systemTrackTable
							.getColHeaders(), systemTrackTable.getRows()));
				}
				
				aPage = new Page(results.getJobIdToJobName().get(jobId), results.getJobIdToJobName().get(jobId),
						items, true);
				resultPages.add(aPage);
			}
		}
		
		
		// do significance tests
		if (getPerformMatlabStatSigTests() && performStatSigTests) {
			getLogger().info("Creating significance test pages...");
			items = new ArrayList<PageItem>();
			items.add(legendTable);
			items.add(new ImageItem(
					"friedmanAUCROCFoldTablePNG",
					"AUCROC by Fold: Friedman's ANOVA w/ Tukey Kramer HSD",
					IOUtil.makeRelative(friedmanAUCROCFoldTablePNG, outputDir)));
			items.add(new ImageItem(
					"friedmanFmeasureTagTablePNG",
					"AUCROC by Tag: Friedman's ANOVA w/ Tukey Kramer HSD",
					IOUtil.makeRelative(friedmanAUCROCTagTablePNG, outputDir)));
			int[] precisionAtNLevels = results.getOverallEvaluation(results.getJobIds().iterator().next()).getIntArrayMetadata(NemaDataConstants.TAG_AFFINITY_PRECISION_AT_N_LEVELS);
			for (int i = 0; i < precisionAtNLevels.length; i++) {
				items.add(new ImageItem(
						"friedmanPrecisionAt" + precisionAtNLevels[i] + "PNG",
						"Precision-at-" + precisionAtNLevels[i] + ": Friedman's ANOVA w/ Tukey Kramer HSD",
						IOUtil.makeRelative(friedmanPrecisionAtNPNGs.get(i), outputDir)));
			}
			
			aPage = new Page("sig_tests", "Significance Tests", items, true);
			resultPages.add(aPage);
		}

		// do files page
		{
			getLogger().info("Creating files page...");
			items = new ArrayList<PageItem>();

			// Overall CSVs
			List<String> overallCsvs = new ArrayList<String>(3);

			overallCsvs.add(IOUtil.makeRelative(summaryCsv, outputDir));

			items.add(new FileListItem("overallCSVs",
					"Overall CSV result files", overallCsvs));

			//per fold CSVs
			List<String> perFoldCsvs = new ArrayList<String>(6);
			for (int i = 0; i < foldCSVs.size(); i++) {
				perFoldCsvs.add(IOUtil.makeRelative(foldCSVs.get(i), outputDir));
			}
			items.add(new FileListItem("perFoldCSVs",
					"Per-fold CSV result files", perFoldCsvs));
			
			//per tag CSVs
			List<String> perTagCsvs = new ArrayList<String>(6);
			for (int i = 0; i < tagCSVs.size(); i++) {
				perTagCsvs.add(IOUtil.makeRelative(tagCSVs.get(i), outputDir));
			}
			items.add(new FileListItem("perTagCSVs",
					"Per-tag CSV result files", perTagCsvs));
			
			// Per system CSVs
			List<String> perSystemCsvs = new ArrayList<String>(numJobs * 2);
			for (Iterator<String> it = jobIDToPerTrackCSV.keySet().iterator(); it
					.hasNext();) {
				jobId = it.next();
				File pertrack = jobIDToPerTrackCSV.get(jobId);
				perSystemCsvs.add(IOUtil.makeRelative(pertrack, outputDir));
				File perfold = jobIDToPerFoldCSV.get(jobId);
				perSystemCsvs.add(IOUtil.makeRelative(perfold, outputDir));
				
			}
			items.add(new FileListItem("perSystemCSVs",
					"Per-system CSV result files", perSystemCsvs));

			// Friedman's tables and plots
			if (getPerformMatlabStatSigTests() && performStatSigTests) {
				// Friedmans tables
				List<String> sigCSVPaths = new ArrayList<String>(2);
				sigCSVPaths.add(IOUtil.makeRelative(friedmanAUCROCFoldTable, outputDir));
				sigCSVPaths.add(IOUtil.makeRelative(friedmanAUCROCTagTable, outputDir));
				for (int i = 0; i < friedmanPrecisionAtNTables.size(); i++) {
					sigCSVPaths.add(IOUtil.makeRelative(friedmanPrecisionAtNTables.get(i), outputDir));
				}

				items.add(new FileListItem("sigCSVs", "Significance test CSVs",
						sigCSVPaths));

				// Friedmans plots
				List<String> sigPNGPaths = new ArrayList<String>(2);
				sigPNGPaths.add(IOUtil.makeRelative(friedmanAUCROCFoldTablePNG, outputDir));
				sigPNGPaths.add(IOUtil.makeRelative(friedmanAUCROCTagTablePNG, outputDir));
				for (int i = 0; i < friedmanPrecisionAtNPNGs.size(); i++) {
					sigCSVPaths.add(IOUtil.makeRelative(friedmanPrecisionAtNPNGs.get(i), outputDir));
				}
				
				
				items.add(new FileListItem("sigPNGs",
						"Significance test plots", sigPNGPaths));
			}

			// System Tarballs
			List<String> tarballPaths = new ArrayList<String>(numJobs);
			for (Iterator<String> it = jobIDToTgz.keySet().iterator(); it
					.hasNext();) {
				jobId = it.next();
				tarballPaths.add(IOUtil.makeRelative(jobIDToTgz.get(jobId),
						outputDir));
			}
			items.add(new FileListItem("tarballs",
					"Per algorithm evaluation tarball", tarballPaths));
			aPage = new Page("files", "Raw data files", items, true);
			resultPages.add(aPage);
		}

		getLogger().info("Writing out pages...");
		Page.writeResultPages(results.getTask().getName(), outputDir,
				resultPages);
		getLogger().info("Done...");
		
	}

}
