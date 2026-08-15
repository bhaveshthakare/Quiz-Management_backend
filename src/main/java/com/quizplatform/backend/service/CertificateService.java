package com.quizplatform.backend.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfWriter;
import com.quizplatform.backend.entity.Attempt;
import com.quizplatform.backend.entity.Certificate;
import com.quizplatform.backend.exception.ApiException;
import com.quizplatform.backend.repository.CertificateRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class CertificateService {

    private final CertificateRepository certificateRepository;

    public CertificateService(CertificateRepository certificateRepository) {
        this.certificateRepository = certificateRepository;
    }

    @Transactional
    public Certificate issue(Attempt attempt) {
        Certificate certificate = certificateRepository.save(Certificate.builder()
                .attempt(attempt)
                .user(attempt.getUser())
                .quiz(attempt.getQuiz())
                .certificateUrl("/api/certificates/" + attempt.getId() + "/download")
                .build());
        return certificate;
    }

    @Transactional(readOnly = true)
    public Optional<Certificate> findByAttempt(Long attemptId) {
        return certificateRepository.findByAttemptId(attemptId);
    }

    @Transactional(readOnly = true)
    public byte[] download(Long attemptId) {
        Certificate certificate = certificateRepository.findByAttemptId(attemptId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Certificate not found"));
        return renderPdf(certificate);
    }

    private byte[] renderPdf(Certificate certificate) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate(), 50, 50, 50, 50);
        PdfWriter.getInstance(document, out);
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 32, new Color(0x3C, 0x6C, 0xF0));
        Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 16);
        Font nameFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 26);
        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 13);
        Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 11, new Color(0x64, 0x74, 0x8B));

        Paragraph title = new Paragraph("CERTIFICATE OF COMPLETION", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(8);
        document.add(title);

        Paragraph sub = new Paragraph("Quiz Management & Online Assessment Platform", subFont);
        sub.setAlignment(Element.ALIGN_CENTER);
        sub.setSpacingAfter(40);
        document.add(sub);

        Paragraph body = new Paragraph("This certificate is proudly presented to", bodyFont);
        body.setAlignment(Element.ALIGN_CENTER);
        document.add(body);

        Paragraph name = new Paragraph(certificate.getUser().getUserName(), nameFont);
        name.setAlignment(Element.ALIGN_CENTER);
        name.setSpacingAfter(6);
        document.add(name);

        Paragraph body2 = new Paragraph("for successfully completing the quiz", bodyFont);
        body2.setAlignment(Element.ALIGN_CENTER);
        document.add(body2);

        Paragraph quiz = new Paragraph(certificate.getQuiz().getTitle(),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18));
        quiz.setAlignment(Element.ALIGN_CENTER);
        quiz.setSpacingAfter(10);
        document.add(quiz);

        Paragraph score = new Paragraph("Score: " + certificate.getAttempt().getPercentage().intValue() + "%",
                bodyFont);
        score.setAlignment(Element.ALIGN_CENTER);
        score.setSpacingAfter(50);
        document.add(score);

        Paragraph issued = new Paragraph("Issued on "
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")), smallFont);
        issued.setAlignment(Element.ALIGN_CENTER);
        document.add(issued);

        Paragraph id = new Paragraph("Certificate ID: QP-" + certificate.getId(), smallFont);
        id.setAlignment(Element.ALIGN_CENTER);
        document.add(id);

        document.close();
        return out.toByteArray();
    }
}