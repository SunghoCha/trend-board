package com.sungho.trendboard.application.post;

import com.sungho.trendboard.application.post.dto.CreatePostRequest;
import com.sungho.trendboard.application.post.dto.CreatePostResponse;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

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

    @Test
    void 시나리오1_ADVERTISER_역할로_게시글_등록시_정상_생성된다() {
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
    void 태그ID가_있으면_태그를_연결해_정상_생성된다() {
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
    void 존재하지_않는_태그ID가_있으면_TAG_NOT_FOUND_예외를_던진다() {
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
    void USER_역할이면_FORBIDDEN_예외를_던진다() {
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
}
