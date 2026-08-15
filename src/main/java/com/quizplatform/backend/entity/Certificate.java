package com.quizplatform.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "certificates")
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id")
    private Attempt attempt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @Column(name = "certificate_url", nullable = false)
    private String certificateUrl;

    @Column(name = "issued_at", updatable = false, insertable = false)
    private LocalDateTime issuedAt;

    public Certificate() {
    }

    public Certificate(Long id, Attempt attempt, User user, Quiz quiz,
                       String certificateUrl, LocalDateTime issuedAt) {
        this.id = id;
        this.attempt = attempt;
        this.user = user;
        this.quiz = quiz;
        this.certificateUrl = certificateUrl;
        this.issuedAt = issuedAt;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public String getCertificateUrl() {
        return certificateUrl;
    }

    public void setCertificateUrl(String certificateUrl) {
        this.certificateUrl = certificateUrl;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Attempt attempt;
        private User user;
        private Quiz quiz;
        private String certificateUrl;
        private LocalDateTime issuedAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder attempt(Attempt attempt) {
            this.attempt = attempt;
            return this;
        }

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public Builder quiz(Quiz quiz) {
            this.quiz = quiz;
            return this;
        }

        public Builder certificateUrl(String certificateUrl) {
            this.certificateUrl = certificateUrl;
            return this;
        }

        public Builder issuedAt(LocalDateTime issuedAt) {
            this.issuedAt = issuedAt;
            return this;
        }

        public Certificate build() {
            return new Certificate(id, attempt, user, quiz, certificateUrl, issuedAt);
        }
    }
}