package com.quizplatform.backend.service;

import com.quizplatform.backend.dto.CategoryDtos.*;
import com.quizplatform.backend.entity.Category;
import com.quizplatform.backend.exception.ApiException;
import com.quizplatform.backend.repository.CategoryRepository;
import com.quizplatform.backend.repository.QuizRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final QuizRepository quizRepository;

    public CategoryService(CategoryRepository categoryRepository, QuizRepository quizRepository) {
        this.categoryRepository = categoryRepository;
        this.quizRepository = quizRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> list() {
        return categoryRepository.findAll().stream()
                .map(c -> new CategoryResponse(c.getId(), c.getName(), c.getDescription(), quizCount(c)))
                .toList();
    }

    @Transactional
    public CategoryResponse create(CategoryRequest req) {
        if (categoryRepository.existsByNameIgnoreCase(req.name())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "A category with this name already exists");
        }
        Category c = categoryRepository.save(Category.builder()
                .name(req.name().trim())
                .description(req.description())
                .build());
        return new CategoryResponse(c.getId(), c.getName(), c.getDescription(), 0);
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest req) {
        Category c = find(id);
        categoryRepository.findByNameIgnoreCase(req.name())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "A category with this name already exists");
                });
        c.setName(req.name().trim());
        c.setDescription(req.description());
        categoryRepository.save(c);
        return new CategoryResponse(c.getId(), c.getName(), c.getDescription(), quizCount(c));
    }

    @Transactional
    public void delete(Long id) {
        Category c = find(id);
        if (quizCount(c) > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only empty categories can be deleted");
        }
        categoryRepository.delete(c);
    }

    private long quizCount(Category c) {
        return quizRepository.findAll().stream().filter(q -> q.getCategory().getId().equals(c.getId())).count();
    }

    private Category find(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Category not found"));
    }
}