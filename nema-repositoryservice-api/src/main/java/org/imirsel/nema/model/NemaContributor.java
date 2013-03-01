package org.imirsel.nema.model;

public class NemaContributor {
	private String title;
	private String firstName;
	private String lastName;
	private String affiliationUrl;
	private String affiliation;
	private String affiliationDept;
	private String affiliationDeptUnit;
	
	public NemaContributor(String title, String firstName, String lastName,
			String affiliationUrl, String affiliation, String affiliationDept,
			String affiliationDeptUnit) {
		super();
		this.title = title;
		this.firstName = firstName;
		this.lastName = lastName;
		this.affiliationUrl = affiliationUrl;
		this.affiliation = affiliation;
		this.affiliationDept = affiliationDept;
		this.affiliationDeptUnit = affiliationDeptUnit;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	public String getTitle() {
		return title;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setAffiliationUrl(String affiliationUrl) {
		this.affiliationUrl = affiliationUrl;
	}
	public String getAffiliationUrl() {
		return affiliationUrl;
	}
	public void setAffiliation(String affiliation) {
		this.affiliation = affiliation;
	}
	public String getAffiliation() {
		return affiliation;
	}
	public void setAffiliationDept(String affiliationDept) {
		this.affiliationDept = affiliationDept;
	}
	public String getAffiliationDept() {
		return affiliationDept;
	}
	public void setAffiliationDeptUnit(String affiliationDeptUnit) {
		this.affiliationDeptUnit = affiliationDeptUnit;
	}
	public String getAffiliationDeptUnit() {
		return affiliationDeptUnit;
	}
}
