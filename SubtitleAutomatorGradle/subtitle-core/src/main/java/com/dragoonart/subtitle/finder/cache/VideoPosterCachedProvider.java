package com.dragoonart.subtitle.finder.cache;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dragoonart.subtitle.finder.FileLocations;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

public enum VideoPosterCachedProvider {
	INSTANCE();
	private static final Logger logger = LoggerFactory.getLogger(VideoPosterCachedProvider.class);
	public static final Path IMAGES_STORAGE_PATH = FileLocations.SETTINGS_DIRECTORY.resolve("movieImgs");
	private Client client = null;

	private String getImageName(String url) {
		return url.substring(url.lastIndexOf("/") + 1, url.length());

	}

	private String getLocalImage(String url) {
		if(!Files.exists(IMAGES_STORAGE_PATH)) {
			try {
				Files.createDirectories(IMAGES_STORAGE_PATH);
			} catch (IOException e) {
				logger.error("unable to create directories from path: "+IMAGES_STORAGE_PATH, e);
			}
		}
		Path pathToImg = IMAGES_STORAGE_PATH.resolve(getImageName(url));
		return Files.exists(pathToImg) ? pathToImg.toUri().toString() : null;
	}

	public String getLocalImagePath(String url) throws Exception {
		if (StringUtils.isEmpty(url)) {
			return null;
		}
		try {
			String imgPath = getLocalImage(url);
			if (imgPath == null) {
				if(client == null) {
					client =  Client.create();
				}
				ClientResponse resp = client.resource(url).get(ClientResponse.class);
				byte[] strContent = resp.getEntity(byte[].class);
				Path toImage = IMAGES_STORAGE_PATH.resolve(getImageName(url));
				Files.write(toImage, strContent);
				imgPath = toImage.toUri().toString();
			}
			return imgPath;
		} catch (Exception e) {
			logger.warn("unable to store image for URL: " + url);
			throw e;
		}
	}
}
