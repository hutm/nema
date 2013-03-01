package org.imirsel.nema.analysis.evaluation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.imirsel.nema.analytics.evaluation.resultpages.Page;
import org.imirsel.nema.analytics.evaluation.resultpages.PageItem;
import org.imirsel.nema.analytics.evaluation.resultpages.ProtovisBarChartPlotItem;
import org.imirsel.nema.analytics.evaluation.resultpages.ProtovisConfusionMatrixPlotItem;
import org.imirsel.nema.model.NemaDataConstants;
import org.junit.BeforeClass;
import org.junit.Test;


public class ProtovisConfusionMatrixTest {
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
	public void testConfusionMatrix()  throws Exception{
		
		List<Page> resultPages = new ArrayList<Page>();
		List<PageItem> items = new ArrayList<PageItem>();
		Page aPage;
		

		List<String> seriesNames = new ArrayList<String>();

		seriesNames.add("axe");
		seriesNames.add("bachata");
		seriesNames.add("forro");
		seriesNames.add("gaucha");
		seriesNames.add("merengue");
		seriesNames.add("pagode");
		seriesNames.add("salsa");
		seriesNames.add("sertaneja");
		seriesNames.add("tango");
		
		double[][] seriesVals = new double[][]{
				{39.94,4.15,5.71,8.31,11.25,5.40,27.12,15.11,18.07},
				{1.28,73.16,6.98,4.47,5.14,5.08,0.65,3.54,0.00},
				{4.15,5.11,53.65,5.43,4.82,3.49,5.23,7.72,15.89},
				{6.39,1.92,6.35,17.89,12.86,1.90,11.11,6.43,19.00},
				{16.61,2.88,5.08,26.84,43.73,9.52,5.88,4.18,20.56},
				{3.19,6.39,4.13,4.15,8.36,62.86,2.61,9.97,0.31},
				{11.18,0.64,4.44,11.82,2.57,0.95,40.52,3.22,8.10},
				{9.27,4.79,4.44,5.11,3.86,8.25,1.31,46.95,0.00},
				{7.99,0.64,4.44,15.02,6.43,1.59,5.23,1.29,17.76}
		};
		
		//divide down as we moved to 0-1 scale
		for (int i = 0; i < seriesVals.length; i++) {
			for (int j = 0; j < seriesVals[i].length; j++) {
				seriesVals[i][j] = seriesVals[i][j] / 100.0;
			}
		}
		
		String name = "dummy_conf_mat";
		String caption = "Dummy Job: Comfusion Matrix";
		ProtovisConfusionMatrixPlotItem chart = new ProtovisConfusionMatrixPlotItem(name, caption, seriesNames, seriesVals);
		
		items.add(chart);
		
		aPage = new Page("dummy_conf_mat", "Test Confusion Matrix", items, true);
		resultPages.add(aPage);

		System.out.println("Writing test confusion matrix to: " + outputDirectory.getAbsolutePath());
		
		Page.writeResultPages("Test Confusion Matrix Results", outputDirectory, resultPages);
		
	}
}
