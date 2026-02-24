package com.sungho.trendboard.global.util;

/**
 * 64bit Snowflake ID 생성기
 *
 * 구조: 1bit(부호) + 41bit(타임스탬프) + 10bit(머신ID) + 12bit(시퀀스)
 * - 타임스탬프: 커스텀 epoch 기준 밀리초 (약 69년 사용 가능)
 * - 머신ID: 0~1023 (기본 1)
 * - 시퀀스: 밀리초당 최대 4096개
 */
public class Snowflake {

    // 2025-01-01 00:00:00 UTC
    private static final long CUSTOM_EPOCH = 1735689600000L;

    private static final long MACHINE_ID_BITS = 10L;
    private static final long SEQUENCE_BITS = 12L;

    private static final long MAX_MACHINE_ID = (1L << MACHINE_ID_BITS) - 1;
    private static final long MAX_SEQUENCE = (1L << SEQUENCE_BITS) - 1;

    private final long machineId;
    private long lastTimestamp = -1L;
    private long sequence = 0L;

    public Snowflake(long machineId) {
        if (machineId < 0 || machineId > MAX_MACHINE_ID) {
            throw new IllegalArgumentException("machineId는 0~" + MAX_MACHINE_ID + " 범위여야 합니다.");
        }
        this.machineId = machineId;
    }

    public Snowflake() {
        this(1L);
    }

    public synchronized long nextId() {
        long currentTimestamp = System.currentTimeMillis() - CUSTOM_EPOCH;

        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                currentTimestamp = waitNextMillis(currentTimestamp);
            }
        } else {
            sequence = 0;
        }

        lastTimestamp = currentTimestamp;

        return (currentTimestamp << (MACHINE_ID_BITS + SEQUENCE_BITS))
                | (machineId << SEQUENCE_BITS)
                | sequence;
    }

    private long waitNextMillis(long currentTimestamp) {
        while (currentTimestamp <= lastTimestamp) {
            currentTimestamp = System.currentTimeMillis() - CUSTOM_EPOCH;
        }
        return currentTimestamp;
    }
}
