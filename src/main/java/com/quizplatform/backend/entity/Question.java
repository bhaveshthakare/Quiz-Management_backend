package com.quizplatform.backend.entity;

import com.quizplatform.backend.enums.Difficulty;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(nullable = false)
    private Integer marks;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('EASY','INTERMEDIATE','HARD')")
    private Difficulty difficulty;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private List<Option> options = new ArrayList<>();

    public Question() {
    }

    public Question(Long id, Quiz quiz, String questionText, Integer marks, String explanation,
                    Difficulty difficulty, LocalDateTime createdAt, List<Option> options) {
        this.id = id;
        this.quiz = quiz;
        this.questionText = questionText;
        this.marks = marks;
        this.explanation = explanation;
        this.difficulty = difficulty;
        this.createdAt = createdAt;
        this.options = options == null ? new ArrayList<>() : options;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public Integer getMarks() {
        return marks;
    }

    public void setMarks(Integer marks) {
        this.marks = marks;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<Option> options) {
        this.options = options == null ? new ArrayList<>() : options;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Quiz quiz;
        private String questionText;
        private Integer marks;
        private String explanation;
        private Difficulty difficulty;
        private LocalDateTime createdAt;
        private List<Option> options = new ArrayList<>();

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder quiz(Quiz quiz) {
            this.quiz = quiz;
            return this;
        }

        public Builder questionText(String questionText) {
            this.questionText = questionText;
            return this;
        }

        public Builder marks(Integer marks) {
            this.marks = marks;
            return this;
        }

        public Builder explanation(String explanation) {
            this.explanation = explanation;
            return this;
        }

        public Builder difficulty(Difficulty difficulty) {
            this.difficulty = difficulty;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder options(List<Option> options) {
            this.options = options == null ? new ArrayList<>() : options;
            return this;
        }

        public Question build() {
            return new Question(id, quiz, questionText, marks, explanation, difficulty, createdAt, options);
        }
    }

    @PrePersist
    void onPersist() {
        if (marks == null) marks = 1;
        if (difficulty == null) difficulty = Difficulty.EASY;
    }
}