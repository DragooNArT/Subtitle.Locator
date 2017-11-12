package com.dragoonart.subtitle.finder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.zip.ZipEntryCallback;
import org.zeroturnaround.zip.ZipUtil;

import com.dragoonart.subtitle.finder.beans.ParsedFileName;

public class SubtitleFileUtils {

	private static final String PATH_TO_UNRAR = "C:\\Program Files\\WinRAR\\UnRAR.exe";
	private static final Logger logger = LoggerFactory.getLogger(SubtitleFileUtils.class);

	public static Map<String, Path> unpackSubs(Path pathToZip, Path targetDir) {
		Map<String, Path> result = new HashMap<String, Path>();
		if (pathToZip != null && Files.exists(pathToZip)) {
			try {
				if (pathToZip.getFileName().toString().endsWith(".zip")) {
					extractZip(pathToZip, targetDir, result);
				} else if (pathToZip.getFileName().toString().endsWith(".rar")) {
					extractRrar(pathToZip, targetDir, result);
				}
			} catch (Exception e) {
				logger.error("failed archive unpack for: " + pathToZip.getFileName().toString(), e);
				e.printStackTrace();
			}
		}
		return result;
	}

	public static boolean hasSubs(Path file) {
		// calculate if it already has subtitles
		String fileName = file.getFileName().toString();
		String srt = fileName.substring(0, fileName.lastIndexOf(".")) + ".srt";
		String sub = fileName.substring(0, fileName.lastIndexOf(".")) + ".sub";
		if (Files.exists(file.getParent().resolve(srt)) || Files.exists(file.getParent().resolve(sub))) {
			return true;
		}
		return false;
	}

	private static void extractRrar(Path pathToZip, Path targetDir, Map<String, Path> result) {
		BufferedReader in = null;
		Process child = null;
		StringBuilder responseData = new StringBuilder();
		if (!Files.exists(Paths.get(PATH_TO_UNRAR))) {
			logger.warn("Can't open rar file! couldn't find C:\\Program Files\\WinRAR\\UnRAR.exe");
			return;
		}
		try {
			ProcessBuilder ps = new ProcessBuilder(PATH_TO_UNRAR, "e", "-o+",
					"\"" + pathToZip.toAbsolutePath().toString() + "\"");
			ps.directory(targetDir.toAbsolutePath().toFile());
			// Execute command
			child = ps.start();

			// Get output stream to write from it
			in = new BufferedReader(new InputStreamReader(child.getInputStream(), "UTF-8"));
			String line = null;
			while ((line = in.readLine()) != null) {
				responseData.append(line).append("\n");
				line = line.replace("Extracting ", "");
				line = line.replace("Skipping ", "");
				line = line.replace(" OK ", "");
				line = line.indexOf('') > -1 ? line.substring(0, line.indexOf('')).trim() : line.trim();
				if (isSubtitleEntry(line) && Files.exists(targetDir.resolve(line))) {
					result.put(line, targetDir.resolve(line));
				}
			}
			if (result.isEmpty()) {
				logger.warn("Couldn't find any subs inside: " + pathToZip);
			}
		} catch (Exception e) {
			logger.error("unable to extract rar for : " + pathToZip.toString() + " output of unrar: "
					+ responseData.toString());
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// ignore
				}
			}
			if (child != null) {
				child.destroy();
			}
		}
	}

	private static void extractZip(Path pathToZip, Path targetDir, Map<String, Path> result) {

		ZipUtil.iterate(pathToZip.toFile(), new ZipEntryCallback() {

			@Override
			public void process(InputStream in, ZipEntry zipEntry) throws IOException {
				String fileName = zipEntry.getName();
				if (isSubtitleEntry(fileName)) {
					Path newFilePath = null;
					if (targetDir == null) {
						newFilePath = pathToZip.getParent().toAbsolutePath().resolve(fileName);
					} else {
						newFilePath = targetDir.toAbsolutePath().resolve(fileName);
					}
					if (!Files.exists(newFilePath)) {
						Files.createDirectories(newFilePath.getParent());
						Files.copy(in, newFilePath);
					}
					try {
						result.put(fileName, newFilePath);
					} catch (Throwable ex) {
						logger.error("Bad fileName: " + fileName, ex);
						ex.printStackTrace();
					}
				}
			}
		});
	}

	public static boolean isSubtitleEntry(String fileName) {
		return StringUtils.endsWithIgnoreCase(fileName, ".srt") || StringUtils.endsWithIgnoreCase(fileName, ".sub");
	}

	public static String toFileSystemSafeName(String name) {
		int size = name.length();
		StringBuffer rc = new StringBuffer(size * 2);
		for (int i = 0; i < size; i++) {
			char c = name.charAt(i);
			boolean valid = c >= 'a' && c <= 'z';
			valid = valid || (c >= 'A' && c <= 'Z');
			valid = valid || (c >= '0' && c <= '9');
			valid = valid || (c == '_') || (c == '-') || (c == '.') || (c == '#');

			if (valid) {
				rc.append(c);
			} else {
				// Encode the character using hex notation
				rc.append(' ');
			}
		}
		String result = rc.toString().trim();
		if (result.length() > 255) {
			result = result.substring(result.length() - 255, result.length());
		}
		return result;
	}

	public static Path getArchivesDir(ParsedFileName pfn) {
		return Paths.get(System.getProperty("user.home") + "/.subFinder/testFiles/" + pfn.getOrigName() + "/archives");
	}
}
