package org.imirsel.nema.analytics.evaluation.structure;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
import org.imirsel.nema.model.fileTypes.StructureTextFile;
import org.imirsel.nema.model.util.DeliminatedTextFileUtilities;

/**
 * 
 * Wraps Paulus' matlab structure eval script.
 * 
 * @author Andreas Ehmann
 * @author kris.west@gmail.com
 * @since 0.3.0
 *
 */
public class StructureEvaluator extends EvaluatorImpl {

	/**
	 * Constructor (no arg - task, dataset, output and working dirs, training
	 * and test sets must be set manually).
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public StructureEvaluator() {
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
			List<NemaData> dataList) throws IllegalArgumentException{


		int numExamples = checkFoldResultsAreComplete(jobID, testSet, dataList);
		NemaData outObj = new NemaData(jobID);
		NemaData gtData;

		// Set up temporary directory for the evaluation to take place in
		File evalTempDir = null;
		try {
			evalTempDir = createTempDir("struct");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Copy necessary matlab .m file resources to evalTempDir; initialize matlab properties
		File destFile;
		destFile = new File(evalTempDir.getAbsolutePath() + File.separator + "condEntropyEval.m");
		CopyFileFromClassPathToDisk.copy("/org/imirsel/nema/analytics/evaluation/structure/resources/condEntropyEval.m", destFile);
		destFile = new File(evalTempDir.getAbsolutePath() + File.separator + "desc2seq.m");
		CopyFileFromClassPathToDisk.copy("/org/imirsel/nema/analytics/evaluation/structure/resources/desc2seq.m", destFile);
		destFile = new File(evalTempDir.getAbsolutePath() + File.separator + "evaluateStructure.m");
		CopyFileFromClassPathToDisk.copy("/org/imirsel/nema/analytics/evaluation/structure/resources/evaluateStructure.m", destFile);
		destFile = new File(evalTempDir.getAbsolutePath() + File.separator + "findUniqueMapping.m");
		CopyFileFromClassPathToDisk.copy("/org/imirsel/nema/analytics/evaluation/structure/resources/findUniqueMapping.m", destFile);
		destFile = new File(evalTempDir.getAbsolutePath() + File.separator + "pairwiseF.m");
		CopyFileFromClassPathToDisk.copy("/org/imirsel/nema/analytics/evaluation/structure/resources/pairwiseF.m", destFile);
		destFile = new File(evalTempDir.getAbsolutePath() + File.separator + "randClusteringIndex.m");
		CopyFileFromClassPathToDisk.copy("/org/imirsel/nema/analytics/evaluation/structure/resources/randClusteringIndex.m", destFile);
		destFile = new File(evalTempDir.getAbsolutePath() + File.separator + "readStructureFromLab.m");
		CopyFileFromClassPathToDisk.copy("/org/imirsel/nema/analytics/evaluation/structure/resources/readStructureFromLab.m", destFile);
		destFile = new File(evalTempDir.getAbsolutePath() + File.separator + "segmentRetrievalEval2.m");
		CopyFileFromClassPathToDisk.copy("/org/imirsel/nema/analytics/evaluation/structure/resources/segmentRetrievalEval2.m", destFile);
		destFile = new File(evalTempDir.getAbsolutePath() + File.separator + "seqAssignEval.m");
		CopyFileFromClassPathToDisk.copy("/org/imirsel/nema/analytics/evaluation/structure/resources/seqAssignEval.m", destFile);

		// File matlabPath = new File("C:\\MATLAB\\R2009b\\bin\\win64\\MATLAB.exe");
		File matlabPath = new File("matlab");
		String evalCommand = "evaluateStructure";
		
		/* Begin track by track evaluation */
		
		// Initialize the file parameters for passing to matlab
		File algFile = null;
		File gtFile = null;
		String algFileName;
		String gtFileName;
		String resultFileName;
		
		// Initialize the per-track and aggregated/averaged results storage variables
		double overSegScore = 0.0;
		double underSegScore = 0.0;
		double pwF = 0.0;
		double pwPrecision = 0.0;
		double pwRecall = 0.0;
		double R = 0.0;
		double fMeasure05 = 0.0;
		double precRate05 = 0.0;
		double recRate05 = 0.0;
		double fMeasure3 = 0.0;
		double precRate3 = 0.0;
		double recRate3 = 0.0;
		double medianTrue2Claim = 0.0;
		double medianClaim2True = 0.0;
		
