package com.sungho.trendboard.domain;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class PostTest {

    @Test
    void memberId가_null이면_예외를_던진다() {
        assertThatThrownBy(() -> Post.builder()
                .title("제목")
                .content("내용")
                .category(PostCategory.FOOD)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("memberId는 필수입니다.");
    }

    @Test
    void title이_비어있으면_예외를_던진다() {
        assertThatThrownBy(() -> Post.builder()
                .memberId(1L)
                .title(" ")
                .content("내용")
                .category(PostCategory.FOOD)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("title은 필수입니다.");
    }

    @Test
    void content가_비어있으면_예외를_던진다() {
        assertThatThrownBy(() -> Post.builder()
                .memberId(1L)
                .title("제목")
                .content(" ")
                .category(PostCategory.FOOD)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("content는 필수입니다.");
    }

    @Test
    void category가_null이면_예외를_던진다() {
        assertThatThrownBy(() -> Post.builder()
                .memberId(1L)
                .title("제목")
                .content("내용")
                .category(null)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("category는 필수입니다.");
    }

    @Test
    void hashtags는_방어적_복사로_보관된다() {
        List<String> hashtags = new ArrayList<>(List.of("맛집", "서울"));

        Post post = Post.builder()
                .memberId(1L)
                .title("제목")
                .content("내용")
                .category(PostCategory.FOOD)
                .hashtags(hashtags)
                .build();

        hashtags.add("신규태그");

        assertThat(post.getHashtags()).containsExactly("맛집", "서울");
    }

    @Test
    void addPostTag_정상_태그를_추가한다() {
        Post post = createPost();
        Tag tag = mock(Tag.class);
        given(tag.getId()).willReturn(1L);

        post.addPostTag(tag);

        assertThat(post.getPostTags()).hasSize(1);
        assertThat(post.getPostTags().get(0).getPost()).isEqualTo(post);
        assertThat(post.getPostTags().get(0).getTag().getId()).isEqualTo(1L);
    }

    @Test
    void addPostTag_null이면_예외를_던진다() {
        Post post = createPost();

        assertThatThrownBy(() -> post.addPostTag(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("tag는 필수입니다.");
    }

    @Test
    void addPostTag_중복_태그는_무시한다() {
        Post post = createPost();
        Tag tag = mock(Tag.class);
        given(tag.getId()).willReturn(1L);

        post.addPostTag(tag);
        post.addPostTag(tag);

        assertThat(post.getPostTags()).hasSize(1);
    }

    @Test
    void addHashtag_정상_해시태그를_추가한다() {
        Post post = createPost();

        post.addHashtag("  브런치  ");

        assertThat(post.getHashtags()).containsExactly("브런치");
    }

    @Test
    void addHashtag_null이면_예외를_던진다() {
        Post post = createPost();

        assertThatThrownBy(() -> post.addHashtag(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("hashtag는 필수입니다.");
    }

    @Test
    void addHashtag_공백이면_예외를_던진다() {
        Post post = createPost();

        assertThatThrownBy(() -> post.addHashtag("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("hashtag는 필수입니다.");
    }

    @Test
    void addHashtag_중복_해시태그는_무시한다() {
        Post post = createPost();

        post.addHashtag("브런치");
        post.addHashtag("브런치");

        assertThat(post.getHashtags()).containsExactly("브런치");
    }

    private Post createPost() {
        return Post.builder()
                .memberId(1L)
                .title("제목")
                .content("내용")
                .category(PostCategory.FOOD)
                .build();
    }
}
