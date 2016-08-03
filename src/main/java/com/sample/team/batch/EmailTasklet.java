package com.sample.team.batch;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.sample.team.config.AppProperties;
import com.sample.team.config.MailProperties;

public class EmailTasklet implements Tasklet {

	private final static Logger log = LoggerFactory.getLogger(EmailTasklet.class);

	private final static String SYSTEM_LINE_SEPARATOR = System.getProperty("line.separator");

	private String lineSeparator = SYSTEM_LINE_SEPARATOR;

	@Autowired
	private AppProperties appProps;

	private String activeProfile = appProps.getActiveProfile();

	private MailProperties mailProps;
	private JavaMailSender javaMailSender;
	private List<String> reportList;
	private boolean outputSkipped = false;

	public EmailTasklet(MailProperties mailProps, JavaMailSender javaMailSender) {
		this.mailProps = mailProps;
		this.javaMailSender = javaMailSender;
		this.reportList = new ArrayList<>();
	}

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		if (outputSkipped) {
			sendSkippedEmail();
			log.warn("Skip warning email sent!");
		} else {
			sendStandardEmail();
		}
		return RepeatStatus.FINISHED;
	}

	private void sendSkippedEmail() {
		String skipContent = "Intake was run in " + activeProfile + " and no input data could be found" + lineSeparator
				+ lineSeparator;
		String skipSubject = "ACTION REQUIRED: Inake in " + activeProfile;
		sendEmail(skipSubject, skipContent);
	}

	private void sendStandardEmail() {
		String standardContent = mailProps.getContent().replace("{REPORT_HERE}", getReportString());
		String standardSubject = mailProps.getSubject() + "in" + activeProfile;
		sendEmail(standardSubject, standardContent);
	}

	private void sendEmail(String subject, String content) {
		log.debug("sendEmail - Start");

		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setTo(mailProps.getRecipients().split(";"));
		mailMessage.setReplyTo(mailProps.getReplyTo());
		mailMessage.setFrom(mailProps.getFrom());
		mailMessage.setSubject(subject);
		mailMessage.setText(content);

		try {
			javaMailSender.send(mailMessage);
			log.info("Email sent");
		} catch (MailException e) {
			log.error("Error in sendEmail {}", e.getMessage());
		}
		log.debug("sendEmail - End");
	}

	public String getReportString() {
		String returnValue = "";
		for (String report : this.reportList) {
			returnValue += report + lineSeparator;
		}
		return returnValue;
	}

	public String getLineSeparator() {
		return lineSeparator;
	}

	public void setLineSeparator(String lineSeparator) {
		this.lineSeparator = lineSeparator;
	}

	public List<String> getReportList() {
		return reportList;
	}

	public void setReportList(List<String> reportList) {
		this.reportList = reportList;
	}

	public void addReport(String reportAddress) {
		this.reportList.add(reportAddress);
	}

	public boolean isOutputSkipped() {
		return outputSkipped;
	}

	public void setOutputSkipped(boolean outputSkipped) {
		this.outputSkipped = outputSkipped;
	}

}
