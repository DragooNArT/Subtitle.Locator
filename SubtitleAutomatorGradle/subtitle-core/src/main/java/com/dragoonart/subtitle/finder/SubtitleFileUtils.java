package com.dragoonart.subtitle.finder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;

import org.apache.commons.lang.StringUtils;
import org.zeroturnaround.zip.ZipEntryCallback;
import org.zeroturnaround.zip.ZipUtil;

import com.dragoonart.subtitle.finder.beans.ParsedFileName;
import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;

public class SubtitleFileUtils {

	public static Map<String, Path> unpackSubs(Path pathToZip, Path targetDir) {
		Map<String, Path> result = new HashMap<String, Path>();
		if (pathToZip != null && Files.exists(pathToZip)) {
			if (pathToZip.getFileName().toString().endsWith(".zip")) {
				// zip lib attempt
				try {
					extractZip(pathToZip, targetDir, result);
				} catch (Exception e) {
					System.out.println("failed apache extract for: " + pathToZip);
					e.printStackTrace();
				}
			} else if (pathToZip.getFileName().toString().endsWith(".rar")) {
				try {
					extractRrar(pathToZip, targetDir, result);
				} catch (Exception e2) {
					System.out.println("failed apache extract for: " + pathToZip);
					e2.printStackTrace();
				}
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
		try (Archive arch = new Archive(pathToZip.toFile())) {
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
					if (!Files.exists(newFilePath)) {
						Files.createDirectories(newFilePath.getParent());
						Files.copy(arch.getInputStream(fh), newFilePath);
					}
					try {
						result.put(fileName, newFilePath);
					} catch (Throwable ex) {
						System.out.println("Bad fileName: " + fileName);
						ex.printStackTrace();
					}
				}

			}
		} catch (RarException | IOException e1) {
			e1.printStackTrace();
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
						System.out.println("Bad fileName: " + fileName);
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
			valid = valid || (c == '_') || (c == '-') || (c == '.') || (c == '#')
					|| (false && ((c == '/') || (c == '\\')));

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
		return Paths.get("./testFiles/" + pfn.getOrigName() + "/archives");
	}
}
