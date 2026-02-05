package com.sungho.trendboard.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Point {

    private final long value;

    public static Point of(long value) {
        return new Point(value);
    }

    public Point add(long delta) {
        long newValue = Math.addExact(value, delta);
        return new Point(newValue);
    }

}
