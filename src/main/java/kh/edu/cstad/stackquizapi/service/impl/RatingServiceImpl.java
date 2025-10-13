package kh.edu.cstad.stackquizapi.service.impl;

import kh.edu.cstad.stackquizapi.domain.Quiz;
import kh.edu.cstad.stackquizapi.domain.Rating;
import kh.edu.cstad.stackquizapi.dto.request.RatingRequest;
import kh.edu.cstad.stackquizapi.dto.response.RatingResponse;
import kh.edu.cstad.stackquizapi.mapper.RatingMapper;
import kh.edu.cstad.stackquizapi.repository.QuizRepository;
import kh.edu.cstad.stackquizapi.repository.RatingRepository;
import kh.edu.cstad.stackquizapi.service.RatingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final RatingMapper ratingMapper;
    private final QuizRepository quizRepository;

    @Override
    public RatingResponse rateQuiz(String quizId, RatingRequest ratingRequest, Jwt accessToken) {

        String userId = accessToken.getSubject();
        log.info("User ID from JWT: {}", userId);

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Quiz ID not found"));

        Rating existingRating = ratingRepository
                .findByQuizIdAndUserId(quizId, userId)
                .orElse(null);

        Rating rating;

        if (existingRating != null) {
            existingRating.setStars(ratingRequest.stars());
            existingRating.setComment(ratingRequest.comment());
            rating = existingRating;
        } else {
            rating = ratingMapper.fromRatingRequest(ratingRequest);
            rating.setQuiz(quiz);
            rating.setUserId(userId);
        }

        ratingRepository.save(rating);

        double avg = ratingRepository.findAverageByQuizId(quizId);

        return RatingResponse.builder()
                .id(rating.getId())
                .quizId(quizId)
                .userId(userId)
                .stars(rating.getStars())
                .comment(rating.getComment())
                .averageRating(avg)
                .build();
    }

    @Override
    public List<RatingResponse> getRatingsByQuiz(String quizId) {

        return ratingRepository.findByQuizId(quizId)
                .stream()
                .map(rating -> RatingResponse.builder()
                        .id(rating.getId())
                        .quizId(quizId)
                        .userId(rating.getUserId())
                        .stars(rating.getStars())
                        .comment(rating.getComment())
                        .averageRating(0.0)
                        .build()
                ).collect(Collectors.toList());
    }


    @Override
    public List<RatingResponse> getRatingsByUser(Jwt accessToken) {

        String userId = accessToken.getSubject();

        return ratingRepository.findByUserId(userId)
                .stream()
                .map(rating -> RatingResponse.builder()
                        .id(rating.getId())
                        .quizId(String.valueOf(rating.getQuiz().getId()))
                        .userId(userId)
                        .stars(rating.getStars())
                        .comment(rating.getComment())
                        .averageRating(0.0)
                        .build()
                ).collect(Collectors.toList());
    }


    @Override
    public double getAverageRating(String quizId) {
        return ratingRepository.findAverageByQuizId(quizId);
    }

    @Override
    public void deleteRating(String quizId, Jwt accessToken) {
        String userId = accessToken.getSubject();

        if(!quizRepository.existsById(quizId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Quiz ID does not exist");
        }

        ratingRepository.deleteByQuizIdAndUserId(quizId, userId);
    }
}
