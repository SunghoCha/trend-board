package com.sungho.trendboard.application;

import com.sungho.trendboard.api.dto.CreatePostRequest;
import com.sungho.trendboard.api.dto.GetPostListResponse;
import com.sungho.trendboard.api.dto.PostSearch;
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
    public PostDetail getPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
        return PostDetail.from(post);
    }

    @Transactional(readOnly = true)
    public GetPostListResponse getPostList(PostSearch postSearch) {
        long startNs = System.nanoTime();
        int page = postSearch.normalizedPage();
        int size = postSearch.normalizedSize();
        // size+1로 조회해서 다음 페이지 존재 여부(hasNext)를 판정한다.
        List<PostListItem> postRows = postRepository.findPostListCoveringIdsThenIn(postSearch.offset(), size + 1);
        boolean hasNext = postRows.size() > size;
        List<PostListItem> postSliced = hasNext ? postRows.subList(0, size) : postRows;

        long elapsedMs = (System.nanoTime() - startNs) / 1_000_000;
        log.info("[PostList] 조회 소요: page={}, size={}, offset={}, elapsedMs={}ms",
                page, size, postSearch.offset(), elapsedMs);
        return GetPostListResponse.of(toItems(postSliced), page, size, hasNext);
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
