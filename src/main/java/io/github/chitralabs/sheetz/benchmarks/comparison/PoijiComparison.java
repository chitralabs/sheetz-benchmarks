package io.github.chitralabs.sheetz.benchmarks.comparison;

import com.poiji.bind.Poiji;
import io.github.chitralabs.sheetz.Sheetz;
import io.github.chitralabs.sheetz.benchmarks.model.Product;

import java.io.File;
import java.util.List;

/**
 * Poiji: Read-only demo in ~15 lines of actual code.
 * Uses Sheetz to write the file first, then Poiji to read it.
 */
public class PoijiComparison {

    public static void main(String[] args) throws Exception {
        new File("output").mkdirs();
        String filePath = "output/poiji_products.xlsx";

        // Write test file using Sheetz (Poiji is read-only)
        Sheetz.write(Product.sampleData(), filePath);

        // ── Read ───────────────────────────────────────────
        List<Product> result = Poiji.fromExcel(new File(filePath), Product.class);

        // ── Print ──────────────────────────────────────────
        System.out.println("=== Poiji (read-only) ===");
        System.out.println("Read " + result.size() + " products:");
        result.forEach(p -> System.out.printf("  %-30s $%8.2f  inStock=%-5s  qty=%d%n",
                p.name, p.price, p.inStock, p.quantity));
    }
}
