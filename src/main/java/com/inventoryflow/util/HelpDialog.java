package com.inventoryflow.util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Displays setup instructions and help documentation within the application.
 */
public class HelpDialog {

    private static final String DARK_BG = "#1e1e2e";
    private static final String CARD_BG = "#27273a";
    private static final String TEXT_PRIMARY = "#f4f4f5";
    private static final String TEXT_SECONDARY = "#a1a1aa";
    private static final String ACCENT = "#7c3aed";
    private static final String SUCCESS = "#10b981";
    private static final String WARNING = "#f59e0b";

    /**
     * Shows the setup guide dialog.
     */
    public static void showSetupGuide(Stage owner) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setTitle("Setup Guide");

        VBox content = createSetupGuideContent(dialog);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: " + DARK_BG + "; -fx-background-color: " + DARK_BG + ";");

        VBox root = new VBox();
        root.setStyle("-fx-background-color: " + DARK_BG + ";");

        // Header with close button
        HBox header = createHeader("Shopify Setup Guide", dialog);
        root.getChildren().addAll(header, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        Scene scene = new Scene(root, 600, 700);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    /**
     * Shows a quick help dialog with basic instructions.
     */
    public static void showQuickHelp(Stage owner) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setTitle("Help");

        VBox content = createQuickHelpContent();

        VBox root = new VBox();
        root.setStyle("-fx-background-color: " + DARK_BG + ";");

        HBox header = createHeader("InventoryFlow Help", dialog);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: " + DARK_BG + "; -fx-background-color: " + DARK_BG + ";");

        Button closeBtn = new Button("Close");
        styleButton(closeBtn, ACCENT);
        closeBtn.setOnAction(e -> dialog.close());

        HBox footer = new HBox(closeBtn);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(15));
        footer.setStyle("-fx-background-color: " + CARD_BG + ";");

        root.getChildren().addAll(header, scrollPane, footer);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        Scene scene = new Scene(root, 500, 500);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private static HBox createHeader(String title, Stage dialog) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");

        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXT_SECONDARY +
                "; -fx-font-size: 16px; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> dialog.close());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(titleLabel, spacer, closeBtn);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15, 15, 15, 20));
        header.setStyle("-fx-background-color: " + CARD_BG + "; -fx-border-color: #3f3f46; " +
                "-fx-border-width: 0 0 1 0;");

        return header;
    }

    private static VBox createSetupGuideContent(Stage dialog) {
        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        content.setStyle("-fx-background-color: " + DARK_BG + ";");

        // Introduction
        content.getChildren().add(createInfoCard(
                "What You'll Need",
                "• A Shopify store with admin access\n" +
                "• About 5-10 minutes to complete setup"
        ));

        // Step 1
        content.getChildren().add(createStepCard("1", "Enable Custom App Development",
                "1. Log in to your Shopify Admin\n" +
                "2. Go to Settings → Apps\n" +
                "3. Click App and sales channel settings\n" +
                "4. Click Develop apps\n" +
                "5. Click Allow custom app development\n\n" +
                "Note: Only the store owner can enable this feature."
        ));

        // Step 2
        content.getChildren().add(createStepCard("2", "Create a Custom App",
                "1. Click Create an app\n" +
                "2. Name it \"InventoryFlow\" (or any name)\n" +
                "3. Click Create app"
        ));

        // Step 3
        content.getChildren().add(createStepCard("3", "Configure API Permissions",
                "1. Click Configure Admin API scopes\n" +
                "2. Enable these scopes:\n" +
                "   • read_products (View product info)\n" +
                "   • read_inventory (View stock levels)\n" +
                "3. Click Save"
        ));

        // Step 4
        content.getChildren().add(createStepCard("4", "Get Your Access Token",
                "1. Go to the API credentials tab\n" +
                "2. Click Install app\n" +
                "3. Click Reveal token once\n" +
                "4. Copy the token (starts with shpat_)\n\n" +
                "⚠️ Important: The token is only shown once!\n" +
                "Copy it immediately before closing the page."
        ));

        // Step 5
        content.getChildren().add(createStepCard("5", "Enter Credentials",
                "1. Store Domain: your-store.myshopify.com\n" +
                "   (Just \"your-store\" also works)\n\n" +
                "2. Access Token: Paste the shpat_ token\n\n" +
                "3. Create a 4-digit PIN for app security"
        ));

        // Finding domain help
        content.getChildren().add(createInfoCard(
                "💡 Finding Your Store Domain",
                "Look at your Shopify Admin URL:\n" +
                "https://YOUR-STORE.myshopify.com/admin\n\n" +
                "Enter: YOUR-STORE.myshopify.com"
        ));

        // Close button
        Button closeBtn = new Button("Got it!");
        styleButton(closeBtn, ACCENT);
        closeBtn.setPrefWidth(200);
        closeBtn.setOnAction(e -> dialog.close());

        HBox buttonBox = new HBox(closeBtn);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        content.getChildren().add(buttonBox);

        return content;
    }

    private static VBox createQuickHelpContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: " + DARK_BG + ";");

        content.getChildren().add(createHelpSection("🔍 Search",
                "Type in the search bar to filter products by SKU or product name."));

        content.getChildren().add(createHelpSection("🔄 Sync",
                "Click the Sync button to refresh inventory data from Shopify."));

        content.getChildren().add(createHelpSection("📊 Inventory Colors",
                "• Green: 10+ items in stock\n" +
                "• Orange: Low stock (1-9 items)\n" +
                "• Red: Out of stock (0 items)"));

        content.getChildren().add(createHelpSection("🔐 Security",
                "Your Shopify token is encrypted and stored locally. " +
                "Your PIN is hashed and never stored in plain text."));

        content.getChildren().add(createHelpSection("❓ Need to Reconnect?",
                "Delete ~/.inventoryflow/inventoryflow.db and restart the app to " +
                "enter new Shopify credentials."));

        return content;
    }

    private static VBox createStepCard(String stepNum, String title, String content) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 8;");

        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label numLabel = new Label(stepNum);
        numLabel.setStyle("-fx-background-color: " + ACCENT + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 12;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");

        titleRow.getChildren().addAll(numLabel, titleLabel);

        Label contentLabel = new Label(content);
        contentLabel.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 13px;");
        contentLabel.setWrapText(true);

        card.getChildren().addAll(titleRow, contentLabel);
        return card;
    }

    private static VBox createInfoCard(String title, String content) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 8; " +
                "-fx-border-color: " + ACCENT + "; -fx-border-width: 0 0 0 3; -fx-border-radius: 8;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");

        Label contentLabel = new Label(content);
        contentLabel.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 13px;");
        contentLabel.setWrapText(true);

        card.getChildren().addAll(titleLabel, contentLabel);
        return card;
    }

    private static VBox createHelpSection(String title, String content) {
        VBox section = new VBox(5);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");

        Label contentLabel = new Label(content);
        contentLabel.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 13px;");
        contentLabel.setWrapText(true);

        section.getChildren().addAll(titleLabel, contentLabel);
        return section;
    }

    private static void styleButton(Button button, String color) {
        button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10 25; -fx-background-radius: 6; -fx-cursor: hand;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: derive(" + color + ", -10%); " +
                "-fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 25; " +
                "-fx-background-radius: 6; -fx-cursor: hand;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10 25; -fx-background-radius: 6; -fx-cursor: hand;"));
    }
}
