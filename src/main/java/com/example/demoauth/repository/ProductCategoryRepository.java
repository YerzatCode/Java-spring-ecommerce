package com.example.demoauth.repository;


import com.example.demoauth.models.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.CrossOrigin;

/**
 * @author Gaëtan Bloch
 * Created on 05/05/2020
 */
@Repository
@CrossOrigin("http://localhost:4200")
@RepositoryRestResource(path = "product-categories", collectionResourceRel = "product_categories")
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
}
