package com.dragoonart.subtitle.finder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.dragoonart.subtitle.finder.beans.SubtitleArchiveEntry;
import com.dragoonart.subtitle.finder.beans.VideoEntry;
import com.dragoonart.subtitle.finder.parsers.IFileNameParser;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.header.ContentDisposition;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class SubtitleLocator {

	private static final String SUBS_SAB_URL = "http://subs.sab.bz/index.php";

	private static final String SUBUNACS_URL = "https://subsunacs.net/search.php";

	Pattern pattern_subLinks = Pattern.compile(".*&attach_id=\\d*");
	private Client client;

	public SubtitleLocator() {
		client = Client.create();
	}

	public List<SubtitleArchiveEntry> getSubtitleZips(VideoEntry vfb) {
		if (vfb.isProccessedForSubtitles()) {
			return vfb.getSubtitles();
		}
		List<SubtitleArchiveEntry> subsFound = lookupSubtitles(vfb);

		vfb.setSubtitles(subsFound);
		return subsFound;
	}

	private List<SubtitleArchiveEntry> lookupSubtitles(VideoEntry ve) {
		List<SubtitleArchiveEntry> result = new ArrayList<SubtitleArchiveEntry>();
		result.addAll(getSubtitlesSubSab(ve));
		result.addAll(getSubtitlesSubSab(ve));
		return result;
	}
	private List<SubtitleArchiveEntry> getSubtitlesSubUnacs(VideoEntry ve) {
		List<SubtitleArchiveEntry> subtitleList = new ArrayList<SubtitleArchiveEntry>();
		WebResource.Builder builder = client.resource(SUBUNACS_URL).getRequestBuilder();
		//TODO e tuka bluskai 
		return subtitleList;
	}
	private List<SubtitleArchiveEntry> getSubtitlesSubSab(VideoEntry ve) {
		List<SubtitleArchiveEntry> subtitleList = new ArrayList<SubtitleArchiveEntry>();
		
		WebResource.Builder builder = client.resource(SUBS_SAB_URL).getRequestBuilder();
		Map<String,String> vfb = ve.getParsedFilename().getParsedAttributes();
		MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
		StringBuilder sb = new StringBuilder();
		
		sb.append(vfb.get(IFileNameParser.SHOW_NAME)).append(" ");
		if (vfb.containsKey(IFileNameParser.SHOW_SEASON) && vfb.containsKey(IFileNameParser.SHOW_EPISODE)) {
			sb.append(vfb.get(IFileNameParser.SHOW_SEASON)).append(" ")
					.append(vfb.get(IFileNameParser.SHOW_EPISODE));
		}
		formData.add("movie", sb.toString());
		formData.add("act", "search");
		formData.add("select-language", "2");
		ClientResponse resp = builder.entity(formData, MediaType.APPLICATION_FORM_URLENCODED)
				.post(ClientResponse.class);
		System.out.println("Looking for subtitles: " + sb.toString());
		String content = resp.getEntity(String.class);
		Document doc = Jsoup.parse(content);
		Elements links = doc.getElementsByTag("a");
		
		for (Element link : links) {
			String href = link.attr("href");
			Matcher matcher = pattern_subLinks.matcher(href);
			if (matcher.matches()) {
				String subName = link.textNodes().get(0).toString();
				Path subtitleZip = downloadSubtitleToTemp(ve.getAcceptableFileName(), href);
				SubtitleArchiveEntry entry = new SubtitleArchiveEntry(subName, href, subtitleZip);
				subtitleList.add(entry);
				System.out.println("Subtitle: " + subName + " Link: " + href);
			}
		}
		return subtitleList;
	}

	private Path downloadSubtitleToTemp(String folderName, String href) {
		WebResource.Builder builder = client.resource(href).getRequestBuilder();
		builder.header("Accept-Encoding", "gzip, deflate");
		builder.header("Referer", "http://subs.sab.bz/index.php?");
		ClientResponse res = builder.post(ClientResponse.class);
		String scd = res.getHeaders().getFirst("Content-Disposition");
		ContentDisposition cdp = null;
		try {
			cdp = new ContentDisposition(scd);
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		InputStream subZip = res.getEntityInputStream();
		Path dir = Paths.get("./testFiles/" + folderName);
		Path file = dir.resolve(cdp.getFileName());
		if(Files.exists(file)) {
			return  file.toAbsolutePath();
		}
		try {
			Files.createDirectories(dir);
			Files.copy(subZip, file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Wrote to: " + file.toAbsolutePath());
		return file.toAbsolutePath();
	}
}
