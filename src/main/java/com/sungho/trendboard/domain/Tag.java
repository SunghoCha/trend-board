package com.sungho.trendboard.domain;

import com.sungho.trendboard.global.domain.BaseTimeEntity;
import com.sungho.trendboard.global.util.SnowflakeId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "tag")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag extends BaseTimeEntity {

    @Id
    @SnowflakeId
    private Long id;

    @Column(name = "name", nullable = false, length = 50, unique = true)
    private String name;

    /**
     * Creates a Tag with the given name after validating and trimming it.
     *
     * @param name the tag name; leading and trailing whitespace will be removed
     * @throws IllegalArgumentException if name is null or blank
     */
    private Tag(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name은 필수입니다.");
        }
        this.name = name.trim();
    }

    /**
     * Create a Tag with the given name.
     *
     * @param name the tag name; must be non-null and not blank (leading/trailing whitespace is trimmed)
     * @return a new Tag with the provided name
     * @throws IllegalArgumentException if {@code name} is null or blank
     */
    public static Tag create(String name) {
        return new Tag(name);
    }
}
