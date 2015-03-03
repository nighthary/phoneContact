package com.night.contact.bean;

import java.util.Date;

public class CallLog {
	private String callName = "";
	private String callNumber;
	private int callType;
	private Date callDate;
	private String numberPlace;
	public String getCallName() {
		return callName;
	}
	public void setCallName(String callName) {
		this.callName = callName;
	}
	public String getCallNumber() {
		return callNumber;
	}
	public void setCallNumber(String callNumber) {
		this.callNumber = callNumber;
	}
	public int getCallType() {
		return callType;
	}
	public void setCallType(int callType) {
		this.callType = callType;
	}
	public Date getCallDate() {
		return callDate;
	}
	public void setCallDate(Date callDate) {
		this.callDate = callDate;
	}
	public String getNumberPlace() {
		return numberPlace;
	}
	public void setNumberPlace(String numberPlace) {
		this.numberPlace = numberPlace;
	}
	
}
