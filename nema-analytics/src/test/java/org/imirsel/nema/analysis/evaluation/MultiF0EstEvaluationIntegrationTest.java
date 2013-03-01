package org.imirsel.nema.analysis.evaluation;


//import static org.imirsel.nema.test.matchers.NemaMatchers.fileContentEquals;

import static org.junit.Assert.assertTrue;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.imirsel.nema.analytics.evaluation.Evaluator;
import org.imirsel.nema.analytics.evaluation.EvaluatorFactory;
import org.imirsel.nema.analytics.evaluation.ResultRenderer;
import org.imirsel.nema.analytics.evaluation.ResultRendererFactory;

import org.imirsel.nema.model.NemaData;
import org.imirsel.nema.model.NemaDataConstants;
import org.imirsel.nema.model.NemaDataset;
import org.imirsel.nema.model.NemaEvaluationResultSet;
import org.imirsel.nema.model.NemaTask;
import org.imirsel.nema.model.NemaTrack;
import org.imirsel.nema.model.NemaTrackList;
import org.imirsel.nema.model.fileTypes.MelodyTextFile;
import org.imirsel.nema.model.fileTypes.MultiF0EstTextFile;
import org.imirsel.nema.model.fileTypes.SingleTrackEvalFileType;
import org.imirsel.nema.test.BaseManagerTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MultiF0EstEvaluationIntegrationTest extends BaseManagerTestCase{

	private NemaTask singleSetTask;
	private NemaTask twoSetTask;
	private NemaDataset singleSetDataset;
	private NemaDataset twoSetDataset;
	private List<NemaTrackList> singleTestSet;
	private List<NemaTrackList> twoTestSets;
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
		singleSetTask = new NemaTask();
        singleSetTask.setId(1);
        singleSetTask.setName("single fold task name");
        singleSetTask.setDescription("single fold task description");
        singleSetTask.setDatasetId(1);
        singleSetTask.setSubjectTrackMetadataId(11);
        singleSetTask.setSubjectTrackMetadataName(NemaDataConstants.MULTI_F0_EST_DATA);
        
        twoSetTask = new NemaTask();
        twoSetTask.setId(2);
        twoSetTask.setName("two fold task name");
        twoSetTask.setDescription("two fold task description");
        twoSetTask.setDatasetId(2);
        twoSetTask.setSubjectTrackMetadataId(11);
        twoSetTask.setSubjectTrackMetadataName(NemaDataConstants.MULTI_F0_EST_DATA);
        
        singleSetDataset = new NemaDataset();
        singleSetDataset.setId(singleSetTask.getDatasetId());
        singleSetDataset.setName("Single fold dataset name");
        singleSetDataset.setDescription("Single fold dataset description");
        
        twoSetDataset = new NemaDataset();
        twoSetDataset.setId(twoSetTask.getDatasetId());
        twoSetDataset.setName("Two fold dataset name");
        twoSetDataset.setDescription("Two fold dataset description");
        
        int idtrackListId = 0;
        {
	        ArrayList<NemaTrack> trackList = new ArrayList<NemaTrack>(4);
	        trackList.add(new NemaTrack("daisy1"));
	        trackList.add(new NemaTrack("daisy2"));
	        trackList.add(new NemaTrack("daisy3"));
	        trackList.add(new NemaTrack("daisy4"));
	        singleTestSet = new ArrayList<NemaTrackList>(1);
	        singleTestSet.add(new NemaTrackList(idtrackListId, singleSetTask.getDatasetId(), 3, "test", idtrackListId, trackList));
	        idtrackListId++;
        }
        
        {
	        ArrayList<NemaTrack> trackList1 = new ArrayList<NemaTrack>(2);
	        trackList1.add(new NemaTrack("daisy1"));
	        trackList1.add(new NemaTrack("daisy2"));
	        
	        ArrayList<NemaTrack> trackList2 = new ArrayList<NemaTrack>(2);
	        trackList2.add(new NemaTrack("daisy3"));
	        trackList2.add(new NemaTrack("daisy4"));
	        
	        twoTestSets = new ArrayList<NemaTrackList>(1);
	        int foldNum = 0;
	        twoTestSets.add(new NemaTrackList(idtrackListId, twoSetTask.getDatasetId(), 3, "test", foldNum++, trackList1));
	        idtrackListId++;
	        twoTestSets.add(new NemaTrackList(idtrackListId, twoSetTask.getDatasetId(), 3, "test", foldNum++, trackList2));
	        idtrackListId++;
        }
    }
	
	


	@Test
	public void testEvaluateManySystems()  throws IllegalArgumentException, IOException, InstantiationException, IllegalAccessException{ 

		
		File groundTruthDirectory = new File("src/test/resources/multiF0Est/groundtruth");
		File resultsDirectory = new File("src/test/resources/multiF0Est/NEOS1");
		
		List<File> systemDirs = new ArrayList<File>();
		List<String> systemNames = new ArrayList<String>();
		File [] files = resultsDirectory.listFiles();
        for (int i = 0; i < files.length; i++) {
			if(files[i].isDirectory() && !(files[i].getName().equals(".svn"))){
				String systemName = files[i].getName();
				
				System.out.println("got system: " + systemName);
				systemDirs.add(files[i]);
				systemNames.add(systemName);
			}
		}
		
		Evaluator evaluator = null;
		ResultRenderer renderer = null;
		
		//evaluator = new MelodyEvaluator(task, dataset, outputDirectory, workingDirectory, testSets);
		evaluator = EvaluatorFactory.getEvaluator(singleSetTask.getSubjectTrackMetadataName(), singleSetTask, singleSetDataset, null, singleTestSet);
		renderer = ResultRendererFactory.getRenderer(singleSetTask.getSubjectTrackMetadataName(), outputDirectory, workingDirectory, false, null);
		SingleTrackEvalFileType reader = new MultiF0EstTextFile();
		
		List<NemaData> groundTruth = reader.readDirectory(groundTruthDirectory, ".txt");
		evaluator.setGroundTruth(groundTruth);
	
		for (int i = 0; i < systemNames.size(); i++) {
			List<NemaData> resultsForAllTracks = reader.readDirectory(systemDirs.get(i), null);
			evaluator.addResults(systemNames.get(i), systemNames.get(i), singleTestSet.get(0), resultsForAllTracks);
		}
		
	
		NemaEvaluationResultSet results = evaluator.evaluate();
		assertTrue(results != null);
		
		//test rendering
		renderer.renderResults(results);
		
	  //File resultFile = new File("src/test/resources/classification/evaluation/GT1/report.txt");
	  //File outputFile = new File(outputDirectory,systemName+System.getProperty("file.separator")+"report.txt");
	 
	  //assertThat(resultFile, fileContentEquals(outputFile));
	
	}
	
	
	

	@After
	public void tearDown() throws Exception {
	}

}
