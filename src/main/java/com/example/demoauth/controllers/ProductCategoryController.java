package com.example.demoauth.controllers;

import com.example.demoauth.models.ProductCategory;
import com.example.demoauth.repository.ProductCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProductCategoryController {

    private final ProductCategoryRepository categoryRepository;

    @Autowired
    public ProductCategoryController(ProductCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // Получить все категории продуктов
    @GetMapping
    public ResponseEntity<List<ProductCategory>> getAllCategories() {
        List<ProductCategory> categories = categoryRepository.findAll();
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    // Получить категорию по ID
    @GetMapping("/{categoryId}")
    public ResponseEntity<ProductCategory> getCategoryById(@PathVariable Long categoryId) {
        Optional<ProductCategory> category = categoryRepository.findById(categoryId);
        return category.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Создать новую категорию
    @PostMapping
    public ResponseEntity<ProductCategory> createCategory(@RequestBody ProductCategory category) {
        ProductCategory newCategory = categoryRepository.save(category);
        return new ResponseEntity<>(newCategory, HttpStatus.CREATED);
    }

    // Обновить существующую категорию
    @PutMapping("/{categoryId}")
    public ResponseEntity<ProductCategory> updateCategory(@PathVariable Long categoryId, @RequestBody ProductCategory categoryDetails) {
        Optional<ProductCategory> optionalCategory = categoryRepository.findById(categoryId);
        if (optionalCategory.isPresent()) {
            ProductCategory category = optionalCategory.get();
            category.setCategoryName(categoryDetails.getCategoryName());
            // Другие поля также могут быть обновлены
            // ...
            ProductCategory updatedCategory = categoryRepository.save(category);
            return new ResponseEntity<>(updatedCategory, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Удалить категорию по ID
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long categoryId) {
        Optional<ProductCategory> category = categoryRepository.findById(categoryId);
        if (category.isPresent()) {
            categoryRepository.delete(category.get());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
