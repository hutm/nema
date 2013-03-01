package org.imirsel.nema.analytics.evaluation.structure;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.imirsel.nema.analytics.evaluation.ResultRendererImpl;
import org.imirsel.nema.analytics.evaluation.WriteCsvResultFiles;
import org.imirsel.nema.analytics.evaluation.resultpages.FileListItem;
import org.imirsel.nema.analytics.evaluation.resultpages.Page;
import org.imirsel.nema.analytics.evaluation.resultpages.PageItem;
import org.imirsel.nema.analytics.evaluation.resultpages.ProtovisFunctionTimestepPlotItem;
import org.imirsel.nema.analytics.evaluation.resultpages.ProtovisSegmentationPlotItem;
import org.imirsel.nema.analytics.evaluation.resultpages.Table;
import org.imirsel.nema.analytics.evaluation.resultpages.TableItem;
import org.imirsel.nema.model.NemaChord;
import org.imirsel.nema.model.NemaData;
import org.imirsel.nema.model.NemaDataConstants;
import org.imirsel.nema.model.NemaEvaluationResultSet;
import org.imirsel.nema.model.NemaSegment;
import org.imirsel.nema.model.NemaTrackList;
import org.imirsel.nema.model.util.IOUtil;

public class StructureResultRenderer extends ResultRendererImpl {

	public static final String STRUCTURE_PLOT_EXT = ".structure.png";
	public static final double TARGET_PLOT_RESOLUTION = 0.05;
	
	public StructureResultRenderer() {
		super();
	}

	public StructureResultRenderer(File workingDir, File outputDir) {
		super(workingDir, outputDir);
	}
	
	@Override
	public void renderResults(NemaEvaluationResultSet results) throws IOException {
		getLogger().info("Creating system result directories...");
		Map<String, File> jobIDToResultDir = makeSystemResultDirs(results);
		
		/* Write out leaderboard CSV file */
		getLogger().info("Writing out leaderboard CSV...");
		File leaderboardCSV = this.writeLeaderBoardCSVFile(NemaDataConstants.STRUCTURE_SEGMENTATION_PWF, results, false);

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
		writeHtmlResultPages(results, summaryCsv, jobIDToPerTrackCSV,
				jobIDToTgz, outputDir);
		
		getLogger().info("Done.");
		
	}
	

	@Override
	public void renderAnalysis(NemaEvaluationResultSet results) throws IOException {
		/* Write analysis HTML pages */
		getLogger().info("Creating result HTML files...");
		writeHtmlAnalysisPages(results, outputDir);
		
		getLogger().info("Done.");
	}


	/**
	 * Writes the analysis HTML pages for multiple jobs/algorithms
	 * 
	 * @param results   The NemaEvaluationResultSet to write analysis pages for.
	 * @param outputDir directory to write the HTML pages to.
	 */
	private void writeHtmlAnalysisPages(NemaEvaluationResultSet results, File outputDir) {
		String jobId;
		List<Page> resultPages = new ArrayList<Page>();
		List<PageItem> items;
		Page aPage;
		
		TableItem legendTable = createLegendTable(results);
		
		//do intro page to describe task
        {
        	resultPages.add(createIntroHtmlPage(results,legendTable));
        }

		/* Do per system pages */
		{
			for (Iterator<String> it = results.getJobIds().iterator(); it
					.hasNext();) {
				jobId = it.next();
				items = new ArrayList<PageItem>();
				TableItem filtLegend = filterLegendTable(legendTable, jobId);
				if(filtLegend != null){
					items.add(filtLegend);
				}
				
				/* Plot structure segmentation against GT for each track result for each system */
				PageItem[] plots = plotTranscriptionForJob(jobId, results);
				for (int i = 0; i < plots.length; i++) {
					items.add(plots[i]);
				}

				aPage = new Page(results.getJobName(jobId) + "_results", results.getJobName(jobId),
						items, true);
				resultPages.add(aPage);
			}
		}
		
		// do comparative plot page
		{
			getLogger().info("Creating comparison plots page...");
			items = new ArrayList<PageItem>();
			items.add(legendTable);
			PageItem[] plots = plotTranscriptionForAllJobs(results);
			for (int i = 0; i < plots.length; i++) {
				items.add(plots[i]);
			}
			getLogger().info("\tdone.");
			aPage = new Page("comparisonPlots", "Comparative plots", items, true);
			resultPages.add(aPage);
		}


		Page.writeResultPages(results.getTask().getName(), outputDir, resultPages);
	}
	
