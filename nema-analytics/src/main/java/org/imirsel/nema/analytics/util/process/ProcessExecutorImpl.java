package org.imirsel.nema.analytics.util.process;

import java.io.File;
import java.io.PrintStream;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;
import org.imirsel.nema.analytics.logging.ProcessExecutorLogFormatter;

/**
 * 
 * @author kriswest
 * 
 */
public abstract class ProcessExecutorImpl implements ProcessExecutorInterface{

	protected Logger _logger;
	protected File outpath;
	protected File processWorkingDir;
	protected File processResultsDir;
	protected File scratchDir;
	
	protected boolean outputIsDirectory = false;
	protected final String outputFileName = "outputFileName.txt";
	protected int inputToExtend = -1;
	protected String extension = ".result";
	protected String envVar = "VAR_NAME1=VAR_VAL1\nVAR_NAME2=VAR_VAL2";

	public ProcessExecutorImpl() {
	}
	

	/**
	 * Sets up the ProcessExecutor, with a specified output path. Note that this file
	 * must be under the process results directory path.
	 * 
	 * @param outpath
	 * @param processWorkingDir
	 * @param processResultsDir
	 * @param scratchDir
	 * @param addExtension
	 * @param extension
	 * @param envVar
	 */
	public ProcessExecutorImpl(File outpath, boolean outputIsDirectory,
			File processWorkingDir,
			File processResultsDir,
			File scratchDir,
			String envVar) {
		this.outpath = outpath;
		this.outputIsDirectory = outputIsDirectory;
		this.processWorkingDir = processWorkingDir;
		this.processResultsDir = processResultsDir;
		this.scratchDir = scratchDir;
		this.inputToExtend = -1;
		this.extension = null;
		this.envVar = envVar;
		
		if (!this.outpath.getAbsolutePath().startsWith(this.processResultsDir.getAbsolutePath())){
			throw new IllegalArgumentException("The output path must be under the process results directory path!\n" +
					"Specified output file:      " + outpath.getAbsolutePath() + "\n" + 
					"Specified result directory: " + processResultsDir.getAbsolutePath());
		}
	}
	
	/**
	 * Sets up the ProcessExecutor, with an output path determined by appending 
	 * '.result' to the specified input within the process results directory.
	 * 
	 * @param processWorkingDir
	 * @param processResultsDir
	 * @param scratchDir
	 * @param commandFormattingStr
	 * @param executablePath
	 * @param envVar
	 */
	public ProcessExecutorImpl(File processWorkingDir,
			File processResultsDir, File scratchDir, String envVar) {
		this.processWorkingDir = processWorkingDir;
		this.processResultsDir = processResultsDir;
		this.scratchDir = scratchDir;
		this.inputToExtend = -1;
		this.envVar = envVar;
		
	}
	
	
	
	/**
	 * Sets up the ProcessExecutor, with an output path determined by appending 
	 * a specified extension to the specified input within the process results 
	 * directory.
	 * 
	 * @param processWorkingDir
	 * @param processResultsDir
	 * @param scratchDir
	 * @param inputToExtend
	 * @param extension
	 * @param envVar
	 */
	public ProcessExecutorImpl(
			File processWorkingDir, File processResultsDir, File scratchDir,
			int inputToExtend, String extension, String envVar) {

		this.processWorkingDir = processWorkingDir;
		this.processResultsDir = processResultsDir;
		this.scratchDir = scratchDir;
		this.inputToExtend = inputToExtend;
		this.extension = extension;
		this.envVar = envVar;
		
		this.outpath = null;
	}

	public Logger getLogger() {
		if (_logger == null){
			_logger = Logger.getLogger(this.getClass().getName());
		}
		return _logger;
	}

	public void addLogDestination(PrintStream stream) {
		Handler handler = new StreamHandler(stream, new ProcessExecutorLogFormatter(getProcessType(), getExecutableName()));
		getLogger().addHandler(handler);
	}
	
	public abstract String getProcessType();
	
	public abstract String getExecutableName();

	public File getOutpath() {
		return outpath;
	}

	public void setOutpath(File outpath) {
		this.outpath = outpath;
	}

	public File getProcessWorkingDir() {
		return processWorkingDir;
	}

	public File getProcessResultsDir() {
		return processResultsDir;
	}

	public boolean isOutputIsDirectory() {
		return outputIsDirectory;
	}

	public String getOutputFileName() {
		return outputFileName;
	}

	public int getInputToExtend() {
		return inputToExtend;
	}

	public String getExtension() {
		return extension;
	}

	public String getEnvVar() {
		return envVar;
	}

	public File getScratchDir() {
		return scratchDir;
	}

}