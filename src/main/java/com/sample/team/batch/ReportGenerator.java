package com.sample.team.batch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import com.sample.team.config.AppProperties;

public class ReportGenerator implements Tasklet {
	
	private final static Logger log = LoggerFactory.getLogger(ReportGenerator.class);
	
	private final static String SYSTEM_LINE_SEPARATOR = System.getProperty("line.separator");
	
	private String lineSeparator = SYSTEM_LINE_SEPARATOR;
	
	@Autowired
	private AppProperties appProps;
	
	@Autowired
	private EmailTasklet emailTask;
	
	private int recordsReceived = 0;
	
	private Map<String, String> errorMap = new HashMap<String, String>();
	
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		log.info("Report Generating");
		generateReport();
		return RepeatStatus.FINISHED;
	}
	
	private void generateReport(){
		String nowTitleDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		String reportFilename = appProps.getReportFilename().replace("##DATE##", nowTitleDate);
		String reportFileAddress = appProps.getResourceAddress() + appProps.getReportAddress() + reportFilename;
		
		if (recordsReceived > 0){
			emailTask.addReport(reportFileAddress);
		}
		BufferedWriter bufferedWriter = null;
		try {
			File reportFile = new File(reportFileAddress);
			log.info("Report built at: {}", reportFile.getAbsolutePath());
			if(!reportFile.exists()){
				reportFile.createNewFile();
			}
			FileWriter fileWriter = new FileWriter(reportFile.getAbsoluteFile());
			bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write("Error, Error Message" + lineSeparator);
			for(Entry<String, String> entry: errorMap.entrySet()) {
				String error = entry.getKey();
				String message = entry.getValue();
				bufferedWriter.write(error + ", " + message + lineSeparator);
			}
			
			bufferedWriter.write(lineSeparator + "Records recieved: " + recordsReceived + lineSeparator);
			
		} catch (Exception e){
			log.warn("Report not generated due to {}", e.getMessage());
			
		} finally {
			try {
				if(bufferedWriter != null){
					bufferedWriter.close();
				}
			} catch (IOException e) {
				log.error("Error closing buffered writer: {}", e.getMessage());
			}
		}
	}
	
	public Map<String, String> getErrorMap(){
		return errorMap;
	}
	
	public void setErrorMap(Map<String, String> errorMap){
		this.errorMap = errorMap;
	}
	
	public void addError(String error, String message) {
		this.errorMap.put(error, message);
	}
	
	public String getLineSeparator() {
		return lineSeparator;
	}

	public void setDefaultLineSeparator(String lineSeparator) {
		this.lineSeparator = lineSeparator;
	}

}
