package com.dragoonart.subtitle.finder.ui.usersettings;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dragoonart.subtitle.finder.ui.StartUI;

public enum PreferencesManager {
	INSTANCE();
	public static Path prefsRootDir;
	public static Path prefLocFile;
	private Set<Path> loadedPaths = new HashSet<Path>();
	private static final Logger logger = LoggerFactory.getLogger(PreferencesManager.class);
	
	PreferencesManager() {
		init();
	}

	protected void init() {
		prefsRootDir = Paths.get(System.getProperty("user.home") + "/.subFinder");
		prefLocFile = prefsRootDir.resolve("locations.prop");
		if (!Files.exists(prefsRootDir)) {
			try {
				Files.createDirectory(prefsRootDir);
				Files.createFile(prefLocFile);
			} catch (Exception e) {
				logger.error("Unable to create dir/file", e);
			}
		} else {
			loadedPaths.addAll(getLocationPaths());
		}
	}
	
	public boolean hasLocations() {
		return !loadedPaths.isEmpty();
	}
	public List<Path> getLocationPaths() {
		List<Path> paths = new ArrayList<Path>();
		if (Files.exists(prefLocFile)) {
			try {
				Files.lines(prefLocFile).forEach(e -> {
					try {
						paths.add(Paths.get(e));
					} catch (Exception ex) {
						logger.error("unable to parse: " + e,ex);
					}
				});
			} catch (IOException e) {
				logger.error("unable to parse: " + prefLocFile, e);
			}
		}
		return paths;
	}
	
	public void removeLocationPath(Path locPath) throws IOException {
		List<String> filtered = Files.lines(prefLocFile).filter(x -> x.equals(locPath))
				.collect(Collectors.toList());
		Files.write(prefLocFile, filtered);
	}

	public void addLocationPath(Path locPath) {
		try (PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(prefLocFile.toFile(), true), "UTF-8")))) {
		    out.println(locPath.toAbsolutePath().toString());
		} catch (IOException e) {
		    //exception handling left as an exercise for the reader
		}
	}
}
