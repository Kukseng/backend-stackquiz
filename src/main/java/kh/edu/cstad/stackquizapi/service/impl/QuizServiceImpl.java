package kh.edu.cstad.stackquizapi.service.impl;

import kh.edu.cstad.stackquizapi.domain.Category;
import kh.edu.cstad.stackquizapi.domain.Quiz;
import kh.edu.cstad.stackquizapi.domain.QuizCategory;
import kh.edu.cstad.stackquizapi.domain.User;
import kh.edu.cstad.stackquizapi.dto.request.CreateQuizRequest;
import kh.edu.cstad.stackquizapi.dto.request.QuizUpdate;
import kh.edu.cstad.stackquizapi.dto.response.OptionResponse;
import kh.edu.cstad.stackquizapi.dto.response.QuestionResponse;
import kh.edu.cstad.stackquizapi.dto.response.QuizResponse;
import kh.edu.cstad.stackquizapi.mapper.QuizMapper;
import kh.edu.cstad.stackquizapi.repository.CategoryRepository;
import kh.edu.cstad.stackquizapi.repository.QuizRepository;
import kh.edu.cstad.stackquizapi.repository.UserRepository;
import kh.edu.cstad.stackquizapi.service.QuizService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final QuizMapper quizMapper;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    /***
     * Create quiz
     * @param
     * @return
     */
    @Override

    public QuizResponse createQuiz(CreateQuizRequest createQuizRequest, Jwt accessToken) {

        String userId = accessToken.getSubject();
        log.info("User ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User Id not found " + userId));

        Quiz quiz = quizMapper.toQuizRequest(createQuizRequest);
        quiz.setUser(user);
        quiz.setCreatedAt(LocalDateTime.now());
        quiz.setUpdatedAt(LocalDateTime.now());
        quiz.setIsActive(true);

        List<QuizCategory> quizCategories = createQuizRequest.categoryIds().stream()
                .map(catId -> {
                    Category category = categoryRepository.findById(catId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                    "Category not found"));
                    QuizCategory qc = new QuizCategory();
                    qc.setQuiz(quiz);
                    qc.setCategory(category);
                    return qc;
                }).toList();

        quiz.setQuizCategories(quizCategories);
        quizRepository.save(quiz);

        return quizMapper.toQuizResponse(quiz);
    }

    @Override
    public QuizResponse getQuizById(String quizId) {

        return quizRepository
                .findById(quizId)
                .map(quizMapper::toQuizResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Quiz not found"));

    }

    @Override
    public List<QuizResponse> getAllQuiz(Boolean active) {
        return quizRepository.findAll().stream()
                .filter(quiz -> quiz.getIsActive().equals(active))
                .map(quizMapper::toQuizResponse)
                .toList();
    }


    @Override
    public QuizResponse updateQuiz(String QuizId, QuizUpdate quizUpdate, Jwt accessToken) {

        String userId = accessToken.getSubject();

        Quiz quiz = quizRepository.findById(QuizId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found"));

        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        );

        if (!quiz.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You are not allowed to update this quiz");
        }

        quizMapper.toQuizUpdateResponse(quizUpdate, quiz);
        quiz = quizRepository.save(quiz);
        return quizMapper.toQuizResponse(quiz);
    }

    @Override
    public void deleteQuiz(String quizId, Jwt accessToken) {

        String userId = accessToken.getSubject();

        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User not found");
        }

        quizRepository.findById(quizId)
                .filter(quiz -> quiz.getUser().getId().equals(userId))
                .map(quiz -> {
                    quiz.setIsActive(false);
                    quizRepository.save(quiz);
                    return true;
                }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found"));
    }

    @Override
    public List<QuizResponse> getQuizByUser(Jwt accessToken) {

        String userId = accessToken.getSubject();

        User user = userRepository.findByIdAndIsActiveTrue(userId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        );

        return quizRepository.findByUserId(user.getId()).stream()
                .map(quizMapper::toQuizResponse).toList();
    }

}
