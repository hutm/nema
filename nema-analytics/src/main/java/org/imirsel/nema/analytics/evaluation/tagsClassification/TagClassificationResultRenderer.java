package org.imirsel.nema.analytics.evaluation.tagsClassification;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
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
public class TagClassificationResultRenderer extends ResultRendererImpl {

	public TagClassificationResultRenderer() {
		super();
	}

	public TagClassificationResultRenderer(File workingDir, File outputDir) {
		super(workingDir, outputDir);
	}
	
	@Override
	public void renderAnalysis(NemaEvaluationResultSet results) throws IOException {
		throw new UnsupportedOperationException("No rendering provided for tag classificaiton without evaluation");
	}
	
	/**
	 * Produces a summary result table.
	 * 
	 * @param results Result set to get per-track, per-system result data from.
	 * @return File Object representing the CSV created.
	 * @throws IOException
	 */
	protected File writeOverallResultsCSV(NemaEvaluationResultSet results)
			throws IOException {
		File summaryCsv = new File(outputDir.getAbsolutePath() + File.separator + "summaryResults.csv");
		List<String> metrics = new ArrayList<String>();
		metrics.add(NemaDataConstants.TAG_ACCURACY);
		metrics.add(NemaDataConstants.TAG_POS_ACCURACY);
		metrics.add(NemaDataConstants.TAG_NEG_ACCURACY);
		metrics.add(NemaDataConstants.TAG_PRECISION);
		metrics.add(NemaDataConstants.TAG_RECALL);
		metrics.add(NemaDataConstants.TAG_FMEASURE);
		WriteCsvResultFiles.writeTableToCsv(
				WriteCsvResultFiles.prepSummaryTable(results.getJobIdToOverallEvaluation(), results.getJobIdToJobName(), metrics),
				summaryCsv
			);
		return summaryCsv;
	}
	
	/**
	 * Default method of writing result CSV files per fold, for each system. 
	 * Uses the declared per-fold metrics and results keys to produce a 
	 * per-fold result table for each system.
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
		Map<NemaTrackList, NemaData>  sysFoldResults;
		Map<String, File> jobIDToPerFoldCSV = new HashMap<String, File>(jobIDToResultDir.size());
		List<String> metrics = new ArrayList<String>();
		metrics.add(NemaDataConstants.TAG_ACCURACY);
		metrics.add(NemaDataConstants.TAG_POS_ACCURACY);
		metrics.add(NemaDataConstants.TAG_NEG_ACCURACY);
		metrics.add(NemaDataConstants.TAG_PRECISION);
		metrics.add(NemaDataConstants.TAG_RECALL);
		metrics.add(NemaDataConstants.TAG_FMEASURE);
		for (Iterator<String> it = results.getJobIds().iterator(); it
				.hasNext();) {
			jobId = it.next();
			sysFoldResults = results.getPerFoldEvaluation(jobId);
			
			File sysDir = jobIDToResultDir.get(jobId);
			File foldCSV = new File(sysDir.getAbsolutePath() + File.separator + "per_fold_results.csv");
			WriteCsvResultFiles.writeTableToCsv(WriteCsvResultFiles
					.prepTableDataOverFolds(results.getTestSetTrackLists(),sysFoldResults, metrics),
					foldCSV);
			jobIDToPerFoldCSV.put(jobId, foldCSV);
		}
		return jobIDToPerFoldCSV;
	}
	
	/**
	 * Default method of writing result CSV files per track, for each system. 
	 * Uses the declared per-track metrics and results keys to produce a 
	 * per-track result table for each system.
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
		Map<NemaTrackList, List<NemaData>> sysResults;
		Map<String, File> jobIDToPerTrackCSV = new HashMap<String, File>(jobIDToResultDir.size());
		List<String> metrics = new ArrayList<String>();
		metrics.add(NemaDataConstants.TAG_ACCURACY);
		metrics.add(NemaDataConstants.TAG_POS_ACCURACY);
		metrics.add(NemaDataConstants.TAG_NEG_ACCURACY);
		metrics.add(NemaDataConstants.TAG_PRECISION);
		metrics.add(NemaDataConstants.TAG_RECALL);
		metrics.add(NemaDataConstants.TAG_FMEASURE);
		for (Iterator<String> it = results.getJobIds().iterator(); it
				.hasNext();) {
			jobId = it.next();
			sysResults = results.getPerTrackEvaluationAndResults(jobId);
			
			File sysDir = jobIDToResultDir.get(jobId);
			File trackCSV = new File(sysDir.getAbsolutePath() + File.separator + "per_track_results.csv");
			WriteCsvResultFiles.writeTableToCsv(
					WriteCsvResultFiles.prepTableDataOverTracks(results.getTestSetTrackLists(), sysResults, metrics)
					,trackCSV);
			jobIDToPerTrackCSV.put(jobId, trackCSV);
		}
		return jobIDToPerTrackCSV;
	}
	
	@Override
	public void renderResults(NemaEvaluationResultSet results)
			throws IOException {
		
		int numJobs = results.getJobIds().size();
		
		
		getLogger().info("Creating system result directories...");
		Map<String, File> jobIDToResultDir = makeSystemResultDirs(results);
		
		/* Write out leaderboard CSV file */
		getLogger().info("Writing out leaderboard CSV...");
		File leaderboardCSV = this.writeLeaderBoardCSVFile(NemaDataConstants.TAG_FMEASURE, results, false);

