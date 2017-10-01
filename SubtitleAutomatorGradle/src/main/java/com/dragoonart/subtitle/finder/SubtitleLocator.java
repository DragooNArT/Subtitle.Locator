package com.dragoonart.subtitle.finder;
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

import com.dragoonart.subtitle.finder.beans.SubtitleEntry;
import com.dragoonart.subtitle.finder.beans.VideoEntry;
import com.dragoonart.subtitle.finder.parsers.IFileNameParser;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class SubtitleLocator {
	
	private static final String SUBS_SAB_URL = "http://subs.sab.bz/index.php";
	
	private static final String SUBUNACS_URL = "https://subsunacs.net/search.php";

	Pattern pattern_subLinks = Pattern.compile("");
	private Client client;
	
	
	public SubtitleLocator() {
		client = Client.create();
	}
	public List<SubtitleEntry> getSubtitles(VideoEntry vfb) {
		if(vfb.isProccessedForSubtitles()) {
			return vfb.getSubtitles();
		}
		List<SubtitleEntry> subsFound = new ArrayList<>();
		lookupSubtitles(vfb.getParsedFilename().getParsedAttributes(), subsFound);
		
		vfb.setSubtitles(subsFound);
		return subsFound;
	}

	private void lookupSubtitles(Map<String,String> fileAttrs, List<SubtitleEntry> subsFound) {
		WebResource.Builder builder = client.resource(SUBS_SAB_URL).getRequestBuilder();
		
		MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
		StringBuilder sb = new StringBuilder();
		sb.append(fileAttrs.get(IFileNameParser.SHOW_NAME)).append(" ").append(fileAttrs.get(IFileNameParser.SHOW_SEASON)).append(" ").append(fileAttrs.get(IFileNameParser.SHOW_EPISODE));
		formData.add("movie", sb.toString());
		formData.add("act", "search");
		formData.add("select-language", "2");
		ClientResponse resp =  builder.entity(formData, MediaType.APPLICATION_FORM_URLENCODED).post(ClientResponse.class);
		String content = resp.getEntity(String.class);
		Document doc = Jsoup.parse(content);
		Elements links = doc.getElementsByTag("a");
		List<String> subtitleList = new ArrayList<String>();
		for(Element link : links) {
			String href = link.attr("href");
			Matcher matcher = pattern_subLinks.matcher(href);
			if(matcher.matches()) {
				subtitleList.add(href);
			}
		}
		
		System.out.println(content);
	}

}
