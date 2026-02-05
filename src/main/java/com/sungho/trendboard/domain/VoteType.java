package com.sungho.trendboard.domain;

public enum VoteType {

    UP(1),
    DOWN(-1);

    private int value;

    VoteType(int value) {
        this.value = value;
    }

    public static VoteType getVoteType(int value) {
        return switch (value) {
            case 1 -> UP;
            case -1 -> DOWN;
            default -> throw new IllegalArgumentException("Invalid VoteType value: " + value);
        };
    }
}
