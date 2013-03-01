package org.imirsel.nema.analysis.evaluation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.imirsel.nema.analytics.evaluation.resultpages.Page;
import org.imirsel.nema.analytics.evaluation.resultpages.PageItem;
import org.imirsel.nema.analytics.evaluation.resultpages.ProtovisBarChartPlotItem;
import org.imirsel.nema.model.NemaDataConstants;
import org.junit.BeforeClass;
import org.junit.Test;


public class ProtovisBarChartTest {
	private static File workingDirectory;
	private static File outputDirectory;
	
	@BeforeClass
	public static void  prepareWorkingLocation(){
		 String tempLocation = System.getProperty("java.io.tmpdir");
	     workingDirectory = new File(tempLocation);
	     outputDirectory = new File(workingDirectory,(System.currentTimeMillis())+"");
	     outputDirectory.mkdirs();
	}
	
	
	@Test
	public void testBarChart()  throws Exception{
		
		List<Page> resultPages = new ArrayList<Page>();
		List<PageItem> items = new ArrayList<PageItem>();
		Page aPage;
		

		List<String> seriesNames = new ArrayList<String>();
		List<Double> seriesVals = new ArrayList<Double>();
		
		seriesNames.add(NemaDataConstants.KEY_DETECTION_WEIGHTED_SCORE);
		seriesVals.add(0.52345238759);
		
		seriesNames.add(NemaDataConstants.KEY_DETECTION_CORRECT);
		seriesVals.add(0.1238419);
		
		seriesNames.add(NemaDataConstants.KEY_DETECTION_PERFECT_FIFTH_ERROR);
		seriesVals.add(0.98191112);
		
		seriesNames.add(NemaDataConstants.KEY_DETECTION_RELATIVE_ERROR);
		seriesVals.add(0.12381041);
		
		seriesNames.add(NemaDataConstants.KEY_DETECTION_PARALLEL_ERROR);
		seriesVals.add(0.1);
		
		seriesNames.add(NemaDataConstants.KEY_DETECTION_ERROR);
		seriesVals.add(0.4231);
		
		String name = "dummy_perf_summary";
		String caption = "Dummy Job: Performance summary";
		ProtovisBarChartPlotItem chart = new ProtovisBarChartPlotItem(name, caption, seriesNames, seriesVals);
		
		items.add(chart);
		
		aPage = new Page("dummy_results", "Test Bar Chart", items, true);
		resultPages.add(aPage);

		System.out.println("Writing test bar chart to: " + outputDirectory.getAbsolutePath());
		
		Page.writeResultPages("Test Bar Chart Results", outputDirectory, resultPages);
		
	}
}
