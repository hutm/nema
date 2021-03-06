package org.imirsel.nema.analysis.evaluation;

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
import org.imirsel.nema.model.fileTypes.StructureTextFile;
import org.imirsel.nema.model.util.PathAndTagCleaner;
import org.imirsel.nema.test.BaseManagerTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class StructureEvaluationIntegrationTest extends BaseManagerTestCase {
	
	private NemaTask singleSetTask;
	private NemaDataset singleSetDataset;
	private List<NemaTrackList> singleTestSet;
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
		
		groundTruthDirectory = new File("src/test/resources/structure/groundtruth");
		
		singleSetTask = new NemaTask();
        singleSetTask.setId(19);
        singleSetTask.setName("Structure MIREX09");
        singleSetTask.setDescription("Structural Segmentation task requiring participants to analyse tracks and determine a (verse/chorus/bridge etc) structure and repeated sections in the music.");
        singleSetTask.setDatasetId(36);
        singleSetTask.setSubjectTrackMetadataId(15);
        singleSetTask.setSubjectTrackMetadataName(NemaDataConstants.STRUCTURE_SEGMENTATION_DATA);
        
        singleSetDataset = new NemaDataset();
        singleSetDataset.setId(singleSetTask.getDatasetId());
        singleSetDataset.setName("MIREX09 Structure");
        singleSetDataset.setDescription("MIREX 2009 Structural segmentation dataset collected by Jouni Paulus (Tampere University of Technology), Ewald Peiszer (Vienna University of Technology) and C4DM (Queen Mary's University of London)");
        
        int idtrackListId = 0;
        {
	        ArrayList<NemaTrack> trackList = new ArrayList<NemaTrack>();
	        
	        File [] files = groundTruthDirectory.listFiles();
	        for (int i = 0; i < files.length; i++) {
				if(files[i].getName().endsWith(".txt")){
					String id = PathAndTagCleaner.convertFileToMIREX_ID(files[i]);
					trackList.add(new NemaTrack(id));
					System.out.println("got track: " + id);
				}
			}
	        
	        singleTestSet = new ArrayList<NemaTrackList>(1);
	        singleTestSet.add(new NemaTrackList(idtrackListId, singleSetTask.getDatasetId(), 3, "test", idtrackListId, trackList));
	        idtrackListId++;
        }

    }
	
	@Test
	public void testEvaluateStruct()  throws IllegalArgumentException, IOException, InstantiationException, IllegalAccessException{ 

		File resultsDirectory = new File("src/test/resources/structure/results");
		
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
		SingleTrackEvalFileType reader = new StructureTextFile();
		
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
