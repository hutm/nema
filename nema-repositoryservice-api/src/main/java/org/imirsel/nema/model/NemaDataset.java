/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.imirsel.nema.model;

import java.io.Serializable;

/**
 * A class representing a NEMA dataset. <code>NemaDataset</code>s have an ID, name, description, 
 * a link to a <code>NemaTrackList</code> defining the tracks in the set, a number of folds in the set 
 * (e.g. a number of paired test and train sets), a number of sets per fold (e.g. 2 for a classic
 * train/test classification experiment, 1 for an analysis dataset), a subject metadata type id (e.g.
 * 1 for genre, -1 for none) which experiments based on the dataset will be based on (e.g. genre
 * for genre classification) and the split will have been optimised for (i.e. making sure that all
 * class in the dataset have examples in both the test and training sets), a filter metadata type id 
 * (e.g. 2 for artist, -1 for none) which was used to filter the split (i.e. making sure the artist
 * only appears in either train or test sets). 
 *
 * @author kris.west@gmail.com
 */
public class NemaDataset implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id = -1;
    private String name = null;
    private String description = null;
    private int subsetTrackListId = -1;
    private int numFolds = -1;
    private int numTrackListsPerFold = -1;
    private String splitClass = null;
    private String splitParametersString = null;
    private int subjectTrackMetadataId = -1;
    private String subjectTrackMetadataName = null;
    private int filterTrackMetadataId = -1;
    private String filterTrackMetadataName = null;

    /**
     * No arg constructor.
     */
    public NemaDataset(){
    	
    }

    /**
     * Constructor. The ID is specified, all other fields are null and must be set manually if
     * they are to be used.
     * 
     * @param id The dataset ID to set.
     */
    public NemaDataset(int id){
        this.id = id;
    }

    /**
     * Full constructor.
     * * @param id The dataset ID.
     * @param name The dataset name.
     * @param description The description of the dataset.
     * @param subsetTrackListId the ID of the subset defining the tracks in the subset.
     * @param numFolds The number of splits in the dataset, 1 or more.
     * @param numTrackListsPerFold The number of sets in each split, 1 or more.
     * @param splitClass Placeholder to be used to hold the method used to produce the split.
     * @param splitParametersString Placeholder to be used to hold the parameters of the split 
     * method used to produce the split.
     * @param subjectTrackMetadataId a subject metadata type id (e.g.
	 * 1 for genre, -1 for none) which experiments based on the dataset will be based on (e.g. genre
	 * for genre classification) and the split will have been optimised for (i.e. making sure that all
	 * class in the dataset have examples in both the test and training sets).
     * @param subjectTrackMetadataName The subject metadata type name.
     * @param filterTrackMetadataId a filter metadata type id 
	 * (e.g. 2 for artist, -1 for none) which was used to filter the split (i.e. making sure the artist
	 * only appears in either train or test sets). 
     * @param filterTrackMetadataName The filter metadata type name.
     */
    public NemaDataset(int id, String name, String description, 
                       int subsetTrackListId, int numFolds, int numTrackListsPerFold,
                       String splitClass, String splitParametersString,
                       int subjectTrackMetadataId, String subjectTrackMetadataName,
                       int filterTrackMetadataId, String filterTrackMetadataName){
        this.id = id;
        this.name = name;
        this.description = description;
        this.subsetTrackListId = subsetTrackListId;
        this.numFolds = numFolds;
        this.numTrackListsPerFold = numTrackListsPerFold;
        this.splitClass = splitClass;
        this.splitParametersString = splitParametersString;
        this.subjectTrackMetadataId = subjectTrackMetadataId;
        this.filterTrackMetadataId = filterTrackMetadataId;
        this.subjectTrackMetadataName = subjectTrackMetadataName;
        this.filterTrackMetadataName = filterTrackMetadataName;
    }

    /**
     * Returns the dataset ID.
     * @return the dataset ID.
     */
    public int getId(){
        return id;
    }

    /**
     * Sets the dataset ID.
     * @param id the dataset ID to set.
     */
    public void setId(Integer id){
        this.id = id;
    }

    /**
     * Returns the dataset name.
     * @return the dataset name.
     */
    public String getName(){
        return name;
    }

    /**
     * Sets the dataset name.
     * @param name the dataset name to set.
     */
    public void setName(String name){
        this.name = name;
    }

    /**
     * Returns the dataset description.
     * @return the dataset description.
     */
    public String getDescription(){
        return description;
    }

    /**
     * Sets the data description.
     * @param description The description to set.
     */
    public void setDescription(String description){
        this.description = description;
    }

    /**
     * Returns the ID of the <code>NemaTrackList</code> defining the subset of tracks under the NEMA repository 
     * in the dataset.
     * @return The ID of the <code>NemaTrackList</code> defining the subset of tracks.
     */
    public int getSubsetTrackListId(){
        return subsetTrackListId;
    }

    /**
     * Sets the ID of the <code>NemaTrackList</code> defining the subset of tracks under the NEMA repository 
     * in the dataset.
     * @param subsetTrackListId Sets the ID of the <code>NemaTrackList</code> defining the subset of tracks.
     */
    public void setSubsetTrackListId(int subsetTrackListId){
        this.subsetTrackListId = subsetTrackListId;
    }

    /**
     * Returns the number of folds.
     * @return the number of folds.
     */
    public int getNumFolds(){
        return numFolds;
    }

    /**
     * Sets the number of folds in the dataset. Must be kept in sync with the fold numbers
     * stored against the NemaTrackList Objects linked to the dataset in the repository.
     * @param numFolds the number of splits in the dataset to set.
     */
    public void setNumFolds(int numFolds){
        this.numFolds = numFolds;
    }

    /**
     * Returns the number of sets in each split of the dataset.
     * @return the number of sets in each split of the dataset.
     */
    public int getNumTrackListsPerFold(){
        return numTrackListsPerFold;
    }

    /**
     * Sets the number of track lists in each fold of the dataset. Must be kept in sync with the 
     * NemaTrackList Objects linked to the dataset in the repository.
     * @param numTrackListsPerFold the number of track lists in each fold of the dataset to set.
     */
    public void setNumTrackListsPerFold(int numTrackListsPerFold){
        this.numTrackListsPerFold = numTrackListsPerFold;
    }

    /**
     * Placeholder for returning the split method used to produce the dataset.
     * @return name of the split class.
     */
    public String getSplitClass(){
        return splitClass;
    }

    /**
     * Placeholder for returning the split method used to produce the dataset.
     * @param splitClass the split class to set.
     */
    public void setSplitClass(String splitClass){
        this.splitClass = splitClass;
    }

    /**
     * Placeholder for returning the parameters of the split method used to produce the dataset.
     * @return The parameters of the split method.
     */
    public String getSplitParametersString(){
        return splitParametersString;
    }

    /**
     * Placeholder for setting the parameters of the split method used to produce the dataset.
     * @param splitParametersString The parameters of the split method to set.
     */
    public void setSplitParametersString(String splitParametersString){
        this.splitParametersString = splitParametersString;
    }

    /**
     * Returns the ID of the subject track metadata.
     * @return the ID of the subject track metadata.
     */
    public int getSubjectTrackMetadataId(){
        return subjectTrackMetadataId;
    }

    /**
     * Sets the ID of the subject track metadata.
     * @param subjectTrackMetadataId the ID of the subject track metadata to set.
     */
    public void setSubjectTrackMetadataId(int subjectTrackMetadataId){
        this.subjectTrackMetadataId = subjectTrackMetadataId;
    }

    /**
     * Returns the ID of the filter track metadata.
     * @return the ID of the filter track metadata.
     */
    public int getFilterTrackMetadataId(){
        return filterTrackMetadataId;
    }

    /**
     * Sets the ID of the filter track metadata.
     * @param filterTrackMetadataId the ID of the filter track metadata.
     */
    public void setFilterTrackMetadataId(int filterTrackMetadataId){
        this.filterTrackMetadataId = filterTrackMetadataId;
    }

    /**
     * Returns the name of the subject track metadata.
     * @return the name of the subject track metadata.
     */
    public String getSubjectTrackMetadataName(){
        return subjectTrackMetadataName;
    }

    /**
     * Sets the name of the subject track metadata.
     * @param subjectTrackMetadataName the name of the subject track metadata.
     */
    public void setSubjectTrackMetadataName(String subjectTrackMetadataName){
        this.subjectTrackMetadataName = subjectTrackMetadataName;
    }

    /**
     * Returns the name of the filter track metadata.
     * @return the name of the filter track metadata.
     */
    public String getFilterTrackMetadataName(){
        return filterTrackMetadataName;
    }

    /**
     * Sets the name of the filter track metadata.
     * @param filterTrackMetadataName the name of the filter track metadata.
     */
    public void setFilterTrackMetadataName(String filterTrackMetadataName){
        this.filterTrackMetadataName = filterTrackMetadataName;
    }

    @Override
    /**
     * HashCodes are based on the dataset ID.
     * @return the HashCode.
     */
    public int hashCode(){
        return id;
    }

    @Override
    /**
     * Returns true if the other Object is an instance of <code>NemaDataset</code> with an
     * identical ID.
     * @param object the object to compare to.
     * @return a boolean indicating equality.
     */
    public boolean equals(Object object){
        if (!(object instanceof NemaDataset)){
            return false;
        }
        NemaDataset other = (NemaDataset)object;
        if (id == other.id){
            return true;
        }
        return false;
    }

	@Override
	public String toString() {
		return "org.imirsel.nema.model.NemaDataset [description=" + description
				+ ", filterTrackMetadataId=" + filterTrackMetadataId
				+ ", filterTrackMetadataName=" + filterTrackMetadataName
				+ ", id=" + id + ", name=" + name + ", numFolds=" + numFolds
				+ ", numTrackListsPerFold=" + numTrackListsPerFold
				+ ", splitClass=" + splitClass + ", splitParametersString="
				+ splitParametersString + ", subjectTrackMetadataId="
				+ subjectTrackMetadataId + ", subjectTrackMetadataName="
				+ subjectTrackMetadataName + ", subsetTrackListId="
				+ subsetTrackListId + "]";
	}

    
}
