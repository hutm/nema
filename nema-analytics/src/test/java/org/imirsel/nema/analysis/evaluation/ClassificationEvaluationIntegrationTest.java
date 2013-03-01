package org.imirsel.nema.analysis.evaluation;


import static org.junit.Assert.*;
import static org.imirsel.nema.test.matchers.NemaMatchers.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.imirsel.nema.analytics.evaluation.Evaluator;
import org.imirsel.nema.analytics.evaluation.EvaluatorFactory;
import org.imirsel.nema.analytics.evaluation.ResultRenderer;
import org.imirsel.nema.analytics.evaluation.ResultRendererFactory;
import org.imirsel.nema.analytics.evaluation.classification.ClassificationEvaluator;
import org.imirsel.nema.model.NemaData;
import org.imirsel.nema.model.NemaDataConstants;
import org.imirsel.nema.model.NemaDataset;
import org.imirsel.nema.model.NemaEvaluationResultSet;
import org.imirsel.nema.model.NemaTask;
import org.imirsel.nema.model.NemaTrack;
import org.imirsel.nema.model.NemaTrackList;
import org.imirsel.nema.model.fileTypes.ClassificationTextFile;
import org.imirsel.nema.model.fileTypes.MultipleTrackEvalFileType;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.imirsel.nema.test.BaseManagerTestCase;


/**
 * 
 * @author kumaramit01
 * @since 0.2.0
 */
public class ClassificationEvaluationIntegrationTest extends BaseManagerTestCase{

	private NemaTask task;
	private NemaDataset dataset;
	List<NemaTrackList> testSets;
	private static File workingDirectory;
	private static File outputDirectory;
	
	@BeforeClass
	public static void  prepareWorkingLocation(){
		 String tempLocation = System.getProperty("java.io.tmpdir");
	     workingDirectory = new File(tempLocation);
	     outputDirectory = new File(workingDirectory,(System.currentTimeMillis())+"");
	     outputDirectory.mkdirs();
	}
	
	
	@Before
	public void setUp() throws Exception {
		task = new NemaTask();
        task.setId(-1);
        task.setName("Classification evaluator test");
        task.setDescription("Test of the classificaiton evaluator and result renderer");
        task.setDatasetId(10);
        task.setSubjectTrackMetadataId(1);
        task.setSubjectTrackMetadataName(NemaDataConstants.CLASSIFICATION_GENRE);
        
        dataset = new NemaDataset();
        dataset.setId(task.getDatasetId());
        dataset.setName("Test genre dataset");
        dataset.setDescription("Test genre dataset");
        
        File resultsDirectory = new File("src/test/resources/classification/HNOS1");
        MultipleTrackEvalFileType reader = new ClassificationTextFile(task.getSubjectTrackMetadataName());
        List<List<NemaData>> aResultSet = reader.readDirectory(resultsDirectory,".txt");
        
        testSets = new ArrayList<NemaTrackList>(3);
        int id = 0;
        for (Iterator<List<NemaData>> iterator = aResultSet.iterator(); iterator.hasNext();) {
        	ArrayList<NemaTrack> trackList = new ArrayList<NemaTrack>(1000);
            List<NemaData> aList = iterator.next();
        	for (Iterator<NemaData> iterator2 = aList.iterator(); iterator2.hasNext();) {
        		trackList.add(new NemaTrack(iterator2.next().getId()));
        	}
        	testSets.add(new NemaTrackList(id, task.getDatasetId(), 3, "test", id, trackList));
        	id++;
		}
        
	}
	
	

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testEvaluateGT1() throws IllegalArgumentException, IOException, InstantiationException, IllegalAccessException{ 
		File groundTruthFile = new File("src/test/resources/classification/audiolatin.all.gt.txt");
		File hierarchyFile = null;
		File resultsDirectory = new File("src/test/resources/classification/GT1");
		String	systemName = "GT1-System";
		Evaluator evaluator = null;
		ResultRenderer renderer = null;
		
		//evaluator = new ClassificationEvaluator(task, dataset, outputDirectory, workingDirectory, testSets, testSets, false, null, hierarchyFile);
		evaluator = EvaluatorFactory.getEvaluator(task.getSubjectTrackMetadataName(), task, dataset, null, testSets);
		renderer = ResultRendererFactory.getRenderer(task.getSubjectTrackMetadataName(), outputDirectory, workingDirectory, false, null);
		
		MultipleTrackEvalFileType reader = new ClassificationTextFile(task.getSubjectTrackMetadataName());
		List<NemaData> groundTruth = reader.readFile(groundTruthFile);
		evaluator.setGroundTruth(groundTruth);
	
		List<List<NemaData>> resultsForAllFolds = reader.readDirectory(resultsDirectory, null);
		int count = 0;
		for (Iterator<List<NemaData>> iterator = resultsForAllFolds.iterator(); iterator
		.hasNext();) {	
			List<NemaData> oneFoldResults = (List<NemaData>) iterator.next();
			evaluator.addResults(systemName, systemName, testSets.get(count++), oneFoldResults);
		}
		
		NemaEvaluationResultSet results = evaluator.evaluate();
		assertTrue(results != null);
		
		//test rendering
		renderer.renderResults(results);
		
			
		//File resultFile = new File("src/test/resources/classification/evaluation/GT1/report.txt");
		//File outputFile = new File(outputDirectory,systemName+System.getProperty("file.separator")+"report.txt");
		// assertThat(resultFile, fileContentEquals(outputFile));
		
	}
	
