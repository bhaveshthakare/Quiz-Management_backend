package com.quizplatform.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "options")
public class Option {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id")
    private Question question;

    @Column(name = "option_text", nullable = false, length = 255)
    private String optionText;

    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect;

    public Option() {
    }

    public Option(Long id, Question question, String optionText, Boolean isCorrect) {
        this.id = id;
        this.question = question;
        this.optionText = optionText;
        this.isCorrect = isCorrect;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(Boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Question question;
        private String optionText;
        private Boolean isCorrect;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder question(Question question) {
            this.question = question;
            return this;
        }

        public Builder optionText(String optionText) {
            this.optionText = optionText;
            return this;
        }

        public Builder isCorrect(Boolean isCorrect) {
            this.isCorrect = isCorrect;
            return this;
        }

        public Option build() {
            return new Option(id, question, optionText, isCorrect);
        }
    }

    @PrePersist
    void onPersist() {
        if (isCorrect == null) isCorrect = false;
    }
}