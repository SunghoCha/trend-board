package com.sungho.trendboard.application.post;

import com.sungho.trendboard.application.post.dto.CreatePostRequest;
import com.sungho.trendboard.application.post.dto.CreatePostResponse;
import com.sungho.trendboard.application.post.dto.UpdatePostRequest;
import com.sungho.trendboard.application.post.dto.UpdatePostResponse;
import com.sungho.trendboard.domain.MemberRole;
import com.sungho.trendboard.domain.Post;
import com.sungho.trendboard.domain.PostCategory;
import com.sungho.trendboard.domain.Tag;
import com.sungho.trendboard.global.domain.CurrentUser;
import com.sungho.trendboard.global.exception.BusinessException;
import com.sungho.trendboard.global.exception.CommonErrorCode;
import com.sungho.trendboard.global.exception.PostErrorCode;
import com.sungho.trendboard.infra.repository.PostRepository;
import com.sungho.trendboard.infra.repository.TagRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @InjectMocks
    private PostService postService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private TagRepository tagRepository;

    /* ==================== createPost ==================== */

    @Test
    @DisplayName("시나리오1: ADVERTISER 역할로 게시글 등록 시 정상 생성된다")
    void createPost_success_whenAdvertiserCreatesPost() {
        // given
        CurrentUser advertiser = new CurrentUser(10L, MemberRole.ADVERTISER);
        CreatePostRequest request = new CreatePostRequest(
                "지라 시나리오1 제목", "지라 시나리오1 내용", PostCategory.FOOD, null, List.of("브런치")
        );

        given(postRepository.save(any(Post.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        CreatePostResponse response = postService.createPost(advertiser, request);

        // then
        then(postRepository).should(times(1)).save(any(Post.class));

        assertThat(response.memberId()).isEqualTo(10L);
        assertThat(response.title()).isEqualTo("지라 시나리오1 제목");
        assertThat(response.content()).isEqualTo("지라 시나리오1 내용");
        assertThat(response.category()).isEqualTo(PostCategory.FOOD);
        assertThat(response.tagIds()).isEmpty();
        assertThat(response.hashtags()).containsExactly("브런치");
    }

    @Test
    @DisplayName("태그 ID가 있으면 태그를 연결해 정상 생성된다")
    void createPost_success_withTagIds() {
        // given
        CurrentUser advertiser = new CurrentUser(10L, MemberRole.ADVERTISER);
        CreatePostRequest request = new CreatePostRequest(
                "태그 포함 제목", "태그 포함 내용", PostCategory.FOOD, List.of(1L, 2L), List.of("브런치")
        );
        Tag tag1 = mock(Tag.class);
        Tag tag2 = mock(Tag.class);
        given(tag1.getId()).willReturn(1L);
        given(tag2.getId()).willReturn(2L);
        given(tagRepository.findAllById(List.of(1L, 2L))).willReturn(List.of(tag1, tag2));
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        CreatePostResponse response = postService.createPost(advertiser, request);

        // then
        then(tagRepository).should(times(1)).findAllById(List.of(1L, 2L));
        then(postRepository).should(times(1)).save(any(Post.class));
        assertThat(response.tagIds()).containsExactly(1L, 2L);
    }

    @Test
    @DisplayName("존재하지 않는 태그 ID가 있으면 TAG_NOT_FOUND 예외를 던진다")
    void createPost_fail_whenTagIdNotFound() {
        // given
        CurrentUser advertiser = new CurrentUser(10L, MemberRole.ADVERTISER);
        CreatePostRequest request = new CreatePostRequest(
                "태그 검증 제목", "태그 검증 내용", PostCategory.FOOD, List.of(1L, 2L), List.of("브런치")
        );
        Tag tag1 = mock(Tag.class);
        given(tagRepository.findAllById(List.of(1L, 2L))).willReturn(List.of(tag1));

        // when & then
        assertThatThrownBy(() -> postService.createPost(advertiser, request))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(PostErrorCode.TAG_NOT_FOUND);

        then(postRepository).should(never()).save(any(Post.class));
    }

    @Test
    @DisplayName("태그 ID가 중복되어도 중복 제거 후 정상 생성된다")
    void createPost_success_whenTagIdsDuplicated() {
        // given
        CurrentUser advertiser = new CurrentUser(10L, MemberRole.ADVERTISER);
        // 서비스에서 tagIds를 distinct 처리하므로 중복 입력이어도 정상 등록되어야 한다.
        CreatePostRequest request = new CreatePostRequest(
                "태그 중복 제목", "태그 중복 내용", PostCategory.FOOD, List.of(1L, 1L, 2L), List.of("브런치")
        );
        Tag tag1 = mock(Tag.class);
        Tag tag2 = mock(Tag.class);
        given(tag1.getId()).willReturn(1L);
        given(tag2.getId()).willReturn(2L);
        given(tagRepository.findAllById(List.of(1L, 2L))).willReturn(List.of(tag1, tag2));
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        CreatePostResponse response = postService.createPost(advertiser, request);

        // then
        then(tagRepository).should(times(1)).findAllById(List.of(1L, 2L));
        then(postRepository).should(times(1)).save(any(Post.class));
        assertThat(response.tagIds()).containsExactly(1L, 2L);
    }

    @Test
    @DisplayName("해시태그가 중복되어도 중복 제거 후 정상 생성된다")
    void createPost_success_whenHashtagsDuplicated() {
        // given
        CurrentUser advertiser = new CurrentUser(10L, MemberRole.ADVERTISER);
        CreatePostRequest request = new CreatePostRequest(
                "해시태그 중복 제목", "해시태그 중복 내용", PostCategory.FOOD, null, List.of("브런치", "브런치")
        );
        given(postRepository.save(any(Post.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        CreatePostResponse response = postService.createPost(advertiser, request);

        // then
        then(postRepository).should(times(1)).save(any(Post.class));
        assertThat(response.hashtags()).containsExactly("브런치");
    }

    @Test
    @DisplayName("해시태그가 공백 차이로 중복되어도 trim 후 중복 제거된다")
    void createPost_success_whenHashtagsDuplicatedByTrim() {
        // given
        CurrentUser advertiser = new CurrentUser(10L, MemberRole.ADVERTISER);
        // 해시태그는 trim 후 비교하므로 공백 차이만 있는 값은 하나로 합쳐진다.
        CreatePostRequest request = new CreatePostRequest(
                "해시태그 정규화 제목", "해시태그 정규화 내용", PostCategory.FOOD, null, List.of("브런치", " 브런치 ")
        );
        given(postRepository.save(any(Post.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        CreatePostResponse response = postService.createPost(advertiser, request);

        // then
        then(postRepository).should(times(1)).save(any(Post.class));
        assertThat(response.hashtags()).containsExactly("브런치");
    }

    @Test
    @DisplayName("USER 역할이면 FORBIDDEN 예외를 던진다")
    void createPost_fail_whenUserRole() {
        // given
        CurrentUser user = new CurrentUser(10L, MemberRole.USER);
        CreatePostRequest request = new CreatePostRequest(
                "권한 테스트 제목", "권한 테스트 내용", PostCategory.FOOD, null, null
        );

        // when & then
        assertThatThrownBy(() -> postService.createPost(user, request))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(CommonErrorCode.FORBIDDEN);

        then(postRepository).should(never()).save(any(Post.class));
        then(tagRepository).should(never()).findAllById(any());
    }

    /* ==================== updatePost ==================== */

    @Test
    @DisplayName("작성자가 게시글을 수정하면 정상 수정된다")
    void update_success_whenAuthorUpdatesPost() {
        // given
        CurrentUser author = new CurrentUser(10L, MemberRole.ADVERTISER);
        // update는 "전체 교체" 의미이므로 중복/공백 입력이 와도 정규화 결과를 기대한다.
        UpdatePostRequest request = new UpdatePostRequest(
                "수정 제목", "수정 내용", PostCategory.BEAUTY, List.of(1L, 1L, 2L), List.of("뷰티", " 뷰티 ")
        );
        Post post = Post.builder()
                .memberId(10L)
                .title("기존 제목")
                .content("기존 내용")
                .category(PostCategory.FOOD)
                .hashtags(List.of("기존해시태그"))
                .build();
        post.addPostTag(mockTag(99L));
        Tag tag1 = mockTag(1L);
        Tag tag2 = mockTag(2L);

        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(tagRepository.findAllById(List.of(1L, 2L))).willReturn(List.of(tag1, tag2));

        // when
        UpdatePostResponse response = postService.updatePost(1L, author, request);

        // then
        assertThat(response.memberId()).isEqualTo(10L);
        assertThat(response.title()).isEqualTo("수정 제목");
        assertThat(response.content()).isEqualTo("수정 내용");
        assertThat(response.category()).isEqualTo(PostCategory.BEAUTY);
        assertThat(response.tagIds()).containsExactly(1L, 2L);
        assertThat(response.hashtags()).containsExactly("뷰티");
    }

    @Test
    @DisplayName("수정 시 tagIds가 null이면 기존 태그는 모두 제거된다")
    void update_success_whenTagIdsNull_thenAllTagsRemoved() {
        // given
        CurrentUser author = new CurrentUser(10L, MemberRole.ADVERTISER);
        // 정책: tagIds == null 이면 "미변경"이 아니라 "전체 제거"로 해석한다.
        UpdatePostRequest request = new UpdatePostRequest(
                "수정 제목", "수정 내용", PostCategory.BEAUTY, null, List.of("뷰티")
        );
        Post post = Post.builder()
                .memberId(10L)
                .title("기존 제목")
                .content("기존 내용")
                .category(PostCategory.FOOD)
                .hashtags(List.of("기존해시태그"))
                .build();
        post.addPostTag(mockTag(99L));
        post.addPostTag(mockTag(100L));

        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        // when
        UpdatePostResponse response = postService.updatePost(1L, author, request);

        // then
        assertThat(response.tagIds()).isEmpty();
        assertThat(response.hashtags()).containsExactly("뷰티");
        // tagIds가 비어 있으므로 태그 조회는 발생하지 않아야 한다.
        then(tagRepository).should(never()).findAllById(any());
    }

    @Test
    @DisplayName("수정 시 tagIds가 빈 리스트면 기존 태그는 모두 제거된다")
    void update_success_whenTagIdsEmpty_thenAllTagsRemoved() {
        // given
        CurrentUser author = new CurrentUser(10L, MemberRole.ADVERTISER);
        // 정책: tagIds == [] 도 null과 동일하게 전체 제거로 처리한다.
        UpdatePostRequest request = new UpdatePostRequest(
                "수정 제목", "수정 내용", PostCategory.BEAUTY, List.of(), List.of("뷰티")
        );
        Post post = Post.builder()
                .memberId(10L)
                .title("기존 제목")
                .content("기존 내용")
                .category(PostCategory.FOOD)
                .hashtags(List.of("기존해시태그"))
                .build();
        post.addPostTag(mockTag(99L));

        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        // when
        UpdatePostResponse response = postService.updatePost(1L, author, request);

        // then
        assertThat(response.tagIds()).isEmpty();
        assertThat(response.hashtags()).containsExactly("뷰티");
        // 태그 조회가 필요 없는 경로인지 함께 검증한다.
        then(tagRepository).should(never()).findAllById(any());
    }

    @Test
    @DisplayName("수정 시 hashtags가 null이면 기존 해시태그는 모두 제거된다")
    void update_success_whenHashtagsNull_thenAllHashtagsRemoved() {
        // given
        CurrentUser author = new CurrentUser(10L, MemberRole.ADVERTISER);
        // 정책: hashtags == null 이면 기존 해시태그를 모두 제거한다.
        UpdatePostRequest request = new UpdatePostRequest(
                "수정 제목", "수정 내용", PostCategory.BEAUTY, List.of(1L), null
        );
        Post post = Post.builder()
                .memberId(10L)
                .title("기존 제목")
                .content("기존 내용")
                .category(PostCategory.FOOD)
                .hashtags(List.of("기존해시태그", "추가해시태그"))
                .build();
        Tag tag1 = mockTag(1L);

        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(tagRepository.findAllById(List.of(1L))).willReturn(List.of(tag1));

        // when
        UpdatePostResponse response = postService.updatePost(1L, author, request);

        // then
        assertThat(response.tagIds()).containsExactly(1L);
        assertThat(response.hashtags()).isEmpty();
    }

    @Test
    @DisplayName("수정 시 hashtags가 빈 리스트면 기존 해시태그는 모두 제거된다")
    void update_success_whenHashtagsEmpty_thenAllHashtagsRemoved() {
        // given
        CurrentUser author = new CurrentUser(10L, MemberRole.ADVERTISER);
        // 정책: hashtags == [] 도 null과 동일하게 전체 제거로 처리한다.
        UpdatePostRequest request = new UpdatePostRequest(
                "수정 제목", "수정 내용", PostCategory.BEAUTY, List.of(1L), List.of()
        );
        Post post = Post.builder()
                .memberId(10L)
                .title("기존 제목")
                .content("기존 내용")
                .category(PostCategory.FOOD)
                .hashtags(List.of("기존해시태그"))
                .build();
        Tag tag1 = mockTag(1L);

        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(tagRepository.findAllById(List.of(1L))).willReturn(List.of(tag1));

        // when
        UpdatePostResponse response = postService.updatePost(1L, author, request);

        // then
        assertThat(response.tagIds()).containsExactly(1L);
        assertThat(response.hashtags()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 게시글이면 POST_NOT_FOUND 예외를 던진다")
    void update_fail_whenPostNotFound() {
        // given
        CurrentUser author = new CurrentUser(10L, MemberRole.ADVERTISER);
        UpdatePostRequest request = new UpdatePostRequest(
                "수정 제목", "수정 내용", PostCategory.BEAUTY, List.of(1L), List.of("뷰티")
        );
        given(postRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.updatePost(1L, author, request))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(PostErrorCode.POST_NOT_FOUND);

        // 게시글 조회에서 실패했으므로 태그 조회로 진행되면 안 된다.
        then(tagRepository).should(never()).findAllById(any());
    }

    @Test
    @DisplayName("작성자가 아니면 FORBIDDEN 예외를 던진다")
    void update_fail_whenNotAuthor() {
        // given
        CurrentUser requester = new CurrentUser(10L, MemberRole.ADVERTISER);
        UpdatePostRequest request = new UpdatePostRequest(
                "수정 제목", "수정 내용", PostCategory.BEAUTY, List.of(1L), List.of("뷰티")
        );
        Post post = Post.builder()
                .memberId(20L)
                .title("기존 제목")
                .content("기존 내용")
                .category(PostCategory.FOOD)
                .hashtags(List.of("기존해시태그"))
                .build();
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> postService.updatePost(1L, requester, request))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(CommonErrorCode.FORBIDDEN);

        // 소유자 검증에서 실패하면 하위 태그 조회는 실행되지 않아야 한다.
        then(tagRepository).should(never()).findAllById(any());
    }

    @Test
    @DisplayName("수정 시 USER 역할이면 FORBIDDEN 예외를 던진다")
    void update_fail_whenUserRole() {
        // given
        CurrentUser user = new CurrentUser(10L, MemberRole.USER);
        UpdatePostRequest request = new UpdatePostRequest(
                "수정 제목", "수정 내용", PostCategory.BEAUTY, List.of(1L), List.of("뷰티")
        );

        // when & then
        assertThatThrownBy(() -> postService.updatePost(1L, user, request))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(CommonErrorCode.FORBIDDEN);

        // 역할 검증에서 조기 실패해야 하므로 저장소 접근 자체가 없어야 한다.
        then(postRepository).should(never()).findById(any());
        then(tagRepository).should(never()).findAllById(any());
    }

    @Test
    @DisplayName("수정 시 존재하지 않는 태그 ID가 있으면 TAG_NOT_FOUND 예외를 던진다")
    void update_fail_whenTagIdNotFound() {
        // given
        CurrentUser author = new CurrentUser(10L, MemberRole.ADVERTISER);
        UpdatePostRequest request = new UpdatePostRequest(
                "수정 제목", "수정 내용", PostCategory.BEAUTY, List.of(1L, 2L), List.of("뷰티")
        );
        Post post = Post.builder()
                .memberId(10L)
                .title("기존 제목")
                .content("기존 내용")
                .category(PostCategory.FOOD)
                .hashtags(List.of("기존해시태그"))
                .build();
        Tag tag1 = mock(Tag.class);

        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(tagRepository.findAllById(List.of(1L, 2L))).willReturn(List.of(tag1));

        // when & then
        assertThatThrownBy(() -> postService.updatePost(1L, author, request))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(PostErrorCode.TAG_NOT_FOUND);
    }

    private Tag mockTag(Long id) {
        Tag tag = mock(Tag.class);
        given(tag.getId()).willReturn(id);
        return tag;
    }
}
