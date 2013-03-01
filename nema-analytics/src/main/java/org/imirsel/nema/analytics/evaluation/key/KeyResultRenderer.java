package org.imirsel.nema.analytics.evaluation.key;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.imirsel.nema.analytics.evaluation.ResultRendererImpl;
import org.imirsel.nema.analytics.evaluation.WriteCsvResultFiles;
import org.imirsel.nema.analytics.evaluation.resultpages.FileListItem;
import org.imirsel.nema.analytics.evaluation.resultpages.Page;
import org.imirsel.nema.analytics.evaluation.resultpages.PageItem;
import org.imirsel.nema.analytics.evaluation.resultpages.ProtovisBarChartPlotItem;
import org.imirsel.nema.analytics.evaluation.resultpages.Table;
import org.imirsel.nema.analytics.evaluation.resultpages.TableItem;
import org.imirsel.nema.model.NemaData;
import org.imirsel.nema.model.NemaDataConstants;
import org.imirsel.nema.model.NemaEvaluationResultSet;
import org.imirsel.nema.model.NemaTrackList;
import org.imirsel.nema.model.util.IOUtil;

public class KeyResultRenderer extends ResultRendererImpl {

	public KeyResultRenderer() {
		super();
	}

	public KeyResultRenderer(File workingDir, File outputDir) {
		super(workingDir, outputDir);
	}
	
	@Override
	public void renderResults(NemaEvaluationResultSet results)
			throws IOException {
		getLogger().info("Creating system result directories...");
		Map<String, File> jobIDToResultDir = makeSystemResultDirs(results);

		/* Write out leaderboard CSV file */
		getLogger().info("Writing out leaderboard CSV...");
		File leaderboardCSV = this.writeLeaderBoardCSVFile(NemaDataConstants.KEY_DETECTION_WEIGHTED_SCORE, results, false);
		
		/* Write out summary CSV */
		getLogger().info("Writing out CSV result files over whole task...");
		File summaryCsv = writeOverallResultsCSV(results);
		

		/* Write out per track CSV for each system */
		getLogger().info("Writing out per-system result files...");
		Map<String, File> jobIDToPerTrackCSV = writePerTrackSystemResultCSVs(
				results, jobIDToResultDir);


		/* Create tar-balls of individual result directories */
		getLogger().info("Preparing evaluation data tarballs...");
		Map<String, File> jobIDToTgz = compressResultDirectories(jobIDToResultDir);

		
		/* Write result HTML pages */
		getLogger().info("Creating result HTML files...");
		writeResultHtmlPages(results, summaryCsv, 
				jobIDToPerTrackCSV, jobIDToTgz, outputDir);
	}
	
	@Override
	public void renderAnalysis(NemaEvaluationResultSet results) throws IOException {
//		/* Write analysis HTML pages */
//		getLogger().info("Creating result HTML files...");
//		writeHtmlAnalysisPages(results, outputDir);
//		
//		getLogger().info("Done.");
		throw new UnsupportedOperationException("No analysis result rendering facilities are implemented for key classification!");
	}
	
	

