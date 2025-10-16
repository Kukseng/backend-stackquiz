package kh.edu.cstad.stackquizapi.mapper;

import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import kh.edu.cstad.stackquizapi.domain.Quiz;
import kh.edu.cstad.stackquizapi.domain.QuizSession;
import kh.edu.cstad.stackquizapi.dto.request.SessionCreateRequest;
import kh.edu.cstad.stackquizapi.dto.response.SessionResponse;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-16T16:34:38+0700",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.3.jar, environment: Java 21.0.8 (Microsoft)"
)
@Component
public class QuizSessionMapperImpl implements QuizSessionMapper {

    @Override
    public SessionResponse toSessionResponse(QuizSession quizSession) {
        if ( quizSession == null ) {
            return null;
        }

        String quizTitle = null;
        String id = null;
        String sessionName = null;
        String sessionCode = null;
        String status = null;
        Integer currentQuestion = null;
        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        LocalDateTime createdAt = null;
        Integer totalQuestions = null;
        String hostName = null;

        quizTitle = quizSessionQuizTitle( quizSession );
        id = quizSession.getId();
        sessionName = quizSession.getSessionName();
        sessionCode = quizSession.getSessionCode();
        if ( quizSession.getStatus() != null ) {
            status = quizSession.getStatus().name();
        }
        currentQuestion = quizSession.getCurrentQuestion();
        startTime = quizSession.getStartTime();
        endTime = quizSession.getEndTime();
        createdAt = quizSession.getCreatedAt();
        totalQuestions = quizSession.getTotalQuestions();
        hostName = quizSession.getHostName();

        Integer participantCount = null;

        SessionResponse sessionResponse = new SessionResponse( id, sessionName, sessionCode, status, currentQuestion, startTime, endTime, createdAt, quizTitle, totalQuestions, hostName, participantCount );

        return sessionResponse;
    }

    @Override
    public QuizSession toSessionRequest(SessionCreateRequest sessionCreateRequest) {
        if ( sessionCreateRequest == null ) {
            return null;
        }

        QuizSession quizSession = new QuizSession();

        quizSession.setSessionName( sessionCreateRequest.sessionName() );
        quizSession.setMode( sessionCreateRequest.mode() );
        quizSession.setSessionTimeLimit( sessionCreateRequest.sessionTimeLimit() );
        quizSession.setScheduledStartTime( sessionCreateRequest.scheduledStartTime() );
        quizSession.setScheduledEndTime( sessionCreateRequest.scheduledEndTime() );
        quizSession.setMaxAttempts( sessionCreateRequest.maxAttempts() );
        quizSession.setAllowJoinInProgress( sessionCreateRequest.allowJoinInProgress() );
        quizSession.setShuffleQuestions( sessionCreateRequest.shuffleQuestions() );
        quizSession.setShowCorrectAnswers( sessionCreateRequest.showCorrectAnswers() );
        quizSession.setDefaultQuestionTimeLimit( sessionCreateRequest.defaultQuestionTimeLimit() );
        quizSession.setMaxParticipants( sessionCreateRequest.maxParticipants() );
        quizSession.setShowLeaderboard( sessionCreateRequest.showLeaderboard() );
        quizSession.setShowProgress( sessionCreateRequest.showProgress() );
        quizSession.setPlaySound( sessionCreateRequest.playSound() );
        quizSession.setIsPublic( sessionCreateRequest.isPublic() );

        return quizSession;
    }

    private String quizSessionQuizTitle(QuizSession quizSession) {
        Quiz quiz = quizSession.getQuiz();
        if ( quiz == null ) {
            return null;
        }
        return quiz.getTitle();
    }
}
