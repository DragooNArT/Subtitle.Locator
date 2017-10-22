package com.dragoonart.subtitle.finder.ui.listeners;

import com.dragoonart.subtitle.finder.beans.VideoEntry;
import com.dragoonart.subtitle.finder.ui.controllers.MainPanelManager;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class VideoSelectedListener implements ChangeListener<VideoEntry> {
	MainPanelManager panelManager;
	public VideoSelectedListener(MainPanelManager panelManager) {
		this.panelManager = panelManager;
	}
	@Override
	public void changed(ObservableValue<? extends VideoEntry> observable, VideoEntry oldValue, VideoEntry newValue) {
		if(observable.getValue().hasSubtitles()) {
			panelManager.loadSubtitles(observable.getValue().getSubtitles());
		} else {
			panelManager.setNoSubtitles(observable.getValue());
		}
		
	}



}
