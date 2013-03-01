package org.imirsel.nema.analytics.evaluation.beat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.imirsel.nema.analytics.evaluation.EvaluatorImpl;
import org.imirsel.nema.analytics.util.io.CopyFileFromClassPathToDisk;
import org.imirsel.nema.analytics.util.process.MatlabExecutorImpl;
import org.imirsel.nema.model.NemaData;
import org.imirsel.nema.model.NemaDataConstants;
import org.imirsel.nema.model.NemaEvaluationResultSet;
import org.imirsel.nema.model.NemaTrackList;
import org.imirsel.nema.model.fileTypes.BeatTextFile;
import org.imirsel.nema.model.fileTypes.StructureTextFile;
import org.imirsel.nema.model.util.DeliminatedTextFileUtilities;

public class BeatEvaluator extends EvaluatorImpl {
	
	/**
	 * Constructor (no arg - task, dataset, output and working dirs, training
	 * and test sets must be set manually).
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public BeatEvaluator() {
		super();
	}

	@Override
	public NemaEvaluationResultSet evaluate() throws IllegalArgumentException,
			IOException {
		
		/* prepare NemaEvaluationResultSet*/
		NemaEvaluationResultSet results = getEmptyEvaluationResultSet();
		{
			/* Make sure we have same number of sets of results per jobId (i.e. system), 
			 * as defined in the experiment */
			checkFolds();

			int numJobs = jobIDToFoldResults.size();

			String jobId, jobName;
			Map<NemaTrackList,List<NemaData>> sysResults;

			//evaluate each fold for each system
			Map<String, Map<NemaTrackList,NemaData>> jobIdToFoldEvaluation = new HashMap<String, Map<NemaTrackList,NemaData>>(numJobs);
			for(Iterator<String> it = jobIDToFoldResults.keySet().iterator(); it.hasNext();){
				jobId = it.next();
				getLogger().info("Evaluating experiment folds for jobID: " + jobId);
				sysResults = jobIDToFoldResults.get(jobId);
				Map<NemaTrackList,NemaData> foldEvals = new HashMap<NemaTrackList,NemaData>(testSets.size());
				for (Iterator<NemaTrackList> trackIt = sysResults.keySet().iterator(); trackIt.hasNext();) {
					//make sure we use the evaluators copy of the track list
					NemaTrackList trackList = testSets.get(testSets.indexOf(trackIt.next()));
					NemaData result = evaluateResultFold(jobId, trackList, sysResults.get(trackList));
					foldEvals.put(trackList, result);
				}

				jobIdToFoldEvaluation.put(jobId, foldEvals);
			}

			/* Aggregated evaluation to produce overall results */
			Map<String, NemaData> jobIdToOverallEvaluation = new HashMap<String, NemaData>(numJobs);
			for (Iterator<String> it = jobIDToFoldResults.keySet().iterator(); it.hasNext();) {
				jobId = it.next();
				getLogger().info("Aggregating results for jobID: " + jobId);
				Map<NemaTrackList,NemaData> foldEvals = jobIdToFoldEvaluation.get(jobId);
				NemaData overall = averageFoldMetrics(jobId, foldEvals.values());
				jobIdToOverallEvaluation.put(jobId, overall);
			}

			/* Populate NemaEvaluationResultSet */
			for (Iterator<String> it = jobIDToName.keySet().iterator(); it.hasNext();) {
				jobId = it.next();
				jobName = jobIDToName.get(jobId);
				results.addCompleteResultSet(jobId, jobName, jobIdToOverallEvaluation.get(jobId), jobIdToFoldEvaluation.get(jobId), jobIDToFoldResults.get(jobId));
			}
		}

