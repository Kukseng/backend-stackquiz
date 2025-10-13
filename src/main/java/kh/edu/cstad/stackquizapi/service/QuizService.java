package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.request.CreateQuizRequest;
import kh.edu.cstad.stackquizapi.dto.request.QuizUpdate;
import kh.edu.cstad.stackquizapi.dto.request.SuspendQuizRequest;
import kh.edu.cstad.stackquizapi.dto.response.AtToFavoriteResponse;
import kh.edu.cstad.stackquizapi.dto.response.QuizResponse;
import kh.edu.cstad.stackquizapi.dto.response.QuizSuspensionResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface QuizService {

    QuizResponse createQuiz(CreateQuizRequest createQuizRequest, MultipartFile file, Jwt jwt);

    QuizResponse getQuizById(String quizId);

    List<QuizResponse> getAllQuiz(Boolean active);

    QuizResponse updateQuiz(String QuizId, QuizUpdate quizUpdate, Jwt accessToken);

    void deleteQuiz(String quizId, Jwt accessToken);

    List<QuizResponse> getQuizByUser(Jwt accessToken);

    QuizSuspensionResponse suspendQuiz(String quizId, SuspendQuizRequest request, Jwt accessToken);

    AtToFavoriteResponse atToFavorite(String quizId, Jwt accessToken);

    void removeFromFavorite(String quizId, Jwt accessToken);

}

