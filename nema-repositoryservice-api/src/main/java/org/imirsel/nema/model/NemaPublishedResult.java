/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.imirsel.nema.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Class representing a published result for an algorithm against a task. 
 * 
 * @author kris.west@gmail.com
 */
public class NemaPublishedResult implements Serializable{
    public static final long serialVersionUID = 1L;

    private int id;
    private int setId;
    private int taskId;
    private String submissionCode;
    private String name;
    private String result_path;
    private String fileType;
    private Date date;

    /**
     * Constructor. A unique id, an id for the task that the result applies to, 
     * the submission code of the publishing flow, an algorithm name, a path to 
     * the results on the filesystem and date of publication must be specified.
     * 
     * @param id The unique id fir the result.
     * @param taskId The task ID that the result refers to.
     * @param setId The set ID that the result refers to.
     * @param submissionCode The submission code of the publishing flow.
     * @param name The name of the system or algorithm that produced the result.
     * @param result_path The path to the results directory on the filesystem/
     * @param date The date of publication.
     * @param fileType The class name of the NemaFileType associated with the 
     * output.
     */
    public NemaPublishedResult(int id, int taskId, int setId, String submissionCode, 
    		String name, String result_path, Date date, String fileType){
        this.id = id;
        this.taskId = taskId;
        this.setId = setId;
    	this.submissionCode = submissionCode;
        this.name = name;
        this.result_path = result_path;
        this.date = date;
        this.fileType = fileType;
    }

    /**
     * @return the id
     */
    public int getId(){
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id){
        this.id = id;
    }
    

    /**
     * @return the task id
     */
    public int getTaskId(){
        return taskId;
    }

    /**
     * @param id the task id to set
     */
    public void setTaskId(int id){
        this.taskId = id;
    }

    /**
     * @return the name
     */
    public String getName(){
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name){
        this.name = name;
    }

    /**
     * @return the result_path
     */
    public String getResult_path(){
        return result_path;
    }

    /**
     * @param result_path the result_path to set
     */
    public void setResult_path(String result_path){
        this.result_path = result_path;
    }

    /**
     * @return the date
     */
    public Date getDate(){
        return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(Date date){
        this.date = date;
    }

    /**
     * @return the submission code
     */
    public String getSubmissionCode(){
        return submissionCode;
    }

    /**
     * @param submissionCode the submission code to set
     */
    public void setSubmissionCode(String submissionCode){
        this.submissionCode = submissionCode;
    }

	@Override
	public String toString() {
		return "org.imirsel.nema.model.NemaPublishedResult [date=" + date + 
				", id=" + id + ", taskId=" + taskId + ", setId=" + setId + 
				", name=" + name + ", result_path=" + result_path + 
				", username=" + submissionCode + 
				", filetype=" + fileType + "]";
	}

	public void setSetId(int setId) {
		this.setId = setId;
	}

	public int getSetId() {
		return setId;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getFileType() {
		return fileType;
	}


}
