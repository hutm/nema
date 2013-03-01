package org.imirsel.nema.model.util;

import java.io.File;

/**
 * 
 * @author kris.west@gmail.com
 *
 */
public class PathAndTagCleaner {

	/**
	 * 
	 */
	private static String WINDOWS_PATH_REGEX = "[A-Z]:\\\\";
    
	/**
	 * 
	 * @param aFile
	 * @return the file name cleaned up
	 */
	public static String convertFileToMIREX_ID(File aFile){
        String name = aFile.getName();
        String cleanName;
        //detect windows paths
        if (name.substring(0,3).matches(WINDOWS_PATH_REGEX)){
            cleanName = name.substring(name.lastIndexOf("\\")+1,name.length()).toLowerCase();
        }else{
            cleanName = name.toLowerCase();
        }
        int idx = cleanName.indexOf('.');
        if (idx != -1){
        	cleanName = cleanName.substring(0,idx);
        }
        return cleanName;
    }
    
    /**
     * 
     * @param tag
     * @return the cleanedup tag
     */
    public static String cleanTag(String tag){
        return tag.toLowerCase().replaceAll("\\s+", "_").replaceAll("[^a-z0-9]", "");
    }

}
