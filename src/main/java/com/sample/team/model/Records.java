package com.sample.team.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Records {
	
	private final static Logger log = LoggerFactory.getLogger(Records.class);
	
	private Map<String, Object> dataMap = new HashMap<String, Object>();

	public void addDataToMap(String key, Object value){
		dataMap.put(key, value);
	}
	
	public List<Object> getMapData(){
		return new ArrayList<Object>(Arrays.asList(dataMap.values().toArray()));
	}
	
	public Map<String, Object> getDataMap() {
		return dataMap;
	}

	public void setDataMap(Map<String, Object> dataMap) {
		this.dataMap = dataMap;
	}
	
	public void clearDataMap(){
		dataMap.clear();
		log.debug("Data Map cleared!");
	}

}
