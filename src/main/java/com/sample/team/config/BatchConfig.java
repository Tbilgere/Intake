package com.sample.team.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.sample.team.batch.ArchiveTasklet;
import com.sample.team.batch.CustomFieldSetMapper;
import com.sample.team.batch.CustomItemProcessor;
import com.sample.team.batch.EmailTasklet;
import com.sample.team.batch.ReportGenerator;
import com.sample.team.batch.StagingPurgeTasklet;
import com.sample.team.model.Records;

@Configuration
@EnableBatchProcessing
@ComponentScan
@EnableAutoConfiguration
public class BatchConfig {

	private final static Logger log = LoggerFactory.getLogger(BatchConfig.class);

	private final static String DEFAULT_SEPARATOR = FileSystems.getDefault().getSeparator();

	// JDBC template for staging table
	@Bean
	public JdbcTemplate stagingTemplate(@Qualifier("outputDB") DataSource dataSrouce) {
		return new JdbcTemplate(dataSrouce);
	}

	@Bean
	public FieldSetMapper<Records> customFieldSetMapper() {
		return new CustomFieldSetMapper();
	}

	@Bean
	public DelimitedLineTokenizer tokenizer() {
		return new DelimitedLineTokenizer();
	}

	@Bean
	public ArchiveTasklet archiveTasklet() {
		return new ArchiveTasklet();
	}

	@Bean
	public ReportGenerator reportGenerator() {
		return new ReportGenerator();
	}

	@Bean
	public AppProperties appProperties() {
		return new AppProperties();
	}

	@Bean
	public MailProperties mailProps() {
		return new MailProperties();
	}

	@Bean
	public StagingPurgeTasklet stagingPurge(JdbcTemplate stagingTemplate) {
		return new StagingPurgeTasklet(stagingTemplate);
	}

	@Bean
	public EmailTasklet emailTasklet(MailProperties mailProps, JavaMailSender javaMailSender) {
		return new EmailTasklet(mailProps, javaMailSender);
	}

	@Bean
	public ItemProcessor<Records, Records> processor(JdbcTemplate stagingTemplate) {
		return new CustomItemProcessor(stagingTemplate);
	}

	// Using a standard JDBC item writer to write to staging table
	@Bean
	public ItemWriter<Records> writer(@Qualifier("outputDB") DataSource dataSource, AppProperties appProps) {
		JdbcBatchItemWriter<Records> writer = new JdbcBatchItemWriter<Records>();
		writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<Records>());
		// SQL statement set up in property file
		writer.setSql(appProps.getStagingWrite());
		writer.setDataSource(dataSource);
		log.debug("Writer Built");
		return writer;
	}

	// Java mail sender to send out report messages
	@Bean
	public JavaMailSender javaMailSender(MailProperties mailProps) {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		Properties mailProperties = new Properties();
		mailProperties.put("mail.smtp.auth", mailProps.isAuth());
		mailProperties.put("mail.smtp.starttls.enable", mailProps.isStarttlsEnable());
		mailSender.setJavaMailProperties(mailProperties);
		mailSender.setHost(mailProps.getHost());
		mailSender.setPort(mailProps.getPort());
		mailSender.setProtocol(mailProps.getProtocol());
		mailSender.setUsername(mailProps.getUsername());
		mailSender.setPassword(mailProps.getPassword());
		if (mailProps.getEncoding() != null) {
			mailSender.setDefaultEncoding(mailProps.getEncoding().name());
		}
		return mailSender;
	}

	/*
	 * Multiple multi-line flat files read in line 1 of *EACH* file is skipped
	 * and assumed to be a header for column assignment
	 */
	@Bean
	public ItemReader<Records> reader(DelimitedLineTokenizer tokenizer,
			FieldSetMapper<Records> customFlatFileFieldSetMapper, AppProperties appProps, ApplicationContext appCtx)
					throws IOException {
		MultiResourceItemReader<Records> reader = new MultiResourceItemReader<Records>();
		FlatFileItemReader<Records> flatFileReader = new FlatFileItemReader<Records>();
		DefaultLineMapper<Records> lineMapper = new DefaultLineMapper<Records>();
		// Searchs for files to read based on values in Property file
		Resource[] resources = appCtx.getResources("file:" + appProps.getResourceAddress() + appProps.getInputAddress()
				+ DEFAULT_SEPARATOR + "*" + appProps.getExt());
		reader.setResources(resources);
		// Assign configured tokenizer ***Default is (,)*** to line mapper
		lineMapper.setLineTokenizer(tokenizer);
		// FieldSet Maper for records assigned to linemapper
		lineMapper.setFieldSetMapper(customFlatFileFieldSetMapper);
		// Set configured linemapper to flat file reader
		flatFileReader.setLineMapper(lineMapper);
		// Reader uses flatFileReader to read each file
		reader.setDelegate(flatFileReader);
		log.debug("Reader Built");
		return reader;
	}

	@Profile("!Test")
	@Bean
	public Job intakeRecord(JobBuilderFactory jobs, @Qualifier("recordStep") Step recordStep,
			@Qualifier("archiveStep") Step archiveStep, @Qualifier("reportStep") Step reportStep,
			@Qualifier("purgeStep") Step purgeStep, @Qualifier("emailStep") Step emailStep) {
		return jobs.get("IntakeRecords").flow(recordStep).next(archiveStep).next(purgeStep).next(reportStep)
				.next(emailStep).end().build();
	}

	@Bean
	public Step recordStep(StepBuilderFactory steps, ItemReader<Records> reader,
			ItemProcessor<Records, Records> processor, ItemWriter<Records> writer) {
		return steps.get("recordStep").<Records, Records> chunk(1).reader(reader).processor(processor).writer(writer)
				.build();
	}

	@Bean
	public Step archiveStep(StepBuilderFactory steps, ArchiveTasklet archiveTasklet) {
		return steps.get("archiveStep").tasklet(archiveTasklet).build();
	}

	@Bean
	public Step purgeStep(StepBuilderFactory steps, StagingPurgeTasklet purgeTasklet) {
		return steps.get("purgeStep").tasklet(purgeTasklet).build();
	}

	@Bean
	public Step reportStep(StepBuilderFactory steps, ReportGenerator reportGen) {
		return steps.get("reportStep").tasklet(reportGen).build();
	}

	@Bean
	public Step emailStep(StepBuilderFactory steps, EmailTasklet emailTasklet) {
		return steps.get("emailStep").tasklet(emailTasklet).build();
	}

}
