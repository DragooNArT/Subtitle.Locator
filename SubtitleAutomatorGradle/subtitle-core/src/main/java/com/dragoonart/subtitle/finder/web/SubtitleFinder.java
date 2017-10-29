package com.dragoonart.subtitle.finder.web;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dragoonart.subtitle.finder.beans.SubtitleArchiveEntry;
import com.dragoonart.subtitle.finder.beans.VideoEntry;

import stormpot.Poolable;
import stormpot.Slot;

public class SubtitleFinder implements Poolable {

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
	private Slot slot;
	
	public SubtitleFinder(Slot slot) {
		this.slot = slot;
	}

	public Set<SubtitleArchiveEntry> lookupEverywhere(VideoEntry ve) {
		Set<SubtitleArchiveEntry> result = new HashSet<SubtitleArchiveEntry>();
		for (AbstractSubtitleService ss : servicesList.values()) {
			try {
			result.addAll(ss.getSubtitles(ve));
			ve.addSubtitles(result);
			ve.setSubtitlesProcessed(true);
			} catch(Exception e) {
				System.out.println("Failed too lookup subs in: "+ss.getServiceProvider().getBaseUrl());
				e.printStackTrace();
			}
		}
		return result;
	}

	public Set<SubtitleArchiveEntry> lookupSpecificSite(SubtitleProvider provider, VideoEntry ve) {
		return servicesList.get(provider).getSubtitles(ve);
	}

	public Set<SubtitleArchiveEntry> lookupSpecificSite(List<SubtitleProvider> providers, VideoEntry ve) {
		Set<SubtitleArchiveEntry> result = new HashSet<SubtitleArchiveEntry>();
		for (SubtitleProvider sp : providers) {
			result.addAll(servicesList.get(sp).getSubtitles(ve));
		}
		return result;
	}

	public AbstractSubtitleService getSubtitleService(SubtitleProvider provider) {
		return servicesList.get(provider);
	}
	
	public void shutdown() {
		servicesList.values().forEach(e -> e.shutdown());
	}

	@Override
	public void release() {
		slot.release(this);
	}
}
