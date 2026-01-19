package com.inventoryflow.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.inventoryflow.model.Product;
import com.inventoryflow.util.DatabaseManager;

/**
 * Service for interacting with Shopify's GraphQL Admin API.
 * Handles all API communication and data transformation.
 */
public class ShopifyService {

    private static final String API_VERSION = "2024-01";
    private static final int PAGE_SIZE = 50;

    private final HttpClient httpClient;
    private final Gson gson;
    private final String shopDomain;
    private final String accessToken;

    public ShopifyService() {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        this.shopDomain = dbManager.getShopifyDomain();
        this.accessToken = dbManager.getShopifyToken();

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.gson = new Gson();
    }

    /**
     * Fetches all products with their inventory levels from Shopify.
     * Uses cursor-based pagination to handle large inventories.
     */
    public CompletableFuture<List<Product>> fetchProducts() {
        return CompletableFuture.supplyAsync(() -> {
            List<Product> allProducts = new ArrayList<>();
            String cursor = null;
            boolean hasNextPage = true;

            while (hasNextPage) {
                try {
                    JsonObject response = executeGraphQL(buildProductsQuery(cursor));
                    JsonObject data = response.getAsJsonObject("data");

                    if (data == null) {
                        JsonArray errors = response.getAsJsonArray("errors");
                        if (errors != null && errors.size() > 0) {
                            String errorMsg = errors.get(0).getAsJsonObject()
                                    .get("message").getAsString();
                            throw new RuntimeException("Shopify API error: " + errorMsg);
                        }
                        throw new RuntimeException("Invalid response from Shopify API");
                    }

                    JsonObject products = data.getAsJsonObject("products");
                    JsonArray edges = products.getAsJsonArray("edges");

                    for (JsonElement edge : edges) {
                        JsonObject node = edge.getAsJsonObject().getAsJsonObject("node");
                        List<Product> parsed = parseProductNode(node);
                        allProducts.addAll(parsed);
                    }

                    JsonObject pageInfo = products.getAsJsonObject("pageInfo");
                    hasNextPage = pageInfo.get("hasNextPage").getAsBoolean();

                    if (hasNextPage && edges.size() > 0) {
                        cursor = edges.get(edges.size() - 1).getAsJsonObject()
                                .get("cursor").getAsString();
                    }

                } catch (Exception e) {
                    throw new RuntimeException("Failed to fetch products: " + e.getMessage(), e);
                }
            }

            return allProducts;
        });
    }

    private String buildProductsQuery(String cursor) {
        String afterClause = cursor != null ? ", after: \"" + cursor + "\"" : "";

        return """
            {
              products(first: %d%s) {
                edges {
                  cursor
                  node {
                    id
                    title
                    featuredImage {
                      url
                    }
                    variants(first: 100) {
                      edges {
                        node {
                          id
                          sku
                          inventoryItem {
                            id
                            inventoryLevels(first: 10) {
                              edges {
                                node {
                                  available
                                }
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
                pageInfo {
                  hasNextPage
                }
              }
            }
            """.formatted(PAGE_SIZE, afterClause);
    }

    private List<Product> parseProductNode(JsonObject node) {
        List<Product> products = new ArrayList<>();

        String productId = node.get("id").getAsString();
        String productName = node.get("title").getAsString();

        String imageUrl = "";
        if (node.has("featuredImage") && !node.get("featuredImage").isJsonNull()) {
            imageUrl = node.getAsJsonObject("featuredImage").get("url").getAsString();
        }

        JsonArray variants = node.getAsJsonObject("variants").getAsJsonArray("edges");

        for (JsonElement variantEdge : variants) {
            JsonObject variant = variantEdge.getAsJsonObject().getAsJsonObject("node");

            String sku = "";
            if (variant.has("sku") && !variant.get("sku").isJsonNull()) {
                sku = variant.get("sku").getAsString();
            }

            String inventoryItemId = "";
            int totalInventory = 0;

            if (variant.has("inventoryItem") && !variant.get("inventoryItem").isJsonNull()) {
                JsonObject inventoryItem = variant.getAsJsonObject("inventoryItem");
                inventoryItemId = inventoryItem.get("id").getAsString();

                JsonArray inventoryLevels = inventoryItem.getAsJsonObject("inventoryLevels")
                        .getAsJsonArray("edges");

                for (JsonElement levelEdge : inventoryLevels) {
                    JsonObject level = levelEdge.getAsJsonObject().getAsJsonObject("node");
                    if (level.has("available") && !level.get("available").isJsonNull()) {
                        totalInventory += level.get("available").getAsInt();
                    }
                }
            }

            products.add(new Product(
                    productId,
                    imageUrl,
                    productName,
                    sku,
                    totalInventory,
                    inventoryItemId
            ));
        }

        return products;
    }

    private JsonObject executeGraphQL(String query) throws Exception {
        String url = String.format("https://%s/admin/api/%s/graphql.json",
                shopDomain, API_VERSION);

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("query", query);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("X-Shopify-Access-Token", accessToken)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                .timeout(Duration.ofSeconds(30))
                .build();

        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("API request failed with status: " + response.statusCode());
        }

        return gson.fromJson(response.body(), JsonObject.class);
    }

    /**
     * Validates that the configured Shopify credentials are working.
     */
    public CompletableFuture<Boolean> validateCredentials() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String query = "{ shop { name } }";
                JsonObject response = executeGraphQL(query);
                return response.has("data") &&
                       response.getAsJsonObject("data").has("shop");
            } catch (Exception e) {
                return false;
            }
        });
    }
}
