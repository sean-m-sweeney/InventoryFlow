package com.inventoryflow;

/**
 * Launcher class for jpackage compatibility.
 * JavaFX requires this indirection when packaged in classpath mode.
 */
public class Launcher {
    public static void main(String[] args) {
        App.main(args);
    }
}
