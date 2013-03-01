package org.imirsel.nema.analytics.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * 
 * @author kriswest
 * @since 0.5.0
 */
public class ProcessExecutorLogFormatter extends Formatter {

	Date dat = new Date();
	private final static String format = "{0,date} {0,time}";
	private MessageFormat formatter;

	private Object args[] = new Object[1];
	
	private String processType = "UNKNOWN";
	private String executableName = "UNKNOWN";
	
	public ProcessExecutorLogFormatter(String processType, String executableName){
		this.processType = processType;
		this.executableName = executableName;
	}
	

	// Line separator string. This is the value of the line.separator
	// property at the moment that the SimpleFormatter was created.
	private String lineSeparator = "\n";

	/**
	 * Format the given LogRecord.
	 * 
	 * @param record
	 *            the log record to be formatted.
	 * @return a formatted log record
	 */
	public synchronized String format(LogRecord record) {
		StringBuffer sb = new StringBuffer();
		// Minimize memory allocations here.
		dat.setTime(record.getMillis());
		args[0] = dat;
		StringBuffer text = new StringBuffer();
		if (formatter == null) {
			formatter = new MessageFormat(format);
		}
		formatter.format(args, text, null);

		//use details of executable and process type (e.g. Java) rather than the class doing the logging
		sb.append(text);
		sb.append(" ");
		sb.append(processType);
		sb.append(" ");
		sb.append(executableName);
		

		sb.append(lineSeparator);
		String message = formatMessage(record);
		sb.append(record.getLevel().getLocalizedName());
		sb.append(": ");
		sb.append(message + "\n");
		if (record.getThrown() != null) {
			try {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				record.getThrown().printStackTrace(pw);
				pw.close();
				sb.append(sw.toString());
			} catch (Exception ex) {
			}
		}
		sb.append(lineSeparator);
		return sb.toString();
	}
}
