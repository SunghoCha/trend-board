package com.sungho.trendboard.infra.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.SplittableRandom;

@Order(2)
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seeder.vote.enabled", havingValue = "true")
public class PostVoteSeeder implements CommandLineRunner {

    private static final long TOTAL_POSTS = 300_000L;

    private static final int TOTAL_VOTES = 1_000_000;
    private static final int BATCH_SIZE = 5000;

    private static final long HOT_POST_COUNT = 50_000L;
    private static final double HOT_VOTE_SHARE = 0.80;

    private static final long USER_COUNT = 100_000L;
    private static final double LIKE_RATIO = 0.90;

    private static final int RECENT_SECONDS = 21_600; // 6시간

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        log.info("[VoteSeeder] START 실행: totalVotes={}, batchSize={}", TOTAL_VOTES, BATCH_SIZE);

        jdbcTemplate.execute("TRUNCATE TABLE post_votes");
        log.info("[VoteSeeder] TRUNCATE 완료");

        seedVotes();

        log.info("[VoteSeeder] END 완료");
    }

    private void seedVotes() {
        // 전제: UNIQUE(post_id, user_id)
        // 중복이면 “표 변경”으로 흡수해서 seeder가 안 죽게 한다.
        String sql = """
            INSERT INTO post_votes (id, post_id, user_id, vote_type, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
              vote_type = VALUES(vote_type),
              updated_at = VALUES(updated_at)
            """;

        SplittableRandom random = new SplittableRandom(42L);
        LocalDateTime baseTime = LocalDateTime.now(); // PostSeeder와 동일한 스타일

        int attempted = 0;
        long nextId = 1L;

        while (attempted < TOTAL_VOTES) {
            int currentBatchSize = Math.min(BATCH_SIZE, TOTAL_VOTES - attempted);
            long batchStartId = nextId;

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    long id = batchStartId + i;

                    long postId = pickPostId(random);
                    long userId = pickUserId(random);

                    String voteType = (random.nextDouble() < LIKE_RATIO) ? "UP" : "DOWN";

                    int secondsAgo = random.nextInt(RECENT_SECONDS);
                    LocalDateTime createdAt = baseTime.minusSeconds(secondsAgo);
                    Timestamp ts = Timestamp.valueOf(createdAt);

                    ps.setLong(1, id);
                    ps.setLong(2, postId);
                    ps.setLong(3, userId);
                    ps.setString(4, voteType);
                    ps.setTimestamp(5, ts);
                    ps.setTimestamp(6, ts);
                }

                @Override
                public int getBatchSize() {
                    return currentBatchSize;
                }
            });

            attempted += currentBatchSize;
            nextId += currentBatchSize;

            if (attempted % (BATCH_SIZE * 20) == 0 || attempted == TOTAL_VOTES) {
                log.info("[VoteSeeder] INSERT 진행: attempted={}/{}", attempted, TOTAL_VOTES);
            }
        }
    }

    private long pickPostId(SplittableRandom random) {
        if (random.nextDouble() < HOT_VOTE_SHARE) {
            return 1L + random.nextLong(HOT_POST_COUNT);
        }
        long tailCount = TOTAL_POSTS - HOT_POST_COUNT;
        return (HOT_POST_COUNT + 1L) + random.nextLong(tailCount);
    }

    private long pickUserId(SplittableRandom random) {
        return 1L + random.nextLong(USER_COUNT);
    }
}
