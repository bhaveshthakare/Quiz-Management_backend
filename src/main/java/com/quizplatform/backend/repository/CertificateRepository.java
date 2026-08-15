package com.quizplatform.backend.repository;

import com.quizplatform.backend.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    Optional<Certificate> findByAttemptId(Long attemptId);
}