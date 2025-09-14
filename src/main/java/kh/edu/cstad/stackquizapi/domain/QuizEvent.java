package kh.edu.cstad.stackquizapi.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizEvent {

    private String type;

    private String sessionId;

    private String sender;

    private Object payload;

}

