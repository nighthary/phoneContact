package com.night.contact.util;

import java.io.Serializable;
import java.util.Map;

import com.night.contact.bean.SortEntry;

public class SerializableMap implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private Map<String,SortEntry> map;


	public Map<String, SortEntry> getMap() {
		return map;
	}


	public void setMap(Map<String, SortEntry> map) {
		this.map = map;
	} 
	
}