	/**
	 * Writes the result HTML pages for the evaluation of multiple jobs/algorithms
	 * 
	 * @param results					Results to be written to HTML files
	 * @param summaryCsv 				the summary csv file that summarizes all jobs
	 * @param jobIDToPerTrackCSV 		map of jobId to individual per-track results csv files for that job
	 * @param jobIDToTgz 				map of jobId to the tar-balls of individual job results
	 */
	private void writeResultHtmlPages(NemaEvaluationResultSet results, 
			File summaryCsv, Map<String, File> jobIDToPerTrackCSV, Map<String, File> jobIDToTgz, File outputDir) {
		String jobId;
		int numJobs = results.getJobIds().size();
		
		List<Page> resultPages = new ArrayList<Page>();
		List<PageItem> items;
		Page aPage;

		Map<NemaTrackList,List<NemaData>> sysResults;
		
		TableItem legendTable = createLegendTable(results);
		
		//do intro page to describe task
        {
        	resultPages.add(createIntroHtmlPage(results,legendTable));
        }
		/* Do summary page */
		{
			items = new ArrayList<PageItem>();
			items.add(legendTable);
			Table summaryTable = WriteCsvResultFiles.prepSummaryTable(
					results.getJobIdToOverallEvaluation(), results.getJobIdToJobName(), results.getOverallEvalMetricsKeys());
			
			items.add(new TableItem("summary_results", "Summary Results",
					summaryTable.getColHeaders(), summaryTable.getRows()));
			
			items.add(plotOverallMetricBarChart(NemaDataConstants.KEY_DETECTION_WEIGHTED_SCORE, results, NemaDataConstants.KEY_DETECTION_WEIGHTED_SCORE, NemaDataConstants.KEY_DETECTION_WEIGHTED_SCORE));
			
			aPage = new Page("summary", "Summary", items, false);
			resultPages.add(aPage);
		}

		/* Do per system pages */
		{
			for (Iterator<String> it = results.getJobIds().iterator(); it.hasNext();) {
				jobId = it.next();
				items = new ArrayList<PageItem>();
				TableItem filtLegend = filterLegendTable(legendTable, jobId);
				if(filtLegend != null){
					items.add(filtLegend);
				}
				sysResults = results.getPerTrackEvaluationAndResults(jobId);
				
				/* Plot summary result bar chart for each system */
				PageItem plot = plotSummaryForJob(jobId, results);
				items.add(plot);
				
				/* Add per track table */
				Table perTrackTable = WriteCsvResultFiles.prepTableDataOverTracks(results.getTestSetTrackLists(), sysResults, results.getTrackEvalMetricsAndResultsKeys());
				items.add(new TableItem(results.getJobIdToJobName().get(jobId) + "_results", results.getJobName(jobId)
						+ " Per Track Results", perTrackTable.getColHeaders(),
						perTrackTable.getRows()));

				
//				
//				
//				/* Add list of plots */
//				List<String> plotPathList = new ArrayList<String>(numJobs);
//				File[] plotPaths = jobIDToResultPlotFileList.get(jobId);
//				for (int i = 0; i < plotPaths.length; i++) {
//					plotPathList.add(IOUtil.makeRelative(plotPaths[i],
//							outputDir));
//				}
//				items.add(new FileListItem("plots", "System summary plot",
//						plotPathList));
//
				aPage = new Page(results.getJobIdToJobName().get(jobId) + "_results", results.getJobName(jobId),
						items, true);
				resultPages.add(aPage);
			}
		}

		/* Do files page */
		{
			items = new ArrayList<PageItem>();

			/* CSVs */
			List<String> CSVPaths = new ArrayList<String>(4);
			CSVPaths.add(IOUtil.makeRelative(summaryCsv, outputDir));
			for (Iterator<String> it = results.getJobIds().iterator(); it
					.hasNext();) {
				jobId = it.next();
				CSVPaths.add(IOUtil.makeRelative(jobIDToPerTrackCSV.get(jobId),
						outputDir));
			}

			items.add(new FileListItem("dataCSVs", "CSV result files",
							CSVPaths));

			/* System tar-balls */
			List<String> tarballPaths = new ArrayList<String>(numJobs);
			for (Iterator<String> it = results.getJobIds().iterator(); it
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

		Page.writeResultPages(results.getTask().getName(), outputDir, resultPages);
	}


	/**
	 * Plots bar chart of the performance scores for a job.
	 * 
	 * @param jobId    the jobId we wish to plot results for.
	 * @param results  The results Object containing the data to plot.
	 * @return         a PageItem that will produce the plot.
	 */
	private static PageItem plotSummaryForJob(String jobId,
			NemaEvaluationResultSet results) {

		NemaData resultSummary = results.getJobIdToOverallEvaluation().get(jobId);

		List<String> seriesNames = new ArrayList<String>();
		List<Double> seriesVals = new ArrayList<Double>();
		
		seriesNames.add(NemaDataConstants.KEY_DETECTION_WEIGHTED_SCORE);
		seriesVals.add(resultSummary.getDoubleMetadata(NemaDataConstants.KEY_DETECTION_WEIGHTED_SCORE));
		
		seriesNames.add(NemaDataConstants.KEY_DETECTION_CORRECT);
		seriesVals.add(resultSummary.getDoubleMetadata(NemaDataConstants.KEY_DETECTION_CORRECT));
		
		seriesNames.add(NemaDataConstants.KEY_DETECTION_PERFECT_FIFTH_ERROR);
		seriesVals.add(resultSummary.getDoubleMetadata(NemaDataConstants.KEY_DETECTION_PERFECT_FIFTH_ERROR));
		
		seriesNames.add(NemaDataConstants.KEY_DETECTION_RELATIVE_ERROR);
		seriesVals.add(resultSummary.getDoubleMetadata(NemaDataConstants.KEY_DETECTION_RELATIVE_ERROR));
		
		seriesNames.add(NemaDataConstants.KEY_DETECTION_PARALLEL_ERROR);
		seriesVals.add(resultSummary.getDoubleMetadata(NemaDataConstants.KEY_DETECTION_PARALLEL_ERROR));
		
		seriesNames.add(NemaDataConstants.KEY_DETECTION_ERROR);
		seriesVals.add(resultSummary.getDoubleMetadata(NemaDataConstants.KEY_DETECTION_ERROR));
		
		String name = results.getJobName(jobId) + "_perf_summary";
		String caption = results.getJobName(jobId) + ": Performance summary";
		ProtovisBarChartPlotItem chart = new ProtovisBarChartPlotItem(name, caption, seriesNames, seriesVals);
		
		return chart;
	}
	
	

}
