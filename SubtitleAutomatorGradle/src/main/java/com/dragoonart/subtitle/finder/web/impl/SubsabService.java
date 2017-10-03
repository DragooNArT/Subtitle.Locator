package com.dragoonart.subtitle.finder.web.impl;

import java.util.regex.Pattern;

import javax.ws.rs.core.MultivaluedMap;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.dragoonart.subtitle.finder.beans.ParsedFileName;
import com.dragoonart.subtitle.finder.web.AbstractSubtitleService;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class SubsabService extends AbstractSubtitleService {
	
	private Pattern pattern_subLinks = Pattern.compile(".*&attach_id=\\d*");
	
	public SubsabService() {}
	
	@Override
	public SubtitleProvider getServiceProvider() {
		return SubtitleProvider.SUBS_SAB;
	}

	@Override
	public String getSearchKeyword(ParsedFileName pfn) {
		StringBuilder sb = new StringBuilder();

		sb.append(pfn.getShowName()).append(" ");
		if (pfn.hasSeason() && pfn.hasEpisode()) {
			sb.append(pfn.getSeason()).append(" ").append(pfn.getEpisode());
		}
		return sb.toString();
	}

	@Override
	protected MultivaluedMap<String, String> getFormData(String searchKeyword, WebResource.Builder builder) {
		MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
		formData.add("movie", searchKeyword);
		formData.add("act", "search");
		formData.add("select-language", "2");
		return formData;
	}

	@Override
	protected void setProivderHeaders(Builder builder) {
		builder.header("Accept-Encoding", "gzip, deflate");
		builder.header("Referer", "http://subs.sab.bz/index.php?");
		
	}

	@Override
	protected boolean acceptLink(String href) {
		return pattern_subLinks.matcher(href).matches();
	}

	@Override
	protected Elements getFilteredLinks(Document siteResults) {
		return siteResults.getElementsByAttribute("onmouseover");
	}
}
