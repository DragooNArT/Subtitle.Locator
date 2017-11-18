package com.dragoonart.subtitle.finder.web;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;

import com.dragoonart.subtitle.finder.SubtitleFileUtils;
import com.dragoonart.subtitle.finder.beans.ParsedFileName;
import com.dragoonart.subtitle.finder.beans.SubtitleArchiveEntry;
import com.dragoonart.subtitle.finder.beans.VideoEntry;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.header.ContentDisposition;

public abstract class AbstractSubtitleService {
	
	protected Client client;
	protected Logger logger;
	public AbstractSubtitleService() {
		init();
	}

	/**
	 * Get subtitle archives for the specified parsed file name(video name)
	 * 
	 * @param ve
	 *            - parsed file name(video name)
	 * @return - list of subtitle archive objects
	 */
	public Set<SubtitleArchiveEntry> getSubtitles(VideoEntry ve) {
		String searchWord = getSearchKeyword(ve.getParsedFileName());
		logger.trace("Looking for \"" + searchWord + "\" in site: " + getServiceProvider().getBaseUrl());
		WebResource.Builder builder = client.resource(getServiceProvider().getSearchUrl()).getRequestBuilder();
		ClientResponse resp = null;
		try {
		resp = builder
				.entity(getFormData(ve.getParsedFileName(), builder), MediaType.APPLICATION_FORM_URLENCODED)
				.post(ClientResponse.class);
		return getSubtitleArchives(Jsoup.parse(resp.getEntity(String.class)),
				ve.getParsedFileName());
		} catch (Exception e) {
			logger.error("Failed to get subtitle archives from: "+getServiceProvider().getBaseUrl()+" for video: "+ve.getFileName(), e);
		} finally {
			if(resp != null && resp.hasEntity()) {
				resp.close();
			}
		}
		// TODO don't log already downloaded subs
		// do the work
		

		// for (SubtitleArchiveEntry zip : subZips) {
		// System.out.println(zip.getSubtitleName());
		// }

		return Collections.emptySet();
	}

	/**
	 * 
	 * @return - returns the subtitle site meta-info object
	 */
	public abstract SubtitleProvider getServiceProvider();

	/**
	 * Returns a keyword for the specific service provider(sub site), based on the
	 * properties of the ParsedFileName(video file name)
	 * 
	 * @param pfn
	 *            - Parsed video file name
	 * @return - string constructed from the parsed video file name properties
	 */
	public String getSearchKeyword(ParsedFileName pfn) {
		StringBuilder sb = new StringBuilder();

		sb.append(pfn.getShowName()).append(" ");
		if (pfn.isEpisodic()) {
			sb.append(pfn.getSeason()).append(" ").append(pfn.getEpisode());
		}
		return sb.toString();
	}

	protected void init() {
		client = Client.create();
	}

	protected Path downloadSubtitleArchive(ParsedFileName pfn, String href) throws ParseException, IOException {

		WebResource.Builder builder = client.resource(href).getRequestBuilder();
		setProivderHeaders(builder);

		ClientResponse res = builder.post(ClientResponse.class);
		if (res.getStatus() != 200) {
			logger.error(
					"search failed with code: " + res.getStatus() + "\n and Content:\n" + res.getEntity(String.class));
			return null;
		}
		String scd = res.getHeaders().getFirst("Content-Disposition");
		ContentDisposition cdp = new ContentDisposition(scd);
		Path dir = SubtitleFileUtils.getArchivesDir(pfn);
		Path file = dir.resolve(SubtitleFileUtils.toFileSystemSafeName(href)
				+ cdp.getFileName().substring(cdp.getFileName().lastIndexOf(".")));
		if (!Files.exists(file.toAbsolutePath())) {
			try (InputStream subZip = res.getEntityInputStream()) {
				logger.trace("Writing archive with path: " + file.toString());
				Files.createDirectories(dir);
				Files.copy(subZip, file.toAbsolutePath());
				subZip.close();
			}
		}
		res.close();
		return file.toAbsolutePath();
	}

	private boolean isLinkAlreadyDownloaded(ParsedFileName pfn, String href) {
		return Files.exists(getArchiveForLink(pfn, href));
	}

	private Path getArchiveForLink(ParsedFileName pfn, String href) {
		return SubtitleFileUtils.getArchivesDir(pfn).resolve(SubtitleFileUtils.toFileSystemSafeName(href));
	}

	protected Set<SubtitleArchiveEntry> getSubtitleArchives(Document siteResults, ParsedFileName pfn) {
		Set<SubtitleArchiveEntry> results = new HashSet<SubtitleArchiveEntry>();
		for (String href : getSubArchiveLinks(siteResults)) {
			// append baseUrl if it's missing
			if (!href.startsWith(getServiceProvider().getBaseUrl())) {
				href = getServiceProvider().getBaseUrl() + href;
			}
			if (!isLinkAlreadyDownloaded(pfn, href)) {
				// download and add to results
				Path subtitleZip = null;
				try {
					subtitleZip = downloadSubtitleArchive(pfn, href);
					SubtitleArchiveEntry entry = new SubtitleArchiveEntry(this.getServiceProvider(), href, subtitleZip);
					results.add(entry);
					logger.info(
							"Found: '"+ entry.getSubtitleArchiveName() + "',  for keyword: '" + getSearchKeyword(pfn) + "', in site: " + getServiceProvider().name());
				} catch (ParseException | IOException e) {
					e.printStackTrace();
				}
			} else {
				SubtitleArchiveEntry entry = new SubtitleArchiveEntry(this.getServiceProvider(), href,
						getArchiveForLink(pfn, href));
				results.add(entry);
			}
		}
		return results;
	}

	protected abstract MultivaluedMap<String, String> getFormData(ParsedFileName pfn, WebResource.Builder builder);

	protected abstract List<String> getSubArchiveLinks(Document siteResults);

	protected abstract void setProivderHeaders(WebResource.Builder builder);

	protected void shutdown() {
		client.destroy();
	}
}
