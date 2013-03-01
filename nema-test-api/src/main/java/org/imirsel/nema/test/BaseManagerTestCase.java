package org.imirsel.nema.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;


/**
 *  This class provided a base class for the other tests to extend.
 *  It loads a property file that can be used by the test class to retrieve
 *  key/value pairs.
 *  
 * @author kumaramit01
 * @since 0.1.0
 */
public abstract class BaseManagerTestCase {
	final Logger logger = Logger.getLogger(getClass().getName());
	private boolean renderPackageNames = false;
	private boolean renderThreadId = false;
	private Properties properties;
	private boolean hasValidProperty = true;

	public BaseManagerTestCase() {
		logger.setUseParentHandlers(false);
		properties = new Properties();
		String propFile = this.getClass().getSimpleName()+".properties";
		// this is okay because we want the extending class to find the
		// property file not this base class
		InputStream in = getClass().getResourceAsStream(propFile);
		if(in!=null){
			try {
				properties.load(in);
			} catch (IOException e) {
				// silent -about this the extending class does not have the property file
				hasValidProperty= false;
			}finally{
			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			}
		
	}else{
		hasValidProperty = false;
	}
		this.logger.setLevel(Level.ALL);
		ConsoleHandler consoleHandler = new ConsoleHandler();
		consoleHandler.setFormatter(new TestLogFormatter(renderThreadId,renderPackageNames));
		Handler[] handlers=this.logger.getHandlers();
		for(Handler handler:handlers){
			handler.setFormatter(new TestLogFormatter(renderThreadId,renderPackageNames));
		}
		logger.addHandler(consoleHandler);
	}
	
	
	/**Copies file
	 * 
	 * @param configFile
	 * @param dirPath
	 * @return file path
	 * @throws IOException
	 */
	public String copyFile(String configFile, String dirPath)
			throws IOException {
		File src = new File(configFile);
		if (!src.exists()) {
			throw new IOException("Source does not exist "
					+ src.getAbsolutePath());
		}
		File dst = new File(dirPath, src.getName());
		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(dst); // Transfer bytes from in
		// to out
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
		return dst.getAbsolutePath();
	}

	/**Deletes a directory
	 * 
	 * @param path
	 * @return boolean true or false
	 */
	public boolean deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}
	
	/**Sets the logger level to one of the following
	* 
	* <li>SEVERE (highest value)</li>
    * <li>WARNING</li>
    * <li>INFO</li>
    * <li>CONFIG</li>
    * <li>FINE</li>
    * <li>FINER</li>
    * <li>FINEST (lowest value)</li> 
	* 
	* @param level
	*/
	public final void setLoggerLevel(Level level){
		logger.setLevel(level);
	}

	/**Return the property value as string
	 * 
	 * @param key
	 * @return property value
	 */
	public final String getPropertyAsString(String key){
		if(!hasValidProperty){
			return null;
		}
		return this.properties.getProperty(key);
	}
	
	/**Return property value as integer
	 * 
	 * @param key
	 * @return property value
	 */
	public final Integer getPropertyAsInteger(String key){
		if(!hasValidProperty){
			return null;
		}
		String stringVal=(String) this.properties.get(key);
		Integer integerVal=Integer.parseInt(stringVal);
		return integerVal;
	}
	
	/** Returns property value
	 * 
	 * @param key
	 * @return property value as long
	 */
	public final Long getPropertyAsLong(String key){
		if(!hasValidProperty){
			return null;
		}
		String stringVal=(String) this.properties.get(key);
		Long longVal=Long.parseLong(stringVal);
		return longVal;
	}

	/**Returns the logger
	 * 
	 * @return the logger
	 */
	public final Logger getLogger(){
		return this.logger;
	}
	
	public class TestLogFormatter extends Formatter {
		
		/** The maximum length of a thread name */
		private final static int MAX_THREAD_NAME_LENGTH = 40;
		
		/** The new line separator */
		private final String NEW_LINE = System.getProperty("line.separator");
		
		/** The date formatter */
		private final SimpleDateFormat FORMATER = new SimpleDateFormat("HH:mm:ss.SSS");
		
		/** Should the thread be reported */
		private boolean renderThreadId = true;

		private boolean renderPackageNames;

		/** Creates the default formatter allowing to choose is the reporting class should be reported.
		 * 
		 * @param bShowClass Should the class be shown?
		 */
		public TestLogFormatter ( boolean renderThreadId , boolean renderPackageNames) {
			this.renderThreadId = renderThreadId;
			this.renderPackageNames = renderPackageNames;
		}
		
		/** Formats the record.
		 * 
		 * @param record The log record to format
		 * @return The formated record
		 */
		  @Override
		public String format(LogRecord record) {
			  String className = record.getSourceClassName();
			  
			  if(!renderPackageNames){
				  int startClass = className.lastIndexOf('.');
				  if(startClass!=-1)
				  className = className.substring(startClass+1);
			  }
			  
			  String threadName="";
			  if(renderThreadId){
				  threadName = Thread.currentThread().getName();
				  if(threadName!=null && threadName.length()>MAX_THREAD_NAME_LENGTH ){
					  threadName = threadName.substring(threadName.length()-MAX_THREAD_NAME_LENGTH);
			  	}
			  }
			  
			  String sTimeStamp = FORMATER.format(new Date(record.getMillis()));
			  
			  return sTimeStamp+"::"+
			   		record.getLevel()+":  "+
			   		record.getMessage()+ "  " + 
			   		" ["+className+"."+record.getSourceMethodName() + "]"+
			   		((renderThreadId)?" <"+threadName+":"+record.getThreadID()+">":"")+
			   		NEW_LINE;
		  }
	}


}
