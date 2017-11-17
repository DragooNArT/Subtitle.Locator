package com.dragoonart.subtitle.finder.onlineDB;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.ws.rs.core.MultivaluedMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dragoonart.subtitle.finder.FileLocations;
import com.dragoonart.subtitle.finder.beans.VideoEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public enum MovieDataProvider {
	INSTANCE();
	private static final String PROVIDER_URL = "http://www.omdbapi.com";
	private static final String TITLE_PARAMETER = "t";
	private static final String YEAR_PARAMETER = "y";
	private static final String API_KEY_PARAMETER = "apikey";

	private static final String API_KEY_VALUE = "595c66dd";

	private static final Logger logger = LoggerFactory.getLogger(MovieDataProvider.class);

	private ObjectMapper mapper = new ObjectMapper();

	private Client client = Client.create();

	private MovieDataProvider() {
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
		if(vmb != null) {
			return vmb;
		}
		MultivaluedMap<String, String> params = new MultivaluedMapImpl();
		params.add(API_KEY_PARAMETER, API_KEY_VALUE);
		params.add(TITLE_PARAMETER, ve.getParsedFilename().getShowName());
		if (ve.getParsedFilename().hasYear()) {
			params.add(YEAR_PARAMETER, ve.getParsedFilename().getYear());
		}
	
		WebResource builder = client.resource(PROVIDER_URL).queryParams(params);
		
		ClientResponse response = builder.get(ClientResponse.class);
		try {
			String json = response.getEntity(String.class);
			if(json.contains("Movie not found!")) {
				vmb = VideoMetaBean.NOT_FOUND;
			} else {
				vmb = mapper.readValue(json, VideoMetaBean.class);
				addToCache(ve,vmb);
			}
			
		} catch (Exception e) {
			logger.warn("unable to load video metadata for: " + ve.getFileName(), e);
		}
		return vmb;
	}

	private void addToCache(VideoEntry ve, VideoMetaBean vmb) {
		Path pathToCachedEntry = FileLocations.MOVIE_META_DIRECTORY.resolve(ve.getAcceptableFileName());
		try {
			mapper.writeValue(pathToCachedEntry.toFile(), vmb);
		} catch (IOException e) {
			logger.warn("Unable to save video meta for: "+ve.getAcceptableFileName()+" to cache", e);
		}
		
	}

	private VideoMetaBean checkCache(VideoEntry ve) {
		Path pathToCachedEntry = FileLocations.MOVIE_META_DIRECTORY.resolve(ve.getAcceptableFileName());
		if(Files.exists(pathToCachedEntry)) {
			try {
				return mapper.readValue(pathToCachedEntry.toFile(), VideoMetaBean.class);
			} catch (Exception e) {
				logger.warn("Unable to load video meta for: "+ve.getAcceptableFileName()+" from cache", e);
			}
		}
		return null;
	}
}
