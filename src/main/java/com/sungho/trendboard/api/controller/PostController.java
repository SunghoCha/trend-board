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

    /**
     * Create a new post on behalf of the authenticated user.
     *
     * @param currentUser the authenticated user creating the post
     * @param request     the request payload containing the post's data
     * @return            a ResponseEntity containing the created post and HTTP status 201 (Created)
     */
    @PostMapping
    public ResponseEntity<CreatePostResponse> createPost(@LoginUser CurrentUser currentUser,
                                                   @RequestBody @Valid CreatePostRequest request) {
        CreatePostResponse response = postService.createPost(currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Updates an existing post identified by {@code postId} with the provided data on behalf of the authenticated user.
     *
     * @param postId       the id of the post to update
     * @param currentUser  the authenticated user performing the update
     * @param request      the update payload containing fields to change
     * @return              the updated post representation
     */
    @PutMapping("/{postId}")
    public ResponseEntity<UpdatePostResponse> updatePost(@PathVariable Long postId,
                                                         @LoginUser CurrentUser currentUser,
                                                         @RequestBody @Valid UpdatePostRequest request) {
        UpdatePostResponse response = postService.updatePost(postId, currentUser, request);
        return ResponseEntity.ok(response);
    }
}
