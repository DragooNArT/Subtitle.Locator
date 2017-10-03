package com.dragoonart.subtitle.finder.web.impl;

import javax.ws.rs.core.MultivaluedMap;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.dragoonart.subtitle.finder.beans.ParsedFileName;
import com.dragoonart.subtitle.finder.web.AbstractSubtitleService;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class SubunacsService extends AbstractSubtitleService {

	public SubunacsService() {

	}

	@Override
	public SubtitleProvider getServiceProvider() {
		return SubtitleProvider.SUBUNACS;
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
	protected MultivaluedMap<String, String> getFormData(String searchKeyword, Builder builder) {
		MultivaluedMap<String, String> formData = new MultivaluedMapImpl();

		formData.add("m", searchKeyword);
		formData.add("l", "-1");
		formData.add("t", "Submit");
		return formData;
	}

	@Override
	protected void setProivderHeaders(Builder builder) {
		builder.header("Accept-Encoding", "gzip, deflate, br");
		builder.header("Referer", "https://subsunacs.net/search.php");
	}

	@Override
	protected boolean acceptLink(String href) {
		return href != null && !href.isEmpty() && href.startsWith("/subtitles/")
				&& href.lastIndexOf('/') == (href.length() - 1);
	}

	@Override
	protected Elements getFilteredLinks(Document siteResults) {
		return siteResults.getElementsByClass("tooltip");
	}
}
