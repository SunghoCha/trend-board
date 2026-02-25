package com.sungho.trendboard.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sungho.trendboard.application.post.PostService;
import com.sungho.trendboard.application.post.dto.CreatePostRequest;
import com.sungho.trendboard.application.post.dto.CreatePostResponse;
import com.sungho.trendboard.application.post.dto.UpdatePostRequest;
import com.sungho.trendboard.application.post.dto.UpdatePostResponse;
import com.sungho.trendboard.domain.MemberRole;
import com.sungho.trendboard.domain.PostCategory;
import com.sungho.trendboard.global.config.SecurityConfig;
import com.sungho.trendboard.global.config.WebMvcConfig;
import com.sungho.trendboard.global.exception.BusinessException;
import com.sungho.trendboard.global.exception.CommonErrorCode;
import com.sungho.trendboard.global.exception.PostErrorCode;
import com.sungho.trendboard.global.security.WithAccount;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
@Import({WebMvcConfig.class, SecurityConfig.class})
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private PostService postService;

    @Test
    @DisplayName("ADVERTISER가 게시글을 정상 등록한다")
    @WithAccount(memberId = 1L, role = MemberRole.ADVERTISER)
    void createPost_withAdvertiserRole_returnsCreated() throws Exception {
        // given
        CreatePostRequest request = new CreatePostRequest(
                "테스트 제목", "테스트 내용", PostCategory.FOOD, List.of(1L, 2L), List.of("맛집", "서울")
        );

        CreatePostResponse response = new CreatePostResponse(
                1L, 1L, "테스트 제목", "테스트 내용",
                PostCategory.FOOD, List.of(1L, 2L), List.of("맛집", "서울"), 0,
                LocalDateTime.now(), LocalDateTime.now()
        );

        given(postService.createPost(any(), any(CreatePostRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("테스트 제목"))
                .andExpect(jsonPath("$.category").value("FOOD"))
                .andExpect(jsonPath("$.tagIds[0]").value(1L))
                .andExpect(jsonPath("$.tagIds[1]").value(2L))
                .andExpect(jsonPath("$.hashtags[0]").value("맛집"))
                .andExpect(jsonPath("$.hashtags[1]").value("서울"))
                .andExpect(jsonPath("$.likeCount").value(0));
    }

    @Test
    @DisplayName("제목이 비어있으면 400 에러를 반환한다")
    @WithAccount(memberId = 1L, role = MemberRole.ADVERTISER)
    void createPost_withEmptyTitle_returns400() throws Exception {
        // given
        CreatePostRequest request = new CreatePostRequest(
                "", "테스트 내용", PostCategory.FOOD, null, null
        );

        // when & then
        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("COMMON-INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[*].field", Matchers.hasItem("title")));
    }

    @Test
    @DisplayName("제목이 50자를 초과하면 400 에러를 반환한다")
    @WithAccount(memberId = 1L, role = MemberRole.ADVERTISER)
    void createPost_withTitleExceeding50Chars_returns400() throws Exception {
        // given
        String longTitle = "가".repeat(51);
        CreatePostRequest request = new CreatePostRequest(
                longTitle, "테스트 내용", PostCategory.FOOD, null, null
        );

        // when & then
        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("COMMON-INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[*].field", Matchers.hasItem("title")));
    }

    @Test
    @DisplayName("내용이 비어있으면 400 에러를 반환한다")
    @WithAccount(memberId = 1L, role = MemberRole.ADVERTISER)
    void createPost_withEmptyContent_returns400() throws Exception {
        // given
        CreatePostRequest request = new CreatePostRequest(
                "테스트 제목", "", PostCategory.FOOD, null, null
        );

        // when & then
        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("COMMON-INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[*].field", Matchers.hasItem("content")));
    }

    @Test
    @DisplayName("카테고리가 없으면 400 에러를 반환한다")
    @WithAccount(memberId = 1L, role = MemberRole.ADVERTISER)
    void createPost_withNullCategory_returns400() throws Exception {
        // given
        CreatePostRequest request = new CreatePostRequest(
                "테스트 제목", "테스트 내용", null, null, null
        );

        // when & then
        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("COMMON-INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[*].field", Matchers.hasItem("category")));
    }

    @Test
    @DisplayName("USER 역할이면 403 에러를 반환한다")
    @WithAccount(memberId = 2L, role = MemberRole.USER)
    void createPost_withUserRole_returns403() throws Exception {
        // given
        CreatePostRequest request = new CreatePostRequest(
                "테스트 제목", "테스트 내용", PostCategory.FOOD, null, null
        );

        given(postService.createPost(any(), any(CreatePostRequest.class)))
                .willThrow(new BusinessException(CommonErrorCode.FORBIDDEN));

        // when & then
        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.code").value("COMMON-FORBIDDEN"));
    }

    @Test
    @DisplayName("해시태그가 공백이면 400 에러를 반환한다")
    @WithAccount(memberId = 1L, role = MemberRole.ADVERTISER)
    void createPost_withBlankHashtag_returns400() throws Exception {
        // given
        CreatePostRequest request = new CreatePostRequest(
                "테스트 제목", "테스트 내용", PostCategory.FOOD, List.of(1L), List.of("맛집", " ")
        );

        // when & then
        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("COMMON-INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[*].field", Matchers.hasItem(Matchers.containsString("hashtags"))));
    }

    @Test
    @DisplayName("해시태그가 중복이어도 중복제거후 201 응답을 반환한다")
    @WithAccount(memberId = 1L, role = MemberRole.ADVERTISER)
    void createPost_withDuplicateHashtags_returnsCreatedAfterDedup() throws Exception {
        // given
        CreatePostRequest request = new CreatePostRequest(
                "테스트 제목", "테스트 내용", PostCategory.FOOD, List.of(1L), List.of("맛집", "맛집")
        );
        CreatePostResponse response = new CreatePostResponse(
                1L, 1L, "테스트 제목", "테스트 내용",
                PostCategory.FOOD, List.of(1L), List.of("맛집"), 0,
                LocalDateTime.now(), LocalDateTime.now()
        );

        given(postService.createPost(any(), any(CreatePostRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.hashtags[0]").value("맛집"));
    }

    @Test
    @DisplayName("해시태그에 null이 포함되면 400 에러를 반환한다")
    @WithAccount(memberId = 1L, role = MemberRole.ADVERTISER)
    void createPost_withNullHashtag_returns400() throws Exception {
        // given
        CreatePostRequest request = new CreatePostRequest(
                "테스트 제목", "테스트 내용", PostCategory.FOOD, List.of(1L), Arrays.asList("맛집", null)
        );

        // when & then
        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("COMMON-INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[*].field", Matchers.hasItem(Matchers.containsString("hashtags"))));
    }

    @Test
    @DisplayName("해시태그가 50자를 초과하면 400 에러를 반환한다")
    @WithAccount(memberId = 1L, role = MemberRole.ADVERTISER)
    void createPost_withHashtagExceeding50Chars_returns400() throws Exception {
        // given
        String longHashtag = "가".repeat(51);
        CreatePostRequest request = new CreatePostRequest(
                "테스트 제목", "테스트 내용", PostCategory.FOOD, List.of(1L), List.of(longHashtag)
        );

        // when & then
        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("COMMON-INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[*].field", Matchers.hasItem(Matchers.containsString("hashtags"))));
    }

    @Test
    @DisplayName("태그ID가 중복이어도 중복제거후 201 응답을 반환한다")
    @WithAccount(memberId = 1L, role = MemberRole.ADVERTISER)
    void createPost_withDuplicateTagIds_returnsCreatedAfterDedup() throws Exception {
        // given
        CreatePostRequest request = new CreatePostRequest(
                "테스트 제목", "테스트 내용", PostCategory.FOOD, List.of(1L, 1L), List.of("맛집")
        );
        CreatePostResponse response = new CreatePostResponse(
                1L, 1L, "테스트 제목", "테스트 내용",
                PostCategory.FOOD, List.of(1L), List.of("맛집"), 0,
                LocalDateTime.now(), LocalDateTime.now()
        );

        given(postService.createPost(any(), any(CreatePostRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.tagIds[0]").value(1L));
    }

    @Test
    @DisplayName("태그ID에 null이 포함되면 400 에러를 반환한다")
    @WithAccount(memberId = 1L, role = MemberRole.ADVERTISER)
    void createPost_withNullTagId_returns400() throws Exception {
        // given
        CreatePostRequest request = new CreatePostRequest(
                "테스트 제목", "테스트 내용", PostCategory.FOOD, Arrays.asList(1L, null), List.of("맛집")
        );

        // when & then
        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("COMMON-INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[*].field", Matchers.hasItem(Matchers.containsString("tagIds"))));
    }

    @Test
    @DisplayName("태그ID가 0이하면 400 에러를 반환한다")
    @WithAccount(memberId = 1L, role = MemberRole.ADVERTISER)
    void createPost_withTagIdZeroOrLess_returns400() throws Exception {
        // given
        CreatePostRequest request = new CreatePostRequest(
                "테스트 제목", "테스트 내용", PostCategory.FOOD, List.of(0L), List.of("맛집")
        );

        // when & then
        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("COMMON-INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[*].field", Matchers.hasItem(Matchers.containsString("tagIds"))));
    }

    @Test
    @DisplayName("태그ID가 10개를 초과하면 400 에러를 반환한다")
    @WithAccount(memberId = 1L, role = MemberRole.ADVERTISER)
    void createPost_withTagIdsExceeding10_returns400() throws Exception {
        // given
        CreatePostRequest request = new CreatePostRequest(
                "테스트 제목",
                "테스트 내용",
                PostCategory.FOOD,
                List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L),
                List.of("맛집")
        );

        // when & then
        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("COMMON-INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[*].field", Matchers.hasItem(Matchers.containsString("tagIds"))));
    }

    @Test
    @DisplayName("해시태그가 10개를 초과하면 400 에러를 반환한다")
    @WithAccount(memberId = 1L, role = MemberRole.ADVERTISER)
    void createPost_withHashtagsExceeding10_returns400() throws Exception {
        // given
        CreatePostRequest request = new CreatePostRequest(
                "테스트 제목",
                "테스트 내용",
                PostCategory.FOOD,
                List.of(1L),
                List.of("a1", "a2", "a3", "a4", "a5", "a6", "a7", "a8", "a9", "a10", "a11")
        );

        // when & then
        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("COMMON-INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[*].field", Matchers.hasItem(Matchers.containsString("hashtags"))));
    }

    @Test
    @DisplayName("존재하지 않는 태그ID면 400 에러를 반환한다")
    @WithAccount(memberId = 1L, role = MemberRole.ADVERTISER)
    void createPost_withNonExistentTagId_returns400() throws Exception {
        // given
        CreatePostRequest request = new CreatePostRequest(
                "테스트 제목", "테스트 내용", PostCategory.FOOD, List.of(999999L), List.of("맛집")
        );

        given(postService.createPost(any(), any(CreatePostRequest.class)))
                .willThrow(new BusinessException(PostErrorCode.TAG_NOT_FOUND));

        // when & then
        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("POST-TAG_NOT_FOUND"));
    }

    @Test
    @DisplayName("작성자는 게시글을 정상 수정한다")
    @WithAccount(memberId = 1L, role = MemberRole.ADVERTISER)
    void updatePost_whenAuthorUpdates_returns200() throws Exception {
        // given
        UpdatePostRequest request = new UpdatePostRequest(
                "수정 제목", "수정 내용", PostCategory.BEAUTY, List.of(1L, 2L), List.of("뷰티")
        );
        UpdatePostResponse response = new UpdatePostResponse(
                1L, 1L, "수정 제목", "수정 내용",
                PostCategory.BEAUTY, List.of(1L, 2L), List.of("뷰티"), 0,
                LocalDateTime.now(), LocalDateTime.now()
        );

        given(postService.updatePost(any(), any(), any(UpdatePostRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(put("/api/v1/posts/{postId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("수정 제목"))
                .andExpect(jsonPath("$.category").value("BEAUTY"))
                .andExpect(jsonPath("$.tagIds[0]").value(1L))
                .andExpect(jsonPath("$.tagIds[1]").value(2L))
                .andExpect(jsonPath("$.hashtags[0]").value("뷰티"));

        then(postService).should(times(1)).updatePost(any(), any(), any(UpdatePostRequest.class));
    }

    @Test
    @DisplayName("작성자가 아니면 수정 시 403 에러를 반환한다")
    @WithAccount(memberId = 2L, role = MemberRole.ADVERTISER)
    void updatePost_whenNotAuthor_returns403() throws Exception {
        // given
        UpdatePostRequest request = new UpdatePostRequest(
                "수정 제목", "수정 내용", PostCategory.BEAUTY, List.of(1L), List.of("뷰티")
        );
        given(postService.updatePost(any(), any(), any(UpdatePostRequest.class)))
                .willThrow(new BusinessException(CommonErrorCode.FORBIDDEN));

        // when & then
        mockMvc.perform(put("/api/v1/posts/{postId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.code").value("COMMON-FORBIDDEN"));
    }

    @Test
    @DisplayName("수정 시 USER 역할이면 403 에러를 반환한다")
    @WithAccount(memberId = 1L, role = MemberRole.USER)
    void updatePost_withUserRole_returns403() throws Exception {
        // given
        UpdatePostRequest request = new UpdatePostRequest(
                "수정 제목", "수정 내용", PostCategory.BEAUTY, List.of(1L), List.of("뷰티")
        );
        given(postService.updatePost(any(), any(), any(UpdatePostRequest.class)))
                .willThrow(new BusinessException(CommonErrorCode.FORBIDDEN));

        // when & then
        mockMvc.perform(put("/api/v1/posts/{postId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.code").value("COMMON-FORBIDDEN"));
    }

    @Test
    @DisplayName("수정 대상 게시글이 없으면 404 에러를 반환한다")
    @WithAccount(memberId = 1L, role = MemberRole.ADVERTISER)
    void updatePost_whenPostNotFound_returns404() throws Exception {
        // given
        UpdatePostRequest request = new UpdatePostRequest(
                "수정 제목", "수정 내용", PostCategory.BEAUTY, List.of(1L), List.of("뷰티")
        );
        given(postService.updatePost(any(), any(), any(UpdatePostRequest.class)))
                .willThrow(new BusinessException(PostErrorCode.POST_NOT_FOUND));

        // when & then
        mockMvc.perform(put("/api/v1/posts/{postId}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("POST-NOT_FOUND"));
    }

    @Test
    @DisplayName("수정 시 존재하지 않는 태그ID면 400 에러를 반환한다")
    @WithAccount(memberId = 1L, role = MemberRole.ADVERTISER)
    void updatePost_whenTagNotFound_returns400() throws Exception {
        // given
        UpdatePostRequest request = new UpdatePostRequest(
                "수정 제목", "수정 내용", PostCategory.BEAUTY, List.of(999999L), List.of("뷰티")
        );
        given(postService.updatePost(any(), any(), any(UpdatePostRequest.class)))
                .willThrow(new BusinessException(PostErrorCode.TAG_NOT_FOUND));

        // when & then
        mockMvc.perform(put("/api/v1/posts/{postId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("POST-TAG_NOT_FOUND"));
    }

    @Test
    @DisplayName("수정 시 제목이 비어있으면 400 에러를 반환한다")
    @WithAccount(memberId = 1L, role = MemberRole.ADVERTISER)
    void updatePost_withEmptyTitle_returns400() throws Exception {
        // given
        UpdatePostRequest request = new UpdatePostRequest(
                "", "수정 내용", PostCategory.BEAUTY, List.of(1L), List.of("뷰티")
        );

        // when & then
        mockMvc.perform(put("/api/v1/posts/{postId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("COMMON-INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[*].field", Matchers.hasItem("title")));

        then(postService).should(never()).updatePost(any(), any(), any(UpdatePostRequest.class));
    }

    @Test
    @DisplayName("수정 시 내용이 비어있으면 400 에러를 반환한다")
    @WithAccount(memberId = 1L, role = MemberRole.ADVERTISER)
    void updatePost_withEmptyContent_returns400() throws Exception {
        // given
        UpdatePostRequest request = new UpdatePostRequest(
                "수정 제목", "", PostCategory.BEAUTY, List.of(1L), List.of("뷰티")
        );

        // when & then
        mockMvc.perform(put("/api/v1/posts/{postId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("COMMON-INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[*].field", Matchers.hasItem("content")));

        then(postService).should(never()).updatePost(any(), any(), any(UpdatePostRequest.class));
    }

    @Test
    @DisplayName("수정 시 카테고리가 없으면 400 에러를 반환한다")
    @WithAccount(memberId = 1L, role = MemberRole.ADVERTISER)
    void updatePost_withNullCategory_returns400() throws Exception {
        // given
        UpdatePostRequest request = new UpdatePostRequest(
                "수정 제목", "수정 내용", null, List.of(1L), List.of("뷰티")
        );

        // when & then
        mockMvc.perform(put("/api/v1/posts/{postId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("COMMON-INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[*].field", Matchers.hasItem("category")));

        then(postService).should(never()).updatePost(any(), any(), any(UpdatePostRequest.class));
    }

    @Test
    @DisplayName("수정 시 카테고리 값이 올바르지 않으면 400 에러를 반환한다")
    @WithAccount(memberId = 1L, role = MemberRole.ADVERTISER)
    void updatePost_withInvalidCategory_returns400() throws Exception {
        // given
        String invalidJson = """
                {
                  "title": "수정 제목",
                  "content": "수정 내용",
                  "category": "INVALID_CATEGORY",
                  "tagIds": [1],
                  "hashtags": ["뷰티"]
                }
                """;

        // when & then
        mockMvc.perform(put("/api/v1/posts/{postId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("COMMON-INVALID_INPUT"));

        then(postService).should(never()).updatePost(any(), any(), any(UpdatePostRequest.class));
    }

    @Test
    @DisplayName("수정 시 요청 바디가 비어있으면 400 에러를 반환한다")
    @WithAccount(memberId = 1L, role = MemberRole.ADVERTISER)
    void updatePost_withEmptyRequestBody_returns400() throws Exception {
        // when & then
        mockMvc.perform(put("/api/v1/posts/{postId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("COMMON-INVALID_INPUT"));

        then(postService).should(never()).updatePost(any(), any(), any(UpdatePostRequest.class));
    }

    @Test
    @DisplayName("수정 시 postId 타입이 올바르지 않으면 400 에러를 반환한다")
    @WithAccount(memberId = 1L, role = MemberRole.ADVERTISER)
    void updatePost_withInvalidPostIdType_returns400() throws Exception {
        // given
        UpdatePostRequest request = new UpdatePostRequest(
                "수정 제목", "수정 내용", PostCategory.BEAUTY, List.of(1L), List.of("뷰티")
        );

        // when & then
        mockMvc.perform(put("/api/v1/posts/{postId}", "abc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("COMMON-INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[*].field", Matchers.hasItem("postId")));

        then(postService).should(never()).updatePost(any(), any(), any(UpdatePostRequest.class));
    }

    @Test
    @DisplayName("수정 시 tagIds와 hashtags가 null이어도 200 응답을 반환한다")
    @WithAccount(memberId = 1L, role = MemberRole.ADVERTISER)
    void updatePost_withNullTagIdsAndHashtags_returns200() throws Exception {
        // given
        UpdatePostRequest request = new UpdatePostRequest(
                "수정 제목", "수정 내용", PostCategory.BEAUTY, null, null
        );
        UpdatePostResponse response = new UpdatePostResponse(
                1L, 1L, "수정 제목", "수정 내용",
                PostCategory.BEAUTY, List.of(), List.of(), 0,
                LocalDateTime.now(), LocalDateTime.now()
        );

        given(postService.updatePost(any(), any(), any(UpdatePostRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(put("/api/v1/posts/{postId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.tagIds").isArray())
                .andExpect(jsonPath("$.tagIds").isEmpty())
                .andExpect(jsonPath("$.hashtags").isArray())
                .andExpect(jsonPath("$.hashtags").isEmpty());
    }
}

