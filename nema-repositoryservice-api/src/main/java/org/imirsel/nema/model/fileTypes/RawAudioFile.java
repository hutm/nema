/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.imirsel.nema.model.fileTypes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.imirsel.nema.model.*;
import org.imirsel.nema.model.util.IOUtil;
import org.imirsel.nema.model.util.PathAndTagCleaner;

/**
 * Reads and writes Raw audio files. When reading data is read into a byte[] and
 * wrapped in a NemaData Object. Writing the file out again recreates the audio 
 * file at the specified path.
 * 
 * @author kris.west@gmail.com
 * @since 0.2.0
 */
public class RawAudioFile extends SingleTrackEvalFileTypeImpl {

	public static final String TYPE_NAME = "Audio file on disk";
	
	public RawAudioFile(File path) {
		super(TYPE_NAME);
		setExamplePath(path);
	}
	
	public RawAudioFile() {
		super(TYPE_NAME);
		this.setFilenameExtension("");
	}
	
	public void setExamplePath(File path){
		String name = path.getName();
		int extIdx = name.lastIndexOf('.');
		if (extIdx == -1) {
			this.setFilenameExtension("");
		}else{
			String ext = name.substring(extIdx);
			this.setFilenameExtension(ext);
		}
	}
	
	@Override
	public NemaData readFile(File theFile)
			throws IllegalArgumentException, FileNotFoundException, IOException {
		byte[] data = IOUtil.readBytesFromFile(theFile);
		NemaData out = new NemaData(PathAndTagCleaner.convertFileToMIREX_ID(theFile));
		out.setMetadata(NemaDataConstants.PROP_FILE_LOCATION, theFile.getAbsolutePath());
		out.setMetadata(NemaDataConstants.FILE_DATA, data);
        return out;
	}
	
	@Override
	public void writeFile(File theFile, NemaData data)
			throws IllegalArgumentException, FileNotFoundException, IOException {
		IOUtil.writeBytesToFile(theFile, (byte[])data.getMetadata(NemaDataConstants.FILE_DATA));
	}
}
