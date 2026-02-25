package com.sungho.trendboard.application.post;

import com.sungho.trendboard.application.post.dto.CreatePostRequest;
import com.sungho.trendboard.application.post.dto.CreatePostResponse;
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

    private void validateAdvertiserRole(CurrentUser currentUser) {
        if (currentUser.role() != MemberRole.ADVERTISER) {
            log.info("게시글 등록 권한 없음: memberId={}, role={}", currentUser.memberId(), currentUser.role());
            throw new BusinessException(CommonErrorCode.FORBIDDEN);
        }
    }

    private void attachTags(Post post, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }

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

        tags.forEach(post::addPostTag);
    }
}
