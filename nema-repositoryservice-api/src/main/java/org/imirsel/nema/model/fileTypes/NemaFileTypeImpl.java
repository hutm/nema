package org.imirsel.nema.model.fileTypes;

import java.io.PrintStream;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import org.imirsel.nema.model.logging.AnalyticsLogFormatter;


/**
 * 
 * @author kris.west@gmail.com
 * @since 0.1.0
 */
public abstract class NemaFileTypeImpl implements NemaFileType{

	protected Logger _logger;
	private String typeName;
	private String filenameExtension = ".txt";
	private boolean refersToAudio;
	
	public NemaFileTypeImpl(String typeName) {
		this.typeName = typeName;
		this.refersToAudio = true;
	}
	
	public NemaFileTypeImpl(String typeName, boolean refersToAudio) {
		this.typeName = typeName;
		this.refersToAudio = refersToAudio;
	}
	
	public Logger getLogger() {
		if (_logger == null){
			_logger = Logger.getLogger(this.getClass().getName());
		}
		return _logger;
	}

	public void addLogDestination(PrintStream stream) {
		Handler handler = new StreamHandler(stream, new AnalyticsLogFormatter());
		getLogger().addHandler(handler);
	}

	public String getTypeName() {
		return typeName;
	}

	public void setFilenameExtension(String filenameExtension) {
		this.filenameExtension = filenameExtension;
	}

	public String getFilenameExtension() {
		return filenameExtension;
	}
	
	public boolean refersToTrackIds() {
		return refersToAudio;
	}
}