	@Test
	public void testEvaluateGT1AndHNOS1()  throws IllegalArgumentException, IOException, InstantiationException, IllegalAccessException{ 
		File groundTruthFile = new File("src/test/resources/classification/audiolatin.all.gt.txt");
		File resultsDirectory1 = new File("src/test/resources/classification/GT1");
		String	systemName1 = "GT1";
		File resultsDirectory2 = new File("src/test/resources/classification/HNOS1");
		String	systemName2 = "HNOS1";
		Evaluator evaluator = null;
		ResultRenderer renderer = null;
		
		evaluator = EvaluatorFactory.getEvaluator(task.getSubjectTrackMetadataName(), task, dataset, null, testSets);
		renderer = ResultRendererFactory.getRenderer(task.getSubjectTrackMetadataName(), outputDirectory, workingDirectory, false, null);
		
		MultipleTrackEvalFileType reader = new ClassificationTextFile(task.getSubjectTrackMetadataName());
		List<NemaData> groundTruth = reader.readFile(groundTruthFile);
		evaluator.setGroundTruth(groundTruth);
	
		List<List<NemaData>> resultsForAllFolds = reader.readDirectory(resultsDirectory1, null);
		int count = 0;
		for (Iterator<List<NemaData>> iterator = resultsForAllFolds.iterator(); iterator
		.hasNext();) {	
			List<NemaData> oneFoldResults = (List<NemaData>) iterator.next();
			evaluator.addResults(systemName1, systemName1, testSets.get(count++), oneFoldResults);
		}
		
		resultsForAllFolds = reader.readDirectory(resultsDirectory2, null);
		count = 0;
		for (Iterator<List<NemaData>> iterator = resultsForAllFolds.iterator(); iterator
		.hasNext();) {	
			List<NemaData> oneFoldResults = (List<NemaData>) iterator.next();
			evaluator.addResults(systemName2, systemName2, testSets.get(count++), oneFoldResults);
		}
		
		
		NemaEvaluationResultSet results = evaluator.evaluate();
		assertTrue(results != null);
		

		//test rendering
		renderer.renderResults(results);
		
		 //File resultFile = new File("src/test/resources/classification/evaluation/HNOS1/report.txt");
		 //File outputFile = new File(outputDirectory,systemName+System.getProperty("file.separator")+"report.txt");
		 //assertThat(resultFile, fileContentEquals(outputFile));
	}

}
