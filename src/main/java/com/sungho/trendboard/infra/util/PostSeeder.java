package com.sungho.trendboard.infra.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Order(1)
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seeder.post.enabled", havingValue = "true")
public class PostSeeder implements CommandLineRunner {

    private static final int TOTAL = 300_000;
    private static final int BATCH_SIZE = 1000;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        log.info("============== SEEDER START ==============");
        long startTime = System.currentTimeMillis();

        jdbcTemplate.execute("TRUNCATE TABLE posts");
        log.info("Table Truncated. Starting Insert...");

        seed();

        long endTime = System.currentTimeMillis();
        log.info("============== SEEDER END (Total: {}ms) ==============", endTime - startTime);
    }

    private void seed() {
        String sql = """
        INSERT INTO posts (id, author_id, title, content, created_at, updated_at, up_count, down_count, version)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        LocalDateTime bastTime = LocalDateTime.now();
        for (int start = 1; start <= TOTAL; start += BATCH_SIZE) {
            int end = Math.min(start + BATCH_SIZE - 1, TOTAL); // [start, end]
            batchInsert(sql, start, end, bastTime);

            double percent = ((double) end / TOTAL) * 100;
            String percentStr = String.format("%.1f", percent);
            log.info("Inserting... {} / {} ({}%)", end, TOTAL, percentStr);
        }
    }

    private void batchInsert(String sql, int start, int end, LocalDateTime baseTime) {
        int batchCount = end - start + 1;

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                int n = start + i; // i는 0 ~ batchCont - 1

                long id = n;
                long authorId =  ((long) (n - 1) % 1000) + 1L;
                String title = "title-" + n;
                String content = "content-" + n;
                LocalDateTime createdAt = baseTime.minusSeconds(n % 86_400L);
                Timestamp ts = Timestamp.valueOf(createdAt);

                ps.setLong(1, id);
                ps.setLong(2, authorId);
                ps.setString(3, title);
                ps.setString(4, content);
                ps.setTimestamp(5, ts);
                ps.setTimestamp(6, ts);

                ps.setLong(7, 0L); // up_count
                ps.setLong(8, 0L); // down_count
                ps.setLong(9, 0L); // version (초기값)


            }

            @Override
            public int getBatchSize() {
                return batchCount;
            }
        });
    }


}
