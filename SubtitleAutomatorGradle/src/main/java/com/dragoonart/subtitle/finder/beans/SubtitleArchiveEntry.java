package com.dragoonart.subtitle.finder.beans;

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

public class SubtitleArchiveEntry {

	private Path pathToSubtitle;

	private String link;

	private String origName;

	private Map<String, Path> subtitleEntries = new HashMap<String, Path>();

	public String getOrigName() {
		return origName;
	}

	private ParsedFileName parsedFileName;

	public SubtitleArchiveEntry(String origName, String link, Path pathToSubtitle) {
		this.pathToSubtitle = pathToSubtitle;
		this.origName = origName;
		this.link = link;
	}

	public String getLink() {
		return link;
	}

	public Map<String, Path> getSubtitleEntries() {
		if (subtitleEntries.isEmpty() && Files.exists(pathToSubtitle)) {
			try {
				subtitleEntries.putAll(unpackSubs());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return subtitleEntries;
	}
	
	private Map<String, Path> unpackSubs() {
		Map<String, Path> result = new HashMap<String,Path>();
		try {
			ZipUtil.iterate(pathToSubtitle.toFile(), new ZipEntryCallback() {

				@Override
				public void process(InputStream in, ZipEntry zipEntry) throws IOException {
					String fileName = zipEntry.getName();
					Path newFilePath = pathToSubtitle.getParent().toAbsolutePath().resolve(fileName);
					if (acceptSubtitleEntry(fileName, newFilePath)) {
						Files.copy(in, newFilePath);
						result.put(fileName, newFilePath);
					}
				}
			});
		} catch (Exception e) {
			try (Archive arch = new Archive(pathToSubtitle.toFile())) {
				FileHeader fh;
				while ((fh = arch.nextFileHeader()) != null) {
					String fileName = fh.getFileNameString();
					Path newFilePath = pathToSubtitle.getParent().resolve(fh.getFileNameString());
					if (acceptSubtitleEntry(fileName, newFilePath)) {
						Files.createDirectories(newFilePath.getParent());
						Files.copy(arch.getInputStream(fh), newFilePath);
					}
					try {
					result.put(fileName, newFilePath);
					} catch(Throwable ex) {
						System.out.println("Bad fileName: "+fileName);
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

	private boolean acceptSubtitleEntry(String fileName, Path newFilePath) {
		return !Files.exists(newFilePath) && (fileName.endsWith(".srt") || fileName.endsWith(".sub"));
	}

	public String getSubtitleName() {
		return pathToSubtitle.getFileName().toString();
	}
}
