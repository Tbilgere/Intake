package com.sample.team;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.ExitCodeMapper;
import org.springframework.batch.core.launch.support.JvmSystemExiter;
import org.springframework.batch.core.launch.support.SimpleJvmExitCodeMapper;
import org.springframework.batch.core.launch.support.SystemExiter;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication(exclude = { BatchAutoConfiguration.class })
public class App {

	private final static Logger log = LoggerFactory.getLogger(App.class);

	public static void main(String[] args) {
		SystemExiter sysExiter = new JvmSystemExiter();
		ExitCodeMapper exitCodeMapper = new SimpleJvmExitCodeMapper();
		// Build Spring Context
		ConfigurableApplicationContext ctx = new SpringApplicationBuilder(App.class).run(args);
		try {
			// Pass current Spring Context and Job name to method to launch job
			JobExecution exec = executeJob(ctx, "createRecord");
			sysExiter.exit(exitCodeMapper.intValue(exec.getExitStatus().getExitCode()));
		} catch (Throwable e) {
			log.error("Job Terminated in error: ", e);
			sysExiter.exit(exitCodeMapper.intValue(ExitStatus.FAILED.getExitCode()));
		}
	}

	public static JobExecution executeJob(ApplicationContext context, String jobName)
			throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException,
			JobParametersInvalidException {
		// Get a JobLauncher bean from current Spring Context
		JobLauncher jobLauncher = context.getBean(JobLauncher.class);
		// Pull job by name from current Spring Context
		Job job = context.getBean(jobName, Job.class);
		// Set Job parameters ***Default is current date and time***
		JobParametersBuilder paramBuilder = new JobParametersBuilder().addDate("date", new Date());
		JobParameters jobParam = paramBuilder.toJobParameters();
		// Launch Job
		JobExecution exec = jobLauncher.run(job, jobParam);
		return exec;
	}
}
