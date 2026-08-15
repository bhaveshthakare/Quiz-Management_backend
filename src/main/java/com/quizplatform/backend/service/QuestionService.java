package com.quizplatform.backend.service;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.quizplatform.backend.dto.QuestionDtos.*;
import com.quizplatform.backend.entity.Option;
import com.quizplatform.backend.entity.Question;
import com.quizplatform.backend.entity.Quiz;
import com.quizplatform.backend.enums.Difficulty;
import com.quizplatform.backend.exception.ApiException;
import com.quizplatform.backend.repository.QuestionRepository;
import com.quizplatform.backend.repository.QuizRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;

    public QuestionService(QuestionRepository questionRepository, QuizRepository quizRepository) {
        this.questionRepository = questionRepository;
        this.quizRepository = quizRepository;
    }

    @Transactional(readOnly = true)
    public List<QuestionResponse> listByQuiz(Long quizId) {
        Quiz quiz = findQuiz(quizId);
        return questionRepository.findByQuizOrderByIdAsc(quiz).stream().map(this::toResponse).toList();
    }

    @Transactional
    public QuestionResponse create(Long quizId, QuestionRequest req) {
        Quiz quiz = findQuiz(quizId);
        validateOptions(req);
        Question q = Question.builder()
                .quiz(quiz)
                .questionText(req.questionText())
                .marks(req.marks())
                .difficulty(req.difficulty())
                .explanation(req.explanation())
                .build();
        for (OptionInput in : req.options()) {
            q.getOptions().add(Option.builder().question(q)
                    .optionText(in.optionText()).isCorrect(in.isCorrect()).build());
        }
        Question saved = questionRepository.save(q);
        return toResponse(saved);
    }

    @Transactional
    public QuestionResponse update(Long id, QuestionRequest req) {
        Question q = questionRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Question not found"));
        validateOptions(req);
        q.setQuestionText(req.questionText());
        q.setMarks(req.marks());
        q.setDifficulty(req.difficulty());
        q.setExplanation(req.explanation());
        q.getOptions().clear();
        for (OptionInput in : req.options()) {
            q.getOptions().add(Option.builder().question(q)
                    .optionText(in.optionText()).isCorrect(in.isCorrect()).build());
        }
        Question saved = questionRepository.save(q);
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        Question q = questionRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Question not found"));
        questionRepository.delete(q);
    }

    @Transactional
    public ImportResponse importQuestions(Long quizId, MultipartFile file) {
        Quiz quiz = findQuiz(quizId);
        List<String> errors = new ArrayList<>();
        List<Question> toSave = new ArrayList<>();
        int totalRows = 0;

        try {
            List<String[]> rows = parseRows(file);
            for (String[] row : rows) {
                totalRows++;
                try {
                    toSave.add(parseRow(quiz, row));
                } catch (Exception e) {
                    errors.add("Row " + totalRows + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Could not read file: " + e.getMessage());
        }

        questionRepository.saveAll(toSave);
        return new ImportResponse(toSave.size(), totalRows, errors.size(), errors);
    }

    private Question parseRow(Quiz quiz, String[] c) {
        if (c.length < 6) {
            throw new IllegalArgumentException("Expected at least 6 columns: question, option1..4, correct");
        }
        String questionText = c[0].trim();
        if (questionText.isEmpty()) {
            throw new IllegalArgumentException("Question text is empty");
        }
        List<String> options = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            String opt = i < c.length ? c[i].trim() : "";
            if (opt.isEmpty()) {
                throw new IllegalArgumentException("Option " + i + " is empty");
            }
            options.add(opt);
        }
        String correctLetter = (c.length > 5 ? c[5] : "").trim().toUpperCase();
        int correctIndex = switch (correctLetter) {
            case "A" -> 0;
            case "B" -> 1;
            case "C" -> 2;
            case "D" -> 3;
            default -> throw new IllegalArgumentException("Correct column must be A, B, C or D");
        };
        int marks = 1;
        if (c.length > 6 && !c[6].trim().isEmpty()) {
            try {
                marks = Integer.parseInt(c[6].trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Marks must be a number");
            }
        }
        Difficulty difficulty = Difficulty.EASY;
        if (c.length > 7 && !c[7].trim().isEmpty()) {
            try {
                difficulty = Difficulty.valueOf(c[7].trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Difficulty must be EASY, INTERMEDIATE or HARD");
            }
        }
        String explanation = c.length > 8 ? c[8].trim() : null;

        Question q = Question.builder()
                .quiz(quiz)
                .questionText(questionText)
                .marks(marks)
                .difficulty(difficulty)
                .explanation(explanation)
                .build();
        for (int i = 0; i < options.size(); i++) {
            q.getOptions().add(Option.builder().question(q)
                    .optionText(options.get(i)).isCorrect(i == correctIndex).build());
        }
        return q;
    }

    private List<String[]> parseRows(MultipartFile file) throws Exception {
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        if (name.endsWith(".csv")) {
            CSVReader reader = new CSVReaderBuilder(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))
                    .withCSVParser(new CSVParserBuilder().withIgnoreQuotations(false).build())
                    .build();
            List<String[]> rows = reader.readAll();
            if (!rows.isEmpty()) {
                rows.remove(0);
            }
            return rows;
        }
        if (name.endsWith(".xlsx") || name.endsWith(".xls")) {
            Workbook wb = WorkbookFactory.create(file.getInputStream());
            Sheet sheet = wb.getSheetAt(0);
            List<String[]> rows = new ArrayList<>();
            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue;
                }
                String[] cells = new String[row.getLastCellNum()];
                for (int i = 0; i < row.getLastCellNum(); i++) {
                    Cell cell = row.getCell(i);
                    cells[i] = cell == null ? "" : new DataFormatter().formatCellValue(cell).trim();
                }
                rows.add(cells);
            }
            wb.close();
            return rows;
        }
        throw new ApiException(HttpStatus.BAD_REQUEST, "File must be CSV, XLSX or XLS");
    }

    private void validateOptions(QuestionRequest req) {
        long correct = req.options().stream().filter(o -> Boolean.TRUE.equals(o.isCorrect())).count();
        if (correct != 1) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Exactly one option must be marked as correct");
        }
    }

    private QuestionResponse toResponse(Question q) {
        List<OptionResponse> opts = q.getOptions().stream()
                .map(o -> new OptionResponse(o.getId(), o.getOptionText(), o.getIsCorrect()))
                .toList();
        return new QuestionResponse(q.getId(), q.getQuiz().getId(), q.getQuestionText(),
                q.getMarks(), q.getDifficulty(), q.getExplanation(), opts);
    }

    private Quiz findQuiz(Long quizId) {
        return quizRepository.findById(quizId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Quiz not found"));
    }
}