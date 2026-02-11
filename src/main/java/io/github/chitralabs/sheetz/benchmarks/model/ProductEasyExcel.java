package io.github.chitralabs.sheetz.benchmarks.model;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * EasyExcel-specific model with @ExcelProperty annotations.
 * EasyExcel requires private fields with getters/setters.
 */
public class ProductEasyExcel {

    @ExcelProperty("Product Name")
    private String name;

    @ExcelProperty("Price")
    private Double price;

    @ExcelProperty("In Stock")
    private Boolean inStock;

    @ExcelProperty("Category")
    private String category;

    @ExcelProperty("Quantity")
    private Integer quantity;

    public ProductEasyExcel() {}

    public ProductEasyExcel(String name, Double price, Boolean inStock, String category, Integer quantity) {
        this.name = name;
        this.price = price;
        this.inStock = inStock;
        this.category = category;
        this.quantity = quantity;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Boolean getInStock() { return inStock; }
    public void setInStock(Boolean inStock) { this.inStock = inStock; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    @Override
    public String toString() {
        return String.format("Product{name='%s', price=%.2f, inStock=%s, category='%s', quantity=%d}",
                name, price, inStock, category, quantity);
    }
}
