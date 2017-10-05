package com.dragoonart.subtitle.finder.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dragoonart.subtitle.finder.beans.ParsedFileName;
import com.dragoonart.subtitle.finder.beans.SubtitleArchiveEntry;

public class SubtitleFinder {

	private static Map<SubtitleProvider, AbstractSubtitleService> servicesList = new HashMap<SubtitleProvider, AbstractSubtitleService>();
	
	static {
		for (SubtitleProvider sf : SubtitleProvider.values()) {
			try {
				servicesList.put(sf, sf.getServiceClass().newInstance());
			} catch (InstantiationException | IllegalAccessException e) {
				System.out.println("Unable to load service provider: " + sf.getBaseUrl());
				e.printStackTrace();
			}
		}
	}
	
	public List<SubtitleArchiveEntry> lookupEverywhere(ParsedFileName pfn) {
		List<SubtitleArchiveEntry> result = new ArrayList<SubtitleArchiveEntry>();
		for (AbstractSubtitleService ss : servicesList.values()) {
			try {
			result.addAll(ss.getSubtitles(pfn));
			} catch(Exception e) {
				System.out.println("Failed too lookup subs in: "+ss.getServiceProvider().getBaseUrl());
				e.printStackTrace();
			}
		}
		return result;
	}

	public List<SubtitleArchiveEntry> lookupSpecificSite(SubtitleProvider provider, ParsedFileName pfn) {
		return servicesList.get(provider).getSubtitles(pfn);
	}

	public List<SubtitleArchiveEntry> lookupSpecificSite(List<SubtitleProvider> providers, ParsedFileName pfn) {
		List<SubtitleArchiveEntry> result = new ArrayList<SubtitleArchiveEntry>();
		for (SubtitleProvider sp : providers) {
			result.addAll(servicesList.get(sp).getSubtitles(pfn));
		}
		return result;
	}

	public AbstractSubtitleService getSubtitleService(SubtitleProvider provider) {
		return servicesList.get(provider);
	}
}
