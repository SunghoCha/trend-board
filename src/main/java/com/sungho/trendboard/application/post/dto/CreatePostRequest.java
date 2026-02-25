package com.sungho.trendboard.application.post.dto;

import com.sungho.trendboard.domain.PostCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreatePostRequest(
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 50, message = "제목은 50자 이하여야 합니다.")
        String title,

        @NotBlank(message = "내용은 필수입니다.")
        String content,

        @NotNull(message = "카테고리는 필수입니다.")
        PostCategory category,

        @Size(max = 10, message = "태그는 최대 10개까지 선택할 수 있습니다.")
        List<
                @NotNull(message = "태그 ID는 필수입니다.")
        @Positive(message = "태그 ID는 1 이상이어야 합니다.")
                        Long> tagIds,

        @Size(max = 10, message = "해시태그는 최대 10개까지 입력할 수 있습니다.")
        List<
                @NotBlank(message = "해시태그는 공백일 수 없습니다.")
                @Size(max = 50, message = "해시태그는 50자 이하여야 합니다.")
                        String> hashtags
) {
}
