package io.github.chitralabs.sheetz.benchmarks.model;

/**
 * Plain POJO for Apache POI and FastExcel (no annotations needed — manual mapping).
 */
public class ProductPoi {

    public String name;
    public Double price;
    public Boolean inStock;
    public String category;
    public Integer quantity;

    public ProductPoi() {}

    public ProductPoi(String name, Double price, Boolean inStock, String category, Integer quantity) {
        this.name = name;
        this.price = price;
        this.inStock = inStock;
        this.category = category;
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return String.format("Product{name='%s', price=%.2f, inStock=%s, category='%s', quantity=%d}",
                name, price, inStock, category, quantity);
    }
}
