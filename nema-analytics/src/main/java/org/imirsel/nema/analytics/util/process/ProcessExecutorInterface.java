package org.imirsel.nema.analytics.util.process;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Handler;
import java.util.logging.Logger;

/**
 * 
 * @author kriswest
 */
public interface ProcessExecutorInterface {

	/**
	 * Returns the configured Logger for this instance.
	 * @return logger 
	 */
	public Logger getLogger();

	/**
	 * Ensures that the log output is also sent to the specified PrintStream.
	 * @param stream The PrintStream to send the log output to.
	 */
	public void addLogDestination(PrintStream stream);

	/**
	 * Kills the process. The runCommand method should them exit gracefully, immeadiately.
	 */
	public void killProcess();

	/**
	 * Sets up and executes the process. Blocks until the process completes.
	 * Output from the process is sent to the Logger.
	 *  
	 * @param input
	 * @return exit value returned by process
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	public int runCommand(final Object[] input)
			throws IllegalArgumentException, IOException;

	/**
	 * Return a String indicating the type of code being executed, e.g. BINARY,
	 * JAVA, MATLAB, VAMP.
	 * @return String indicating the type of code being executed
	 */
	public String getProcessType();
	
	/**
	 * Return a String giving the name of the executable being run for logging purposes.
	 * @return  a String giving the name of the executable being run.
	 */
	public String getExecutableName();
	
	/**
	 * @return the outpath
	 */
	public File getOutpath();

	/**
	 * @param outpath the outpath to set
	 */
	public void setOutpath(File outpath);

	/**
	 * @return the processWorkingDir
	 */
	public File getProcessWorkingDir();

	/**
	 * @return the processResultsDir
	 */
	public File getProcessResultsDir();
	
	/**
	 * @return the scratchDir
	 */
	public File getScratchDir();
	
	/**
	 * @return the outputIsDirectory
	 */
	public boolean isOutputIsDirectory();

	/**
	 * @return the isRunning
	 */
	public boolean isRunning();

	/**
	 * @return the commandFormattingStr
	 */
	public String getCommandFormattingStr();

	/**
	 * @return the outputFileName
	 */
	public String getOutputFileName();

	/**
	 * @return the inputToExtend
	 */
	public int getInputToExtend();

	/**
	 * @return the extension
	 */
	public String getExtension();

	/**
	 * @return the envVar
	 */
	public String getEnvVar();

}