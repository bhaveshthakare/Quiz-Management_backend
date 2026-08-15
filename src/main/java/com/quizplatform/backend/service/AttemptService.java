package com.quizplatform.backend.service;

import com.quizplatform.backend.dto.AttemptDtos.*;
import com.quizplatform.backend.entity.*;
import com.quizplatform.backend.enums.AttemptStatus;
import com.quizplatform.backend.enums.NotificationType;
import com.quizplatform.backend.enums.QuizStatus;
import com.quizplatform.backend.exception.ApiException;
import com.quizplatform.backend.repository.*;
import com.quizplatform.backend.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AttemptService {

    private final AttemptRepository attemptRepository;
    private final AnswerRepository answerRepository;
    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final CertificateService certificateService;
    private final NotificationService notificationService;
    private final EmailService emailService;

    public AttemptService(AttemptRepository attemptRepository,
                          AnswerRepository answerRepository,
                          QuizRepository quizRepository,
                          UserRepository userRepository,
                          QuestionRepository questionRepository,
                          CertificateService certificateService,
                          NotificationService notificationService,
                          EmailService emailService) {
        this.attemptRepository = attemptRepository;
        this.answerRepository = answerRepository;
        this.quizRepository = quizRepository;
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
        this.certificateService = certificateService;
        this.notificationService = notificationService;
        this.emailService = emailService;
    }

    @Transactional
    public StartAttemptResponse start(Long quizId) {
        Long userId = SecurityUtils.currentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Quiz not found"));
        if (quiz.getStatus() != QuizStatus.PUBLISHED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This quiz is not available");
        }
        LocalDateTime now = LocalDateTime.now();
        if (quiz.getStartDate() != null && now.isBefore(quiz.getStartDate())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This quiz has not started yet");
        }
        if (quiz.getEndDate() != null && now.isAfter(quiz.getEndDate())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This quiz has already ended");
        }

        Optional<Attempt> existing = attemptRepository
                .findFirstByUserAndQuizAndStatusOrderByStartedAtDesc(user, quiz, AttemptStatus.IN_PROGRESS);
        if (existing.isPresent()) {
            return buildSession(existing.get());
        }

        long completed = attemptRepository.findByUserOrderByStartedAtDesc(user).stream()
                .filter(a -> a.getQuiz().getId().equals(quizId))
                .filter(a -> a.getStatus() != AttemptStatus.IN_PROGRESS)
                .count();
        if (completed >= quiz.getMaxAttempts()) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "You have reached the maximum number of attempts (" + quiz.getMaxAttempts() + ") for this quiz");
        }

        List<Question> questions = questionRepository.findByQuizOrderByIdAsc(quiz);
        if (questions.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This quiz has no questions yet");
        }
        Collections.shuffle(questions);

        Attempt attempt = attemptRepository.save(Attempt.builder()
                .quiz(quiz)
                .user(user)
                .score(0)
                .percentage(BigDecimal.ZERO)
                .correctAnswers(0)
                .incorrectAnswers(0)
                .unanswered(questions.size())
                .status(AttemptStatus.IN_PROGRESS)
                .startedAt(now)
                .completedAt(now.plusMinutes(quiz.getDuration()))
                .build());

        int position = 0;
        for (Question q : questions) {
            List<Option> shuffled = new ArrayList<>(q.getOptions());
            Collections.shuffle(shuffled);
            String order = shuffled.stream().map(o -> String.valueOf(o.getId()))
                    .collect(Collectors.joining(","));
            answerRepository.save(Answer.builder()
                    .attempt(attempt)
                    .question(q)
                    .questionPosition(position++)
                    .optionOrder(order)
                    .isCorrect(false)
                    .build());
        }
        return buildSession(attempt);
    }

    @Transactional
    public void saveAnswer(Long attemptId, AnswerRequest req) {
        Attempt attempt = findOwnAttempt(attemptId);
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This attempt is already submitted");
        }
        if (isExpired(attempt)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Time is up, please submit the attempt");
        }
        Answer answer = answerRepository.findByAttemptAndQuestion(attempt, questionRepository
                        .findById(req.questionId())
                        .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Question not found")))
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Question is not part of this attempt"));
        answer.setSelectedOptionId(req.selectedOptionId());
        answerRepository.save(answer);
    }

    @Transactional
    public AttemptResult submit(Long attemptId) {
        Attempt attempt = findOwnAttempt(attemptId);
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            return buildResult(attempt);
        }
        Quiz quiz = attempt.getQuiz();
        boolean expired = isExpired(attempt);

        List<Answer> answers = attempt.getAnswers();
        int correct = 0;
        int incorrect = 0;
        int unanswered = 0;
        BigDecimal score = BigDecimal.ZERO;
        BigDecimal totalMarks = BigDecimal.ZERO;
        BigDecimal negativeValue = quiz.getNegativeMarking() != null && quiz.getNegativeMarking()
                ? (quiz.getNegativeMarkValue() == null ? BigDecimal.ZERO : quiz.getNegativeMarkValue())
                : BigDecimal.ZERO;

        for (Answer a : answers) {
            BigDecimal marks = BigDecimal.valueOf(a.getQuestion().getMarks());
            totalMarks = totalMarks.add(marks);
            if (a.getSelectedOptionId() == null) {
                unanswered++;
                a.setIsCorrect(false);
            } else {
                Option chosen = a.getQuestion().getOptions().stream()
                        .filter(o -> o.getId().equals(a.getSelectedOptionId()))
                        .findFirst().orElse(null);
                if (chosen != null && Boolean.TRUE.equals(chosen.getIsCorrect())) {
                    correct++;
                    a.setIsCorrect(true);
                    score = score.add(marks);
                } else {
                    incorrect++;
                    a.setIsCorrect(false);
                    score = score.subtract(negativeValue);
                }
            }
        }
        score = score.max(BigDecimal.ZERO);
        BigDecimal percentage = totalMarks.compareTo(BigDecimal.ZERO) > 0
                ? score.multiply(BigDecimal.valueOf(100)).divide(totalMarks, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        boolean passed = percentage.compareTo(BigDecimal.valueOf(quiz.getPassingScore())) >= 0;

        LocalDateTime now = LocalDateTime.now();
        attempt.setScore(score.intValue());
        attempt.setPercentage(percentage);
        attempt.setCorrectAnswers(correct);
        attempt.setIncorrectAnswers(incorrect);
        attempt.setUnanswered(unanswered);
        attempt.setTimeTaken((int) Duration.between(attempt.getStartedAt(), now).getSeconds());
        attempt.setStatus(passed ? AttemptStatus.PASSED : AttemptStatus.FAILED);
        attempt.setCompletedAt(now);
        attemptRepository.save(attempt);

        Long certificateId = null;
        if (passed && certificateService.findByAttempt(attempt.getId()).isEmpty()) {
            Certificate certificate = certificateService.issue(attempt);
            certificateId = certificate.getId();
        }
        notificationService.create(attempt.getUser(), NotificationType.RESULT,
                "Your result for \"" + quiz.getTitle() + "\" is " + percentage.intValue() + "% ("
                        + (passed ? "PASSED" : "FAILED") + ")");
        if (certificateId != null) {
            notificationService.create(attempt.getUser(), NotificationType.CERTIFICATE,
                    "Congratulations! You earned a certificate for \"" + quiz.getTitle() + "\".");
        }
        emailService.sendResultEmail(attempt.getUser().getEmail(), attempt.getUser().getUserName(),
                quiz.getTitle(), percentage.intValue() + "%");

        return buildResult(attempt);
    }

    @Transactional(readOnly = true)
    public List<AttemptSummary> myHistory() {
        Long userId = SecurityUtils.currentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
        return attemptRepository.findByUserOrderByStartedAtDesc(user).stream()
                .filter(a -> a.getStatus() != AttemptStatus.IN_PROGRESS)
                .map(a -> toSummary(a, null, null))
                .toList();
    }

    @Transactional(readOnly = true)
    public AttemptResult myResult(Long attemptId) {
        Attempt attempt = findOwnAttempt(attemptId);
        return buildResult(attempt);
    }

    @Transactional(readOnly = true)
    public List<AttemptSummary> adminList() {
        return attemptRepository.findAllByOrderByStartedAtDesc().stream()
                .filter(a -> a.getStatus() != AttemptStatus.IN_PROGRESS)
                .map(a -> toSummary(a, a.getUser().getId(), a.getUser().getUserName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public AttemptResult adminResult(Long attemptId) {
        Attempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Attempt not found"));
        return buildResult(attempt);
    }

    private StartAttemptResponse buildSession(Attempt attempt) {
        List<AttemptQuestion> questions = attempt.getAnswers().stream()
                .sorted(Comparator.comparing(Answer::getQuestionPosition))
                .map(a -> {
                    List<Option> ordered = optionOrder(a.getOptionOrder(), a.getQuestion().getOptions());
                    List<AttemptOption> opts = ordered.stream()
                            .map(o -> new AttemptOption(o.getId(), o.getOptionText()))
                            .toList();
                    return new AttemptQuestion(a.getQuestion().getId(),
                            a.getQuestion().getQuestionText(),
                            a.getQuestion().getDifficulty(),
                            a.getSelectedOptionId(), opts);
                })
                .toList();
        return new StartAttemptResponse(attempt.getId(), attempt.getQuiz().getTitle(),
                attempt.getCompletedAt(), questions);
    }

    private List<Option> optionOrder(String orderCsv, List<Option> options) {
        if (orderCsv == null || orderCsv.isBlank()) {
            return options.stream().sorted(Comparator.comparing(Option::getId)).toList();
        }
        Map<Long, Option> byId = options.stream().collect(Collectors.toMap(Option::getId, o -> o));
        return Arrays.stream(orderCsv.split(","))
                .map(s -> byId.get(Long.valueOf(s.trim())))
                .filter(Objects::nonNull)
                .toList();
    }

    private AttemptSummary toSummary(Attempt a, Long studentId, String studentName) {
        String category = a.getQuiz().getCategory() != null
                ? a.getQuiz().getCategory().getName() : null;
        return new AttemptSummary(a.getId(),
                studentId != null ? studentId : a.getUser().getId(),
                studentName != null ? studentName : a.getUser().getUserName(),
                a.getQuiz().getTitle(), category,
                a.getStartedAt(), a.getCompletedAt(), a.getScore(), a.getPercentage(),
                a.getCorrectAnswers(), a.getIncorrectAnswers(), a.getUnanswered(),
                a.getTimeTaken(), a.getStatus());
    }

    private AttemptResult buildResult(Attempt attempt) {
        List<ReviewItem> review = attempt.getAnswers().stream()
                .sorted(Comparator.comparing(Answer::getQuestionPosition))
                .map(a -> {
                    Question q = a.getQuestion();
                    List<ReviewOption> opts = q.getOptions().stream()
                            .map(o -> new ReviewOption(o.getId(), o.getOptionText(), o.getIsCorrect(),
                                    a.getSelectedOptionId() != null && a.getSelectedOptionId().equals(o.getId())))
                            .toList();
                    return new ReviewItem(q.getId(), q.getQuestionText(), q.getMarks(),
                            Boolean.TRUE.equals(a.getIsCorrect()), a.getSelectedOptionId() != null,
                            q.getExplanation(), opts);
                })
                .toList();
        Long certificateId = certificateService.findByAttempt(attempt.getId())
                .map(Certificate::getId).orElse(null);
        return new AttemptResult(attempt.getId(), attempt.getQuiz().getTitle(),
                attempt.getStatus() == AttemptStatus.PASSED, attempt.getPercentage(),
                attempt.getStatus(), attempt.getAnswers().size(),
                attempt.getCorrectAnswers(), attempt.getIncorrectAnswers(), attempt.getUnanswered(),
                attempt.getTimeTaken(), certificateId, review);
    }

    private Attempt findOwnAttempt(Long attemptId) {
        Attempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Attempt not found"));
        if (!attempt.getUser().getId().equals(SecurityUtils.currentUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not have access to this attempt");
        }
        return attempt;
    }

    private boolean isExpired(Attempt attempt) {
        LocalDateTime deadline = attempt.getCompletedAt();
        if (deadline == null) {
            return false;
        }
        if (attempt.getStartedAt() != null) {
            LocalDateTime computed = attempt.getStartedAt().plusMinutes(attempt.getQuiz().getDuration());
            deadline = computed.isAfter(deadline) ? deadline : computed;
        }
        return LocalDateTime.now().isAfter(deadline);
    }
}