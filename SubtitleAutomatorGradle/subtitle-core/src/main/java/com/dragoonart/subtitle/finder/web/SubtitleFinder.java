package com.dragoonart.subtitle.finder.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dragoonart.subtitle.finder.beans.SubtitleArchiveEntry;
import com.dragoonart.subtitle.finder.beans.VideoEntry;

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
	
	public Set<SubtitleArchiveEntry> lookupEverywhere(VideoEntry ve) {
		Set<SubtitleArchiveEntry> result = new HashSet<SubtitleArchiveEntry>();
		for (AbstractSubtitleService ss : servicesList.values()) {
			try {
			result.addAll(ss.getSubtitles(ve.getParsedFilename()));
			ve.setSubtitles(result);
			ve.setSubtitlesFound(true);
			} catch(Exception e) {
				System.out.println("Failed too lookup subs in: "+ss.getServiceProvider().getBaseUrl());
				e.printStackTrace();
			}
		}
		return result;
	}

	public List<SubtitleArchiveEntry> lookupSpecificSite(SubtitleProvider provider, VideoEntry ve) {
		return servicesList.get(provider).getSubtitles(ve.getParsedFilename());
	}

	public List<SubtitleArchiveEntry> lookupSpecificSite(List<SubtitleProvider> providers, VideoEntry ve) {
		List<SubtitleArchiveEntry> result = new ArrayList<SubtitleArchiveEntry>();
		for (SubtitleProvider sp : providers) {
			result.addAll(servicesList.get(sp).getSubtitles(ve.getParsedFilename()));
		}
		return result;
	}

	public AbstractSubtitleService getSubtitleService(SubtitleProvider provider) {
		return servicesList.get(provider);
	}
}
