package kh.edu.cstad.stackquizapi.service.impl;

import kh.edu.cstad.stackquizapi.domain.Category;
import kh.edu.cstad.stackquizapi.domain.FavoriteQuiz;
import kh.edu.cstad.stackquizapi.domain.Quiz;
import kh.edu.cstad.stackquizapi.domain.QuizCategory;
import kh.edu.cstad.stackquizapi.domain.User;
import kh.edu.cstad.stackquizapi.dto.request.CreateQuizRequest;
import kh.edu.cstad.stackquizapi.dto.request.QuizUpdate;
import kh.edu.cstad.stackquizapi.dto.request.SuspendQuizRequest;
import kh.edu.cstad.stackquizapi.dto.response.AtToFavoriteResponse;
import kh.edu.cstad.stackquizapi.dto.response.MediaResponse;
import kh.edu.cstad.stackquizapi.dto.response.QuizResponse;
import kh.edu.cstad.stackquizapi.dto.response.QuizSuspensionResponse;
import kh.edu.cstad.stackquizapi.mapper.QuizMapper;
import kh.edu.cstad.stackquizapi.repository.CategoryRepository;
import kh.edu.cstad.stackquizapi.repository.FavoriteQuizRepository;
import kh.edu.cstad.stackquizapi.repository.QuizRepository;
import kh.edu.cstad.stackquizapi.repository.UserRepository;
import kh.edu.cstad.stackquizapi.service.MediaService;
import kh.edu.cstad.stackquizapi.service.QuizService;
import kh.edu.cstad.stackquizapi.util.QuizStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final QuizMapper quizMapper;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final FavoriteQuizRepository favoriteQuizRepository;
    private final MediaService mediaService;

    @Override
    public QuizResponse createQuiz(CreateQuizRequest createQuizRequest, MultipartFile file, Jwt accessToken) {

        String userId = accessToken.getSubject();
        log.info("User ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User Id not found " + userId));

        Quiz quiz = quizMapper.toQuizRequest(createQuizRequest);
        MediaResponse media = mediaService.upload(file);

        String thumbnail = media.uri();

        quiz.setUser(user);
        quiz.setThumbnailUrl(thumbnail);
        quiz.setFlagged(false);
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
        return quizRepository.findByIsActive(active).stream()
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

    @Override
    public QuizSuspensionResponse suspendQuiz(String quizId, SuspendQuizRequest request, Jwt accessToken) {

        String adminId = accessToken.getSubject();
        log.info("Admin ID from JWT: {}", adminId);

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz ID not found"));

        String oldStatus = quiz.getStatus().name();

        quiz.setStatus(QuizStatus.SUSPENDED);
        quiz.setIsActive(false);
        quiz.setFlagged(true);
        quizRepository.save(quiz);

        QuizSuspensionResponse response = new QuizSuspensionResponse(
                "success",
                "suspend_quiz",
                new QuizSuspensionResponse.QuizInfo(
                        quiz.getId(),
                        quiz.getTitle(),
                        oldStatus,
                        quiz.getStatus().name(),
                        quiz.getIsActive(),
                        quiz.getFlagged()
                ),
                request.reason(),
                new QuizSuspensionResponse.AdminInfo(
                        adminId,
                        "ADMIN"
                ),
                LocalDateTime.now(),
                new QuizSuspensionResponse.NextSteps(
                        "/api/quizzes/" + quiz.getId() + "/appeal",
                        LocalDateTime.now().plusDays(7)
                ),
                "Quiz '" + quiz.getTitle() + "' has been suspended successfully and the creator has been notified."
        );

        return response;
    }

    @Override
    public AtToFavoriteResponse atToFavorite(String quizId, Jwt accessToken) {
        String userId = accessToken.getSubject();

        Quiz quiz = quizRepository.findQuizById(quizId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Quiz ID not found"
                ));

        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User ID not found"
                ));

        Optional<FavoriteQuiz> existingFavorite = favoriteQuizRepository.findByUserAndQuiz(user, quiz);
        if (existingFavorite.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Quiz already in favorites");
        }

        FavoriteQuiz favoriteQuiz = new FavoriteQuiz();
        favoriteQuiz.setUser(user);
        favoriteQuiz.setQuiz(quiz);
        favoriteQuiz.setCreatedAt(LocalDateTime.now());

        favoriteQuizRepository.save(favoriteQuiz);

        return AtToFavoriteResponse.builder()
                .id(favoriteQuiz.getId())
                .quizId(quiz.getId())
                .username(user.getUsername())
                .build();
    }

    @Override
    @Transactional
    public void removeFromFavorite(String quizId, Jwt accessToken) {
        String userId = accessToken.getSubject();

        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User ID not found"
                ));

        Quiz quiz = quizRepository.findQuizById(quizId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Quiz ID not found"
                ));

        FavoriteQuiz favoriteQuiz = favoriteQuizRepository.findByUserAndQuiz(user, quiz)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Favorite not found"
                ));

        favoriteQuizRepository.delete(favoriteQuiz);
    }


}
