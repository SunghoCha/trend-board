package com.sungho.trendboard.global.util;

import java.util.random.RandomGenerator;

public class Snowflake {
    private static final int UNUSED_BITS = 1;
    private static final int EPOCH_BITS = 41;
    private static final int NODE_ID_BITS = 10;
    private static final int SEQUENCE_BITS = 12;
    private static final long maxNodeId = 1023L;
    private static final long maxSequence = 4095L;
    private final long nodeId = RandomGenerator.getDefault().nextLong(1024L);
    private final long startTimeMillis = 1704067200000L;
    private long lastTimeMillis = 1704067200000L;
    private long sequence = 0L;

    public Snowflake() {
    }

    public synchronized long nextId() {
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis < this.lastTimeMillis) {
            throw new IllegalStateException("Invalid Time");
        } else {
            if (currentTimeMillis == this.lastTimeMillis) {
                this.sequence = this.sequence + 1L & 4095L;
                if (this.sequence == 0L) {
                    currentTimeMillis = this.waitNextMillis(currentTimeMillis);
                }
            } else {
                this.sequence = 0L;
            }

            this.lastTimeMillis = currentTimeMillis;
            return currentTimeMillis - 1704067200000L << 22 | this.nodeId << 12 | this.sequence;
        }
    }

    private long waitNextMillis(long currentTimestamp) {
        while (currentTimestamp <= this.lastTimeMillis) {
            currentTimestamp = System.currentTimeMillis();
        }

        return currentTimestamp;
    }

}
