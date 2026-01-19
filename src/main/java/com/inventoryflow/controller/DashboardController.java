package com.inventoryflow.controller;

import java.util.List;

import com.inventoryflow.App;
import com.inventoryflow.model.Product;
import com.inventoryflow.service.ShopifyService;
import com.inventoryflow.util.HelpDialog;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Controller for the main dashboard displaying product inventory.
 */
public class DashboardController {

    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, String> imageColumn;
    @FXML private TableColumn<Product, String> productNameColumn;
    @FXML private TableColumn<Product, String> skuColumn;
    @FXML private TableColumn<Product, Number> inventoryColumn;

    @FXML private TextField searchField;
    @FXML private Button syncButton;

    @FXML private VBox loadingOverlay;
    @FXML private Label loadingLabel;
    @FXML private VBox errorBox;
    @FXML private Label errorMessageLabel;

    @FXML private Label statusLabel;
    @FXML private Label countLabel;

    private ShopifyService shopifyService;
    private ObservableList<Product> productList;
    private FilteredList<Product> filteredProducts;

    @FXML
    public void initialize() {
        shopifyService = new ShopifyService();
        productList = FXCollections.observableArrayList();
        filteredProducts = new FilteredList<>(productList, p -> true);

        setupTableColumns();
        productsTable.setItems(filteredProducts);

        // Auto-load products on startup
        handleSync();
    }

    private void setupTableColumns() {
        // Image column with custom cell factory
        imageColumn.setCellValueFactory(data -> data.getValue().imageUrlProperty());
        imageColumn.setCellFactory(column -> new TableCell<>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitWidth(50);
                imageView.setFitHeight(50);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);
            }

            @Override
            protected void updateItem(String imageUrl, boolean empty) {
                super.updateItem(imageUrl, empty);
                if (empty || imageUrl == null || imageUrl.isEmpty()) {
                    setGraphic(null);
                } else {
                    try {
                        // Load image with size parameters for efficiency
                        String optimizedUrl = imageUrl.contains("?")
                                ? imageUrl + "&width=100"
                                : imageUrl + "?width=100";
                        Image image = new Image(optimizedUrl, 50, 50, true, true, true);
                        imageView.setImage(image);
                        setGraphic(imageView);
                    } catch (Exception e) {
                        setGraphic(null);
                    }
                }
            }
        });

        // Product name column
        productNameColumn.setCellValueFactory(data -> data.getValue().productNameProperty());

        // SKU column
        skuColumn.setCellValueFactory(data -> data.getValue().skuProperty());

        // Inventory column with formatting
        inventoryColumn.setCellValueFactory(data -> data.getValue().inventoryLevelProperty());
        inventoryColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    int inventory = item.intValue();
                    setText(String.valueOf(inventory));

                    // Color code based on inventory level
                    if (inventory == 0) {
                        setStyle("-fx-text-fill: #ef4444;"); // Red for out of stock
                    } else if (inventory < 10) {
                        setStyle("-fx-text-fill: #f59e0b;"); // Orange for low stock
                    } else {
                        setStyle("-fx-text-fill: #10b981;"); // Green for in stock
                    }
                }
            }
        });
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText();
        if (searchText == null || searchText.isEmpty()) {
            filteredProducts.setPredicate(p -> true);
        } else {
            String lowerSearch = searchText.toLowerCase();
            filteredProducts.setPredicate(product ->
                    (product.getSku() != null &&
                     product.getSku().toLowerCase().contains(lowerSearch)) ||
                    (product.getProductName() != null &&
                     product.getProductName().toLowerCase().contains(lowerSearch))
            );
        }
        updateCountLabel();
    }

    @FXML
    private void handleSync() {
        showLoading(true, "Syncing inventory from Shopify...");
        hideError();
        syncButton.setDisable(true);
        statusLabel.setText("Syncing...");

        shopifyService.fetchProducts()
                .thenAccept(products -> Platform.runLater(() -> {
                    productList.clear();
                    productList.addAll(products);
                    showLoading(false, null);
                    syncButton.setDisable(false);
                    statusLabel.setText("Last synced: just now");
                    updateCountLabel();

                    // Re-apply filter
                    handleSearch();
                }))
                .exceptionally(error -> {
                    Platform.runLater(() -> {
                        showLoading(false, null);
                        showError(error.getMessage());
                        syncButton.setDisable(false);
                        statusLabel.setText("Sync failed");
                    });
                    return null;
                });
    }

    @FXML
    private void handleLogout() {
        try {
            App.setRoot("login");
        } catch (Exception e) {
            showError("Failed to logout: " + e.getMessage());
        }
    }

    @FXML
    private void handleHelp() {
        Stage stage = (Stage) productsTable.getScene().getWindow();
        HelpDialog.showQuickHelp(stage);
    }

    private void showLoading(boolean show, String message) {
        loadingOverlay.setVisible(show);
        loadingOverlay.setManaged(show);
        if (message != null) {
            loadingLabel.setText(message);
        }
    }

    private void showError(String message) {
        errorBox.setVisible(true);
        errorBox.setManaged(true);
        // Clean up nested exception messages
        String cleanMessage = message;
        if (cleanMessage.contains(": ")) {
            cleanMessage = cleanMessage.substring(cleanMessage.lastIndexOf(": ") + 2);
        }
        errorMessageLabel.setText(cleanMessage);
    }

    private void hideError() {
        errorBox.setVisible(false);
        errorBox.setManaged(false);
    }

    private void updateCountLabel() {
        int showing = filteredProducts.size();
        int total = productList.size();
        if (showing == total) {
            countLabel.setText(total + " products");
        } else {
            countLabel.setText(showing + " of " + total + " products");
        }
    }
}
