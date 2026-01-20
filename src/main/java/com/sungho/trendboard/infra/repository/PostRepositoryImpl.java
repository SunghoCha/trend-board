package com.sungho.trendboard.infra.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sungho.trendboard.application.dto.PostListItem;
import com.sungho.trendboard.domain.QPost;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static com.sungho.trendboard.domain.QPost.post;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<PostListItem> findPostList(long offset, int limit) {
        return queryFactory
                .select(Projections.constructor(
                        PostListItem.class,
                        post.id,
                        post.authorId,
                        post.title,
                        post.createdAt
                ))
                .from(post)
                .orderBy(post.createdAt.desc(), post.id.desc())
                .offset(offset)
                .limit(limit)
                .fetch();
    }

    @Override
    public List<PostListItem> findPostListCoveringIdsThenIn(long offset, int limit) {
        long t1 = System.nanoTime();
        List<Long> ids = queryFactory
                .select(post.id)
                .from(post)
                .orderBy(post.createdAt.desc(), post.id.desc())
                .offset(offset)
                .limit(limit)
                .fetch();
        long idsMs = (System.nanoTime() - t1) / 1_000_000;
        if (ids.isEmpty()) {
            log.info("[PostListTwoStep] IDS 조회: offset={}, limit={}, idsCount=0, idsMs={}",
                    offset, limit, idsMs);
            return Collections.emptyList();
        }

        long t2 = System.nanoTime();
        List<PostListItem> rows = queryFactory
                .select(Projections.constructor(
                        PostListItem.class,
                        post.id,
                        post.authorId,
                        post.title,
                        post.createdAt
                ))
                .from(post)
                .where(post.id.in(ids))
                .orderBy(post.createdAt.desc(), post.id.desc())
                .fetch();

        long inMs = (System.nanoTime() - t2) / 1_000_000;

        log.info("[PostListTwoStep] 조회: offset={}, limit={}, idsCount={}, idsMs={}, inMs={}, elapsedMs={}",
                offset, limit, ids.size(), idsMs, inMs, idsMs + inMs);
        return rows;
    }


}
