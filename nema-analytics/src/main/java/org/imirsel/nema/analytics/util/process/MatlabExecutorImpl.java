package org.imirsel.nema.analytics.util.process;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.Level;

public class MatlabExecutorImpl extends ProcessExecutorImpl {
	
	protected String matlabArgs = "-nodesktop -nosplash";
	protected File matlabBin = new File("/opt/matlab2010/bin/matlab");
	protected String functionName;
	protected boolean isRunning = false;
	protected Process process;
	protected ProcessOutputReceiver procOutputReceiverThread = null;
	protected String commandFormattingStr = "('$1','$o')";
	 
	/**
	 * Sets up the ProcessExecutor, with a specified output path. Note that this file
	 * must be under the process results directory path.
	 * 
	 * @param outpath
	 * @param processWorkingDir
	 * @param processResultsDir
	 * @param commandFormattingStr
	 * @param functionName
	 * @param envVar
	 */
	public MatlabExecutorImpl(File outpath, boolean outputIsDirectory,
			File processWorkingDir,
			File processResultsDir, 
			File scratchDir,
			String commandFormattingStr,
			String functionName,
			String envVar) {
		super(outpath, outputIsDirectory, processWorkingDir, processResultsDir, scratchDir, envVar);
		this.commandFormattingStr = commandFormattingStr;
		this.functionName = functionName;
		this.isRunning = false;
		this.procOutputReceiverThread = null;
		this.process = null;
	}
	
	/**
	 * Sets up the ProcessExecutor, with an output path determined by appending 
	 * a specified extension to the specified input within the process results 
	 * directory.
	 * 
	 * @param processWorkingDir
	 * @param processResultsDir
	 * @param commandFormattingStr
	 * @param functionName
	 * @param inputToExtend
	 * @param extension
	 * @param envVar
	 */
	public MatlabExecutorImpl(
			File processWorkingDir, File processResultsDir,
			File scratchDir,
			String commandFormattingStr, String functionName,
			int inputToExtend,
			String extension, String envVar) {
		super(processWorkingDir, processResultsDir, scratchDir, inputToExtend, extension, envVar);
		this.functionName = functionName;
		this.commandFormattingStr = commandFormattingStr;
		this.outpath = null;
		this.isRunning = false;
		this.procOutputReceiverThread = null;
		this.process = null;
	}

	/* (non-Javadoc)
	 * @see org.imirsel.nema.analytics.util.process.ProcessExecutorInterface#killProcess()
	 */
	public void killProcess(){
		if(process != null) {
            process.destroy();
        }
	}
	
