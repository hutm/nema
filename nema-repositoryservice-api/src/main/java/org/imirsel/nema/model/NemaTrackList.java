/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.imirsel.nema.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A class representing a set of tracks from the NEMA repository. Sets are primarily used to 
 * provide lists of tracks for use with <code>NemaDataset</code>s. Hence, <code>NemaTrackList</code>
 * Objects have a unique ID, a <code>NemaDataset</code> ID to which they belong, set type information
 * and a split number (identifying which split within the dataset that the set belongs to). 
 * 
 * @author kris.west@gmail.com
 */
public class NemaTrackList implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private Integer datasetId;
    private Integer trackListTypeId;
    private String trackListTypeName;
    private Integer foldNumber;
    private List<NemaTrack> tracks;

    /**
     * Constructor. Sets only the track list ID. All other parameters must be manually set if they are used.
     * @param id The ID to set.
     */
    public NemaTrackList(int id){
        this.id = id;
        this.tracks = null;
    }

    /**
     * Constructor. Sets the ID, dataset ID, track list type ID, track list type name and fold number. List
     * of tracks is null.
     * @param id The list ID.
     * @param datasetId The dataset ID to which this set belongs.
     * @param trackListTypeId The type ID of the track list.
     * @param trackListTypeName The type name corresponding to the track list type ID (e.g. subset, train, test etc.).
     * @param foldNumber The fold number within the dataset to which the set belongs. 
     */
    public NemaTrackList(int id, int datasetId, int trackListTypeId, String trackListTypeName,int foldNumber){
        this.id = id;
        this.datasetId = datasetId;
        this.trackListTypeId = trackListTypeId;
        this.trackListTypeName = trackListTypeName;
        this.foldNumber = foldNumber;
        this.tracks = null;
    }

    

    /**
	 * Constructor. Sets the ID, dataset ID, track list type ID, track list type name, fold number and List
     * of tracks.
     * @param id The list ID.
	 * @param datasetId The dataset ID to which this set belongs.
	 * @param trackListTypeId The type ID of the track list.
	 * @param trackListTypeName The type name corresponding to the track list type ID (e.g. subset, train, test etc.).
	 * @param foldNumber The fold number within the dataset to which the set belongs. 
	 * @param tracks The list of NemaTrack Objects in the list.
	 */
	public NemaTrackList(int id, int datasetId, int trackListTypeId,
			String trackListTypeName, int foldNumber, List<NemaTrack> tracks) {
		this.id = id;
		this.datasetId = datasetId;
		this.trackListTypeId = trackListTypeId;
		this.trackListTypeName = trackListTypeName;
		this.foldNumber = foldNumber;
		this.tracks = tracks;
	}
    
	/**
     * Returns the ID of the set.
     * @return the ID.
     */
    public int getId(){
        return id;
    }

    /**
     * Sets the ID of the set.
     * @param id the ID to set.
     */
    public void setId(int id){
        this.id = id;
    }

    /**
     * Returns the ID of the <code>NemaDataset</code> that the set belongs to.
     * @return the ID of the dataset.
     */
    public int getDatasetId(){
        return datasetId;
    }

    /**
     * Sets the ID of the <code>NemaDataset</code> that the set belongs to.
     * @param datasetId the dataset ID to set.
     */
    public void setDatasetId(int datasetId){
        this.datasetId = datasetId;
    }

    /** 
     * Returns the set type ID.
     * @return the set type ID.
     */
    public int getTrackListTypeId(){
        return trackListTypeId;
    }

    /**
     * Sets the set type ID.
     * @param trackListTypeId the set type ID to set.
     */
    public void setTrackListTypeId(int setTypeId){
        this.trackListTypeId = setTypeId;
    }

    /**
     * Returns the set type name.
     * @return the trackListTypeName.
     */
    public String getTrackListTypeName(){
        return trackListTypeName;
    }

    /**
     * Sets the set type name.
     * @param trackListTypeName the trackListTypeName to set
     */
    public void setTrackListTypeName(String trackListTypeName){
        this.trackListTypeName = trackListTypeName;
    }
    
    /**
     * Returns the split number within the dataset that the set belongs to.
     * @return the split number.
     */
    public int getFoldNumber(){
        return foldNumber;
    }

    /**
     * Sets the split number within the dataset that the set belongs to.
     * @param foldNumber the split number to set.
     */
    public void setFoldNumber(int foldNumber){
        this.foldNumber = foldNumber;
    }
    
    /**
     * Returns the list of NemaTrack Objects in the list.
     * @return the list of NemaTrack Objects.
     */
    public List<NemaTrack> getTracks(){
    	return tracks;
    }
    
    /**
     * Sets the list of NemaTrack Objects in the list.
     * @param tracks the list of NemaTrack Objects to set.
     */
    public void setTracks(List<NemaTrack> tracks){
    	this.tracks = tracks;
    }

    @Override
    /**
     * HashCodes are based on the set ID which should be uniquely assigned by the repository DB.
     * @return the HashCode.
     */
    public int hashCode(){
        return id;
    }

    @Override
    /**
     * Returns true if the other object is an instance of <code>NemaTrackList</code> and has the same ID.
     * @param object The Object to compare to.
     * @return a boolean indicating equality.
     */
    public boolean equals(Object object){
        if (!(object instanceof NemaTrackList)){
            return false;
        }
        NemaTrackList other = (NemaTrackList)object;
        if (id == other.id){
            return true;
        }
        return false;
    }

    @Override
    public String toString(){
        return "org.imirsel.nema.model.NemaTrackList[id=" + id + ", datasetID=" + datasetId + ", splitNum=" + foldNumber + "," +
        		" setTypeID=" + trackListTypeId + ", trackListTypeName=" + trackListTypeName + "]";
    }

}
