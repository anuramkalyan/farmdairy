package com.anuramkalyan.farmdairy.repository;

import com.anuramkalyan.farmdairy.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

}