		double overSegScoreAvg = 0.0;
		double underSegScoreAvg = 0.0;
		double pwFAvg = 0.0;
		double pwPrecisionAvg = 0.0;
		double pwRecallAvg = 0.0;
		double RAvg = 0.0;
		double fMeasure05Avg = 0.0;
		double precRate05Avg = 0.0;
		double recRate05Avg = 0.0;
		double fMeasure3Avg = 0.0;
		double precRate3Avg = 0.0;
		double recRate3Avg = 0.0;
		double medianTrue2ClaimAvg = 0.0;
		double medianClaim2TrueAvg = 0.0;
		
		ArrayList<File> resultFiles = new ArrayList<File>();
		String evalMFileContent = "echo on\n";
		StructureTextFile structFileWriter = new StructureTextFile();
		
		String filePrefix;
		
		for(NemaData data:dataList){
			gtData = trackIDToGT.get(data.getId());
			filePrefix = testSet.getId() + "_" + data.getId();
			
			algFileName = evalTempDir.getAbsolutePath() + File.separator + "alg_" + filePrefix + ".txt";
			gtFileName = evalTempDir.getAbsolutePath() + File.separator + "gt_" + filePrefix + ".txt";
			resultFileName = evalTempDir.getAbsolutePath() + File.separator + "res_" + filePrefix + ".txt";
			algFile = new File(algFileName);
			gtFile = new File(gtFileName);
			
			File resultFile = new File(resultFileName);
			resultFiles.add(resultFile);
			
			try{
				structFileWriter.writeFile(algFile, data);
				structFileWriter.writeFile(gtFile, gtData);
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
		//String functionID = UUID.nameUUIDFromBytes(jobIdBytes).toString();
		
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
			getLogger().log(Level.SEVERE, "Failed to write structure files and evaluate them using MATLAB",e);
			throw new IllegalArgumentException(e);
		}
        
        for(int i=0;i<resultFiles.size();i++){
        	NemaData data = dataList.get(i);
        	File resultFile = resultFiles.get(i);
	        String[][] structResultsStrArray = null;
	        
			try {
				structResultsStrArray = DeliminatedTextFileUtilities.loadDelimTextData(resultFile, ",", -1);
			} catch (Exception e) {
				getLogger().log(Level.SEVERE, "Failed to write structure files and evaluate them using MATLAB",e);
				throw new IllegalArgumentException(e);
			}
			
			overSegScore = Double.valueOf(structResultsStrArray[0][0]);
			underSegScore = Double.valueOf(structResultsStrArray[0][1]);
			pwF = Double.valueOf(structResultsStrArray[0][2]);
			pwPrecision = Double.valueOf(structResultsStrArray[0][3]);
			pwRecall = Double.valueOf(structResultsStrArray[0][4]);
			R = Double.valueOf(structResultsStrArray[0][5]);
			fMeasure05 = Double.valueOf(structResultsStrArray[0][6]);
			precRate05 = Double.valueOf(structResultsStrArray[0][7]);
			recRate05 = Double.valueOf(structResultsStrArray[0][8]);
			fMeasure3 = Double.valueOf(structResultsStrArray[0][9]);
			precRate3 = Double.valueOf(structResultsStrArray[0][10]);
			recRate3 = Double.valueOf(structResultsStrArray[0][11]);
			medianTrue2Claim = Double.valueOf(structResultsStrArray[0][12]);
			medianClaim2True = Double.valueOf(structResultsStrArray[0][13]);
			
			overSegScoreAvg += overSegScore;
			underSegScoreAvg += underSegScore;
			pwFAvg += pwF;
			pwPrecisionAvg += pwPrecision;
			pwRecallAvg += pwRecall;
			RAvg += R;
			fMeasure05Avg += fMeasure05;
			precRate05Avg += precRate05;
			recRate05Avg += recRate05;
			fMeasure3Avg += fMeasure3;
			precRate3Avg += precRate3;
			recRate3Avg += recRate3;
			medianTrue2ClaimAvg += medianTrue2Claim;
			medianClaim2TrueAvg += medianClaim2True;
			
			/* 
			 * Populate each track's NemaData object with the measures.  */
			data.setMetadata(NemaDataConstants.STRUCTURE_SEGMENTATION_OVERSEGSCORE, overSegScore);
			data.setMetadata(NemaDataConstants.STRUCTURE_SEGMENTATION_UNDERSEGSCORE, underSegScore);
			data.setMetadata(NemaDataConstants.STRUCTURE_SEGMENTATION_PWF, pwF);
			data.setMetadata(NemaDataConstants.STRUCTURE_SEGMENTATION_PWPRECISION, pwPrecision);
			data.setMetadata(NemaDataConstants.STRUCTURE_SEGMENTATION_PWRECALL, pwRecall);
			data.setMetadata(NemaDataConstants.STRUCTURE_SEGMENTATION_R, R);
			data.setMetadata(NemaDataConstants.STRUCTURE_SEGMENTATION_FMEASUREATPOINTFIVE, fMeasure05);
			data.setMetadata(NemaDataConstants.STRUCTURE_SEGMENTATION_PRECRATEATPOINTFIVE, precRate05);
			data.setMetadata(NemaDataConstants.STRUCTURE_SEGMENTATION_RECRATEATPOINTFIVE, recRate05);
			data.setMetadata(NemaDataConstants.STRUCTURE_SEGMENTATION_FMEASUREATTHREE, fMeasure3);
			data.setMetadata(NemaDataConstants.STRUCTURE_SEGMENTATION_PRECRATEATTHREE, precRate3);
			data.setMetadata(NemaDataConstants.STRUCTURE_SEGMENTATION_RECRATEATTHREE, recRate3);
			data.setMetadata(NemaDataConstants.STRUCTURE_SEGMENTATION_MEDCLAIM2TRUE, medianTrue2Claim);
			data.setMetadata(NemaDataConstants.STRUCTURE_SEGMENTATION_MEDTRUE2CLAIM, medianClaim2True);

		}
		
		// Calculate and populate average/summary result 
		overSegScoreAvg /= (double)numExamples;
		underSegScoreAvg /= (double)numExamples;
		pwFAvg /= (double)numExamples;
		pwPrecisionAvg /= (double)numExamples;
		pwRecallAvg /= (double)numExamples;
		RAvg /= (double)numExamples;
		fMeasure05Avg /= (double)numExamples;
		precRate05Avg /= (double)numExamples;
		recRate05Avg /= (double)numExamples;
		fMeasure3Avg /= (double)numExamples;
		precRate3Avg /= (double)numExamples;
		recRate3Avg /= (double)numExamples;
		medianTrue2ClaimAvg /= (double)numExamples;
		medianClaim2TrueAvg /= (double)numExamples;
		
		outObj.setMetadata(NemaDataConstants.STRUCTURE_SEGMENTATION_OVERSEGSCORE, overSegScoreAvg);
		outObj.setMetadata(NemaDataConstants.STRUCTURE_SEGMENTATION_UNDERSEGSCORE, underSegScoreAvg);
		outObj.setMetadata(NemaDataConstants.STRUCTURE_SEGMENTATION_PWF, pwFAvg);
		outObj.setMetadata(NemaDataConstants.STRUCTURE_SEGMENTATION_PWPRECISION, pwPrecisionAvg);
		outObj.setMetadata(NemaDataConstants.STRUCTURE_SEGMENTATION_PWRECALL, pwRecallAvg);
		outObj.setMetadata(NemaDataConstants.STRUCTURE_SEGMENTATION_R, RAvg);
		outObj.setMetadata(NemaDataConstants.STRUCTURE_SEGMENTATION_FMEASUREATPOINTFIVE, fMeasure05Avg);
		outObj.setMetadata(NemaDataConstants.STRUCTURE_SEGMENTATION_PRECRATEATPOINTFIVE, precRate05Avg);
		outObj.setMetadata(NemaDataConstants.STRUCTURE_SEGMENTATION_RECRATEATPOINTFIVE, recRate05Avg);
		outObj.setMetadata(NemaDataConstants.STRUCTURE_SEGMENTATION_FMEASUREATTHREE, fMeasure3Avg);
		outObj.setMetadata(NemaDataConstants.STRUCTURE_SEGMENTATION_PRECRATEATTHREE, precRate3Avg);
		outObj.setMetadata(NemaDataConstants.STRUCTURE_SEGMENTATION_RECRATEATTHREE, recRate3Avg);
		outObj.setMetadata(NemaDataConstants.STRUCTURE_SEGMENTATION_MEDCLAIM2TRUE, medianTrue2ClaimAvg);
		outObj.setMetadata(NemaDataConstants.STRUCTURE_SEGMENTATION_MEDTRUE2CLAIM, medianClaim2TrueAvg);
		
		// remove temporary directory and files
		recursiveDeleteTempDir(evalTempDir);


		return outObj;
	}

