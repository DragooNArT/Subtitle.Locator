package com.dragoonart.subtitle.finder.web.impl;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.ws.rs.core.MultivaluedMap;

import org.jsoup.nodes.Document;
import org.slf4j.LoggerFactory;

import com.dragoonart.subtitle.finder.beans.ParsedFileName;
import com.dragoonart.subtitle.finder.web.AbstractSubtitleService;
import com.dragoonart.subtitle.finder.web.SubtitleProvider;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class SubsabService extends AbstractSubtitleService {

	private Pattern pattern_subLinks = Pattern.compile(".*&attach_id=\\d*");
	
	@Override
	protected void init() {
		// TODO Auto-generated method stub
		super.init();
		logger = LoggerFactory.getLogger(getClass());

	}
	@Override
	public SubtitleProvider getServiceProvider() {
		return SubtitleProvider.SUBS_SAB;
	}

	@Override
	protected MultivaluedMap<String, String> getFormData(ParsedFileName pfn, WebResource.Builder builder) {
		MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
		formData.add("movie", getSearchKeyword(pfn));
		formData.add("act", "search");
		if (pfn.hasYear()) {
			formData.add("yr", pfn.getYear());
		}
		// doesn't work
		// if(pfn.hasRelease()) {
		// formData.add("release", pfn.getRelease());
		// }
		// choose Bulgarian
		formData.add("select-language", "2");
		return formData;
	}

	@Override
	protected void setProivderHeaders(Builder builder) {
		builder.header("Accept-Encoding", "gzip, deflate");
		builder.header("Referer", "http://subs.sab.bz/index.php?");

	}

	@Override
	protected List<String> getSubArchiveLinks(Document siteResults) {
		return siteResults.getElementsByAttribute("onmouseover").stream()
				.filter(e -> pattern_subLinks.matcher(e.attr("href")).matches()).map(e -> e.attr("href")).collect(Collectors.toList());
	}
}
