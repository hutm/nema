package org.imirsel.nema.analytics.evaluation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Logger;

import org.imirsel.nema.model.NemaEvaluationResultSet;

public interface ResultRenderer {

    /**
     * Returns the logger in use. Can be used to change the logging verbosity 
     * level with:
     * getLogger.setLevel(Level.WARNING).
     * @return the Logger that will be used for console output.
     */
    public Logger getLogger();
    
    /**
	 * Ensures that the log output is also sent to the specified PrintStream.
	 * @param stream The PrintStream to send the log output to.
	 */
	public void addLogDestination(PrintStream stream);

	/**
	 * Set the working directory for the utility, to be used for creation of an temp files
	 * required.
	 * 
	 * @param workingDir File representing the path to the working directory.
	 * @throws FileNotFoundException Thrown if the specified path can't be found or created.
	 */
    public void setWorkingDir(File workingDir) throws FileNotFoundException;
    
    /**
     * Sets the output directory for the utility. Outputs may include encoded data files,
     * reports, an HTML mini-site providing an interface to the results etc.
     * 
     * @param outputDir File representing the output directory.
     * @throws FileNotFoundException Thrown if the specified path can't be found or created.
     */
    public void setOutputDir(File outputDir) throws FileNotFoundException;
    
	/**
     * Render results from the Evaluator to a folder on disk.
     * 
     * @param results The results Object to render.
     * @throws IOException Thrown if an IOException occurs while rendering the 
     * results.
     */
    public void renderResults(NemaEvaluationResultSet results) throws IOException;
    
    /**
     * Render packaged analysis results to a folder on disk. Analysis results do 
     * not contain any evaluation metrics.
     * 
     * @param results The results Object to render.
     * @throws IOException Thrown if an IOException occurs while rendering the 
     * results.
     */
    public void renderAnalysis(NemaEvaluationResultSet results) throws IOException;
    
    
    //TODO: remove these temporary methods when we get a java implementation of stats tests
    
    /**
     * Sets a flag determining whether significance tests are performed in 
     * matlab.
     * @param performMatlabStatSigTests The flag to set.
     */
	public void setPerformMatlabStatSigTests(boolean performMatlabStatSigTests);

	/**
	 * Returns a flag determining whether significance tests are performed in 
	 * matlab.
	 * @return The flag value.
	 */
	public boolean getPerformMatlabStatSigTests();

	/**
	 * Sets the path to the matlab executable.
	 * @param matlabPath The executable path.
	 */
	public void setMatlabPath(File matlabPath);

	/**
	 * Returns  the path to the matlab executable.
	 * @return matlabPath The executable path.
	 */
	public File getMatlabPath();
}
