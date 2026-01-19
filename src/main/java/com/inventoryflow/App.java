package com.inventoryflow;

import java.io.IOException;
import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main application entry point for InventoryFlow.
 * Manages scene transitions and application lifecycle.
 */
public class App extends Application {

    private static Scene scene;
    private static final int DEFAULT_WIDTH = 1200;
    private static final int DEFAULT_HEIGHT = 800;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("login"), DEFAULT_WIDTH, DEFAULT_HEIGHT);

        URL cssUrl = getClass().getResource("/css/dark-theme.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("Warning: dark-theme.css not found");
        }

        stage.setTitle("InventoryFlow - Shopify Inventory Management");
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Changes the root view of the application.
     */
    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        URL fxmlUrl = App.class.getResource("/fxml/" + fxml + ".fxml");
        if (fxmlUrl == null) {
            throw new IOException("FXML resource not found: " + fxml);
        }
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
