package com.share.srt.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

public class FileHelper {

	private static final Logger logger = LoggerFactory.getLogger(FileHelper.class);
	
    private static final String WORK_BASE_DIR = new File(System.getProperty("user.home")).getAbsolutePath() + "/temp_work/";
	private static final String TEMP_FOLDER = WORK_BASE_DIR + "temp/";

	public static void writeToFile(String fileName, String content) {
		Path path = Paths.get(fileName);

		try (BufferedWriter writer = Files.newBufferedWriter(path)) {
			writer.write(content);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static File saveStringToFile(String base64ContentString, String fileName) {
		String newFileName = fileName + MessageCode.TXT;
		writeToFile(newFileName, base64ContentString);
		return new File(newFileName);
	}

	public static void downloadFromS3ToLocal(String preassignedS3Url, String localFileName) {
		
		try {
			URL url = new URL(preassignedS3Url);
		   InputStream in = url.openStream();
		   Files.copy(in, Paths.get(localFileName), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
		   logger.error("IO Exception was caught in downloadFromS3ToLocal.", e);
		}
	}
	
	public static String readFileToString(String filePath) {
		StringBuilder contentBuilder = new StringBuilder();
		try (Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8)) {
			stream.forEach(s -> contentBuilder.append(s).append("\n"));
		} catch (IOException e) {
			logger.error("IOException in readFileToString.", e);
		}
		String dataString = contentBuilder.toString().trim();
		if (dataString.startsWith(MessageCode.SQUARE_BRACKET)) {
			dataString = dataString.substring(1, dataString.length()-1);
		}
		return dataString;
	}



	public static void deleteFilesWithExtension(String path, String extension) {
		File file = new File(path);
		File[] fileList = file.listFiles((d, f) -> f.toLowerCase().endsWith(extension));

		long cutOff = System.currentTimeMillis() - 604800000; // 7 days
		assert fileList != null;
		for (File f : fileList) {
			if (f.lastModified() < cutOff) {
				if (!f.delete()) {
					logger.info("Not able to delete file: " + f.getAbsolutePath());
				} else {
					logger.info("The successfully deleted file is: " + f.getAbsolutePath());
				}
			}
		}
	}
	
	public static void deleteFilesOldThen7Days(String path) {
		File file = new File(path);
		File[] fileList = file.listFiles();

		long cutOff = System.currentTimeMillis() - 604800000; // 7 days
		assert fileList != null;
		for (File f : fileList) {
			if (f.lastModified() < cutOff) {
				if (!f.delete()) {
					logger.info("Not able to delete file: " + f.getAbsolutePath());
				} else {
					logger.info("The successfully deleted file is: " + f.getAbsolutePath());
				}
			}
		}
	}	
	
	public static void deleteFilesOldThen7DaysInTempFolder() {
		File file = new File(TEMP_FOLDER);
		File[] fileList = file.listFiles();

		long cutOff = System.currentTimeMillis() - 604800000; // 7 days
		assert fileList != null;
		for (File f : fileList) {
			if (f.lastModified() < cutOff) {
				if (!f.delete()) {
					logger.info("Not able to delete file: " + f.getAbsolutePath());
				} else {
					logger.info("The successfully deleted file is: " + f.getAbsolutePath());
				}
			}
		}
	}

	public static void writeToFileUtf8(String fileName, String content) {
		Writer w;
		try {
			w = new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8");
			w.append(content);
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
