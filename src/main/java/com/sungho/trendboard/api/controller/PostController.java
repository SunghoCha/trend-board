package com.sungho.trendboard.api.controller;

import com.sungho.trendboard.application.post.PostService;
import com.sungho.trendboard.application.post.dto.CreatePostRequest;
import com.sungho.trendboard.application.post.dto.CreatePostResponse;
import com.sungho.trendboard.application.post.dto.UpdatePostRequest;
import com.sungho.trendboard.application.post.dto.UpdatePostResponse;
import com.sungho.trendboard.global.domain.CurrentUser;
import com.sungho.trendboard.global.web.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<CreatePostResponse> createPost(@LoginUser CurrentUser currentUser,
                                                   @RequestBody @Valid CreatePostRequest request) {
        CreatePostResponse response = postService.createPost(currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{postId}")
    public ResponseEntity<UpdatePostResponse> updatePost(@PathVariable Long postId,
                                                         @LoginUser CurrentUser currentUser,
                                                         @RequestBody @Valid UpdatePostRequest request) {
        UpdatePostResponse response = postService.updatePost(postId, currentUser, request);
        return ResponseEntity.ok(response);
    }
}
