package com.dragoonart.subtitle.finder.ui;
	
import com.dragoonart.subtitle.finder.ui.usersettings.PreferencesManager;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class StartUI extends Application {
	
	
	public StartUI() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			Scene scene;
			if(PreferencesManager.INSTANCE.hasLocations()) {
				scene = new Scene(ResourceManager.getScene("MainPanel.fxml"),1280,800);
			} else {
				scene = new Scene(ResourceManager.getScene("ChooseLocation.fxml"),640,480);
			}
			primaryStage.setTitle("Subtitle Finder BG");
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.centerOnScreen();
			primaryStage.setResizable(false);
			primaryStage.show();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
