package com.sungho.trendboard.infra.repository;

import com.sungho.trendboard.application.dto.PostListItem;

import java.util.List;

public interface PostRepositoryCustom {

    List<PostListItem> findPostList(long offset, int limit);
}
