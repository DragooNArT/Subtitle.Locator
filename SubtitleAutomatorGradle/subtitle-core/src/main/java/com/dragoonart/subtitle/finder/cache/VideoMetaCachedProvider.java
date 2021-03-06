package com.dragoonart.subtitle.finder.cache;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.ws.rs.core.MultivaluedMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dragoonart.subtitle.finder.FileLocations;
import com.dragoonart.subtitle.finder.beans.VideoEntry;
import com.dragoonart.subtitle.finder.beans.videometa.VideoMetaBean;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public enum VideoMetaCachedProvider {
	INSTANCE();
	private static final String PROVIDER_URL = "http://www.omdbapi.com";
	private static final String TITLE_PARAMETER = "t";
	private static final String YEAR_PARAMETER = "y";
	private static final String API_KEY_PARAMETER = "apikey";

	private static final String API_KEY_VALUE = "595c66dd";

	private static final Logger logger = LoggerFactory.getLogger(VideoMetaCachedProvider.class);

	private ObjectMapper mapper = new ObjectMapper();

	private Client client = Client.create();

	private VideoMetaCachedProvider() {
		if (!Files.exists(FileLocations.MOVIE_META_DIRECTORY)) {
			try {
				Files.createDirectories(FileLocations.MOVIE_META_DIRECTORY);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	public synchronized VideoMetaBean getMovieData(VideoEntry ve) {
		VideoMetaBean vmb = checkCache(ve);
		if (vmb != null) {
			return vmb;
		}
		MultivaluedMap<String, String> params = new MultivaluedMapImpl();
		params.add(API_KEY_PARAMETER, API_KEY_VALUE);
		params.add("s", ve.getParsedFileName().getShowName());
		if (ve.getParsedFileName().hasYear()) {
			params.add(YEAR_PARAMETER, ve.getParsedFileName().getYear());
		}

		WebResource builder = client.resource(PROVIDER_URL).queryParams(params);

		ClientResponse response = null;

		try {
			response = builder.get(ClientResponse.class);
			String json = response.getEntity(String.class);
			if (json.contains("Movie not found!")) {
				vmb = VideoMetaBean.NOT_FOUND;
			} else {
				vmb = mapper.readValue(json, VideoMetaBean.class);
				addToCache(ve, vmb);
			}
			return vmb;
		} catch (Exception e) {
			logger.warn("unable to load video metadata for: " + ve.getFileName(), e);

		}
		logHasError(response);
		return null;
	}

	private void logHasError(ClientResponse response) {
		if (response != null && response.getStatus() != 200) {
			try {
				logger.warn("unable to load video metadata from site, error code: " + response.getStatus() + "\n "
						+ response.getEntity(String.class));
			} catch (Exception e) {
				logger.warn("unable to load video metadata from site and parse response, error code: "
						+ response.getStatus());
			}
		}
	}

	private void addToCache(VideoEntry ve, VideoMetaBean vmb) {
		Path pathToCachedEntry = FileLocations.MOVIE_META_DIRECTORY.resolve(ve.getAcceptableFileName());
		try {
			mapper.writeValue(pathToCachedEntry.toFile(), vmb);
		} catch (IOException e) {
			logger.warn("Unable to save video meta for: " + ve.getAcceptableFileName() + " to cache", e);
		}

	}

	private VideoMetaBean checkCache(VideoEntry ve) {
		Path pathToCachedEntry = FileLocations.MOVIE_META_DIRECTORY.resolve(ve.getAcceptableFileName());
		if (Files.exists(pathToCachedEntry)) {
			try {
				return mapper.readValue(pathToCachedEntry.toFile(), VideoMetaBean.class);
			} catch (Exception e) {
				logger.warn("Unable to load video meta for: " + ve.getAcceptableFileName() + " from cache", e);
			}
		}
		return null;
	}
}
