package com.sungho.trendboard.api.controller;

import com.sungho.trendboard.api.dto.*;
import com.sungho.trendboard.application.PostService;
import com.sungho.trendboard.application.dto.PostDetail;
import com.sungho.trendboard.global.domain.CurrentUser;
import com.sungho.trendboard.global.web.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping("/posts")
    public ResponseEntity<CreatePostResponse> createPost(@LoginUser CurrentUser currentUser,
                                                         @RequestBody @Valid CreatePostRequest request) {
        Long postId = postService.createPost(currentUser.getId(), request); // 커맨드객체는 나중에
        URI location = URI.create("/api/v1/posts/" + postId);
        return ResponseEntity.created(location).body(new CreatePostResponse(postId));
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<GetPostResponse> getPost(@LoginUser(required = false) CurrentUser currentUser,
                                                   @PathVariable Long postId) {
        PostDetail postDetail = postService.getPost(postId);
        // 해당 글의 주인인지 체크
        boolean isOwner = currentUser != null && postDetail.authorId().equals(currentUser.getId());
        return ResponseEntity.ok(GetPostResponse.of(postDetail, isOwner));
    }

    @GetMapping("/posts")
    public ResponseEntity<GetPostListResponse> getPosts(@ModelAttribute PostSearch postSearch) {
        log.info("postSearch page : {}, size: {}", postSearch.getPage(), postSearch.getSize());
        GetPostListResponse response = postService.getPostList(postSearch);
        return ResponseEntity.ok(response);
    }

}
