package com.anuramkalyan.farmdairy.service;

import com.anuramkalyan.farmdairy.dto.ProductRequest;
import com.anuramkalyan.farmdairy.dto.ProductResponse;
import com.anuramkalyan.farmdairy.entity.Product;
import com.anuramkalyan.farmdairy.repository.ProductRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream().map(this::toResponse).toList();
    }

    public ProductResponse getProductById(Long id) {
        Product product = findProductOrThrow(id);
        return toResponse(product);
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        boolean available = request.available() == null || request.available();

        Product product = new Product(
                request.name(),
                request.description(),
                request.category(),
                request.price(),
                request.unit(),
                available
        );

        Product savedProduct = productRepository.save(product);
        return toResponse(product);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = findProductOrThrow(id);

        product.setName(request.name());
        product.setDescription(request.description());
        product.setCategory(request.category());
        product.setPrice(request.price());
        product.setUnit(request.unit());

        if(request.available() != null) {
            product.setAvailable(request.available());
        }

        Product savedProduct = productRepository.save(product);
        return toResponse(savedProduct);
    }

    @Transactional
    public void deleteProduct() {

    }

    private Product findProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found with id: " + id
                ));
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getCategory(),
                product.getPrice(),
                product.getUnit(),
                product.isAvailable(),
                product.getCreatedAt()
        );
    }
}
