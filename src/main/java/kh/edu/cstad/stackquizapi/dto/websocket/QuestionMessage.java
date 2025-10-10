package kh.edu.cstad.stackquizapi.dto.websocket;

import kh.edu.cstad.stackquizapi.domain.Option;
import kh.edu.cstad.stackquizapi.domain.Question;
import kh.edu.cstad.stackquizapi.dto.response.OptionResponse;
import kh.edu.cstad.stackquizapi.dto.response.QuestionResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class QuestionMessage extends WebSocketMessage {
    private QuestionResponse question;
    private Integer questionNumber;
    private Integer totalQuestions;
    private Long timeLimit;
    private String action;

    // ✅ Constructor using QuestionResponse directly
    public QuestionMessage(String sessionId, String senderId, QuestionResponse question,
                           Integer questionNumber, Integer totalQuestions, Long timeLimit, String action) {
        super("QUESTION", sessionId, senderId);
        this.question = question;
        this.questionNumber = questionNumber;
        this.totalQuestions = totalQuestions;
        this.timeLimit = timeLimit;
        this.action = action;
    }

    // ✅ Constructor using domain Question (maps to QuestionResponse)
    public QuestionMessage(String sessionId, String hostName, Question question,
                           int questionNumber, int totalQuestions, int timeLimit, String action) {
        super("QUESTION", sessionId, hostName);

        // Convert Question -> QuestionResponse
        this.question = new QuestionResponse(
                question.getId(),
                question.getText(),
                question.getType(),
                question.getQuestionOrder(),
                question.getTimeLimit(),
                question.getPoints(),
                question.getImageUrl(),
                mapOptions(question.getOptions())
        );

        this.questionNumber = questionNumber;
        this.totalQuestions = totalQuestions;
        this.timeLimit = (long) timeLimit;
        this.action = action;
    }

    private static List<OptionResponse> mapOptions(List<Option> options) {
        if (options == null) return List.of();
        return options.stream()
                .map(o -> new OptionResponse(
                        o.getId(),
                        o.getOptionText(),
                        o.getOptionOrder(),
                        o.getCreatedAt(),
                        o.getIsCorrected()
                ))
                .toList();
    }

}
