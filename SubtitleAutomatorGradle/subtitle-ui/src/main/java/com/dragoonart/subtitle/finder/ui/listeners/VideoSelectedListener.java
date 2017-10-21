package com.dragoonart.subtitle.finder.ui.listeners;

import com.dragoonart.subtitle.finder.beans.VideoEntry;
import com.dragoonart.subtitle.finder.ui.controllers.MainPanelController;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class VideoSelectedListener implements ChangeListener<VideoEntry> {
	MainPanelController mainPanelController;
	public VideoSelectedListener(MainPanelController mainPanelController) {
		this.mainPanelController = mainPanelController;
	}
	@Override
	public void changed(ObservableValue<? extends VideoEntry> observable, VideoEntry oldValue, VideoEntry newValue) {
		System.out.println(observable.getValue().getFileName());
		
	}



}
