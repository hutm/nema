/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.imirsel.nema.model.fileTypes;


/**
 * File type to represent a directory of data files on disk in opaque 
 * (unspecified) formats.
 * 
 * Used to handle file formats that are opaque (i.e. we don't understand format 
 * and can't read it, but need to return its path).
 * 
 * Has no reading or writing functions.
 * 
 * @author kris.west@gmail.com
 * @since 0.3.0
 */
public class OpaqueDirectoryFormat extends NemaFileTypeImpl {

	public static final String TYPE_NAME = "Directory of opaque format files on disk";
	String extension;
	
	public OpaqueDirectoryFormat() {
		super(TYPE_NAME, false);
		this.setFilenameExtension("");
	}
	
}
