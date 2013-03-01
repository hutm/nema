package org.imirsel.nema.analysis.evaluation;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.imirsel.nema.analytics.evaluation.resultpages.Page;
import org.imirsel.nema.analytics.evaluation.resultpages.PageItem;
import org.imirsel.nema.analytics.evaluation.resultpages.ProtovisSegmentationPlotItem;
import org.imirsel.nema.model.NemaData;
import org.imirsel.nema.model.NemaDataConstants;
import org.imirsel.nema.model.NemaSegment;
import org.imirsel.nema.model.fileTypes.SingleTrackEvalFileType;
import org.imirsel.nema.model.fileTypes.StructureTextFile;
import org.imirsel.nema.test.BaseManagerTestCase;
import org.junit.BeforeClass;
import org.junit.Test;


public class StructureFileAndSegmentationPlotTest extends BaseManagerTestCase{
	
	private static File workingDirectory;
	private static File outputDirectory;

	

	@BeforeClass
	public static void  prepareWorkingLocation(){
		 String tempLocation = System.getProperty("java.io.tmpdir");
	     workingDirectory = new File(tempLocation);
	     outputDirectory = new File(workingDirectory,(System.currentTimeMillis())+"");
	     outputDirectory.mkdirs();
	}
	

	@SuppressWarnings("unchecked")
	@Test
	public void plotTranscriptionsAndGroundTruth() throws Exception {
		File groundTruthDirectory = new File("src/test/resources/structure/groundtruth");
        SingleTrackEvalFileType reader = new StructureTextFile();
        List<NemaData> gtList = reader.readDirectory(groundTruthDirectory, null);
    	Map<String,NemaData> gtMap = new HashMap<String, NemaData>();
        for (Iterator<NemaData> iterator = gtList.iterator(); iterator.hasNext();) {
			NemaData gt = iterator.next();
			gtMap.put(gt.getId(),gt);
		}
        
        File resultsFile = new File("src/test/resources/structure/results/paulus/01__help.lab"); 
		NemaData result = reader.readFile(resultsFile);
        
		List<PageItem> plotItems = new ArrayList<PageItem>();
		NemaData groundtruth = gtMap.get(result.getId());
		
		List<NemaSegment> rawGtData = (List<NemaSegment>)groundtruth.getMetadata(NemaDataConstants.STRUCTURE_SEGMENTATION_DATA);
		List<NemaSegment> rawData = (List<NemaSegment>)result.getMetadata(NemaDataConstants.STRUCTURE_SEGMENTATION_DATA);
		
		//setup time line for for X-axis
		double startTimeSecs = 0.0;
		//end at last offset from GT or predictions
		double endTimeSecs = Math.max(rawGtData.get(rawGtData.size()-1).getOffset(), rawData.get(rawData.size()-1).getOffset());
		
		//setup data-series to plot
		Map<String,List<NemaSegment>> series = new HashMap<String, List<NemaSegment>>(2);
		series.put("Prediction", rawData);
		series.put("Ground-truth", rawGtData);
		List<String> seriesNames = new ArrayList<String>(2);
		seriesNames.add("Prediction");
		seriesNames.add("Ground-truth");
		
		ProtovisSegmentationPlotItem plot = new ProtovisSegmentationPlotItem(
				//plotname
				"Test_chord_plot_" + result.getId(), 
				//plot caption
				"Test chord plot: Chord transcription for track " + result.getId(), 
				//start time for x axis
				startTimeSecs, 
				//end time for x axis
				endTimeSecs, 
				//series to plot
				series,
				//series names in order to plot
				seriesNames,
				//output dir
				outputDirectory);
		plotItems.add(plot);
		
		
		
		List<Page> resultPages = new ArrayList<Page>();
		Page aPage;
		
		aPage = new Page("dummy_results", "Test Segmentation Plot", plotItems, true);
		resultPages.add(aPage);

		System.out.println("Writing test segmentation plots to: " + outputDirectory.getAbsolutePath());
		
		Page.writeResultPages("Test Segmentation Plot Results", outputDirectory, resultPages);
    }
	
	
}
