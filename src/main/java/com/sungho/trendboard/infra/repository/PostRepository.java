package com.sungho.trendboard.infra.repository;

import com.sungho.trendboard.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