		getLogger().info("Writing out CSV result files...");
		/* Write out summary CSV */
		//write out results summary CSV
		File summaryCSV = writeOverallResultsCSV(results);
		
		//write out summaries for each metric over folds
		//acc
		File accCSV = new File(outputDir.getAbsolutePath() + File.separator + "accuracyByFold.csv");
		WriteCsvResultFiles.writeTableToCsv(WriteCsvResultFiles.prepTableDataOverFoldsAndSystems(results.getTestSetTrackLists(),results.getJobIdToPerFoldEvaluation(),results.getJobIdToJobName(),NemaDataConstants.TAG_ACCURACY),accCSV);
		
		//fmeasure
		File fmeasureCSV = new File(outputDir.getAbsolutePath() + File.separator + "fmeasureByFold.csv");
		WriteCsvResultFiles.writeTableToCsv(WriteCsvResultFiles.prepTableDataOverFoldsAndSystems(results.getTestSetTrackLists(),results.getJobIdToPerFoldEvaluation(),results.getJobIdToJobName(),NemaDataConstants.TAG_FMEASURE),fmeasureCSV);
		
		//precision
		File precisionCSV = new File(outputDir.getAbsolutePath() + File.separator + "precisionByFold.csv");
		WriteCsvResultFiles.writeTableToCsv(WriteCsvResultFiles.prepTableDataOverFoldsAndSystems(results.getTestSetTrackLists(),results.getJobIdToPerFoldEvaluation(),results.getJobIdToJobName(),NemaDataConstants.TAG_PRECISION),precisionCSV);
		
		//recall
		File recallCSV = new File(outputDir.getAbsolutePath() + File.separator + "recallByFold.csv");
		WriteCsvResultFiles.writeTableToCsv(WriteCsvResultFiles.prepTableDataOverFoldsAndSystems(results.getTestSetTrackLists(),results.getJobIdToPerFoldEvaluation(),results.getJobIdToJobName(),NemaDataConstants.TAG_RECALL),recallCSV);
		
		//positive example acc
		File posExAccCSV = new File(outputDir.getAbsolutePath() + File.separator + "positiveExampleAccuracyByFold.csv");
		WriteCsvResultFiles.writeTableToCsv(WriteCsvResultFiles.prepTableDataOverFoldsAndSystems(results.getTestSetTrackLists(),results.getJobIdToPerFoldEvaluation(),results.getJobIdToJobName(),NemaDataConstants.TAG_POS_ACCURACY),posExAccCSV);

		//negative example acc
		File negExAccCSV = new File(outputDir.getAbsolutePath() + File.separator + "negativeExampleAccuracyByFold.csv");
		WriteCsvResultFiles.writeTableToCsv(WriteCsvResultFiles.prepTableDataOverFoldsAndSystems(results.getTestSetTrackLists(),results.getJobIdToPerFoldEvaluation(),results.getJobIdToJobName(),NemaDataConstants.TAG_NEG_ACCURACY),negExAccCSV);
		
