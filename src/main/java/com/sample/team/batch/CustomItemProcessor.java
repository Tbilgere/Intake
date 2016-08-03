package com.sample.team.batch;

import java.util.Map;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import com.sample.team.config.AppProperties;
import com.sample.team.model.Records;

public class CustomItemProcessor implements ItemProcessor<Records, Records>{
	
	@Autowired
	private AppProperties appProps;
	
	private JdbcTemplate stagingTemplate;
	
	public CustomItemProcessor(JdbcTemplate jdbcTemplate){
		this.stagingTemplate = jdbcTemplate;
	}

	@Override
	public Records process(Records item) throws Exception {
		Records returnValue = null;
		if(item !=null){
			Records recordInTable = findCurrentData(item);
			if(recordInTable != null){
				Map<String, Object> currentDataMap = item.getDataMap();
				String newContent = recordInTable.getDataMap().get("recordContent") + ", " + currentDataMap.get("recordContent");
				currentDataMap.put("recordContent", newContent);
			}
			returnValue = item;
		}
		return returnValue;
	}
	
	private Records findCurrentData(Records currentItem){
		Records returnValue = new Records();
		String currentRecordId = (String) currentItem.getDataMap().get("recordId");
		try{
			// Attempt to create record from staging to be returned
			returnValue = stagingTemplate.queryForObject(appProps.getStagingRead(), new CustomRowMapper(), currentRecordId);
			// If record is found mark as processed in staging 
			stagingTemplate.update(appProps.getProcessUpdate(), currentRecordId);
			
		} catch (EmptyResultDataAccessException e) {
			// If no record is found in staging return null
			returnValue = null;
		}
		return returnValue;
	}

}
