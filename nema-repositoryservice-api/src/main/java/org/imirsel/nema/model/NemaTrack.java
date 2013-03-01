/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.imirsel.nema.model;

import java.io.Serializable;

/**
 * A class representing a track in the NEMA repository. A track is essentially just a unique ID for
 * which metadata and files maybe retrieved.
 * 
 * @author kriswest
 */
public class NemaTrack implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;

    /**
     * Constructor. The trackID must be specified.
     * @param id The trackID to set.
     */
    public NemaTrack(String id){
        this.id = id;
    }

    /**
     * Returns the trackID.
     * @return the trackID.
     */
    public String getId(){
        return id;
    }

    /** 
     * Sets the trackID.
     * @param id the trackID.
     */
    public void setId(String id){
        this.id = id;
    }

    @Override
    /**
     * HashCodes are based on the String HashCode of the trackID.
     * @return the HashCode.
     */
    public int hashCode(){
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    /**
     * Returns true if the other Object is an instance of <code>NemaTrack</code> with an 
     * identical trackID. The compariosn is case sensitive.
     * @param object the Object to compare to.
     * @return a flag indicating equality.
     */
    public boolean equals(Object object){
        if (!(object instanceof NemaTrack)){
            return false;
        }
        NemaTrack other = (NemaTrack)object;
        if (id.equals(other.id)){
            return true;
        }
        return false;
    }

	@Override
	public String toString() {
		return "org.imirsel.nema.model.NemaTrack [id=" + id + "]";
	}



}
