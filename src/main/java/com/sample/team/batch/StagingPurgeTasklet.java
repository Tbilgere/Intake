package com.sample.team.batch;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.sample.team.config.AppProperties;

public class StagingPurgeTasklet implements Tasklet {

	@Autowired
	private AppProperties appProps;
	
	private JdbcTemplate stagingTemplate;
	
	public StagingPurgeTasklet(JdbcTemplate jdbcTemplate) {
		this.stagingTemplate= jdbcTemplate;
	}
	
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		stagingTemplate.execute(appProps.getPurgeStatement());
		return RepeatStatus.FINISHED;
	}

}
