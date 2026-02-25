package com.sungho.trendboard.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sungho.trendboard.application.post.dto.CreatePostRequest;
import com.sungho.trendboard.application.post.dto.UpdatePostRequest;
import com.sungho.trendboard.domain.MemberRole;
import com.sungho.trendboard.domain.Post;
import com.sungho.trendboard.domain.PostCategory;
import com.sungho.trendboard.domain.Tag;
import com.sungho.trendboard.global.security.WithAccount;
import com.sungho.trendboard.infra.repository.PostRepository;
import com.sungho.trendboard.infra.repository.TagRepository;
import com.sungho.trendboard.support.MySqlContainerSupport;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PostControllerIntegrationTest extends MySqlContainerSupport {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TagRepository tagRepository;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();
        tagRepository.deleteAll();
    }

    @Test
    @DisplayName("통합: ADVERTISER가 게시글을 생성하면 201을 반환한다")
    @WithAccount(memberId = 1L, role = MemberRole.ADVERTISER)
    void createPost_withAdvertiserRole_thenReturns201() throws Exception {
        // given
        Tag tag1 = tagRepository.save(Tag.create("맛집"));
        Tag tag2 = tagRepository.save(Tag.create("서울"));

        CreatePostRequest request = new CreatePostRequest(
                "생성 제목",
                "생성 내용",
                PostCategory.FOOD,
                List.of(tag1.getId(), tag2.getId()),
                List.of("핫플", "신상")
        );

        // when & then
        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.memberId").value(1L))
                .andExpect(jsonPath("$.title").value("생성 제목"))
                .andExpect(jsonPath("$.category").value("FOOD"))
                .andExpect(jsonPath("$.tagIds", Matchers.hasSize(2)))
                .andExpect(jsonPath("$.tagIds", Matchers.hasItems(tag1.getId(), tag2.getId())))
                .andExpect(jsonPath("$.hashtags", Matchers.containsInAnyOrder("핫플", "신상")));
    }

    @Test
    @DisplayName("통합: USER 역할은 게시글 생성 시 403을 반환한다")
    @WithAccount(memberId = 2L, role = MemberRole.USER)
    void createPost_withUserRole_thenReturns403() throws Exception {
        // given
        CreatePostRequest request = new CreatePostRequest(
                "생성 제목",
                "생성 내용",
                PostCategory.FOOD,
                List.of(),
                List.of("핫플")
        );

        // when & then
        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.code").value("COMMON-FORBIDDEN"));
    }

    @Test
    @DisplayName("통합: 생성 시 존재하지 않는 태그가 포함되면 400을 반환한다")
    @WithAccount(memberId = 1L, role = MemberRole.ADVERTISER)
    void createPost_whenTagNotFound_thenReturns400() throws Exception {
        // given
        CreatePostRequest request = new CreatePostRequest(
                "생성 제목",
                "생성 내용",
                PostCategory.FOOD,
                List.of(999999L),
                List.of("핫플")
        );

        // when & then
        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("POST-TAG_NOT_FOUND"));
    }

    @Test
    @DisplayName("통합: 작성자가 게시글을 수정하면 DB와 응답에 변경이 반영된다")
    @WithAccount(memberId = 1L, role = MemberRole.ADVERTISER)
    void updatePost_whenAuthorUpdates_thenReflectsInResponseAndDatabase() throws Exception {
        // given
        Tag oldTag = tagRepository.save(Tag.create("기존태그"));
        Tag newTag1 = tagRepository.save(Tag.create("신규태그1"));
        Tag newTag2 = tagRepository.save(Tag.create("신규태그2"));

        Post post = Post.builder()
                .memberId(1L)
                .title("기존 제목")
                .content("기존 내용")
                .category(PostCategory.FOOD)
                .hashtags(List.of("기존해시태그"))
                .build();
        post.addPostTag(oldTag);
        Post saved = postRepository.saveAndFlush(post);

        UpdatePostRequest request = new UpdatePostRequest(
                "수정 제목",
                "수정 내용",
                PostCategory.BEAUTY,
                List.of(newTag1.getId(), newTag2.getId()),
                List.of("뷰티")
        );

        // when & then
        mockMvc.perform(put("/api/v1/posts/{postId}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.title").value("수정 제목"))
                .andExpect(jsonPath("$.content").value("수정 내용"))
                .andExpect(jsonPath("$.category").value("BEAUTY"))
                .andExpect(jsonPath("$.tagIds", Matchers.hasSize(2)))
                .andExpect(jsonPath("$.tagIds", Matchers.hasItems(newTag1.getId(), newTag2.getId())))
                .andExpect(jsonPath("$.hashtags", Matchers.contains("뷰티")));
    }

    @Test
    @DisplayName("통합: 작성자가 아니면 게시글 수정 시 403을 반환한다")
    @WithAccount(memberId = 2L, role = MemberRole.ADVERTISER)
    void updatePost_whenNotAuthor_thenReturns403() throws Exception {
        // given
        Tag tag = tagRepository.save(Tag.create("태그"));

        Post post = Post.builder()
                .memberId(1L)
                .title("기존 제목")
                .content("기존 내용")
                .category(PostCategory.FOOD)
                .hashtags(List.of("기존해시태그"))
                .build();
        post.addPostTag(tag);
        Post saved = postRepository.saveAndFlush(post);

        UpdatePostRequest request = new UpdatePostRequest(
                "수정 제목",
                "수정 내용",
                PostCategory.BEAUTY,
                List.of(tag.getId()),
                List.of("뷰티")
        );

        // when & then
        mockMvc.perform(put("/api/v1/posts/{postId}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.code").value("COMMON-FORBIDDEN"));
    }

    @Test
    @DisplayName("통합: USER 역할이면 게시글 수정 시 403을 반환한다")
    @WithAccount(memberId = 1L, role = MemberRole.USER)
    void updatePost_withUserRole_thenReturns403() throws Exception {
        // given
        Tag tag = tagRepository.save(Tag.create("태그"));

        Post post = Post.builder()
                .memberId(1L)
                .title("기존 제목")
                .content("기존 내용")
                .category(PostCategory.FOOD)
                .hashtags(List.of("기존해시태그"))
                .build();
        post.addPostTag(tag);
        Post saved = postRepository.saveAndFlush(post);

        UpdatePostRequest request = new UpdatePostRequest(
                "수정 제목",
                "수정 내용",
                PostCategory.BEAUTY,
                List.of(tag.getId()),
                List.of("뷰티")
        );

        // when & then
        mockMvc.perform(put("/api/v1/posts/{postId}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.code").value("COMMON-FORBIDDEN"));
    }

    @Test
    @DisplayName("통합: 수정 대상 게시글이 없으면 404를 반환한다")
    @WithAccount(memberId = 1L, role = MemberRole.ADVERTISER)
    void updatePost_whenPostNotFound_thenReturns404() throws Exception {
        // given
        UpdatePostRequest request = new UpdatePostRequest(
                "수정 제목",
                "수정 내용",
                PostCategory.BEAUTY,
                List.of(),
                List.of("뷰티")
        );

        // when & then
        mockMvc.perform(put("/api/v1/posts/{postId}", 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("POST-NOT_FOUND"));
    }

    @Test
    @DisplayName("통합: 수정 시 존재하지 않는 태그가 포함되면 400을 반환한다")
    @WithAccount(memberId = 1L, role = MemberRole.ADVERTISER)
    void updatePost_whenTagNotFound_thenReturns400() throws Exception {
        // given
        Post post = Post.builder()
                .memberId(1L)
                .title("기존 제목")
                .content("기존 내용")
                .category(PostCategory.FOOD)
                .hashtags(List.of("기존해시태그"))
                .build();
        Post saved = postRepository.saveAndFlush(post);

        UpdatePostRequest request = new UpdatePostRequest(
                "수정 제목",
                "수정 내용",
                PostCategory.BEAUTY,
                List.of(999999L),
                List.of("뷰티")
        );

        // when & then
        mockMvc.perform(put("/api/v1/posts/{postId}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("POST-TAG_NOT_FOUND"));
    }

    @Test
    @DisplayName("통합: tagIds와 hashtags가 null이면 기존 연관 데이터가 모두 제거된다")
    @WithAccount(memberId = 1L, role = MemberRole.ADVERTISER)
    void updatePost_withNullTagIdsAndHashtags_thenClearsAllCollections() throws Exception {
        // given
        Tag tag = tagRepository.save(Tag.create("기존태그"));

        Post post = Post.builder()
                .memberId(1L)
                .title("기존 제목")
                .content("기존 내용")
                .category(PostCategory.FOOD)
                .hashtags(List.of("기존해시태그"))
                .build();
        post.addPostTag(tag);
        Post saved = postRepository.saveAndFlush(post);

        UpdatePostRequest request = new UpdatePostRequest(
                "수정 제목",
                "수정 내용",
                PostCategory.BEAUTY,
                null,
                null
        );

        // when & then
        mockMvc.perform(put("/api/v1/posts/{postId}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.tagIds", Matchers.empty()))
                .andExpect(jsonPath("$.hashtags", Matchers.empty()));


    }
}
