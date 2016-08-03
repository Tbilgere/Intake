package com.sample.team.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.beans.factory.annotation.Autowired;

import com.sample.team.config.AppProperties;
import com.sample.team.model.Records;

// Mapper must be configured for expected flat file.
public class CustomFieldSetMapper implements FieldSetMapper<Records> {

	private final static Logger log = LoggerFactory.getLogger(CustomFieldSetMapper.class);

	@Autowired
	private AppProperties appProps;

	@Override
	public Records mapFieldSet(FieldSet fieldSet) {
		Records returnValue = null;
		try {
			returnValue = new Records();
			returnValue.addDataToMap("recordId", fieldSet.readString(0));
			returnValue.addDataToMap("recordDate", fieldSet.readDate(1, appProps.getDateFormat()));
			returnValue.addDataToMap("recordContent", fieldSet.readString(2));
		} catch (IllegalArgumentException e) {
			log.warn("Record date is not formated as expceted");
			String failId = "could not read";
			if (returnValue != null && !returnValue.getDataMap().isEmpty()) {
				returnValue.getDataMap().get("recordId");
			}
			log.debug("failId: " + failId);
			returnValue = null;
		} catch (Exception e) {
			log.warn("Blank line found! Skipping to protect processing");
			String failId = "could not read";
			if (returnValue != null && !returnValue.getDataMap().isEmpty()) {
				returnValue.getDataMap().get("recordId");
			}
			log.debug("failId: " + failId);
			returnValue = null;
		}

		return returnValue;
	}

}
