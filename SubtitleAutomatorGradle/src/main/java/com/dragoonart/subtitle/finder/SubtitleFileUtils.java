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
	
	public static Map<String, Path> unpackSubs(Path pathToZip) {
		Map<String, Path> result = new HashMap<String, Path>();
		try {
			ZipUtil.iterate(pathToZip.toFile(), new ZipEntryCallback() {

				@Override
				public void process(InputStream in, ZipEntry zipEntry) throws IOException {
					String fileName = zipEntry.getName();
					Path newFilePath = pathToZip.getParent().toAbsolutePath().resolve(fileName);
					if (isSubtitleEntry(fileName, newFilePath)) {
						Files.copy(in, newFilePath);
						result.put(fileName, newFilePath);
					}
				}
			});
		} catch (Exception e) {
			try (Archive arch = new Archive(pathToZip.toFile())) {
				FileHeader fh;
				while ((fh = arch.nextFileHeader()) != null) {
					String fileName = fh.getFileNameString();
					Path newFilePath = pathToZip.getParent().resolve(fh.getFileNameString());
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
		return result;
	}
	public static boolean isSubtitleEntry(String fileName, Path newFilePath) {
		return !Files.exists(newFilePath) && (fileName.endsWith(".srt") || fileName.endsWith(".sub"));
	}
}
