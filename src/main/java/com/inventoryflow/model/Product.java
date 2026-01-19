package com.inventoryflow.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Model representing a Shopify product with inventory information.
 */
public class Product {
    private final StringProperty id;
    private final StringProperty imageUrl;
    private final StringProperty productName;
    private final StringProperty sku;
    private final IntegerProperty inventoryLevel;
    private final StringProperty inventoryItemId;

    public Product(String id, String imageUrl, String productName, String sku,
                   int inventoryLevel, String inventoryItemId) {
        this.id = new SimpleStringProperty(id);
        this.imageUrl = new SimpleStringProperty(imageUrl);
        this.productName = new SimpleStringProperty(productName);
        this.sku = new SimpleStringProperty(sku);
        this.inventoryLevel = new SimpleIntegerProperty(inventoryLevel);
        this.inventoryItemId = new SimpleStringProperty(inventoryItemId);
    }

    // ID
    public String getId() { return id.get(); }
    public void setId(String value) { id.set(value); }
    public StringProperty idProperty() { return id; }

    // Image URL
    public String getImageUrl() { return imageUrl.get(); }
    public void setImageUrl(String value) { imageUrl.set(value); }
    public StringProperty imageUrlProperty() { return imageUrl; }

    // Product Name
    public String getProductName() { return productName.get(); }
    public void setProductName(String value) { productName.set(value); }
    public StringProperty productNameProperty() { return productName; }

    // SKU
    public String getSku() { return sku.get(); }
    public void setSku(String value) { sku.set(value); }
    public StringProperty skuProperty() { return sku; }

    // Inventory Level
    public int getInventoryLevel() { return inventoryLevel.get(); }
    public void setInventoryLevel(int value) { inventoryLevel.set(value); }
    public IntegerProperty inventoryLevelProperty() { return inventoryLevel; }

    // Inventory Item ID
    public String getInventoryItemId() { return inventoryItemId.get(); }
    public void setInventoryItemId(String value) { inventoryItemId.set(value); }
    public StringProperty inventoryItemIdProperty() { return inventoryItemId; }
}
