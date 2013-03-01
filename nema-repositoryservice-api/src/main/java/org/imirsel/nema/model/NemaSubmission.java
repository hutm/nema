package org.imirsel.nema.model;

import java.util.List;


public class NemaSubmission {
	private String submissionCode;
	private String submissionName;
	private List<NemaContributor> contributors;
	private String abstractUrl;
	
	public NemaSubmission(String submissionCode, String submissionName,
			List<NemaContributor> contributors, String abstractUrl) {
		this.submissionCode = submissionCode;
		this.submissionName = submissionName;
		this.contributors = contributors;
		this.abstractUrl = abstractUrl;
	}
	
	public String getSubmissionCode() {
		return submissionCode;
	}
	public void setSubmissionCode(String submissionCode) {
		this.submissionCode = submissionCode;
	}
	public String getSubmissionName() {
		return submissionName;
	}
	public void setSubmissionName(String submissionName) {
		this.submissionName = submissionName;
	}
	public List<NemaContributor> getContributors() {
		return contributors;
	}
	public void setContributors(List<NemaContributor> contributors) {
		this.contributors = contributors;
	}
	public String getAbstractUrl() {
		return abstractUrl;
	}
	public void setAbstractUrl(String abstractUrl) {
		this.abstractUrl = abstractUrl;
	}
	
	
}
