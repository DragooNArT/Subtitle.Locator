package com.dragoonart.subtitle.finder.ui.listeners;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import com.dragoonart.subtitle.finder.ui.managers.MainPanelManager;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class SubtitleSelectedListener implements ChangeListener<Path> {
	MainPanelManager panelManager;
	Alert alert;
	public SubtitleSelectedListener(MainPanelManager panelManager) {
		this.panelManager = panelManager;
		alert = new Alert(Alert.AlertType.CONFIRMATION,"Do you want to apply these subtitles?");
		alert.setTitle("Confirmation Dialog");
	}


	@Override
	public void changed(ObservableValue<? extends Path> observable, Path oldValue, Path newValue) {
			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.OK) {
				try {
					copySubtitleToMovie(panelManager.getSelectedVideo(), observable.getValue());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

	}

	private boolean hasSubtitle(Path pathToVideo) {
		Path pathToSRT = pathToVideo.getParent().resolve(
				pathToVideo.getFileName().toString().substring(0, pathToVideo.getFileName().toString().lastIndexOf("."))
						+ ".srt");
		Path pathToSUB = pathToVideo.getParent().resolve(
				pathToVideo.getFileName().toString().substring(0, pathToVideo.getFileName().toString().lastIndexOf("."))
						+ ".sub");
		return Files.exists(pathToSRT) || Files.exists(pathToSUB);
	}

	private void copySubtitleToMovie(Path newFilePath, Path subPath) throws IOException {
		// remove the old subtitle file
		String movieFileName = newFilePath.getFileName().toString();
		newFilePath = newFilePath.getParent()
				.resolve(movieFileName.substring(0, movieFileName.lastIndexOf(".")) + ".srt");
		if (Files.exists(newFilePath)) {
			Files.delete(newFilePath);
		}
		// place the new subtitle file
		Files.copy(subPath, newFilePath);
	}
}
