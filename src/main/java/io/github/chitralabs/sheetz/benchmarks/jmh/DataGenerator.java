package io.github.chitralabs.sheetz.benchmarks.jmh;

import io.github.chitralabs.sheetz.Sheetz;
import io.github.chitralabs.sheetz.benchmarks.model.Product;
import io.github.chitralabs.sheetz.benchmarks.model.ProductEasyExcel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Generates test data for benchmarks.
 */
public class DataGenerator {

    private static final String[] PRODUCT_NAMES = {
            "Laptop", "Mouse", "Keyboard", "Monitor", "Headphones",
            "Webcam", "Speaker", "Tablet", "Phone", "Charger",
            "Cable", "Hub", "Stand", "Lamp", "Chair",
            "Desk", "Microphone", "Camera", "Printer", "Scanner"
    };

    private static final String[] CATEGORIES = {
            "Electronics", "Accessories", "Audio", "Furniture", "Office"
    };

    public static List<Product> generateProducts(int count) {
        List<Product> products = new ArrayList<>(count);
        Random random = new Random(42); // fixed seed for reproducibility
        for (int i = 0; i < count; i++) {
            products.add(new Product(
                    PRODUCT_NAMES[random.nextInt(PRODUCT_NAMES.length)] + " Model-" + i,
                    Math.round((10.0 + random.nextDouble() * 990.0) * 100.0) / 100.0,
                    random.nextBoolean(),
                    CATEGORIES[random.nextInt(CATEGORIES.length)],
                    random.nextInt(500)
            ));
        }
        return products;
    }

    public static List<ProductEasyExcel> toEasyExcel(List<Product> products) {
        return products.stream()
                .map(p -> new ProductEasyExcel(p.name, p.price, p.inStock, p.category, p.quantity))
                .collect(Collectors.toList());
    }

    public static void generateTestFile(String path, int rowCount) throws Exception {
        new File(path).getParentFile().mkdirs();
        Sheetz.write(generateProducts(rowCount), path);
    }
}