		return results;
	}

	@Override
	public NemaData evaluateResultFold(String jobID, NemaTrackList testSet,
			List<NemaData> dataList) {
		


		int numExamples = checkFoldResultsAreComplete(jobID, testSet, dataList);
		NemaData outObj = new NemaData(jobID);
		NemaData gtData;

		// Set up temporary directory for the evaluation to take place in
		File evalTempDir = null;
		try {
			evalTempDir = createTempDir("beat");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Copy necessary matlab .m file resources to evalTempDir; initialize matlab properties
		File destFile;
		destFile = new File(evalTempDir.getAbsolutePath() + File.separator + "be_cemgilAcc.m");
		CopyFileFromClassPathToDisk.copy("/org/imirsel/nema/analytics/evaluation/beat/resources/be_cemgilAcc.m", destFile);
		destFile = new File(evalTempDir.getAbsolutePath() + File.separator + "be_confidenceIntervals.m");
		CopyFileFromClassPathToDisk.copy("/org/imirsel/nema/analytics/evaluation/beat/resources/be_confidenceIntervals.m", destFile);
		destFile = new File(evalTempDir.getAbsolutePath() + File.separator + "be_continuityBased.m");
		CopyFileFromClassPathToDisk.copy("/org/imirsel/nema/analytics/evaluation/beat/resources/be_continuityBased.m", destFile);
		destFile = new File(evalTempDir.getAbsolutePath() + File.separator + "be_evalWrapper.m");
		CopyFileFromClassPathToDisk.copy("/org/imirsel/nema/analytics/evaluation/beat/resources/be_evalWrapper.m", destFile);
		destFile = new File(evalTempDir.getAbsolutePath() + File.separator + "be_fMeasure.m");
		CopyFileFromClassPathToDisk.copy("/org/imirsel/nema/analytics/evaluation/beat/resources/be_fMeasure.m", destFile);
		destFile = new File(evalTempDir.getAbsolutePath() + File.separator + "be_gotoAcc.m");
		CopyFileFromClassPathToDisk.copy("/org/imirsel/nema/analytics/evaluation/beat/resources/be_gotoAcc.m", destFile);
		destFile = new File(evalTempDir.getAbsolutePath() + File.separator + "be_informationGain.m");
		CopyFileFromClassPathToDisk.copy("/org/imirsel/nema/analytics/evaluation/beat/resources/be_informationGain.m", destFile);
		destFile = new File(evalTempDir.getAbsolutePath() + File.separator + "be_informationGain_nor.m");
		CopyFileFromClassPathToDisk.copy("/org/imirsel/nema/analytics/evaluation/beat/resources/be_informationGain_nor.m", destFile);
		destFile = new File(evalTempDir.getAbsolutePath() + File.separator + "be_mirexWrapper.m");
		CopyFileFromClassPathToDisk.copy("/org/imirsel/nema/analytics/evaluation/beat/resources/be_mirexWrapper.m", destFile);
		destFile = new File(evalTempDir.getAbsolutePath() + File.separator + "be_params.m");
		CopyFileFromClassPathToDisk.copy("/org/imirsel/nema/analytics/evaluation/beat/resources/be_params.m", destFile);
		destFile = new File(evalTempDir.getAbsolutePath() + File.separator + "be_pScore.m");
		CopyFileFromClassPathToDisk.copy("/org/imirsel/nema/analytics/evaluation/beat/resources/be_pScore.m", destFile);
		//File matlabPath = new File("C:\\MATLAB\\R2009b\\bin\\win64\\MATLAB.exe");
		File matlabPath = new File("matlab");
		String evalCommand = "be_mirexWrapper";
		
		/* Begin track by track evaluation */
		
		// Initialize the file parameters for passing to matlab
		File algFile = null;
		File gtFile = null;
		String algFileName;
		String gtFileName;
		String resultFileName;
		
		// Initialize the per-track and aggregated/averaged results storage variables
		double cemgilAcc = 0.0;
		double fMeasure = 0.0;
		double gotoAcc = 0.0;
		double pScore = 0.0;
		double cmlc = 0.0;
		double cmlt = 0.0;
		double amlc = 0.0;
		double amlt = 0.0;
		double D = 0.0;
		
		double cemgilAccAvg = 0.0;
		double fMeasureAvg = 0.0;
		double gotoAccAvg = 0.0;
		double pScoreAvg = 0.0;
		double cmlcAvg = 0.0;
		double cmltAvg = 0.0;
		double amlcAvg = 0.0;
		double amltAvg = 0.0;
		double DAvg = 0.0;
		double DgAvg = 0.0;
		double[] globalBinHistogram = null;
		
		ArrayList<File> resultFiles = new ArrayList<File>();
		String evalMFileContent = "echo on\n";
		BeatTextFile beatFileWriter = new BeatTextFile();
		
		String filePrefix;
		
		for(NemaData data:dataList){
			gtData = trackIDToGT.get(data.getId());
			filePrefix =  testSet.getId() + "_" + data.getId();
			
			algFileName = evalTempDir.getAbsolutePath() + File.separator + "alg_" + filePrefix + ".txt";
			gtFileName = evalTempDir.getAbsolutePath() + File.separator + "gt_" + filePrefix + ".txt";
			resultFileName = evalTempDir.getAbsolutePath() + File.separator + "res_" + filePrefix + ".txt";
			algFile = new File(algFileName);
			gtFile = new File(gtFileName);
			
			File resultFile = new File(resultFileName);
			resultFiles.add(resultFile);
			
			try{
				beatFileWriter.writeFile(algFile, data);
				beatFileWriter.writeFile(gtFile, gtData);
			}catch(IOException e){
				getLogger().log(Level.SEVERE,"Failed to write out data files for evaluation in matlab!",e);
			}
			evalMFileContent += evalCommand + "('" + gtFile.getAbsolutePath() + "','" + algFile.getAbsolutePath() + "','" + resultFile.getAbsolutePath() + "');\n";		
			//"echo 'evaluating track " + data.getId() + " for job " + jobID + "';\n" + 
		}
		evalMFileContent += "exit;\n";
		byte[] jobIdBytes = {1};
		try {
			jobIdBytes = jobID.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		String functionID = UUID.nameUUIDFromBytes(jobIdBytes).toString();
		String evalFunction = "evaluateJobSet" + testSet.getId();
		File evalMFile = new File(evalTempDir.getAbsolutePath() + File.separator + evalFunction + ".m");
		
		//write out m file
		try{
			BufferedWriter out= new BufferedWriter(new FileWriter(evalMFile));
			try{
				out.write(evalMFileContent);
			}finally{
				out.flush();
				out.close();
			}
		}catch(IOException e){
			getLogger().log(Level.SEVERE,"Failed to write out data files for evaluation in matlab!",e);
		}
		
		MatlabExecutorImpl matlabIntegrator = new MatlabExecutorImpl(evalTempDir,true,evalTempDir,evalTempDir,evalTempDir,"",evalFunction,null);
        matlabIntegrator.setMatlabBin(matlabPath);
        try {
			matlabIntegrator.runCommand(null);
		} catch (Exception e) {
			getLogger().log(Level.SEVERE, "Failed to write beat files and evaluate them using MATLAB",e);
			throw new IllegalArgumentException(e);
		}
        
        for(int i=0;i<resultFiles.size();i++){
        	NemaData data = dataList.get(i);
        	File resultFile = resultFiles.get(i);
	        String[][] beatResultsStrArray = null;
	        
			try {
				beatResultsStrArray = DeliminatedTextFileUtilities.loadDelimTextData(resultFile, ",", -1);
			} catch (Exception e) {
				getLogger().log(Level.SEVERE, "Failed to write beat files and evaluate them using MATLAB",e);
				throw new IllegalArgumentException(e);
			}
			
			fMeasure = Double.valueOf(beatResultsStrArray[0][0]);
			cemgilAcc = Double.valueOf(beatResultsStrArray[0][1]);
			gotoAcc = Double.valueOf(beatResultsStrArray[0][2]);
			pScore = Double.valueOf(beatResultsStrArray[0][3]);
			cmlc = Double.valueOf(beatResultsStrArray[0][4]);
			cmlt = Double.valueOf(beatResultsStrArray[0][5]);
			amlc = Double.valueOf(beatResultsStrArray[0][6]);
			amlt = Double.valueOf(beatResultsStrArray[0][7]);
			D = Double.valueOf(beatResultsStrArray[0][8]);
			
			double[] fileBinHistogram = new double[beatResultsStrArray[0].length - 9];
			for(int b=0; b < fileBinHistogram.length; b++) {
				fileBinHistogram[b] = Double.valueOf(beatResultsStrArray[0][b+9]);
			}
			
			fMeasureAvg += fMeasure;
			cemgilAccAvg += cemgilAcc;
			gotoAccAvg += gotoAcc;
			pScoreAvg += pScore;
			cmlcAvg += cmlc;
			cmltAvg += cmlt;
			amlcAvg += amlc;
			amltAvg += amlt;
			DAvg += D;

			if(globalBinHistogram == null) {
				globalBinHistogram = fileBinHistogram;
			} else {
				for(int b = 0; b<globalBinHistogram.length; b++) {
					globalBinHistogram[b] = globalBinHistogram[b] + fileBinHistogram[b];
				}
			}
			
			/* 
			 * Populate each track's NemaData object with the measures.  */
			data.setMetadata(NemaDataConstants.BEAT_TRACKING_FMEASURE, fMeasure);
			data.setMetadata(NemaDataConstants.BEAT_TRACKING_CEMGIL, cemgilAcc);
			data.setMetadata(NemaDataConstants.BEAT_TRACKING_GOTO, gotoAcc);
			data.setMetadata(NemaDataConstants.BEAT_TRACKING_MCKINNEY, pScore);
			data.setMetadata(NemaDataConstants.BEAT_TRACKING_CMLC, cmlc);
			data.setMetadata(NemaDataConstants.BEAT_TRACKING_CMLT, cmlt);
			data.setMetadata(NemaDataConstants.BEAT_TRACKING_AMLC, amlc);
			data.setMetadata(NemaDataConstants.BEAT_TRACKING_AMLT, amlt);
			data.setMetadata(NemaDataConstants.BEAT_TRACKING_D, D);
			

		}
		
		// Calculate and populate average/summary result 
		fMeasureAvg /= (double)numExamples;
		cemgilAccAvg /= (double)numExamples;
		gotoAccAvg /= (double)numExamples;
		pScoreAvg /= (double)numExamples;
		cmlcAvg /= (double)numExamples;
		cmltAvg /= (double)numExamples;
		amlcAvg /= (double)numExamples;
		amltAvg /= (double)numExamples;
		DAvg /= (double)numExamples;
		DgAvg = computeInformationGain(globalBinHistogram); 
		
		outObj.setMetadata(NemaDataConstants.BEAT_TRACKING_FMEASURE, fMeasureAvg);
		outObj.setMetadata(NemaDataConstants.BEAT_TRACKING_CEMGIL, cemgilAccAvg);
		outObj.setMetadata(NemaDataConstants.BEAT_TRACKING_GOTO, gotoAccAvg);
		outObj.setMetadata(NemaDataConstants.BEAT_TRACKING_MCKINNEY, pScoreAvg);
		outObj.setMetadata(NemaDataConstants.BEAT_TRACKING_CMLC, cmlcAvg);
		outObj.setMetadata(NemaDataConstants.BEAT_TRACKING_CMLT, cmltAvg);
		outObj.setMetadata(NemaDataConstants.BEAT_TRACKING_AMLC, amlcAvg);
		outObj.setMetadata(NemaDataConstants.BEAT_TRACKING_AMLT, amltAvg);
		outObj.setMetadata(NemaDataConstants.BEAT_TRACKING_D, DAvg);
		outObj.setMetadata(NemaDataConstants.BEAT_TRACKING_DG, DgAvg);
		
		// remove temporary directory and files
		recursiveDeleteTempDir(evalTempDir);


		return outObj;
	}

	@Override
	protected void setupEvalMetrics() {
		
		this.trackEvalMetrics.clear();
		this.trackEvalMetrics.add(NemaDataConstants.BEAT_TRACKING_FMEASURE);
		this.trackEvalMetrics.add(NemaDataConstants.BEAT_TRACKING_CEMGIL);
		this.trackEvalMetrics.add(NemaDataConstants.BEAT_TRACKING_GOTO);
		this.trackEvalMetrics.add(NemaDataConstants.BEAT_TRACKING_MCKINNEY);
		this.trackEvalMetrics.add(NemaDataConstants.BEAT_TRACKING_CMLC);
		this.trackEvalMetrics.add(NemaDataConstants.BEAT_TRACKING_CMLT);
		this.trackEvalMetrics.add(NemaDataConstants.BEAT_TRACKING_AMLC);
		this.trackEvalMetrics.add(NemaDataConstants.BEAT_TRACKING_AMLT);
		this.trackEvalMetrics.add(NemaDataConstants.BEAT_TRACKING_D);

		this.overallEvalMetrics.clear();
		this.overallEvalMetrics.clear();
		this.overallEvalMetrics.add(NemaDataConstants.BEAT_TRACKING_FMEASURE);
		this.overallEvalMetrics.add(NemaDataConstants.BEAT_TRACKING_CEMGIL);
		this.overallEvalMetrics.add(NemaDataConstants.BEAT_TRACKING_GOTO);
		this.overallEvalMetrics.add(NemaDataConstants.BEAT_TRACKING_MCKINNEY);
		this.overallEvalMetrics.add(NemaDataConstants.BEAT_TRACKING_CMLC);
		this.overallEvalMetrics.add(NemaDataConstants.BEAT_TRACKING_CMLT);
		this.overallEvalMetrics.add(NemaDataConstants.BEAT_TRACKING_AMLC);
		this.overallEvalMetrics.add(NemaDataConstants.BEAT_TRACKING_AMLT);
		this.overallEvalMetrics.add(NemaDataConstants.BEAT_TRACKING_D);
		this.overallEvalMetrics.add(NemaDataConstants.BEAT_TRACKING_DG);

		//same as overall metrics
		this.foldEvalMetrics = this.overallEvalMetrics;

	}
	
	public static File createTempDir(String prefix) throws IOException
	{
		File sysTempDir = new File(System.getProperty("java.io.tmpdir"));
		String evalTempDirName;
		evalTempDirName = prefix + UUID.randomUUID().toString();
		File evalTempDir = new File(sysTempDir, evalTempDirName);

		if(evalTempDir.mkdirs())
		{
			return evalTempDir;
		}
		else
		{
			throw new IOException(
					"Failed to create temp dir named " +
					evalTempDir.getAbsolutePath());
		}
	}

	/**
	 * Recursively delete file or directory
	 * @param fileOrDir
	 *          the file or dir to delete
	 * @return
	 *          true iff all files are successfully deleted
	 */
	public static boolean recursiveDeleteTempDir(File tmpDir)
	{
		if(!tmpDir.exists()) {
			return true;
		}
		boolean res = true;
		if(tmpDir.isDirectory()) {
			File[] files = tmpDir.listFiles();
			for(int i = 0; i < files.length; i++) {
				res &= recursiveDeleteTempDir(files[i]);
			}
			res = tmpDir.delete();
		} else {
			res = tmpDir.delete();
		}
		return res;
	}
	
	public static double computeInformationGain(double[] binVals) {
		double Dg = 0.0;
		double sumOfBinVals = 0.0;
		for(int i=0; i<binVals.length; i++) {
			binVals[i] += Double.MIN_VALUE;
			sumOfBinVals += binVals[i];
		}
		double entropy = 0.0;
		for(int i=0; i<binVals.length; i++) {
			binVals[i] /= sumOfBinVals;
			entropy += -(binVals[i] * Math.log(binVals[i])/Math.log(2.0));
		}
		Dg = Math.log(binVals.length)/Math.log(2.0) - entropy;
		return Dg;
	}


}
