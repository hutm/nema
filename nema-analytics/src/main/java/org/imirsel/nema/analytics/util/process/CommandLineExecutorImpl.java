package org.imirsel.nema.analytics.util.process;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.Level;

public class CommandLineExecutorImpl extends ProcessExecutorImpl {

	protected boolean isRunning = false;
	protected Process process;
	protected ProcessOutputReceiver procOutputReceiverThread = null;
	protected String commandFormattingStr = "$m -anOption $1 $2 $o";
	protected File executablePath;

	/**
	 * Sets up the ProcessExecutor, with a specified output path. Note that this
	 * file must be under the process results directory path.
	 * 
	 * @param outpath
	 * @param processWorkingDir
	 * @param processResultsDir
	 * @param commandFormattingStr
	 * @param executablePath
	 * @param addExtension
	 * @param extension
	 * @param envVar
	 */
	public CommandLineExecutorImpl(File outpath, boolean outputIsDirectory,
			File processWorkingDir, File processResultsDir, File scratchDir,
			String commandFormattingStr, File executablePath, String envVar) {
		super(outpath, outputIsDirectory, processWorkingDir, processResultsDir, scratchDir,
				envVar);
		this.commandFormattingStr = commandFormattingStr;
		this.executablePath = executablePath;
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
	 * @param executablePath
	 * @param inputToExtend
	 * @param extension
	 * @param envVar
	 */
	public CommandLineExecutorImpl(File processWorkingDir,
			File processResultsDir, File scratchDir, String commandFormattingStr,
			File executablePath, int inputToExtend, String extension,
			String envVar) {
		super(processWorkingDir, processResultsDir, scratchDir, inputToExtend, extension, envVar);
		this.commandFormattingStr = commandFormattingStr;
		this.executablePath = executablePath;
		this.outpath = null;
		this.isRunning = false;
		this.procOutputReceiverThread = null;
		this.process = null;
	}

	public void killProcess() {
		if (process != null) {
			process.destroy();
		}
		if (procOutputReceiverThread != null){
			procOutputReceiverThread.kill();
		}
	}

	public int runCommand(final Object[] input)
			throws IllegalArgumentException, IOException {
		isRunning = true;
		
		// if necessary, create the output filename
		if (inputToExtend != -1) {
			if (inputToExtend >= input.length) {
				isRunning = false;
				throw new IllegalArgumentException(
						"Received input array length " + input.length
								+ ", was expecting a minimum of "
								+ (inputToExtend + 1)
								+ " based on the inputToExtend argument");
			}
			if (input[inputToExtend] instanceof File) {
				outpath = new File(processResultsDir.getCanonicalPath()
						+ File.separator
						+ ((File) input[inputToExtend]).getName() + extension);
			} else {
				outpath = new File(processResultsDir.getCanonicalPath()
						+ File.separator + input[inputToExtend] + extension);
			}

		}

		// Set any environment variables required
		String[] envp = null;
		if (envVar != null && !envVar.equals("")) {
			envp = envVar.split("\n");
		}

		File toExecute = null;
		if (!executablePath.exists()) {
			File executablePath2 = new File(processWorkingDir
					.getCanonicalPath()
					+ File.separator + executablePath);
			if (!executablePath2.exists()) {
				isRunning = false;
				throw new IllegalArgumentException(
						"Unable to locate your executable!\n"
								+ "File names tried:\n\t"
								+ executablePath.getCanonicalPath() + "\n\t"
								+ executablePath2.getCanonicalPath() + "\n");
			} else {
				toExecute = executablePath2;
			}
		} else {
			toExecute = executablePath;
		}

		// Create command
		String[] components = commandFormattingStr.split("[$]");

		int commandLength = 0;// components.length;
		String[] cmdArray;
		for (int i = 0; i < components.length; i++) {
			if (components[i].length() >= 1) {
				char testSymbol = components[i].charAt(0);

				if (testSymbol == 'm') {
					commandLength++;
					if (!components[i].substring(1).trim().equals("")) {
						String[] comps = components[i].substring(1).trim()
								.split(" ");
						commandLength += comps.length;
					}
					// System.out.println("m component: " +
					// components[i].substring(1));

				} else if (Character.isDigit(testSymbol)) {
					int idx = Integer.parseInt("" + testSymbol);
					if (idx > input.length) {
						isRunning = false;
						throw new IllegalArgumentException(
								"Received input array length "
										+ input.length
										+ ", was expecting a minimum of "
										+ idx
										+ " based on the command formatting string");
					}

					commandLength++;
					if (!components[i].substring(1).trim().equals("")) {
						String[] comps = components[i].substring(1).trim()
								.split(" ");
						commandLength += comps.length;
					}
					// System.out.println("i component: " +
					// components[i].substring(1));
				} else if (testSymbol == 'o') {
					commandLength++;
					if (!components[i].substring(1).trim().equals("")) {
						String[] comps = components[i].substring(1).trim()
								.split(" ");
						commandLength += comps.length;
					}
					// System.out.println("o component: " +
					// components[i].substring(1));
				} else if (testSymbol == 's') {
					commandLength++;
					if (!components[i].substring(1).trim().equals("")) {
						String[] comps = components[i].substring(1).trim()
								.split(" ");
						commandLength += comps.length;
					}
				} else {
					if (components[i].trim().equals("")) {
						// commandLength--;
					} else {
						String[] comps = components[i].trim().split(" ");
						commandLength += comps.length;
					}
				}

			} else {
				// ExternalCommand += components[i];
				// System.out.println("short component: " + components[i]);
			}
		}
		cmdArray = new String[commandLength];

		int cmdCount = 0;
		for (int i = 0; i < components.length; i++) {
			if (components[i].length() >= 1) {
				char testSymbol = components[i].charAt(0);
				if (testSymbol == 'm') {

					// cmdArray[cmdCount] = "\"" + command.getCanonicalPath() +
					// "\"";
					cmdArray[cmdCount] = toExecute.getCanonicalPath();
					cmdCount++;
					if (!components[i].substring(1).trim().equals("")) {
						String[] comps = components[i].substring(1).trim()
								.split(" ");
						for (int j = 0; j < comps.length; j++) {
							cmdArray[cmdCount] = comps[j].trim();
							cmdCount++;
						}
					}
					// System.out.println("m component: " +
					// components[i].substring(1));
				} else if (Character.isDigit(testSymbol)) {
					// cmdArray[cmdCount] = "\"" + inputFilename1 + "\"";
					int idx = Integer.parseInt("" + testSymbol);
					if (input[idx] instanceof File) {
						cmdArray[cmdCount] = ((File) input[idx])
								.getCanonicalPath();
					} else {
						cmdArray[cmdCount] = input[idx].toString();
					}

					cmdCount++;
					if (!components[i].substring(1).trim().equals("")) {
						String[] comps = components[i].substring(1).trim()
								.split(" ");
						for (int j = 0; j < comps.length; j++) {
							cmdArray[cmdCount] = comps[j].trim();
							cmdCount++;
						}
					}
					// System.out.println("input component: " +
					// components[i].substring(1));
				} else if (testSymbol == 'o') {
					// cmdArray[cmdCount] = "\"" + outfile + "\"";
					cmdArray[cmdCount] = outpath.getCanonicalPath();
					cmdCount++;
					if (!components[i].substring(1).trim().equals("")) {
						String[] comps = components[i].substring(1).trim()
								.split(" ");
						for (int j = 0; j < comps.length; j++) {
							cmdArray[cmdCount] = comps[j].trim();
							cmdCount++;
						}
					}
					// System.out.println("o component: " +
					// components[i].substring(1));
				} else if (testSymbol == 's') {
					// cmdArray[cmdCount] = "\"" + outfile + "\"";
					cmdArray[cmdCount] = scratchDir.getCanonicalPath();
					cmdCount++;
					if (!components[i].substring(1).trim().equals("")) {
						String[] comps = components[i].substring(1).trim()
								.split(" ");
						for (int j = 0; j < comps.length; j++) {
							cmdArray[cmdCount] = comps[j].trim();
							cmdCount++;
						}
					}
				} else {
					if (components[i].trim().equals("")) {

					} else {
						String[] comps = components[i].trim().split(" ");
						for (int j = 0; j < comps.length; j++) {
							cmdArray[cmdCount] = comps[j].trim();
							cmdCount++;
						}

					}
				}
			} else {
				// ExternalCommand += components[i];
				// System.out.println("short component: " + components[i]);
			}
		}

		String msg = "Running command:    ";
		for (int i = 0; i < cmdArray.length; i++) {
			msg += cmdArray[i] + " ";
		}
		msg += "\n";
		msg += "In directory:       " + processWorkingDir.getCanonicalPath()
				+ "\n";
		msg += "Sending results to: " + processResultsDir.getCanonicalPath()
				+ "\n";
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
		try {
			process = pb.start();
			is = process.getInputStream();
			getLogger().info("*******************************************\n"
					+ "EXTERNAL PROCESS STDOUT AND STDERR:");

			procOutputReceiverThread = new ProcessOutputReceiver(is, getLogger());
			procOutputReceiverThread.start();
			int exitStatus;
			try {
				exitStatus = process.waitFor();
				getLogger().info("EXTERNAL PROCESS EXIT STATUS: " + exitStatus
						+ "\n" + "*******************************************");
				return exitStatus;
			} catch (InterruptedException e) {
				getLogger().log(Level.WARNING,
						"Interupted while waiting for process to exit", e);
			}
		} finally {
			if (procOutputReceiverThread != null) {
				procOutputReceiverThread.kill();
			}
			if (process != null) {
				process.getErrorStream().close();
			}
			if (is != null) {
				is.close();
			}
			isRunning = false;
		}
		return -1;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public String getCommandFormattingStr() {
		return commandFormattingStr;
	}

	public File getExecutablePath() {
		return executablePath;
	}
	
	@Override
	public String getExecutableName() {
		return getExecutablePath().getName();
	}

	@Override
	public String getProcessType() {
		return "BINARY";
	}

}
