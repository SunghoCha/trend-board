package com.sungho.trendboard.api.controller;

import com.sungho.trendboard.api.dto.CreatePostRequest;
import com.sungho.trendboard.api.dto.CreatePostResponse;
import com.sungho.trendboard.applicatioin.PostService;
import com.sungho.trendboard.domain.Post;
import com.sungho.trendboard.global.domain.CurrentUser;
import com.sungho.trendboard.global.web.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping("/posts")
    public ResponseEntity<CreatePostResponse> createPost(@LoginUser CurrentUser currentUser,
                                                         @RequestBody @Valid CreatePostRequest request) {
        Post post = postService.create(currentUser.getId(), request);
        CreatePostResponse response = CreatePostResponse.of(post);

        return ResponseEntity.status(201).body(response);
    }
}
