package com.example.repository;

import com.example.model.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CategoryRepository {
    private static final Logger logger = LoggerFactory.getLogger(CategoryRepository.class);

    private final Map<Long, Category> categories = new HashMap<>();
    private final Map<String, Category> categoriesByName = new HashMap<>();
    private Long nextId = 1L;

    public Category save(Category category) {
        if (category.getId() == null) {
            category.setId(nextId++);
            logger.info("Ny kategori skapad med ID: {}", category.getId());
        } else {
            logger.info("Kategori uppdaterad med ID: {}", category.getId());
        }

        categories.put(category.getId(), category);
        categoriesByName.put(category.getName().toLowerCase(), category);
        return category;
    }

    public Optional<Category> findById(Long id) {
        return Optional.ofNullable(categories.get(id));
    }

    public Optional<Category> findByName(String name) {
        return Optional.ofNullable(categoriesByName.get(name.toLowerCase()));
    }

    public List<Category> findAll() {
        return new ArrayList<>(categories.values());
    }

    public boolean existsByName(String name) {
        return categoriesByName.containsKey(name.toLowerCase());
    }

    public void delete(Category category) {
        categories.remove(category.getId());
        categoriesByName.remove(category.getName().toLowerCase());
        logger.info("Kategori raderad: {}", category.getName());
    }

    public long count() {
        return categories.size();
    }

    public void deleteAll() {
        categories.clear();
        categoriesByName.clear();
        nextId = 1L;
        logger.info("Alla kategorier raderade");
    }
}