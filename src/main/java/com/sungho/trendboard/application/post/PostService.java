package com.sungho.trendboard.application.post;

import com.sungho.trendboard.application.post.dto.CreatePostRequest;
import com.sungho.trendboard.application.post.dto.CreatePostResponse;
import com.sungho.trendboard.application.post.dto.UpdatePostRequest;
import com.sungho.trendboard.application.post.dto.UpdatePostResponse;
import com.sungho.trendboard.domain.MemberRole;
import com.sungho.trendboard.domain.Post;
import com.sungho.trendboard.domain.Tag;
import com.sungho.trendboard.global.domain.CurrentUser;
import com.sungho.trendboard.global.exception.BusinessException;
import com.sungho.trendboard.global.exception.CommonErrorCode;
import com.sungho.trendboard.global.exception.PostErrorCode;
import com.sungho.trendboard.infra.repository.PostRepository;
import com.sungho.trendboard.infra.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;

    @Transactional
    public CreatePostResponse createPost(CurrentUser currentUser, CreatePostRequest request) {
        validateAdvertiserRole(currentUser);

        Post post = Post.builder()
                .memberId(currentUser.memberId())
                .title(request.title())
                .content(request.content())
                .category(request.category())
                .hashtags(request.hashtags())
                .build();

        attachTags(post, request.tagIds());

        Post saved = postRepository.save(post);
        return CreatePostResponse.from(saved);
    }

    @Transactional
    public UpdatePostResponse updatePost(Long postId, CurrentUser currentUser, UpdatePostRequest request) {
        validateAdvertiserRole(currentUser);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.info("게시글 없음: postId={}", postId);
                    return new BusinessException(PostErrorCode.POST_NOT_FOUND);
                });

        validatePostOwner(currentUser, post);

        post.update(request.title(), request.content(), request.category());
        // update는 연관 컬렉션을 부분 변경이 아닌 "전체 교체"로 처리한다.
        // tagIds/hashtags가 null 또는 empty이면 기존 값은 모두 제거된다.
        post.replacePostTags(findTagsByIds(request.tagIds()));
        post.replaceHashtags(request.hashtags());

        return UpdatePostResponse.from(post);
    }

    private void validateAdvertiserRole(CurrentUser currentUser) {
        if (currentUser.role() != MemberRole.ADVERTISER) {
            log.info("게시글 관리 권한 없음: memberId={}, role={}", currentUser.memberId(), currentUser.role());
            throw new BusinessException(CommonErrorCode.FORBIDDEN);
        }
    }

    private void attachTags(Post post, List<Long> tagIds) {
        findTagsByIds(tagIds).forEach(post::addPostTag);
    }

    private void validatePostOwner(CurrentUser currentUser, Post post) {
        if (!post.getMemberId().equals(currentUser.memberId())) {
            log.info("게시글 수정 권한 없음: memberId={}, postId={}, postMemberId={}",
                    currentUser.memberId(),
                    post.getId(),
                    post.getMemberId());
            throw new BusinessException(CommonErrorCode.FORBIDDEN);
        }
    }

    private List<Tag> findTagsByIds(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return List.of();
        }

        // 요청 입력의 중복을 제거해 조회/검증 기준을 일관되게 맞춘다.
        List<Long> distinctTagIds = tagIds.stream()
                .distinct()
                .toList();

        List<Tag> tags = tagRepository.findAllById(distinctTagIds);
        if (tags.size() != distinctTagIds.size()) {
            log.info("존재하지 않는 태그 포함: requestedTagIds={}, distinctTagIds={}, foundCount={}",
                    tagIds,
                    distinctTagIds,
                    tags.size());
            throw new BusinessException(PostErrorCode.TAG_NOT_FOUND);
        }
        return tags;
    }
}
