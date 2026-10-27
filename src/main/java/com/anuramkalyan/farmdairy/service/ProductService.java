package com.anuramkalyan.farmdairy.service;

import com.anuramkalyan.farmdairy.dto.ProductRequest;
import com.anuramkalyan.farmdairy.dto.ProductResponse;
import com.anuramkalyan.farmdairy.entity.Product;
import com.anuramkalyan.farmdairy.repository.CartItemRepository;
import com.anuramkalyan.farmdairy.repository.ProductRepository;
import com.anuramkalyan.farmdairy.repository.SubscriptionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final SubscriptionRepository subscriptionRepository;

    public ProductService(
            ProductRepository productRepository,
            CartItemRepository cartItemRepository,
            SubscriptionRepository subscriptionRepository) {
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
        this.subscriptionRepository = subscriptionRepository;
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
                normalizeCategory(request.category()),
                request.price(),
                request.unit(),
                available
        );

        Product savedProduct = productRepository.save(product);
        return toResponse(savedProduct);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = findProductOrThrow(id);

        product.setName(request.name());
        product.setDescription(request.description());
        product.setCategory(normalizeCategory(request.category()));
        product.setPrice(request.price());
        product.setUnit(request.unit());

        if(request.available() != null) {
            product.setAvailable(request.available());
        }

        Product savedProduct = productRepository.save(product);
        return toResponse(savedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = findProductOrThrow(id);

        if (cartItemRepository.existsByProduct_Id(id)
                || subscriptionRepository.existsByProduct_Id(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Product is in use by carts or subscriptions. "
                            + "Set available=false instead of deleting it."
            );
        }

        productRepository.delete(product);
    }

    private Product findProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found with id: " + id
                ));
    }

    private String normalizeCategory(String category) {
        return category.trim().toUpperCase(Locale.ROOT);
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

    public Page<ProductResponse> searchProducts(String category, Boolean available, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        String normalizedCategory = category == null || category.isBlank() ? null : normalizeCategory(category);

        return productRepository.search(normalizedCategory, available, pageable).map(this::toResponse);
    }
}
