package com.sungho.trendboard.applicatioin;

import com.sungho.trendboard.api.dto.CreatePostRequest;
import com.sungho.trendboard.domain.Post;
import com.sungho.trendboard.global.util.Snowflake;
import com.sungho.trendboard.infra.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final Snowflake snowflake;
    private final PostRepository postRepository;

    public Post create(Long userId, CreatePostRequest request) {
        Post post = Post.builder()
                .id(snowflake.nextId())
                .authorId(userId)
                .title(request.title())
                .content(request.content())
                .build();
        return postRepository.save(post);
    }
}
