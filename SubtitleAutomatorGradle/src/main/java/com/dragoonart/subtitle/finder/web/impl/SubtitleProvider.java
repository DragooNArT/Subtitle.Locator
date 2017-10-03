package com.dragoonart.subtitle.finder.web.impl;

import com.dragoonart.subtitle.finder.web.AbstractSubtitleService;

public enum SubtitleProvider {
	SUBS_SAB("http://subs.sab.bz","index.php", SubsabService.class), SUBUNACS("https://subsunacs.net", "search.php",
			SubunacsService.class);

	private String baseUrl;
	private String searchSuffix;
	private Class<? extends AbstractSubtitleService> serviceClass;

	SubtitleProvider(String baseUrl,String searchPrefix, Class<? extends AbstractSubtitleService> serviceClass) {
		this.baseUrl = baseUrl;
		this.searchSuffix = searchPrefix;
		this.serviceClass = serviceClass;
	}

	public String getBaseUrl() {
		return baseUrl;
	}
	public String getSearchUrl() {
		return baseUrl + "/" + (searchSuffix == null ? "" : searchSuffix);
	}
	public Class<? extends AbstractSubtitleService> getServiceClass() {
		return serviceClass;
	}

}