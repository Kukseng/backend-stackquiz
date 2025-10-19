package kh.edu.cstad.stackquizapi.service;

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
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface QuizService {

    QuizResponse createQuiz(CreateQuizRequest createQuizRequest, Jwt jwt);

    QuizResponse getQuizById(String quizId);

    List<QuizResponse> getAllQuiz(Boolean active);

    QuizResponse updateQuiz(String QuizId, QuizUpdateRequest quizUpdateRequest, Jwt accessToken);

    void deleteQuiz(String quizId, Jwt accessToken);

    List<QuizResponse> getQuizByUser(Jwt accessToken);

    QuizSuspensionResponse suspendQuiz(String quizId, SuspendQuizRequest request, Jwt accessToken);

    FavoriteQuizResponse atToFavorite(String quizId, Jwt accessToken);

    void removeFromFavorite(String quizId, Jwt accessToken);

    List<FavoriteQuizResponse> getFavoriteQuizzes();

    List<FavoriteQuizResponse> getCurrentUserFavoriteQuizzes(Jwt accessToken);

    QuizResponse folkQuiz(Jwt accessToken, String quizId, FolkQuizRequest folkQuizRequest);

    CreateFeedbackResponse giveFeedback(CreateFeedbackRequest createFeedbackRequest, String quizId, Jwt accessToken);

    List<QuizFeedbackResponse> getAllFeedbacks();

    List<QuizFeedbackResponse> getCurrentUserQuizFeedbacks(Jwt accessToken);

}

