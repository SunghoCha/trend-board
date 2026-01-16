package com.sungho.trendboard.application;

import com.sungho.trendboard.api.dto.CreatePostRequest;
import com.sungho.trendboard.application.dto.PostDetail;
import com.sungho.trendboard.domain.Post;
import com.sungho.trendboard.domain.exception.PostNotFoundException;
import com.sungho.trendboard.global.util.Snowflake;
import com.sungho.trendboard.infra.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
