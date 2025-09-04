package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.request.CreateQuizRequest;
import kh.edu.cstad.stackquizapi.dto.request.QuizUpdate;
import kh.edu.cstad.stackquizapi.dto.response.QuizResponse;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

public interface QuizService {

    QuizResponse createQuiz(CreateQuizRequest createQuizRequest, Jwt jwt);

    QuizResponse getQuizById(String quizId);

    List<QuizResponse> getAllQuiz(Boolean active);

    QuizResponse updateQuiz(String QuizId, QuizUpdate quizUpdate, Jwt accessToken);

    boolean deleteQuiz(String quizId);

    List<QuizResponse> getQuizByUser(Jwt accessToken);

}

