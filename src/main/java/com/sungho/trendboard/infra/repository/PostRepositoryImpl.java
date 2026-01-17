package com.sungho.trendboard.infra.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sungho.trendboard.application.dto.PostListItem;
import com.sungho.trendboard.domain.QPost;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import static com.sungho.trendboard.domain.QPost.post;

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
}