	/**
	 * Writes the result HTML pages for the evaluation of multiple jobs/algorithms
	 * 
	 * @param results                   The NemaEvaluationResultSet to write results pages for.
	 * @param jobIDToResultPlotFileList map of a jobId to the results plots for that job.
	 * @param summaryCsv 				the summary csv file that summarizes all jobs.
	 * @param jobIDToPerTrackCSV 		map of jobId to individual per-track results csv files for that job.
	 * @param jobIDToTgz 				map of jobId to the tar-balls of individual job results.
	 * @param outputDir                 directory to write the HTML pages to.
	 */
	private void writeHtmlResultPages(NemaEvaluationResultSet results, File summaryCsv,
			Map<String, File> jobIDToPerTrackCSV, Map<String, File> jobIDToTgz, File outputDir) {
		String jobId;
		Map<NemaTrackList,List<NemaData>> sysResults;
		List<Page> resultPages = new ArrayList<Page>();
		List<PageItem> items;
		Page aPage;
		int numJobs = results.getJobIds().size();

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
			aPage = new Page("summary", "Summary", items, false);
			resultPages.add(aPage);
		}

		/* Do per system pages */
		{
			for (Iterator<String> it = results.getJobIds().iterator(); it
					.hasNext();) {
				jobId = it.next();
				items = new ArrayList<PageItem>();
				TableItem filtLegend = filterLegendTable(legendTable, jobId);
				if(filtLegend != null){
					items.add(filtLegend);
				}
				sysResults = results.getPerTrackEvaluationAndResults(jobId);
				
				/* Add per track table */
				Table perTrackTable = WriteCsvResultFiles.prepTableDataOverTracks(
						results.getTestSetTrackLists(), sysResults, 
						results.getTrackEvalMetricsAndResultsKeys()
					);
				
				items.add(new TableItem(results.getJobName(jobId) + "_results", results.getJobName(jobId)
						+ " Per Track Results", perTrackTable.getColHeaders(),
						perTrackTable.getRows()));

				/* Plot structure segmentation against GT for each track result for each system */
				PageItem[] plots = plotTranscriptionForJob(jobId, results);
				for (int i = 0; i < plots.length; i++) {
					items.add(plots[i]);
				}

				aPage = new Page(results.getJobName(jobId) + "_results", results.getJobName(jobId),
						items, true);
				resultPages.add(aPage);
			}
		}
		
