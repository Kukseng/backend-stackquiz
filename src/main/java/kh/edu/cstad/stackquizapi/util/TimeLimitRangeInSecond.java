package kh.edu.cstad.stackquizapi.util;

import lombok.Getter;

@Getter
public enum TimeLimitRangeInSecond {

    FIVE(5),
    TEN(10),
    FIFTEEN(15),
    TWENTY(20),
    THIRTY(30);

    private final int seconds;

    TimeLimitRangeInSecond(int seconds) {
        this.seconds = seconds;
    }

}

