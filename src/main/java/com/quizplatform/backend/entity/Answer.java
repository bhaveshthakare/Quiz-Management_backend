package com.quizplatform.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "answers")
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id")
    private Attempt attempt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id")
    private Question question;

    @Column(name = "selected_option_id")
    private Long selectedOptionId;

    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect;

    @Column(name = "question_position", nullable = false)
    private Integer questionPosition;

    @Column(name = "option_order", columnDefinition = "TEXT")
    private String optionOrder;

    public Answer() {
    }

    public Answer(Long id, Attempt attempt, Question question, Long selectedOptionId,
                  Boolean isCorrect, Integer questionPosition, String optionOrder) {
        this.id = id;
        this.attempt = attempt;
        this.question = question;
        this.selectedOptionId = selectedOptionId;
        this.isCorrect = isCorrect;
        this.questionPosition = questionPosition;
        this.optionOrder = optionOrder;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Attempt getAttempt() {
        return attempt;
    }

    public void setAttempt(Attempt attempt) {
        this.attempt = attempt;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public Long getSelectedOptionId() {
        return selectedOptionId;
    }

    public void setSelectedOptionId(Long selectedOptionId) {
        this.selectedOptionId = selectedOptionId;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(Boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    public Integer getQuestionPosition() {
        return questionPosition;
    }

    public void setQuestionPosition(Integer questionPosition) {
        this.questionPosition = questionPosition;
    }

    public String getOptionOrder() {
        return optionOrder;
    }

    public void setOptionOrder(String optionOrder) {
        this.optionOrder = optionOrder;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Attempt attempt;
        private Question question;
        private Long selectedOptionId;
        private Boolean isCorrect;
        private Integer questionPosition;
        private String optionOrder;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder attempt(Attempt attempt) {
            this.attempt = attempt;
            return this;
        }

        public Builder question(Question question) {
            this.question = question;
            return this;
        }

        public Builder selectedOptionId(Long selectedOptionId) {
            this.selectedOptionId = selectedOptionId;
            return this;
        }

        public Builder isCorrect(Boolean isCorrect) {
            this.isCorrect = isCorrect;
            return this;
        }

        public Builder questionPosition(Integer questionPosition) {
            this.questionPosition = questionPosition;
            return this;
        }

        public Builder optionOrder(String optionOrder) {
            this.optionOrder = optionOrder;
            return this;
        }

        public Answer build() {
            return new Answer(id, attempt, question, selectedOptionId, isCorrect,
                    questionPosition, optionOrder);
        }
    }

    @PrePersist
    void onPersist() {
        if (isCorrect == null) isCorrect = false;
        if (questionPosition == null) questionPosition = 0;
    }
}