package com.inventoryflow;

import static org.junit.jupiter.api.Assertions.*;

import com.inventoryflow.model.Product;
import org.junit.jupiter.api.Test;

class ProductTest {

    @Test
    void testProductCreation() {
        Product product = new Product(
            "gid://shopify/Product/123",
            "https://example.com/image.jpg",
            "Test Product",
            "SKU-001",
            50,
            "gid://shopify/InventoryItem/456"
        );

        assertEquals("gid://shopify/Product/123", product.getId());
        assertEquals("https://example.com/image.jpg", product.getImageUrl());
        assertEquals("Test Product", product.getProductName());
        assertEquals("SKU-001", product.getSku());
        assertEquals(50, product.getInventoryLevel());
        assertEquals("gid://shopify/InventoryItem/456", product.getInventoryItemId());
    }

    @Test
    void testProductPropertyUpdates() {
        Product product = new Product(
            "id1", "url1", "Name1", "SKU1", 10, "inv1"
        );

        product.setProductName("Updated Name");
        product.setInventoryLevel(25);

        assertEquals("Updated Name", product.getProductName());
        assertEquals(25, product.getInventoryLevel());
    }

    @Test
    void testProductProperties() {
        Product product = new Product(
            "id1", "url1", "Name1", "SKU1", 10, "inv1"
        );

        assertNotNull(product.idProperty());
        assertNotNull(product.imageUrlProperty());
        assertNotNull(product.productNameProperty());
        assertNotNull(product.skuProperty());
        assertNotNull(product.inventoryLevelProperty());
        assertNotNull(product.inventoryItemIdProperty());
    }
}
