package com.sample.team.config;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

@Configuration
public class DataConfig {

	private final static Logger log = LoggerFactory.getLogger(DataConfig.class);

	@Autowired
	private ApplicationContext appCtx;

	// Script to build HSQL DB for backup meta-data Tables
	@Value("${db.jobRepDB.initScript}")
	private String backupInitScript;

	// Primary output DataSource
	@Primary
	@Profile("!Test")
	@Bean(name = "outputDB")
	@ConfigurationProperties(prefix = "db.outputDB")
	public DataSource outputDB() {
		return DataSourceBuilder.create().build();
	}

	// Backup in-memory JobRepository 'Meta-data' Tables
	@Bean(name = "jobRepDB")
	@ConfigurationProperties(prefix = "db.jobRepDB")
	public DataSource jobRepDB() {
		return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.HSQL).addScript(backupInitScript).build();
	}

	/*
	 * Checks for primary meta-data tables, defaults to transaction-less mode if
	 * tables are not found
	 */
	@Bean
	public BatchConfigurer configurer() {
		DataSource jobRepDB = null;
		try {
			//JobRep Meta tables
			jobRepDB = (DataSource) appCtx.getBean("jobRepDB");
			return new DefaultBatchConfigurer(jobRepDB);
		} catch(Exception e) {
			log.warn("Could not connect to in-mem meta-data tables. Defaulting to transaction less mode");
			return new DefaultBatchConfigurer(null);
		}
	}
}
