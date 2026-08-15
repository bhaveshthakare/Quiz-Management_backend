package com.quizplatform.backend.entity;

import com.quizplatform.backend.enums.Difficulty;
import com.quizplatform.backend.enums.QuizStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "quizzes")
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('EASY','INTERMEDIATE','HARD')")
    private Difficulty difficulty;

    @Column(nullable = false)
    private Integer duration;

    @Column(name = "passing_score", nullable = false)
    private Integer passingScore;

    @Column(name = "max_attempts", nullable = false)
    private Integer maxAttempts;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('DRAFT','PUBLISHED','UNPUBLISHED')")
    private QuizStatus status;

    @Column(length = 255)
    private String thumbnail;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "negative_marking", nullable = false)
    private Boolean negativeMarking;

    @Column(name = "negative_mark_value", nullable = false, precision = 4, scale = 2)
    private BigDecimal negativeMarkValue;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", updatable = false, insertable = false)
    private LocalDateTime updatedAt;

    public Quiz() {
    }

    public Quiz(Long id, String title, String description, Category category, Difficulty difficulty,
                Integer duration, Integer passingScore, Integer maxAttempts, QuizStatus status,
                String thumbnail, LocalDateTime startDate, LocalDateTime endDate,
                Boolean negativeMarking, BigDecimal negativeMarkValue,
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.difficulty = difficulty;
        this.duration = duration;
        this.passingScore = passingScore;
        this.maxAttempts = maxAttempts;
        this.status = status;
        this.thumbnail = thumbnail;
        this.startDate = startDate;
        this.endDate = endDate;
        this.negativeMarking = negativeMarking;
        this.negativeMarkValue = negativeMarkValue;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getPassingScore() {
        return passingScore;
    }

    public void setPassingScore(Integer passingScore) {
        this.passingScore = passingScore;
    }

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(Integer maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public QuizStatus getStatus() {
        return status;
    }

    public void setStatus(QuizStatus status) {
        this.status = status;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public Boolean getNegativeMarking() {
        return negativeMarking;
    }

    public void setNegativeMarking(Boolean negativeMarking) {
        this.negativeMarking = negativeMarking;
    }

    public BigDecimal getNegativeMarkValue() {
        return negativeMarkValue;
    }

    public void setNegativeMarkValue(BigDecimal negativeMarkValue) {
        this.negativeMarkValue = negativeMarkValue;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String title;
        private String description;
        private Category category;
        private Difficulty difficulty;
        private Integer duration;
        private Integer passingScore;
        private Integer maxAttempts;
        private QuizStatus status;
        private String thumbnail;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Boolean negativeMarking;
        private BigDecimal negativeMarkValue;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder category(Category category) {
            this.category = category;
            return this;
        }

        public Builder difficulty(Difficulty difficulty) {
            this.difficulty = difficulty;
            return this;
        }

        public Builder duration(Integer duration) {
            this.duration = duration;
            return this;
        }

        public Builder passingScore(Integer passingScore) {
            this.passingScore = passingScore;
            return this;
        }

        public Builder maxAttempts(Integer maxAttempts) {
            this.maxAttempts = maxAttempts;
            return this;
        }

        public Builder status(QuizStatus status) {
            this.status = status;
            return this;
        }

        public Builder thumbnail(String thumbnail) {
            this.thumbnail = thumbnail;
            return this;
        }

        public Builder startDate(LocalDateTime startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder endDate(LocalDateTime endDate) {
            this.endDate = endDate;
            return this;
        }

        public Builder negativeMarking(Boolean negativeMarking) {
            this.negativeMarking = negativeMarking;
            return this;
        }

        public Builder negativeMarkValue(BigDecimal negativeMarkValue) {
            this.negativeMarkValue = negativeMarkValue;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Quiz build() {
            return new Quiz(id, title, description, category, difficulty, duration, passingScore,
                    maxAttempts, status, thumbnail, startDate, endDate, negativeMarking,
                    negativeMarkValue, createdAt, updatedAt);
        }
    }

    @PrePersist
    void onPersist() {
        if (difficulty == null) difficulty = Difficulty.EASY;
        if (status == null) status = QuizStatus.DRAFT;
        if (passingScore == null) passingScore = 60;
        if (maxAttempts == null) maxAttempts = 1;
        if (negativeMarking == null) negativeMarking = false;
        if (negativeMarkValue == null) negativeMarkValue = BigDecimal.ZERO;
    }
}