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
import org.imirsel.nema.model.fileTypes.SingleTrackEvalFileType;
import org.imirsel.nema.model.util.PathAndTagCleaner;
import org.imirsel.nema.test.BaseManagerTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MelodyEvaluationIntegrationTest extends BaseManagerTestCase{

	private NemaTask singleSetTask;
	private NemaTask twoSetTask;
	private NemaDataset singleSetDataset;
	private NemaDataset twoSetDataset;
	private List<NemaTrackList> singleTestSet;
	private List<NemaTrackList> twoTestSets;
	private static File workingDirectory;
	private static File outputDirectory;
	File groundTruthDirectory;
	
	@BeforeClass
	public static void  prepareWorkingLocation(){
		 String tempLocation = System.getProperty("java.io.tmpdir");
	     workingDirectory = new File(tempLocation);
	     outputDirectory = new File(workingDirectory,(System.currentTimeMillis())+"");
	     outputDirectory.mkdirs();
	}
	
	
	@Before
	public void setUp() throws Exception {
		
		groundTruthDirectory = new File("src/test/resources/melody/groundtruth");
		
		singleSetTask = new NemaTask();
        singleSetTask.setId(10);
        singleSetTask.setName("Melody Extraction ADC04 Vocal");
        singleSetTask.setDescription("Melody extraction from audio task requiring participants to annotate the dominant F0 at each 10ms timestep on the ADC04 Vocal dataset.");
        singleSetTask.setDatasetId(26);
        singleSetTask.setSubjectTrackMetadataId(11);
        singleSetTask.setSubjectTrackMetadataName(NemaDataConstants.MELODY_EXTRACTION_DATA);
        
        twoSetTask = new NemaTask();
        twoSetTask.setId(10);
        twoSetTask.setName("two fold task name");
        twoSetTask.setDescription("two fold task description");
        twoSetTask.setDatasetId(2);
        twoSetTask.setSubjectTrackMetadataId(11);
        twoSetTask.setSubjectTrackMetadataName(NemaDataConstants.MELODY_EXTRACTION_DATA);
        
        singleSetDataset = new NemaDataset();
        singleSetDataset.setId(singleSetTask.getDatasetId());
        singleSetDataset.setName("ADC04 Melody Vocal");
        singleSetDataset.setDescription("Audio Description Contest 2004 (ADC04) Melody transcription vocal dataset collected by Emilia Gmez, Beesuan Ong and Sebastian Streich (MTG-UP)");
        
        twoSetDataset = new NemaDataset();
        twoSetDataset.setId(twoSetTask.getDatasetId());
        twoSetDataset.setName("Two fold dataset name");
        twoSetDataset.setDescription("Two fold dataset description");
        
        int idtrackListId = 0;
        {
	        ArrayList<NemaTrack> trackList = new ArrayList<NemaTrack>(4);
	        File [] files = groundTruthDirectory.listFiles();
	        for (int i = 0; i < files.length; i++) {
				if(files[i].getName().endsWith(".txt")){
					String id = PathAndTagCleaner.convertFileToMIREX_ID(files[i]);
					trackList.add(new NemaTrack(id));
					System.out.println("got track: " + id);
				}
			}
//	        trackList.add(new NemaTrack("daisy1"));
//	        trackList.add(new NemaTrack("daisy2"));
//	        trackList.add(new NemaTrack("daisy3"));
//	        trackList.add(new NemaTrack("daisy4"));
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
	public void testEvaluateKDTwoFolds()  throws IllegalArgumentException, IOException, InstantiationException, IllegalAccessException{ 
		
		File resultsDirectory1 = new File("src/test/resources/melody/KD-2-fold/fold1");
		File resultsDirectory2 = new File("src/test/resources/melody/KD-2-fold/fold2");
		String	systemName = "SystemName";
		Evaluator evaluator = null;
		ResultRenderer renderer = null;
		
		//evaluator = new MelodyEvaluator(task, dataset, outputDirectory, workingDirectory, testSets);
		evaluator = EvaluatorFactory.getEvaluator(twoSetTask.getSubjectTrackMetadataName(), twoSetTask, twoSetDataset, null, twoTestSets);
		renderer = ResultRendererFactory.getRenderer(twoSetTask.getSubjectTrackMetadataName(), outputDirectory, workingDirectory, false, null);
		SingleTrackEvalFileType reader = new MelodyTextFile();
		
		List<NemaData> groundTruth = reader.readDirectory(groundTruthDirectory, ".txt");
		evaluator.setGroundTruth(groundTruth);
	
		List<NemaData> results1 = reader.readDirectory(resultsDirectory1, null);
		List<NemaData> results2 = reader.readDirectory(resultsDirectory2, null);
		
		//divide into the two folds
		
		evaluator.addResults(systemName, "**FlowID**", twoTestSets.get(0), results1);
		evaluator.addResults(systemName, "**FlowID**", twoTestSets.get(1), results2);
		
		NemaEvaluationResultSet evalResults = evaluator.evaluate();
		assertTrue(evalResults != null);
		

		//test rendering
		renderer.renderResults(evalResults);
		
	  //File resultFile = new File("src/test/resources/classification/evaluation/GT1/report.txt");
	  //File outputFile = new File(outputDirectory,systemName+System.getProperty("file.separator")+"report.txt");
	 
	  //assertThat(resultFile, fileContentEquals(outputFile));
	
	}
	
	@Test
	public void testEvaluateManySystems()  throws IllegalArgumentException, IOException, InstantiationException, IllegalAccessException{ 
		File groundTruthDirectory = new File("src/test/resources/melody/groundtruth");
		File resultsDirectory = new File("src/test/resources/melody/results");
		
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
		SingleTrackEvalFileType reader = new MelodyTextFile();
		
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
