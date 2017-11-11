package com.dragoonart.subtitle.finder;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

import de.innosystec.unrar.Archive;
import de.innosystec.unrar.NativeStorage;
import de.innosystec.unrar.rarfile.FileHeader;

public class SubtitleFileUtils {
	
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
		try (Archive arch = new Archive(new NativeStorage(pathToZip.toFile()))) {
			FileHeader fh;
			while ((fh = arch.nextFileHeader()) != null) {
				String fileName = fh.getFileNameString();
				if (isSubtitleEntry(fileName)) {
					Path newFilePath;
					if (targetDir == null) {
						newFilePath = pathToZip.getParent().resolve(fh.getFileNameString());
					} else {
						newFilePath = targetDir.resolve(fh.getFileNameString());
					}
					FileOutputStream os = null;
					try {
						if (!Files.exists(newFilePath)) {
							Files.createDirectories(newFilePath.getParent());
							os = new FileOutputStream(newFilePath.toFile());
							arch.extractFile(fh, os);
						}
						result.put(fileName, newFilePath);
					} catch (Throwable ex) {
						logger.warn("Bad RAR fileName: " + fileName, ex);
					} finally {
						if(os != null) {
							os.close();
						}
					}
				}

			}
		} catch (Exception e1) {
			logger.warn("Bad rar archive: " + pathToZip.toFile(), e1);
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
						logger.error("Bad fileName: " + fileName,ex);
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
