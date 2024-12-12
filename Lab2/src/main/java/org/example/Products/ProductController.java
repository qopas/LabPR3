package org.example.Products;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    // Create a new product
    @PostMapping
    public ResponseEntity<String> createProduct(@RequestBody Product product) {
        Product createdProduct = productService.createProduct(product);
        return ResponseEntity.ok("Product with id " + createdProduct.getId() + " was created successfully.");
    }

    // Get all products
    @GetMapping
    public List<Product> getAllProducts(@RequestParam(name = "offset", defaultValue = "0") int offset, @RequestParam(name = "limit", defaultValue = "5") int limit) {
        return productService.getAllProducts(offset, limit);
    }

    // Get a product by ID
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable("id") Long id) {
        Optional<Product> product = productService.getProductById(id);
        return product.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Update a product
    @PutMapping("/{id}")
    public ResponseEntity<String> updateProduct(@PathVariable("id") Long id, @RequestBody ProductUpdateDTO productDetails) {
        Product updatedProduct = productService.updateProduct(id, productDetails);
        return ResponseEntity.ok("Product with id " + updatedProduct.getId() + " was updated successfully.");
    }

    // Delete a product
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable("id") Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok("Product with id " + id + " was deleted");
    }
    // File upload
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // Convert the file content to a String
            String content = new String(file.getBytes());

            // Use Jackson ObjectMapper to map the JSON content to a list of Product objects
            ObjectMapper objectMapper = new ObjectMapper();
            Product product = objectMapper.readValue(content, Product.class);

            // Save the products to the database
            productService.createProduct(product);

            return ResponseEntity.ok("File uploaded and products saved successfully");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to process file");
        }
    }
}
