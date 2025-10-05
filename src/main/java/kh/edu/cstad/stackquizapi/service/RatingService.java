package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.request.RatingRequest;
import kh.edu.cstad.stackquizapi.dto.response.RatingResponse;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

public interface RatingService {

    RatingResponse rateQuiz(String quizId, RatingRequest ratingRequest, Jwt accessToken);

    List<RatingResponse> getRatingsByQuiz(String quizId);

    List<RatingResponse> getRatingsByUser(Jwt accessToken);

    double getAverageRating(String quizId);

    void deleteRating(String quizId, Jwt accessToken);

}
