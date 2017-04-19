package com.microservice.data;

import java.io.Serializable;




public class Author implements Serializable
{
	private static final long serialVersionUID = 8737396563395199126L;
	
	//????ID??????
	private int ID;		
	private String name;
	
	//????????
	private String url;
	
	//??????????????????ID????PDMI????????????ID??
	private int adminID;
	private String adminName;
	
	private int role;
	
	//????????PDMI?е????ID
	private String pdmiID;
	
	//??????????????ID?????????????????????
	private String deptID;
	private String deptName;
 
	//???????????????????????ж??????(????????????????)
	private String alias;
	
	//?????????????
	private int status;
	
	/**
	 * ??????
	 */
	public Author()
	{
		
	}
	
	
    public String getName() {
    	return name;
    }

    public void setName(String name) {
    	this.name = name;
    }
    
    
    public String getUrl() {
		return url;
	}


	public void setUrl(String url) {
		this.url = url;
	}


	public int getID() {
		return ID;
	}

	public void setID(int ID) {
		this.ID = ID;
	}

	public int getAdminID() {
		return adminID;
	}

	public void setAdminID(int adminID) {
		this.adminID = adminID;
	}


	public String getAdminName() {
		return adminName;
	}


	public void setAdminName(String adminName) {
		this.adminName = adminName;
	}


	public int getRole() {
		return role;
	}


	public void setRole(int role) {
		this.role = role;
	}


	public String getDeptID() {
		return deptID;
	}


	public void setDeptID(String deptID) {
		this.deptID = deptID;
	}


	public String getDeptName() {
		return deptName;
	}


	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}


	
	public String getAlias() {
		return alias;
	}


	public void setAlias(String alias) {
		this.alias = alias;
	}


	public String getPdmiID() {
		return pdmiID;
	}


	public void setPdmiID(String pdmiID) {
		this.pdmiID = pdmiID;
	}


	public int getStatus() {
		return status;
	}


	public void setStatus(int status) {
		this.status = status;
	}

	
}
