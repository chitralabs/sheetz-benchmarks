package io.github.chitralabs.sheetz.benchmarks.model;

import com.poiji.annotation.ExcelCellName;
import io.github.chitralabs.sheetz.annotation.Column;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared model used by Sheetz (via @Column) and Poiji (via @ExcelCellName).
 * Both annotation systems coexist on the same fields.
 */
public class Product {

    @Column("Product Name")
    @ExcelCellName("Product Name")
    public String name;

    @Column("Price")
    @ExcelCellName("Price")
    public Double price;

    @Column("In Stock")
    @ExcelCellName("In Stock")
    public Boolean inStock;

    @Column("Category")
    @ExcelCellName("Category")
    public String category;

    @Column("Quantity")
    @ExcelCellName("Quantity")
    public Integer quantity;

    public Product() {}

    public Product(String name, Double price, Boolean inStock, String category, Integer quantity) {
        this.name = name;
        this.price = price;
        this.inStock = inStock;
        this.category = category;
        this.quantity = quantity;
    }

    public static List<Product> sampleData() {
        List<Product> products = new ArrayList<>();
        products.add(new Product("Laptop Pro 15", 1299.99, true, "Electronics", 45));
        products.add(new Product("Wireless Mouse", 29.99, true, "Accessories", 230));
        products.add(new Product("USB-C Hub", 49.99, false, "Accessories", 0));
        products.add(new Product("Mechanical Keyboard", 89.99, true, "Accessories", 120));
        products.add(new Product("4K Monitor", 599.99, true, "Electronics", 35));
        products.add(new Product("Webcam HD", 79.99, false, "Electronics", 0));
        products.add(new Product("Laptop Stand", 39.99, true, "Furniture", 85));
        products.add(new Product("Desk Lamp", 24.99, true, "Furniture", 150));
        products.add(new Product("Noise Cancelling Headphones", 249.99, true, "Audio", 60));
        products.add(new Product("Portable Speaker", 59.99, false, "Audio", 0));
        return products;
    }

    @Override
    public String toString() {
        return String.format("Product{name='%s', price=%.2f, inStock=%s, category='%s', quantity=%d}",
                name, price, inStock, category, quantity);
    }
}
