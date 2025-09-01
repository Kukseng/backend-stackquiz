package kh.edu.cstad.stackquizapi.service.impl;

import kh.edu.cstad.stackquizapi.domain.Category;
import kh.edu.cstad.stackquizapi.domain.Quiz;
import kh.edu.cstad.stackquizapi.domain.QuizCategory;
import kh.edu.cstad.stackquizapi.domain.User;
import kh.edu.cstad.stackquizapi.dto.request.CreateQuizRequest;
import kh.edu.cstad.stackquizapi.dto.request.QuizUpdate;
import kh.edu.cstad.stackquizapi.dto.response.QuizResponse;
import kh.edu.cstad.stackquizapi.mapper.QuizMapper;
import kh.edu.cstad.stackquizapi.repository.CategoryRepository;
import kh.edu.cstad.stackquizapi.repository.QuizRepository;
import kh.edu.cstad.stackquizapi.repository.UserRepository;
import kh.edu.cstad.stackquizapi.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
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

    public QuizResponse createQuiz(CreateQuizRequest createQuizRequest) {
        User user = userRepository.findById(createQuizRequest.userId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User Id not found " + createQuizRequest.userId())
        );


        Quiz quiz = quizMapper.toQuizRequest(createQuizRequest);
        quiz.setUser(user);
        quiz.setCreatedAt(LocalDateTime.now());

        quiz.setUpdatedAt(LocalDateTime.now());
        quiz.setIsActive(true);

        List<QuizCategory> quizCategories = createQuizRequest.categoryIds().stream()
                .map(catId -> {
                    Category category = categoryRepository.findById(catId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
                    QuizCategory qc = new QuizCategory();
                    qc.setQuiz(quiz);
                    qc.setCategory(category);
                    return qc;
                }).toList();

        quiz.setQuizCategories(quizCategories);
        quizRepository.save(quiz);


        return quizMapper.toQuizResponse(quiz);
    }

    /***
     *
     * @param quizId
     * @return
     */

    @Override
    public QuizResponse getQuizById(String quizId) {
        return quizRepository.findById(quizId).map(quizMapper::toQuizResponse).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Quiz not found"));
    }

    @Override
    public List<QuizResponse> getAllQuiz(Boolean active) {
        return quizRepository.findAll().stream()
                .filter(quiz -> quiz.getIsActive() == active)
                .map(quizMapper::toQuizResponse).toList();
    }

    @Override
    public QuizResponse updateQuiz(String QuizId, QuizUpdate quizUpdate) {
//        User user = userRepository.findById(quizRequest.userId()).orElseThrow(
//                ()-> new ResponseStatusException(HttpStatus.NOT_FOUND,
//                        "User not found")
//        );

        Quiz quiz = quizRepository.findById(QuizId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found"));
        User user = userRepository.findById(quizUpdate.userId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        );

        if (!quiz.getUser().getId().equals(quizUpdate.userId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to update this quiz");
        }

        quizMapper.toQuizUpdateResponse(quizUpdate, quiz);
        quiz = quizRepository.save(quiz);
        return quizMapper.toQuizResponse(quiz);
    }

    @Override
    public boolean deleteQuiz(String quizId) {
        return quizRepository.findById(quizId)
                .map(quiz -> {
                    quiz.setIsActive(false);
                    quizRepository.save(quiz);
                    return true;
                }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found"));
    }

    @Override
    public List<QuizResponse> getQuizByUserId(String userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        );

        return quizRepository.findByUserId(user.getId()).stream()
                .map(quizMapper::toQuizResponse).toList();
    }
}
