package com.sample.team.batch;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.sample.team.model.Records;

// RowMapper must be configured for expected SQL Table
public class CustomRowMapper implements RowMapper<Records>{

	@Override
	public Records mapRow(ResultSet rs, int rowNum) throws SQLException {
		Records returnValue = new Records();
		
		returnValue.addDataToMap("recordId", rs.getString("Record_Id"));
		returnValue.addDataToMap("recordDate", rs.getDate("Record_Date"));
		returnValue.addDataToMap("recordContent", rs.getString("Record_Content"));
		
		return returnValue;
	}

}
