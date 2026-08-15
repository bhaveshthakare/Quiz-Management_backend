package com.quizplatform.backend.entity;

import com.quizplatform.backend.enums.AttemptStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "attempts")
public class Attempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private Integer score;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal percentage;

    @Column(name = "correct_answers", nullable = false)
    private Integer correctAnswers;

    @Column(name = "incorrect_answers", nullable = false)
    private Integer incorrectAnswers;

    @Column(nullable = false)
    private Integer unanswered;

    @Column(name = "time_taken")
    private Integer timeTaken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('IN_PROGRESS','PASSED','FAILED')")
    private AttemptStatus status;

    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("questionPosition ASC")
    private List<Answer> answers = new ArrayList<>();

    public Attempt() {
    }

    public Attempt(Long id, Quiz quiz, User user, Integer score, BigDecimal percentage,
                   Integer correctAnswers, Integer incorrectAnswers, Integer unanswered,
                   Integer timeTaken, AttemptStatus status, LocalDateTime startedAt,
                   LocalDateTime completedAt, List<Answer> answers) {
        this.id = id;
        this.quiz = quiz;
        this.user = user;
        this.score = score;
        this.percentage = percentage;
        this.correctAnswers = correctAnswers;
        this.incorrectAnswers = incorrectAnswers;
        this.unanswered = unanswered;
        this.timeTaken = timeTaken;
        this.status = status;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.answers = answers == null ? new ArrayList<>() : answers;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }

    public Integer getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(Integer correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public Integer getIncorrectAnswers() {
        return incorrectAnswers;
    }

    public void setIncorrectAnswers(Integer incorrectAnswers) {
        this.incorrectAnswers = incorrectAnswers;
    }

    public Integer getUnanswered() {
        return unanswered;
    }

    public void setUnanswered(Integer unanswered) {
        this.unanswered = unanswered;
    }

    public Integer getTimeTaken() {
        return timeTaken;
    }

    public void setTimeTaken(Integer timeTaken) {
        this.timeTaken = timeTaken;
    }

    public AttemptStatus getStatus() {
        return status;
    }

    public void setStatus(AttemptStatus status) {
        this.status = status;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Answer> answers) {
        this.answers = answers == null ? new ArrayList<>() : answers;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Quiz quiz;
        private User user;
        private Integer score;
        private BigDecimal percentage;
        private Integer correctAnswers;
        private Integer incorrectAnswers;
        private Integer unanswered;
        private Integer timeTaken;
        private AttemptStatus status;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private List<Answer> answers = new ArrayList<>();

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder quiz(Quiz quiz) {
            this.quiz = quiz;
            return this;
        }

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public Builder score(Integer score) {
            this.score = score;
            return this;
        }

        public Builder percentage(BigDecimal percentage) {
            this.percentage = percentage;
            return this;
        }

        public Builder correctAnswers(Integer correctAnswers) {
            this.correctAnswers = correctAnswers;
            return this;
        }

        public Builder incorrectAnswers(Integer incorrectAnswers) {
            this.incorrectAnswers = incorrectAnswers;
            return this;
        }

        public Builder unanswered(Integer unanswered) {
            this.unanswered = unanswered;
            return this;
        }

        public Builder timeTaken(Integer timeTaken) {
            this.timeTaken = timeTaken;
            return this;
        }

        public Builder status(AttemptStatus status) {
            this.status = status;
            return this;
        }

        public Builder startedAt(LocalDateTime startedAt) {
            this.startedAt = startedAt;
            return this;
        }

        public Builder completedAt(LocalDateTime completedAt) {
            this.completedAt = completedAt;
            return this;
        }

        public Builder answers(List<Answer> answers) {
            this.answers = answers == null ? new ArrayList<>() : answers;
            return this;
        }

        public Attempt build() {
            return new Attempt(id, quiz, user, score, percentage, correctAnswers, incorrectAnswers,
                    unanswered, timeTaken, status, startedAt, completedAt, answers);
        }
    }

    @PrePersist
    void onPersist() {
        if (score == null) score = 0;
        if (percentage == null) percentage = BigDecimal.ZERO;
        if (correctAnswers == null) correctAnswers = 0;
        if (incorrectAnswers == null) incorrectAnswers = 0;
        if (unanswered == null) unanswered = 0;
        if (status == null) status = AttemptStatus.IN_PROGRESS;
        if (startedAt == null) startedAt = LocalDateTime.now();
    }
}