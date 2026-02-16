package com.sungho.trendboard.application;

import com.sungho.trendboard.api.dto.*;
import com.sungho.trendboard.application.dto.PostDetail;
import com.sungho.trendboard.application.dto.PostListItem;
import com.sungho.trendboard.domain.Post;
import com.sungho.trendboard.domain.exception.PostNotFoundException;
import com.sungho.trendboard.global.util.Snowflake;
import com.sungho.trendboard.infra.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PostService {

    private final Snowflake snowflake;
    private final PostRepository postRepository;

    public Long createPost(Long userId, CreatePostRequest request) {
        Post post = Post.builder()
                .id(snowflake.nextId())
                .authorId(userId)
                .title(request.title())
                .content(request.content())
                .build();
        Post savedPost = postRepository.save(post);
        return savedPost.getId();
    }

    @Transactional(readOnly = true)
    public GetPostResponse getPost(Long viewerId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
        boolean isOwner = post.getAuthorId().equals(viewerId);

        return GetPostResponse.of(post, isOwner);
    }

    @Transactional(readOnly = true)
    public GetPostListResponse getPostList(PostSearch postSearch) {
        int page = postSearch.normalizedPage();
        int size = postSearch.normalizedSize();

        // size+1로 조회해서 다음 페이지 존재 여부(hasNext) 판정
        List<PostListItem> postRows = postRepository.findPostListCoveringIdsThenIn(postSearch.offset(), size + 1);
        boolean hasNext = postRows.size() > size;
        List<PostListItem> postSliced = hasNext ? postRows.subList(0, size) : postRows;

        return GetPostListResponse.of(toItems(postSliced), page, size, hasNext);
    }

    // 커서기반 성능 테스트용. 게시판에선 사용안함
    @Transactional(readOnly = true)
    public GetPostCursorListResponse getPostListByCursor(PostCursor cursor, Integer size) {
        long startNs = System.nanoTime();

        List<PostListItem> rows = postRepository.findPostListByCursor(cursor, size + 1);
        boolean hasNext = rows.size() > size;
        List<PostListItem> postSliced = hasNext ? rows.subList(0, size) : rows;

        Long nextCursorId = null;
        if (hasNext && !postSliced.isEmpty()) {
            PostListItem last = postSliced.get(postSliced.size() - 1);
            nextCursorId = last.postId();
        }
        long elapsedMs = (System.nanoTime() - startNs) / 1_000_000;
        log.info("[PostListCursor] 조회 완료:  cursorId={}, size={}, rows={}, hasNext={}, elapsedMs={}",
                cursor == null ? null : cursor.id(),
                size,
                postSliced.size(),
                hasNext,
                elapsedMs
        );

        return GetPostCursorListResponse.of(
                toCursorItems(postSliced),
                size,
                hasNext,
                nextCursorId
        );

    }

    private static List<GetPostCursorListResponse.Item> toCursorItems(List<PostListItem> postSliced) {
        return postSliced.stream()
                .map(row -> new GetPostCursorListResponse.Item(
                        row.postId(),
                        row.authorId(),
                        row.title(),
                        row.createdAt()
                ))
                .toList();
    }

    private static List<GetPostListResponse.Item> toItems(List<PostListItem> postSliced) {
        return postSliced.stream()
                .map(row -> new GetPostListResponse.Item(
                        row.postId(),
                        row.authorId(),
                        row.title(),
                        row.createdAt()
                ))
                .toList();
    }
}
