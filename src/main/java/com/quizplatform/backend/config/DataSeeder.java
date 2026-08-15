package com.quizplatform.backend.config;

import com.quizplatform.backend.entity.*;
import com.quizplatform.backend.enums.*;
import com.quizplatform.backend.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.name}")
    private String adminName;

    public DataSeeder(UserRepository userRepository,
                      CategoryRepository categoryRepository,
                      QuizRepository quizRepository,
                      QuestionRepository questionRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedAdmin();
        seedSampleData();
    }

    private void seedAdmin() {
        if (userRepository.count() == 0) {
            userRepository.save(User.builder()
                    .userName(adminName)
                    .email(adminEmail.toLowerCase())
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .build());
        }
    }

    private void seedSampleData() {
        if (categoryRepository.count() > 0) {
            return;
        }
        List<String> names = List.of("HTML", "CSS", "JavaScript", "React", "Node.js",
                "Python", "Java", "Database", "Computer Networks", "Cyber Security");
        for (String n : names) {
            categoryRepository.save(Category.builder().name(n)
                    .description("Quizzes about " + n).build());
        }

        Category js = categoryRepository.findByNameIgnoreCase("JavaScript").orElseThrow();
        Quiz jsQuiz = quizRepository.save(Quiz.builder()
                .title("JavaScript Fundamentals")
                .description("Test your knowledge of JavaScript fundamentals: variables, functions, objects and the DOM.")
                .category(js)
                .difficulty(Difficulty.INTERMEDIATE)
                .duration(10)
                .passingScore(60)
                .maxAttempts(3)
                .status(QuizStatus.PUBLISHED)
                .negativeMarking(false)
                .negativeMarkValue(BigDecimal.ZERO)
                .build());
        seedQuestion(jsQuiz, "Which keyword is used to declare a constant variable?",
                List.of("var", "let", "const", "static"), 2, 1, Difficulty.EASY,
                "const declares a constant that cannot be reassigned.");
        seedQuestion(jsQuiz, "Which method converts a JSON string into a JavaScript object?",
                List.of("JSON.stringify()", "JSON.parse()", "JSON.convert()", "JSON.object()"), 1, 1,
                Difficulty.EASY, "JSON.parse() converts a JSON string into a JavaScript object.");
        seedQuestion(jsQuiz, "Which of the following is a JavaScript framework?",
                List.of("Django", "Rails", "React", "Flask"), 2, 1, Difficulty.EASY,
                "React is a JavaScript library for building user interfaces.");
        seedQuestion(jsQuiz, "What will '2' + 2 evaluate to in JavaScript?",
                List.of("4", "22", "NaN", "TypeError"), 1, 1, Difficulty.INTERMEDIATE,
                "The '+' operator performs string concatenation when one operand is a string.");
        seedQuestion(jsQuiz, "Which array method removes the last element?",
                List.of("push()", "pop()", "shift()", "splice(0,1)"), 1, 1, Difficulty.INTERMEDIATE,
                "pop() removes the last element from an array.");

        Category python = categoryRepository.findByNameIgnoreCase("Python").orElseThrow();
        Quiz pyQuiz = quizRepository.save(Quiz.builder()
                .title("Python Basics")
                .description("A quick test of core Python concepts for beginners.")
                .category(python)
                .difficulty(Difficulty.EASY)
                .duration(10)
                .passingScore(50)
                .maxAttempts(3)
                .status(QuizStatus.PUBLISHED)
                .negativeMarking(false)
                .negativeMarkValue(BigDecimal.ZERO)
                .build());
        seedQuestion(pyQuiz, "What is the output of print(type([]))?",
                List.of("<class 'list'>", "<class 'tuple'>", "<class 'dict'>", "<class 'set'>"), 0, 1,
                Difficulty.EASY, "[] creates a list.");
        seedQuestion(pyQuiz, "Which keyword defines a function in Python?",
                List.of("function", "def", "func", "lambda"), 1, 1, Difficulty.EASY,
                "Functions are defined using the def keyword.");
        seedQuestion(pyQuiz, "Which of these is an immutable data type?",
                List.of("list", "dict", "tuple", "set"), 2, 1, Difficulty.INTERMEDIATE,
                "Tuples are immutable once created.");
        seedQuestion(pyQuiz, "What does len('hello') return?",
                List.of("4", "5", "6", "None"), 1, 1, Difficulty.EASY,
                "len() returns the number of characters.");
    }

    private void seedQuestion(Quiz quiz, String text, List<String> options, int correctIndex,
                              int marks, Difficulty difficulty, String explanation) {
        Question q = questionRepository.save(Question.builder()
                .quiz(quiz)
                .questionText(text)
                .marks(marks)
                .explanation(explanation)
                .difficulty(difficulty)
                .build());
        for (int i = 0; i < options.size(); i++) {
            q.getOptions().add(Option.builder()
                    .question(q)
                    .optionText(options.get(i))
                    .isCorrect(i == correctIndex)
                    .build());
        }
    }
}