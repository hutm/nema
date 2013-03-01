package org.imirsel.nema.analytics.util.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;

/**
 * 
 * @author kriswest
 * @since 0.4.0
 *
 */
public class ProcessOutputReceiver extends Thread
{
	// ------------------------------ FIELDS ------------------------------

	/**
	 * stream to receive data from child
	 */
	private final InputStream is;
	Logger logger;
	private boolean run = true;

	private static final int BATCHSIZE = 5;
	
	//--------------------------- CONSTRUCTORS ---------------------------

	/**
	 * constructor
	 *
	 * @param is inputstream to receive data from child
	 * @param logger The logger to use to report console output from the process being monitored.
	 */
	public ProcessOutputReceiver( InputStream is, Logger logger )
	{
		this.is = is;
		//this should be the only class where we pass loggers around. Elsewhere the logger
		//from the specific class should be used instead.
		this.logger = logger;
	}
	
	public void kill(){
		run = false;
	}
 
	// -------------------------- PUBLIC INSTANCE  METHODS --------------------------
	/**
	 * method invoked when Receiver thread started.  Reads data from child and displays in on System.out.
	 */
	public void run()
	{
		run = true;
		try
		{
			final BufferedReader br = new BufferedReader( new InputStreamReader( is ));
			String line = null;
			String lines = null;
			long count = 0;
			while (run )
			{
				line = br.readLine();
				if(line == null){
					if (lines == null){
						try {
							sleep(5);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}else{
						logger.info(lines);
						lines = null;
						count = 0;
					}
				}else{
					if (lines == null){
						lines = line;
					}else{
						lines += "\n" + line;
					}
					count++;
					if (count == BATCHSIZE){
						logger.info(lines);
						lines = null;
						count = 0;
					}
				}
			}
			br.close();
		}
		catch ( IOException e )
		{
			throw new IllegalArgumentException( "IOException receiving data from child process." );
		}

		
		
		
		/*
		
		int bufferSize = 1048576; // 1 mb buffer
		byte[] OutputBuffer = new byte[bufferSize];
		int OutputBufferIndex = 0;
		int numBytes = 0;
		
		BufferedInputStream bis = new BufferedInputStream(is, bufferSize);

		while (true) {
			int numBytesAvailable = 0;
			try {
				numBytesAvailable = bis.available();
				cout.println("bis.available: " + numBytesAvailable);
				cout.flush();
			} catch (IOException e) {
				System.out.println("inputStream.available() error!!!");
				e.printStackTrace();
			}

			if (numBytesAvailable == 0) {
				break;
			}

			try {
				numBytes = bis.read(OutputBuffer, OutputBufferIndex, numBytesAvailable);
				if (numBytes != numBytesAvailable) {
					System.out.println("numBytes != numBytesAvailable");
					throw new IOException();
				}
			} catch (IOException e) {
				System.out.println("inputStream.read() error!!!");
				e.printStackTrace();
			}

			cout.print(new String(OutputBuffer, OutputBufferIndex, numBytesAvailable));
			cout.flush();
			OutputBufferIndex += numBytesAvailable;
		}
		try {
			bis.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("error closing buffered input stream!!!");
			e.printStackTrace();
		}
		
		*/

	}
}
