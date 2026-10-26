package com.anuramkalyan.farmdairy.repository;

import com.anuramkalyan.farmdairy.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("""
        SELECT p FROM Product p 
        WHERE (:category IS NULL or p.category = :category)
        AND (:available IS NULL or p.available = :available)
    """)
    Page<Product> search(@Param("category") String category,
                         @Param("available") Boolean available,
                         Pageable pageable);
}
