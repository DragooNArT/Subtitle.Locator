package com.dragoonart.subtitle.finder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;

import org.zeroturnaround.zip.ZipEntryCallback;
import org.zeroturnaround.zip.ZipUtil;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;

public class SubtitleFileUtils {

	public static Map<String, Path> unpackSubs(Path pathToZip, Path targetDir) {
		Map<String, Path> result = new HashMap<String, Path>();
		try {
			extractZip(pathToZip, targetDir, result);
		} catch (Exception e) {
			extractRrar(pathToZip, targetDir, result, e);

		}
		return result;
	}

	private static void extractRrar(Path pathToZip, Path targetDir, Map<String, Path> result, Exception e) {
		try (Archive arch = new Archive(pathToZip.toFile())) {
			FileHeader fh;
			while ((fh = arch.nextFileHeader()) != null) {
				String fileName = fh.getFileNameString();
				Path newFilePath;
				if(targetDir == null) {
				newFilePath = pathToZip.getParent().resolve(fh.getFileNameString());
				} else {
				newFilePath = targetDir.resolve(fh.getFileNameString());
				}
				if (isSubtitleEntry(fileName, newFilePath)) {
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
		} catch (RarException | IOException e1) {
			e.printStackTrace();
			e1.printStackTrace();
		}
	}

	private static void extractZip(Path pathToZip, Path targetDir, Map<String, Path> result) {
		ZipUtil.iterate(pathToZip.toFile(), new ZipEntryCallback() {

			@Override
			public void process(InputStream in, ZipEntry zipEntry) throws IOException {
				String fileName = zipEntry.getName();
				Path newFilePath = null;
				if (targetDir == null) {
					newFilePath = pathToZip.getParent().toAbsolutePath().resolve(fileName);
				} else {
					newFilePath = targetDir.toAbsolutePath().resolve(fileName);
				}
				if (isSubtitleEntry(fileName, newFilePath)) {
					Files.copy(in, newFilePath);
					result.put(fileName, newFilePath);
				}
			}
		});
	}

	public static Map<? extends String, ? extends Path> unpackSubs(Path pathToSubtitle) {
		return unpackSubs(pathToSubtitle, null);
	}

	public static boolean isSubtitleEntry(String fileName, Path newFilePath) {
		return !Files.exists(newFilePath) && (fileName.endsWith(".srt") || fileName.endsWith(".sub"));
	}

}
