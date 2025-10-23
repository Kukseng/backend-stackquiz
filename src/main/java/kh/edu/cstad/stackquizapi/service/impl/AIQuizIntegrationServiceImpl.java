package kh.edu.cstad.stackquizapi.service.impl;

import kh.edu.cstad.stackquizapi.domain.Option;
import kh.edu.cstad.stackquizapi.domain.Question;
import kh.edu.cstad.stackquizapi.domain.Quiz;
import kh.edu.cstad.stackquizapi.dto.request.AIQuizGenerationRequest;
import kh.edu.cstad.stackquizapi.dto.response.AIQuizGenerationResponse;
import kh.edu.cstad.stackquizapi.repository.OptionRepository;
import kh.edu.cstad.stackquizapi.repository.QuestionRepository;
import kh.edu.cstad.stackquizapi.repository.QuizRepository;
import kh.edu.cstad.stackquizapi.service.AIQuizGenerationService;
import kh.edu.cstad.stackquizapi.util.QuestionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIQuizIntegrationServiceImpl {

    private final AIQuizGenerationService aiQuizGenerationService;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;

    @Transactional
    public List<Question> generateAndSaveQuestions(String quizId, AIQuizGenerationRequest request) {
        log.info("Generating and saving {} questions for quiz: {}", request.getNumberOfQuestions(), quizId);
        
        // Find the quiz
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found: " + quizId));
        
        // Generate questions using AI
        AIQuizGenerationResponse aiResponse = aiQuizGenerationService.generateQuestions(request);
        
        // Convert and save questions
        List<Question> savedQuestions = new ArrayList<>();
        Integer currentMaxOrder = questionRepository.findMaxQuestionOrderByQuizId(quizId);
        if (currentMaxOrder == null) {
            currentMaxOrder = 0;
        }
        
        for (AIQuizGenerationResponse.GeneratedQuestion genQuestion : aiResponse.getQuestions()) {
            Question question = convertToQuestion(genQuestion, quiz, ++currentMaxOrder);
            Question savedQuestion = questionRepository.save(question);
            
            // Save options
            List<Option> options = convertToOptions(genQuestion, savedQuestion);
            optionRepository.saveAll(options);
            
            savedQuestion.setOptions(options);
            savedQuestions.add(savedQuestion);
        }
        
        log.info("Successfully saved {} AI-generated questions to quiz {}", savedQuestions.size(), quizId);
        return savedQuestions;
    }

    @Transactional
    public Question generateAndSaveSingleQuestion(String quizId, String topic, String difficulty, String questionType) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found: " + quizId));
        
        AIQuizGenerationResponse.GeneratedQuestion genQuestion = 
            aiQuizGenerationService.generateSingleQuestion(topic, difficulty, questionType);
        
        Integer currentMaxOrder = questionRepository.findMaxQuestionOrderByQuizId(quizId);
        int nextOrder = (currentMaxOrder == null ? 0 : currentMaxOrder) + 1;
        
        Question question = convertToQuestion(genQuestion, quiz, nextOrder);
        Question savedQuestion = questionRepository.save(question);
        
        List<Option> options = convertToOptions(genQuestion, savedQuestion);
        optionRepository.saveAll(options);
        
        savedQuestion.setOptions(options);
        return savedQuestion;
    }

    private Question convertToQuestion(AIQuizGenerationResponse.GeneratedQuestion genQuestion, 
                                      Quiz quiz, int order) {
        Question question = new Question();
        question.setText(genQuestion.getQuestionText());
        question.setType(parseQuestionType(genQuestion.getQuestionType()));
        question.setQuestionOrder(order);
        question.setTimeLimit(genQuestion.getTimeLimit());
        question.setPoints(genQuestion.getPoints());
        question.setQuiz(quiz);
        
        if (genQuestion.getImageUrl() != null) {
            question.setImageUrl(genQuestion.getImageUrl());
        }
        
        return question;
    }

    private List<Option> convertToOptions(AIQuizGenerationResponse.GeneratedQuestion genQuestion, 
                                         Question savedQuestion) {
        List<Option> options = new ArrayList<>();
        
        for (AIQuizGenerationResponse.GeneratedOption genOption : genQuestion.getOptions()) {
            Option option = new Option();
            option.setOptionText(genOption.getOptionText());
            option.setIsCorrected(genOption.getIsCorrect());
            option.setOptionOrder(genOption.getOptionOrder());
            option.setQuestion(savedQuestion);
            options.add(option);
        }
        
        return options;
    }

    private QuestionType parseQuestionType(String type) {
        try {
            // Map common types to the enum values
            if (type.equalsIgnoreCase("MULTIPLE_CHOICE") || type.equalsIgnoreCase("MCQ")) {
                return QuestionType.MCQ;
            } else if (type.equalsIgnoreCase("TRUE_FALSE") || type.equalsIgnoreCase("TF")) {
                return QuestionType.TF;
            } else if (type.equalsIgnoreCase("FILL_THE_BLANK") || type.equalsIgnoreCase("SHORT_ANSWER")) {
                return QuestionType.FILL_THE_BLANK;
            }
            return QuestionType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown question type: {}, defaulting to MCQ", type);
            return QuestionType.MCQ;
        }
    }
}

