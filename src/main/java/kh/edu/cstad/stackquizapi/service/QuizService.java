package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.request.CreateQuizRequest;
import kh.edu.cstad.stackquizapi.dto.request.QuizUpdate;
import kh.edu.cstad.stackquizapi.dto.response.QuizResponse;

import java.util.List;

public interface QuizService {

    QuizResponse createQuiz(CreateQuizRequest createQuizRequest);

    QuizResponse getQuizById(String quizId);

    List<QuizResponse> getAllQuiz(Boolean active);

    QuizResponse updateQuiz(String QuizId, QuizUpdate quizUpdate);

    boolean deleteQuiz(String quizId);

    List<QuizResponse> getQuizByUserId(String userId);

}

