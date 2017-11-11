package com.dragoonart.subtitle.finder.web.impl;

import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.core.MultivaluedMap;

import org.jsoup.nodes.Document;
import org.slf4j.LoggerFactory;

import com.dragoonart.subtitle.finder.beans.ParsedFileName;
import com.dragoonart.subtitle.finder.web.AbstractSubtitleService;
import com.dragoonart.subtitle.finder.web.SubtitleProvider;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class SubunacsService extends AbstractSubtitleService {
	
	@Override
	protected void init() {
		// TODO Auto-generated method stub
		super.init();
		logger = LoggerFactory.getLogger(getClass());

	}
	@Override
	public SubtitleProvider getServiceProvider() {
		return SubtitleProvider.SUBUNACS;
	}

	@Override
	protected MultivaluedMap<String, String> getFormData(ParsedFileName pfn, Builder builder) {
		MultivaluedMap<String, String> formData = new MultivaluedMapImpl();

		formData.add("m", getSearchKeyword(pfn));
		formData.add("l", "-1");
		if (pfn.hasYear()) {
			formData.add("y", pfn.getYear());
		}
		formData.add("t", "Submit");
		return formData;
	}

	@Override
	protected void setProivderHeaders(Builder builder) {
		builder.header("Accept-Encoding", "gzip, deflate, br");
		builder.header("Referer", "https://subsunacs.net/search.php");
	}

	private boolean acceptLink(String href) {
		return href != null && !href.isEmpty() && href.startsWith("/subtitles/")
				&& href.lastIndexOf('/') == (href.length() - 1);
	}

	@Override
	protected List<String> getSubArchiveLinks(Document siteResults) {
		return siteResults.getElementsByClass("tooltip").stream().filter(e -> acceptLink(e.attr("href")))
				.map(e -> e.attr("href")).collect(Collectors.toList());
	}
}
