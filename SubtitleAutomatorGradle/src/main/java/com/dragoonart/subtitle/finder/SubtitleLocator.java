package com.dragoonart.subtitle.finder;
import java.util.ArrayList;
import java.util.List;

import com.dragoonart.subtitle.finder.beans.SubtitleEntry;
import com.dragoonart.subtitle.finder.beans.VideoEntry;
import com.sun.jersey.api.client.Client;

public class SubtitleLocator {
	
	private static final String SUBS_SAB_URL = "http://subs.sab.bz/";
	
	private static final String SUBUNACS_URL = "https://subsunacs.net/search.php";

	private Client client;
	
	
	public SubtitleLocator() {
		// TODO Auto-generated constructor stub
	}
	public List<SubtitleEntry> getSubtitles(VideoEntry vfb) {
		if(vfb.isProccessedForSubtitles()) {
			return vfb.getSubtitles();
		}
		List<SubtitleEntry> subsFound = new ArrayList<>();
		lookupSubtitles(vfb.getFileName(), subsFound);
		
		vfb.setSubtitles(subsFound);
		return subsFound;
	}

	private void lookupSubtitles(String fileName, List<SubtitleEntry> subsFound) {
		// TODO Auto-generated method stub
		
	}

}
