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
 * Reads and writes Raw data files. When reading data is read into a byte[] and
 * wrapped in a NemaData Object. Writing the file out again recreates the 
 * file exactly at the specified path.
 * 
 * Used to handle file formats that are opaque (i.e. we don't understand format 
 * and can't read it, but need to move it around - e.g. model files).
 * 
 * @author kris.west@gmail.com
 * @since 0.2.0
 */
public class OpaqueFileFormat extends NemaFileTypeImpl {

	public static final String TYPE_NAME = "Opaque format file on disk";
	String extension;
	
	public OpaqueFileFormat() {
		super(TYPE_NAME, false);
		this.setFilenameExtension("");
	}
	
	public OpaqueFileFormat(String extension) {
		super(TYPE_NAME, false);
		this.setFilenameExtension(extension);
	}
	
	public NemaData readFile(File theFile)
			throws IllegalArgumentException, FileNotFoundException, IOException {
		byte[] data = IOUtil.readBytesFromFile(theFile);
		NemaData out = new NemaData(PathAndTagCleaner.convertFileToMIREX_ID(theFile));
		out.setMetadata(NemaDataConstants.PROP_FILE_LOCATION, theFile.getAbsolutePath());
		out.setMetadata(NemaDataConstants.FILE_DATA, data);
        return out;
	}
	
	public void writeFile(File theFile, NemaData data)
			throws IllegalArgumentException, FileNotFoundException, IOException {
		IOUtil.writeBytesToFile(theFile, (byte[])data.getMetadata(NemaDataConstants.FILE_DATA));
	}
}
