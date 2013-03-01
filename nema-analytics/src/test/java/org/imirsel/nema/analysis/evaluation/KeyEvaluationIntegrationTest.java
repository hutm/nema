package org.imirsel.nema.analysis.evaluation;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import org.imirsel.nema.analytics.evaluation.Evaluator;
import org.imirsel.nema.analytics.evaluation.EvaluatorFactory;
import org.imirsel.nema.analytics.evaluation.ResultRenderer;
import org.imirsel.nema.analytics.evaluation.ResultRendererFactory;
import org.imirsel.nema.model.NemaData;
import org.imirsel.nema.model.NemaDataConstants;
import org.imirsel.nema.model.NemaDataset;
import org.imirsel.nema.model.NemaEvaluationResultSet;
import org.imirsel.nema.model.NemaTask;
import org.imirsel.nema.model.NemaTrackList;
import org.imirsel.nema.model.NemaTrack;
import org.imirsel.nema.model.fileTypes.*;
import org.imirsel.nema.test.BaseManagerTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.imirsel.nema.analytics.evaluation.Evaluator;


public class KeyEvaluationIntegrationTest extends BaseManagerTestCase{


	private NemaTask task;
	private NemaDataset dataset;
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
        task.setId(17);
        task.setName("Key MIREX09");
        task.setDescription("Key transcription task requiring participants to annotate and segment the chord events in the MIREX09chord transcription dataset.");
        task.setDatasetId(33);
        task.setSubjectTrackMetadataId(13);
        task.setSubjectTrackMetadataName(NemaDataConstants.KEY_DETECTION_DATA);
        
        dataset = new NemaDataset();
        dataset.setId(task.getDatasetId());
        dataset.setName("MIREX09 Chord");
        dataset.setDescription("MIREX 2009 Chord transcription dataset composed of Christopher Harte's Beatles dataset (C4DM, Queen Mary's University of London) and Matthias Mauch's Queen and Zweieck dataset (C4DM, Queen Mary's University of London)");
        
    }
	
	@Test
	public void testEvaluateShortHandBasedSystem() throws FileNotFoundException, IOException, IllegalArgumentException, IOException, InstantiationException, IllegalAccessException{ 
		List<NemaTrackList> testSets;
		File groundTruthDirectory = new File("/home/hut/Beatles/labelsKey");
        SingleTrackEvalFileType reader = new KeyTextFile();
        List<NemaData> groundTruth = reader.readDirectory(groundTruthDirectory, null);
        
        ArrayList<NemaTrack> trackList = new ArrayList<NemaTrack>(groundTruth.size());
        for (Iterator<NemaData> iterator = groundTruth.iterator(); iterator.hasNext();) {
        	trackList.add(new NemaTrack(iterator.next().getId()));
		}
        
        testSets = new ArrayList<NemaTrackList>(1);
        int id = 0;
        testSets.add(new NemaTrackList(id, task.getDatasetId(), 3, "test", id, trackList));
        id++;
        

		File resultsDirectory = new File("/home/hut/Beatles/labelsKey");
		String	systemName = "KeyGT-System";
		Evaluator evaluator = null;
		ResultRenderer renderer = null;
		
		//test reader and setup for evaluation
//		evaluator = new ChordEvaluator(task, dataset, outputDirectory, workingDirectory, testSets, false, null);
		evaluator = EvaluatorFactory.getEvaluator(task.getSubjectTrackMetadataName(), task, dataset, null, testSets);
		renderer = ResultRendererFactory.getRenderer(task.getSubjectTrackMetadataName(), outputDirectory, workingDirectory, false, null);
		
		evaluator.setGroundTruth(groundTruth);
	
		List<NemaData> resultsForAllTracks = reader.readDirectory(resultsDirectory, null);
		evaluator.addResults(systemName, systemName, testSets.get(0), resultsForAllTracks);
		
		
		//test evaluation
		NemaEvaluationResultSet results = evaluator.evaluate();
		assertTrue(results != null);
		
		//test rendering
		renderer.renderResults(results);
	}

	@Test
	public void testEvaluateManyShortHandBasedSystems() throws FileNotFoundException, IOException, IllegalArgumentException, IOException, InstantiationException, IllegalAccessException{ 
		List<NemaTrackList> testSets;
		File groundTruthDirectory = new File("src/test/resources/chord/long/groundtruth");
        SingleTrackEvalFileType reader = new ChordShortHandTextFile();
        List<NemaData> groundTruth = reader.readDirectory(groundTruthDirectory, null);
        
        ArrayList<NemaTrack> trackList = new ArrayList<NemaTrack>(groundTruth.size());
        for (Iterator<NemaData> iterator = groundTruth.iterator(); iterator.hasNext();) {
        	trackList.add(new NemaTrack(iterator.next().getId()));
		}
        
        testSets = new ArrayList<NemaTrackList>(1);
        int id = 0;
        testSets.add(new NemaTrackList(id, task.getDatasetId(), 3, "test", id, trackList));
        id++;
        
        
		File resultsDirectory = new File("src/test/resources/chord/long/results");
		
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
		
		//test reader and setup for evaluation
		evaluator = EvaluatorFactory.getEvaluator(task.getSubjectTrackMetadataName(), task, dataset, null, testSets);
		renderer = ResultRendererFactory.getRenderer(task.getSubjectTrackMetadataName(), outputDirectory, workingDirectory, false, null);
		
		evaluator.setGroundTruth(groundTruth);
	
		//read system results
		for (int i = 0; i < systemNames.size(); i++) {
			List<NemaData> resultsForAllTracks = reader.readDirectory(systemDirs.get(i), null);
			evaluator.addResults(systemNames.get(i), systemNames.get(i), testSets.get(0), resultsForAllTracks);
		}
		
		//test evaluation
		//test evaluation
		NemaEvaluationResultSet results = evaluator.evaluate();
		assertTrue(results != null);

		//test rendering
		renderer.renderResults(results);
	}
	
	@After
	public void tearDown() throws Exception {
	}

}
