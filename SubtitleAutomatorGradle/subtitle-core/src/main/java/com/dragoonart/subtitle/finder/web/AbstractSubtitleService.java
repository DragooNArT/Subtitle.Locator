package com.dragoonart.subtitle.finder.web;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.dragoonart.subtitle.finder.beans.ParsedFileName;
import com.dragoonart.subtitle.finder.beans.SubtitleArchiveEntry;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.header.ContentDisposition;

public abstract class AbstractSubtitleService {

	protected Client client;

	public AbstractSubtitleService() {
		init();
	}

	/**
	 * Get subtitle archives for the specified parsed file name(video name)
	 * 
	 * @param pfn
	 *            - parsed file name(video name)
	 * @return - list of subtitle archive objects
	 */
	public List<SubtitleArchiveEntry> getSubtitles(ParsedFileName pfn) {
		String searchWord = getSearchKeyword(pfn);
		System.out.println("Looking for \"" + searchWord + "\" in site: " + getServiceProvider().getBaseUrl());
		WebResource.Builder builder = client.resource(getServiceProvider().getSearchUrl()).getRequestBuilder();

		ClientResponse resp = builder.entity(getFormData(pfn, builder), MediaType.APPLICATION_FORM_URLENCODED)
				.post(ClientResponse.class);
		
		//do the work
		List<SubtitleArchiveEntry> subZips = getSubtitleArchives(Jsoup.parse(resp.getEntity(String.class)));

		System.out.println("Found " + subZips.size() + " for search: \"" + searchWord + "\" in site: "
				+ getServiceProvider().getBaseUrl());
		for (SubtitleArchiveEntry zip : subZips) {
			System.out.println(zip.getSubtitleName());
		}

		return subZips;
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

	protected Path downloadSubtitleArchive(String folderName, String href) throws ParseException, IOException {

		WebResource.Builder builder = client.resource(href).getRequestBuilder();
		setProivderHeaders(builder);

		ClientResponse res = builder.post(ClientResponse.class);
		if (res.getStatus() != 200) {
			System.out.println(
					"search failed with code: " + res.getStatus() + "\n and Content:\n" + res.getEntity(String.class));
			return null;
		}
		String scd = res.getHeaders().getFirst("Content-Disposition");
		ContentDisposition cdp = new ContentDisposition(scd);

		InputStream subZip = res.getEntityInputStream();
		Path dir = Paths.get("./testFiles/" + folderName + "/archives");
		Path file = dir.resolve(cdp.getFileName());
		if (Files.exists(file)) {
			return file.toAbsolutePath();
		}
		Files.createDirectories(dir);
		Files.copy(subZip, file);

		System.out.println("Wrote to: " + file.toAbsolutePath());
		return file.toAbsolutePath();
	}

	protected List<SubtitleArchiveEntry> getSubtitleArchives(Document siteResults) {
		List<SubtitleArchiveEntry> results = new ArrayList<SubtitleArchiveEntry>();
		for (Element link : getFilteredLinks(siteResults)) {
			String href = link.attr("href");
			//append baseUrl if it's missing
			if (!href.startsWith(getServiceProvider().getBaseUrl())) {
				href = getServiceProvider().getBaseUrl() + href;
			}

			// this is how to get the zip name
			String subZipName = link.textNodes().get(0).toString();
			//download and add to results
			try {
				Path subtitleZip = downloadSubtitleArchive(subZipName, href);
				SubtitleArchiveEntry entry = new SubtitleArchiveEntry(href, subtitleZip);
				results.add(entry);
			} catch (ParseException | IOException e) {
				e.printStackTrace();
			}
			System.out.println("Subtitle: " + subZipName + " Link: " + href);

		}
		return results;
	}

	protected abstract MultivaluedMap<String, String> getFormData(ParsedFileName pfn, WebResource.Builder builder);

	protected abstract List<Element> getFilteredLinks(Document siteResults);

	protected abstract void setProivderHeaders(WebResource.Builder builder);

	protected void shutdown() {
		client.destroy();
	}
}
