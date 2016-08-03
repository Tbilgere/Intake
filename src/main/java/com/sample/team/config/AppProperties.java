package com.sample.team.config;

import org.springframework.beans.factory.annotation.Value;

public class AppProperties {

	// Active spring profile
	@Value("${spring.profiles.active}")
	private String activeProfile;

	// SQL Statement to read records from StagingTable
	@Value("${SQL.Staging.Read}")
	private String stagingRead;

	// SQL Statement to update processed field in staging
	@Value("${SQL.Staging.ProcessedUpdate}")
	private String processUpdate;

	// SQL statement to write to staging table
	@Value("${SQL.Staging.Write}")
	private String stagingWrite;

	// SQL statement to purge old records
	@Value("${SQL.Staging.PurgeStatment}")
	private String purgeStatement;

	// Work dir location
	@Value("${resourceAddress}")
	private String resourceAddress;

	// Location of input flat files
	@Value("${inputAddress}")
	private String inputAddress;

	// Flat file extention type
	@Value("${ext}")
	private String ext;

	// Expected date formating
	@Value("${dateFormat}")
	private String dateFormat;

	// Location of archive files
	@Value("${archiveAddress}")
	private String archiveAddress;

	// Archive file name *** ##DATE## used to be replaced with current date/time
	@Value("${archiveFilename}")
	private String archiveFileName;

	// Location of report file
	@Value("${reportAddress}")
	private String reportAddress;

	// Report file name ** ##DATE## used to be replaed with current data/time
	@Value("${reportFilename}")
	private String reportFilename;

	// SMTP server host
	@Value("${mail.host}")
	private String mailHost;

	// SMTP server port
	@Value("${mail.port}")
	private int mailPort;

	// SMTP login username
	@Value("${mail.username")
	private String mailUsername;

	// SMTP login password
	@Value("${mail.password}")
	private String mailPassword;

	// SMTP protocol used
	@Value("${mail.protocol")
	private String mailProtocol;

	// SMTP auth
	@Value("${mail.smtp.auth}")
	private boolean mailAuth;

	// SMTP starttls enable
	@Value("${mail.smtp.starttls.enable}")
	private boolean mailStarttlsEnable;

	// SMTP mail recipients
	@Value("${mail.recipients}")
	private String mailRecipients;

	// SMTP mail replyTo
	@Value("${mail.replyTo}")
	private String mailReplyTo;

	// SMTP mail sender
	@Value("${mail.from}")
	private String mailFrom;

	// SMTP mail subject line
	@Value("${mail.subject}")
	private String mailSubject;

	// SMTP mail body content
	@Value("${mail.content}")
	private String mailContent;

	// Getters

	public String getActiveProfile() {
		return activeProfile;
	}

	public String getStagingRead() {
		return stagingRead;
	}

	public String getProcessUpdate() {
		return processUpdate;
	}

	public String getStagingWrite() {
		return stagingWrite;
	}

	public String getPurgeStatement() {
		return purgeStatement;
	}

	public String getResourceAddress() {
		return resourceAddress;
	}

	public String getInputAddress() {
		return inputAddress;
	}

	public String getExt() {
		return ext;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public String getArchiveAddress() {
		return archiveAddress;
	}

	public String getArchiveFileName() {
		return archiveFileName;
	}

	public String getReportAddress() {
		return reportAddress;
	}

	public String getReportFilename() {
		return reportFilename;
	}

	public String getMailHost() {
		return mailHost;
	}

	public int getMailPort() {
		return mailPort;
	}

	public String getMailUsername() {
		return mailUsername;
	}

	public String getMailPassword() {
		return mailPassword;
	}

	public String getMailProtocol() {
		return mailProtocol;
	}

	public boolean isMailAuth() {
		return mailAuth;
	}

	public boolean isMailStarttlsEnable() {
		return mailStarttlsEnable;
	}

	public String getMailRecipients() {
		return mailRecipients;
	}

	public String getMailReplyTo() {
		return mailReplyTo;
	}

	public String getMailFrom() {
		return mailFrom;
	}

	public String getMailSubject() {
		return mailSubject;
	}

	public String getMailContent() {
		return mailContent;
	}

}