package com.dragoonart.subtitle.finder;

import java.nio.file.Path;
import java.nio.file.Paths;

public class FileLocations {
	public static final Path SETTINGS_DIRECTORY = Paths.get(System.getProperty("user.home"),"subFinder");
	public static final Path MOVIE_META_DIRECTORY = SETTINGS_DIRECTORY.resolve("videoMeta");
}
