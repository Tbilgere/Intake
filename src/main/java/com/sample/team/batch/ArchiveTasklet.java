package com.sample.team.batch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import com.sample.team.config.AppProperties;

public class ArchiveTasklet implements Tasklet {

	private final static Logger log = LoggerFactory.getLogger(ArchiveTasklet.class);

	@Autowired
	private AppProperties appProps;
	
	@Autowired
	private EmailTasklet emailTask;
	
	@Autowired
	private ReportGenerator reportGen;

	// Current Time for archive filename
	private String currentDateTime = new SimpleDateFormat("__yyyyMMdd-HHmmss").format(new Date());

	// name of archive file with current date/time inserted
	private String archiveFileName = appProps.getArchiveAddress().replace("##DATE##", currentDateTime);

	// dir of archive files
	private String archiveFullAddress = appProps.getResourceAddress() + appProps.getArchiveAddress();

	// dir of input files to be archived
	private String inputDir = appProps.getResourceAddress() + appProps.getInputAddress();

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		archiveFiles(archiveFullAddress + archiveFileName, generateFileList(new File(inputDir), appProps.getExt()));
		return RepeatStatus.FINISHED;
	}

	public void archiveFiles(String outputFile, List<File> fileList) {

		// 16kb read buffer
		byte[] buffer = new byte[1024 * 16];

		// Look for blank files to weed out of archive
		List<Integer> removeList = new ArrayList<Integer>();
		for (File file : fileList) {
			if (file.length() == 0) {
				int index = fileList.indexOf(file);
				removeList.add(index);
				log.warn("Blank file found, Skipping file in archive");
			}
		}
		// Remove any found blank files
		for (int removeIndex : removeList) {
			fileList.remove(removeIndex);
		}

		// If there are files left in the list after removing blanks archive
		// them
		if (fileList.size() > 0) {
			try {
				log.info("Archiving to {}", outputFile);
				// Stream to destination file
				FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
				ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);

				for (File file : fileList) {
					String fileName = file.getName();
					log.info("Adding file to archive: {}", fileName);
					// Create new file in zip archive
					ZipEntry zipEntry = new ZipEntry(fileName);
					zipOutputStream.putNextEntry(zipEntry);

					// opens current file to be read into zip archive file
					FileInputStream fileInputStream = new FileInputStream(file);

					// Writes file to zip archive entry in buffer size pieces
					int length;
					while ((length = fileInputStream.read(buffer)) > 0) {
						zipOutputStream.write(buffer, 0, length);
					}
					fileInputStream.close();
					log.info("Archiving completed on {} purging from input dir", fileName);
					// Once file has been archived it can be removed from work
					// dir
					file.delete();
				}
				// Close destination file streams
				zipOutputStream.closeEntry();
				zipOutputStream.close();

				log.info("Archiving completed");
			} catch (Exception e) {
				log.error("Archive error: {}", e.getMessage());
				reportGen.addError("Archive error", e.getMessage());
			}
		} else {
			log.info("No Files to archive in input dir skipping step");
			emailTask.setOutputSkipped(true);
		}
	}

	public List<File> generateFileList(File node, final String ext) {
		// Uses FilenameFilter to only add files ending with correct ext to list
		ArrayList<File> returnValue = new ArrayList<File>();
		returnValue.addAll(Arrays.asList(node.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return (name.endsWith(ext));
			}
		})));

		return returnValue;
	}

}
