module com.inventoryflow {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.net.http;
    requires com.google.gson;
    opens com.inventoryflow to javafx.fxml;
    opens com.inventoryflow.controller to javafx.fxml;
    opens com.inventoryflow.model to com.google.gson, javafx.base;

    exports com.inventoryflow;
    exports com.inventoryflow.controller;
    exports com.inventoryflow.model;
    exports com.inventoryflow.service;
    exports com.inventoryflow.util;
}