		File[] foldCSVs = new File[]{accCSV,fmeasureCSV,precisionCSV,recallCSV,posExAccCSV,negExAccCSV};
		
		
		//write out summaries for each metric over tags
		//get tag names
		List<String> tags = new ArrayList<String>((Collection<String>)results.getJobIdToOverallEvaluation().values().iterator().next().getMetadata(NemaDataConstants.TAG_EXPERIMENT_CLASSNAMES));

		//acc
		File accTagCSV = new File(outputDir.getAbsolutePath() + File.separator + "accuracyByTag.csv");
		WriteCsvResultFiles.writeTableToCsv(WriteCsvResultFiles.prepTableDataOverClassMaps(results.getJobIdToOverallEvaluation(),results.getJobIdToJobName(),tags,NemaDataConstants.TAG_ACCURACY_TAG_MAP),accTagCSV);
		
		//fmeasure
		File fmeasureTagCSV = new File(outputDir.getAbsolutePath() + File.separator + "fmeasureByTag.csv");
		WriteCsvResultFiles.writeTableToCsv(WriteCsvResultFiles.prepTableDataOverClassMaps(results.getJobIdToOverallEvaluation(),results.getJobIdToJobName(),tags,NemaDataConstants.TAG_FMEASURE_TAG_MAP),fmeasureTagCSV);
		
		//precision
		File precisionTagCSV = new File(outputDir.getAbsolutePath() + File.separator + "precisionByTag.csv");
		WriteCsvResultFiles.writeTableToCsv(WriteCsvResultFiles.prepTableDataOverClassMaps(results.getJobIdToOverallEvaluation(),results.getJobIdToJobName(),tags,NemaDataConstants.TAG_PRECISION_TAG_MAP),precisionTagCSV);
		
		//recall
		File recallTagCSV = new File(outputDir.getAbsolutePath() + File.separator + "recallByTag.csv");
		WriteCsvResultFiles.writeTableToCsv(WriteCsvResultFiles.prepTableDataOverClassMaps(results.getJobIdToOverallEvaluation(),results.getJobIdToJobName(),tags,NemaDataConstants.TAG_RECALL_TAG_MAP),recallTagCSV);
		
		//positive example acc
		File posExAccTagCSV = new File(outputDir.getAbsolutePath() + File.separator + "positiveExampleAccuracyByTag.csv");
		WriteCsvResultFiles.writeTableToCsv(WriteCsvResultFiles.prepTableDataOverClassMaps(results.getJobIdToOverallEvaluation(),results.getJobIdToJobName(),tags,NemaDataConstants.TAG_POS_ACCURACY_TAG_MAP),posExAccTagCSV);

		//negative example acc
		File negExAccTagCSV = new File(outputDir.getAbsolutePath() + File.separator + "negativeExampleAccuracyByTag.csv");
		WriteCsvResultFiles.writeTableToCsv(WriteCsvResultFiles.prepTableDataOverClassMaps(results.getJobIdToOverallEvaluation(),results.getJobIdToJobName(),tags,NemaDataConstants.TAG_NEG_ACCURACY_TAG_MAP),negExAccTagCSV);
		
		File[] tagCSVs = new File[]{accTagCSV,fmeasureTagCSV,precisionTagCSV,recallTagCSV,posExAccTagCSV,negExAccTagCSV};
		
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

		File friedmanFmeasureFoldTablePNG = null;
		File friedmanFmeasureFoldTable = null;
		File friedmanFmeasureTagTablePNG = null;
		File friedmanFmeasureTagTable = null;

		//stats test on fmeasure by tag
		//stats test fmeasure by track
		if (getPerformMatlabStatSigTests() && performStatSigTests) {
			getLogger().info("Performing Friedman's tests in Matlab...");

			File[] tmp = FriedmansAnovaTkHsd.performFriedman(outputDir,
					fmeasureCSV, 0, 1, 1, numJobs, getMatlabPath());
			friedmanFmeasureFoldTablePNG = tmp[0];
			friedmanFmeasureFoldTable = tmp[1];

			tmp = FriedmansAnovaTkHsd.performFriedman(outputDir, fmeasureTagCSV, 0,
					1, 1, numJobs, getMatlabPath());
			friedmanFmeasureTagTablePNG = tmp[0];
			friedmanFmeasureTagTable = tmp[1];
		}

