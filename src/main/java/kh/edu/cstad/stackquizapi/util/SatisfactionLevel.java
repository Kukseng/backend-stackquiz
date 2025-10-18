package kh.edu.cstad.stackquizapi.util;

import lombok.Getter;

@Getter
public enum SatisfactionLevel {

    VERY_DISSATISFIED(1),

    DISSATISFIED(2),

    NEUTRAL(3),

    SATISFIED(4),

    VERY_SATISFIED(5);

    private final int level;

    SatisfactionLevel(int level) {
        this.level = level;
    }

}
