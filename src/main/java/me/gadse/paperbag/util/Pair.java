package me.gadse.paperbag.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Pair {
    @Getter
    private final String key;
    @Getter
    private final String value;

    public Pair(String key, int value) {
        this.key = key;
        this.value = String.valueOf(value);
    }

    public Pair(String key, double value) {
        this.key = key;
        this.value = String.valueOf(value);
    }
}