		/* Create tar-balls of individual result directories */
		getLogger().info("Preparing evaluation data tarballs...");
		Map<String, File> jobIDToTgz = compressResultDirectories(jobIDToResultDir);

		// write result HTML pages
		writeHtmlResultPages(performStatSigTests, results, tags, summaryCSV, foldCSVs,
				tagCSVs, jobIDToPerTrackCSV, jobIDToPerFoldCSV, friedmanFmeasureFoldTablePNG,
				friedmanFmeasureFoldTable, friedmanFmeasureTagTablePNG,
				friedmanFmeasureTagTable, jobIDToTgz);
	}

	private void writeHtmlResultPages(boolean performStatSigTests,
			NemaEvaluationResultSet results, List<String> tags,
			File summaryCsv,
			File[] foldCSVs,
			File[] tagCSVs,
			Map<String, File> jobIDToPerTrackCSV, 
			Map<String, File> jobIDToPerFoldCSV, 
			File friedmanFmeasureFoldTablePNG,
			File friedmanFmeasureFoldTable, 
			File friedmanFmeasureTagTablePNG,
			File friedmanFmeasureTagTable, 
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
        
        List<String> basic_metrics = new ArrayList<String>();
        basic_metrics.add(NemaDataConstants.TAG_ACCURACY);
        basic_metrics.add(NemaDataConstants.TAG_POS_ACCURACY);
        basic_metrics.add(NemaDataConstants.TAG_NEG_ACCURACY);
        basic_metrics.add(NemaDataConstants.TAG_PRECISION);
        basic_metrics.add(NemaDataConstants.TAG_RECALL);
        basic_metrics.add(NemaDataConstants.TAG_FMEASURE);

		// do summary page
		{
			getLogger().info("Creating summary page...");
			items = new ArrayList<PageItem>();
			items.add(legendTable);

			Table summaryTable = WriteCsvResultFiles.prepSummaryTable(results
					.getJobIdToOverallEvaluation(),
					results.getJobIdToJobName(), basic_metrics);
			items.add(new TableItem("summary_results", "Summary Results",
					summaryTable.getColHeaders(), summaryTable.getRows()));

			
			
			aPage = new Page("summary", "Summary", items, true);
			resultPages.add(aPage);
			
		}

		// do a page per metric
		getLogger().info("Creating per-metric pages...");
		{	
			items = new ArrayList<PageItem>();
			items.add(legendTable);

			items.add(plotSummaryOverMetric(results,NemaDataConstants.TAG_ACCURACY));
			
			Table theFoldTable = WriteCsvResultFiles.prepTableDataOverFoldsAndSystems(results.getTestSetTrackLists(),results.getJobIdToPerFoldEvaluation(),results.getJobIdToJobName(),NemaDataConstants.TAG_ACCURACY);
			items.add(new TableItem("acc_by_fold",
					"Accuracy Per Fold",
					theFoldTable.getColHeaders(),
					theFoldTable.getRows()));

			Table theTagTable = WriteCsvResultFiles.prepTableDataOverClassMaps(results.getJobIdToOverallEvaluation(),results.getJobIdToJobName(),tags,NemaDataConstants.TAG_ACCURACY_TAG_MAP);
			items.add(new TableItem("acc_by_tag",
					"Accuracy Per Tag", 
					theTagTable.getColHeaders(), 
					theTagTable.getRows()));
			
			aPage = new Page("accuracy",
					"Accuracy", items, true);
			resultPages.add(aPage);
		}
		{	
			items = new ArrayList<PageItem>();
			items.add(legendTable);

			items.add(plotSummaryOverMetric(results,NemaDataConstants.TAG_FMEASURE));
			
			Table theFoldTable = WriteCsvResultFiles.prepTableDataOverFoldsAndSystems(results.getTestSetTrackLists(),results.getJobIdToPerFoldEvaluation(),results.getJobIdToJobName(),NemaDataConstants.TAG_FMEASURE);
			items.add(new TableItem("fmeasure_by_fold",
					"F-measure Per Fold",
					theFoldTable.getColHeaders(),
					theFoldTable.getRows()));

			Table theTagTable = WriteCsvResultFiles.prepTableDataOverClassMaps(results.getJobIdToOverallEvaluation(),results.getJobIdToJobName(),tags,NemaDataConstants.TAG_FMEASURE_TAG_MAP);
			items.add(new TableItem("fmeasure_by_tag",
					"Fmeasure Per Tag", 
					theTagTable.getColHeaders(), 
					theTagTable.getRows()));
			
			aPage = new Page("fmeasure",
					"F-measure", items, true);
			resultPages.add(aPage);
		}
		{	
			items = new ArrayList<PageItem>();
			items.add(legendTable);

			items.add(plotSummaryOverMetric(results,NemaDataConstants.TAG_PRECISION));
			
			Table theFoldTable = WriteCsvResultFiles.prepTableDataOverFoldsAndSystems(results.getTestSetTrackLists(),results.getJobIdToPerFoldEvaluation(),results.getJobIdToJobName(),NemaDataConstants.TAG_PRECISION);
			items.add(new TableItem("precision_by_fold",
					"Precision Per Fold",
					theFoldTable.getColHeaders(),
					theFoldTable.getRows()));

			Table theTagTable = WriteCsvResultFiles.prepTableDataOverClassMaps(results.getJobIdToOverallEvaluation(),results.getJobIdToJobName(),tags,NemaDataConstants.TAG_PRECISION_TAG_MAP);
			items.add(new TableItem("precision_by_tag",
					"Precision Per Tag", 
					theTagTable.getColHeaders(), 
					theTagTable.getRows()));
			
			aPage = new Page("precision",
					"Precision", items, true);
			resultPages.add(aPage);
		}
		{	
			items = new ArrayList<PageItem>();
			items.add(legendTable);

			items.add(plotSummaryOverMetric(results,NemaDataConstants.TAG_RECALL));
			
			Table theFoldTable = WriteCsvResultFiles.prepTableDataOverFoldsAndSystems(results.getTestSetTrackLists(),results.getJobIdToPerFoldEvaluation(),results.getJobIdToJobName(),NemaDataConstants.TAG_RECALL);
			items.add(new TableItem("recall_by_fold",
					"Recall Per Fold",
					theFoldTable.getColHeaders(),
					theFoldTable.getRows()));

			Table theTagTable = WriteCsvResultFiles.prepTableDataOverClassMaps(results.getJobIdToOverallEvaluation(),results.getJobIdToJobName(),tags,NemaDataConstants.TAG_RECALL_TAG_MAP);
			items.add(new TableItem("recall_by_tag",
					"Recall Per Tag", 
					theTagTable.getColHeaders(), 
					theTagTable.getRows()));
			
			aPage = new Page("recall",
					"Recall", items, true);
			resultPages.add(aPage);
		}
		{	
			items = new ArrayList<PageItem>();
			items.add(legendTable);

			items.add(plotSummaryOverMetric(results,NemaDataConstants.TAG_POS_ACCURACY));
			
			Table theFoldTable = WriteCsvResultFiles.prepTableDataOverFoldsAndSystems(results.getTestSetTrackLists(),results.getJobIdToPerFoldEvaluation(),results.getJobIdToJobName(),NemaDataConstants.TAG_POS_ACCURACY);
			items.add(new TableItem("pos_ex_acc_by_fold",
					"Positive Example Accuracy Per Fold",
					theFoldTable.getColHeaders(),
					theFoldTable.getRows()));

			Table theTagTable = WriteCsvResultFiles.prepTableDataOverClassMaps(results.getJobIdToOverallEvaluation(),results.getJobIdToJobName(),tags,NemaDataConstants.TAG_POS_ACCURACY_TAG_MAP);
			items.add(new TableItem("pos_ex_acc_by_tag",
					"Positive Example Accuracy Per Tag", 
					theTagTable.getColHeaders(), 
					theTagTable.getRows()));
			
			aPage = new Page("pos_ex_accuracy",
					"Positive Example Accuracy", items, true);
			resultPages.add(aPage);
		}
		{	
			items = new ArrayList<PageItem>();
			items.add(legendTable);

			items.add(plotSummaryOverMetric(results,NemaDataConstants.TAG_NEG_ACCURACY));
			
			Table theFoldTable = WriteCsvResultFiles.prepTableDataOverFoldsAndSystems(results.getTestSetTrackLists(),results.getJobIdToPerFoldEvaluation(),results.getJobIdToJobName(),NemaDataConstants.TAG_NEG_ACCURACY);
			items.add(new TableItem("neg_ex_acc_by_fold",
					"Negative Example Accuracy Per Fold",
					theFoldTable.getColHeaders(),
					theFoldTable.getRows()));

			Table theTagTable = WriteCsvResultFiles.prepTableDataOverClassMaps(results.getJobIdToOverallEvaluation(),results.getJobIdToJobName(),tags,NemaDataConstants.TAG_NEG_ACCURACY_TAG_MAP);
			items.add(new TableItem("neg_ex_acc_by_tag",
					"Negative Example Accuracy Per Tag", 
					theTagTable.getColHeaders(), 
					theTagTable.getRows()));
			
			aPage = new Page("neg_ex_accuracy",
					"Negative Example Accuracy", items, true);
			resultPages.add(aPage);
		}
		

		
		// do per system pages
		{
			getLogger().info("Creating per-system page...");
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
					Table systemFoldTable = WriteCsvResultFiles
							.prepTableDataOverFolds(results.getTestSetTrackLists(),
									systemFoldResults, basic_metrics);
					items.add(new TableItem(results.getJobIdToJobName().get(jobId) + "_per_fold", results
							.getJobIdToJobName().get(jobId)
							+ " per fold results", systemFoldTable.getColHeaders(),
							systemFoldTable.getRows()));
				}
				{
					Table systemTrackTable = WriteCsvResultFiles
							.prepTableDataOverTracks(
									results.getTestSetTrackLists(), sysResults,
									basic_metrics);
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
			getLogger().info("Performing significance tests...");
			items = new ArrayList<PageItem>();
			items.add(legendTable);
			items.add(new ImageItem("friedmanFmeasureFoldTablePNG",
					"F-measure by Fold: Friedman's ANOVA w/ Tukey Kramer HSD",
					IOUtil.makeRelative(friedmanFmeasureFoldTablePNG, outputDir)));
			items
					.add(new ImageItem(
							"friedmanFmeasureTagTablePNG",
							"F-measure by Tag: Friedman's ANOVA w/ Tukey Kramer HSD",
							IOUtil.makeRelative(
									friedmanFmeasureTagTablePNG, outputDir)));

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
			for (int i = 0; i < foldCSVs.length; i++) {
				perFoldCsvs.add(IOUtil.makeRelative(foldCSVs[i], outputDir));
			}
			items.add(new FileListItem("perFoldCSVs",
					"Per-fold CSV result files", perFoldCsvs));
			
			//per tag CSVs
			List<String> perTagCsvs = new ArrayList<String>(6);
			for (int i = 0; i < tagCSVs.length; i++) {
				perTagCsvs.add(IOUtil.makeRelative(tagCSVs[i], outputDir));
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
				sigCSVPaths.add(IOUtil.makeRelative(friedmanFmeasureFoldTable,
						outputDir));
				sigCSVPaths.add(IOUtil.makeRelative(
						friedmanFmeasureTagTable, outputDir));

				items.add(new FileListItem("sigCSVs", "Significance test CSVs",
						sigCSVPaths));

				// Friedmans plots
				List<String> sigPNGPaths = new ArrayList<String>(2);
				sigPNGPaths.add(IOUtil.makeRelative(friedmanFmeasureFoldTablePNG,
						outputDir));
				sigPNGPaths.add(IOUtil.makeRelative(
						friedmanFmeasureTagTablePNG, outputDir));

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