	@Override
	protected void setupEvalMetrics() {

		this.trackEvalMetrics.clear();
		this.trackEvalMetrics.add(NemaDataConstants.STRUCTURE_SEGMENTATION_OVERSEGSCORE);
		this.trackEvalMetrics.add(NemaDataConstants.STRUCTURE_SEGMENTATION_UNDERSEGSCORE);
		this.trackEvalMetrics.add(NemaDataConstants.STRUCTURE_SEGMENTATION_PWF);
		this.trackEvalMetrics.add(NemaDataConstants.STRUCTURE_SEGMENTATION_PWPRECISION);
		this.trackEvalMetrics.add(NemaDataConstants.STRUCTURE_SEGMENTATION_PWRECALL);
		this.trackEvalMetrics.add(NemaDataConstants.STRUCTURE_SEGMENTATION_R);
		this.trackEvalMetrics.add(NemaDataConstants.STRUCTURE_SEGMENTATION_FMEASUREATPOINTFIVE);
		this.trackEvalMetrics.add(NemaDataConstants.STRUCTURE_SEGMENTATION_PRECRATEATPOINTFIVE);
		this.trackEvalMetrics.add(NemaDataConstants.STRUCTURE_SEGMENTATION_RECRATEATPOINTFIVE);
		this.trackEvalMetrics.add(NemaDataConstants.STRUCTURE_SEGMENTATION_FMEASUREATTHREE);
		this.trackEvalMetrics.add(NemaDataConstants.STRUCTURE_SEGMENTATION_PRECRATEATTHREE);
		this.trackEvalMetrics.add(NemaDataConstants.STRUCTURE_SEGMENTATION_RECRATEATTHREE);
		this.trackEvalMetrics.add(NemaDataConstants.STRUCTURE_SEGMENTATION_MEDCLAIM2TRUE);
		this.trackEvalMetrics.add(NemaDataConstants.STRUCTURE_SEGMENTATION_MEDTRUE2CLAIM);


		this.overallEvalMetrics.clear();
		this.overallEvalMetrics.clear();
		this.overallEvalMetrics.add(NemaDataConstants.STRUCTURE_SEGMENTATION_OVERSEGSCORE);
		this.overallEvalMetrics.add(NemaDataConstants.STRUCTURE_SEGMENTATION_UNDERSEGSCORE);
		this.overallEvalMetrics.add(NemaDataConstants.STRUCTURE_SEGMENTATION_PWF);
		this.overallEvalMetrics.add(NemaDataConstants.STRUCTURE_SEGMENTATION_PWPRECISION);
		this.overallEvalMetrics.add(NemaDataConstants.STRUCTURE_SEGMENTATION_PWRECALL);
		this.overallEvalMetrics.add(NemaDataConstants.STRUCTURE_SEGMENTATION_R);
		this.overallEvalMetrics.add(NemaDataConstants.STRUCTURE_SEGMENTATION_FMEASUREATPOINTFIVE);
		this.overallEvalMetrics.add(NemaDataConstants.STRUCTURE_SEGMENTATION_PRECRATEATPOINTFIVE);
		this.overallEvalMetrics.add(NemaDataConstants.STRUCTURE_SEGMENTATION_RECRATEATPOINTFIVE);
		this.overallEvalMetrics.add(NemaDataConstants.STRUCTURE_SEGMENTATION_FMEASUREATTHREE);
		this.overallEvalMetrics.add(NemaDataConstants.STRUCTURE_SEGMENTATION_PRECRATEATTHREE);
		this.overallEvalMetrics.add(NemaDataConstants.STRUCTURE_SEGMENTATION_RECRATEATTHREE);
		this.overallEvalMetrics.add(NemaDataConstants.STRUCTURE_SEGMENTATION_MEDCLAIM2TRUE);
		this.overallEvalMetrics.add(NemaDataConstants.STRUCTURE_SEGMENTATION_MEDTRUE2CLAIM);

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



}
