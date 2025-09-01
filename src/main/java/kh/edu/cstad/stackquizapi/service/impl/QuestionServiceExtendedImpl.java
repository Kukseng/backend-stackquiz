package kh.edu.cstad.stackquizapi.service.impl;

import kh.edu.cstad.stackquizapi.domain.Question;
import kh.edu.cstad.stackquizapi.domain.QuizSession;
import kh.edu.cstad.stackquizapi.dto.request.CreateQuestionRequest;
import kh.edu.cstad.stackquizapi.dto.request.UpdateQuestionRequest;
import kh.edu.cstad.stackquizapi.dto.response.QuestionResponse;
import kh.edu.cstad.stackquizapi.mapper.QuestionMapper;
import kh.edu.cstad.stackquizapi.repository.QuestionRepository;
import kh.edu.cstad.stackquizapi.repository.QuizSessionRepository;
import kh.edu.cstad.stackquizapi.service.QuestionServiceExtended;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Primary
public class QuestionServiceExtendedImpl implements QuestionServiceExtended {

    private final QuestionServiceImpl baseService;
    private final QuestionRepository questionRepository;
    private final QuizSessionRepository quizSessionRepository;
    private final QuestionMapper questionMapper;

    @Override
    public QuestionResponse createNewQuestion(CreateQuestionRequest createQuestionRequest) {
        return baseService.createNewQuestion(createQuestionRequest);
    }

    @Override
    public List<QuestionResponse> getAllQuestions() {
        return baseService.getAllQuestions();
    }

    @Override
    public QuestionResponse getQuestionById(String id) {
        return baseService.getQuestionById(id);
    }

    @Override
    public QuestionResponse updateQuestionById(String id, UpdateQuestionRequest updateQuestionRequest) {
        return baseService.updateQuestionById(id, updateQuestionRequest);
    }

    @Override
    public void deleteQuestionById(String id) {
        baseService.deleteQuestionById(id);
    }

    @Override
    public void deleteQuestionsByIds(List<String> ids) {
        baseService.deleteQuestionsByIds(ids);
    }

    @Override
    public QuestionResponse getNextQuestionForSession(String sessionId) {
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        List<Question> questions = questionRepository.findByQuizId(session.getQuiz().getId());

        Integer currentQuestionIndex = session.getCurrentQuestion();
        if (currentQuestionIndex == null) {
            currentQuestionIndex = 0;
        }

        if (currentQuestionIndex < questions.size()) {
            Question nextQuestion = questions.get(currentQuestionIndex);
            return questionMapper.toQuestionResponse(nextQuestion);
        }

        return null; // No more questions
    }

    @Override
    public List<QuestionResponse> getQuestionsForSession(String sessionId) {
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        List<Question> questions = questionRepository.findByQuizId(session.getQuiz().getId());

        return questions.stream()
                .map(questionMapper::toQuestionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public QuestionResponse getQuestionByIndexForSession(String sessionId, Integer questionIndex) {
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        List<Question> questions = questionRepository.findByQuizId(session.getQuiz().getId());

        if (questionIndex >= 0 && questionIndex < questions.size()) {
            Question question = questions.get(questionIndex);
            return questionMapper.toQuestionResponse(question);
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found at index: " + questionIndex);
    }

    @Override
    public QuestionResponse getCurrentQuestionForSession(String sessionId) {
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        Integer currentQuestionIndex = session.getCurrentQuestion();
        if (currentQuestionIndex == null) {
            return null;
        }

        return getQuestionByIndexForSession(sessionId, currentQuestionIndex);
    }
}