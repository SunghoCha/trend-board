package com.sungho.trendboard.infra.repository;

import com.sungho.trendboard.api.dto.PostCursor;
import com.sungho.trendboard.application.dto.PostListItem;

import java.util.List;

public interface PostRepositoryCustom {

    List<PostListItem> findPostList(long offset, int limit);
    List<PostListItem> findPostListCoveringIdsThenIn(long offset, int limit);
    List<PostListItem> findPostListByCursor(PostCursor cursor, int limit);
}
