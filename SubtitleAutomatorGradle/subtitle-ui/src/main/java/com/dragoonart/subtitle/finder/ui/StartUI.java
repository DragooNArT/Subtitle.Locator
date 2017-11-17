package com.dragoonart.subtitle.finder.ui;

import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dragoonart.subtitle.finder.ui.usersettings.PreferencesManager;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class StartUI extends Application {
	private static final Logger logger = LoggerFactory.getLogger(StartUI.class);
	public StartUI() {
		// TODO Auto-generated constructor stub
	}

	private static Stage stage;

	public static java.awt.TrayIcon trayIcon;

	@Override
	public void start(Stage primaryStage) {
		stage = primaryStage;
		try {
			Scene scene;
			if (PreferencesManager.INSTANCE.hasLocations()) {
				scene = new Scene(ResourceManager.getScene("MainPanel.fxml"), 853, 557);
			} else {
				scene = new Scene(ResourceManager.getScene("ChooseLocation.fxml"), 640, 480);
			}
		
			Platform.setImplicitExit(false);
			// sets up the tray icon (using awt code run on the swing thread).
			SwingUtilities.invokeLater(() -> addAppToTray(primaryStage));
			// instructs the javafx system not to exit implicitly when the last application
			// window is shut.\
			
			primaryStage.setTitle("Subtitle Finder BG");
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.centerOnScreen();
			primaryStage.setResizable(false);
			primaryStage.show();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean isUiVisible() {
		return stage.isShowing();
	}

//	/**
//	 * Sets up a system tray icon for the application.
//	 */
	private void addAppToTray(Stage primaryStage) {
		try {
			// ensure awt toolkit is initialized.
			java.awt.Toolkit.getDefaultToolkit();

			// app requires system tray support, just exit if there is no support.
			if (!java.awt.SystemTray.isSupported()) {
				logger.warn("No system tray support, application exiting.");
				primaryStage.setOnCloseRequest(event -> {
					Platform.exit();
					System.exit(0);
				});
			}

			// set up a system tray icon.
			java.awt.SystemTray tray = java.awt.SystemTray.getSystemTray();
			URL imageLoc = getClass().getResource("Subscript_16px.png");
			java.awt.Image image = ImageIO.read(imageLoc);
			trayIcon = new java.awt.TrayIcon(image);

			// if the user double-clicks on the tray icon, show the main app stage.
			trayIcon.addActionListener(event -> Platform.runLater(this::showStage));

			// if the user selects the default menu item (which includes the app name),
			// show the main app stage.
			java.awt.MenuItem openItem = new java.awt.MenuItem("Open");
			openItem.addActionListener(event -> Platform.runLater(this::showStage));

			// the convention for tray icons seems to be to set the default icon for opening
			// the application stage in a bold font.
			java.awt.Font defaultFont = java.awt.Font.decode(null);
			java.awt.Font boldFont = defaultFont.deriveFont(java.awt.Font.BOLD);
			openItem.setFont(boldFont);

			// to really exit the application, the user must go to the system tray icon
			// and select the exit option, this will shutdown JavaFX and remove the
			// tray icon (removing the tray icon will also shut down AWT).
			java.awt.MenuItem exitItem = new java.awt.MenuItem("Exit");
			exitItem.addActionListener(event -> {
				// notificationTimer.cancel();
				Platform.exit();
				tray.remove(trayIcon);
				System.exit(0);
			});

			// setup the popup menu for the application.
			final java.awt.PopupMenu popup = new java.awt.PopupMenu();
			popup.add(openItem);
			popup.addSeparator();
			popup.add(exitItem);
			trayIcon.setPopupMenu(popup);

			// // create a timer which periodically displays a notification message.
			// notificationTimer.schedule(new TimerTask() {
			// @Override
			// public void run() {
			// javax.swing.SwingUtilities.invokeLater(() -> trayIcon.displayMessage("hello",
			// "The time is now " + timeFormat.format(new Date()),
			// java.awt.TrayIcon.MessageType.INFO));
			// }
			// }, 5_000, 60_000);

			// add the application tray icon to the system tray.
			tray.add(trayIcon);
		} catch (Exception e) {
			logger.error("Unable to init system tray, the app will close when you close the window", e);
			primaryStage.setOnCloseRequest(event -> {
				Platform.exit();
				System.exit(0);
			});
		}
	}

	/**
	 * Shows the application stage and ensures that it is brought ot the front of
	 * all stages.
	 */
	private void showStage() {
		if (stage != null) {
			stage.show();
			stage.toFront();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