		// do comparative plot page
		{
			getLogger().info("Creating comparison plots page...");
			items = new ArrayList<PageItem>();
			items.add(legendTable);
			PageItem[] plots = plotTranscriptionForAllJobs(results);
			for (int i = 0; i < plots.length; i++) {
				items.add(plots[i]);
			}
			getLogger().info("\tdone.");
			aPage = new Page("comparisonPlots", "Comparative plots", items, true);
			resultPages.add(aPage);
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
	 * Plots the structural segmentations for each job, for each file
	 * 
	 * @param jobId    the jobId we wish to plot results for.
	 * @param results  The results Object containing the data to plot.
	 * @return         an array of page items that will produce the plots.
	 */
	private PageItem[] plotTranscriptionForJob(String jobId,
			NemaEvaluationResultSet results) {
		NemaData result, groundtruth = null;

		/* Plot each result */
		Map<NemaTrackList, List<NemaData>> job_results = results.getPerTrackEvaluationAndResults(jobId);
		List<PageItem> plotItems = new ArrayList<PageItem>();
		
		for (Iterator<NemaTrackList> foldIt = job_results.keySet().iterator(); foldIt.hasNext();){
			NemaTrackList testSet = foldIt.next();
			for (Iterator<NemaData> iterator = job_results.get(testSet).iterator(); iterator
					.hasNext();) {
				
				result = iterator.next();
				
				getLogger().info("\t\tplotting track " + result.getId() +"...");
				if(results.getTrackIDToGT() != null){
					groundtruth = results.getTrackIDToGT().get(result.getId());
				}
				if(groundtruth == null){
					getLogger().warning("No ground-truth found for '" + result.getId() + "' to be used in plotting");
				}
				List<NemaSegment> rawData = (List<NemaSegment>)result.getMetadata(NemaDataConstants.STRUCTURE_SEGMENTATION_DATA);
				
				//setup time line for for X-axis
				double startTimeSecs = 0.0;
				//end at last offset from GT or predictions
				double endTimeSecs = rawData.get(rawData.size()-1).getOffset();

				List<NemaSegment> rawGtData = null;
				if(groundtruth != null && groundtruth.hasMetadata(NemaDataConstants.STRUCTURE_SEGMENTATION_DATA)){
					rawGtData = (List<NemaSegment>)groundtruth.getMetadata(NemaDataConstants.STRUCTURE_SEGMENTATION_DATA);
					endTimeSecs = Math.max(rawGtData.get(rawGtData.size()-1).getOffset(), endTimeSecs);
				}else{
					getLogger().warning("No ground-truth found for '" + result.getId() + "' to be used in plotting");
				}
				
				//setup data-series to plot
				Map<String,List<NemaSegment>> series = new HashMap<String, List<NemaSegment>>(2);
				List<String> seriesNames = new ArrayList<String>(2);
				if(rawGtData != null){
					series.put("Ground-truth", (List<NemaSegment>) rawGtData);
					seriesNames.add("Ground-truth");
				}
				series.put("Prediction", (List<NemaSegment>) rawData);
				seriesNames.add("Prediction");
				try{
					ProtovisSegmentationPlotItem plot = new ProtovisSegmentationPlotItem(
							//plotname
							results.getJobName(jobId) + "_segments_" + result.getId(), 
							//plot caption
							results.getJobName(jobId) + ": Structural segmentation for track " + result.getId(), 
							//start time for x axis
							startTimeSecs, 
							//end time for x axis
							endTimeSecs, 
							//series to plot
							series,
							//series names in order to plot
							seriesNames,
							//output dir
							outputDir);
					plotItems.add(plot);
				}catch(IOException e){
					getLogger().log(Level.SEVERE, "Failed to plot results for job " + results.getJobName(jobId) + " (" + jobId + ") for track " + result.getId(), e);
				}
			}
		}
		return plotItems.toArray(new PageItem[plotItems.size()]);
	}

	/**
	 * Plots the chord transcriptions for all jobs, for each file.
	 * 
	 * @param testSets    the list of test sets.
	 * @param results  The results Object containing the data to plot.
	 * @return         an array of page items that will produce the plots.
	 */
	@SuppressWarnings("unchecked")
	private PageItem[] plotTranscriptionForAllJobs(NemaEvaluationResultSet results) {
		NemaData groundtruth = null;

		/* Plot each result */
		Map<String,Map<NemaTrackList, List<NemaData>>> perTrackResults = results.getJobIdToPerTrackEvaluationAndResults();
		List<PageItem> plotItems = new ArrayList<PageItem>();
		Map<String,NemaData[]> trackIDToTranscripts = new HashMap<String,NemaData[]>();
		//get job names and sort
		List<String> jobNames = new ArrayList<String>(perTrackResults.keySet());
		Collections.sort(jobNames);
		
		for (Iterator<NemaTrackList> foldIt = results.getTestSetTrackLists().iterator(); foldIt.hasNext();){
			NemaTrackList testSet = foldIt.next();
			
			//map IDs for tracks to an array of NemaData Objects for each system, use nulls in case of missing results
			for (Iterator<String> systemIt = perTrackResults.keySet().iterator(); systemIt.hasNext();){
				String system = systemIt.next();
				int systemIdx = jobNames.indexOf(system);
				Map<NemaTrackList,List<NemaData>> sysResults = perTrackResults.get(system);
				List<NemaData> sysSetResults = sysResults.get(testSet);
				for(Iterator<NemaData> trackIt = sysSetResults.iterator(); trackIt.hasNext();){
					NemaData track = trackIt.next();
					NemaData[] transcripts = trackIDToTranscripts.get(track.getId());
					if(transcripts == null){
						transcripts = new NemaData[jobNames.size()];
						trackIDToTranscripts.put(track.getId(), transcripts);
					}
					transcripts[systemIdx] = track;
				}
			}
		}
		
		//iterate over tracks (in alphabetical order) and produce each plot
		List<String> trackIds = new ArrayList<String>(trackIDToTranscripts.keySet());
		Collections.sort(trackIds);	
		
		for(Iterator<String> trackIt = trackIds.iterator(); trackIt.hasNext();){
			String trackId = trackIt.next();
			NemaData[] transcripts = trackIDToTranscripts.get(trackId);
			getLogger().info("\t\tplotting track " + trackId +"...");
			

			//setup data-series to plot
			Map<String,List<NemaSegment>> series = new HashMap<String, List<NemaSegment>>(2);			
			List<String> seriesNames = new ArrayList<String>(1 + transcripts.length);

			//setup time line for for X-axis
			double startTimeSecs = 0.0;
			double endTimeSecs = 0;
			
			if(results.getTrackIDToGT() != null){
				groundtruth = results.getTrackIDToGT().get(trackId);
			}
			if(groundtruth == null){
				getLogger().warning("No ground-truth found for '" + trackId + "' to be used in plotting");
			}
			
			List<NemaSegment> rawGtData = null;
			if(groundtruth != null && groundtruth.hasMetadata(NemaDataConstants.STRUCTURE_SEGMENTATION_DATA)){
				rawGtData = (List<NemaSegment>)groundtruth.getMetadata(NemaDataConstants.STRUCTURE_SEGMENTATION_DATA);
				endTimeSecs = Math.max(rawGtData.get(rawGtData.size()-1).getOffset(), endTimeSecs);
			}else{
				getLogger().warning("No ground-truth found for '" + trackId + "' to be used in plotting");
			}
			if(groundtruth != null && groundtruth.hasMetadata(NemaDataConstants.STRUCTURE_SEGMENTATION_DATA)){
				rawGtData = (List<NemaSegment>)groundtruth.getMetadata(NemaDataConstants.STRUCTURE_SEGMENTATION_DATA);
				endTimeSecs = Math.max(rawGtData.get(rawGtData.size()-1).getOffset(), endTimeSecs);
				series.put("Ground-truth", rawGtData);
				seriesNames.add("Ground-truth");
			}else{
				getLogger().warning("No ground-truth found for '" + trackId + "' to be used in plotting");
			}
			
			for (int i = 0; i < transcripts.length; i++) {
				NemaData nemaData = transcripts[i];
				Object rawData = nemaData.getMetadata(NemaDataConstants.STRUCTURE_SEGMENTATION_DATA);
				if(rawData != null){
					List<NemaSegment> rawDataList = (List<NemaSegment>) rawData;
					endTimeSecs = Math.max(endTimeSecs, rawDataList.get(rawDataList.size()-1).getOffset());
					series.put(jobNames.get(i), rawDataList);
					seriesNames.add(jobNames.get(i));
				}
			}
			
			try{
				ProtovisSegmentationPlotItem plot = new ProtovisSegmentationPlotItem(
						//plotname
						"segments_" + trackId, 
						//plot caption
						" Structural segmentation for track " + trackId, 
						//start time for x axis
						startTimeSecs, 
						//end time for x axis
						endTimeSecs, 
						//series to plot
						series,
						//series names in order to plot
						seriesNames,
						//output dir
						outputDir);
				plotItems.add(plot);
			}catch(IOException e){
				getLogger().log(Level.SEVERE, "Failed to plot results for track " + trackId, e);
			}
		}
		return plotItems.toArray(new PageItem[plotItems.size()]);
	}
}