	/* (non-Javadoc)
	 * @see org.imirsel.nema.analytics.util.process.ProcessExecutorInterface#runCommand(java.lang.Object[])
	 */
	public int runCommand(final Object[] input)
			throws IllegalArgumentException, IOException {
		isRunning = true;
		// if necessary, create the output filename
		if (inputToExtend != -1) {
			if (inputToExtend >= input.length){
				isRunning = false;
				throw new IllegalArgumentException("Received input array length " + input.length + ", was expecting a minimum of " + (inputToExtend+1) + ", based on the inputToExtend argument");
			}
			if (inputToExtend == input.length){
				isRunning = false;
				throw new IllegalArgumentException("Received null input array, was expecting an array of length " + (inputToExtend+1) + " or more, based on the inputToExtend argument");
			}
			if (input[inputToExtend] instanceof File){
				outpath = new File(processResultsDir.getCanonicalPath() + File.separator
						+ ((File)input[inputToExtend]).getName() + extension);
			}else{
				outpath = new File(processResultsDir.getCanonicalPath() + File.separator
						+ input[inputToExtend] + extension);
			}
			
		}

		// Set any environment variables required
		String[] envp = null;
		if (envVar != null && !envVar.equals("")){
			envp = envVar.split("\n");
		}
		
//		if (!matlabBin.exists()) {
//			isRunning = false;
//			throw new IllegalArgumentException(
//					"Unable to locate the matlab executable!\n"
//					+ "Matlab path: "
//					+ matlabBin.getCanonicalPath()			
//				);
//		}

		// Create command
        String[] argArray = matlabArgs.split(" ");
        int commandLength = argArray.length+3;
        int commandArgsIdx = commandLength-1;
        String[] components = commandFormattingStr.split("[$]");
		
		String[] cmdArray = new String[commandLength];
        cmdArray[0] = this.matlabBin.getPath();//.getAbsolutePath();
        for (int i = 0; i < argArray.length; i++) {
        	cmdArray[i + 1] = argArray[i];
        }
        cmdArray[argArray.length + 1] = "-r";
        
        cmdArray[commandArgsIdx] = functionName;
        
        for (int i = 0; i < components.length; i++) {
			if (components[i].length() >= 1) {
				char testSymbol = components[i].charAt(0);
				if(Character.isDigit(testSymbol)){
					// cmdArray[cmdCount] = "\"" + inputFilename1 + "\"";
					int idx = Integer.parseInt(""+ testSymbol);
					if (input[idx] instanceof File){
						cmdArray[commandArgsIdx] += ((File)input[idx]).getCanonicalPath() + components[i].substring(1);
					}else{
						cmdArray[commandArgsIdx] += input[idx].toString() + components[i].substring(1);
					}
				}
				else if(testSymbol == 'o'){
					cmdArray[commandArgsIdx] = outpath.getCanonicalPath();
					if (!components[i].substring(1).trim().equals("")) {
						String[] comps = components[i].substring(1).trim()
								.split(" ");
						for (int j = 0; j < comps.length; j++) {
							cmdArray[commandArgsIdx] += comps[j].trim();
						}
					}
				}
				else if(testSymbol == 's'){
					cmdArray[commandArgsIdx] = scratchDir.getCanonicalPath();
					if (!components[i].substring(1).trim().equals("")) {
						String[] comps = components[i].substring(1).trim()
								.split(" ");
						for (int j = 0; j < comps.length; j++) {
							cmdArray[commandArgsIdx] += comps[j].trim();
						}
					}
				} else {
					if (components[i].trim().equals("")) {

					} else {
						String[] comps = components[i].trim().split(" ");
						for (int j = 0; j < comps.length; j++) {
							cmdArray[commandArgsIdx] += comps[j].trim();
						}
					}
				}
			}
		}
		
		String msg = "Running command:    ";
		for (int i=0;i<cmdArray.length;i++) {
			msg += cmdArray[i] + " ";
		}
		msg += "\n";
		msg += "In directory:       " + processWorkingDir.getCanonicalPath() + "\n";
		msg += "Sending results to: " + processResultsDir.getCanonicalPath() + "\n";
		getLogger().info(msg);
		
		ProcessBuilder pb = new ProcessBuilder(cmdArray);
		Map<String, String> env = pb.environment();
		if (envp != null){
			for (int i = 0; i < envp.length; i++) {
				if (!envp[i].equals("")) {
					String[] envPair = envp[i].split("=");
					if (envPair.length == 2) {
						env.put(envPair[0], envPair[1]);
						getLogger().info("Environment variable " + envPair[0] + "="
								+ envPair[1] + " succesfully set.");
					} else {
						getLogger().info("The environment variable " + envVar
								+ " can not be parsed !!!");
					}
				}
			}
		}
		
		pb.directory(processWorkingDir);
		pb.redirectErrorStream(true);
		InputStream is = null;
		try{
			process = pb.start();
			is = process.getInputStream();
			getLogger().info("*******************************************\n" +
			"MATLAB STDOUT AND STDERR:");
			
			procOutputReceiverThread = new ProcessOutputReceiver( is, _logger );
			procOutputReceiverThread.start();
			int exitStatus;
			try {
				exitStatus = process.waitFor();
				getLogger().info("MATLAB EXIT STATUS: " + exitStatus + "\n" +
				"*******************************************");
				return exitStatus;
			} catch (InterruptedException e) {
				getLogger().log(Level.WARNING, "Interupted while waiting for process to exit", e);
			}
		}finally{
			if(procOutputReceiverThread != null){
				procOutputReceiverThread.kill();
			}
			if(process != null){
				process.getErrorStream().close();
			}
			if(is != null){
				is.close();
			}
			isRunning = false;
		}
		return -1;
	}


	/* (non-Javadoc)
	 * @see org.imirsel.nema.analytics.util.process.ProcessExecutorInterface#isAborted()
	 */
	public boolean isRunning() {
		return isRunning;
	}

	/* (non-Javadoc)
	 * @see org.imirsel.nema.analytics.util.process.ProcessExecutorInterface#getCommandFormattingStr()
	 */
	public String getCommandFormattingStr() {
		return commandFormattingStr;
	}

	/* (non-Javadoc)
	 * @see org.imirsel.nema.analytics.util.process.ProcessExecutorInterface#getExecutablePath()
	 */
	public String getFunctionName() {
		return functionName;
	}


    /**
     * Returns the arguments to pass to Matlab
     * @return the arguments to pass to Matlab
     */
    public String getMatlabArgs() {
        return matlabArgs;
    }

    /**
     * Sets the arguments to pass to Matlab
     * @param val the arguments to pass to Matlab
     */
    public void setMatlabArgs(String val) {
        matlabArgs = val;
    }

    /**
     * Get the path to the Matlab binary (fully qualified)
     * @return the fully qualified path to the matlab binary
     **/
    public File getMatlabBin() {
        return matlabBin;
    }

    /**
     * Set the path to the Matlab binary (fully qualified)
     * @param bin the fully qualified path to the matlab binary
     **/
    public void setMatlabBin(File bin) {
        this.matlabBin = bin;
    }

	@Override
	public String getExecutableName() {
		return getFunctionName();
	}

	@Override
	public String getProcessType() {
		return "MATLAB";
	}
}
