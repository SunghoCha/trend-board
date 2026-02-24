package com.sungho.trendboard.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sungho.trendboard.application.post.PostService;
import com.sungho.trendboard.application.post.dto.CreatePostRequest;
import com.sungho.trendboard.application.post.dto.CreatePostResponse;
import com.sungho.trendboard.domain.MemberRole;
import com.sungho.trendboard.domain.PostCategory;
import com.sungho.trendboard.global.config.SecurityConfig;
import com.sungho.trendboard.global.config.WebMvcConfig;
import com.sungho.trendboard.global.exception.BusinessException;
import com.sungho.trendboard.global.exception.CommonErrorCode;
import com.sungho.trendboard.global.exception.PostErrorCode;
import com.sungho.trendboard.global.security.WithAccount;
import org.hamcrest.Matchers;
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
    @WithAccount(memberId = 1L, role = MemberRole.ADVERTISER)
    void ADVERTISER가_게시글을_정상_등록한다() throws Exception {
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
        mockMvc.perform(post("/api/posts")
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
    @WithAccount(memberId = 1L, role = MemberRole.ADVERTISER)
    void 제목이_비어있으면_400_에러를_반환한다() throws Exception {
        // given
        CreatePostRequest request = new CreatePostRequest(
                "", "테스트 내용", PostCategory.FOOD, null, null
        );

        // when & then
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("COMMON-INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[0].field").value("title"));
    }

    @Test
    @WithAccount(memberId = 1L, role = MemberRole.ADVERTISER)
    void 제목이_50자를_초과하면_400_에러를_반환한다() throws Exception {
        // given
        String longTitle = "가".repeat(51);
        CreatePostRequest request = new CreatePostRequest(
                longTitle, "테스트 내용", PostCategory.FOOD, null, null
        );

        // when & then
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("COMMON-INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[0].field").value("title"));
    }

    @Test
    @WithAccount(memberId = 1L, role = MemberRole.ADVERTISER)
    void 내용이_비어있으면_400_에러를_반환한다() throws Exception {
        // given
        CreatePostRequest request = new CreatePostRequest(
                "테스트 제목", "", PostCategory.FOOD, null, null
        );

        // when & then
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("COMMON-INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[0].field").value("content"));
    }

    @Test
    @WithAccount(memberId = 1L, role = MemberRole.ADVERTISER)
    void 카테고리가_없으면_400_에러를_반환한다() throws Exception {
        // given
        CreatePostRequest request = new CreatePostRequest(
                "테스트 제목", "테스트 내용", null, null, null
        );

        // when & then
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("COMMON-INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[0].field").value("category"));
    }

    @Test
    @WithAccount(memberId = 2L, role = MemberRole.USER)
    void USER_역할이면_403_에러를_반환한다() throws Exception {
        // given
        CreatePostRequest request = new CreatePostRequest(
                "테스트 제목", "테스트 내용", PostCategory.FOOD, null, null
        );

        given(postService.createPost(any(), any(CreatePostRequest.class)))
                .willThrow(new BusinessException(CommonErrorCode.FORBIDDEN));

        // when & then
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.code").value("COMMON-FORBIDDEN"));
    }

    @Test
    @WithAccount(memberId = 1L, role = MemberRole.ADVERTISER)
    void 해시태그가_공백이면_400_에러를_반환한다() throws Exception {
        // given
        CreatePostRequest request = new CreatePostRequest(
                "테스트 제목", "테스트 내용", PostCategory.FOOD, List.of(1L), List.of("맛집", " ")
        );

        // when & then
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("COMMON-INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[0].field", Matchers.containsString("hashtags")));
    }

    @Test
    @WithAccount(memberId = 1L, role = MemberRole.ADVERTISER)
    void 해시태그가_중복이면_400_에러를_반환한다() throws Exception {
        // given
        CreatePostRequest request = new CreatePostRequest(
                "테스트 제목", "테스트 내용", PostCategory.FOOD, List.of(1L), List.of("맛집", "맛집")
        );

        // when & then
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("COMMON-INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[0].field", Matchers.containsString("hashtags")));
    }

    @Test
    @WithAccount(memberId = 1L, role = MemberRole.ADVERTISER)
    void 해시태그에_null이_포함되면_400_에러를_반환한다() throws Exception {
        // given
        CreatePostRequest request = new CreatePostRequest(
                "테스트 제목", "테스트 내용", PostCategory.FOOD, List.of(1L), Arrays.asList("맛집", null)
        );

        // when & then
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("COMMON-INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[0].field", Matchers.containsString("hashtags")));
    }

    @Test
    @WithAccount(memberId = 1L, role = MemberRole.ADVERTISER)
    void 해시태그가_50자를_초과하면_400_에러를_반환한다() throws Exception {
        // given
        String longHashtag = "가".repeat(51);
        CreatePostRequest request = new CreatePostRequest(
                "테스트 제목", "테스트 내용", PostCategory.FOOD, List.of(1L), List.of(longHashtag)
        );

        // when & then
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("COMMON-INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[0].field", Matchers.containsString("hashtags")));
    }

    @Test
    @WithAccount(memberId = 1L, role = MemberRole.ADVERTISER)
    void 태그ID가_중복이면_400_에러를_반환한다() throws Exception {
        // given
        CreatePostRequest request = new CreatePostRequest(
                "테스트 제목", "테스트 내용", PostCategory.FOOD, List.of(1L, 1L), List.of("맛집")
        );

        // when & then
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("COMMON-INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[0].field", Matchers.containsString("tagIds")));
    }

    @Test
    @WithAccount(memberId = 1L, role = MemberRole.ADVERTISER)
    void 태그ID에_null이_포함되면_400_에러를_반환한다() throws Exception {
        // given
        CreatePostRequest request = new CreatePostRequest(
                "테스트 제목", "테스트 내용", PostCategory.FOOD, Arrays.asList(1L, null), List.of("맛집")
        );

        // when & then
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("COMMON-INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[0].field", Matchers.containsString("tagIds")));
    }

    @Test
    @WithAccount(memberId = 1L, role = MemberRole.ADVERTISER)
    void 태그ID가_0이하면_400_에러를_반환한다() throws Exception {
        // given
        CreatePostRequest request = new CreatePostRequest(
                "테스트 제목", "테스트 내용", PostCategory.FOOD, List.of(0L), List.of("맛집")
        );

        // when & then
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("COMMON-INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[0].field", Matchers.containsString("tagIds")));
    }

    @Test
    @WithAccount(memberId = 1L, role = MemberRole.ADVERTISER)
    void 태그ID가_10개를_초과하면_400_에러를_반환한다() throws Exception {
        // given
        CreatePostRequest request = new CreatePostRequest(
                "테스트 제목",
                "테스트 내용",
                PostCategory.FOOD,
                List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L),
                List.of("맛집")
        );

        // when & then
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("COMMON-INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[0].field", Matchers.containsString("tagIds")));
    }

    @Test
    @WithAccount(memberId = 1L, role = MemberRole.ADVERTISER)
    void 해시태그가_10개를_초과하면_400_에러를_반환한다() throws Exception {
        // given
        CreatePostRequest request = new CreatePostRequest(
                "테스트 제목",
                "테스트 내용",
                PostCategory.FOOD,
                List.of(1L),
                List.of("a1", "a2", "a3", "a4", "a5", "a6", "a7", "a8", "a9", "a10", "a11")
        );

        // when & then
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("COMMON-INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[0].field", Matchers.containsString("hashtags")));
    }

    @Test
    @WithAccount(memberId = 1L, role = MemberRole.ADVERTISER)
    void 존재하지_않는_태그ID면_400_에러를_반환한다() throws Exception {
        // given
        CreatePostRequest request = new CreatePostRequest(
                "테스트 제목", "테스트 내용", PostCategory.FOOD, List.of(999999L), List.of("맛집")
        );

        given(postService.createPost(any(), any(CreatePostRequest.class)))
                .willThrow(new BusinessException(PostErrorCode.TAG_NOT_FOUND));

        // when & then
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("POST-TAG_NOT_FOUND"));
    }
}
