/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.imirsel.nema.model;

import java.io.Serializable;

/**
 * A class representing a 'task' for NEMA. A task is a specific experiment definition
 * describing what must done, but abstracts details of how that should be accomplished.
 * For example a task might be defined as the classification of music according to
 * genre on a specified dataset. The performance of multiple solutions to a task
 * can be evaluated together and statistical comparison of results performed.
 * 
 * In the context of the NEMA service a NemaTask will be associated with a specific
 * flow  template that may be completed to implement a solution to the task. The flow
 * will define components to provide access to the dataset, perform evaluation 
 * of results and a number of external execution components that can execute code 
 * external to the NEMA service to perform specific tasks (e.g. feature extraction, 
 * model training and application of the model to produce classifications for novel 
 * examples).
 * 
 * @author kris.west@gmail.com
 */
public class NemaTask implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int id = -1;
    private String name = null;
    private String description = null;
    private int subjectTrackMetadataId = -1;
    private String subjectTrackMetadataName = null;
    private int datasetId = -1;

    public NemaTask(){
    }

    public NemaTask(Integer id){
        this.id = id;
    }

    public NemaTask(int id, String name, String description, int subjectTrackMetadataID,
            String subjectTrackMetadataName, int datasetId){
        this.id = id;
        this.name = name;
        this.description = description;
        this.subjectTrackMetadataName = subjectTrackMetadataName;
        this.subjectTrackMetadataId = subjectTrackMetadataID;
        this.datasetId = datasetId;
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getDescription(){
        return description;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public String getSubjectTrackMetadataName(){
        return subjectTrackMetadataName;
    }

    public void setSubjectTrackMetadataName(String subjectTrackMetadataName){
        this.subjectTrackMetadataName = subjectTrackMetadataName;
    }


    @Override
    public int hashCode(){
        return id;
    }

    @Override
    public boolean equals(Object object){
        if (!(object instanceof NemaTask)){
            return false;
        }
        NemaTask other = (NemaTask)object;
        if (this.id == other.id){
            return true;
        }
        return false;
    }

    /**
     * @return the subjectTrackMetadataID
     */
    public int getSubjectTrackMetadataId(){
        return subjectTrackMetadataId;
    }

    /**
     * @param subjectTrackMetadataID the subjectTrackMetadataID to set
     */
    public void setSubjectTrackMetadataId(int subjectTrackMetadataID){
        this.subjectTrackMetadataId = subjectTrackMetadataID;
    }

    /**
     * @return the datasetId
     */
    public int getDatasetId(){
        return datasetId;
    }

    /**
     * @param datasetId the datasetId to set
     */
    public void setDatasetId(int datasetId){
        this.datasetId = datasetId;
    }

	@Override
	public String toString() {
		return "org.imirsel.nema.model.NemaTask [datasetId=" + datasetId + ", description="
				+ description + ", id=" + id + ", name=" + name
				+ ", subjectTrackMetadataId=" + subjectTrackMetadataId
				+ ", subjectTrackMetadataName=" + subjectTrackMetadataName
				+ "]";
	}

}
