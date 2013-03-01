/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.imirsel.nema.model;

import java.io.File;
import java.io.Serializable;

/**
 * A class representing a file referred to by the NEMA repository that is linked to
 * a trackID. The <code>NemaFile</code> has a unique ID assigned by the repository DB
 * and a filesystem path.
 * @author kris.west@gmail.com
 * @since 0.1.0
 */
public class NemaFile implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String trackId;
    private String path;
    private String site;

    /**
     * No arg constructor. All fields must be set manually.
     */
    public NemaFile(){
    }

    /**
     * Constructor. Sets the ID field but not the trackID, path or site.
     * @param id the ID to set.
     */
    public NemaFile(int id){
        this.id = id;
    }

    /**
     * Constructor. Sets the ID, trackID and filesystem path.
     * @param id the ID to set.
     * @param trackId the trackID to set.
     * @param path the filesystem path to set.
     */
    public NemaFile(int id, String trackId, String path, String site){
        this.id = id;
        this.trackId = trackId;
        this.path = path;
        this.site = site;
    }

    /**
     * Returns the ID.
     * @return the ID.
     */
    public int getId(){
        return id;
    }

    /**
     * Sets the ID.
     * @param id the ID to set.
     */
    public void setId(Integer id){
        this.id = id;
    }

    /**
     * Returns the trackID that the <code>NemaFile</code> corresponds to.
     * @return the trackID.
     */
    public String getTrackId(){
        return trackId;
    }

    /**
     * Sets the trackID that the <code>NemaFile</code> corresponds to.
     * @param trackId
     */
    public void setTrackId(String trackId){
        this.trackId = trackId;
    }

    /**
     * Returns the filesystem path of the <code>NemaFile</code> Object.
     * @return the filesystem path.
     */
    public String getPath(){
        return path;
    }

    /**
     * Returns a <code>File</code> Object representing the filesystem path of the <code>NemaFile</code> Object.
     * @return a <code>File</code> Object representing the filesystem path.
     */
    public File getFile(){
        return new File(path);
    }

    /**
     * Sets the filesystem path of the <code>NemaFile</code> Object.
     * @param path the path to set.
     */
    public void setPath(String path){
        this.path = path;
    }
    
    /**
     * Sets the site label (denoting at which site the path is valid).
     * @param site
     */
	public void setSite(String site) {
		this.site = site;
	}

	/**
	 * Returns the site label (denoting at which site the path is valid).
	 * @return the file name
	 */
	public String getSite() {
		return site;
	}

    @Override
    /**
     * HashCodes are purely based on the id of the <code>NemaFile</code> Object.
     * @return the HashCode.
     */
    public int hashCode(){
        return id;
    }

    @Override
    /**
     * Returns true if the other Object is a <code>NemaFile</code> instance with the same ID set.
     * @param object the Object to compare to.
     * @return a boolean indicating equality.
     */
    public boolean equals(Object object){
        if (!(object instanceof NemaFile)){
            return false;
        }
        NemaFile other = (NemaFile)object;
        if (id == other.id){
            return true;
        }
        return false;
    }

	@Override
	public String toString() {
		return "org.imirsel.nema.model.NemaFile [id=" + id + ", path=" + path + 
				", trackId=" + trackId + ",site=" + site
				+ "]";
	}


}

