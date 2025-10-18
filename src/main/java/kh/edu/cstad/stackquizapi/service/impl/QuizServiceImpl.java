package kh.edu.cstad.stackquizapi.service.impl;

import kh.edu.cstad.stackquizapi.domain.*;
import kh.edu.cstad.stackquizapi.dto.request.CreateFeedbackRequest;
import kh.edu.cstad.stackquizapi.dto.request.CreateQuizRequest;
import kh.edu.cstad.stackquizapi.dto.request.FolkQuizRequest;
import kh.edu.cstad.stackquizapi.dto.request.QuizUpdateRequest;
import kh.edu.cstad.stackquizapi.dto.request.SuspendQuizRequest;
import kh.edu.cstad.stackquizapi.dto.response.FavoriteQuizResponse;
import kh.edu.cstad.stackquizapi.dto.response.CreateFeedbackResponse;
import kh.edu.cstad.stackquizapi.dto.response.QuizFeedbackResponse;
import kh.edu.cstad.stackquizapi.dto.response.QuizResponse;
import kh.edu.cstad.stackquizapi.dto.response.QuizSuspensionResponse;
import kh.edu.cstad.stackquizapi.mapper.QuizMapper;
import kh.edu.cstad.stackquizapi.repository.*;
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

import java.sql.Timestamp;
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
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final QuizFeedbackRepository quizFeedbackRepository;

    @Override
    public QuizResponse createQuiz(CreateQuizRequest createQuizRequest, MultipartFile file, Jwt accessToken) {

        String userId = accessToken.getSubject();
        log.info("User ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User Id not found " + userId));

        Quiz quiz = quizMapper.toQuizRequest(createQuizRequest);

        quiz.setUser(user);
        quiz.setFlagged(false);
        quiz.setVersionNumber(1);
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
                .filter(quiz ->
                        quiz.getIsActive().equals(true)
                                && !quiz.getStatus().equals(QuizStatus.DRAFT)
                                && !quiz.getFlagged())
                .map(quizMapper::toQuizResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Quiz not found"));

    }

    @Override
    public List<QuizResponse> getAllQuiz(Boolean active) {
        return quizRepository.findAll().stream()
                .filter(quiz ->
                        quiz.getIsActive().equals(true)
                                && !quiz.getStatus().equals(QuizStatus.DRAFT)
                                && !quiz.getFlagged())
                .map(quizMapper::toQuizResponse)
                .toList();
    }


    @Override
    public QuizResponse updateQuiz(String QuizId, QuizUpdateRequest quizUpdateRequest, Jwt accessToken) {

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

        quizMapper.toQuizUpdateResponse(quizUpdateRequest, quiz);
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

        return quizRepository.findByUserId(user.getId())
                .stream()
                .filter(quiz ->
                        quiz.getIsActive().equals(true)
                                && !quiz.getStatus().equals(QuizStatus.DRAFT)
                                && !quiz.getFlagged())
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
    public FavoriteQuizResponse atToFavorite(String quizId, Jwt accessToken) {
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

        return FavoriteQuizResponse.builder()
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

    @Override
    public List<FavoriteQuizResponse> getFavoriteQuizzes() {

        return favoriteQuizRepository.findAll()
                .stream()
                .map(favoriteQuiz -> FavoriteQuizResponse
                        .builder()
                        .id(favoriteQuiz.getId())
                        .quizId(favoriteQuiz.getQuiz().getId())
                        .username(favoriteQuiz.getUser().getUsername())
                        .createdAt(favoriteQuiz.getCreatedAt())
                        .build())
                .toList();
    }

    @Override
    public List<FavoriteQuizResponse> getCurrentUserFavoriteQuizzes(Jwt accessToken) {
        String userId = accessToken.getSubject();

        return favoriteQuizRepository.findAll()
                .stream()
                .filter(fav -> fav.getUser().getId().equals(userId))
                .map(favoriteQuiz -> FavoriteQuizResponse
                        .builder()
                        .id(favoriteQuiz.getId())
                        .quizId(favoriteQuiz.getQuiz().getId())
                        .username(favoriteQuiz.getUser().getUsername())
                        .createdAt(favoriteQuiz.getCreatedAt())
                        .build())
                .toList();

    }


    @Override
    public QuizResponse folkQuiz(Jwt accessToken, String quizId, FolkQuizRequest folkQuizRequest) {
        String userId = accessToken.getSubject();

        Quiz newQuiz = cloneQuiz(userId, quizId);

        newQuiz = quizRepository.save(newQuiz);

        cloneQuestionsAndOptions(newQuiz, quizId);

        if (folkQuizRequest != null) {
            if (folkQuizRequest.title() != null) newQuiz.setTitle(folkQuizRequest.title());
            if (folkQuizRequest.description() != null) newQuiz.setDescription(folkQuizRequest.description());
            if (folkQuizRequest.thumbnailUrl() != null) newQuiz.setThumbnailUrl(folkQuizRequest.thumbnailUrl());
            if (folkQuizRequest.visibility() != null) newQuiz.setVisibility(folkQuizRequest.visibility());
            if (folkQuizRequest.questionTimeLimit() != null)
                newQuiz.setQuestionTimeLimit(folkQuizRequest.questionTimeLimit());
            if (folkQuizRequest.difficulty() != null) newQuiz.setDifficulty(folkQuizRequest.difficulty());
        }

        quizRepository.save(newQuiz);

        return quizMapper.toQuizResponse(newQuiz);
    }


    private Quiz cloneQuiz(String userId, String quizId) {
        User currentUser = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User ID not found"));

        Quiz originalQuiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz ID not found"));

        // 🔹 Count existing forks
        int nextVersion = quizRepository.countByParentQuizId(originalQuiz.getId()) + 1;

        Quiz newQuiz = new Quiz();
        newQuiz.setTitle(originalQuiz.getTitle());
        newQuiz.setDescription(originalQuiz.getDescription());
        newQuiz.setThumbnailUrl(originalQuiz.getThumbnailUrl());
        newQuiz.setVisibility(originalQuiz.getVisibility());
        newQuiz.setQuestionTimeLimit(originalQuiz.getQuestionTimeLimit());
        newQuiz.setDifficulty(originalQuiz.getDifficulty());
        newQuiz.setStatus(originalQuiz.getStatus());
        newQuiz.setIsActive(true);
        newQuiz.setFlagged(false);
        newQuiz.setVersionNumber(nextVersion);
        newQuiz.setParentQuiz(originalQuiz);
        newQuiz.setUser(currentUser);
        newQuiz.setCreatedAt(LocalDateTime.now());
        newQuiz.setUpdatedAt(LocalDateTime.now());
        return newQuiz;
    }


    private void cloneQuestionsAndOptions(Quiz savedQuiz, String originalQuizId) {
        Quiz originalQuiz = quizRepository.findById(originalQuizId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Original quiz not found"));

        for (Question originalQuestion : originalQuiz.getQuestions()) {
            Question newQuestion = new Question();
            newQuestion.setQuiz(savedQuiz);
            newQuestion.setText(originalQuestion.getText());
            newQuestion.setType(originalQuestion.getType());
            newQuestion.setQuestionOrder(originalQuestion.getQuestionOrder());
            newQuestion.setTimeLimit(originalQuestion.getTimeLimit());
            newQuestion.setPoints(originalQuestion.getPoints());
            newQuestion.setImageUrl(originalQuestion.getImageUrl());
            newQuestion.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            newQuestion.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

            questionRepository.save(newQuestion);

            for (Option originalOption : originalQuestion.getOptions()) {
                Option newOption = new Option();
                newOption.setQuestion(newQuestion);
                newOption.setOptionText(originalOption.getOptionText());
                newOption.setOptionOrder(originalOption.getOptionOrder());
                newOption.setIsCorrected(originalOption.getIsCorrected());
                newOption.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                optionRepository.save(newOption);
            }
        }
    }


    @Override
    public CreateFeedbackResponse giveFeedback(CreateFeedbackRequest createFeedbackRequest, String quizId, Jwt accessToken) {
        String userId = accessToken.getSubject();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User ID not found"));

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Quiz ID not found"));

        QuizFeedback feedback = new QuizFeedback();
        feedback.setSatisfactionLevel(createFeedbackRequest.satisfactionLevel());
        feedback.setText(createFeedbackRequest.text());
        feedback.setUser(user);
        feedback.setQuiz(quiz);
        feedback.setCreatedAt(LocalDateTime.now());

        quizFeedbackRepository.save(feedback);

        return CreateFeedbackResponse
                .builder()
                .feedbackId(feedback.getId())
                .quizId(feedback.getQuiz().getId())
                .userId(feedback.getUser().getId())
                .status("success")
                .message("Feedback submitted successfully.")
                .satisfactionLevel(feedback.getSatisfactionLevel())
                .build();
    }

    @Override
    public List<QuizFeedbackResponse> getAllFeedbacks() {
        return quizFeedbackRepository.findAll()
                .stream().map(quiz -> QuizFeedbackResponse
                        .builder()
                        .feedbackId(quiz.getId())
                        .quizId(quiz.getQuiz().getId())
                        .userId(quiz.getUser().getId())
                        .satisfactionLevel(quiz.getSatisfactionLevel())
                        .createdAt(quiz.getCreatedAt())
                        .text(quiz.getText())
                        .build())
                .toList();
    }

    @Override
    public List<QuizFeedbackResponse> getCurrentUserQuizFeedbacks(Jwt accessToken) {
        String userId = accessToken.getSubject();

        return quizFeedbackRepository.findAll()
                .stream()
                .filter(quiz -> quiz.getUser().getId().equals(userId))
                .map(quiz -> QuizFeedbackResponse
                        .builder()
                        .feedbackId(quiz.getId())
                        .quizId(quiz.getQuiz().getId())
                        .userId(quiz.getUser().getId())
                        .satisfactionLevel(quiz.getSatisfactionLevel())
                        .createdAt(quiz.getCreatedAt())
                        .text(quiz.getText())
                        .build())
                .toList();
    }

}
