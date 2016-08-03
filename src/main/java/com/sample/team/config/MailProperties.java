package com.sample.team.config;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class MailProperties {

	private final static Logger log = LoggerFactory.getLogger(MailProperties.class);
	private final static Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

	@Autowired
	AppProperties appProps;

	// SMTP server host
	private final String host;
	// SMTP server port
	private final int port;
	// Login username for SMTP server
	private final String username;
	// Login password for SMTP server
	private final String password;
	// Protocol used by SMTP server
	private final String protocol;

	private final boolean auth;
	private final boolean starttlsEnable;
	private final String recipients;
	private final String replyTo;
	private final String from;
	private final String subject;
	private final String content;

	@Autowired
	public MailProperties() {
		this.host = appProps.getMailHost();
		this.port = appProps.getMailPort();
		this.username = appProps.getMailUsername();
		this.password = appProps.getMailPassword();
		this.protocol = appProps.getMailProtocol();
		this.auth = appProps.isMailAuth();
		this.starttlsEnable = appProps.isMailStarttlsEnable();
		this.recipients = appProps.getMailRecipients();
		this.replyTo = appProps.getMailReplyTo();
		this.from = appProps.getMailFrom();
		this.subject = appProps.getMailSubject();
		this.content = appProps.getMailContent();
	}

	// Default MimeMessage encoding
	private Charset encoding = DEFAULT_CHARSET;

	// Additional JavaMail session properties
	private Map<String, String> properties = new HashMap<String, String>();

	// Test that the mail server is available on startup
	private boolean testConnection;


	public static Charset getDefaultCharset() {
		return DEFAULT_CHARSET;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getProtocol() {
		return protocol;
	}

	public boolean isAuth() {
		return auth;
	}

	public boolean isStarttlsEnable() {
		return starttlsEnable;
	}

	public String getRecipients() {
		return recipients;
	}

	public String getReplyTo() {
		return replyTo;
	}

	public String getFrom() {
		return from;
	}

	public String getSubject() {
		return subject;
	}

	public String getContent() {
		return content;
	}

	public AppProperties getAppProps() {
		return appProps;
	}

	public Charset getEncoding() {
		return encoding;
	}

	public void setEncoding(Charset encoding) {
		this.encoding = encoding;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	public boolean isTestConnection() {
		return testConnection;
	}

	public void setTestConnection(boolean testConnection) {
		this.testConnection = testConnection;
	}

	@PostConstruct
	public void writeConfigurationToLog() {
		log.info("Starting mail applicaiton");
	}

	@Override
	public String toString() {
		return "MailProperties [host=" + host + ", port=" + port + ", username=" + username + ", password=" + password
				+ ", protocol=" + protocol + ", auth=" + auth + ", starttlsEnable=" + starttlsEnable + ", recipients="
				+ recipients + ", replyTo=" + replyTo + ", from=" + from + ", subject=" + subject + ", content="
				+ content + ", defaultEncoding=" + encoding + ", properties=" + properties + ", testConnection="
				+ testConnection + "]";
	}
}