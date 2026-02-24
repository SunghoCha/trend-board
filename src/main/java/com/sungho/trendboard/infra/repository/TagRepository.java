package com.sungho.trendboard.infra.repository;

import com.sungho.trendboard.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
}
