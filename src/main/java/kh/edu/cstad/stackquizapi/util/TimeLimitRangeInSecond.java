package kh.edu.cstad.stackquizapi.util;

import lombok.Getter;

@Getter
public enum TimeLimitRangeInSecond {

    DEFAULT(0),
    FIVE(5),
    SIX(6),
    SEVEN(7),
    EIGHT(8),
    NINE(9),
    TEN(10),
    FIFTEEN(15),
    TWENTY(20),
    THIRTY(30);

    private final int seconds;

    TimeLimitRangeInSecond(int seconds) {
        this.seconds = seconds;
    }

}

